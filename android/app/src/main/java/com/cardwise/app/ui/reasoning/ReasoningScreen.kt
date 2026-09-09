package com.cardwise.app.ui.reasoning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.CardWiseSpacing
import com.cardwise.app.ui.theme.GlassCard

@Composable
fun ReasoningScreen(viewModel: RecommendationViewModel, payment: UpiPaymentRequest?, onComplete: () -> Unit, modifier: Modifier = Modifier) {
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

    LazyColumn(modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(CardWiseSpacing.lg), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm + CardWiseSpacing.xs)) {
        item {
            Column(Modifier.fillMaxWidth().semantics { heading() }) {
                Text("DECISION TRACE", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                Text("Synthesizing optimal route", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    if (payment?.merchantName != null) "Evaluating ${payment.merchantName} using your local card rules." else "Evaluating your payment context using your local card rules.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = CardWiseSpacing.xs)
                )
            }
        }
        when (state) {
            is RecommendationUiState.Loading -> item { ReasoningLoader("Loading cards and reward rules…") }
            is RecommendationUiState.Error -> item { ReasoningLoader((state as RecommendationUiState.Error).message, spinning = false) }
            is RecommendationUiState.Empty -> item { ReasoningLoader("There is not enough eligible data to produce a route.", spinning = false) }
            is RecommendationUiState.Ready -> items(steps.take(visibleSteps), key = { it.id }) { step -> ReasoningStep(step) }
        }
    }
}

@Composable
private fun ReasoningLoader(message: String, spinning: Boolean = true) {
    GlassCard(elevated = true) {
        Row(Modifier.padding(CardWiseSpacing.md), horizontalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm)) {
            if (spinning) CircularProgressIndicator(strokeWidth = 3.dp)
            Column { Text(message, fontWeight = FontWeight.Bold); Text("No credentials or PINs are involved.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun ReasoningStep(step: RecommendationTraceStep) {
    AnimatedVisibility(visible = true, enter = fadeIn()) {
        GlassCard(elevated = step.status == RecommendationTraceStatus.CURRENT) {
            Column(Modifier.padding(CardWiseSpacing.md), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
                Text("✓  ${step.title}", fontWeight = FontWeight.Bold, color = CardWisePalette.Emerald)
                Text(step.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
