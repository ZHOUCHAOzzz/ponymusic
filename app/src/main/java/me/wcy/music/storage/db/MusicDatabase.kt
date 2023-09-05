package me.wcy.music.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase
import me.wcy.music.storage.db.dao.PlaylistDao
import me.wcy.music.storage.db.entity.SongEntity

/**
 * Created by wangchenyan.top on 2023/8/29.
 */
@Database(
    entities = [
        SongEntity::class,
    ],
    version = 1
)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun playlistDao(): PlaylistDao
}