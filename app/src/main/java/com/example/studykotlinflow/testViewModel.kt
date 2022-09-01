package com.example.studykotlinflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

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


    //아래 코드는 cold stream 인 flow 를 hot stream 으로 바꾸는 shareIn stateIn 사용법의 예제이다.
    //주로 flow 의 코드가 무거울 경우. 유지 작업 자체의 비용이 많이 들때 사용한다. collect 를 여러번 호출 하면
    //cold flow 의 경우 collect() 가 호출된 만큼 재실행 되기 때문에 한 번만 logic 을 실행시키고, 구독하는 형태인
    //hot stream 으로 변경하기 위해 사용한다.

    //stateIn vs shareIn

    /*
    val backendMessages: Flow<Message> = flow {
        connectToBackend()
        try{
            while(true){
                emit(receiveMessageFromBackend())
            }
        }finally {
            disconnectFromBackend()
        }
    }

    val messages : SharedFlow<Message> = backendMessages.shareIn(scope, SharingStarted.Eagerly)

     */

    //78 line 에 있는 shareIn 과 마찬가지로 stateIn 또한 현재 coroutineScope, 그리고 SharingStarted param 을 넘겨주게 된다.
    //coroutineScope 는 hotStream 을 수행하기 위해,
    //SharingStarted 는 아래 3가지 옵션을 통해 flow 를 실행하는 세부적인 정의를 내린다.

    //SharingStarted.Eagerly
    //subscriber 가 존재하지 않아도 upstream flow 는 그대로 동작하며 중지되지 않는다. 누적되는 값은 replay 개수 만큼이고
    //replay 개수보다 많을 경우, BufferOverflow.DROP_OLDEST 로 동작한다. (오랜된 value 부터 삭제)

    //SharingStarted.Lazily
    //첫번째 subscriber 가 등록된 후 부터 upstream flow 가 동작을 시작하며 중지되지 않는다. 첫 번째 subscriber 는
    //모든 emit 된 value 를 얻어가고 이후에 등록된 subscriber 는 replay 에 설정된 값 만큼 가져면서 collect 를 시작한다.
    //subscriber 가 모두 사라져도 동작을 유지하고, 이때 emit 되는 value 는 replay 수 만큼만 cache 된다.
    // (replay 만큼 cache 된 이후는 정지하는게 아니라 값만 들고 있고 실행되는건가..?)

    //SharingStarted.WhileSubscribed
    //subscriber 가 등록되면 sharing 을 시작하고, 없어지면 중지한다. replay 개수 만큼 cache 한다.

//    fun WhileSubscribed(stopTimeoutMillis: Long = 0,
//                        replayExpirationMillis: Long = Long.MAX_VALUE): SharingStarted

    // stopTimeoutMillis : 구독자가 사라진 이후 정지 시킬 delay, value 가 0 이면 즉시 정지한다.
    // replayExpirationMillis : replay 를 위해 cache 한 값을 유지시킬 시간을 정의한다. subscriber 가 모두
    // 사라지고 난 후에는 설정한 시간값 이후 replay 를 위해 저장한 값을 초기화 시킨다.

    private val _connectionFlow = flow {
        initHeavyLogic()
        var i = 0
        while(true){
            delay(500)
            emit(i++)
        }
    }

    val connectionFlow = _connectionFlow

    val connectionSharedFlow = _connectionFlow.shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    private suspend fun initHeavyLogic() {
        delay(1000)
        println("initHeavyLogic()")
    }
}