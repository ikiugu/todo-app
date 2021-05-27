package com.task.noteapp.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.sql.Date
import java.text.SimpleDateFormat

@Entity(tableName = "notes_table")
@Parcelize
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val imageUrl: String = "",
    val completed: Boolean = false,
    val created: Long = System.currentTimeMillis(),
    val updated: Long = System.currentTimeMillis(),
) : Parcelable {
    val formattedCreatedDate: String
        get() {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
            val date = Date(created)
            return simpleDateFormat.format(date)
        }
}