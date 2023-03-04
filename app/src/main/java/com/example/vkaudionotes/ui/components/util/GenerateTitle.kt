package com.example.vkaudionotes.ui.components.util

import java.io.File

fun generateTitle(dir:String): String {
    var title = "Новая запись.3gpp"
    var i = 1
    while (File(dir, title).exists()) {
        title = "Новая запись($i).3gpp"
        i++
    }
    return title
}