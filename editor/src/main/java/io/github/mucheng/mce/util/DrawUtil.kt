/*
 * An experimental code editor library on Android.
 * https://github.com/CaiMuCheng/MceEditor
 * Copyright (c) 2022 CaiMuCheng - All rights reserved
 *
 * This library is free software. You can redistribute it or
 * modify it under the terms of the Mozilla Public
 * License Version 2.0 by the Mozilla.
 *
 * You can use it for commercial purposes, but you must
 * know the copyright's owner is author and mark the copyright
 * with author in your project.
 *
 * Do not without the author, the license, the repository link.
 */

package io.github.mucheng.mce.util

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build

object DrawUtil {

    @JvmStatic
    fun drawTextRun(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        contextStart: Int,
        contextEnd: Int,
        x: Float,
        y: Float,
        isRtl: Boolean,
        paint: Paint
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            canvas.drawTextRun(text, start, end, contextStart, contextEnd, x, y, isRtl, paint)
        } else {
            canvas.drawText(
                text, start, end, x, y, paint
            )
        }
    }

    @JvmStatic
    fun drawTextRun(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        y: Float,
        isRtl: Boolean,
        paint: Paint
    ) {
        return drawTextRun(
            canvas, text, start, end, start, end, x, y, isRtl, paint
        )
    }

}