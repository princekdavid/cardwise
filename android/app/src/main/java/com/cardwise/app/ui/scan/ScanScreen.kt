package com.cardwise.app.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.cardwise.app.domain.scan.UpiPaymentRequest
import com.cardwise.app.domain.scan.UpiQrParseResult
import com.cardwise.app.domain.scan.UpiQrParser
import com.cardwise.app.ui.theme.CardWiseMotion
import com.cardwise.app.ui.theme.CardWiseSpacing
import com.cardwise.app.ui.theme.GlassCard
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private sealed interface ScanState {
    data object Scanning : ScanState
    data object CameraError : ScanState
    data class Detected(val payment: UpiPaymentRequest) : ScanState
    data class Invalid(val reason: InvalidScanReason) : ScanState
}

private enum class InvalidScanReason { NOT_UPI, MALFORMED }

@Composable
fun ScanScreen(
    onPaymentDetected: (UpiPaymentRequest) -> Unit = {},
    onPaymentHandoffRequested: (UpiPaymentRequest) -> Unit = {}
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var state by remember { mutableStateOf<ScanState>(ScanState.Scanning) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        state = if (granted) ScanState.Scanning else ScanState.CameraError
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = hasPermission to state,
            transitionSpec = {
                fadeIn(animationSpec = tween(CardWiseMotion.contentTransitionMillis)) togetherWith
                    fadeOut(animationSpec = tween(CardWiseMotion.contentTransitionMillis))
            },
            label = "scan_state"
        ) { (permissionGranted, scanState) ->
            when {
                !permissionGranted -> PermissionContent { permissionLauncher.launch(Manifest.permission.CAMERA) }
                scanState is ScanState.Scanning -> CameraPreview { state = it }
                scanState is ScanState.Detected -> DetectedContent(
                    payment = scanState.payment,
                    onContinueToPayment = { onPaymentHandoffRequested(scanState.payment) },
                    onFindBestCard = { onPaymentDetected(scanState.payment) },
                    onScanAgain = { state = ScanState.Scanning }
                )
                scanState is ScanState.Invalid -> InvalidContent(
                    reason = scanState.reason,
                    onScanAgain = { state = ScanState.Scanning }
                )
                else -> CameraErrorContent(onRetry = {
                    hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    state = ScanState.Scanning
                })
            }
        }
    }
}

@Composable
private fun PermissionContent(onGrant: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(CardWiseSpacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Camera access needed", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        Text(
            "CardWise uses the camera only while you scan a payment QR. The QR payload is processed locally and is not stored.",
            modifier = Modifier.padding(top = CardWiseSpacing.sm),
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = onGrant, modifier = Modifier.padding(top = CardWiseSpacing.lg)) { Text("Allow camera") }
    }
}

@Composable
private fun CameraPreview(onResult: (ScanState) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        )
    }
    val handled = remember { AtomicBoolean(false) }
    val disposed = remember { AtomicBoolean(false) }
    val cameraProvider = remember { mutableStateOf<ProcessCameraProvider?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            disposed.set(true)
            cameraProvider.value?.unbindAll()
            scanner.close()
            executor.shutdown()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize().semantics {
            contentDescription = "Camera preview. Point the camera at a UPI payment QR code."
        },
        factory = { viewContext ->
            val previewView = PreviewView(viewContext)
            val providerFuture = ProcessCameraProvider.getInstance(viewContext)
            providerFuture.addListener({
                if (disposed.get()) return@addListener
                runCatching {
                    val provider = providerFuture.get()
                    if (disposed.get()) {
                        provider.unbindAll()
                        return@runCatching
                    }
                    cameraProvider.value = provider
                    val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    analysis.setAnalyzer(executor) { imageProxy ->
                        if (disposed.get() || handled.get()) {
                            imageProxy.close()
                            return@setAnalyzer
                        }
                        val mediaImage = imageProxy.image
                        if (mediaImage == null) {
                            imageProxy.close()
                            return@setAnalyzer
                        }
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                if (disposed.get() || handled.get()) return@addOnSuccessListener
                                val raw = barcodes.firstOrNull()?.rawValue ?: return@addOnSuccessListener
                                if (!handled.compareAndSet(false, true)) return@addOnSuccessListener
                                when (val parsed = UpiQrParser.parse(raw)) {
                                    is UpiQrParseResult.Success -> onResult(ScanState.Detected(parsed.payment))
                                    UpiQrParseResult.NotUpi -> onResult(ScanState.Invalid(InvalidScanReason.NOT_UPI))
                                    is UpiQrParseResult.Invalid -> onResult(ScanState.Invalid(InvalidScanReason.MALFORMED))
                                }
                            }
                            .addOnFailureListener {
                                if (!disposed.get() && handled.compareAndSet(false, true)) {
                                    onResult(ScanState.Invalid(InvalidScanReason.MALFORMED))
                                }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    }
                    if (disposed.get()) {
                        analysis.clearAnalyzer()
                        provider.unbindAll()
                        return@runCatching
                    }
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                }.onFailure {
                    if (!disposed.get()) onResult(ScanState.CameraError)
                }
            }, ContextCompat.getMainExecutor(viewContext))
            previewView
        },
        update = {}
    )
}

@Composable
private fun DetectedContent(
    payment: UpiPaymentRequest,
    onContinueToPayment: () -> Unit,
    onFindBestCard: () -> Unit,
    onScanAgain: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(CardWiseSpacing.lg),
        verticalArrangement = Arrangement.Center
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
            Column(modifier = Modifier.padding(CardWiseSpacing.lg)) {
                Text("UPI payment found", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                payment.merchantName?.let { Text(it, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = CardWiseSpacing.sm)) }
                Text(payment.vpa, modifier = Modifier.padding(top = CardWiseSpacing.xs))
                payment.amount?.let { Text("₹$it", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = CardWiseSpacing.md)) }
                payment.note?.let { Text(it, modifier = Modifier.padding(top = CardWiseSpacing.sm)) }
            }
        }
        Button(onClick = onFindBestCard, modifier = Modifier.fillMaxWidth().padding(top = CardWiseSpacing.md)) {
            Text("Find best card")
        }
        Button(onClick = onContinueToPayment, modifier = Modifier.fillMaxWidth().padding(top = CardWiseSpacing.sm)) {
            Text("Continue to payment")
        }
        Button(onClick = onScanAgain, modifier = Modifier.fillMaxWidth().padding(top = CardWiseSpacing.sm), contentPadding = PaddingValues(CardWiseSpacing.sm + CardWiseSpacing.xs)) {
            Text("Scan again")
        }
    }
}

@Composable
private fun InvalidContent(reason: InvalidScanReason, onScanAgain: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(CardWiseSpacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (reason == InvalidScanReason.NOT_UPI) "This QR isn't a UPI payment QR" else "This UPI QR can't be read safely",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() }
        )
        Text("Try another payment QR.", modifier = Modifier.padding(top = CardWiseSpacing.sm))
        Button(onClick = onScanAgain, modifier = Modifier.padding(top = CardWiseSpacing.lg)) { Text("Scan again") }
    }
}

@Composable
private fun CameraErrorContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(CardWiseSpacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Camera unavailable", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        Text("Check camera permission and try again.", modifier = Modifier.padding(top = CardWiseSpacing.sm))
        Button(onClick = onRetry, modifier = Modifier.padding(top = CardWiseSpacing.lg)) { Text("Try again") }
    }
}
