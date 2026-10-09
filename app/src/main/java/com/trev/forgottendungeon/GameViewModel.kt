package com.trev.forgottendungeon

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

const val EXTRA_LIFE_PRICE = 100
const val ROLL_BOOST_PRICE = 50

data class EncounterLog(val monster: String, val lines: List<String>)

enum class Screen { MENU, SHOP, LETTER, ENCOUNTER, GAME_OVER }

class GameViewModel(app: Application) : AndroidViewModel(app) {
    private val save = SaveRepository(app)

    var screen by mutableStateOf(Screen.MENU)
        private set

    // Saved values (shown on the menu and in the shop)
    var savedGold by mutableIntStateOf(0)
        private set
    var savedLives by mutableIntStateOf(0)
        private set
    var savedBoosts by mutableIntStateOf(0)
        private set

    // Current run values (Player isn't observable, so mirror them here)
    var gold by mutableIntStateOf(0)
        private set
    var lives by mutableIntStateOf(0)
        private set
    var boosts by mutableIntStateOf(0)
        private set

    var boostArmed by mutableStateOf(false)
        private set
    var reviveOffer by mutableStateOf(false)
        private set

    var letter by mutableStateOf<Letter?>(null)
        private set
    var monster by mutableStateOf(monsterList.random())
        private set
    var resultLines by mutableStateOf<List<String>>(emptyList())
        private set
    var awaitingNext by mutableStateOf(false)
        private set
    var died by mutableStateOf(false)
        private set
    var shopMessage by mutableStateOf("")
        private set

    val history = mutableStateListOf<EncounterLog>()

    private lateinit var player: Player
    private val defeated = mutableListOf<String>()
    private var avengeTarget = "none"

    init {
        refreshSaved()
    }

    private fun refreshSaved() {
        val data = save.load()
        savedGold = data.gold
        savedLives = data.extraLives
        savedBoosts = data.rollBoosts
    }

    private fun syncFromPlayer() {
        gold = player.gold
        lives = player.extraLives
        boosts = player.rollBoosts
    }

    // ---------- Shop ----------

    fun openShop() {
        shopMessage = ""
        refreshSaved()
        screen = Screen.SHOP
    }

    fun closeShop() {
        refreshSaved()
        screen = Screen.MENU
    }

    fun buyExtraLife() {
        if (savedGold < EXTRA_LIFE_PRICE) {
            shopMessage = "Not enough gold."
            return
        }
        save.saveShop(savedGold - EXTRA_LIFE_PRICE, savedLives + 1, savedBoosts)
        refreshSaved()
        shopMessage = "Extra Life purchased!"
    }

    fun buyRollBoost() {
        if (savedGold < ROLL_BOOST_PRICE) {
            shopMessage = "Not enough gold."
            return
        }
        save.saveShop(savedGold - ROLL_BOOST_PRICE, savedLives, savedBoosts + 1)
        refreshSaved()
        shopMessage = "+1 Roll purchased!"
    }

    // ---------- Run ----------

    fun startRun(name: String) {
        val data = save.load()
        player = Player(
            name.ifBlank { "Adventurer" },
            data.gold,
            data.extraLives,
            data.rollBoosts
        )
        defeated.clear()
        history.clear()
        syncFromPlayer()
        boostArmed = false
        reviveOffer = false
        avengeTarget = "none"
        if (data.killer != "none") {
            val l = buildLetter(data.gold, data.defeated, data.killer)
            avengeTarget = l.avengeTarget
            letter = l
            screen = Screen.LETTER
        } else {
            beginExploring()
        }
    }

    fun beginExploring() {
        nextMonster()
        screen = Screen.ENCOUNTER
    }

    private fun nextMonster() {
        monster = monsterList.random()
        resultLines = emptyList()
        awaitingNext = false
        boostArmed = false
        reviveOffer = false
    }

    // Tap once to arm a +1 Roll for the next roll, tap again to cancel.
    fun toggleBoost() {
        if (player.rollBoosts > 0 && !awaitingNext && !reviveOffer) {
            boostArmed = !boostArmed
        }
    }

    fun onChoice(fight: Boolean) {
        val useBoost = boostArmed && player.rollBoosts > 0
        val r = resolveEncounter(player, monster, fight, defeated, avengeTarget, useBoost)
        boostArmed = false
        resultLines = r.lines
        syncFromPlayer()
        when {
            r.survived -> awaitingNext = true
            player.extraLives > 0 -> reviveOffer = true   // ask the player if they want to revive
            else -> finishDeath()
        }
    }

    fun revive() {
        if (!reviveOffer || player.extraLives <= 0) return
        player.extraLives -= 1
        syncFromPlayer()
        resultLines = resultLines + "Your Extra Life revives you!"
        reviveOffer = false
        awaitingNext = true
    }

    fun declineRevive() = finishDeath()

    private fun finishDeath() {
        died = true
        reviveOffer = false
        save.saveRun(player.gold, defeated, monster.name, player.extraLives, player.rollBoosts)
        screen = Screen.GAME_OVER
    }

    fun continueExploring() {
        history.add(0, EncounterLog(monster.name, resultLines))
        nextMonster()
    }

    fun retreat() {
        died = false
        save.saveRun(player.gold, emptyList(), "none", player.extraLives, player.rollBoosts)
        screen = Screen.GAME_OVER
    }

    fun backToMenu() {
        refreshSaved()
        died = false
        screen = Screen.MENU
    }
}