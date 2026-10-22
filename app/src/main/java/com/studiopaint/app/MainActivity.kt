package com.studiopaint.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PaintLine(
    val points: List<Offset>,
    val color: Color,
    val width: Float
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudioPaintApp()
        }
    }
}

@Composable
fun StudioPaintApp() {
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var brushSize by remember { mutableFloatStateOf(8f) }
    val lines = remember { mutableStateListOf<PaintLine>() }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F3F8))
                .padding(12.dp)
        ) {
            Text(
                text = "StudioPaint",
                fontSize = 28.sp,
                color = Color(0xFF6250D8)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Color.Black,
                    Color.Red,
                    Color.Blue,
                    Color(0xFF8A2BE2),
                    Color.Green,
                    Color(0xFFFF9800)
                ).forEach { color ->
                    Button(
                        onClick = { selectedColor = color },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(42.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = color
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Tamanho do pincel: ${brushSize.toInt()}")

            Slider(
                value = brushSize,
                onValueChange = { brushSize = it },
                valueRange = 2f..30f
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { lines.clear() }) {
                    Text("Limpar tela")
                }

                Button(onClick = { if (lines.isNotEmpty()) lines.removeAt(lines.lastIndex) }) {
                    Text("Desfazer")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .pointerInput(selectedColor, brushSize) {
                        detectDragGestures(
                            onDragStart = { start ->
                                currentPoints = listOf(start)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPoints = currentPoints + change.position
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    lines.add(
                                        PaintLine(
                                            currentPoints,
                                            selectedColor,
                                            brushSize
                                        )
                                    )
                                }
                                currentPoints = emptyList()
                            }
                        )
                    }
            ) {
                fun drawLine(points: List<Offset>, color: Color, width: Float) {
                    if (points.isEmpty()) return

                    if (points.size == 1) {
                        drawCircle(
                            color = color,
                            radius = width / 2,
                            center = points.first()
                        )
                    } else {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            points.drop(1).forEach {
                                lineTo(it.x, it.y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = color,
                            style = Stroke(width = width)
                        )
                    }
                }

                lines.forEach {
                    drawLine(it.points, it.color, it.width)
                }

                drawLine(currentPoints, selectedColor, brushSize)
            }
        }
    }
}
