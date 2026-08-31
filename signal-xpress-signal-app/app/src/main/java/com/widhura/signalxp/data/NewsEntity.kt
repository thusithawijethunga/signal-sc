package com.widhura.signalxp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_news")
data class NewsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val time: String,
    val currency: String,
    val title: String,
    val impact: String, // "HIGH", "MEDIUM", "LOW"
    val forecast: String,
    val previous: String,
    val actual: String = "",
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
