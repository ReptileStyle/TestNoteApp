package com.example.vkaudionotes

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vkaudionotes.ui.main.MainScreen
import com.example.vkaudionotes.ui.main.MainViewModel
import com.example.vkaudionotes.ui.theme.VKAudioNotesTheme
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
   // private val mainViewModel:MainViewModel by viewModels { MainViewModelFactory(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("main","onCreate")

        setContent {
            VKAudioNotesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    override fun onDestroy() {

        super.onDestroy()
    }
}

//class MainViewModelFactory(private val context: Context):ViewModelProvider.NewInstanceFactory(){
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return MainViewModel(context) as T
//    }
//}
