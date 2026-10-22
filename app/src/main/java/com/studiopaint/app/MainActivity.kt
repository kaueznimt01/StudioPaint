package com.studiopaint.app

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AppBg = Color(0xFF101116)
private val Panel = Color(0xFF1B1D25)
private val Accent = Color(0xFF8B5CF6)
private val Ink = Color(0xFFF3F4F6)

data class StrokeData(
    val points: List<Offset>,
    val color: Color,
    val width: Float
)

data class PaintLayer(
    val name: String,
    val strokes: List<StrokeData> = emptyList()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StudioPaintApp() }
    }
}

@Composable
fun StudioPaintApp() {
    val context = LocalContext.current
    val colors = listOf(
        Color.White, Color.Black, Color(0xFF60A5FA), Color(0xFF8B5CF6),
        Color(0xFFF472B6), Color(0xFFF87171), Color(0xFFFBBF24),
        Color(0xFF34D399), Color(0xFF22D3EE)
    )
    var selectedColor by remember { mutableStateOf(colors[0]) }
    var brushSize by remember { mutableFloatStateOf(8f) }
    var layers by remember { mutableStateOf(listOf(PaintLayer("Camada 1"))) }
    var activeLayer by remember { mutableIntStateOf(0) }
    var currentStroke by remember { mutableStateOf<StrokeData?>(null) }
    var undoStack by remember { mutableStateOf(listOf<List<PaintLayer>>()) }
    var redoStack by remember { mutableStateOf(listOf<List<PaintLayer>>()) }
    var showLayers by remember { mutableStateOf(false) }

    fun snapshot() {
        undoStack = undoStack + listOf(layers)
        if (undoStack.size > 30) undoStack = undoStack.drop(1)
        redoStack = emptyList()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack = redoStack + listOf(layers)
            layers = undoStack.last()
            undoStack = undoStack.dropLast(1)
            activeLayer = activeLayer.coerceIn(0, layers.lastIndex)
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack = undoStack + listOf(layers)
            layers = redoStack.last()
            redoStack = redoStack.dropLast(1)
            activeLayer = activeLayer.coerceIn(0, layers.lastIndex)
        }
    }

    fun exportPng() {
        try {
            val bitmap = Bitmap.createBitmap(1200, 1600, Bitmap.Config.ARGB_8888)
            val canvas = AndroidCanvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            val scaleX = 1200f / 360f
            val scaleY = 1600f / 480f
            layers.forEach { layer ->
                layer.strokes.forEach { stroke ->
                    if (stroke.points.isNotEmpty()) {
                        val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                            color = stroke.color.toArgb()
                            style = AndroidPaint.Style.STROKE
                            strokeWidth = stroke.width * scaleX
                            strokeCap = AndroidPaint.Cap.ROUND
                            strokeJoin = AndroidPaint.Join.ROUND
                        }
                        val path = AndroidPath()
                        path.moveTo(stroke.points.first().x * scaleX, stroke.points.first().y * scaleY)
                        stroke.points.drop(1).forEach { path.lineTo(it.x * scaleX, it.y * scaleY) }
                        canvas.drawPath(path, paint)
                    }
                }
            }
            val filename = "StudioPaint_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".png"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/StudioPaint")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                Toast.makeText(context, "PNG exportado para Pictures/StudioPaint", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Não foi possível exportar o PNG.", Toast.LENGTH_LONG).show()
            }
            bitmap.recycle()
        } catch (e: Exception) {
            Toast.makeText(context, "Falha ao exportar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            secondary = Color(0xFF60A5FA),
            background = AppBg,
            surface = Panel,
            onBackground = Ink,
            onSurface = Ink
        )
    ) {
        Column(
            Modifier.fillMaxSize().background(AppBg).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("StudioPaint", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("ESTÚDIO CRIATIVO  •  v0.1.0", color = Color(0xFF9CA3AF), fontSize = 10.sp, letterSpacing = 1.sp)
                }
                TextButton(onClick = { undo() }, enabled = undoStack.isNotEmpty()) { Text("↶") }
                TextButton(onClick = { redo() }, enabled = redoStack.isNotEmpty()) { Text("↷") }
                Button(onClick = { exportPng() }, contentPadding = PaddingValues(horizontal = 12.dp)) {
                    Text("Exportar PNG", fontSize = 12.sp)
                }
            }

            Row(
                Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("PINCEL", color = Color(0xFF9CA3AF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = brushSize, onValueChange = { brushSize = it },
                    valueRange = 2f..30f, modifier = Modifier.weight(1f)
                )
                Text("${brushSize.toInt()} px", color = Ink, fontSize = 12.sp)
            }

            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .background(Panel, RoundedCornerShape(14.dp)).padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("COR", color = Color(0xFF9CA3AF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                colors.forEach { color ->
                    Box(
                        Modifier.size(28.dp)
                            .background(color, RoundedCornerShape(50))
                            .then(if (selectedColor == color) Modifier.border(2.dp, Accent, RoundedCornerShape(50)) else Modifier)
                    ) {
                        Surface(
                            onClick = { selectedColor = color },
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(50)
                        ) {}
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("TELA DE DESENHO", color = Color(0xFF9CA3AF), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { showLayers = !showLayers }) {
                    Text(if (showLayers) "Ocultar camadas" else "Camadas (${layers.size})")
                }
                TextButton(onClick = {
                    snapshot()
                    layers = layers.toMutableList().also { it[activeLayer] = it[activeLayer].copy(strokes = emptyList()) }
                }) { Text("Limpar") }
            }

            if (showLayers) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        .background(Panel, RoundedCornerShape(12.dp)).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    layers.forEachIndexed { index, layer ->
                        FilterChip(
                            selected = activeLayer == index,
                            onClick = { activeLayer = index },
                            label = { Text(layer.name) }
                        )
                    }
                    Button(onClick = {
                        layers = layers + PaintLayer("Camada ${layers.size + 1}")
                        activeLayer = layers.lastIndex
                    }) { Text("+ Camada") }
                    TextButton(
                        enabled = layers.size > 1,
                        onClick = {
                            snapshot()
                            layers = layers.toMutableList().also { it.removeAt(activeLayer) }
                            activeLayer = activeLayer.coerceAtMost(layers.lastIndex)
                        }
                    ) { Text("Excluir") }
                }
            }

            Box(
                Modifier.weight(1f).fillMaxWidth().background(Color(0xFF262832), RoundedCornerShape(16.dp)).padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    Modifier.fillMaxSize().background(Color.White, RoundedCornerShape(8.dp))
                        .pointerInput(activeLayer, selectedColor, brushSize, layers) {
                            detectDragGestures(
                                onDragStart = { start ->
                                    snapshot()
                                    val normalized = Offset(
                                        start.x / size.width * 360f,
                                        start.y / size.height * 480f
                                    )
                                    currentStroke = StrokeData(listOf(normalized), selectedColor, brushSize)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val normalized = Offset(
                                        change.position.x / size.width * 360f,
                                        change.position.y / size.height * 480f
                                    )
                                    currentStroke = currentStroke?.let { it.copy(points = it.points + normalized) }
                                },
                                onDragEnd = {
                                    currentStroke?.let { stroke ->
                                        layers = layers.toMutableList().also { list ->
                                            list[activeLayer] = list[activeLayer].copy(strokes = list[activeLayer].strokes + stroke)
                                        }
                                    }
                                    currentStroke = null
                                },
                                onDragCancel = { currentStroke = null }
                            )
                        }
                ) {
                    val sx = size.width / 360f
                    val sy = size.height / 480f
                    fun drawStroke(stroke: StrokeData) {
                        if (stroke.points.isEmpty()) return
                        val path = Path().apply {
                            moveTo(stroke.points.first().x * sx, stroke.points.first().y * sy)
                            stroke.points.drop(1).forEach { lineTo(it.x * sx, it.y * sy) }
                        }
                        drawPath(
                            path = path,
                            color = stroke.color,
                            style = Stroke(
                                width = stroke.width * sx,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                        if (stroke.points.size == 1) {
                            drawCircle(stroke.color, radius = stroke.width * sx / 2f,
                                center = Offset(stroke.points[0].x * sx, stroke.points[0].y * sy))
                        }
                    }
                    layers.forEach { layer -> layer.strokes.forEach { drawStroke(it) } }
                    currentStroke?.let { drawStroke(it) }
                }
            }

            Text(
                "Dica: desenhe com o dedo. Crie camadas para separar elementos e exporte sua arte em PNG.",
                color = Color(0xFF9CA3AF), fontSize = 11.sp
            )
        }
    }
}
