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
    val length2: Long =-1L,
    val date: Long = -1L,
    val title: String = "Default title",
) : Parcelable