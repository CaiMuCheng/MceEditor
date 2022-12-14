import com.android.build.gradle.tasks.GenerateBuildConfig
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

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

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "io.github.mucheng.mce"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.withType<GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    api("androidx.annotation:annotation:${Versions.androidxAnnotationVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutineVersion}")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}