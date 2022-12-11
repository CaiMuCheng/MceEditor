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

package io.github.mucheng.mce

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.mucheng.mce.databinding.ActivityMainBinding
import io.github.mucheng.mce.textmodel.model.TextModel
import io.github.mucheng.mce.textmodel.model.android.AndroidTextModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val editor = viewBinding.editor
        editor.setLineSpacing(2f, 1.1f)
        editor.setTypeface(Typeface.createFromAsset(assets, "font/JetBrainsMono-Regular.ttf"))

        mainScope.launch(Dispatchers.IO){
            editor.setText(
                assets.open("capacity/vue.js").bufferedReader().readText()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }

}