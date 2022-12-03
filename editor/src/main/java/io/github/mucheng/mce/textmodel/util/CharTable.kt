package io.github.mucheng.mce.textmodel.util

@Suppress("unused")
object CharTable {

    const val NULL = '\u0000'

    const val NULL_STRING = "\u0000"

    const val CR = '\r'

    const val CR_STRING = "\r"

    const val LF = '\n'

    const val LF_STRING = "\n"

    const val CRLF = "\r\n"

    val systemLineSeparator = System.lineSeparator() ?: "\n"

}