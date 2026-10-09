package com.trev.forgottendungeon

import kotlin.random.Random

data class EncounterResult(
    val survived: Boolean,
    val roll: Int,
    val goldFound: Int = 0,
    val lines: List<String>   // text for the screen to display
)

fun rollTwoDice(): Int = Random.nextInt(1, 7) + Random.nextInt(1, 7)

// useBoost: the player chose to spend one +1 Roll before rolling.
// A failed roll always returns survived = false; the ViewModel decides whether an Extra Life can revive.
fun resolveEncounter(
    player: Player,
    monster: Monster,
    fight: Boolean,
    defeatedMonsters: MutableList<String>,
    avengeTarget: String,
    useBoost: Boolean
): EncounterResult {
    val base = rollTwoDice()
    val vengeance = fight && monster.name == avengeTarget

    val parts = mutableListOf<String>()
    var bonus = 0
    if (useBoost && player.rollBoosts > 0) {
        player.rollBoosts -= 1
        bonus += 1
        parts += "1 from your +1 Roll"
    }
    if (vengeance) {
        bonus += 1
        parts += "1 for vengeance"
    }

    val roll = base + bonus
    val rollLine = if (parts.isEmpty()) "You rolled a $roll!"
    else "You rolled a $base, plus ${parts.joinToString(" and ")}: $roll!"

    if (!fight) {
        return if (roll >= RUN_REQUIRED_ROLL) {
            EncounterResult(true, roll, 0, listOf(
                rollLine,
                "You escape from the ${monster.name} safely."
            ))
        } else {
            EncounterResult(false, roll, 0, listOf(
                rollLine,
                "The ${monster.name} catches you as you flee.",
                "You have died."
            ))
        }
    }

    return if (roll >= monster.requiredRoll) {
        val gold = Random.nextInt(monster.minGold, monster.maxGold + 1)
        player.gold += gold
        defeatedMonsters.add(monster.name)
        EncounterResult(true, roll, gold, listOf(
            rollLine,
            "You defeated the ${monster.name}!",
            "You found $gold gold. Total gold: ${player.gold}"
        ))
    } else {
        EncounterResult(false, roll, 0, listOf(
            rollLine,
            "The ${monster.name} was too strong.",
            "You have died."
        ))
    }
}