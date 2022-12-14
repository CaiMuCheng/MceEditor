/*
 * An experimental code editor library on Android.
 * https://github.com/CaiMuCheng/MceEditor
 * Copyright (c) 2022 CaiMuCheng - All rights reserved
 *
 * This library is free software. You can redistribute it or
 * modify it under the terms of the Mozilla Public
 * License Version 2.0 by the Mozilla.
 *
 * You can use it for commercial purposes, but you must
 * know the copyright's owner is author and mark the copyright
 * with author in your project.
 *
 * Do not without the author, the license, the repository link.
 */

package io.github.mucheng.mce.widget

import android.view.GestureDetector
import android.view.MotionEvent
import java.lang.Integer.max
import java.lang.Integer.min

class EditorTouchEventHandler(editor: CodeEditor) : GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {

    private val editor: CodeEditor

    init {
        this.editor = editor
    }

    override fun onDown(e: MotionEvent): Boolean {
        return editor.isEnabled && !editor.isMeasureCacheBusy()
    }

    override fun onShowPress(e: MotionEvent) {

    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        editor.getScroller().forceFinished(true)
        if (editor.isEditable()) {
            val layout = editor.getLayout()
            val line = layout.getOffsetLine(e.y + editor.getOffsetY())
            val column = layout.getOffsetColumn(
                line,
                e.x + editor.getOffsetX()
            )
            editor.setSelection(line, column)
            editor.showSoftInput()
        }
        return false
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        val scroller = editor.getScroller()
        val targetDistanceX =
            min(editor.getScrollMaxX(), max(0, scroller.currX + distanceX.toInt())) - scroller.currX
        val targetDistanceY =
            min(editor.getScrollMaxY(), max(0, scroller.currY + distanceY.toInt())) - scroller.currY

        scroller.startScroll(
            scroller.currX,
            scroller.currY,
            targetDistanceX,
            targetDistanceY,
            0
        )

        editor.invalidate()
        return true
    }

    override fun onLongPress(e: MotionEvent) {

    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val scroller = editor.getScroller()
        scroller.forceFinished(true)
        scroller.fling(
            scroller.currX,
            scroller.currY,
            -velocityX.toInt(),
            -velocityY.toInt(),
            0,
            editor.getScrollMaxX(),
            0,
            editor.getScrollMaxY()
        )
        editor.invalidate()
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        return true
    }

}