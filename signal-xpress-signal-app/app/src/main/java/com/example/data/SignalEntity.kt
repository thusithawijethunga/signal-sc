package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signals")
data class SignalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val no: Int,
    val date: String, // YYYY-MM-DD
    val pair: String, // EUR/USD, XAU/USD, GBP/JPY etc.
    val type: String, // BUY / SELL
    val entry: String,
    val tp1: String,
    val tp2: String,
    val tp3: String,
    val tp4: String,
    val sl: String,
    val pips: Int,
    val profit: Double,
    val hitLevel: String, // "1", "2", "3", "4", "BE", "SL", "CLOSE", "NONE"
    val status: String,
    val result: String, // "WIN", "LOSS", "RUNNING"
    val thumbsCount: Int = 0,
    val fireCount: Int = 0,
    val rocketCount: Int = 0,
    val brokenHeartCount: Int = 0,
    val userReactedEmoji: String? = null
)
