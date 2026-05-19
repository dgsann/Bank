package com.example.bank

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.bank.data.AppStorage
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.screens.AddReceiptScreen
import com.example.bank.ui.screens.DiscountsScreen
import com.example.bank.ui.screens.HomeScreen
import com.example.bank.ui.screens.ProfileScreen
import com.example.bank.ui.screens.ReceiptsScreen
import com.example.bank.ui.theme.BankTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage = AppStorage(applicationContext)
        val viewModel: MainViewModel by viewModels { MainViewModel.factory(storage) }
        setContent { BankTheme { MainScreen(viewModel) } }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val error by viewModel.error.collectAsState()
    val toast by viewModel.toast.collectAsState()

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }
    LaunchedEffect(toast) {
        toast?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Главная") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text("Чеки") }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.AddCircle, null) },
                    label = { Text("Добавить") }
                )
                NavigationBarItem(
                    selected = tab == 3,
                    onClick = { tab = 3 },
                    icon = { Icon(Icons.Default.Star, null) },
                    label = { Text("Скидки") }
                )
                NavigationBarItem(
                    selected = tab == 4,
                    onClick = { tab = 4 },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Профиль") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (tab) {
                0 -> HomeScreen(viewModel)
                1 -> ReceiptsScreen(viewModel)
                2 -> AddReceiptScreen(viewModel, onSaved = { tab = 0 })
                3 -> DiscountsScreen(viewModel)
                4 -> ProfileScreen(viewModel)
            }
        }
    }
}
