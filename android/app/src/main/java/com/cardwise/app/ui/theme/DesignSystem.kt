package com.cardwise.app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cardwise.app.domain.model.Card

object CardWisePalette {
    val Obsidian = Color(0xFF080B11)
    val Ink = Color(0xFF0D121B)
    val Slate = Color(0xFF1A2230)
    val Pearl = Color(0xFFF8FAFC)
    val Emerald = Color(0xFF10B981)
    val Teal = Color(0xFF14B8A6)
    val Sky = Color(0xFF38BDF8)
    val Muted = Color(0xFF94A3B8)
    val Line = Color(0xFF263244)
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f), RoundedCornerShape(20.dp)),
        color = if (elevated) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = if (elevated) 3.dp else 0.dp,
        shadowElevation = if (elevated) 10.dp else 0.dp
    ) { content() }
}

@Composable
fun SectionTitle(
    title: String,
    eyebrow: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        eyebrow?.let {
            Text(it.uppercase(), style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
        }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MetricTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EnginePulse(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "engine_pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "engine_alpha"
    )
    Box(modifier = modifier.size(9.dp).clip(RoundedCornerShape(50)).background(CardWisePalette.Emerald.copy(alpha = alpha)))
}

@Composable
fun DecisionPulse(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "decision_pulse")
    val scale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "decision_scale"
    )
    Box(
        modifier = modifier
            .size(74.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.radialGradient(listOf(CardWisePalette.Emerald.copy(alpha = 0.24f), Color.Transparent)))
    )
}

@Composable
fun PhysicalCard(
    card: Card,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    skin: CardSkin = cardSkinFor(card.id.hashCode())
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 126.dp else 178.dp)
            .clip(RoundedCornerShape(22.dp))
            .shadow(if (compact) 7.dp else 14.dp, RoundedCornerShape(22.dp))
            .background(skin.gradient)
            .padding(if (compact) 14.dp else 18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(card.issuer.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f), fontWeight = FontWeight.Bold)
            Text("◉", color = skin.chip, style = MaterialTheme.typography.titleMedium)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(card.name, color = Color.White, style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("•••• •••• •••• ${card.lastFour}", color = Color.White.copy(alpha = 0.86f), style = MaterialTheme.typography.labelMedium)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(card.network.name, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
            Text(if (card.isActive) "ACTIVE" else "PAUSED", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}
