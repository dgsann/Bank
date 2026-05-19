package com.example.bank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.bank.data.AppStorage
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.screens.HomeScreen
import com.example.bank.ui.theme.BankTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage = AppStorage(applicationContext)
        val viewModel: MainViewModel by viewModels { MainViewModel.factory(storage) }
        setContent { BankTheme { HomeScreen(viewModel) } }
    }
}
