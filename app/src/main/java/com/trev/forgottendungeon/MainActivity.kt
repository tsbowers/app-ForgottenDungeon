package com.trev.forgottendungeon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF14110F)
private val CardBg = Color(0xFF241E1A)
private val GoldColor = Color(0xFFE0B04A)
private val Muted = Color(0xFFB9AFA3)
private val Blood = Color(0xFFD65A5A)

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = GoldColor,
                    onPrimary = Color(0xFF1A1307),
                    background = Bg,
                    surface = Bg,
                    onSurface = Color(0xFFEDE6DB)
                )
            ) {
                Surface(Modifier.fillMaxSize(), color = Bg) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (vm.screen) {
                            Screen.MENU -> MenuScreen(vm)
                            Screen.SHOP -> ShopScreen(vm)
                            Screen.LETTER -> LetterScreen(vm)
                            Screen.ENCOUNTER -> EncounterScreen(vm)
                            Screen.GAME_OVER -> GameOverScreen(vm)
                        }
                    }
                }
            }
        }
    }
}

// ---------- Shared pieces ----------

@Composable
fun WideButton(
    text: String,
    onClick: () -> Unit,
    outlined: Boolean = false,
    enabled: Boolean = true
) {
    val mod = Modifier.fillMaxWidth().height(52.dp)
    if (outlined) {
        OutlinedButton(onClick = onClick, modifier = mod, enabled = enabled) {
            Text(text, fontSize = 16.sp)
        }
    } else {
        Button(onClick = onClick, modifier = mod, enabled = enabled) {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(Modifier.padding(20.dp)) { content() }
    }
}

@Composable
fun GoldRow(gold: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text("Gold: $gold", color = GoldColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatsRow(gold: Int, lives: Int, boosts: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Text("Gold: $gold", color = GoldColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Lives: $lives", color = Muted, fontSize = 16.sp)
        Text("+1 Rolls: $boosts", color = Muted, fontSize = 16.sp)
    }
}

// ---------- Screens ----------

@Composable
fun MenuScreen(vm: GameViewModel) {
    var name by remember { mutableStateOf("") }
    Spacer(Modifier.height(48.dp))
    Text(
        "THE FORGOTTEN\nDUNGEON",
        color = GoldColor,
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 3.sp,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(8.dp))
    Text("Fame, fortune and luck await.", color = Muted)
    Spacer(Modifier.height(32.dp))
    InfoCard { StatsRow(vm.savedGold, vm.savedLives, vm.savedBoosts) }
    Spacer(Modifier.height(24.dp))
    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Your name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(16.dp))
    WideButton("Start Game", { vm.startRun(name) })
    Spacer(Modifier.height(12.dp))
    WideButton("Shop", { vm.openShop() }, outlined = true)
}

@Composable
fun ShopItem(
    title: String,
    description: String,
    status: String,
    price: Int,
    enabled: Boolean,
    onBuy: () -> Unit
) {
    InfoCard {
        Text(title, color = GoldColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(description)
        Spacer(Modifier.height(4.dp))
        Text(status, color = Muted)
        Spacer(Modifier.height(12.dp))
        WideButton("Buy for $price gold", onBuy, enabled = enabled)
    }
}

@Composable
fun ShopScreen(vm: GameViewModel) {
    Spacer(Modifier.height(24.dp))
    Text(
        "THE SHOP",
        color = GoldColor,
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 3.sp
    )
    Spacer(Modifier.height(16.dp))
    GoldRow(vm.savedGold)
    Spacer(Modifier.height(24.dp))
    ShopItem(
        title = "Extra Life",
        description = "When a roll would kill you, you can use this to revive and keep going. One use.",
        status = "Owned: ${vm.savedLives}",
        price = EXTRA_LIFE_PRICE,
        enabled = vm.savedGold >= EXTRA_LIFE_PRICE,
        onBuy = { vm.buyExtraLife() }
    )
    Spacer(Modifier.height(16.dp))
    ShopItem(
        title = "+1 Roll",
        description = "Use it before a roll to add +1 to that roll. One use.",
        status = "Owned: ${vm.savedBoosts}",
        price = ROLL_BOOST_PRICE,
        enabled = vm.savedGold >= ROLL_BOOST_PRICE,
        onBuy = { vm.buyRollBoost() }
    )
    if (vm.shopMessage.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Text(vm.shopMessage, color = GoldColor, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(24.dp))
    WideButton("Back to menu", { vm.closeShop() }, outlined = true)
}

@Composable
fun LetterScreen(vm: GameViewModel) {
    val l = vm.letter
    Spacer(Modifier.height(16.dp))
    Text("A letter is waiting for you at the entrance...", color = Muted, textAlign = TextAlign.Center)
    Spacer(Modifier.height(16.dp))
    if (l != null) {
        InfoCard {
            Text("A note left by a fallen adventurer", color = GoldColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("They were slain by a ${l.killer}.")
            Spacer(Modifier.height(8.dp))
            if (l.gold > 0) {
                Text("They amassed ${l.gold} gold before falling, and left it all to you.")
            } else {
                Text("They had no gold left to give you.")
            }
            Spacer(Modifier.height(12.dp))
            if (l.kills.isEmpty()) {
                Text("They fell before defeating anything.", color = Muted)
            } else {
                Text("They defeated:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                l.kills.forEach { (monster, count) ->
                    Text("\u2022 $count\u00D7 $monster")
                }
            }
            if (l.avengeTarget != "none") {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Avenge them: +1 to your roll when you fight the ${l.avengeTarget}!",
                    color = Blood,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    WideButton("Enter the dungeon", { vm.beginExploring() })
}

@Composable
fun EncounterScreen(vm: GameViewModel) {
    val m = vm.monster
    StatsRow(vm.gold, vm.lives, vm.boosts)
    Spacer(Modifier.height(16.dp))
    InfoCard {
        Text("A wild appears!", color = Muted)
        Spacer(Modifier.height(4.dp))
        Text(m.name, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = GoldColor)
        Spacer(Modifier.height(8.dp))
        Text("Fight needs ${m.requiredRoll}+  \u2022  Run needs $RUN_REQUIRED_ROLL+", color = Muted)
    }
    if (vm.resultLines.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        InfoCard {
            vm.resultLines.forEach { Text(it) }
        }
    }
    Spacer(Modifier.height(24.dp))
    when {
        vm.reviveOffer -> {
            Text(
                "You have fallen... but you still carry an Extra Life.",
                color = Blood,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            WideButton("Revive (${vm.lives} Extra Life left)", { vm.revive() })
            Spacer(Modifier.height(12.dp))
            WideButton("Accept your fate", { vm.declineRevive() }, outlined = true)
        }
        vm.awaitingNext -> {
            WideButton("Continue exploring", { vm.continueExploring() })
            Spacer(Modifier.height(12.dp))
            WideButton("Retreat with ${vm.gold} gold", { vm.retreat() }, outlined = true)
        }
        else -> {
            if (vm.boosts > 0) {
                WideButton(
                    text = if (vm.boostArmed) "+1 Roll ready (tap to cancel)"
                    else "Use +1 Roll (${vm.boosts} left)",
                    onClick = { vm.toggleBoost() },
                    outlined = !vm.boostArmed
                )
                Spacer(Modifier.height(12.dp))
            }
            WideButton("Fight", { vm.onChoice(true) })
            Spacer(Modifier.height(12.dp))
            WideButton("Run", { vm.onChoice(false) }, outlined = true)
        }
    }
    if (vm.history.isNotEmpty()) {
        Spacer(Modifier.height(32.dp))
        Text("Earlier in this run", color = Muted, fontWeight = FontWeight.Bold)
        vm.history.forEach { log ->
            Spacer(Modifier.height(12.dp))
            InfoCard {
                Text(log.monster, color = GoldColor, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                log.lines.forEach { Text(it) }
            }
        }
    }
}

@Composable
fun GameOverScreen(vm: GameViewModel) {
    Spacer(Modifier.height(48.dp))
    Text(
        if (vm.died) "YOU DIED" else "YOU RETREATED",
        color = if (vm.died) Blood else GoldColor,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 3.sp
    )
    Spacer(Modifier.height(24.dp))
    if (vm.died && vm.resultLines.isNotEmpty()) {
        InfoCard { vm.resultLines.forEach { Text(it) } }
        Spacer(Modifier.height(16.dp))
    }
    InfoCard { GoldRow(vm.gold) }
    Spacer(Modifier.height(24.dp))
    WideButton("Return to menu", { vm.backToMenu() })
}