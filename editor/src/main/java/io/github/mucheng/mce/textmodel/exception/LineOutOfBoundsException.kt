package io.github.mucheng.mce.textmodel.exception

@Suppress("unused")
class LineOutOfBoundsException : IndexOutOfBoundsException {

    constructor(column: Int) : super("Column out of range: $column")

    constructor(string: String?) : super(string)

    constructor() : super()

}