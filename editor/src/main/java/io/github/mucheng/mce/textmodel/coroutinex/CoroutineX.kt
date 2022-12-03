package io.github.mucheng.mce.textmodel.coroutinex

import io.github.mucheng.mce.textmodel.model.TextRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

fun TextRow.asFlow(): Flow<Char> = charIterator().asFlow()