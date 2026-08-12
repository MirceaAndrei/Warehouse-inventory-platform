package com.inventory.scanner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
fun SimpleBarcodeScanner(
    onBarcodeScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var detectedBarcode by remember { mutableStateOf<String?>(null) }
    var scanningActive by remember { mutableStateOf(false) } 
    
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000) 
        scanningActive = true
        Log.d("SimpleScanner", "📸 Scanare activată după 2 secunde!")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    
                    val preview = Preview.Builder()
                        .build()
                        .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                    
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    
                    val scanner = BarcodeScanning.getClient()

                    imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        scanBarcodeFromImage(imageProxy, scanner) { barcode ->
                            if (barcode != null && scanningActive) {
                                scanningActive = false
                                detectedBarcode = barcode
                                
                                
                                val vibrator = ctx.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    @Suppress("DEPRECATION")
                                    vibrator.vibrate(300)
                                }
                                
                                Log.d("SimpleScanner", "✅ BARCODE FOUND: $barcode")
                            }
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

                        
                        camera.cameraControl.enableTorch(false)
                        
                    } catch (e: Exception) {
                        Log.e("SimpleScanner", "Error: ${e.message}", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
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
                        text = "Scan Barcode",
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = onClose) {
                        Text("✖ Close")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📸 POSITION THE CODE IN THE FRAME",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1.2f)
                ) {
                    val strokeWidth = 6f
                    val cornerLength = 120f
                    val color = if (detectedBarcode != null) Color.Green else Color.Red

                    
                    drawRect(
                        color = color,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height),
                        style = Stroke(width = strokeWidth)
                    )

                    
                    
                    drawLine(color, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth = strokeWidth * 2)
                    drawLine(color, Offset(0f, 0f), Offset(0f, cornerLength), strokeWidth = strokeWidth * 2)

                    
                    drawLine(color, Offset(size.width - cornerLength, 0f), Offset(size.width, 0f), strokeWidth = strokeWidth * 2)
                    drawLine(color, Offset(size.width, 0f), Offset(size.width, cornerLength), strokeWidth = strokeWidth * 2)

                    
                    drawLine(color, Offset(0f, size.height - cornerLength), Offset(0f, size.height), strokeWidth = strokeWidth * 2)
                    drawLine(color, Offset(0f, size.height), Offset(cornerLength, size.height), strokeWidth = strokeWidth * 2)

                    
                    drawLine(color, Offset(size.width, size.height - cornerLength), Offset(size.width, size.height), strokeWidth = strokeWidth * 2)
                    drawLine(color, Offset(size.width - cornerLength, size.height), Offset(size.width, size.height), strokeWidth = strokeWidth * 2)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "💡 Keep the phone steady\n📏 Distance: 10-20 cm\n💡 Good lighting required",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), shape = MaterialTheme.shapes.small)
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))

            
            if (detectedBarcode != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✅ BARCODE DETECTED!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = detectedBarcode!!,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    detectedBarcode = null
                                    scanningActive = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🔄 Rescan")
                            }
                            Button(
                                onClick = {
                                    onBarcodeScanned(detectedBarcode!!)
                                    
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("✓ Confirm")
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun scanBarcodeFromImage(
    imageProxy: ImageProxy,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onResult: (String?) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { value ->
                        val formatName = when (barcode.format) {
                            Barcode.FORMAT_EAN_13 -> "EAN-13"
                            Barcode.FORMAT_EAN_8 -> "EAN-8"
                            Barcode.FORMAT_UPC_A -> "UPC-A"
                            Barcode.FORMAT_UPC_E -> "UPC-E"
                            Barcode.FORMAT_CODE_128 -> "CODE-128"
                            Barcode.FORMAT_CODE_39 -> "CODE-39"
                            Barcode.FORMAT_CODE_93 -> "CODE-93"
                            Barcode.FORMAT_CODABAR -> "CODABAR"
                            Barcode.FORMAT_ITF -> "ITF"
                            Barcode.FORMAT_QR_CODE -> "QR-CODE"
                            else -> "UNKNOWN"
                        }
                        Log.d("SimpleScanner", "🔍 Detected: $value | Format: $formatName")
                        onResult(value)
                        return@addOnSuccessListener
                    }
                }
                onResult(null)
            }
            .addOnFailureListener { e ->
                Log.e("SimpleScanner", "Scan error: ${e.message}")
                onResult(null)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
        onResult(null)
    }
}
