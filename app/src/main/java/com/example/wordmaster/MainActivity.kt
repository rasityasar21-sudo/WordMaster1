package com.example.wordmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray

data class Word(val en: String, val tr: String, val level: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("wordmaster", MODE_PRIVATE)
        val words = try {
            val json = assets.open("dictionary.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val item = arr.getJSONObject(i)
                Word(item.optString("en"), item.optString("tr"), item.optString("level", "A1"))
            }
        } catch (_: Exception) { listOf(Word("hello", "merhaba", "A1"), Word("book", "kitap", "A1")) }
        setContent { WordMasterApp(words, prefs) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordMasterApp(words: List<Word>, prefs: android.content.SharedPreferences) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("Tümü") }
    var tab by remember { mutableIntStateOf(0) }
    var xp by remember { mutableIntStateOf(prefs.getInt("xp", 0)) }
    var coins by remember { mutableIntStateOf(prefs.getInt("coins", 0)) }
    val learned = remember { mutableStateListOf<String>().apply { addAll(prefs.getStringSet("learned", emptySet()) ?: emptySet()) } }
    val favorites = remember { mutableStateListOf<String>().apply { addAll(prefs.getStringSet("favorites", emptySet()) ?: emptySet()) } }
    val filtered = words.filter {
        (selected == "Tümü" || it.level == selected) &&
        (it.en.contains(query, true) || it.tr.contains(query, true)) &&
        (tab != 1 || it.en in favorites) && (tab != 2 || it.en in learned)
    }
    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("WordMaster") }) }) { padding ->
            Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
                Text("XP: $xp     Coin: $coins", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                TabRow(selectedTabIndex = tab) {
                    listOf("Kelimeler", "Favoriler", "Öğrendiklerim").forEachIndexed { index, label ->
                        Tab(selected = tab == index, onClick = { tab = index }, text = { Text(label) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text("İngilizce veya Türkçe ara") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Tümü", "A1", "A2", "B1", "B2", "C1").forEach { level ->
                        FilterChip(selected = selected == level, onClick = { selected = level }, label = { Text(level) })
                    }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(filtered, key = { it.en }) { word ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(word.en, style = MaterialTheme.typography.titleMedium)
                                Text(word.tr)
                                Text("Seviye: " + word.level, style = MaterialTheme.typography.labelSmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = {
                                        if (word.en in learned) learned.remove(word.en) else { learned.add(word.en); xp += 5; prefs.edit().putInt("xp", xp).putStringSet("learned", learned.toSet()).apply() }
                                    }) { Text(if (word.en in learned) "Öğrendim ✓" else "Öğrendim") }
                                    TextButton(onClick = {
                                        if (word.en in favorites) favorites.remove(word.en) else favorites.add(word.en)
                                        prefs.edit().putStringSet("favorites", favorites.toSet()).apply()
                                    }) { Text(if (word.en in favorites) "★ Favori" else "☆ Favori") }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { coins += 1; xp += 10; prefs.edit().putInt("coins", coins).putInt("xp", xp).apply() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Günlük mini test (+10 XP, +1 coin)")
                }
            }
        }
    }
}
