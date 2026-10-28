package com.phlox.tvwebbrowser.activity.main

import android.os.Handler
import android.os.SystemClock
import android.view.KeyEvent

/**
 * Two OK presses in a row on the remote run [onDoubleOk] instead of being two clicks. A single press is still a
 * click, delivered [DOUBLE_PRESS_MS] later (it may be the first of two), and a long press is left as it is.
 */
class DoubleOkDetector(
    private val handler: Handler,
    private val deliver: (KeyEvent) -> Boolean,
    private val onDoubleOk: () -> Unit
) {
    companion object {
        const val DOUBLE_PRESS_MS = 350L

        fun isOk(keyCode: Int) = keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER ||
                keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_BUTTON_A
    }

    private enum class State {
        IDLE,
        HELD, //first press is down, held back
        WAITING, //first press is up, a second one may follow
        SECOND, //second press is down: played/paused, its up is dropped
        PASS //long press: delivered as it is
    }

    private var state = State.IDLE
    private var keyCode = 0
    private var down: KeyEvent? = null
    private var up: KeyEvent? = null
    private val deliverSinglePress = Runnable { deliverHeldPress() }

    /** a press is being handled: the next events of it must come here too */
    val isBusy get() = state != State.IDLE

    /** @return true if the event was taken */
    fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (state == State.IDLE) {
            if (!isOk(event.keyCode) || event.action != KeyEvent.ACTION_DOWN || event.repeatCount != 0) return false
            keyCode = event.keyCode
            down = KeyEvent(event)
            state = State.HELD
            return true
        }
        if (event.keyCode != keyCode) {
            if (isOk(event.keyCode)) return true //another OK key code of the same press
            //another key: what was held back goes first
            when (state) {
                State.HELD -> { down?.let { deliver(it) }; state = State.PASS }
                State.WAITING -> { handler.removeCallbacks(deliverSinglePress); deliverHeldPress() }
                else -> {}
            }
            return false
        }
        when (state) {
            State.HELD -> if (event.action == KeyEvent.ACTION_UP) {
                up = KeyEvent(event)
                state = State.WAITING
                handler.postDelayed(deliverSinglePress, DOUBLE_PRESS_MS)
            } else if (event.repeatCount > 0) {
                down?.let { deliver(it) }
                deliver(event)
                state = State.PASS
            }
            State.WAITING -> if (event.action == KeyEvent.ACTION_DOWN) {
                handler.removeCallbacks(deliverSinglePress)
                state = State.SECOND
                onDoubleOk()
            }
            State.SECOND -> if (event.action == KeyEvent.ACTION_UP) state = State.IDLE
            State.PASS -> {
                if (event.action == KeyEvent.ACTION_UP) state = State.IDLE
                return false
            }
            State.IDLE -> {}
        }
        return true
    }

    private fun deliverHeldPress() {
        state = State.IDLE
        val now = SystemClock.uptimeMillis()
        down?.let { deliver(it.at(now)) }
        up?.let { deliver(it.at(now)) }
        down = null
        up = null
    }

    private fun KeyEvent.at(time: Long) =
        KeyEvent(time, time, action, keyCode, repeatCount, metaState, deviceId, scanCode, flags, source)
}
