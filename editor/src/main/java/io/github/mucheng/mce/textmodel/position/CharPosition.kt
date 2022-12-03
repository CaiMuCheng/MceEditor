package io.github.mucheng.mce.textmodel.position

data class CharPosition(var line: Int, var column: Int, var index: Int) {

    companion object {

        @JvmStatic
        fun createZero(): CharPosition {
            return CharPosition(1, 0, 0)
        }

        @JvmStatic
        fun createNoIndex(): CharPosition {
            return CharPosition(1, 0, -1)
        }

    }

    fun from(charPosition: CharPosition) {
        this.line = charPosition.line
        this.column = charPosition.column
        this.index = charPosition.index
    }

}