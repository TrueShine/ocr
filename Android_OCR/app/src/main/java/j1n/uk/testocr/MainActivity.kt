package j1n.uk.testocr

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.camera.core.Preview

import j1n.uk.testocr.ui.theme.TestOCRTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestOCRTheme {
                val context = LocalContext.current
                val packageName = context.packageName

                val cameraPermissionState = remember { mutableStateOf(false) }
                var showSettingsPrompt by remember { mutableStateOf(false) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) {
                        cameraPermissionState.value = true
                        showSettingsPrompt = false
                    } else {
                        val shouldShowRationale = shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                        showSettingsPrompt = !shouldShowRationale
                    }
                }

                LaunchedEffect(Unit) {
                    val isGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    if (isGranted) {
                        cameraPermissionState.value = true
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (cameraPermissionState.value) {
                        FullScreenCard(
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        if (showSettingsPrompt) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("카메라 권한이 필요합니다.\n설정에서 수동으로 허용해주세요.", lineHeight = 20.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", packageName, null)
                                        }
                                        context.startActivity(intent)
                                    }) {
                                        Text("설정으로 이동")
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("카메라 권한이 필요합니다.")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenCard(modifier: Modifier = Modifier) {
    var detectedText by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onTextDetected = { detectedText = it }
        )
        CardOverlay(modifier = Modifier.fillMaxSize())

        if (detectedText.isNotBlank()) {
            Text(
                text = detectedText,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onTextDetected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalyzer.setAnalyzer(
                    ContextCompat.getMainExecutor(ctx),
                    TextAnalyzer { recognizedText ->
                        Log.d("OCR", "Detected text: $recognizedText")
                        onTextDetected(recognizedText)
                    }
                )

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

@Composable
fun CardOverlay(
    modifier: Modifier = Modifier,
    cornerRadiusDp: Dp = 12.dp,
    cardRatio: Float = 1.586f
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val cardWidth = canvasWidth * 0.85f
        val cardHeight = cardWidth / cardRatio
        val cardLeft = (canvasWidth - cardWidth) / 2f
        val cardTop = (canvasHeight - cardHeight) / 2f

        drawRect(
            color = Color.Black.copy(alpha = 0.6f),
            size = size
        )

        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(cardLeft, cardTop),
            size = Size(cardWidth, cardHeight),
            cornerRadius = CornerRadius(cornerRadiusDp.toPx(), cornerRadiusDp.toPx()),
            blendMode = BlendMode.Clear
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(cardLeft, cardTop),
            size = Size(cardWidth, cardHeight),
            cornerRadius = CornerRadius(cornerRadiusDp.toPx(), cornerRadiusDp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

// @Preview(showBackground = true)
@Composable
fun FullScreenCardPreview() {
    TestOCRTheme {
        FullScreenCard(modifier = Modifier)
    }
}