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

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.OverScroller
import androidx.annotation.Px
import io.github.mucheng.mce.annotations.UnsafeApi
import io.github.mucheng.mce.text.ICursor
import io.github.mucheng.mce.text.measure.IMeasureCache
import io.github.mucheng.mce.text.measure.MeasureCache
import io.github.mucheng.mce.textmodel.model.TextModel
import io.github.mucheng.mce.textmodel.model.android.AndroidTextModel
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
    }

    private var textModel: TextModel

    private var cursor: ICursor

    private val editorRenderer: EditorRenderer

    private var measureCache: IMeasureCache

    private val scroller: OverScroller

    private var layout: Layout

    private var tabWidth = 1

    private var textSizePx = 0f

    private var typeface: Typeface

    init {
        textModel = AndroidTextModel("var a = 10\nprint(a);", threadSafe = true)
        cursor = textModel.getCursor()
        editorRenderer = EditorRenderer(this)
        measureCache = MeasureCache(textModel, editorRenderer)
        scroller = OverScroller(context)
        layout = TextModelLayout(this)
        typeface = Typeface.MONOSPACE

        setTextSize(DEFAULT_TEXT_SIZE)
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
            this.textModel = TextModel(text, threadSafe = true)
        }
        this.cursor = textModel.getCursor()
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
    }

    open fun setTextSizePx(@Px textSizePx: Float) {
        this.textSizePx = textSizePx
        editorRenderer.update()
    }

    open fun getTextSizePx(): Float {
        return this.textSizePx
    }

    open fun setTypeface(typeface: Typeface) {
        this.typeface = typeface
        editorRenderer.update()
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

    open fun getRowHeight(): Int {
        val metrics = editorRenderer.getLineNumberPaint().fontMetricsInt
        return metrics.bottom - metrics.top
    }

    open fun setTabWidth(width: Int) {
        if (width < 1) {
            throw IllegalArgumentException("Tab width must not be less than 1.")
        }
        this.tabWidth = width
        invalidate()
    }

    open fun getTabWidth(): Int {
        return this.tabWidth
    }

    override fun onCheckIsTextEditor(): Boolean {
        return isEnabled
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo?): InputConnection {
        return super.onCreateInputConnection(outAttrs)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        editorRenderer.onDraw(canvas)
    }

}