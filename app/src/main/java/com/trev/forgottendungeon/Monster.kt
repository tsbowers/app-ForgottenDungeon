package com.trev.forgottendungeon


// Represents a monster type.
// minGold/maxGold: gold awarded on a successful fight.
// requiredRoll: minimum 2d6 roll (2-12) needed to beat this monster.
class Monster(val name: String, val minGold: Int, val maxGold: Int, val requiredRoll: Int)

// All monster types that can appear. monsterList.random() picks one per encounter.
// Rough 2d6 win-odds for reference: 3+ ~97%, 5+ ~75%, 7+ ~58%, 9+ ~28%, 11+ ~8%
val monsterList = listOf(
    Monster("Goblin", 5, 15, requiredRoll = 5),
    Monster("Skeleton", 10, 25, requiredRoll = 7),
    Monster("Dungeon Slime", 1, 5, requiredRoll = 3),
    Monster("Earth Elemental", 15, 30, requiredRoll = 9),
    Monster("Dragon", 50, 69, requiredRoll = 12)
)

// Fixed difficulty for escaping any monster by running instead of fighting.
const val RUN_REQUIRED_ROLL = 5