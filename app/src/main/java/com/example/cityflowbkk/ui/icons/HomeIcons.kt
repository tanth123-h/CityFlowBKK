package com.example.cityflowbkk.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

enum class HomeIcon {
    Route,
    Train,
    Subway,
    Ticket,
    School,
    Search,
    Notification,
    Home,
    Map,
    Station,
    Profile,
    Warning,
    Back,
    Walk,
    Car,
    ChevronUp,
    ChevronDown,
}

@Composable
fun HomeIconGraphic(
    icon: HomeIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val finalModifier = modifier.size(24.dp)
    val semanticModifier = if (contentDescription == null) {
        finalModifier
    } else {
        finalModifier.semantics { this.contentDescription = contentDescription }
    }
    Canvas(modifier = semanticModifier) {
        val stroke = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)
        fun point(x: Float, y: Float) = Offset(size.width * x, size.height * y)

        when (icon) {
            HomeIcon.Search -> {
                drawCircle(tint, radius = size.minDimension * 0.27f, center = point(0.43f, 0.43f), style = stroke)
                drawLine(tint, point(0.64f, 0.64f), point(0.86f, 0.86f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Route -> {
                val path = Path().apply {
                    moveTo(size.width * 0.22f, size.height * 0.78f)
                    quadraticTo(size.width * 0.46f, size.height * 0.3f, size.width * 0.78f, size.height * 0.22f)
                }
                drawPath(path, tint, style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.12f, center = point(0.22f, 0.78f))
                drawCircle(tint, radius = size.minDimension * 0.12f, center = point(0.78f, 0.22f))
            }

            HomeIcon.Train, HomeIcon.Subway -> {
                drawRoundRect(
                    color = tint,
                    topLeft = point(0.23f, 0.14f),
                    size = Size(size.width * 0.54f, size.height * 0.58f),
                    cornerRadius = CornerRadius(size.minDimension * 0.12f),
                    style = stroke,
                )
                drawLine(tint, point(0.34f, 0.32f), point(0.66f, 0.32f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.34f, 0.52f), point(0.66f, 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.38f, 0.86f), point(0.48f, 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.62f, 0.86f), point(0.52f, 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Ticket -> {
                val path = Path().apply {
                    moveTo(size.width * 0.14f, size.height * 0.32f)
                    lineTo(size.width * 0.84f, size.height * 0.2f)
                    lineTo(size.width * 0.9f, size.height * 0.46f)
                    lineTo(size.width * 0.8f, size.height * 0.54f)
                    lineTo(size.width * 0.86f, size.height * 0.78f)
                    lineTo(size.width * 0.16f, size.height * 0.66f)
                    lineTo(size.width * 0.24f, size.height * 0.52f)
                    close()
                }
                drawPath(path, tint, style = stroke)
                drawLine(tint, point(0.48f, 0.28f), point(0.56f, 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.School -> {
                val cap = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.16f)
                    lineTo(size.width * 0.88f, size.height * 0.36f)
                    lineTo(size.width * 0.5f, size.height * 0.56f)
                    lineTo(size.width * 0.12f, size.height * 0.36f)
                    close()
                }
                drawPath(cap, tint)
                drawLine(tint, point(0.26f, 0.5f), point(0.26f, 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.26f, 0.7f), point(0.74f, 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.74f, 0.7f), point(0.74f, 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Notification -> {
                val path = Path().apply {
                    moveTo(size.width * 0.28f, size.height * 0.68f)
                    quadraticTo(size.width * 0.38f, size.height * 0.52f, size.width * 0.34f, size.height * 0.34f)
                    quadraticTo(size.width * 0.5f, size.height * 0.12f, size.width * 0.66f, size.height * 0.34f)
                    quadraticTo(size.width * 0.62f, size.height * 0.52f, size.width * 0.72f, size.height * 0.68f)
                    close()
                }
                drawPath(path, tint, style = stroke)
                drawLine(tint, point(0.42f, 0.82f), point(0.58f, 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Home -> {
                drawLine(tint, point(0.14f, 0.46f), point(0.5f, 0.18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.5f, 0.18f), point(0.86f, 0.46f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.25f, 0.42f), point(0.25f, 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.75f, 0.42f), point(0.75f, 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.25f, 0.84f), point(0.75f, 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Map -> {
                drawLine(tint, point(0.16f, 0.24f), point(0.36f, 0.16f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.36f, 0.16f), point(0.64f, 0.26f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.64f, 0.26f), point(0.84f, 0.18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.16f, 0.24f), point(0.16f, 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.36f, 0.16f), point(0.36f, 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.64f, 0.26f), point(0.64f, 0.86f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.84f, 0.18f), point(0.84f, 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.16f, 0.84f), point(0.36f, 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.36f, 0.76f), point(0.64f, 0.86f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.64f, 0.86f), point(0.84f, 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Station -> {
                drawCircle(tint, radius = size.minDimension * 0.27f, center = point(0.5f, 0.38f), style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.1f, center = point(0.5f, 0.38f))
                drawLine(tint, point(0.5f, 0.65f), point(0.5f, 0.9f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Profile -> {
                drawCircle(tint, radius = size.minDimension * 0.17f, center = point(0.5f, 0.32f), style = stroke)
                val shoulders = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.86f)
                    quadraticTo(size.width * 0.5f, size.height * 0.56f, size.width * 0.82f, size.height * 0.86f)
                }
                drawPath(shoulders, tint, style = stroke)
            }

            HomeIcon.Warning -> {
                val path = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.15f)
                    lineTo(size.width * 0.88f, size.height * 0.82f)
                    lineTo(size.width * 0.12f, size.height * 0.82f)
                    close()
                }
                drawPath(path, tint, style = stroke)
                drawLine(tint, point(0.5f, 0.42f), point(0.5f, 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(tint, radius = stroke.width * 0.6f, center = point(0.5f, 0.73f))
            }

            HomeIcon.Back -> {
                drawLine(tint, point(0.85f, 0.5f), point(0.15f, 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.42f, 0.23f), point(0.15f, 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.42f, 0.77f), point(0.15f, 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Walk -> {
                drawCircle(tint, radius = size.minDimension * 0.11f, center = point(0.55f, 0.18f))
                val body = Path().apply {
                    moveTo(size.width * 0.45f, size.height * 0.35f)
                    lineTo(size.width * 0.55f, size.height * 0.55f)
                    lineTo(size.width * 0.42f, size.height * 0.82f)
                }
                drawPath(body, tint, style = stroke)
                drawLine(tint, point(0.55f, 0.55f), point(0.72f, 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.45f, 0.35f), point(0.32f, 0.55f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.45f, 0.35f), point(0.68f, 0.35f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Car -> {
                drawRoundRect(
                    color = tint,
                    topLeft = point(0.15f, 0.42f),
                    size = Size(size.width * 0.7f, size.height * 0.35f),
                    cornerRadius = CornerRadius(size.minDimension * 0.08f),
                    style = stroke
                )
                val roof = Path().apply {
                    moveTo(size.width * 0.28f, size.height * 0.42f)
                    lineTo(size.width * 0.38f, size.height * 0.22f)
                    lineTo(size.width * 0.62f, size.height * 0.22f)
                    lineTo(size.width * 0.72f, size.height * 0.42f)
                }
                drawPath(roof, tint, style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.08f, center = point(0.28f, 0.77f), style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.08f, center = point(0.72f, 0.77f), style = stroke)
            }

            HomeIcon.ChevronUp -> {
                drawLine(tint, point(0.22f, 0.64f), point(0.5f, 0.36f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.78f, 0.64f), point(0.5f, 0.36f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.ChevronDown -> {
                drawLine(tint, point(0.22f, 0.36f), point(0.5f, 0.64f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.78f, 0.36f), point(0.5f, 0.64f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
        }
    }
}
