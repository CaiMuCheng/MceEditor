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

package io.github.mucheng.mce.widget.layout

/**
 * The text measuring tool interface,
 * it is so easy to measure text.
 * */
interface Layout {

    fun getLayoutWidth(): Int

    fun getLayoutHeight(): Int

    fun getStartVisibleRow(): Int

    fun getEndVisibleRow(): Int

    fun getStartVisibleColumn(line: Int): Int

    fun getEndVisibleColumn(line: Int): Int

    fun getRowCount(): Int

    fun getMaxOffset(): Float

    fun getOffsetLine(offsetY: Float): Int

    fun getOffsetColumn(line: Int, offsetX: Float): Int

    fun destroy()

    fun setVisibleRowEnabled(isVisibleRowEnabled: Boolean)

    fun isVisibleRowEnabled(): Boolean

    fun setQuick(isQuick: Boolean)

    fun isQuick(): Boolean

}