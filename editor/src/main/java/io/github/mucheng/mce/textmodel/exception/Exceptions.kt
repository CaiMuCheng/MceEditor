package io.github.mucheng.mce.textmodel.exception

import kotlin.IndexOutOfBoundsException

open class IndexOutOfBoundsException(string: String?) : IndexOutOfBoundsException(string) {

    constructor(index: Int) : this("Index out of range: $index")

}

open class LineOutOfBoundsException(string: String?) : IndexOutOfBoundsException(string) {

    constructor(line: Int) : this("Line out of range: $line")

}

open class ColumnOutOfBoundsException(string: String?) : IndexOutOfBoundsException(string) {

    constructor(line: Int) : this("Column out of range: $line")

}