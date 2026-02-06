package com.giiboy.fishcatchgame.databaseAPP

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "fish")
data class Fish(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val point: Int
)
