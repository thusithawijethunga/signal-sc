package com.widhura.signalxp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vip_members")
data class VipMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rank: Int = 1,
    val name: String,
    val memberId: String = "—", // e.g. SX1043 or "—"
    val lots: Double,
    val progressFraction: Float = 0.5f, // Proportional bar length
    val accentHex: Long = 0xFFF59E0B, // Color for underline & value
    val period: String = "MONTHLY", // "MONTHLY", "ALL_TIME", "WEEKLY"
    val winRate: Double = 82.0,
    val totalTrades: Int = 50,
    val broker: String = "Exness VIP",
    val favoritePair: String = "XAU/USD"
)
