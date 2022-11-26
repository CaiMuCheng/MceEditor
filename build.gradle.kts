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

import com.android.build.gradle.BaseExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.0.0-alpha08" apply false
    id("com.android.library") version "8.0.0-alpha08" apply false
    id("org.jetbrains.kotlin.android") version Versions.kotlinVersion apply false
}

fun Project.configureBaseExtension() {
    extensions.findByType(BaseExtension::class)?.run {
        compileSdkVersion(Versions.compileSdkVersion)
        buildToolsVersion = Versions.buildToolsVersion

        defaultConfig {
            minSdk = Versions.minSdkVersion
            targetSdk = Versions.targetSdkVersion
            versionCode = Versions.versionCode
            versionName = Versions.versionName
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }
}

subprojects {
    plugins.withId("com.android.application") {
        configureBaseExtension()
    }
    plugins.withId("com.android.library") {
        configureBaseExtension()
    }
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}

val excludeSubmoduleName = arrayOf("app", "buildSrc")

tasks.register("bundleAll") {
    group = "CaiMuCheng"
    allprojects
        .filter { it.name !in excludeSubmoduleName }
        .forEach { dependsOn(it.getTasksByName("bundleReleaseAar", false)) }
}