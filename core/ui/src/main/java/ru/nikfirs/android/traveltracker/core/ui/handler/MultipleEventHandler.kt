package ru.nikfirs.android.traveltracker.core.ui.handler

internal interface MultipleEventHandler {
    fun processEvent(event: () -> Unit)

    companion object
}

internal fun MultipleEventHandler.Companion.setDuration(duration: Long = 500L): MultipleEventHandler =
    MultipleEventHandlerImpl(duration)

private class MultipleEventHandlerImpl(val duration: Long) : MultipleEventHandler {
    private val currentEventTime: Long
        get() = System.currentTimeMillis()

    private var previousEventTime: Long = 0

    override fun processEvent(event: () -> Unit) {
        if (currentEventTime - previousEventTime >= duration) {
            event.invoke()
        }
        previousEventTime = currentEventTime
    }
}