package com.hold1.keyboardheightprovider

import android.app.Activity
import android.graphics.Point
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.PopupWindow

/**
 * Created by Cristian Holdunu on 11/01/2019.
 */
class KeyboardHeightProvider(val activity: Activity) : PopupWindow(activity) {

    private var resizableView: View

    private var keyboardListeners = ArrayList<KeyboardListener>()

    init {
        contentView = View.inflate(activity, R.layout.keyboard_popup, null)
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
        
        width = 20
        height = WindowManager.LayoutParams.MATCH_PARENT

        resizableView = activity.findViewById(android.R.id.content)
        resizableView.viewTreeObserver.addOnGlobalLayoutListener(getGlobalLayoutListener())
    }

    private fun getGlobalLayoutListener() = ViewTreeObserver.OnGlobalLayoutListener {
        computeKeyboardState()
    }

    private fun computeKeyboardState() {
        val screenSize = Point()
        activity.windowManager.defaultDisplay.getSize(screenSize)
        val rect = Rect()
        resizableView.getWindowVisibleDisplayFrame(rect)
        val orientation = activity.resources.configuration.orientation

        val keyboardHeight = screenSize.y + topCutoutHeight - rect.bottom
        notifyKeyboardHeightChanged(keyboardHeight, orientation)
    }

    private val topCutoutHeight: Int
        get() {
            val decorView = activity.window.decorView ?: return 0
            var cutOffHeight = 0
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val windowInsets = decorView.rootWindowInsets
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val displayCutout = windowInsets.displayCutout
                    if (displayCutout != null) {
                        val list = displayCutout.boundingRects
                        for (rect in list) {
                            if (rect.top == 0) {
                                cutOffHeight += rect.bottom - rect.top
                            }
                        }
                    }
                }

            }
            return cutOffHeight
        }

    fun addKeyboardListener(listener: KeyboardListener) {
        keyboardListeners.add(listener)
    }

    fun removeKeyboardListener(listener: KeyboardListener) {
        keyboardListeners.remove(listener)
    }

    private fun notifyKeyboardHeightChanged(height: Int, orientation: Int) {
        keyboardListeners.forEach {
            it.onHeightChanged(height)
        }
    }

    /**
     * Start the KeyboardHeightProvider, this must be called after the onResume of the Activity.
     * PopupWindows are not allowed to be registered before the onResume has finished
     * of the Activity.
     */
    fun start() {
        if (!isShowing && resizableView.windowToken != null) {
            showAtLocation(resizableView, Gravity.NO_GRAVITY, 0, 0)
        }
    }

    /**
     * Close the keyboard height provider,
     * this provider will not be used anymore.
     */
    fun close() {
        this.keyboardListeners.clear()
        dismiss()
    }

    interface KeyboardListener {
        fun onHeightChanged(height: Int)
    }
}