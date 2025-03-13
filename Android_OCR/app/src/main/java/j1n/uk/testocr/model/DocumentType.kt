package j1n.uk.testocr.model

enum class DocumentType(
    val label: String,
    val ocrType: OcrType,
    val rectRatio: RectFPercentage
) {
    CARD(
        label = "카드",
        ocrType = OcrType.CARD,
        rectRatio = RectFPercentage(
            leftPercent = 0.075f,
            topPercent = 0.4f,
            rightPercent = 0.925f,
            bottomPercent = 0.6f
        )
    ),
    LICENSE(
        label = "운전면허증",
        ocrType = OcrType.LICENSE,
        rectRatio = RectFPercentage(
            leftPercent = 0.075f,
            topPercent = 0.3f,
            rightPercent = 0.925f,
            bottomPercent = 0.7f
        )
    )
}