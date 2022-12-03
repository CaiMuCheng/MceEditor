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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.OverScroller
import androidx.annotation.Px
import io.github.mucheng.mce.text.measure.IMeasureCache
import io.github.mucheng.mce.text.measure.MeasureCache
import io.github.mucheng.mce.textmodel.annoations.UnsafeApi
import io.github.mucheng.mce.textmodel.base.ICursor
import io.github.mucheng.mce.textmodel.model.TextModel
import io.github.mucheng.mce.textmodel.model.android.AndroidTextModel
import io.github.mucheng.mce.textmodel.position.CharPosition
import io.github.mucheng.mce.textmodel.util.Cursor
import io.github.mucheng.mce.util.Logger
import io.github.mucheng.mce.widget.layout.Layout
import io.github.mucheng.mce.widget.layout.TextModelLayout
import io.github.mucheng.mce.widget.renderer.EditorRenderer


/**
 * The code editor view.
 * It uses code analysing to make the regions highlighting.
 *
 * @constructor Context
 * @author CaiMuCheng
 * */
@Suppress("unused", "LeakingThis")
open class CodeEditor @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val DEFAULT_TEXT_SIZE = 18f
        const val DEFAULT_LINE_NUMBER_SPACING = 5f
        val logger = Logger("CodeEditor")
    }

    private var textModel: TextModel

    private var cursor: ICursor

    private val editorRenderer: EditorRenderer

    private var measureCache: IMeasureCache

    private val scroller: OverScroller

    private var layout: Layout

    private val inputMethodManager: InputMethodManager

    private val editorTouchEventHandler: EditorTouchEventHandler

    private val gestureDetector: GestureDetector

    private val inputConnection: EditorInputConnection

    private var isEditable = true

    private var autoBuildMeasureCache = true

    private var tabWidth = 1

    private var measureTabUseWhitespace = true

    private var textSizePx = 0f

    private var lineSpacingMultiplier = 1f

    private var lineSpacingAdd = 0f

    private var lineNumberSpacingPx: Float

    private var typeface: Typeface

    init {
        textModel = AndroidTextModel("var a = 10\nprint(a);")
        cursor = Cursor(textModel)
        editorRenderer = EditorRenderer(this)
        measureCache = MeasureCache(textModel, editorRenderer)
        scroller = OverScroller(context)
        layout = TextModelLayout(this)
        inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        editorTouchEventHandler = EditorTouchEventHandler(this)
        gestureDetector = GestureDetector(context, editorTouchEventHandler).apply {
            setOnDoubleTapListener(editorTouchEventHandler)
        }
        inputConnection = EditorInputConnection(this)
        typeface = Typeface.MONOSPACE

        this.textSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE, Resources.getSystem().displayMetrics
        )
        this.lineNumberSpacingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, DEFAULT_LINE_NUMBER_SPACING, Resources.getSystem().displayMetrics
        )
        setTabWidth(4)
        isFocusable = true
        isFocusableInTouchMode = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            defaultFocusHighlightEnabled = false
        }

        requestLayout()
    }

    open fun setText(text: CharSequence) {
        return setText(text, true)
    }

    open fun setText(text: CharSequence, resetTextModel: Boolean) {
        if (text is TextModel && resetTextModel) {
            this.textModel = text
        } else {
            this.textModel = AndroidTextModel(text)
            this.textModel.setThreadSafe(true)
        }
        this.cursor = Cursor(this.textModel)
        this.measureCache.setTextModel(textModel)
    }

    open fun getText(): TextModel {
        return textModel
    }

    open fun setTextSize(textSize: Float) {
        val displayMetrics = Resources.getSystem().displayMetrics
        this.textSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, textSize, displayMetrics
        )
        editorRenderer.update()
        autoBuildMeasureCache()
    }

    open fun setTextSizePx(@Px textSizePx: Float) {
        this.textSizePx = textSizePx
        editorRenderer.update()
        autoBuildMeasureCache()
    }

    open fun getTextSizePx(): Float {
        return this.textSizePx
    }

    open fun setLineNumberSpacing(spacing: Float) {
        val displayMetrics = Resources.getSystem().displayMetrics
        this.lineNumberSpacingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, spacing, displayMetrics
        )
    }

    open fun setLineNumberSpacingPx(@Px spacing: Float) {
        this.lineNumberSpacingPx = spacing
    }

    open fun getLineNumberSpacingPx(): Float {
        return this.lineNumberSpacingPx
    }

    open fun setTypeface(typeface: Typeface) {
        this.typeface = typeface
        editorRenderer.update()
        autoBuildMeasureCache()
    }

    open fun getTypeface(): Typeface {
        return this.typeface
    }

    open fun setCursor(cursor: ICursor) {
        this.cursor = cursor
    }

    open fun getCursor(): ICursor {
        return this.cursor
    }

    open fun setEditable(isEditable: Boolean) {
        this.isEditable = isEditable
    }

    open fun isEditable(): Boolean {
        return isEditable
    }

    open fun rebuildMeasureCache() {
        measureCache.buildMeasureCache()
    }

    open fun setAutoBuildMeasureCache(isAutoBuildMeasureCache: Boolean) {
        this.autoBuildMeasureCache = isAutoBuildMeasureCache
    }

    open fun isAutoBuildMeasureCache(): Boolean {
        return this.autoBuildMeasureCache
    }

    open fun getEditorRenderer(): EditorRenderer {
        return this.editorRenderer
    }

    @UnsafeApi
    open fun setMeasureCache(measureCache: IMeasureCache) {
        this.measureCache.destroy()
        this.measureCache = measureCache
    }

    @UnsafeApi
    open fun getMeasureCache(): IMeasureCache {
        return this.measureCache
    }

    private fun autoBuildMeasureCache() {
        if (autoBuildMeasureCache) {
            rebuildMeasureCache()
        }
    }

    open fun getScroller(): OverScroller {
        return scroller
    }

    open fun getOffsetX(): Int {
        return scroller.currX
    }

    open fun getOffsetY(): Int {
        return scroller.currY
    }

    open fun setLayout(layout: Layout) {
        this.layout.destroy()
        this.layout = layout
    }

    open fun getLayout(): Layout {
        return layout
    }

    open fun setLineSpacing(lineSpacingAdd: Float, lineSpacingMultiplier: Float) {
        this.lineSpacingAdd = lineSpacingAdd
        this.lineSpacingMultiplier = lineSpacingMultiplier
    }

    open fun setLineSpacingMultiplier(lineSpacingMultiplier: Float) {
        this.lineSpacingMultiplier = lineSpacingMultiplier
    }

    open fun getLineSpacingMultiplier(): Float {
        return this.lineSpacingMultiplier
    }

    open fun setLineSpacingAdd(lineSpacingAdd: Float) {
        this.lineSpacingAdd = lineSpacingAdd
    }

    open fun getLineSpacingAdd(): Float {
        return this.lineSpacingAdd
    }

    open fun getLineSpacingPixels(): Int {
        val metrics = editorRenderer.getLineNumberPaint().fontMetricsInt
        return ((metrics.descent - metrics.ascent) * (this.lineSpacingMultiplier - 1f) + this.lineSpacingAdd).toInt() / 2 * 2
    }

    open fun getRowBaseline(row: Int): Int {
        val metrics = editorRenderer.getLineNumberPaint().fontMetricsInt
        val lineSpacing = getLineSpacingPixels()
        return (metrics.descent - metrics.ascent + lineSpacing) * row - metrics.descent - lineSpacing / 2
    }

    open fun getRowHeight(): Int {
        val metrics = editorRenderer.getLineNumberPaint().fontMetricsInt
        return metrics.descent - metrics.ascent + getLineSpacingPixels()
    }

    open fun getRowTop(row: Int): Int {
        return getRowHeight() * row
    }

    open fun getRowBottom(row: Int): Int {
        return getRowHeight() * (row + 1)
    }

    open fun getRowTopOfText(row: Int): Int {
        return getRowTop(row) + getLineSpacingPixels() / 2
    }

    open fun getRowBottomOfText(row: Int): Int {
        return getRowBottom(row) - getLineSpacingPixels() / 2
    }

    open fun getRowHeightOfText(): Int {
        val metrics = editorRenderer.getLineNumberPaint().fontMetricsInt
        return metrics.descent - metrics.ascent
    }

    open fun setTabWidth(width: Int) {
        if (width < 1) {
            throw IllegalArgumentException("Tab width must not be less than 1.")
        }
        this.tabWidth = width
    }

    open fun getTabWidth(): Int {
        return this.tabWidth
    }

    open fun setMeasureTabUseWhitespace(isEnabled: Boolean) {
        this.measureTabUseWhitespace = isEnabled
    }

    open fun isMeasureTabUseWhitespace(): Boolean {
        return this.measureTabUseWhitespace
    }

    open fun showSoftInput() {
        if (isEditable() && isEnabled) {
            // Need to request focus first.
            if (isInTouchMode && !hasFocus()) {
                requestFocusFromTouch()
            }
            if (!hasFocus()) {
                requestFocus()
            }
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    open fun hideSoftInput() {
        inputMethodManager.hideSoftInputFromWindow(
            windowToken,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }

    override fun onCheckIsTextEditor(): Boolean {
        return true
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.inputType = EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
        return inputConnection
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    open fun setSelection(start: CharPosition, end: CharPosition) {
        cursor.setLeft(start.line, start.column)
        cursor.setRight(end.line, end.column)
        invalidate()
    }

    open fun setSelection(line: Int, column: Int) {
        textModel.checkLineColumn(line, column, allowEqualsLength = true)
        cursor.set(line, column)
        invalidate()
    }

    open fun moveCursorToLeft() {
        cursor.moveToLeft()
        invalidate()
    }

    open fun moveCursorToTop() {
        cursor.moveToTop()
        invalidate()
    }

    open fun moveCursorToRight() {
        cursor.moveToRight()
        invalidate()
    }

    open fun moveCursorToBottom() {
        cursor.moveToBottom()
        invalidate()
    }

    open fun moveCursorToStart() {
        cursor.moveToStart()
        invalidate()
    }

    open fun moveCursorToEnd() {
        cursor.moveToEnd()
        invalidate()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {

            KeyEvent.KEYCODE_ENTER -> {
                inputConnection.commitText("\n", 0)
            }

            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_FORWARD_DEL -> {
                inputConnection.deleteSurroundingText(0, 0)
            }

            KeyEvent.KEYCODE_SPACE -> {
                inputConnection.commitText(" ", 0)
            }

            KeyEvent.KEYCODE_TAB -> {
                inputConnection.commitText("\t", 0)
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
                moveCursorToLeft()
            }

            KeyEvent.KEYCODE_DPAD_UP -> {
                moveCursorToTop()
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                moveCursorToRight()
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
                moveCursorToBottom()
            }

            else -> {
                if (event.isPrintingKey) {
                    inputConnection.commitText(
                        String(byteArrayOf(event.unicodeChar.toByte())),
                        0
                    )
                }
            }

        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        editorRenderer.onDraw(canvas)
    }

}