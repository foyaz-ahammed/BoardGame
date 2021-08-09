package com.exam.board

import android.os.Looper

fun Int.getColumnNo() = this % 4
fun String.toIntList(): List<Int>{
    //Remove brace
    val plainString = substring(1, length - 1)
    val parts = plainString.split(", ")
    return parts.map { it.toInt() }
}

//If callback is on ui thread, run the callback through thread, otherwise run  immediately
fun ensureBackgroundThread(callback: () -> Unit) {
    //Check main thread
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}