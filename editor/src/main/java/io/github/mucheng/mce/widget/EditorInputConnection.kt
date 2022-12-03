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

import android.view.inputmethod.BaseInputConnection
import io.github.mucheng.mce.textmodel.base.ICursor
import io.github.mucheng.mce.textmodel.model.TextModel
import io.github.mucheng.mce.util.Logger

class EditorInputConnection(editor: CodeEditor) :
    BaseInputConnection(editor, true) {

    companion object {
        private val logger = Logger("EditorInputConnection")
        private const val DEBUG = true
    }

    private val editor: CodeEditor

    init {
        this.editor = editor
    }

    fun getEditor(): CodeEditor {
        return this.editor
    }

    override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
        if (DEBUG) {
            logger.e("Commit text: $text")
        }

        if (!editor.isEditable() || text == null) {
            return false
        }

        commitTextInternal(text)
        editor.invalidate()
        return true
    }

    private fun commitTextInternal(text: CharSequence): CharSequence {
        val textModel = editor.getText()
        val cursor = editor.getCursor()
        if (!cursor.isSelected()) {
            val line = cursor.getLeftLine()
            val column = cursor.getLeftColumn()

            textModel.insert(line, column, text)
            cursor.moveToRight(text.length)
        }
        return text
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        val textModel = editor.getText()
        val cursor = editor.getCursor()
        if (!cursor.isSelected()) {
            deleteTextInternal(textModel, cursor)
        }
        editor.invalidate()
        return true
    }

    @Suppress("unused")
    private fun deleteTextInternal(textModel: TextModel, cursor: ICursor) {
        val line = cursor.getLeftLine()
        val column = cursor.getLeftColumn()


        if (line == 1 && column > 0) {
            val text = textModel.subSequence(line, column - 1, line, column)
            textModel.delete(line, column - 1, line, column)
            cursor.moveToLeft(text.length)
        }

        if (line > 1) {
            if (column == 0) {
                val lastLine = cursor.getLeftLine() - 1
                val lastColumn = textModel.getTextRowSize(lastLine)
                textModel.delete(lastLine, lastColumn, cursor.getLeftLine(), cursor.getLeftColumn())
                cursor.set(lastLine, lastColumn)
            } else {
                val text = textModel.subSequence(line, column - 1, line, column)
                textModel.delete(line, column - 1, line, column)
                cursor.moveToLeft(text.length)
            }
        }
    }


    override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
        return false
    }

}