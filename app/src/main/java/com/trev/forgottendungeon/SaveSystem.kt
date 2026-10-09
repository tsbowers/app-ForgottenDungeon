package com.trev.forgottendungeon

import android.content.Context

data class SaveData(
    val gold: Int,
    val defeated: List<String>,
    val killer: String,
    val extraLives: Int,
    val rollBoosts: Int
)

class SaveRepository(context: Context) {
    private val prefs = context.getSharedPreferences("forgotten_dungeon", Context.MODE_PRIVATE)

    fun load() = SaveData(
        gold = prefs.getInt("gold", 0),
        defeated = (prefs.getString("defeated", "") ?: "").split(",").filter { it.isNotBlank() },
        killer = prefs.getString("killer", "none") ?: "none",
        extraLives = prefs.getInt("extraLives", 0),
        rollBoosts = prefs.getInt("rollBoosts", 0)
    )

    // Called when a run ends (death or retreat).
    fun saveRun(gold: Int, defeated: List<String>, killer: String, extraLives: Int, rollBoosts: Int) {
        prefs.edit()
            .putInt("gold", gold)
            .putString("defeated", defeated.joinToString(","))
            .putString("killer", killer)
            .putInt("extraLives", extraLives)
            .putInt("rollBoosts", rollBoosts)
            .apply()
    }

    // Called when the player buys something. Leaves the letter data alone.
    fun saveShop(gold: Int, extraLives: Int, rollBoosts: Int) {
        prefs.edit()
            .putInt("gold", gold)
            .putInt("extraLives", extraLives)
            .putInt("rollBoosts", rollBoosts)
            .apply()
    }
}