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

package io.github.mucheng.mce.textmodel.iterator

import kotlin.collections.CharIterator

open class CharIterator<T : CharSequence>(textRow: T) : CharIterator() {

    private val textRow: T

    private var index: Int

    init {
        this.textRow = textRow
        this.index = -1
    }

    override fun hasNext(): Boolean {
        return index + 1 < textRow.length
    }

    override fun nextChar(): Char {
        return textRow[index++]
    }

}