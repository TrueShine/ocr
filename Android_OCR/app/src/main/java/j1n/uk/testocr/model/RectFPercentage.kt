package j1n.uk.testocr.model

import android.graphics.Rect
import android.graphics.RectF

/**
 * 화면 비율 기준으로 인식할 사각형 영역을 표현하는 클래스
 * (좌상단 ~ 우하단의 비율로 지정)
 */
data class RectFPercentage(
    val leftPercent: Float,
    val topPercent: Float,
    val rightPercent: Float,
    val bottomPercent: Float
) {
    /**
     * 사각형의 너비 비율
     */
    val widthPercent: Float
        get() = rightPercent - leftPercent

    /**
     * 사각형의 높이 비율
     */
    val heightPercent: Float
        get() = bottomPercent - topPercent

    /**
     * 중심점 Y 비율
     */
    val centerYPercent: Float
        get() = (topPercent + bottomPercent) / 2f

    /**
     * 중심점 X 비율
     */
    val centerXPercent: Float
        get() = (leftPercent + rightPercent) / 2f

    /**
     * 주어진 해상도에 맞춰 실제 픽셀 단위의 RectF로 변환
     */
    fun toRectF(imageWidth: Int, imageHeight: Int): RectF {
        return RectF(
            imageWidth * leftPercent,
            imageHeight * topPercent,
            imageWidth * rightPercent,
            imageHeight * bottomPercent
        )
    }

    /**
     * toRectF()에서 만들어진 RectF를 정수형 Rect로도 변환 가능
     */
    fun toRect(imageWidth: Int, imageHeight: Int): Rect {
        return Rect(
        (imageWidth * leftPercent).toInt(),
            (imageHeight * topPercent).toInt(),
            (imageWidth * rightPercent).toInt(),
            (imageHeight * bottomPercent).toInt()
        )
    }
}