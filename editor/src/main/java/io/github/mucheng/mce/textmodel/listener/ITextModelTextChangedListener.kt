package io.github.mucheng.mce.textmodel.listener

import io.github.mucheng.mce.textmodel.util.TextChangedType

interface ITextModelTextChangedListener : ITextModelListener {

    fun onTextChanged(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence,
        textChangedType: TextChangedType
    )

    override fun afterInsert(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        super.afterInsert(startLine, startColumn, endLine, endColumn, charSequence)
        // notify update
        onTextChanged(startLine, startColumn, endLine, endColumn, charSequence, TextChangedType.INSERT)
    }

    override fun afterDelete(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        super.afterDelete(startLine, startColumn, endLine, endColumn, charSequence)
        // notify update
        onTextChanged(startLine, startColumn, endLine, endColumn, charSequence, TextChangedType.DELETE)
    }

}