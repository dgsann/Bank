package com.example.bank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.bank.data.AppStorage
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.theme.BankTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage = AppStorage(applicationContext)
        val viewModel: MainViewModel by viewModels { MainViewModel.factory(storage) }
        setContent { BankTheme { RootPlaceholder(viewModel) } }
    }
}

@Composable
private fun RootPlaceholder(viewModel: MainViewModel) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Финансовый компаньон")
        }
    }
}
