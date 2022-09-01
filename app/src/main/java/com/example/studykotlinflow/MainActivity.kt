package com.example.studykotlinflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.MainThread
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.studykotlinflow.ui.theme.StudyKotlinFlowTheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val testViewModel = TestViewModel()

//        MainScope().launch {
//            testViewModel.stateFlow.collect {
//                println("stateFlow #1 : $it - ${Thread.currentThread().name}")
//            }
//            println("State Collect End #1 - ${Thread.currentThread().name}")
//        }
//
//        MainScope().launch {
//            testViewModel.stateFlow.collect {
//                println("stateFlow #2 : $it - ${Thread.currentThread().name }")
//            }
//            println("State Collect End #2 - ${Thread.currentThread().name}")
//        }
//
//        MainScope().launch {
//            testViewModel.startSendDataToStateFlow()
//        }

        // stateFlow의 테스트 코드

//        MainScope().launch {
//            testViewModel.startSendDataToSharedFlow()
//        }
//
//        MainScope().launch {
//            testViewModel.sharedFlow.collect {
//                println("sharedFlow #1: $it - ${Thread.currentThread().name}")
//            }
//            println("Shared Collect End #1 - ${Thread.currentThread().name}")
//        }
//
//        MainScope().launch {
//            testViewModel.sharedFlow.collect {
//                println("sharedFlow #2: $it - ${Thread.currentThread().name}")
//            }
//            println("Shared Collect End #2")
//        }

        // shaedFlow의 테스트 코드

        //collect 를 여러곳에서 진행하는 이유, channel의 pan out 과 flow 의 동작의 차이점을 알기 위해서 했음.


//
//        MainScope().launch {
//            testViewModel.sharedFlow.collect {
//                println("sharedFlow: $it")
//            }
//        }
//
//        MainScope().launch {
//            testViewModel.stateFlow.collect {
//                println("stateFlow: $it")
//            }
//        }
//
//        MainScope().launch {
//            testViewModel.repeatSameDataToEachFlow()
//        }

        //shareIn, stateIn test 를 위한 기본 동작 코드

        MainScope().launch {

            testViewModel.connectionFlow.collect {
                println("connectionFlow : $it")
            }
        }

        //shareIn code
        MainScope().launch {
            delay(5100)
            testViewModel.connectionSharedFlow.collect {
                println("connectionSharedFlow #2: $it")
            }
        }

        // 원본 cold stream 과 다르게 shareIn, stateIn 으로 생성된 hot stream 은 다른 coroutine Scope 에서 동작한다.

        //stateFlow 의 경우 내부적으로 동일한 값이 들어올 경우 SKIP 한다! sharedFlow 의 경우는 skip 하지 않는다.
        setContent {
            StudyKotlinFlowTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    StudyKotlinFlowTheme {
        Greeting("Android")
    }
}