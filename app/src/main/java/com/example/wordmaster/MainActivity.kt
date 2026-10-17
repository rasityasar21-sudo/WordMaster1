package com.example.wordmaster

import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
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

private val Navy = Color(0xFF202044)
private val Blue = Color(0xFF6957E8)
private val PaleBlue = Color(0xFFF0EDFF)
private val Mint = Color(0xFFE1F8F0)
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
            }.sortedBy { it.en.lowercase(Locale.ROOT) }
        } catch (_: Exception) { listOf(Word("hello", "merhaba", "A1")) }
        setContent { WordMasterApp(words, prefs) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordMasterApp(words: List<Word>, prefs: SharedPreferences) {
    var query by remember { mutableStateOf("") }
    var filterQuery by remember { mutableStateOf("") }
    LaunchedEffect(query) {
        delay(180)
        filterQuery = query.trim()
    }
    var currentScreen by remember { mutableStateOf("Dersler") }
    var selected by remember { mutableStateOf("Tümü") }
    var selectedTopic by remember { mutableStateOf("Tümü") }
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
    var showTools by remember { mutableStateOf(false) }
    var showPronunciation by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf(prefs.getString("displayName", "Kelime Kaşifi") ?: "Kelime Kaşifi") }
    var username by remember { mutableStateOf(prefs.getString("username", "@kelimekasifi") ?: "@kelimekasifi") }
    var nameDraft by remember { mutableStateOf(displayName) }
    var usernameDraft by remember { mutableStateOf(username) }
    var themeChoice by remember { mutableIntStateOf(prefs.getInt("themeChoice", 0)) }
    var compactCards by remember { mutableStateOf(prefs.getBoolean("compactCards", false)) }
    var dailyGoal by remember { mutableIntStateOf(prefs.getInt("dailyGoal", 10)) }
    var chosenLevel by remember { mutableStateOf(prefs.getString("chosenLevel", "") ?: "") }
    var showOnboarding by remember { mutableStateOf(chosenLevel.isBlank()) }
    var onboardingChoice by remember { mutableStateOf("A1") }
    var placementMode by remember { mutableStateOf(false) }
    var placementAnswered by remember { mutableIntStateOf(0) }
    var placementCorrect by remember { mutableIntStateOf(0) }
    var placementLevel by remember { mutableStateOf("A1") }
    var showMatchGame by remember { mutableStateOf(false) }
    var matchRound by remember { mutableIntStateOf(0) }
    var matchWords by remember { mutableStateOf<List<Word>>(emptyList()) }
    var matched by remember { mutableStateOf(setOf<String>()) }
    var selectedEnglish by remember { mutableStateOf<String?>(null) }
    var matchMessage by remember { mutableStateOf("İngilizce kelimeyi seç, sonra Türkçe karşılığını bul.") }
    var totalCorrect by remember { mutableIntStateOf(prefs.getInt("totalCorrect", 0)) }
    var totalAnswered by remember { mutableIntStateOf(prefs.getInt("totalAnswered", 0)) }
    var dailyRewardClaimed by remember { mutableStateOf(prefs.getString("dailyRewardDate", "") == LocalDate.now().toString()) }
    var quizMode by remember { mutableStateOf("İngilizce → Türkçe") }
    val reviewWords = remember { mutableStateListOf<String>().apply {
        addAll(prefs.getStringSet("reviewWords", emptySet()) ?: emptySet())
    } }
    val level = xp / 100 + 1
    val xpInLevel = xp % 100
    val today = LocalDate.now().toString() 
    val pageBackground = when (themeChoice) { 1 -> Color(0xFFEAF8F3); 2 -> Color(0xFFFFF1E5); else -> Color(0xFFF7F5FF) } 
    val heroBrush = when (themeChoice) { 1 -> Brush.linearGradient(listOf(Color(0xFF075E54), Color(0xFF16A085))); 2 -> Brush.linearGradient(listOf(Color(0xFF7C2D12), Color(0xFFF59E0B))); else -> Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF9333EA), Color(0xFFEC4899))) }
    LaunchedEffect(today) {
        if (lastLoginDate != today) {
            dailyStreak = if (lastLoginDate == LocalDate.now().minusDays(1).toString()) dailyStreak + 1 else 1
            lastLoginDate = today
            coins += 3
            prefs.edit().putInt("dailyStreak", dailyStreak).putString("lastLoginDate", today).putInt("coins", coins).apply()
        }
    }
    if (showPronunciation) PronunciationPracticeDialog(words = words, onDismiss = { showPronunciation = false })

    val learned = remember { mutableStateListOf<String>().apply { addAll(prefs.getStringSet("learned", emptySet()) ?: emptySet()) } }
    val favorites = remember { mutableStateListOf<String>().apply { addAll(prefs.getStringSet("favorites", emptySet()) ?: emptySet()) } }

    fun startQuiz() {
        if (words.size < 4) return
        val levels = listOf("A1", "A2", "B1", "B2", "C1")
        // Seviye tespitinde sorular, kullanıcının seçtiği başlangıç seviyesinden gelir.
        // Sıradan mini testte ise önce yanlış cevaplanan kelimeler tekrar edilir.
        val pool = if (placementMode) {
            val preferred = words.filter { it.level == onboardingChoice }
            val index = levels.indexOf(onboardingChoice).coerceAtLeast(0)
            preferred.ifEmpty {
                val nearestLevels = levels.sortedBy { kotlin.math.abs(levels.indexOf(it) - index) }
                nearestLevels.asSequence().map { level -> words.filter { it.level == level } }
                    .firstOrNull { it.isNotEmpty() } ?: words
            }
        } else {
            val reviewPool = words.filter { it.en in reviewWords }
            if (reviewPool.isNotEmpty()) reviewPool else words
        }
        val target = pool.random()
        val sameLevelChoices = words.filter { it.level == target.level && it.en != target.en }
        val otherChoices = words.filter { it.en != target.en && it !in sameLevelChoices }
        quizWord = target
        quizChoices = (listOf(target) + sameLevelChoices.shuffled().take(3).let { picked ->
            if (picked.size == 3) picked else picked + otherChoices.shuffled().take(3 - picked.size)
        }).shuffled()
        quizMessage = null
    }

    val favoriteSet = favorites.toSet()
    val learnedSet = learned.toSet()
    // 100.000 kelimelik sözlükte filtrelemeyi ana UI iş parçacığından çıkar.
    var filtered by remember { mutableStateOf<List<Word>>(emptyList()) }
    LaunchedEffect(words, selected, selectedTopic, filterQuery, tab, favoriteSet, learnedSet) {
        val search = filterQuery.lowercase(Locale.ROOT)
        filtered = withContext(Dispatchers.Default) {
            words.asSequence().filter { word ->
                (selected == "Tümü" || word.level == selected) &&
                (selectedTopic == "Tümü" || topicForWord(word) == selectedTopic) &&
                (search.isBlank() || word.en.contains(search, ignoreCase = true) || word.tr.contains(search, ignoreCase = true)) &&
                (tab != 1 || word.en in favoriteSet) && (tab != 2 || word.en in learnedSet)
            }.toList()
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF7C3AED), onPrimary = Color.White, background = pageBackground,
            surface = Color.White, onSurface = Ink, secondary = Color(0xFF13A879)
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(containerColor = Color.Black, contentColor = Color.White) {
                    NavigationBarItem(selected = currentScreen == "Dersler", onClick = { currentScreen = "Dersler"; selectedTopic = "Tümü"; selected = "A1" }, icon = { Text("📚") }, label = { Text("Dersler") })
                    NavigationBarItem(selected = currentScreen == "Sözlük", onClick = { currentScreen = "Sözlük"; selectedTopic = "Tümü"; selected = "Tümü"; tab = 0; query = "" }, icon = { Text("A-Z") }, label = { Text("Sözlük") })
                    NavigationBarItem(selected = false, onClick = { showProfile = true }, icon = { Text("👤") }, label = { Text("Profil") })
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF0ECFF), Color(0xFFFFFFFF), Color(0xFFFFF0F7)))).padding(padding),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (currentScreen == "Dersler") {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("📚 Dersler", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
                            Text("Bir seviye ve konu seç.", fontSize = 13.sp, color = Color(0xFF68758E))
                            Row(
                                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("A1", "A2", "B1", "B2", "C1").forEach { levelName ->
                                    val count = remember(words) { words.count { it.level == levelName } }
                                    FilterChip(
                                        selected = selected == levelName,
                                        onClick = { selected = levelName; selectedTopic = "Tümü"; tab = 0 },
                                        label = { Text(levelName, fontSize = 12.sp) }
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (selected == "Tümü") "Konular" else "$selected konuları", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
                                Spacer(Modifier.weight(1f))
                                Text("Birini seç", fontSize = 11.sp, color = Color(0xFF68758E))
                            }
                            val lessonTitles = when (selected) {
                                "A1" -> listOf("Greetings & Introducing", "Spelling words & Telling phone numbers", "Countries and nationalities", "Counting objects", "Expressing possession", "Family members", "Everyday objects", "Colors and numbers", "Food and drinks", "At home", "School life", "Days and months", "Telling the time", "Daily routines", "Likes and dislikes", "Describing people", "Clothes", "Weather", "Places in town", "Asking for directions", "Transport", "Can / can't", "There is / There are", "A1 review")
                                "A2" -> listOf("Daily routines", "Shopping", "Asking for directions", "Health and body", "Hobbies and free time", "Past events", "Plans and invitations", "Comparisons", "Travel", "Food choices", "At work", "Household tasks", "Making requests", "Giving advice", "Feelings", "Describing places", "Weather forecast", "Transport problems", "Life experiences", "Future plans", "A2 review")
                                "B1" -> listOf("Simple Past Tense", "Used to / Get used to / Be used to", "Past Continuous Tense", "Present Perfect", "Future forms", "Giving opinions", "Education and work", "Travel experiences", "Feelings and relationships", "Media and technology", "Solving problems", "Environment", "Goals and ambitions", "Conditionals", "Passive voice", "Reported speech", "Making decisions", "Culture", "Health and lifestyle", "Describing trends", "Phrasal verbs", "B1 review")
                                "B2" -> listOf("Debates and opinions", "Professional life", "Society and culture", "Science and technology", "News and media", "Complex feelings", "Academic vocabulary", "Idioms", "Advanced conditionals", "Cause and effect", "Formal writing", "Presentations", "Negotiation", "Global issues", "Research", "Collocations", "Nuanced adjectives", "Advanced phrasal verbs", "Workplace communication", "B2 review")
                                else -> listOf("Advanced expressions", "Academic English", "Abstract concepts", "Culture and society", "Professional communication", "Subtle meaning differences", "Advanced verbs", "Idioms and phrases", "Critical analysis", "Formal register", "Research vocabulary", "Persuasive language", "Literature", "Politics and society", "Advanced collocations", "C1 review")
                            }
                            val lessonIcons = listOf("🤝", "🔤", "🌍", "🔢", "🎒", "👨‍👩‍👧", "🏠", "🎨", "🍎", "🏡", "📚", "📅", "🕒", "🧭")
                            val completedLessons = remember { mutableStateListOf<String>().apply {
                                addAll(prefs.getStringSet("completedLessons", emptySet()) ?: emptySet())
                            } }
                            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                lessonTitles.forEachIndexed { lessonIndex, title ->
                                    val lessonKey = "${selected}_${lessonIndex}_${title}"
                                    val completed = lessonKey in completedLessons
                                    val isNext = lessonIndex == 0 || "${selected}_${lessonIndex - 1}_${lessonTitles.getOrNull(lessonIndex - 1).orEmpty()}" in completedLessons
                                    Row(Modifier.fillMaxWidth().heightIn(min = 108.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.width(76.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier.size(62.dp).background(
                                                    if (completed) Color(0xFFE8F8EC) else if (isNext) Color(0xFFFFF200) else Color(0xFFF0F0F0),
                                                    RoundedCornerShape(18.dp)
                                                ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(lessonIcons[lessonIndex % lessonIcons.size], fontSize = 28.sp)
                                            }
                                            if (lessonIndex < lessonTitles.lastIndex) {
                                                Box(Modifier.width(4.dp).height(44.dp).background(if (completed) Color(0xFF43A85B) else Color.LightGray))
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
                                            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Navy)
                                            Text(if (completed) "Tamamlandı ✓" else if (isNext) "Derse başla" else "Sıradaki ders", fontSize = 12.sp, color = if (completed) Color(0xFF32964B) else Color(0xFF68758E))
                                        }
                                        if (completed) {
                                            Text("✓", fontSize = 24.sp, color = Color(0xFF43A85B), fontWeight = FontWeight.Bold)
                                        } else if (isNext) {
                                            TextButton(onClick = {
                                                currentScreen = "Sözlük"
                                                selected = selected
                                                selectedTopic = "Tümü"
                                                tab = 0
                                                query = ""
                                            }) { Text("▶", color = Color.Black, fontSize = 20.sp) }
                                        } else {
                                            Text("🔒", fontSize = 19.sp)
                                        }
                                    }
                                }
                            }
                            if (selectedTopic != "Tümü") {
                                TextButton(onClick = { selectedTopic = "Tümü" }, modifier = Modifier.align(Alignment.End)) {
                                    Text("Tüm konuları göster")
                                }
                            }
                        }
                    }
                    }
                if (currentScreen == "Sözlük") {
                    item {
                        Text(if (selectedTopic == "Tümü") "Kelime defterim" else "$selectedTopic · Kelimeler", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
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
                            colors = CardDefaults.cardColors(containerColor = listOf(Color(0xFFFFFBF0), Color(0xFFF4F0FF), Color(0xFFEDFAF7), Color(0xFFFFF0F5), Color(0xFFEEF6FF))[filtered.indexOf(word).coerceAtLeast(0) % 5]),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(Modifier.padding(if (compactCards) 11.dp else 17.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(word.en.replaceFirstChar { it.uppercase() }, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
                                        Spacer(Modifier.height(3.dp))
                                        Text(word.tr.replaceFirstChar { it.uppercase() }, fontSize = 16.sp, color = Color(0xFF46536D))
                                    }
                                    Surface(color = listOf(Color(0xFFE9DEFF), Color(0xFFD9F7ED), Color(0xFFFFE6D5), Color(0xFFDDEEFF), Color(0xFFFFE0EB))[filtered.indexOf(word).coerceAtLeast(0) % 5], shape = RoundedCornerShape(12.dp)) {
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
    
                    }
                item {
                    Button(onClick = { showTools = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("📅 Günlük görevler  •  🏅 Rozetler  •  🎮 Eşleştirme  •  📊 İstatistikler")
                    }
                }

                item {
                    Text("İpucu: Kelimeleri favorilerine ekleyip sonra tekrar çalışabilirsin.", color = Color(0xFF758099), fontSize = 12.sp)
                }
            }
        }
    }

    if (showOnboarding) AlertDialog(
        onDismissRequest = { },
        title = { Text("WordMaster’a hoş geldin! ✨", fontWeight = FontWeight.ExtraBold) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("İngilizce seviyeni seç. Emin değilsen seviye tespit sınavını çöz; uygulama seviyeni tahmin etsin.")
            listOf("A1" to "Başlangıç", "A2" to "Temel", "B1" to "Orta", "B2" to "Orta-ileri", "C1" to "İleri").forEach { (code, desc) ->
                Surface(color = if (onboardingChoice == code) Color(0xFFEDE9FE) else Color(0xFFF8FAFC), shape = RoundedCornerShape(14.dp), onClick = { onboardingChoice = code }) {
                    Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = onboardingChoice == code, onClick = { onboardingChoice = code })
                        Column { Text(code, fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9)); Text(desc, fontSize = 12.sp, color = Color(0xFF64748B)) }
                    }
                }
            }
        } },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = { chosenLevel = onboardingChoice; selected = onboardingChoice; prefs.edit().putString("chosenLevel", chosenLevel).apply(); showOnboarding = false }) { Text("Seviyemle devam et") }
                Button(onClick = { placementMode = true; placementAnswered = 0; placementCorrect = 0; sessionAnswered = 0; sessionCorrect = 0; sessionActive = true; showOnboarding = false; selected = "Tümü"; quizMode = "İngilizce → Türkçe"; startQuiz() }) { Text("🧭 Seviye tespit sınavı") }
            }
        }
    )
    if (showProfile) AlertDialog(
        onDismissRequest = { showProfile = false },
        title = { Text("Profilim 👤", fontWeight = FontWeight.ExtraBold) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(28.dp), color = PaleBlue) {
                    Text("🧑‍🎓", modifier = Modifier.padding(18.dp), fontSize = 34.sp)
                }
            }
            Text("Seviye ${xp / 100 + 1}  •  ${xp} XP  •  ${dailyStreak} günlük seri", color = Blue, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it.take(30) }, label = { Text("Görünen ad") }, singleLine = true)
            OutlinedTextField(value = usernameDraft, onValueChange = { usernameDraft = it.filter { c -> c.isLetterOrDigit() || c == '_' }.take(20).let { if (it.isBlank()) "" else "@$it" } }, label = { Text("Kullanıcı adı") }, supportingText = { Text("Harf, rakam ve _ kullanabilirsin.") }, singleLine = true)
            Text("📚 Öğrenilen: ${learned.size} kelime   ⭐ Favori: ${favorites.size}", color = Ink)
            Text("🏆 En iyi seri: $bestCorrectStreak   🎯 Test doğrusu: $totalCorrect", color = Ink)
        } },
        confirmButton = { TextButton(onClick = {
            displayName = nameDraft.trim().ifBlank { "Kelime Kaşifi" }
            username = usernameDraft.trim().ifBlank { "@kelimekasifi" }
            prefs.edit().putString("displayName", displayName).putString("username", username).apply()
            showProfile = false
        }) { Text("Kaydet") } },
        dismissButton = { TextButton(onClick = { showProfile = false }) { Text("Vazgeç") } }
    )

    if (showSettings) AlertDialog(
        onDismissRequest = { showSettings = false },
        title = { Text("Görünüm ve ayarlar ⚙️", fontWeight = FontWeight.ExtraBold) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Uygulama teması", fontWeight = FontWeight.Bold)
            listOf("Mor-pembe gün batımı", "Zümrüt yeşili", "Sıcak gün batımı").forEachIndexed { index, label ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = themeChoice == index, onClick = { themeChoice = index })
                    Text(label)
                }
            }
            HorizontalDivider()
            Text("Günlük kelime hedefi", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15, 20).forEach { goal ->
                    FilterChip(selected = dailyGoal == goal, onClick = { dailyGoal = goal }, label = { Text("$goal") })
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Kompakt kelime kartları", fontWeight = FontWeight.SemiBold)
                    Text("Daha fazla kelimeyi ekranda göster", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(checked = compactCards, onCheckedChange = { compactCards = it })
            }
            Text("Seçimler bu cihazda saklanır.", fontSize = 12.sp, color = Color.Gray)
        } },
        confirmButton = { TextButton(onClick = {
            prefs.edit().putInt("themeChoice", themeChoice).putInt("dailyGoal", dailyGoal).putBoolean("compactCards", compactCards).apply()
            showSettings = false
        }) { Text("Uygula") } },
        dismissButton = { TextButton(onClick = { showSettings = false }) { Text("Kapat") } }
    )

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
                                    totalAnswered++
                                    if (placementMode) {
                                        placementAnswered++
                                        if (choice.en == quizWord!!.en) placementCorrect++
                                    }
                                    if (choice.en == quizWord!!.en) {
                                        quizCorrect++
                                        sessionCorrect++
                                        totalCorrect++
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
                                    prefs.edit().putInt("totalCorrect", totalCorrect).putInt("totalAnswered", totalAnswered).putStringSet("reviewWords", reviewWords.toSet()).putInt("xp", xp).putInt("coins", coins).putInt("correctStreak", correctStreak).putInt("bestCorrectStreak", bestCorrectStreak).apply()
                                    if (placementMode && placementAnswered >= 10) {
                                        val levelOrder = listOf("A1", "A2", "B1", "B2", "C1")
                                        val baseIndex = levelOrder.indexOf(onboardingChoice).coerceAtLeast(0)
                                        val assessedIndex = when {
                                            placementCorrect >= 8 -> (baseIndex + 1).coerceAtMost(levelOrder.lastIndex)
                                            placementCorrect >= 5 -> baseIndex
                                            else -> (baseIndex - 1).coerceAtLeast(0)
                                        }
                                        chosenLevel = levelOrder[assessedIndex]
                                        placementLevel = chosenLevel
                                        selected = chosenLevel
                                        prefs.edit().putString("chosenLevel", chosenLevel).apply()
                                        placementMode = false
                                        quizWord = null
                                        quizMessage = null
                                        sessionActive = false
                                        showQuizSummary = true
                                    }
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

    if (showTools) AlertDialog(
        onDismissRequest = { showTools = false },
        title = { Text("WordMaster Pro") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("📅 GÜNLÜK GÖREVLER", fontWeight = FontWeight.Bold)
            Text("• $dailyGoal kelime öğren: ${learned.size.coerceAtMost(dailyGoal)}/$dailyGoal")
            Text("• Testte 5 soruyu cevapla: ${quizCount.coerceAtMost(5)}/5")
            Text("• Günlük seri: $dailyStreak gün")
            Button(
                enabled = !dailyRewardClaimed && learned.size >= 10,
                onClick = {
                    xp += 20
                    coins += 10
                    dailyRewardClaimed = true
                    prefs.edit().putInt("xp", xp).putInt("coins", coins).putString("dailyRewardDate", LocalDate.now().toString()).apply()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (dailyRewardClaimed) "✅ Bugünkü ödül alındı" else "🎁 Günlük hedef ödülünü al (+20 XP, +10 coin)") }
            HorizontalDivider()
            Text("🏅 ROZETLER", fontWeight = FontWeight.Bold)
            Text((if (learned.isNotEmpty()) "✅" else "🔒") + " İlk kelime")
            Text((if (xp >= 100) "✅" else "🔒") + " 100 XP")
            Text((if (learned.size >= 50) "✅" else "🔒") + " 50 kelime")
            Text((if (bestCorrectStreak >= 5) "✅" else "🔒") + " 5 doğru üst üste")
            Text((if (dailyStreak >= 7) "✅" else "🔒") + " 7 günlük seri")
            HorizontalDivider()
            Text("🎮 KELİME EŞLEŞTİRME", fontWeight = FontWeight.Bold)
            Button(onClick = {
                matchWords = words.shuffled().take(4)
                matched = emptySet()
                selectedEnglish = null
                matchMessage = "İngilizce kelimeyi seç, sonra Türkçe karşılığını bul."
                showTools = false
                showMatchGame = true
            }, modifier = Modifier.fillMaxWidth()) { Text("Oyunu başlat") }
            HorizontalDivider()
            Text("📊 İSTATİSTİKLER", fontWeight = FontWeight.Bold)
            Text("Öğrenilen kelime: ${learned.size} / ${words.size}")
            Text("XP: $xp • Seviye: ${xp / 100 + 1} • Coin: $coins")
            Text("Doğru cevap: $totalCorrect / $totalAnswered")
            Text("Başarı oranı: ${if (totalAnswered == 0) 0 else totalCorrect * 100 / totalAnswered}%")
            Text("Doğru seri rekoru: $bestCorrectStreak • Tekrar kuyruğu: ${reviewWords.size}")
        } },
        confirmButton = { TextButton(onClick = { showTools = false }) { Text("Kapat") } }
    )

    if (showMatchGame) AlertDialog(
        onDismissRequest = { showMatchGame = false },
        title = { Text("Kelime eşleştirme 🎮") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Eşleşen: ${matched.size}/${matchWords.size}")
                LinearProgressIndicator(progress = { if (matchWords.isEmpty()) 0f else matched.size.toFloat() / matchWords.size }, modifier = Modifier.fillMaxWidth())
                Text(matchMessage, color = Blue, fontWeight = FontWeight.SemiBold)
                HorizontalDivider()
                Text("İngilizce", fontWeight = FontWeight.Bold)
                matchWords.filter { it.en !in matched }.forEach { word ->
                    OutlinedButton(
                        onClick = { selectedEnglish = word.en; matchMessage = "Şimdi ${word.en} kelimesinin Türkçesini seç." },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedEnglish == null || selectedEnglish == word.en
                    ) { Text(if (selectedEnglish == word.en) "👉 ${word.en}" else word.en) }
                }
                HorizontalDivider()
                Text("Türkçe", fontWeight = FontWeight.Bold)
                matchWords.filter { it.en !in matched }.shuffled().forEach { word ->
                    OutlinedButton(
                        onClick = {
                            val selected = selectedEnglish
                            if (selected == null) {
                                matchMessage = "Önce İngilizce kelime seç."
                            } else if (selected == word.en) {
                                matched = matched + word.en
                                selectedEnglish = null
                                xp += 5
                                prefs.edit().putInt("xp", xp).apply()
                                matchMessage = if (matched.size == matchWords.size) "Harika! Tüm eşleşmeler tamamlandı. +${matchWords.size * 5} XP 🎉" else "Doğru eşleşme! +5 XP"
                            } else {
                                selectedEnglish = null
                                matchMessage = "Bu eşleşme doğru değil. Tekrar dene!"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(word.tr) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                matchWords = words.shuffled().take(4)
                matched = emptySet()
                selectedEnglish = null
                matchMessage = "Yeni tur! İngilizce kelimeyi seç, sonra Türkçe karşılığını bul."
            }) { Text("Yeni tur") }
        },
        dismissButton = { TextButton(onClick = { showMatchGame = false }) { Text("Bitir") } }
    )

    if (showQuizSummary) {
        AlertDialog(
            onDismissRequest = { showQuizSummary = false },
            title = { Text("Test tamamlandı! 🏆", fontWeight = FontWeight.ExtraBold) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (placementAnswered >= 10) "Seviye tespit sonucu: $placementLevel" else "10 soruda $sessionCorrect doğru cevap verdin.", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(if (placementAnswered >= 10) "10 soruda $placementCorrect doğru cevap." else "Başarı oranı: ${sessionCorrect * 10}%", color = Blue, fontSize = 16.sp)
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
private fun PronunciationPracticeDialog(words: List<Word>, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var current by remember { mutableStateOf(words.random()) }
    var searchText by remember { mutableStateOf("") }
    var choices by remember { mutableStateOf((listOf(current) + words.filter { it.en != current.en }.shuffled().take(3)).shuffled()) }
    var message by remember { mutableStateOf("") }
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
    }
    DisposableEffect(tts) {
        onDispose { tts.stop(); tts.shutdown() }
    }
    LaunchedEffect(ttsReady) {
        if (ttsReady) tts.language = Locale.US
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🎧 Kelime Telaffuzu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("İstediğin İngilizce kelimeyi yazıp telaffuzunu dinleyebilirsin.")
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Kelime ara") },
                    placeholder = { Text("Örn. beautiful") },
                    singleLine = true
                )
                Button(
                    onClick = {
                        val term = searchText.trim()
                        if (term.isBlank()) {
                            message = "Önce bir kelime yaz."
                        } else if (ttsReady) {
                            tts.language = Locale.US
                            tts.speak(term, TextToSpeech.QUEUE_FLUSH, null, "wordmaster_search_word")
                            message = "Dinletiliyor: $term"
                        } else message = "Ses motoru hazırlanıyor. Birazdan tekrar dene."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("🔎 Ara ve telaffuzunu dinle") }
                HorizontalDivider()
                Text("Kelime pratiği", fontWeight = FontWeight.Bold)
                Text("Türkçe anlamı: ${current.tr}", color = Blue, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        if (ttsReady) {
                            tts.language = Locale.US
                            tts.speak(current.en, TextToSpeech.QUEUE_FLUSH, null, "wordmaster_word")
                        } else message = "Ses motoru hazırlanıyor. Birazdan tekrar dene."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("🔊 Seçili kelimeyi dinle") }
                choices.forEach { option ->
                    OutlinedButton(
                        onClick = {
                            message = if (option.en == current.en) "Doğru! ✓" else "Tekrar dene. İpucu: Türkçe anlamı düşün."
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(option.en.replaceFirstChar { it.uppercase() }) }
                }
                if (message.isNotBlank()) Text(message, color = if (message.startsWith("Doğru")) Color(0xFF12805C) else Color(0xFFB45309), fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                current = words.random()
                choices = (listOf(current) + words.filter { it.en != current.en }.shuffled().take(3)).shuffled()
                message = ""
            }) { Text("Sonraki kelime") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Bitir") } }
    )
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

private fun topicForWord(word: Word): String {
    val value = (word.en + " " + word.tr).lowercase()
    return when {
        listOf("hello", "goodbye", "please", "thank", "sorry", "merhaba", "hoşça", "teşekkür", "afedersin").any { it in value } -> "Selamlaşma"
        listOf("mother", "father", "sister", "brother", "family", "friend", "anne", "baba", "kardeş", "aile", "arkadaş").any { it in value } -> "Aile ve insanlar"
        listOf("water", "food", "bread", "apple", "milk", "eat", "drink", "su", "yemek", "ekmek", "elma", "süt", "içmek").any { it in value } -> "Yiyecek ve içecek"
        listOf("house", "room", "door", "window", "table", "chair", "bed", "ev", "oda", "kapı", "pencere", "masa", "sandalye", "yatak").any { it in value } -> "Ev ve eşyalar"
        listOf("school", "teacher", "student", "book", "learn", "okul", "öğretmen", "öğrenci", "kitap", "öğren").any { it in value } -> "Okul ve öğrenme"
        listOf("day", "night", "morning", "today", "tomorrow", "week", "time", "gün", "gece", "sabah", "bugün", "yarın", "hafta", "zaman").any { it in value } -> "Zaman ve günlük hayat"
        listOf("city", "country", "street", "bus", "train", "car", "travel", "şehir", "ülke", "sokak", "otobüs", "tren", "araba", "seyahat").any { it in value } -> "Yerler ve ulaşım"
        listOf("happy", "sad", "good", "bad", "big", "small", "beautiful", "angry", "mutlu", "üzgün", "iyi", "kötü", "büyük", "küçük", "güzel").any { it in value } -> "Duygular ve sıfatlar"
        listOf("go", "come", "make", "do", "have", "get", "take", "see", "run", "walk", "gitmek", "gelmek", "yapmak", "almak", "görmek", "koşmak", "yürümek").any { it in value } -> "Fiiller ve eylemler"
        else -> "Diğer kelimeler"
    }
}
