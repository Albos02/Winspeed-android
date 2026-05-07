package com.winspeed.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp

@Composable
fun WinspeedLogo(modifier: Modifier = Modifier) {
    val logoColor = MaterialTheme.colorScheme.onBackground
    Canvas(modifier = modifier.size(200.dp)) {
        val scale = size.width / 200f
        
        // Background Rect removed to support transparency as per latest SVG style
        
        val clipPath = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    0f, 0f, 200f * scale, 200f * scale,
                    CornerRadius(44f * scale, 44f * scale)
                )
            )
        }
        
        clipPath(clipPath) {
            // Swoosh 1 (M268,110 Q310,48 385,75 Q340,68 310,105 Z) -> subtract (240, 20)
            // M28,90 Q70,28 145,55 Q100,48 70,85 Z
            val path1 = Path().apply {
                moveTo(28f * scale, 90f * scale)
                quadraticTo(70f * scale, 28f * scale, 145f * scale, 55f * scale)
                quadraticTo(100f * scale, 48f * scale, 70f * scale, 85f * scale)
                close()
            }
            drawPath(path1, logoColor)
            
            // Swoosh 2 (M290,118 Q330,62 400,85 Q358,76 328,112 Z) -> subtract (240, 20)
            // M50,98 Q90,42 160,65 Q118,56 88,92 Z
            val path2 = Path().apply {
                moveTo(50f * scale, 98f * scale)
                quadraticTo(90f * scale, 42f * scale, 160f * scale, 65f * scale)
                quadraticTo(118f * scale, 56f * scale, 88f * scale, 92f * scale)
                close()
            }
            drawPath(path2, logoColor.copy(alpha = 0.35f))
            
            // Top line: (258, 153, 140, 2.5) -> subtract (240, 20)
            // (18, 133, 140, 2.5)
            drawRoundRect(
                color = logoColor,
                topLeft = Offset(18f * scale, 133f * scale),
                size = Size(140f * scale, 2.5f * scale),
                cornerRadius = CornerRadius(1.25f * scale, 1.25f * scale)
            )
            
            // Accent line: (310, 167, 88, 3) -> subtract (240, 20)
            // (70, 147, 88, 3)
            drawRoundRect(
                color = Color(0xFF00CFFF),
                topLeft = Offset(70f * scale, 147f * scale),
                size = Size(88f * scale, 3f * scale),
                cornerRadius = CornerRadius(1f * scale, 1f * scale),
                alpha = 0.85f
            )
            
            // Bottom line: (348, 179, 50, 2) -> subtract (240, 20)
            // (108, 159, 50, 2)
            drawRoundRect(
                color = logoColor,
                topLeft = Offset(108f * scale, 159f * scale),
                size = Size(50f * scale, 2.5f * scale),
                cornerRadius = CornerRadius(1f * scale, 1f * scale),
                alpha = 0.3f
            )
        }
    }
}
