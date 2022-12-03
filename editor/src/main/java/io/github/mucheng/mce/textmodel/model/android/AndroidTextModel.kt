package io.github.mucheng.mce.textmodel.model.android

import io.github.mucheng.mce.textmodel.annoations.UnsafeApi
import io.github.mucheng.mce.textmodel.model.TextModel
import io.github.mucheng.mce.textmodel.util.CharTable

@Suppress("unused")
open class AndroidTextModel(capacity: Int) : TextModel(capacity) {

    companion object {

        private const val DEFAULT_ROW_CAPACITY = 50

        /**
         * Create AndroidTextModel from charSequence
         * @param charSequence text
         * @return the created AndroidTextModel
         * */
        @JvmStatic
        @JvmName("newInstance")
        operator fun invoke(charSequence: CharSequence): AndroidTextModel {
            val textModel = AndroidTextModel()
            textModel.append(charSequence)
            return textModel
        }

        /**
         * Create AndroidTextModel from list of charSequence
         * @param charSequenceList text of lines
         * @return the created AndroidTextModel
         * */
        @JvmStatic
        @JvmName("newInstance")
        operator fun invoke(charSequenceList: List<CharSequence>): AndroidTextModel {
            val textModel = AndroidTextModel(charSequenceList.size)
            val size = charSequenceList.size
            var index = 0
            while (index < size) {
                textModel.append(charSequenceList[index])
                if (index + 1 < size) {
                    textModel.append(CharTable.LF_STRING)
                }
                ++index
            }
            return textModel
        }

        /**
         * Create an empty TextModel
         * @return the created TextModel
         * */
        @JvmStatic
        @JvmName("newInstance")
        operator fun invoke(): AndroidTextModel {
            return AndroidTextModel(DEFAULT_ROW_CAPACITY)
        }

    }

    override fun createTextRow(capacity: Int): AndroidTextRow {
        return AndroidTextRow(capacity)
    }

    override fun getTextRow(line: Int): AndroidTextRow {
        return super.getTextRow(line) as AndroidTextRow
    }

    @UnsafeApi
    override fun getTextRowUnsafe(line: Int): AndroidTextRow {
        return super.getTextRowUnsafe(line) as AndroidTextRow
    }

}