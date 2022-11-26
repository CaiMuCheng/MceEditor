package io.github.mucheng.mce.textmodel.exception

import kotlin.IndexOutOfBoundsException

open class IndexOutOfBoundsException : IndexOutOfBoundsException {

    constructor(index: Int) : super("Index out of range: $index")

    constructor() : super()

    constructor(string: String?) : super(string)

}