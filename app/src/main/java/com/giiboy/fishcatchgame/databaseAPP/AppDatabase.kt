package com.giiboy.fishcatchgame.databaseAPP

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Fish::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fishDao(): FishDao
}