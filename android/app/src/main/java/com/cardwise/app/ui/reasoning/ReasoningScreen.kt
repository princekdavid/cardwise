package com.cardwise.app.ui.reasoning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardwise.app.domain.recommendation.RecommendationTraceStep
import com.cardwise.app.domain.recommendation.RecommendationTraceStatus
import com.cardwise.app.domain.scan.UpiPaymentRequest
import com.cardwise.app.ui.recommendation.RecommendationUiState
import com.cardwise.app.ui.recommendation.RecommendationViewModel
import com.cardwise.app.ui.theme.CardWiseMotion
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.CardWiseSpacing
import com.cardwise.app.ui.theme.GlassCard

@Composable
fun ReasoningScreen(
    viewModel: RecommendationViewModel,
    payment: UpiPaymentRequest?,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var visibleSteps by remember { mutableIntStateOf(0) }
    val ready = state as? RecommendationUiState.Ready
    val steps = ready?.trace?.steps.orEmpty()

    LaunchedEffect(steps) {
        if (steps.isEmpty()) return@LaunchedEffect
        visibleSteps = 0
        steps.indices.forEach { index ->
            visibleSteps = index + 1
            kotlinx.coroutines.delay(260)
        }
        kotlinx.coroutines.delay(180)
        onComplete()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(CardWiseSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm + CardWiseSpacing.xs)
    ) {
        item {
            Column(
                Modifier.fillMaxWidth().semantics { heading() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)
            ) {
                ReasoningCore(Modifier.padding(top = CardWiseSpacing.md))
                Text("Synthesizing Optimal Route", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    if (payment?.merchantName != null) "Cross-referencing your local card rules for ${payment.merchantName}."
                    else "Cross-referencing your local card rules and payment context.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            when (state) {
                is RecommendationUiState.Loading -> ReasoningLoader("Preparing your local payment route…")
                is RecommendationUiState.Error -> ReasoningLoader((state as RecommendationUiState.Error).message, spinning = false)
                is RecommendationUiState.Empty -> ReasoningLoader("There is not enough eligible data to produce a route.", spinning = false)
                is RecommendationUiState.Ready -> Unit
            }
        }

        if (state is RecommendationUiState.Ready) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                    Column(Modifier.padding(CardWiseSpacing.md), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("DECISION PIPELINE", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                            Text("LOCAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
                            steps.take(visibleSteps).forEach { step -> ReasoningStep(step) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReasoningCore(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "reasoning_orbit")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(CardWiseMotion.enginePulseMillis * 3, easing = FastOutSlowInEasing)),
        label = "reasoning_orbit_rotation"
    )

    Box(modifier.size(112.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(112.dp).rotate(rotation).border(
                1.dp,
                CardWisePalette.Emerald.copy(alpha = 0.28f),
                RoundedCornerShape(56.dp)
            )
        )
        Surface(
            modifier = Modifier.size(80.dp).border(1.dp, CardWisePalette.Emerald.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 4.dp,
            shadowElevation = 10.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("✦", style = MaterialTheme.typography.headlineSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                Text("SYNTHESIZING", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ReasoningLoader(message: String, spinning: Boolean = true) {
    GlassCard(elevated = true) {
        Row(Modifier.padding(CardWiseSpacing.md), horizontalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            if (spinning) CircularProgressIndicator(strokeWidth = 3.dp)
            Column {
                Text(message, fontWeight = FontWeight.Bold)
                Text("No credentials or PINs are involved.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ReasoningStep(step: RecommendationTraceStep) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(CardWiseMotion.contentTransitionMillis)) + slideInVertically(tween(CardWiseMotion.cardEnterMillis)) { it / 5 }
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = CardWiseSpacing.xs), horizontalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm), verticalAlignment = Alignment.Top) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (step.status == RecommendationTraceStatus.CURRENT) CardWisePalette.Emerald.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✓", color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(step.title, fontWeight = FontWeight.Bold)
                Text(step.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (step.status == RecommendationTraceStatus.CURRENT) {
                Text("ACTIVE", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
            }
        }
    }
}
