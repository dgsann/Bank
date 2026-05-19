package com.example.bank.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bank.logic.FinancialHealthEngine
import com.example.bank.ui.theme.Accent
import com.example.bank.ui.theme.BorderColor
import com.example.bank.ui.theme.SurfaceCard

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BorderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}

@Composable
fun AvatarRing(financialHealth: Int, modifier: Modifier = Modifier) {
    val clamped = financialHealth.coerceIn(0, 100)
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(150.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            drawArc(
                color = BorderColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = Accent,
                startAngle = -90f,
                sweepAngle = 360f * (clamped / 100f),
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        Text(text = FinancialHealthEngine.avatarEmoji(clamped), fontSize = 56.sp)
    }
}

@Composable
fun StatBar(label: String, value: Int, barColor: androidx.compose.ui.graphics.Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = com.example.bank.ui.theme.TextSecondary)
            Text("$value", fontSize = 12.sp, color = com.example.bank.ui.theme.TextPrimary)
        }
        androidx.compose.material3.LinearProgressIndicator(
            progress = { value.coerceIn(0, 100) / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = BorderColor
        )
    }
}
