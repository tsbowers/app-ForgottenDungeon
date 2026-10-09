package com.trev.forgottendungeon

data class Letter(
    val killer: String,
    val gold: Int,
    val kills: List<Pair<String, Int>>,   // monster name to number slain
    val avengeTarget: String
)

fun buildLetter(gold: Int, defeated: List<String>, killer: String): Letter {
    val kills = defeated
        .groupingBy { it }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
    val avenge = if (defeated.isEmpty()) killer else "none"
    return Letter(killer, gold, kills, avenge)
}