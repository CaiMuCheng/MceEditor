package io.github.mucheng.mce.textmodel.position

data class CharPosition(var line: Int, var column: Int, var index: Int) {

    companion object {

        fun createZero(): CharPosition {
            return CharPosition(line = 1, column = 0, index = 0)
        }

    }

}