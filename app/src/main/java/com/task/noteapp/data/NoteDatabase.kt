package com.task.noteapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Note::class], version = 1)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao


    class Callback @Inject constructor(
        private val database: Provider<NoteDatabase>,
        private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().noteDao()
            /*applicationScope.launch {
                dao.insert(
                    Note(
                        title = "Read Books",
                        description = "This will be the one talking about many books"
                    )
                )
                dao.insert(
                    Note(
                        title = "Cook food",
                        description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec accumsan nec risus non rutrum. Praesent ut leo sit amet nunc consequat tempor a a nisi. Nulla sed erat a justo viverra aliquam eget in tellus. Ut ut turpis dignissim, scelerisque erat non, pellentesque ex. Mauris consectetur imperdiet justo. Nulla erat ante, consectetur at dignissim eu, accumsan non leo. Vestibulum tempor lobortis sapien. Fusce urna eros, maximus nec rhoncus vel, tempus eget turpis. Mauris sit amet nisi sed metus placerat viverra. Donec sagittis urna ac tellus imperdiet tempor. Sed molestie convallis tincidunt. Morbi ac ex fringilla, convallis libero nec, tincidunt lacus. Vestibulum ornare dolor sed ante eleifend auctor."
                    )
                )
            }*/
        }
    }
}