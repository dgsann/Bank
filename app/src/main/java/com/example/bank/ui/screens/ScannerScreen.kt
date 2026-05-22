package com.example.bank.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.bank.presentation.MainViewModel
import androidx.camera.lifecycle.ProcessCameraProvider

@Composable
fun ScannerScreen(viewModel: MainViewModel) {
    val step by viewModel.scannerStep.collectAsState()
    val receipt by viewModel.scannedReceipt.collectAsState()
    val context = LocalContext.current

    // Проверка разрешений
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (!hasCameraPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Нужно разрешение на камеру", color = Color.White)
            }
        } else {
            when (step) {
                MainViewModel.ScannerStep.CAPTURING -> {
                    CameraPreviewScreen(
                        onCapture = { viewModel.takePhoto() },
                        onClose = { viewModel.closeScanner() }
                    )
                }
                MainViewModel.ScannerStep.ANALYZING -> {
                    AnalysisSimulation()
                }
                MainViewModel.ScannerStep.RESULT -> {
                    receipt?.let {
                        ResultScreen(
                            receipt = it,
                            onConfirm = { viewModel.confirmScan() },
                            onCancel = { viewModel.closeScanner() }
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun CameraPreviewScreen(onCapture: () -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        ) { view ->
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(view.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }

        // Overlay - темные области вокруг рамки
        Box(modifier = Modifier.fillMaxSize()) {
            // Рамка (прозрачная часть)
            Box(
                modifier = Modifier
                    .size(width = 280.dp, height = 450.dp)
                    .align(Alignment.Center)
                    .background(Color.Transparent)
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(Color.Black.copy(0.4f), RoundedCornerShape(20.dp))
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White)
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Поместите чек в рамку",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.background(Color.Black.copy(0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onCapture,
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(35.dp)).background(Color.White).padding(4.dp).background(Color.Black).padding(2.dp).background(Color.White))
            }
        }
    }
}

@Composable
fun AnalysisSimulation() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Сканирующий лазер
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.01f)
                .offset(y = (scanProgress * 800).dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Cyan, Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Color.Cyan)
            Spacer(Modifier.height(16.dp))
            Text("Анализ чека...", color = Color.Cyan, fontWeight = FontWeight.Bold)
            Text("Распознавание товаров и цен", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun ResultScreen(
    receipt: com.example.bank.model.Receipt,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Результат сканирования", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                IconButton(onClick = onCancel) { Icon(Icons.Default.Close, null, tint = Color.Black) }
            }
            Text(receipt.store ?: "Неизвестный магазин", color = Color.DarkGray, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(receipt.items) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.name, modifier = Modifier.weight(1f), color = Color.Black)
                        Text("${item.price.toInt()} ₽", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ИТОГО:", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text("${receipt.totalAmount.toInt()} ₽", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF2E7D32))
            }
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text("Подтвердить и зачислить", fontWeight = FontWeight.Bold)
            }
        }
    }
}
