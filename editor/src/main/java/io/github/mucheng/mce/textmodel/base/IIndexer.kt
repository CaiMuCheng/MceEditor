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

package io.github.mucheng.mce.textmodel.base

import io.github.mucheng.mce.textmodel.position.CharPosition

interface IIndexer {

    fun getCharPosition(line: Int, column: Int): CharPosition

    fun getCharPosition(index: Int): CharPosition

    fun getCharLine(index: Int): Int

    fun getCharColumn(index: Int): Int

    fun getCharIndex(line: Int, column: Int): Int

}