package com.inventory.scanner.ui

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun QRScannerScreen(
    onBarcodeScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(android.util.Size(1920, 1080)) 
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setImageQueueDepth(1) 
                        .build()

                    
                    val options = com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS
                        )
                        .build()
                    val barcodeScanner = BarcodeScanning.getClient(options)

                    imageAnalysis.setAnalyzer(
                        Executors.newSingleThreadExecutor()
                    ) { imageProxy ->
                        
                        if (scannedCode != null) {
                            imageProxy.close()
                            return@setAnalyzer
                        }
                        
                        if (!isProcessing) {
                            isProcessing = true
                            processImageProxy(barcodeScanner, imageProxy) { barcode ->
                                if (barcode != null && scannedCode == null) {
                                    scannedCode = barcode
                                    
                                    
                                    val vibrator = ctx.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                                    } else {
                                        @Suppress("DEPRECATION")
                                        vibrator.vibrate(200)
                                    }
                                    android.util.Log.d("BarcodeScanner", "📳 Vibration feedback + CODE FOUND!")
                                }
                                isProcessing = false
                            }
                        } else {
                            imageProxy.close()
                        }
                    }

                    
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        
                        
                        previewView.setOnTouchListener { _, event ->
                            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                                val factory = previewView.meteringPointFactory
                                val point = factory.createPoint(event.x, event.y)
                                val action = androidx.camera.core.FocusMeteringAction.Builder(point).build()
                                camera.cameraControl.startFocusAndMetering(action)
                                android.util.Log.d("BarcodeScanner", "📸 Tap to focus triggered")
                            }
                            true
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("BarcodeScanner", "❌ Camera error: ${e.message}")
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scan QR/Barcode",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onClose) {
                        Text("Close")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📸 FRAME THE CODE",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Keep the phone steady • Good lighting",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val strokeWidth = 8f
                    val cornerLength = 80f
                    
                    
                    
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.Red,
                        start = androidx.compose.ui.geometry.Offset(0f, cornerLength),
                        end = androidx.compose.ui.geometry.Offset(0f, 0f),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.Red,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(cornerLength, 0f),
                        strokeWidth = strokeWidth
                    )
                    
                    
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.Red,
                        start = androidx.compose.ui.geometry.Offset(size.width - cornerLength, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.Red,
                        start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, cornerLength),
                        strokeWidth = strokeWidth
                    )
                    
                    
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.Red,
                        start = androidx.compose.ui.geometry.Offset(0f, size.height - cornerLength),
                        end = androidx.compose.ui.geometry.Offset(0f, size.height),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.Red,
                        start = androidx.compose.ui.geometry.Offset(0f, size.height),
                        end = androidx.compose.ui.geometry.Offset(cornerLength, size.height),
                        strokeWidth = strokeWidth
                    )
                    
                    
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.Red,
                        start = androidx.compose.ui.geometry.Offset(size.width - cornerLength, size.height),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.Red,
                        start = androidx.compose.ui.geometry.Offset(size.width, size.height - cornerLength),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = strokeWidth
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            
            if (scannedCode != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✅ Scanned code:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = scannedCode!!,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onBarcodeScanned(scannedCode!!)
                                onClose()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirmă și continuă")
                        }
                        TextButton(
                            onClick = {
                                scannedCode = null
                                isProcessing = false
                            }
                        ) {
                            Text("Scan again")
                        }
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onBarcodeDetected: (String?) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    android.util.Log.d("BarcodeScanner", "🔍 Found ${barcodes.size} barcode(s)")
                    
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { value ->
                            if (value.isNotEmpty()) {
                                
                                val format = when(barcode.format) {
                                    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13 -> "EAN-13"
                                    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8 -> "EAN-8"
                                    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A -> "UPC-A"
                                    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E -> "UPC-E"
                                    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128 -> "CODE-128"
                                    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39 -> "CODE-39"
                                    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE -> "QR"
                                    else -> "OTHER"
                                }
                                android.util.Log.d("BarcodeScanner", "✅ DETECTED: $value | Format: $format")
                                onBarcodeDetected(value)
                                return@addOnSuccessListener
                            }
                        }
                    }
                } else {
                    
                    android.util.Log.v("BarcodeScanner", "⏳ Scanning... no barcodes found in frame")
                }
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("BarcodeScanner", "❌ Scan failed: ${exception.message}")
                exception.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
