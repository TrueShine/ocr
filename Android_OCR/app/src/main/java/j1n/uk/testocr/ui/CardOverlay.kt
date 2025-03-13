package j1n.uk.testocr.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun CardOverlay(
    modifier: Modifier = Modifier,
    cornerRadiusDp: Dp = 12.dp,
    cardRatio: Float = 1.586f,
    recognizedText: String = ""
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val cardWidth = canvasWidth * 0.85f
        val cardHeight = cardWidth / cardRatio
        val cardLeft = (canvasWidth - cardWidth) / 2f
        val cardTop = (canvasHeight - cardHeight) / 2f

        val cardRect = Rect(
            offset = Offset(cardLeft, cardTop),
            size = Size(cardWidth, cardHeight)
        )

        // 배경 어둡게
        drawRect(
            color = Color.Black.copy(alpha = 0.6f),
            size = size
        )

        // 기존 카드 모양 레이아웃을 다시 추가합니다.
        // 가운데 투명 카드 영역 (잘 보이도록 BlendMode.Clear 사용)
        drawRoundRect(
            color = Color.Transparent,
            topLeft = cardRect.topLeft,
            size = cardRect.size,
            cornerRadius = CornerRadius(cornerRadiusDp.toPx(), cornerRadiusDp.toPx()),
            blendMode = BlendMode.Clear
        )

        // 카드 영역 빨간 테두리
        drawRoundRect(
            color = Color.Red.copy(alpha = 0.8f),
            topLeft = cardRect.topLeft,
            size = cardRect.size,
            cornerRadius = CornerRadius(cornerRadiusDp.toPx(), cornerRadiusDp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )

        // 카드 영역 아래 OCR 인식 텍스트 영역 가이드 선 (디버깅용)
        drawLine(
            color = Color.Yellow,
            start = Offset(cardLeft, cardTop + cardHeight + 8.dp.toPx()),
            end = Offset(cardLeft + cardWidth, cardTop + cardHeight + 8.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
        // 여러 줄 텍스트 표시
        val textLines = recognizedText.split("\n")
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.YELLOW
            textSize = 24.dp.toPx()
            isAntiAlias = true
        }
        textLines.forEachIndexed { index, line ->
            drawContext.canvas.nativeCanvas.drawText(
                line,
                cardLeft,
                cardTop + cardHeight + 32.dp.toPx() + index * 28.dp.toPx(),
                paint
            )
        }
    }
}