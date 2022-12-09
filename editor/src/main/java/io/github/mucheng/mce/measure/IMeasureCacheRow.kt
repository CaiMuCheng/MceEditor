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

package io.github.mucheng.mce.text.measure

import io.github.mucheng.mce.textmodel.model.TextRow

interface IMeasureCacheRow {

    fun getMeasureCache(): FloatArray

    fun getMeasureCacheLength(): Int

    fun append(floatArray: FloatArray)

    fun setTextRow(textRow: TextRow)

    fun getTextRow(): TextRow

    fun afterInsert(startIndex: Int, endIndex: Int, charSequence: CharSequence)

    fun afterDelete(startIndex: Int, endIndex: Int)

    fun destroy()

}