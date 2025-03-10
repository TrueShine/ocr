package j1n.uk.testocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import java.io.ByteArrayOutputStream

class TextAnalyzer(
    private val onTextFound: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(
        KoreanTextRecognizerOptions.Builder().build()
    )

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return imageProxy.close()
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val bitmap = toBitmap(mediaImage)  // 카메라 프레임을 Bitmap으로 변환

        // 1️⃣ 전체 이미지 크기
        val width = bitmap.width
        val height = bitmap.height

        // 2️⃣ 카드 가이드 영역 크기 설정 (카드 오버레이와 동일한 비율)
        val cardRatio = 1.586f // 카드 가로/세로 비율
        val cardWidth = (width * 0.85f).toInt()  // 화면의 85% 크기
        val cardHeight = (cardWidth / cardRatio).toInt()

        // 3️⃣ 카드 영역 위치 계산 (가운데 정렬)
        val left = ((width - cardWidth) / 2f).toInt()
        val top = ((height - cardHeight) / 2f).toInt()

        // 4️⃣ 카드 영역만 잘라서 OCR 수행
        val croppedBitmap = Bitmap.createBitmap(bitmap, left, top, cardWidth, cardHeight)
        val inputImage = InputImage.fromBitmap(croppedBitmap, rotationDegrees)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                onTextFound(visionText.text)  // 인식된 텍스트 UI에 전달
            }
            .addOnFailureListener { it.printStackTrace() }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun toBitmap(image: Image): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}