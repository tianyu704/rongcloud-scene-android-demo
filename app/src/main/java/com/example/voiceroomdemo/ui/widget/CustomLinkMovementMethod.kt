package com.jiaxun.nim.main.widget

import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.TextView


/**
 * @Author gusd
 * @Date 2020/8/28
 */
private const val TAG = "CustomLinkMovementMethod"

class CustomLinkMovementMethod : LinkMovementMethod() {
    private var lastClickTime: Long = 0
    private var clickDelay: Long = ViewConfiguration.getLongPressTimeout().toLong()

    override fun onTouchEvent(widget: TextView?, buffer: Spannable?, event: MotionEvent?): Boolean {
        event?.let {
            var action = event.action
            if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_DOWN
            ) {
                var x = event.x.toInt()
                var y = event.y.toInt()
                x -= widget?.totalPaddingLeft ?: 0
                y -= widget?.totalPaddingTop ?: 0

                x += widget?.scaleX?.toInt() ?: 0
                y += widget?.scaleY?.toInt() ?: 0

                var layout = widget?.layout
                var line = layout?.getLineForVertical(y)
                var off = layout?.getOffsetForHorizontal(line ?: 0, x.toFloat())
                off?.let {

                    var link = buffer?.getSpans(off, off, ClickableSpan::class.java)
                    if (link != null && link.isNotEmpty()) {
                        when (action) {
                            MotionEvent.ACTION_UP -> {
                                link.let {
                                    widget?.let {
                                        if (System.currentTimeMillis() - lastClickTime < clickDelay) {
                                            link[0].onClick(widget)
                                        }
                                    }
                                }
                            }
                            MotionEvent.ACTION_DOWN -> {
                                lastClickTime = System.currentTimeMillis()

                            }
                        }
                        return true
                    } else {
                        Selection.removeSelection(buffer)
                    }

                }
            }
            return super.onTouchEvent(widget, buffer, event)
        }


        return super.onTouchEvent(widget, buffer, event)
    }

    var instance: LinkMovementMethod? = null
    open fun get(): MovementMethod? {
        if (instance == null) instance = LinkMovementMethod()
        return instance
    }
}