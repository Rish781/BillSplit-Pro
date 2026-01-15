package com.billsplitpro

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

@Composable
fun PieChart(
    data: Map<String, Double>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum()
    
    // FIX: Converted the result to Float explicitly so Canvas accepts it
    val angles = data.values.map { (it / total).toFloat() * 360f }
    
    var startAngle = -90f // Start at the top (12 o'clock)

    Canvas(modifier = modifier) {
        val strokeWidth = 80f // Thickness of the donut

        angles.forEachIndexed { index, sweepAngle ->
            val color = colors.getOrElse(index) { Color.Gray }
            
            // Draw the slice
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true, 
                size = Size(size.width, size.height),
                topLeft = Offset(0f, 0f)
            )
            
            startAngle += sweepAngle
        }
    }
}