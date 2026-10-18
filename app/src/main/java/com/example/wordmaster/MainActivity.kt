package com.example.wordmaster

import android.content.SharedPreferences
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.media.MediaPlayer
import java.io.File
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

private const val CAFE_AUDIO_URL = "https://www.oxfordonlineenglish.com/wp-content/uploads/2021/09/Ordering-at-a-Coffee-Shop-full.mp3"
private const val CAFE_AUDIO_FILE = "wordmaster_cafe_dialogue.mp3"
private const val CAFE_AUDIO_DOWNLOAD_ID = "cafeAudioDownloadId"

/** Downloads the currently configured lesson audio once on first app launch.
 * Android DownloadManager continues the transfer in the background. Wi-Fi is required
 * by default to avoid unexpectedly using mobile data.
 */
private fun ensureLessonMediaDownloaded(context: Context, prefs: SharedPreferences) {
    val folder = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: return
    val file = File(folder, CAFE_AUDIO_FILE)
    if (file.exists() && file.length() > 0L) return

    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return
    val oldId = prefs.getLong(CAFE_AUDIO_DOWNLOAD_ID, -1L)
    if (oldId != -1L) {
        val query = DownloadManager.Query().setFilterById(oldId)
        val cursor = manager.query(query)
        val stillActive = cursor?.use {
            it.moveToFirst() && it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) in
                listOf(DownloadManager.STATUS_PENDING, DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PAUSED)
        } ?: false
        if (stillActive) return
    }

    try {
        val request = DownloadManager.Request(Uri.parse(CAFE_AUDIO_URL))
            .setTitle("WordMaster – Kafe diyaloğu")
            .setDescription("Dinleme dersi çevrimdışı kullanım için indiriliyor")
            .setMimeType("audio/mpeg")
            .setAllowedOverMetered(false)
            .setAllowedOverRoaming(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MUSIC, CAFE_AUDIO_FILE)
        val id = manager.enqueue(request)
        prefs.edit().putLong(CAFE_AUDIO_DOWNLOAD_ID, id).apply()
    } catch (_: Exception) {
        // The next app launch retries if the download could not be queued.
    }
}


private val Navy = Color(0xFF202044)
private val Blue = Color(0xFF6957E8)
private val PaleBlue = Color(0xFFF0EDFF)
private val Mint = Color(0xFFE1F8F0)
private val Ink = Color(0xFF172033)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("wordmaster", MODE_PRIVATE)
        ensureLessonMediaDownloaded(this, prefs)
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
    val appContext = LocalContext.current
    val learningStore = remember { LearningProgressStore(appContext) }
    var query by remember { mutableStateOf("") }
    var filterQuery by remember { mutableStateOf("") }
    LaunchedEffect(query) {
        delay(180)
        filterQuery = query.trim()
    }
    var currentScreen by remember { mutableStateOf("Dersler") }
    var activeLesson by remember { mutableStateOf("") }
    var activeLessonIndex by remember { mutableIntStateOf(0) }
    var lessonAnswer by remember { mutableStateOf(false) }
    var lessonStage by remember { mutableIntStateOf(0) }
    var lessonChoice by remember { mutableStateOf("") }
    var offlineVideoFrame by remember { mutableIntStateOf(0) }
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
    var placementCompleted by remember { mutableStateOf(prefs.getBoolean("placementCompleted", false)) }
    var savedPlacementCorrect by remember { mutableIntStateOf(prefs.getInt("placementCorrect", 0)) }
    var showOnboarding by remember { mutableStateOf(chosenLevel.isBlank()) }
    var onboardingChoice by remember { mutableStateOf(chosenLevel.ifBlank { "A1" }) }
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
            // Önce zamanı gelen aralıklı tekrar kelimeleri; sonra eski yanlış cevap kuyruğu.
            val dueSet = learningStore.dueForReview().map { it.word }.toSet()
            val duePool = words.filter { it.en in dueSet }
            val reviewPool = words.filter { it.en in reviewWords }
            when {
                duePool.isNotEmpty() -> duePool
                reviewPool.isNotEmpty() -> reviewPool
                else -> words
            }
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
                        val dueCount = learningStore.dueForReview().size
                        val todayProgress = (prefs.getInt("todayWords", 0)).coerceAtMost(dailyGoal)
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("WORDMASTER", color = Color(0xFF6D5AE6), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                                    Text("Merhaba, $displayName 👋", color = Color(0xFF17152B), fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                                    Text("Bugün küçük bir adım, yarın büyük ilerleme.", color = Color(0xFF77738B), fontSize = 13.sp)
                                }
                                Surface(shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 4.dp) {
                                    Column(Modifier.padding(horizontal = 13.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🔥 $dailyStreak", color = Color(0xFFE45D36), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                        Text("seri", color = Color(0xFF77738B), fontSize = 11.sp)
                                    }
                                }
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Column(
                                    Modifier.fillMaxWidth().background(heroBrush).padding(22.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                            Text("BUGÜNÜN ROTASI", color = Color.White.copy(alpha = 0.78f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                                            Text("İngilizceni bir adım ileri taşı.", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 28.sp)
                                        }
                                        Text("🚀", fontSize = 42.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text("$todayProgress / $dailyGoal kelime hedefi", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Spacer(Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                progress = { if (dailyGoal > 0) (todayProgress.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f },
                                                modifier = Modifier.fillMaxWidth().height(7.dp),
                                                color = Color.White,
                                                trackColor = Color.White.copy(alpha = 0.28f)
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = { placementMode = false; quizMode = "İngilizce → Türkçe"; startQuiz(); sessionAnswered = 0; sessionCorrect = 0; sessionActive = true; currentScreen = "Sınav" },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF4F46E5))
                                    ) {
                                        Text("Öğrenmeye başla  →", fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(vertical = 5.dp))
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Card(Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(Modifier.fillMaxWidth().clickable { showMatchGame = true; matchRound = 0; matched = emptySet(); selectedEnglish = null; matchWords = words.shuffled().take(5) }.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Text("🧩", fontSize = 25.sp)
                                        Text("Eşleştirme", color = Color(0xFF211D3A), fontWeight = FontWeight.Bold)
                                        Text("Hızlı oyun", color = Color(0xFF77738B), fontSize = 12.sp)
                                    }
                                }
                                Card(Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(Modifier.fillMaxWidth().clickable { showPronunciation = true }.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Text("🎙️", fontSize = 25.sp)
                                        Text("Konuşma", color = Color(0xFF211D3A), fontWeight = FontWeight.Bold)
                                        Text("Sesli pratik", color = Color(0xFF77738B), fontSize = 12.sp)
                                    }
                                }
                                Card(Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(Modifier.fillMaxWidth().clickable { showProfile = true }.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Text("📈", fontSize = 25.sp)
                                        Text("İlerleme", color = Color(0xFF211D3A), fontWeight = FontWeight.Bold)
                                        Text("$xp XP • Seviye $level", color = Color(0xFF77738B), fontSize = 11.sp)
                                    }
                                }
                            }
                            if (dueCount > 0) {
                                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7E8))) {
                                    Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("🧠", fontSize = 25.sp)
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text("$dueCount kelime tekrar bekliyor", fontWeight = FontWeight.Bold, color = Color(0xFF7C4A03))
                                            Text("Unutmadan önce bilgini tazele.", fontSize = 12.sp, color = Color(0xFF926C30))
                                        }
                                        TextButton(onClick = { placementMode = false; startQuiz(); sessionAnswered = 0; sessionCorrect = 0; sessionActive = true; currentScreen = "Sınav" }) { Text("Tekrar et") }
                                    }
                                }
                            }
                        }
                    }
                }
                if (currentScreen == "Ders İçeriği") {
                    item {
                        val lesson = lessonContent(activeLesson)
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { currentScreen = "Dersler"; lessonStage = 0 }) { Text("✕ Çık") }
                                Spacer(Modifier.weight(1f))
                                Text("Bölüm ${lessonStage + 1}/3", color = Blue, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(progress = { (lessonStage + 1) / 3f }, modifier = Modifier.fillMaxWidth(), color = Blue)
                            Text(activeLesson, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
                            when (lessonStage) {
                                0 -> {
                                    Text("KONU ANLATIMI", color = Blue, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                                            Text(lesson.heading, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                                            Text(lesson.explanation, fontSize = 16.sp, lineHeight = 25.sp, color = Ink)
                                            Surface(color = PaleBlue, shape = RoundedCornerShape(16.dp)) {
                                                Text(lesson.rule, Modifier.fillMaxWidth().padding(16.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy)
                                            }
                                            Text("Örnekler", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                            lesson.examples.forEach { example ->
                                                Column(Modifier.fillMaxWidth().background(Color(0xFFF7F8FC), RoundedCornerShape(14.dp)).padding(13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(example.first, fontWeight = FontWeight.Bold, color = Ink, fontSize = 16.sp)
                                                    Text(example.second, color = Color(0xFF68758E), fontSize = 14.sp)
                                                }
                                            }
                                            Text("Kelimeler", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                            lesson.words.forEach { pair ->
                                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(pair.first, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = Ink)
                                                    Text(pair.second, color = Color(0xFF68758E))
                                                }
                                                HorizontalDivider(color = Color(0xFFEDEEF4))
                                            }
                                        }
                                    }
                                    Button(onClick = { lessonStage = 1 }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text("Videoya geç  →", modifier = Modifier.padding(6.dp)) }
                                }
                                1 -> {
                                    Text("LISTENING • DİNLEME", color = Blue, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    Text("Kısa konuşmayı dinle, ardından soruları cevapla.", color = Color(0xFF68758E), lineHeight = 22.sp)
                                    val context = LocalContext.current
                                    var listeningReady by remember { mutableStateOf(false) }
                                    val listeningTts = remember {
                                        TextToSpeech(context) { status ->
                                            listeningReady = status == TextToSpeech.SUCCESS
                                        }
                                    }
                                    DisposableEffect(listeningTts) {
                                        onDispose { listeningTts.stop(); listeningTts.shutdown() }
                                    }
                                    LaunchedEffect(listeningReady) {
                                        if (listeningReady) listeningTts.language = Locale.US
                                    }
                                    val dialogue = lesson.examples.take(3).ifEmpty {
                                        listOf(lesson.heading to lesson.explanation)
                                    }
                                    Card(
                                        shape = RoundedCornerShape(22.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF19172D))
                                    ) {
                                        Column(
                                            Modifier.fillMaxWidth().padding(18.dp),
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Text("🎧", fontSize = 30.sp)
                                                Column {
                                                    Text("LISTENING PRACTICE", color = Color(0xFFBDB4FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text(lesson.heading, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                                                }
                                            }
                                            Text("Konuşmayı önce dinle. İstersen tekrar dinleyip sonra metni kontrol et.", color = Color(0xFFE5E2FF), lineHeight = 22.sp)
                                            Button(
                                                onClick = {
                                                    if (listeningReady) {
                                                        listeningTts.language = Locale.US
                                                        val script = dialogue.mapIndexed { i, pair ->
                                                            (if (i % 2 == 0) "Speaker one. " else "Speaker two. ") + pair.first
                                                        }.joinToString(". ")
                                                        listeningTts.speak(script, TextToSpeech.QUEUE_FLUSH, null, "lesson_listening_dialogue")
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(14.dp)
                                            ) { Text("▶ Konuşmayı dinle", modifier = Modifier.padding(4.dp)) }
                                            HorizontalDivider(color = Color(0xFF55516C))
                                            Text("Konuşma metni", color = Color(0xFFBDB4FF), fontWeight = FontWeight.Bold)
                                            dialogue.forEachIndexed { index, pair ->
                                                Row(
                                                    Modifier.fillMaxWidth().background(Color(0xFF292541), RoundedCornerShape(12.dp)).padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text(if (index % 2 == 0) "A" else "B", color = Color(0xFFFFE500), fontWeight = FontWeight.ExtraBold)
                                                        Text(pair.first, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                                        Text(pair.second, color = Color(0xFFD0CBEA), fontSize = 14.sp)
                                                    }
                                                    IconButton(onClick = {
                                                        if (listeningReady) {
                                                            listeningTts.language = Locale.US
                                                            listeningTts.speak(pair.first, TextToSpeech.QUEUE_FLUSH, null, "lesson_line_$index")
                                                        }
                                                    }) { Text("🔊", fontSize = 20.sp) }
                                                }
                                            }
                                            Text("Dinledikten sonra aşağıdaki alıştırmada doğru cevabı seç.", color = Color.White, fontWeight = FontWeight.SemiBold)
                                            Spacer(Modifier.height(8.dp))
                                            Text("Bu dinleme bölümünde yukarıdaki konuşmayı dinleyip cümleleri kontrol edebilirsin.", color = Color(0xFFE5E2FF), fontSize = 13.sp, lineHeight = 19.sp)
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedButton(onClick = { lessonStage = 0 }, modifier = Modifier.weight(1f)) { Text("← Konu anlatımı") }
                                        Button(onClick = { lessonStage = 2; lessonAnswer = false; lessonChoice = "" }, modifier = Modifier.weight(1f)) { Text("Teste geç →") }
                                    }
                                }
                                else -> {
                                    var practiceIndex by remember(activeLesson) { mutableIntStateOf(0) }
                                    var practiceCorrect by remember(activeLesson) { mutableIntStateOf(0) }
                                    var practiceChoice by remember(activeLesson) { mutableStateOf("") }
                                    var practiceFeedback by remember(activeLesson) { mutableStateOf("") }
                                    val practiceQuestions = remember(lesson) {
                                        val base = listOf(lesson.question to lesson.options to lesson.correct).map { Triple(it.first.first, it.first.second, it.second) }
                                        val examples = lesson.examples.map { pair ->
                                            Triple("“${pair.first}” cümlesinin Türkçe karşılığı nedir?", lesson.examples.map { it.second }.distinct().shuffled(), pair.second)
                                        }
                                        (base + examples + examples.reversed() + examples).take(10)
                                    }
                                    val practice = practiceQuestions[practiceIndex.coerceIn(0, practiceQuestions.lastIndex)]
                                    Text("ALIŞTIRMA • ${practiceIndex + 1}/${practiceQuestions.size}", color = Blue, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            Text("Soru ${practiceIndex + 1} / ${practiceQuestions.size}", fontSize = 13.sp, color = Color(0xFF68758E), fontWeight = FontWeight.Bold)
                                            LinearProgressIndicator(progress = { (practiceIndex + 1) / practiceQuestions.size.toFloat() }, modifier = Modifier.fillMaxWidth(), color = Blue)
                                            Text(if (practiceIndex == 0) "Boşluğu doldur" else "Anlamı veya doğru kullanımı seç", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                                            Text(practice.first, fontSize = 18.sp, lineHeight = 26.sp, color = Ink)
                                            practice.second.shuffled().forEach { option ->
                                                OutlinedButton(
                                                    onClick = {
                                                        practiceChoice = option
                                                        val correct = option == practice.third
                                                        practiceFeedback = if (correct) "Doğru cevap! ✓" else "Tekrar dene."
                                                        if (correct) practiceCorrect = (practiceCorrect + 1).coerceAtMost(practiceQuestions.size)
                                                        lessonChoice = if (correct) lesson.correct else option
                                                        lessonAnswer = correct
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = if (practiceChoice == option && option == practice.third) Mint else Color.White)
                                                ) { Text(option, modifier = Modifier.padding(4.dp)) }
                                            }
                                            if (practiceFeedback.isNotBlank()) Text(practiceFeedback, color = if (practiceChoice == practice.third) Color(0xFF16845D) else Color(0xFFB45309), fontWeight = FontWeight.Bold)
                                            Text("Doğru cevaplanan soru: $practiceCorrect / ${practiceQuestions.size}", color = Color(0xFF68758E), fontSize = 13.sp)
                                            HorizontalDivider()
                                            Text("Dinleme / konu kontrolü", fontWeight = FontWeight.Bold)
                                            Text(lesson.videoQuestion, fontSize = 16.sp, lineHeight = 24.sp)
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedButton(onClick = { lessonStage = 1 }, modifier = Modifier.weight(1f)) { Text("← Videoya dön") }
                                        Button(enabled = lessonAnswer, onClick = {
                                            val key = "${selected}_${activeLessonIndex}_${activeLesson}"
                                            val done = (prefs.getStringSet("completedLessons", emptySet()) ?: emptySet()) + key
                                            prefs.edit().putStringSet("completedLessons", done).apply()
                                            CourseProgressStore.addStudyMinutes(prefs, 10)
                                            currentScreen = "Dersler"
                                            lessonStage = 0
                                        }, modifier = Modifier.weight(1f)) { Text("Dersi tamamla ✓") }
                                    }
                                }
                            }
                        }
                    }
                }
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
                            val lessonIcons = listOf("✦", "Aa", "◎", "123", "▣", "⌂", "◇", "＋", "☼", "↗", "▤", "◷")
                            val completedLessons = remember { mutableStateListOf<String>().apply {
                                addAll(prefs.getStringSet("completedLessons", emptySet()) ?: emptySet())
                            } }
                            val lessonRows = lessonTitles.chunked(2)
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                lessonRows.forEachIndexed { rowIndex, rowTitles ->
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        rowTitles.forEachIndexed { columnIndex, title ->
                                            val lessonIndex = rowIndex * 2 + columnIndex
                                            val lessonKey = "${selected}_${lessonIndex}_${title}"
                                            val completed = lessonKey in completedLessons
                                            val unlocked = lessonIndex == 0 || "${selected}_${lessonIndex - 1}_${lessonTitles.getOrNull(lessonIndex - 1).orEmpty()}" in completedLessons
                                            Card(
                                                modifier = Modifier.weight(1f).heightIn(min = 142.dp),
                                                shape = RoundedCornerShape(22.dp),
                                                colors = CardDefaults.cardColors(containerColor = if (unlocked) Color.White else Color(0xFFF0F1F6)),
                                                elevation = CardDefaults.cardElevation(defaultElevation = if (unlocked) 3.dp else 0.dp),
                                                onClick = {
                                                    if (unlocked) {
                                                        activeLesson = title
                                                        activeLessonIndex = lessonIndex
                                                        lessonAnswer = false
                                                        currentScreen = "Ders İçeriği"
                                                    }
                                                }
                                            ) {
                                                Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            Modifier.size(38.dp).background(
                                                                if (completed) Color(0xFFDDF7EA) else if (unlocked) Color(0xFFE3F5F0) else Color(0xFFE2E4EC),
                                                                RoundedCornerShape(12.dp)
                                                            ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(lessonIcons[lessonIndex % lessonIcons.size], fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF175E57))
                                                        }
                                                        Spacer(Modifier.weight(1f))
                                                        Text(
                                                            if (completed) "✓" else if (unlocked) "→" else "•",
                                                            fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                                            color = if (completed) Color(0xFF16845D) else Color(0xFF667085)
                                                        )
                                                    }
                                                    Spacer(Modifier.height(12.dp))
                                                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink, lineHeight = 19.sp)
                                                    Spacer(Modifier.height(6.dp))
                                                    Text(
                                                        if (completed) "Tamamlandı" else if (unlocked) "Başlamak için dokun" else "Daha sonra açılır",
                                                        fontSize = 11.sp, color = Color(0xFF667085)
                                                    )
                                                }
                                            }
                                        }
                                        if (rowTitles.size == 1) Spacer(Modifier.weight(1f))
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
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.fillMaxWidth().height(112.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF9333EA), Color(0xFFEC4899))),
                            RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("📚", fontSize = 34.sp)
                        Text("WORDMASTER", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
                        Text("Her gün biraz daha İngilizce", color = Color.White, fontSize = 12.sp)
                    }
                }
                Text("Hoş geldin! ✨", fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, color = Navy)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Sana uygun dersleri hazırlayalım. Seviyeni seçebilir ya da kısa sınavla belirleyebilirsin.", fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF596579))
                if (placementCompleted) {
                    Surface(color = Color(0xFFE1F8F0), shape = RoundedCornerShape(14.dp)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("✓", color = Color(0xFF087F5B), fontSize = 22.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Seviye tespit sınavın kaydedildi", fontWeight = FontWeight.Bold, color = Color(0xFF087F5B))
                                Text("$chosenLevel seviyesi • $savedPlacementCorrect/10 doğru", fontSize = 12.sp, color = Color(0xFF087F5B))
                            }
                        }
                    }
                }
                listOf("A1" to "Başlangıç", "A2" to "Temel", "B1" to "Orta", "B2" to "Orta-ileri", "C1" to "İleri").forEach { (code, desc) ->
                    Surface(
                        color = if (onboardingChoice == code) Color(0xFFEDE9FE) else Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(14.dp),
                        onClick = { onboardingChoice = code }
                    ) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = onboardingChoice == code, onClick = { onboardingChoice = code })
                            Column {
                                Text(code, fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9))
                                Text(desc, fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                            Spacer(Modifier.weight(1f))
                            if (onboardingChoice == code) Text("✓", color = Color(0xFF6D28D9), fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Button(
                    onClick = {
                        chosenLevel = onboardingChoice
                        selected = onboardingChoice
                        prefs.edit().putString("chosenLevel", chosenLevel).apply()
                        showOnboarding = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                ) { Text("🚀  $onboardingChoice seviyesiyle başla", modifier = Modifier.padding(vertical = 5.dp)) }
                OutlinedButton(
                    onClick = {
                        placementMode = true
                        placementAnswered = 0
                        placementCorrect = 0
                        sessionAnswered = 0
                        sessionCorrect = 0
                        sessionActive = true
                        showOnboarding = false
                        selected = "Tümü"
                        quizMode = "İngilizce → Türkçe"
                        startQuiz()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                ) { Text("🧭  Seviye tespit sınavını çöz") }
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
            Text("📖 Tamamlanan ders: ${CourseProgressStore.completedCount(prefs)}", color = Ink)
            Text("⏱️ Günlük çalışma: ${CourseProgressStore.studyMinutes(prefs)} / ${CourseProgressStore.dailyGoalMinutes(prefs)} dakika", color = Ink)
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
                                    val answerWasCorrect = choice.en == quizWord!!.en
                                    // Her cevap gerçek aralıklı tekrar kaydına yazılır; doğru/yanlış aralığı günceller.
                                    learningStore.rateRecall(quizWord!!.en, if (answerWasCorrect) 4 else 1)
                                    learningStore.recordStudyDay()
                                    if (answerWasCorrect) {
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
                                        placementCompleted = true
                                        savedPlacementCorrect = placementCorrect
                                        prefs.edit()
                                            .putString("chosenLevel", chosenLevel)
                                            .putBoolean("placementCompleted", true)
                                            .putInt("placementCorrect", placementCorrect)
                                            .apply()
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
                    val dueCount = learningStore.dueForReview().size
                    Text("Akıllı tekrar: $dueCount kelime hazır • Cevapların tekrar tarihini günceller", color = Blue, fontSize = 12.sp)
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

private data class LessonContent(
    val heading: String, val explanation: String, val rule: String,
    val examples: List<Pair<String, String>>, val words: List<Pair<String, String>>,
    val question: String, val options: List<String>, val correct: String, val videoQuestion: String,
    val offlineFrames: List<Pair<String, String>> = listOf("Learn the rule" to "Kuralı öğren", "Read the example" to "Örneği oku", "Try it yourself" to "Kendin dene")
)

private fun lessonContent(title: String): LessonContent {
    val t = title.lowercase(Locale.ROOT)
    return when {
        "past continuous" in t -> LessonContent("Geçmişte devam eden eylemler", "Geçmişte belirli bir anda devam etmekte olan işleri anlatırız. Genellikle başka bir olay olduğunda devam eden eylem için kullanılır.", "Özne + was/were + fiil-ing", listOf("I was studying at 8 p.m." to "Saat 8'de ders çalışıyordum.", "They were playing when it started to rain." to "Yağmur başladığında oyun oynuyorlardı.", "She wasn't sleeping." to "Uyumuyordu."), listOf("while" to "-iken", "when" to "-dığı zaman", "study" to "ders çalışmak", "wait" to "beklemek"), "I ___ watching TV when you called.", listOf("was", "were", "am", "did"), "was", "Videoda was/were yardımcı fiillerinin hangi öznelerle kullanıldığını dinle.")
        "simple past" in t || "past events" in t -> LessonContent("Geçmişte tamamlanan olaylar", "Geçmişte belirli bir zamanda bitmiş olayları anlatır. Düzenli fiiller çoğunlukla -ed alır; düzensiz fiillerin ikinci hâli kullanılır.", "Özne + fiilin 2. hâli", listOf("I visited my aunt yesterday." to "Dün teyzemi ziyaret ettim.", "He went to school." to "Okula gitti.", "We didn't watch TV." to "Televizyon izlemedik."), listOf("yesterday" to "dün", "last week" to "geçen hafta", "visited" to "ziyaret etti", "went" to "gitti"), "She ___ to the museum yesterday.", listOf("went", "go", "goes", "going"), "went", "Videodaki örneklerde geçmiş zaman fiilinin ikinci hâline dikkat et.")
        "present perfect" in t || "life experiences" in t -> LessonContent("Deneyimler ve sonucu süren durumlar", "Present Perfect, zamanı kesin belirtilmeyen deneyimler veya geçmişte başlayıp etkisi süren durumlar için kullanılır.", "Özne + have/has + V3", listOf("I have visited London." to "Londra'yı ziyaret ettim.", "She has finished her homework." to "Ödevini bitirdi.", "Have you ever tried sushi?" to "Hiç suşi denedin mi?"), listOf("already" to "çoktan", "yet" to "henüz", "ever" to "hiç", "never" to "asla / hiç"), "He ___ finished his work.", listOf("has", "have", "was", "did"), "has", "Videoda have ve has kullanımını ve fiilin üçüncü hâlini belirle.")
        "passive voice" in t -> LessonContent("Edilgen yapı", "Eylemi kimin yaptığı değil, eylemden etkilenen şey önemliyse edilgen yapı kullanılır.", "Özne + be + V3", listOf("The bridge was built in 1990." to "Köprü 1990'da inşa edildi.", "English is spoken here." to "Burada İngilizce konuşulur.", "The meal was prepared by Ali." to "Yemek Ali tarafından hazırlandı."), listOf("built" to "inşa edildi", "made" to "yapıldı", "spoken" to "konuşulan", "by" to "tarafından"), "The letters ___ sent yesterday.", listOf("were", "was", "are", "did"), "were", "Videoda edilgen yapıda be fiili ile V3 arasındaki ilişkiye dikkat et.")
        "reported speech" in t -> LessonContent("Dolaylı anlatım", "Birinin sözlerini doğrudan alıntılamak yerine aktardığımızda zaman ve kişi zamirleri değişebilir.", "He said (that) + aktarılan cümle", listOf("“I am tired.” → He said he was tired." to "“Yorgunum.” → Yorgun olduğunu söyledi.", "“I can swim.” → She said she could swim." to "“Yüzebilirim.” → Yüzebildiğini söyledi."), listOf("said" to "söyledi", "told" to "anlattı / söyledi", "asked" to "sordu", "promised" to "söz verdi"), "He said he ___ tired.", listOf("was", "is", "were", "be"), "was", "Videoda doğrudan konuşmadan dolaylı anlatıma geçerken zaman değişimini dinle.")
        "greetings" in t || "introducing" in t -> LessonContent("Selamlaşma ve kendini tanıtma", "İlk kez karşılaştığımız kişilerle selamlaşır, adımızı söyler ve karşımızdakinin adını sorarız.", "Hello / My name is… / Nice to meet you.", listOf("Hello, my name is Ece." to "Merhaba, benim adım Ece.", "Nice to meet you." to "Tanıştığıma memnun oldum.", "What is your name?" to "Adın ne?"), listOf("hello" to "merhaba", "good morning" to "günaydın", "goodbye" to "hoşça kal", "name" to "isim"), "My name ___ Ece.", listOf("is", "are", "am", "be"), "is", "Videoda selamlaşma ve kendini tanıtma ifadelerini dinle.")
        "shopping" in t -> LessonContent("Alışverişte konuşma", "Fiyat sormak, ürün istemek ve fiyatları karşılaştırmak için bu ifadeleri kullanırız.", "How much is it? / I'd like…", listOf("How much is this shirt?" to "Bu gömlek ne kadar?", "I'd like a bottle of water." to "Bir şişe su istiyorum.", "It's too expensive." to "Çok pahalı."), listOf("price" to "fiyat", "cheap" to "ucuz", "expensive" to "pahalı", "receipt" to "fiş"), "How much ___ it?", listOf("is", "are", "do", "be"), "is", "Videoda fiyat sorma ve kibarca bir ürün isteme kalıplarını dinle.")
        "countries" in t || "nationalities" in t -> LessonContent("Ülkeler ve milliyetler", "Nereli olduğumuzu söylerken be fiilini ve ülke/milliyet adlarını kullanırız.", "Where are you from? — I'm from Türkiye.", listOf("Where are you from?" to "Nerelisin?", "I'm from Türkiye." to "Türkiye'denim.", "I'm Turkish." to "Türküm."), listOf("country" to "ülke", "nationality" to "milliyet", "Turkish" to "Türk", "from" to "-den / -dan"), "They ___ from Spain.", listOf("are", "is", "am", "be"), "are", "Videoda ülkeler ve milliyetlerden söz ederken kullanılan ifadeleri dinle.")
        "daily routines" in t -> LessonContent("Günlük rutinler", "Günlük yaptığımız işleri anlatırken geniş zaman kullanırız. He/she/it ile olumlu cümlede fiile genellikle -s eklenir.", "I wake up. / She wakes up.", listOf("I have breakfast at seven." to "Saat yedide kahvaltı yaparım.", "He goes to work by bus." to "İşe otobüsle gider.", "They go to bed early." to "Erken yatarlar."), listOf("wake up" to "uyanmak", "get dressed" to "giyinmek", "have breakfast" to "kahvaltı yapmak", "go to bed" to "yatmaya gitmek"), "She ___ up at seven.", listOf("wakes", "wake", "waking", "woke"), "wakes", "Videoda günlük rutinleri anlatan fiilleri ve he/she ile gelen -s ekini dinle.")

        "advanced expressions" in t -> LessonContent("İleri seviye ifadeler", "Daha doğal ve akıcı İngilizce için kalıp ifadeleri bağlama göre kullan. Bu ifadeler kelime kelime çevrilmek yerine bir bütün olarak öğrenilir.", "To be honest = açıkçası • It depends = duruma bağlı • As far as I know = bildiğim kadarıyla", listOf("To be honest, I disagree." to "Açıkçası, katılmıyorum.", "It depends on the situation." to "Duruma bağlı.", "As far as I know, the meeting is at six." to "Bildiğim kadarıyla toplantı altıda."), listOf("to be honest" to "açıkçası", "it depends" to "duruma bağlı", "as far as I know" to "bildiğim kadarıyla", "in other words" to "başka bir deyişle"), "___, I think we need more time.", listOf("To be honest", "By the way", "At least", "For example"), "To be honest", "İfadelerin anlamını tek tek kelimelerden değil, cümlenin bağlamından çıkar.")
        "counting objects" in t || "counting" in t -> LessonContent("Nesneleri sayma", "Sayıları isimlerle birlikte kullanırız. Sayılabilen tekil isimlerde a/an; çoğul isimlerde çoğunlukla -s kullanılır.", "one book • two books • three apples", listOf("I have two books." to "İki kitabım var.", "There are five chairs." to "Beş sandalye var.", "She has an orange." to "Bir portakalı var."), listOf("one" to "bir", "two" to "iki", "three" to "üç", "How many?" to "kaç tane?"), "There are three ___.", listOf("apples", "apple", "an apple", "a apples"), "apples", "Sayıdan sonra çoğul isim kullanımını ve how many kalıbını hatırla.")
        "family members" in t || "family" in t -> LessonContent("Aile üyeleri", "Aile bireylerini tanıtırken sahiplik bildiren my, your, his ve her kelimelerini kullanırız.", "my mother • his brother • their parents", listOf("This is my sister." to "Bu benim kız kardeşim.", "His father is a doctor." to "Onun babası doktor.", "Their parents live in Bursa." to "Onların anne babası Bursa'da yaşıyor."), listOf("mother" to "anne", "father" to "baba", "sister" to "kız kardeş", "brother" to "erkek kardeş"), "This is ___ brother.", listOf("my", "me", "I", "mine"), "my", "Aile bireyleriyle kullanılan sahiplik sıfatlarına dikkat et.")
        "expressing possession" in t -> LessonContent("Sahiplik bildirme", "Bir şeyin kime ait olduğunu have/has veya iyelik sıfatlarıyla anlatabiliriz.", "I have • she has • my/your/his/her", listOf("I have a blue bag." to "Mavi bir çantam var.", "She has two cats." to "İki kedisi var.", "This is their house." to "Bu onların evi."), listOf("my" to "benim", "your" to "senin", "his" to "onun (erkek)", "their" to "onların"), "He ___ a new phone.", listOf("has", "have", "having", "had"), "has", "He/she/it ile has kullanımını ve iyelik sıfatlarını ayırt et.")
        "food and drinks" in t || "food choices" in t -> LessonContent("Yiyecekler ve içecekler", "Yiyecek sipariş ederken some/any ve kibar istek kalıpları işe yarar.", "I'd like… • some water • any questions?", listOf("I'd like some tea, please." to "Bir çay istiyorum, lütfen.", "We need some bread." to "Biraz ekmeğe ihtiyacımız var.", "Do you have any apples?" to "Hiç elmanız var mı?"), listOf("bread" to "ekmek", "water" to "su", "vegetables" to "sebzeler", "I'd like" to "istiyorum"), "I'd like ___ water, please.", listOf("some", "many", "a", "an"), "some", "Yiyecek siparişi ve some/any kullanım örneklerini takip et.")
        "can / can't" in t -> LessonContent("Yetenekler: can / can't", "Can bir yeteneği veya mümkün olan bir şeyi anlatır. Ardından fiilin yalın hâli gelir.", "Özne + can/can't + fiilin yalın hâli", listOf("I can swim." to "Yüzebilirim.", "She can't drive." to "Araba kullanamaz.", "Can you help me?" to "Bana yardım edebilir misin?"), listOf("can" to "-ebilmek", "swim" to "yüzmek", "drive" to "araba kullanmak", "help" to "yardım etmek"), "He can ___ English.", listOf("speak", "speaks", "speaking", "spoke"), "speak", "Can sonrasında fiilin yalın hâlde geldiğine dikkat et.")
        "there is / there are" in t -> LessonContent("Bir şeyin var olduğunu söyleme", "Tekil isimlerle there is, çoğul isimlerle there are kullanılır.", "There is + tekil • There are + çoğul", listOf("There is a park near my home." to "Evimin yakınında bir park var.", "There are two windows." to "İki pencere var.", "Is there a bank nearby?" to "Yakınlarda banka var mı?"), listOf("there is" to "var (tekil)", "there are" to "var (çoğul)", "near" to "yakın", "between" to "arasında"), "There ___ three books on the table.", listOf("are", "is", "am", "be"), "are", "Tekil ve çoğul isimleri ayırt ederek there is/are seç.")
        "asking for directions" in t || "places in town" in t -> LessonContent("Şehirde yerler ve yön sorma", "Bir yere nasıl gidileceğini sorarken kibar soru kalıpları ve yön ifadeleri kullanılır.", "How can I get to…? • Turn left/right • Go straight", listOf("Excuse me, where is the station?" to "Affedersiniz, istasyon nerede?", "Go straight and turn left." to "Dümdüz git ve sola dön.", "It's next to the bank." to "Bankanın yanında."), listOf("turn left" to "sola dön", "turn right" to "sağa dön", "go straight" to "dümdüz git", "next to" to "yanında"), "Turn ___ at the traffic lights.", listOf("left", "breakfast", "expensive", "yesterday"), "left", "Yön tariflerinde kullanılan ifadeleri ve yer belirten kalıpları dinle.")
        "transport" in t || "travel" in t -> LessonContent("Ulaşım ve seyahat", "Ulaşım türlerini ve yolculukla ilgili temel ifadeleri öğren. By bus/train/car ulaşım aracını belirtir.", "go by bus • catch a train • book a ticket", listOf("I go to school by bus." to "Okula otobüsle giderim.", "We need to book a ticket." to "Bilet ayırtmamız gerekiyor.", "The train is late." to "Tren gecikti."), listOf("ticket" to "bilet", "platform" to "peron", "journey" to "yolculuk", "late" to "geç"), "We travel ___ train.", listOf("by", "on", "at", "with"), "by", "Ulaşım aracı belirtirken by bus/by train kalıplarına dikkat et.")
        else -> LessonContent(title, "Bu derste konuya ait temel ifadeleri ve örnek cümleleri öğreneceksin. Kuralı örneklerle incele, sonra videoyu izle ve alıştırmayı tamamla.", "Örnek cümleye bak; özne, fiil ve zaman yapısını belirle.", listOf("Read the example carefully." to "Örnek cümleyi dikkatlice oku.", "Listen and repeat." to "Dinle ve tekrar et.", "Try the exercise." to "Alıştırmayı dene."), listOf("example" to "örnek", "sentence" to "cümle", "meaning" to "anlam", "practice" to "alıştırma"), "She ___ English every day.", listOf("studies", "study", "studying", "studied"), "studies", "Videoda başlıktaki konunun örneklerini ve kullanılan dil yapısını takip et.")
    }
}

