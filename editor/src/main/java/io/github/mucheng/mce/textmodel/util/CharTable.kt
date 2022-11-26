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

package io.github.mucheng.mce.textmodel.util

/**
 * The char table.
 * */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object CharTable {

    /**
     * Empty string.
     * */
    const val EMPTY_STRING: String = ""

    /**
     * Enter char.
     * */
    const val CR: Char = '\r'

    /**
     * New line char.
     * */
    const val LF: Char = '\n'

    /**
     * The system line separator.
     * */
    val CONSTANT_NEW_LINE: String = System.lineSeparator()

    /**
     * Enter & New line string.
     * */
    const val CRLF: String = "\r\n"

    /**
     * Empty char.
     * */
    const val NULL: Char = '\u0000'

}