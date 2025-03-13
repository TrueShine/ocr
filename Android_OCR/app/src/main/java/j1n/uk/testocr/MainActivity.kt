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
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import j1n.uk.testocr.model.DocumentType
import j1n.uk.testocr.ui.CameraPreview
import j1n.uk.testocr.ui.CardOverlay
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
                        FullScreenOCR(modifier = Modifier.padding(innerPadding))
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
fun FullScreenOCR(modifier: Modifier = Modifier) {
    var detectedText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DocumentType.CARD) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DocumentType.values().forEach { type ->
                Button(
                    onClick = { selectedType = type },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(type.label)
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            CameraPreview(
                type = selectedType.ocrType,
                cardRectRatio = selectedType.rectRatio,
                onTextFound = { detectedText = it }
            )

            CardOverlay(
                modifier = Modifier.fillMaxSize()
            )
            if (detectedText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .fillMaxWidth()
                ) {
                    Text(
                        text = detectedText,
                        color = Color.White,
                        fontSize = if (detectedText.length > 200) 12.sp else 16.sp,
                        modifier = Modifier.padding(8.dp),
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

        }
    }
}