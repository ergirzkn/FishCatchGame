package com.giiboy.fishcatchgame.databaseAPP

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FishDao {

    @Insert
    fun insert(fish: Fish)

    // TOTAL SCORE
    @Query("SELECT SUM(point) FROM fish")
    fun getTotalPoint(): Int?

    // HIGH SCORE
    @Query("SELECT MAX(point) FROM fish")
    fun getHighScore(): Int?
}