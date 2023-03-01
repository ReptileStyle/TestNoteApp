package com.example.vkaudionotes.ui.components.util

import java.util.concurrent.TimeUnit

fun formatMilli(milli:Long):String{
    return String.format(
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(milli), // The change is in this line
        TimeUnit.MILLISECONDS.toSeconds(milli) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milli))
    )
}