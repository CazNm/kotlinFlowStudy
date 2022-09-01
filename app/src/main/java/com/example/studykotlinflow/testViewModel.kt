package com.example.studykotlinflow

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

// cold stream
// collect() 또는 subscribe 할 때 마다, flow block 이 재실행 된다. 1~10 까지 emit 하는 flow 가 있다면, collect 를 호출 할 때 마다 1~10 을 전달 받는다.

// hot stream
// collect() 또는 subscribe 하더라도, 즉시 flow block 이 실행되지 않는다. collect() 시점 이후에 emit 된 데이터를 전달 받는다.

class TestViewModel : ViewModel() {
    private val _stateFlow  = MutableStateFlow(99)
    val stateFlow = _stateFlow

    private val _sharedFlow = MutableSharedFlow<Int>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    // replay : collect() 시에 전달받을 이전 데이터의 개수를 지저한다. 0 이라면 collect 시점에 담겨있던 데이터부터 전달받는다.
    // extraBufferCapacity : buffer 개수를 설정한다. flow 의 emit 이 빠르고 collect 가 느릴 때 지정된 개수만큼  buffer 에 저장되고, 지정된 개수가 넘어가면
    // onBufferOverFlow 에 설정된 정책에 따라 동작하게 된다.
    // onBufferOverFlow : Buffer 가 다 찼을 때의 동작을 정의한다. 이는 channel 에서 사용하는 buffer 의 정의와 동일하다.

    // BufferOverflow.SUSPEND : buffer 가 꽉 찼을 떄 emit 을 수행하면 emit 코드가 block 된다. buffer 에 빈자리가 생기면 emit 코드가 실행된다.
    // BufferOverflow.DROP_OLDEST : buffer 가 꽉 찼을 떄 emit 을 수행하면 오래된 데이터 부터 삭제하면서 새로운 데이터를 넣는다.
    // BufferOverflow.DROP_LATEST : buffer 가 꽉 찼을 때 emit 을 수행하면 최근 데이터를 삭제하고 새로운 데이터를 넣는다.

    val sharedFlow = _sharedFlow

    suspend fun startSendDataToStateFlow() {
        repeat(10) {
            _stateFlow.value = it
            delay(500)
        }
    } // State flow 에 보내는 테스트 코드

    suspend fun startSendDataToSharedFlow() {
        repeat(10) {
            _sharedFlow.emit(it) // sharedFlow 는 value 로 값을 할당 할 수 없다.
            delay(500)
        }
    } // Shared flow 에 보내는 테스트 코드

    suspend fun repeatSameDataToEachFlow() {
        repeat(5) {
            println("sendData #$it")
            _sharedFlow.emit(100)
            _stateFlow.value = 100
            delay(500)
        }
    } // 둘의 차이를 알기 위한 테스트 코드
}