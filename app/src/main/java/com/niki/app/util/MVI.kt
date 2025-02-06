package com.niki.app.util

import com.zephyr.base.log.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


abstract class MVI<Intent, State, Effect>(protected val scope: CoroutineScope) {
    protected abstract val TAG: String

    protected abstract fun getInitState(): State

    /**
     * state 状态流
     */
    protected val _stateFlow = MutableStateFlow(getInitState())
    val uiStateFlow: StateFlow<State> = _stateFlow.asStateFlow()

    /**
     * effect 事件流 - 外部需要 collect 才能接受数据
     */
    private val _effectFlow = MutableSharedFlow<Effect>()
    val uiEffectFlow: SharedFlow<Effect> by lazy { _effectFlow.asSharedFlow() }

    /**
     * intent 管道
     */
    protected val intentChannel = Channel<Intent>(Channel.UNLIMITED)

    init {
        scope.launch {
            intentChannel.consumeAsFlow().collect { intent ->
                logE(TAG, "接受 intent: ${intent!!::class.java.simpleName}")
                handleIntent(intent)
            }
        }
    }

    /**
     * channel 接收并处理 intent
     */
    protected abstract fun handleIntent(intent: Intent)

    /**
     * view 用于发送意图
     */
    fun sendIntent(intent: Intent) =
        scope.launch {
            logE(TAG, "发送 intent: ${intent!!::class.java.simpleName}")
            intentChannel.send(intent)
        }

    /**
     * 对事件流发送数据
     */
    protected fun sendEffect(effect: Effect) = scope.launch {
        logE(TAG, "发送 effect: ${effect!!::class.java.simpleName}")
        _effectFlow.emit(effect)
    }

    /**
     * 更新状态流数据
     */
    protected fun updateState(update: State.() -> State) =
        _stateFlow.update(update)

    /**
     * view 用于观察 state 的变化
     */
    fun observeState(observe: Flow<State>.() -> Unit) = observe(uiStateFlow)
}