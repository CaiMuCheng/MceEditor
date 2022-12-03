package io.github.mucheng.mce.textmodel.position

data class TextRange(var start: CharPosition, var end: CharPosition) {

    companion object {

        @JvmStatic
        fun createZero(): TextRange {
            return TextRange(CharPosition.createZero(), CharPosition(1, 1, 1))
        }

        @JvmStatic
        fun createNoIndex(): TextRange {
            return TextRange(CharPosition(1, 0, -1), CharPosition(1, 1, -1))
        }

    }

    fun from(textRange: TextRange) {
        start.from(textRange.start)
        end.from(textRange.end)
    }

}
