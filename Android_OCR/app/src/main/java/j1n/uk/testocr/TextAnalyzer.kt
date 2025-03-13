package j1n.uk.testocr

import android.annotation.SuppressLint
import android.graphics.*
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import j1n.uk.testocr.model.OcrType
import j1n.uk.testocr.model.RectFPercentage
import java.io.ByteArrayOutputStream

class TextAnalyzer(
    private val type: OcrType,
    private val cardRectRatio: RectFPercentage,
    private val onTextFound: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val bitmap = imageProxyToBitmap(imageProxy)

        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val absoluteRect = RectF(
            cardRectRatio.leftPercent * width,
            cardRectRatio.topPercent * height,
            cardRectRatio.rightPercent * width,
            cardRectRatio.bottomPercent * height
        )

        val croppedBitmap = cropToRectFPercentage(bitmap, absoluteRect)
        val croppedInputImage = InputImage.fromBitmap(croppedBitmap, 0)

        recognizer.process(croppedInputImage)
            .addOnSuccessListener { visionText ->
                val text = visionText.text
                if (text.isNotBlank()) {
                    Log.d("TextAnalyzer", "OCR result:\n$text")
                    onTextFound(text)
                }
            }
            .addOnFailureListener { e ->
                Log.e("TextAnalyzer", "Text recognition failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val imageBytes = out.toByteArray()

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun cropToRectFPercentage(bitmap: Bitmap, rectF: RectF): Bitmap {
        val left = rectF.left.toInt().coerceAtLeast(0)
        val top = rectF.top.toInt().coerceAtLeast(0)
        val width = (rectF.width()).toInt().coerceAtMost(bitmap.width - left)
        val height = (rectF.height()).toInt().coerceAtMost(bitmap.height - top)
        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }
}