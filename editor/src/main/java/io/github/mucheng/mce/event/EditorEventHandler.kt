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

package io.github.mucheng.mce.event

import io.github.mucheng.mce.widget.CodeEditor

open class EditorEventHandler(editor: CodeEditor) {

    private val editor: CodeEditor

    private val sizeChangedEvents = ArrayList<SizeChangedEvent>()

    private val onDrawEvents = ArrayList<OnDrawEvent>()

    init {
        this.editor = editor
    }

    open fun <T : Event, V : T> subscribeEvent(targetClass: Class<T>, event: V) {
        when (targetClass) {
            SizeChangedEvent::class.java -> sizeChangedEvents.add(event as SizeChangedEvent)
            OnDrawEvent::class.java -> onDrawEvents.add(event as OnDrawEvent)
        }
        event.onSubscribe()
    }

    open fun <T : Event, V : T> unsubscribeEvent(targetClass: Class<T>, event: V) {
        when (targetClass) {
            SizeChangedEvent::class.java -> sizeChangedEvents.remove(event as SizeChangedEvent)
            OnDrawEvent::class.java -> onDrawEvents.remove(event as OnDrawEvent)
        }
        event.onUnsubscribe()
    }

    open fun dispatchOnSizeChangedEvent(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        sizeChangedEvents.forEach {
            it.onSizeChanged(width, height, oldWidth, oldHeight)
        }
    }

    open fun dispatchBeforeOnDrawEvent() {
        onDrawEvents.forEach {
            it.onDrawBefore()
        }
    }

    open fun dispatchAfterOnDrawEvent() {
        onDrawEvents.forEach {
            it.onDrawAfter()
        }
    }

}