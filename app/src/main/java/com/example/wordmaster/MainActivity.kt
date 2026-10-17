package com.example.wordmaster

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.animateContentSize
import java.time.LocalDate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray

data class Word(val en: String, val tr: String, val level: String, val example: String = "")

private val Navy = Color(0xFF15264A)
private val Blue = Color(0xFF4568F5)
private val PaleBlue = Color(0xFFEFF3FF)
private val Mint = Color(0xFFDAF7EB)
private val Ink = Color(0xFF172033)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("wordmaster", MODE_PRIVATE)
        val words = try {
            val json = assets.open("dictionary.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val item = arr.getJSONObject(i)
                Word(item.optString("en"), item.optString("tr"), item.optString("level", "A1"), item.optString("example", ""))
            }
        } catch (_: Exception) { listOf(Word("hello", "merhaba", "A1")) }
        setContent { WordMasterApp(words, prefs) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordMasterApp(words: List<Word>, prefs: SharedPreferences) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("Tümü") }
    var tab by remember { mutableIntStateOf(0) }
    var xp by remember { mutableIntStateOf(prefs.getInt("xp", 0)) }
    var coins by remember { mutableIntStateOf(prefs.getInt("coins", 0)) }
    var quizWord by remember { mutableStateOf<Word?>(null) }
    var quizChoices by remember { mutableStateOf<List<Word>>(emptyList()) }
    var quizMessage by remember { mutableStateOf<String?>(null) }
    var quizCount by remember { mutableIntStateOf(0) }
    var quizCorrect by remember { mutableIntStateOf(0) }
    var correctStreak by remember { mutableIntStateOf(prefs.getInt("correctStreak", 0)) }
    var bestCorrectStreak by remember { mutableIntStateOf(prefs.getInt("bestCorrectStreak", 0)) }
    var dailyStreak by remember { mutableIntStateOf(prefs.getInt("dailyStreak", 0)) }
    var lastLoginDate by remember { mutableStateOf(prefs.getString("lastLoginDate", "") ?: "") }
    var sessionAnswered by remember { mutableIntStateOf(0) }
    var sessionCorrect by remember { mutableIntStateOf(0) }
    var sessionActive by remember { mutableStateOf(false) }
    var showQuizSummary by remember { mutableStateOf(false) }
    var quizMode by remember { mutableStateOf("İngilizce → Türkçe") }
    val reviewWords = remember { mutableStateListOf<String>().apply {
        addAll(prefs.getStringSet("reviewWords", emptySet()) ?: emptySet())
    } }
    val level = xp / 100 + 1
    val xpInLevel = xp % 100
    val today = LocalDate.now().toString()
    LaunchedEffect(today) {
        if (lastLoginDate != today) {
            dailyStreak = if (lastLoginDate == LocalDate.now().minusDays(1).toString()) dailyStreak + 1 else 1
            lastLoginDate = today
            coins += 3
            prefs.edit().putInt("dailyStreak", dailyStreak).putString("lastLoginDate", today).putInt("coins", coins).apply()
        }
    }
    val learned = remember { mutableStateListOf<String>().apply { addAll(prefs.getStringSet("learned", emptySet()) ?: emptySet()) } }
    val favorites = remember { mutableStateListOf<String>().apply { addAll(prefs.getStringSet("favorites", emptySet()) ?: emptySet()) } }

    fun startQuiz() {
        if (words.size < 4) return
        val reviewPool = words.filter { it.en in reviewWords }
        val target = if (reviewPool.isNotEmpty() && (0..1).random() == 0) reviewPool.random() else words.random()
        quizWord = target
        quizChoices = (listOf(target) + words.filter { it.en != target.en }.shuffled().take(3)).shuffled()
        quizMessage = null
    }

    val filtered = words.filter {
        (selected == "Tümü" || it.level == selected) &&
        (it.en.contains(query, true) || it.tr.contains(query, true)) &&
        (tab != 1 || it.en in favorites) && (tab != 2 || it.en in learned)
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue, onPrimary = Color.White, background = Color(0xFFF5F7FC),
            surface = Color.White, onSurface = Ink, secondary = Color(0xFF13A879)
        )
    ) {
        Scaffold(containerColor = Color(0xFFF5F7FC)) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column(
                        Modifier.fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Navy, Color(0xFF3858B8))), RoundedCornerShape(28.dp))
                            .padding(22.dp)
                    ) {
                        Text("WORDMASTER", color = Color(0xFFBFD0FF), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(7.dp))
                        Text("Her gün biraz daha\nİngilizce! ✨", color = Color.White, fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(7.dp))
                        Text("Kelime öğren • Tekrar et • İlerle", color = Color.White.copy(alpha = .82f), fontSize = 14.sp)
                        Spacer(Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatPill("⚡", "$xp XP")
                            StatPill("🪙", "$coins coin")
                            StatPill("📚", "${learned.size} kelime")
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = PaleBlue),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("📖", fontSize = 24.sp)
                                Spacer(Modifier.height(7.dp))
                                Text("Kelime hazinesi", fontWeight = FontWeight.Bold, color = Navy)
                                Text("${words.size} kelime", color = Color(0xFF5D6C8A), fontSize = 13.sp)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Mint),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("🎯", fontSize = 24.sp)
                                Spacer(Modifier.height(7.dp))
                                Text("Mini test", fontWeight = FontWeight.Bold, color = Color(0xFF12694D))
                                Text("Bilgini dene", color = Color(0xFF397A66), fontSize = 13.sp)
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StreakCard("🔥", "Günlük seri", "$dailyStreak gün", Color(0xFFFFF0D8), Color(0xFF9A5800), Modifier.weight(1f))
                        StreakCard("⚡", "Doğru serisi", "$correctStreak", Color(0xFFE6F8EE), Color(0xFF13734D), Modifier.weight(1f))
                    }
                }
                item {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Text("🏆 Günlük hedef", fontWeight = FontWeight.Bold, color = Navy); Spacer(Modifier.weight(1f)); Text("${learned.size.coerceAtMost(10)}/10 kelime", color = Blue, fontWeight = FontWeight.Bold) }
                            Spacer(Modifier.height(9.dp))
                            LinearProgressIndicator(progress = { learned.size.coerceAtMost(10) / 10f }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Blue, trackColor = PaleBlue)
                            Spacer(Modifier.height(5.dp))
                            Text("Hedef: her gün 10 kelime öğren. Günlük giriş ödülün: +3 coin.", fontSize = 12.sp, color = Color(0xFF68758E))
                        }
                    }
                }
                item {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐ Seviye $level", fontWeight = FontWeight.ExtraBold, color = Navy, fontSize = 18.sp)
                                Spacer(Modifier.weight(1f))
                                Text("$xpInLevel / 100 XP", color = Blue, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(progress = { xpInLevel / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Blue, trackColor = PaleBlue)
                            Spacer(Modifier.height(6.dp))
                            Text("Her 100 XP'de yeni seviyeye ulaşırsın. Yanlış kelimeler tekrar listene eklenir.", color = Color(0xFF68758E), fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("🏅 Başarılar: ${if (xp >= 100) "İlk seviye ✓" else "İlk seviye: 100 XP"}  •  ${if (bestCorrectStreak >= 5) "5 doğru üst üste ✓" else "Seri rekoru: $bestCorrectStreak"}", color = Navy, fontSize = 12.sp)
                        }
                    }
                }
                item {
                    Text("Test modu", fontWeight = FontWeight.Bold, color = Navy, fontSize = 18.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("İngilizce → Türkçe", "Türkçe → İngilizce").forEach { mode ->
                            FilterChip(selected = quizMode == mode, onClick = { quizMode = mode }, label = { Text(mode, fontSize = 11.sp) })
                        }
                    }
                }
                item {
                    Button(
                        onClick = { sessionActive = true; sessionAnswered = 0; sessionCorrect = 0; showQuizSummary = false; startQuiz() },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(17.dp)
                    ) { Text("🎯  Teste Başla", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                }
                item {
                    Text("Kelime defterim", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
                }
                item {
                    OutlinedTextField(
                        value = query, onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("İngilizce veya Türkçe ara") },
                        placeholder = { Text("Örn. apple, elma...") },
                        singleLine = true,
                        shape = RoundedCornerShape(17.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue, focusedLabelColor = Blue)
                    )
                }
                item {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Tümü", "A1", "A2", "B1", "B2", "C1").forEach { level ->
                            FilterChip(
                                selected = selected == level,
                                onClick = { selected = level },
                                label = { Text(level) },
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }
                item {
                    ScrollableTabRow(selectedTabIndex = tab, edgePadding = 0.dp, containerColor = Color.Transparent, divider = {}) {
                        listOf("Tüm kelimeler", "⭐ Favoriler", "✓ Öğrendiklerim").forEachIndexed { index, label ->
                            Tab(selected = tab == index, onClick = { tab = index }, text = { Text(label, fontWeight = if (tab == index) FontWeight.Bold else FontWeight.Normal) })
                        }
                    }
                }
                if (filtered.isEmpty()) {
                    item {
                        Card(shape = RoundedCornerShape(20.dp)) {
                            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔎", fontSize = 32.sp)
                                Text("Kelime bulunamadı", fontWeight = FontWeight.Bold)
                                Text("Aramayı veya seçtiğin filtreyi değiştir.", color = Color.Gray)
                            }
                        }
                    }
                }
                items(filtered, key = { it.en }) { word ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(Modifier.padding(17.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(word.en.replaceFirstChar { it.uppercase() }, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
                                    Spacer(Modifier.height(3.dp))
                                    Text(word.tr.replaceFirstChar { it.uppercase() }, fontSize = 16.sp, color = Color(0xFF46536D))
                                }
                                Surface(color = PaleBlue, shape = RoundedCornerShape(10.dp)) {
                                    Text(word.level, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (word.example.isNotBlank()) {
                                Spacer(Modifier.height(10.dp))
                                Text("Örnek: ${word.example}", color = Color(0xFF67748C), fontSize = 13.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFF0F2F7))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton(onClick = {
                                    if (word.en in learned) learned.remove(word.en)
                                    else { learned.add(word.en); xp += 5; prefs.edit().putInt("xp", xp).putStringSet("learned", learned.toSet()).apply() }
                                    prefs.edit().putStringSet("learned", learned.toSet()).apply()
                                }) { Text(if (word.en in learned) "✓ Öğrenildi" else "✓ Öğrendim") }
                                Spacer(Modifier.weight(1f))
                                TextButton(onClick = {
                                    if (word.en in favorites) favorites.remove(word.en) else favorites.add(word.en)
                                    prefs.edit().putStringSet("favorites", favorites.toSet()).apply()
                                }) { Text(if (word.en in favorites) "★ Favoride" else "☆ Favori") }
                            }
                        }
                    }
                }
                item {
                    Text("İpucu: Kelimeleri favorilerine ekleyip sonra tekrar çalışabilirsin.", color = Color(0xFF758099), fontSize = 12.sp)
                }
            }
        }
    }

    if (quizWord != null) {
        AlertDialog(
            onDismissRequest = { quizWord = null; quizMessage = null },
            title = { Text("Mini test 🎯", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(
                        if (quizMode == "İngilizce → Türkçe")
                            "“${quizWord!!.en.replaceFirstChar { it.uppercase() }}” kelimesinin Türkçesi nedir?"
                        else "“${quizWord!!.tr.replaceFirstChar { it.uppercase() }}” kelimesinin İngilizcesi nedir?",
                        fontSize = 17.sp, fontWeight = FontWeight.SemiBold
                    )
                    quizChoices.forEach { choice ->
                        OutlinedButton(
                            onClick = {
                                if (quizMessage == null) {
                                    quizCount++
                                    sessionAnswered++
                                    if (choice.en == quizWord!!.en) {
                                        quizCorrect++
                                        sessionCorrect++
                                        correctStreak++
                                        bestCorrectStreak = maxOf(bestCorrectStreak, correctStreak)
                                        xp += 10
                                        coins += 1
                                        reviewWords.remove(quizWord!!.en)
                                        quizMessage = "Doğru! +10 XP ve +1 coin 🎉  |  🔥 $correctStreak seri"
                                    } else {
                                        correctStreak = 0
                                        if (quizWord!!.en !in reviewWords) reviewWords.add(quizWord!!.en)
                                        quizMessage = "Doğru cevap: ${if (quizMode == "İngilizce → Türkçe") quizWord!!.tr.replaceFirstChar { it.uppercase() } else quizWord!!.en.replaceFirstChar { it.uppercase() }}  |  Seri sıfırlandı."
                                    }
                                    prefs.edit().putStringSet("reviewWords", reviewWords.toSet()).putInt("xp", xp).putInt("coins", coins).putInt("correctStreak", correctStreak).putInt("bestCorrectStreak", bestCorrectStreak).apply()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(13.dp)
                        ) { Text(if (quizMode == "İngilizce → Türkçe") choice.tr.replaceFirstChar { it.uppercase() } else choice.en.replaceFirstChar { it.uppercase() }) }
                    }
                    quizMessage?.let { Text(it, fontWeight = FontWeight.Bold, color = if (it.startsWith("Doğru!")) Color(0xFF12805C) else Color(0xFFB45309)) }
                    Text("Bu test: $sessionCorrect / $sessionAnswered   •   Genel: $quizCorrect / $quizCount", color = Color.Gray, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (quizMessage == null) { quizWord = null }
                    else if (sessionActive && sessionAnswered >= 10) { quizWord = null; sessionActive = false; showQuizSummary = true }
                    else startQuiz()
                }) { Text(if (quizMessage == null) "Kapat" else if (sessionActive && sessionAnswered >= 10) "Sonucu gör" else "Sonraki soru") }
            },
            dismissButton = { TextButton(onClick = { quizWord = null; quizMessage = null }) { Text("Bitir") }
            }
        )
    }
    if (showQuizSummary) {
        AlertDialog(
            onDismissRequest = { showQuizSummary = false },
            title = { Text("Test tamamlandı! 🏆", fontWeight = FontWeight.ExtraBold) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("10 soruda $sessionCorrect doğru cevap verdin.", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Başarı oranı: ${sessionCorrect * 10}%", color = Blue, fontSize = 16.sp)
                Text("En iyi doğru serin: $bestCorrectStreak", color = Color(0xFF13734D))
                Text(if (sessionCorrect >= 8) "Harika iş! Böyle devam et! 🌟" else "Biraz tekrar yap, sonra yeniden dene 💪")
            } },
            confirmButton = { TextButton(onClick = { showQuizSummary = false; sessionActive = true; sessionAnswered = 0; sessionCorrect = 0; startQuiz() }) { Text("Tekrar dene") } },
            dismissButton = { TextButton(onClick = { showQuizSummary = false }) { Text("Kapat") } }
        )
    }
}

@Composable
private fun StreakCard(emoji: String, title: String, value: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier.animateContentSize(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = bg)) {
        Column(Modifier.padding(14.dp)) { Text(emoji, fontSize = 22.sp); Spacer(Modifier.height(4.dp)); Text(title, color = fg, fontWeight = FontWeight.Bold, fontSize = 12.sp); Text(value, color = fg, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold) }
    }
}

@Composable
private fun StatPill(emoji: String, label: String) {
    Surface(color = Color.White.copy(alpha = .14f), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(emoji, fontSize = 14.sp)
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
