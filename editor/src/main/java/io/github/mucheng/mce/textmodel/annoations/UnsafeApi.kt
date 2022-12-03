package io.github.mucheng.mce.textmodel.annoations

/**
 * The annotation attach to the unsafe APIs.
 * */
@RequiresOptIn(
    message = "This API is unsafe, you should not use it.",
    level = RequiresOptIn.Level.WARNING
)
annotation class UnsafeApi