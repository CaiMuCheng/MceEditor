package io.github.mucheng.mce.textmodel.exception

@Suppress("unused")
class ColumnOutOfBoundsException : IndexOutOfBoundsException {

    constructor(row: Int) : super("Row out of range: $row")

    constructor(string: String?) : super(string)

    constructor() : super()

}