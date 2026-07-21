package com.nutritrack.app.ui.screens.addfood

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@Composable
fun LabelScanTabContent(
    isProcessing: Boolean,
    onCaptured: () -> Unit,
    onTextRecognized: (String) -> Unit,
    onRecognitionFailed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (hasCameraPermission) {
        LabelCameraCapture(
            isProcessing = isProcessing,
            onCaptured = onCaptured,
            onTextRecognized = onTextRecognized,
            onRecognitionFailed = onRecognitionFailed,
            modifier = modifier.fillMaxSize(),
        )
    } else {
        LabelCameraPermissionRationale(
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            modifier = modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun LabelCameraPermissionRationale(onRequestPermission: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Camera access is needed to scan a nutrition label.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onRequestPermission, modifier = Modifier.padding(top = 12.dp)) {
                Text("Grant Camera Permission")
            }
        }
    }
}

@Composable
private fun LabelCameraCapture(
    isProcessing: Boolean,
    onCaptured: () -> Unit,
    onTextRecognized: (String) -> Unit,
    onRecognitionFailed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx -> PreviewView(ctx) },
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val capture = ImageCapture.Builder().build()
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture,
                        )
                        imageCapture = capture
                    },
                    ContextCompat.getMainExecutor(context),
                )
            },
        )

        ScanOverlayBanner(
            "Frame the nutrition facts table, then tap Capture.",
            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
        )

        Button(
            onClick = {
                val capture = imageCapture ?: return@Button
                onCaptured()
                capture.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            recognizeText(image, onTextRecognized, onRecognitionFailed)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            onRecognitionFailed()
                        }
                    },
                )
            },
            enabled = !isProcessing && imageCapture != null,
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            }
            Text(if (isProcessing) "Reading..." else "Capture")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ProcessCameraProvider.getInstance(context).get().unbindAll()
        }
    }
}

@Composable
private fun ScanOverlayBanner(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

// Runs on the in-memory captured frame directly - no need to persist the photo to disk since it's
// only ever used for this one-shot OCR pass.
private fun recognizeText(imageProxy: ImageProxy, onResult: (String) -> Unit, onFailed: () -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        onFailed()
        return
    }
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    recognizer.process(image)
        .addOnSuccessListener { visionText -> onResult(visionText.text) }
        .addOnFailureListener { onFailed() }
        .addOnCompleteListener { imageProxy.close() }
}
