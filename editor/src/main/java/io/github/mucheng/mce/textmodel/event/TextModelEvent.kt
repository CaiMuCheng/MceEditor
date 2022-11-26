package io.github.mucheng.mce.textmodel.event

interface TextModelEvent {

    fun beforeInsert(
        line: Int,
        column: Int,
        charSequence: CharSequence
    ) {}

    fun beforeDelete(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {}

    fun afterInsert(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    )

    fun afterDelete(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    )

}