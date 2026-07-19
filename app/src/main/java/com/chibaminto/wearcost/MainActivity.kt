package com.chibaminto.wearcost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chibaminto.wearcost.data.ClothingRepository
import com.chibaminto.wearcost.data.CurrencySettingsRepository
import com.chibaminto.wearcost.data.WearCostDatabase
import com.chibaminto.wearcost.ui.WearCostApp
import com.chibaminto.wearcost.ui.theme.WearCostTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WearCostTheme {
                val repository = remember {
                    ClothingRepository(WearCostDatabase.getInstance(applicationContext).clothingDao())
                }
                val currencySettingsRepository = remember {
                    CurrencySettingsRepository(applicationContext)
                }
                val viewModel: WearCostViewModel = viewModel(
                    factory = WearCostViewModelFactory(repository, currencySettingsRepository)
                )
                WearCostApp(viewModel = viewModel)
            }
        }
    }
}
