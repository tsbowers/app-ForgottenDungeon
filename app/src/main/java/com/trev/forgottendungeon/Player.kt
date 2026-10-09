package com.trev.forgottendungeon

// Represents the player character for the current run.
// gold, extraLives and rollBoosts persist across runs via SharedPreferences.
class Player(
    var name: String,
    var gold: Int = 0,
    var extraLives: Int = 0,
    var rollBoosts: Int = 0
)