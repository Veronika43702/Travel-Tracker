package ru.nikfirs.android.traveltracker.core.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.nikfirs.android.traveltracker.core.domain.coroutines.DefaultDispatcherProvider
import ru.nikfirs.android.traveltracker.core.domain.coroutines.DispatcherProvider

abstract class ViewModel<A : MviAction, E : MviEffect, S : MviState>(
    protected val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
) : ViewModel() {

    private val initialState: S by lazy { createInitialState() }
    private val initialLanguage: String? = null

    private val _action: MutableSharedFlow<A> = MutableSharedFlow()
    val action get() = _action.asSharedFlow()

    private val _effect: Channel<E> = Channel()
    val effect get() = _effect.receiveAsFlow()

    private val _state: MutableStateFlow<S> = MutableStateFlow(initialState)
    val state get() = _state.asStateFlow()

    private val _language: MutableStateFlow<String?> = MutableStateFlow(initialLanguage)
    val language get() = _language.asStateFlow()

    val currentState: S get() = _state.value

    init {
        subscribeEvents()
    }

    abstract fun createInitialState(): S

    protected abstract fun handleAction(action: A)

    fun setAction(action: A) {
        launch {
            _action.emit(action)
        }
    }

    protected fun setEffect(builder: () -> E) {
        launch {
            _effect.send(builder())
        }
    }

    protected fun setState(reduce: (S) -> S) {
        _state.update { reduce(it) }
    }

    private fun subscribeEvents() {
        launch {
            _action.collect { handleAction(it) }
        }
    }

    protected open fun setLanguage(language: String) {
        _language.update { language }
    }

    protected fun isLanguageChanged(language: String): Boolean {
        val isLanguageChanged = _language.value != null && (_language.value != language)
        return if (isLanguageChanged) {
            setLanguage(language)
            true
        } else {
            false
        }
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launch { block() }

    protected fun launchIO(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launch(dispatchers.io) { block() }

    protected fun launchDefault(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launch(dispatchers.default) { block() }

    protected fun launchMain(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launch(dispatchers.main) { block() }
}
