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

package io.github.mucheng.mce.util

import android.util.Log

/**
 * The log util class,
 * you can use it to print log.
 * @sample ```
 * Logger("CustomName").e("Hi")
 * ```
 * @author CaiMuCheng
 * */
@Suppress("unused")
class Logger private constructor(name: String) {

    companion object Invoker {

        @JvmStatic
        private val map: MutableMap<String, Logger> = HashMap()

        @JvmStatic
        operator fun invoke(name: String): Logger {
            synchronized(Logger::class.java) {
                var value = map[name]
                if (value == null) {
                    value = Logger(name)
                    map[name] = value
                }
                return value
            }
        }

    }

    private val name: String

    private var isEnabled: Boolean

    init {
        this.name = name
        this.isEnabled = true
    }

    fun getName(): String {
        return this.name
    }

    fun setEnabled(isEnabled: Boolean) {
        this.isEnabled = isEnabled
    }

    fun isEnabled(): Boolean {
        return this.isEnabled
    }

    fun d(message: String) {
        if (isEnabled) {
            Log.d(name, message)
        }
    }

    fun d(message: String, vararg format: Any?) {
        if (isEnabled) {
            Log.d(name, String.format(message, format))
        }
    }

    fun i(message: String) {
        if (isEnabled) {
            Log.i(name, message)
        }
    }

    fun i(message: String, vararg format: Any?) {
        if (isEnabled) {
            Log.i(name, String.format(message, format))
        }
    }

    fun v(message: String) {
        if (isEnabled) {
            Log.v(name, message)
        }
    }

    fun v(message: String, vararg format: Any?) {
        if (isEnabled) {
            Log.v(name, String.format(message, format))
        }
    }

    fun w(message: String) {
        if (isEnabled) {
            Log.w(name, message)
        }
    }

    fun w(message: String, vararg format: Any?) {
        if (isEnabled) {
            Log.w(name, String.format(message, format))
        }
    }

    fun e(message: String) {
        if (isEnabled) {
            Log.e(name, message)
        }
    }

    fun e(message: String, vararg format: Any?) {
        if (isEnabled) {
            Log.e(name, String.format(message, format))
        }
    }

    fun e(message: String, e: Throwable) {
        if (isEnabled) {
            Log.e(name, message, e)
        }
    }

    fun e(message: String, e: Throwable, vararg format: Any?) {
        if (isEnabled) {
            Log.e(name, String.format(message, format), e)
        }
    }

}