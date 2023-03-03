package com.example.vkaudionotes.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime


@Parcelize
@Entity(tableName = "notes_table")
data class AudioNote(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val length:String = "0:00",
    val date: String = "неизвестно",
    val title: String = "Default title"
) : Parcelable {
    val name
    get()="$title.3gpp"
}