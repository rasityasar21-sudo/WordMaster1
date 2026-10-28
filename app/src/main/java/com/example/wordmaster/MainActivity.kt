package com.example.wordmaster

import android.app.Activity
import android.content.SharedPreferences
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.animateContentSize
import java.time.LocalDate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray

data class Word(val en: String, val tr: String, val level: String, val example: String = "")

private val Navy = Color(0xFF111936)
private val Blue = Color(0xFF7C5CFF)
private val PaleBlue = Color(0xFFEAE5FF)
private val Mint = Color(0xFFD9F8F0)
private val Ink = Color(0xFF17213C)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("wordmaster", MODE_PRIVATE)
        // WordMaster's default is the selected professional navy theme; the saved choice
        // is applied immediately so the loading screen and system bars do not flash red/white.
        val initialThemeChoice = prefs.getInt("themeChoice", 1).coerceIn(0, 2)
        val initialDarkTheme = initialThemeChoice == 1 ||
            (initialThemeChoice == 2 && (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES)
        window.navigationBarColor = if (initialDarkTheme) android.graphics.Color.rgb(11, 16, 34) else android.graphics.Color.rgb(255, 255, 255)
        window.statusBarColor = if (initialDarkTheme) android.graphics.Color.rgb(11, 16, 34) else android.graphics.Color.rgb(247, 245, 255)
        var initialFlags = window.decorView.systemUiVisibility
        initialFlags = if (initialDarkTheme) {
            initialFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        } else {
            initialFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        window.decorView.systemUiVisibility = initialFlags
        val wordsState = mutableStateOf<List<Word>?>(null)

        setContent {
            val words by wordsState
            if (words == null) {
                DictionaryLoadingScreen(themeChoice = prefs.getInt("themeChoice", 1).coerceIn(0, 2))
            } else {
                WordMasterApp(words!!, prefs)
            }
        }

        // JSON okuma, ayrıştırma, tekrar ayıklama ve sıralama UI iş parçacığını bloke etmesin.
        lifecycleScope.launch(Dispatchers.IO) {
            val words = loadDictionary()
            withContext(Dispatchers.Main) {
                wordsState.value = words
            }
        }
    }

    private fun loadDictionary(): List<Word> = try {
        val json = assets.open("dictionary.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        (0 until arr.length()).mapNotNull { i ->
            val item = arr.getJSONObject(i)
            val english = item.optString("en").trim()
            val turkish = item.optString("tr").trim()
            val example = item.optString("example", "").trim()
            if (DictionaryEntryFilter.shouldExclude(english, turkish)) {
                null
            } else {
                Word(english, turkish, item.optString("level", "A1"), example)
            }
        }.distinctBy { it.en.lowercase(Locale.ROOT) }
            .sortedBy { it.en.lowercase(Locale.ROOT) }
    } catch (_: Exception) {
        listOf(Word("hello", "merhaba", "A1"))
    }
}

@Composable
private fun DictionaryLoadingScreen(themeChoice: Int) {
    val darkTheme = themeChoice == 1 ||
        (themeChoice == 2 && androidx.compose.foundation.isSystemInDarkTheme())
    val background = if (darkTheme) Color(0xFF0B1022) else Color(0xFFF7F5FF)
    val accent = if (darkTheme) Color(0xFF9B8CFF) else Color(0xFF6550D9)
    val foreground = if (darkTheme) Color(0xFFF3F1FF) else Color(0xFF202044)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = accent)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "WORDMASTER",
                color = accent,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sözlük hazırlanıyor...",
                color = foreground,
                fontSize = 15.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordMasterApp(words: List<Word>, prefs: SharedPreferences) {
    val appContext = LocalContext.current
    val scope = rememberCoroutineScope()
    var quickPracticeWords by remember { mutableStateOf<List<Word>>(emptyList()) }
    LaunchedEffect(words) {
        quickPracticeWords = withContext(Dispatchers.Default) {
            // A single English headword must map to one practice card; duplicate dictionary
            // entries can otherwise create repeated choices or make matching ambiguous.
            val filtered = words.filter { DictionaryEntryFilter.isQuickPracticeEntry(it.en, it.tr) }
            val fallback = words.filter { it.en.length <= 22 && it.tr.length <= 48 }
            (filtered.ifEmpty { fallback })
                .distinctBy { it.en.trim().lowercase(Locale.ROOT) }
        }
    }
    val learningStore = remember { LearningProgressStore(appContext) }
    var query by remember { mutableStateOf("") }
    var filterQuery by remember { mutableStateOf("") }
    LaunchedEffect(query) {
        delay(180)
        filterQuery = query.trim()
    }
    var currentScreen by remember { mutableStateOf("Dersler") }
    var lessonReturnScreen by remember { mutableStateOf("Dersler") }
    var activeLesson by remember { mutableStateOf("") }
    var activeCourseLessonId by remember { mutableStateOf<String?>(null) }
    var lessonSessionStartedAt by remember { mutableStateOf<Long?>(null) }
    var lessonSessionAccumulatedMillis by remember { mutableStateOf(0L) }
    LaunchedEffect(currentScreen) {
        lessonSessionAccumulatedMillis = 0L
        lessonSessionStartedAt = if (currentScreen == "Ders İçeriği") System.currentTimeMillis() else null
    }
    val lifecycleActivity = appContext as? ComponentActivity
    DisposableEffect(lifecycleActivity, currentScreen) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) {
                lessonSessionStartedAt?.let { startedAt ->
                    lessonSessionAccumulatedMillis += (System.currentTimeMillis() - startedAt).coerceAtLeast(0L)
                }
                lessonSessionStartedAt = null
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME &&
                currentScreen == "Ders İçeriği" && lessonSessionStartedAt == null
            ) {
                lessonSessionStartedAt = System.currentTimeMillis()
            }
        }
        lifecycleActivity?.lifecycle?.addObserver(observer)
        onDispose { lifecycleActivity?.lifecycle?.removeObserver(observer) }
    }
    var completedCourseIds by remember { mutableStateOf(CourseProgressStore.completedIds(prefs)) }
    var activeLessonIndex by remember { mutableIntStateOf(0) }
    var lessonAnswer by remember { mutableStateOf(false) }
    var lessonStage by remember { mutableIntStateOf(0) }
    var lessonChoice by remember { mutableStateOf("") }
    var offlineVideoFrame by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf("Tümü") }
    var selectedTopic by remember { mutableStateOf("Tümü") }
    var selectedCareerTrack by remember { mutableStateOf("Uçak Bakım") }
    var tab by remember { mutableIntStateOf(0) }
    var xp by remember { mutableIntStateOf(prefs.getInt("xp", 0)) }
    var coins by remember { mutableIntStateOf(prefs.getInt("coins", 0)) }
    var quizWord by remember { mutableStateOf<Word?>(null) }
    var quizLoading by remember { mutableStateOf(false) }
    var dueReviewCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(learningStore) {
        dueReviewCount = withContext(Dispatchers.IO) { learningStore.dueForReview().size }
    }
    var quizChoices by remember { mutableStateOf<List<Word>>(emptyList()) }
    var quizMessage by remember { mutableStateOf<String?>(null) }
    var quizCount by remember { mutableIntStateOf(0) }
    var quizCorrect by remember { mutableIntStateOf(0) }
    var correctStreak by remember { mutableIntStateOf(prefs.getInt("correctStreak", 0)) }
    var bestCorrectStreak by remember { mutableIntStateOf(prefs.getInt("bestCorrectStreak", 0)) }
    var dailyStreak by remember { mutableIntStateOf(prefs.getInt("dailyStreak", 0)) }
    var lastLoginDate by remember { mutableStateOf(prefs.getString("lastLoginDate", "") ?: "") }

    // A streak advances only when the user actually studies, not merely when the app opens.
    fun recordStudyDay() {
        val studyDate = LocalDate.now().toString()
        if (lastLoginDate == studyDate) return
        dailyStreak = if (lastLoginDate == LocalDate.now().minusDays(1).toString()) dailyStreak + 1 else 1
        lastLoginDate = studyDate
        prefs.edit().putInt("dailyStreak", dailyStreak).putString("lastLoginDate", studyDate).apply()
    }
    var sessionAnswered by remember { mutableIntStateOf(0) }
    var sessionCorrect by remember { mutableIntStateOf(0) }
    var sessionActive by remember { mutableStateOf(false) }
    var showQuizSummary by remember { mutableStateOf(false) }
    var showTools by remember { mutableStateOf(false) }
    var toolsDestination by remember { mutableStateOf("Günlük görevler") }
    var showPronunciation by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val userId = remember { prefs.getString("userId", null) ?: UUID.randomUUID().toString().replace("-", "").take(10).uppercase(Locale.ROOT).also { prefs.edit().putString("userId", it).apply() } }
    val defaultUsername = "kasif" + userId.lowercase(Locale.ROOT).takeLast(6)
    var displayName by remember { mutableStateOf(prefs.getString("displayName", "Kelime Kaşifi") ?: "Kelime Kaşifi") }
    var username by remember { mutableStateOf(prefs.getString("username", "@$defaultUsername") ?: "@$defaultUsername") }
    var accountEmail by remember { mutableStateOf(prefs.getString("accountEmail", "") ?: "") }
    var accountPhone by remember { mutableStateOf(prefs.getString("accountPhone", "") ?: "") }
    var profileError by remember { mutableStateOf<String?>(null) }
    var nameDraft by remember { mutableStateOf(displayName) }
    var usernameDraft by remember { mutableStateOf(username) }
    var emailDraft by remember { mutableStateOf(accountEmail) }
    var phoneDraft by remember { mutableStateOf(accountPhone) }
    var themeChoice by remember { mutableIntStateOf(prefs.getInt("themeChoice", 1).coerceIn(0, 2)) }
    var compactCards by remember { mutableStateOf(prefs.getBoolean("compactCards", false)) }
    var dailyGoal by remember { mutableIntStateOf(prefs.getInt("dailyGoal", 10)) }
    var dailyStudyGoal by remember { mutableIntStateOf(CourseProgressStore.dailyGoalMinutes(prefs)) }
    var chosenLevel by remember { mutableStateOf(prefs.getString("chosenLevel", "") ?: "") }
    var placementCompleted by remember { mutableStateOf(prefs.getBoolean("placementCompleted", false)) }
    var savedPlacementCorrect by remember { mutableIntStateOf(prefs.getInt("placementCorrect", 0)) }
    var showOnboarding by remember { mutableStateOf(chosenLevel.isBlank()) }
    var onboardingChoice by remember { mutableStateOf(chosenLevel.ifBlank { "A1" }) }
    var learningPurpose by remember { mutableStateOf(prefs.getString("learningPurpose", "Genel İngilizce") ?: "Genel İngilizce") }
    var placementMode by remember { mutableStateOf(false) }
    var placementAnswered by remember { mutableIntStateOf(0) }
    var placementCorrect by remember { mutableIntStateOf(0) }
    var placementLevel by remember { mutableStateOf("A1") }
    var showMatchGame by remember { mutableStateOf(false) }
    var matchRound by remember { mutableIntStateOf(0) }
    var matchPairCount by remember { mutableIntStateOf(prefs.getInt("matchPairCount", 6).coerceIn(4, 8)) }
    var matchWords by remember { mutableStateOf<List<Word>>(emptyList()) }
    var matchTurkishOrder by remember { mutableStateOf<List<Word>>(emptyList()) }
    var matched by remember { mutableStateOf(setOf<String>()) }
    // MutableStateList updates immediately, preventing duplicate XP from rapid taps
    // before the screen has recomposed with the latest matched set.
    val matchRewarded = remember { mutableStateListOf<String>() }
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
    // Index the large dictionary once so each quiz question does not rescan every entry.
    val wordsByLevel = remember(words) { words.groupBy { it.level } }
    val wordsByEnglish = remember(words) { words.groupBy { it.en } }
    val studyPlanLevel = chosenLevel.ifBlank { selected.takeIf { it in listOf("A1", "A2", "B1", "B2", "C1") } ?: "A1" }
    var reviewRefresh by remember { mutableIntStateOf(0) }
    var showReviewQueue by remember { mutableStateOf(false) }
    var dueReviewWords by remember { mutableStateOf<List<WordProgress>>(emptyList()) }
    LaunchedEffect(learningStore, reviewRefresh) {
        dueReviewWords = withContext(Dispatchers.IO) { learningStore.dueForReview() }
    }
    val studyPlan = remember(studyPlanLevel, completedCourseIds, dueReviewWords, today, dailyStudyGoal) {
        DailyStudyPlan.create(
            level = studyPlanLevel,
            completedLessonIds = completedCourseIds,
            dueWords = dueReviewWords,
            targetMinutes = dailyStudyGoal,
            date = today
        )
    }
    val darkTheme = themeChoice == 1 || (themeChoice == 2 && androidx.compose.foundation.isSystemInDarkTheme())
    // Keep Android system bars synchronized with the user's theme choice.
    SideEffect {
        val activity = appContext as? Activity
        activity?.window?.let { window ->
            window.statusBarColor = if (darkTheme) android.graphics.Color.rgb(11, 16, 34) else android.graphics.Color.rgb(247, 245, 255)
            window.navigationBarColor = if (darkTheme) android.graphics.Color.rgb(11, 16, 34) else android.graphics.Color.rgb(255, 255, 255)
            var flags = window.decorView.systemUiVisibility
            flags = if (darkTheme) {
                flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            } else {
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            window.decorView.systemUiVisibility = flags
        }
    }
    val pageBackground = if (darkTheme) Color(0xFF0B1022) else Color(0xFFF7F5FF)
    val heroBrush = Brush.linearGradient(listOf(Color(0xFF222D62), Color(0xFF542C9C), Color(0xFF111936)))
    if (showPronunciation) PronunciationPracticeDialog(words = quickPracticeWords, onDismiss = { showPronunciation = false })

    val learned = remember { mutableStateListOf<String>().apply { addAll(prefs.getStringSet("learned", emptySet()) ?: emptySet()) } }
    // A word grants its learning XP only once, even if the user toggles its learned state.
    val learnedRewarded = remember { mutableStateListOf<String>().apply {
        addAll(prefs.getStringSet("learnedRewarded", emptySet()) ?: emptySet())
        addAll(prefs.getStringSet("learned", emptySet()) ?: emptySet())
    } }
    val favorites = remember { mutableStateListOf<String>().apply { addAll(prefs.getStringSet("favorites", emptySet()) ?: emptySet()) } }

    fun startQuiz() {
        if (words.size < 4 || quizLoading) return
        val isPlacement = placementMode
        val selectedLevel = onboardingChoice
        val reviewSet = reviewWords.toSet()
        quizWord = null
        quizLoading = true
        quizMessage = null
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                // Progress-store reads and large vocabulary filtering stay off the UI thread.
                val dueSet = if (isPlacement) emptySet() else learningStore.dueForReview().map { it.word }.toSet()
                val levels = listOf("A1", "A2", "B1", "B2", "C1")
                val pool = if (isPlacement) {
                    val preferred = wordsByLevel[selectedLevel].orEmpty()
                    val index = levels.indexOf(selectedLevel).coerceAtLeast(0)
                    preferred.ifEmpty {
                        val nearestLevels = levels.sortedBy { kotlin.math.abs(levels.indexOf(it) - index) }
                        nearestLevels.asSequence().map { level -> wordsByLevel[level].orEmpty() }
                            .firstOrNull { it.isNotEmpty() } ?: words
                    }
                } else {
                    val duePool = dueSet.flatMap { wordsByEnglish[it].orEmpty() }
                    val reviewPool = reviewSet.flatMap { wordsByEnglish[it].orEmpty() }
                    when {
                        duePool.isNotEmpty() -> duePool
                        reviewPool.isNotEmpty() -> reviewPool
                        else -> words
                    }
                }
                val target = pool.randomOrNull() ?: words.randomOrNull()
                target?.let { it to QuizChoiceSelector.select(it, words, count = 4) }
            }
            quizLoading = false
            if (result != null) {
                quizWord = result.first
                quizChoices = result.second
            } else {
                quizMessage = "Test için yeterli kelime bulunamadı."
            }
        }
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

    val hasDismissibleOverlay = quizLoading || showProfile || showSettings || showTools || showPronunciation ||
        showMatchGame || showQuizSummary || quizWord != null

    BackHandler(enabled = hasDismissibleOverlay || currentScreen != "Dersler") {
        when {
            showProfile -> { showProfile = false; profileError = null }
            showSettings -> showSettings = false
            showPronunciation -> showPronunciation = false
            showMatchGame -> showMatchGame = false
            showTools -> showTools = false
            quizWord != null -> { quizWord = null; quizMessage = null; sessionActive = false; placementMode = false }
            showQuizSummary -> showQuizSummary = false
            currentScreen != "Dersler" -> {
                val destination = if (currentScreen == "Ders İçeriği") lessonReturnScreen else "Dersler"
                currentScreen = destination
                if (destination == "Dersler") {
                    selectedTopic = "Tümü"
                    selected = "A1"
                }
                if (destination != "Ders İçeriği") {
                    activeCourseLessonId = null
                    lessonStage = 0
                }
            }
        }
    }

    SideEffect {
        val activity = appContext as? ComponentActivity
        activity?.window?.navigationBarColor = if (darkTheme) android.graphics.Color.rgb(11, 16, 34) else android.graphics.Color.WHITE
        activity?.window?.statusBarColor = if (darkTheme) android.graphics.Color.rgb(11, 16, 34) else android.graphics.Color.rgb(247, 245, 255)
        @Suppress("DEPRECATION")
        activity?.window?.decorView?.systemUiVisibility = if (darkTheme) 0 else
            (View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
    }
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme(
            primary = Color(0xFFB8A7FF),
            onPrimary = Color(0xFF20134B),
            secondary = Color(0xFF55D6BE),
            onSecondary = Color(0xFF082C28),
            background = Color(0xFF0B1022),
            onBackground = Color(0xFFF3F4FF),
            surface = Color(0xFF151D38),
            onSurface = Color(0xFFF3F4FF),
            surfaceVariant = Color(0xFF202945),
            onSurfaceVariant = Color(0xFFD0D7EA),
            outline = Color(0xFF8995B5),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005)
        ) else lightColorScheme(
            primary = Color(0xFF5B43CC),
            onPrimary = Color.White,
            secondary = Color(0xFF087F68),
            onSecondary = Color.White,
            background = Color(0xFFF7F5FF),
            onBackground = Color(0xFF17213C),
            surface = Color.White,
            onSurface = Color(0xFF17213C),
            surfaceVariant = Color(0xFFEAE5F8),
            onSurfaceVariant = Color(0xFF49445F),
            outline = Color(0xFF77718B),
            error = Color(0xFFBA1A1A),
            onError = Color.White
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(containerColor = if (darkTheme) Color(0xFF10172D) else Color.White, contentColor = if (darkTheme) Color(0xFFE8E9FF) else Color(0xFF17213C)) {
                    NavigationBarItem(selected = currentScreen == "Dersler", onClick = { currentScreen = "Dersler"; selectedTopic = "Tümü"; selected = "A1" }, icon = { Text("📚") }, label = { Text("Dersler") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = if (darkTheme) Color(0xFFB8A7FF) else Color(0xFF6D4AFF), selectedTextColor = if (darkTheme) Color(0xFFE5DEFF) else Color(0xFF6D4AFF), indicatorColor = if (darkTheme) Color(0xFF30275A) else Color(0xFFEAE5FF), unselectedIconColor = if (darkTheme) Color(0xFF929BB8) else Color(0xFF77738B), unselectedTextColor = if (darkTheme) Color(0xFF929BB8) else Color(0xFF77738B)))
                    NavigationBarItem(selected = currentScreen == "Sözlük", onClick = { currentScreen = "Sözlük"; selectedTopic = "Tümü"; selected = "Tümü"; tab = 0; query = "" }, icon = { Text("A-Z") }, label = { Text("Sözlük") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFB8A7FF), selectedTextColor = Color(0xFFE5DEFF), indicatorColor = Color(0xFF30275A), unselectedIconColor = Color(0xFF929BB8), unselectedTextColor = Color(0xFF929BB8)))
                    NavigationBarItem(selected = currentScreen == "Mesleki İngilizce", onClick = { currentScreen = "Mesleki İngilizce" }, icon = { Text("✈️") }, label = { Text("Mesleki") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFB8A7FF), selectedTextColor = Color(0xFFE5DEFF), indicatorColor = Color(0xFF30275A), unselectedIconColor = Color(0xFF929BB8), unselectedTextColor = Color(0xFF929BB8)))
                    NavigationBarItem(selected = false, onClick = { showProfile = true }, icon = { Text("👤") }, label = { Text("Profil") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFB8A7FF), selectedTextColor = Color(0xFFE5DEFF), indicatorColor = Color(0xFF30275A), unselectedIconColor = Color(0xFF929BB8), unselectedTextColor = Color(0xFF929BB8)))
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(if (darkTheme) listOf(Color(0xFF0B1022), Color(0xFF111936), Color(0xFF17132F)) else listOf(Color(0xFFF1EDFF), Color.White, Color(0xFFFFF2F8)))).padding(padding),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (currentScreen == "Dersler") {
                    item {
                        val dueCount = dueReviewWords.size
                        val todayProgress = (if (prefs.getString("todayWordsDate", "") == today) prefs.getInt("todayWords", 0) else 0).coerceAtMost(dailyGoal)
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("WORDMASTER", color = Color(0xFFB8A7FF), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                                    Text("Merhaba, $displayName 👋", color = Color(0xFFF5F3FF), fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                                    Text("Bugün küçük bir adım, yarın büyük ilerleme.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 13.sp)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.clip(RoundedCornerShape(15.dp)).clickable {
                                            themeChoice = if (darkTheme) 0 else 1
                                            prefs.edit().putInt("themeChoice", themeChoice).apply()
                                        },
                                        shape = RoundedCornerShape(15.dp),
                                        color = if (darkTheme) Color(0xFF30275A) else Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (darkTheme) Color(0xFF55458D) else Color(0xFFE5E0F4))
                                    ) {
                                        Row(
                                            Modifier.padding(horizontal = 11.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text(if (darkTheme) "☀️" else "🌙", fontSize = 16.sp)
                                            Text(if (darkTheme) "Açık" else "Koyu", color = if (darkTheme) Color(0xFFF3F0FF) else Color(0xFF30275A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(15.dp),
                                        color = if (darkTheme) Color(0xFF202A49) else Color.White,
                                        shadowElevation = 2.dp,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (darkTheme) Color(0xFF303B5D) else Color(0xFFE5E0F4))
                                    ) {
                                        Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🔥 $dailyStreak", color = Color(0xFFE45D36), fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                                            Text("seri", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 11.sp)
                                        }
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
                                            Text("$todayProgress / $dailyGoal günlük soru hedefi", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = Color(0xFFB91C1C))
                                    ) {
                                        Text("Öğrenmeye başla  →", fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(vertical = 5.dp))
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Card(Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Column(Modifier.fillMaxWidth().clickable { showMatchGame = true; matchRound = 0; matched = emptySet(); matchRewarded.clear(); selectedEnglish = null; matchWords = quickPracticeWords.shuffled().take(6); matchTurkishOrder = matchWords.shuffled(); matchMessage = "İngilizce kelimeyi seç, sonra Türkçe karşılığını bul." }.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Text("🧩", fontSize = 25.sp)
                                        Text("Eşleştirme", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                        Text("Hızlı oyun", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 12.sp)
                                    }
                                }
                                Card(Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Column(Modifier.fillMaxWidth().clickable { showPronunciation = true }.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Text("🎙️", fontSize = 25.sp)
                                        Text("Konuşma", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                        Text("Sesli pratik", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 12.sp)
                                    }
                                }
                                Card(Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Column(Modifier.fillMaxWidth().clickable { showProfile = true }.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Text("📈", fontSize = 25.sp)
                                        Text("İlerleme", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                        Text("$xp XP • Seviye $level", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 11.sp)
                                    }
                                }
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { currentScreen = "Mesleki İngilizce" },
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF211D3A))
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF211D3A), Color(0xFF40356F)))).padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("✈️", fontSize = 30.sp)
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("MESLEKİ İNGİLİZCE", color = Color(0xFFD8D0FF), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
                                        Text("Kendi alanında uzmanlaş", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                        Text("Havacılık, yazılım, sağlık ve daha fazlası", color = Color(0xFFE2DDF5), fontSize = 12.sp, maxLines = 2)
                                    }
                                    Text("→", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                val progressSummary = learningStore.summary()
                                val completedLessonCount = completedCourseIds.size
                                val totalStudyMinutes = CourseProgressStore.studyMinutes(prefs, today)
                                val studyMinuteGoal = dailyStudyGoal
                                val studyProgress = (totalStudyMinutes.toFloat() / studyMinuteGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
                                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text("ÖĞRENME ÖZETİ", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
                                            Text("İlerlemen tek yerde", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                        Text("📊", fontSize = 28.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Column(Modifier.weight(1f).background(if (darkTheme) Color(0xFF202A49) else PaleBlue, RoundedCornerShape(14.dp)).padding(11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("📚", fontSize = 18.sp)
                                            Text("${completedLessonCount}", color = MaterialTheme.colorScheme.onSurface, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                                            Text("Tamamlanan ders", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 11.sp)
                                        }
                                        Column(Modifier.weight(1f).background(if (darkTheme) Color(0xFF183B42) else Mint, RoundedCornerShape(14.dp)).padding(11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("🧠", fontSize = 18.sp)
                                            Text("${progressSummary.totalWords}", color = MaterialTheme.colorScheme.onSurface, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                                            Text("Takip edilen kelime", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 11.sp)
                                        }
                                        Column(Modifier.weight(1f).background(if (darkTheme) Color(0xFF3B2C35) else Color(0xFFFFF1E5), RoundedCornerShape(14.dp)).padding(11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("🎯", fontSize = 18.sp)
                                            Text("${progressSummary.accuracyPercent}%", color = MaterialTheme.colorScheme.onSurface, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                                            Text("Doğruluk", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 11.sp)
                                        }
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Bugünkü çalışma süresi", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                            Text("${totalStudyMinutes} / ${studyMinuteGoal} dk", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        LinearProgressIndicator(
                                            progress = { studyProgress },
                                            modifier = Modifier.fillMaxWidth().height(6.dp),
                                            color = if (totalStudyMinutes >= studyMinuteGoal) Color(0xFF13A879) else Blue,
                                            trackColor = if (darkTheme) Color(0xFF303B5D) else Color(0xFFEDEAF7)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Tekrar bekleyen: ${progressSummary.dueWords} kelime", color = if (darkTheme) Color(0xFFFFD18A) else Color(0xFF7C4A03), fontSize = 12.sp, modifier = Modifier.weight(1f))
                                        Text("Seri: ${progressSummary.currentStreak} gün", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text("İstatistikler bu cihazda kaydedilen ders ve kelime çalışmalarından hesaplanır.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                }
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text("GÜNLÜK GÖREVLER", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
                                            Text("Bugünün küçük kazanımları", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                        Text("🎯", fontSize = 28.sp)
                                    }
                                    val taskOne = todayProgress >= dailyGoal
                                    val taskTwo = totalCorrect >= 5
                                    val taskThree = dailyStreak >= 3
                                    listOf(
                                        Triple("Cevapla $dailyGoal soru", todayProgress, dailyGoal.coerceAtLeast(1)),
                                        Triple("5 doğru cevap biriktir", totalCorrect.coerceAtMost(5), 5),
                                        Triple("3 günlük serini koru", dailyStreak.coerceAtMost(3), 3)
                                    ).forEachIndexed { index, task ->
                                        val completed = when (index) { 0 -> taskOne; 1 -> taskTwo; else -> taskThree }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(if (completed) "✅" else "◯", fontSize = 18.sp)
                                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                                Text(task.first, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                LinearProgressIndicator(
                                                    progress = { (task.second.toFloat() / task.third).coerceIn(0f, 1f) },
                                                    modifier = Modifier.fillMaxWidth().height(5.dp),
                                                    color = if (completed) Color(0xFF13A879) else Color(0xFF7C3AED),
                                                    trackColor = if (darkTheme) Color(0xFF303B5D) else Color(0xFFEDEAF7)
                                                )
                                            }
                                            Text(if (completed) "Tamam" else "${task.second}/${task.third}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 11.sp)
                                        }
                                    }
                                    Text("Not: Görevler bu cihazdaki kayıtlarına göre hesaplanır.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                }
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text("KİŞİSEL ÇALIŞMA PLANI", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
                                            Text("${studyPlan.targetMinutes} dakikalık rota", color = MaterialTheme.colorScheme.onSurface, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                        Text("🗺️", fontSize = 28.sp)
                                    }
                                    Text("Seviye: $studyPlanLevel · Tahmini süre: ${studyPlan.estimatedMinutes} dk", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
                                    if (studyPlan.lessonIds.isEmpty() && studyPlan.reviewWords.isEmpty()) {
                                        Text("Şu anda bu seviyede açılabilir yeni ders yok. Ön koşulları tamamla veya zamanı gelen kelimelerini tekrar et.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp, lineHeight = 19.sp)
                                    } else {
                                        studyPlan.lessonIds.take(3).forEachIndexed { index, lessonId ->
                                            val plannedLesson = WordMasterCourseCatalog.find(lessonId)
                                            if (plannedLesson != null) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    Surface(shape = RoundedCornerShape(10.dp), color = PaleBlue) {
                                                        Text("${index + 1}", Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = Blue, fontWeight = FontWeight.Bold)
                                                    }
                                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        Text(plannedLesson.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                        Text("${plannedLesson.category} · ${plannedLesson.estimatedMinutes} dk", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 11.sp)
                                                    }
                                                    TextButton(onClick = {
                                                        lessonReturnScreen = "Dersler"
                                                        activeCourseLessonId = plannedLesson.id
                                                        activeLesson = plannedLesson.title
                                                        activeLessonIndex = 0
                                                        lessonAnswer = false
                                                        lessonStage = 0
                                                        currentScreen = "Ders İçeriği"
                                                    }) { Text("Başla") }
                                                }
                                            }
                                        }
                                        if (studyPlan.reviewWords.isNotEmpty()) {
                                            Text("🧠 ${studyPlan.reviewWords.size} kelime tekrar listende", color = if (darkTheme) Color(0xFFFFD18A) else Color(0xFF7C4A03), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                            if (dueCount > 0) {
                                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (darkTheme) Color(0xFF302B28) else Color(0xFFFFF7E8))) {
                                    Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🧠", fontSize = 25.sp)
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text("$dueCount kelime tekrar bekliyor", fontWeight = FontWeight.Bold, color = if (darkTheme) Color(0xFFFFD18A) else Color(0xFF7C4A03))
                                                Text("Unutmadan önce bilgini tazele.", fontSize = 12.sp, color = if (darkTheme) Color(0xFFE4C58E) else Color(0xFF926C30))
                                            }
                                            TextButton(onClick = { placementMode = false; startQuiz(); sessionAnswered = 0; sessionCorrect = 0; sessionActive = true; currentScreen = "Sınav" }) { Text("Test et") }
                                        }
                                        TextButton(onClick = { showReviewQueue = !showReviewQueue }) {
                                            Text(if (showReviewQueue) "Tekrar listesini gizle" else "Kelime listesini aç")
                                        }
                                        if (showReviewQueue) {
                                            val adaptiveAdvice = AdaptiveLearningAdvisor.recommend(learningStore.all())
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (darkTheme) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF2F0FF)
                                                )
                                            ) {
                                                Column(
                                                    Modifier.fillMaxWidth().padding(12.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(adaptiveAdvice.title, color = if (darkTheme) Color(0xFFC5B9FF) else Color(0xFF4F46A5), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text(adaptiveAdvice.message, color = if (darkTheme) Color(0xFFD0D3E4) else Color(0xFF55536B), fontSize = 12.sp, lineHeight = 17.sp)
                                                }
                                            }
                                            dueReviewWords.take(5).forEach { progress ->
                                                val translation = words.firstOrNull { it.en.equals(progress.word, ignoreCase = true) }?.tr
                                                Column(Modifier.fillMaxWidth().background(if (darkTheme) MaterialTheme.colorScheme.surfaceVariant else Color.White, RoundedCornerShape(12.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                                    Text(progress.word, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                    Text(translation ?: "Türkçe karşılık sözlükte bulunamadı", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                                                    Text("Doğru: ${progress.correctCount} · Hata: ${progress.mistakeCount} · Başarı: ${progress.accuracyPercent}%", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 11.sp)
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        OutlinedButton(
                                                            onClick = {
                                                                scope.launch {
                                                                    withContext(Dispatchers.IO) {
                                                                        learningStore.rateRecall(progress.word, quality = 1)
                                                                    }
                                                                    reviewRefresh++
                                                                }
                                                            },
                                                            modifier = Modifier.weight(1f)
                                                        ) { Text("Zorlandım") }
                                                        Button(
                                                            onClick = {
                                                                scope.launch {
                                                                    withContext(Dispatchers.IO) {
                                                                        learningStore.rateRecall(progress.word, quality = 4)
                                                                    }
                                                                    reviewRefresh++
                                                                }
                                                            },
                                                            modifier = Modifier.weight(1f)
                                                        ) { Text("Biliyordum") }
                                                    }
                                                }
                                            }
                                            if (dueReviewWords.size > 5) {
                                                Text("İlk 5 kelime gösteriliyor. Test modunda tüm tekrar kuyruğu kullanılabilir.", color = Color(0xFF926C30), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (currentScreen == "Dersler") {
                    item {
                        CourseCatalogSection(
                            level = studyPlanLevel,
                            completedIds = completedCourseIds,
                            onLaunch = { selectedLesson ->
                                lessonReturnScreen = "Dersler"
                                activeCourseLessonId = selectedLesson.id
                                activeLesson = selectedLesson.title
                                activeLessonIndex = 0
                                lessonAnswer = false
                                lessonStage = 0
                                lessonChoice = ""
                                currentScreen = "Ders İçeriği"
                            }
                        )
                    }
                }

                if (currentScreen == "Ders İçeriği") {
                    item {
                        val lesson = lessonContent(activeLesson)
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = {
                                    currentScreen = lessonReturnScreen
                                    lessonStage = 0
                                    activeCourseLessonId = null
                                    if (currentScreen == "Dersler") {
                                        selectedTopic = "Tümü"
                                        selected = "A1"
                                    }
                                }) { Text("✕ Çık") }
                                Spacer(Modifier.weight(1f))
                                Text("Bölüm ${lessonStage + 1}/3", color = Blue, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(progress = { (lessonStage + 1) / 3f }, modifier = Modifier.fillMaxWidth(), color = Blue)
                            Text(activeLesson, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            val bookChapter = remember(activeLesson) { LessonBook.chapter(activeLesson) }
                            when (lessonStage) {
                                0 -> {
                                    Text("KONU ANLATIMI · KİTAP MODU", color = Blue, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(bookChapter.purpose, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                            Text(bookChapter.explanation, fontSize = 15.sp, lineHeight = 23.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text("Yapı ve kullanım", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Surface(color = if (darkTheme) Color(0xFF263254) else PaleBlue, shape = RoundedCornerShape(14.dp)) {
                                                Text(bookChapter.formation, Modifier.fillMaxWidth().padding(14.dp), fontSize = 14.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                            Text("Ek örnekler", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                            bookChapter.examples.forEach { example ->
                                                Column(Modifier.fillMaxWidth().background(if (darkTheme) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF7F8FC), RoundedCornerShape(12.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(example.first, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                                    Text(example.second, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                                }
                                            }
                                            Text("Sık yapılan hatalar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                            bookChapter.commonMistakes.forEach { mistake ->
                                                Text("• $mistake", fontSize = 13.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                            Text("Kendini kontrol et", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                            bookChapter.checkpoints.forEachIndexed { index, checkpoint ->
                                                Text("${index + 1}. $checkpoint", fontSize = 13.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }
                                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                                            Text(lesson.heading, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                            Text(lesson.explanation, fontSize = 16.sp, lineHeight = 25.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Surface(color = if (darkTheme) Color(0xFF263254) else PaleBlue, shape = RoundedCornerShape(16.dp)) {
                                                Text(lesson.rule, Modifier.fillMaxWidth().padding(16.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                            Text("Örnekler", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                            lesson.examples.forEach { example ->
                                                Column(Modifier.fillMaxWidth().background(if (darkTheme) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF7F8FC), RoundedCornerShape(14.dp)).padding(13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(example.first, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                                                    Text(example.second, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
                                                }
                                            }
                                            Text("Kelimeler", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                            lesson.words.forEach { pair ->
                                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(pair.first, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                                    Text(pair.second, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                                }
                                                HorizontalDivider(color = Color(0xFFEDEEF4))
                                            }
                                        }
                                    }
                                    Button(onClick = { lessonStage = 1 }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text("Videoya geç  →", modifier = Modifier.padding(6.dp)) }
                                }
                                1 -> {
                                    Text("LISTENING • DİNLEME", color = Blue, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    Text("Kısa konuşmayı dinle, ardından soruları cevapla.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), lineHeight = 22.sp)
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
                                    var slowListening by remember(activeLesson) { mutableStateOf(false) }
                                    val dialogue = remember(activeLesson, lesson.heading) {
                                        listeningDialogueFor(activeLesson, lesson)
                                    }
                                    Card(
                                        shape = RoundedCornerShape(22.dp),
                                        colors = CardDefaults.cardColors(containerColor = if (darkTheme) Color(0xFF19172D) else Color(0xFFF0EDFF))
                                    ) {
                                        Column(
                                            Modifier.fillMaxWidth().padding(18.dp),
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Text("🎧", fontSize = 30.sp)
                                                Column {
                                                    Text("LISTENING PRACTICE", color = if (darkTheme) Color(0xFFBDB4FF) else Color(0xFF6550B8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text(lesson.heading, color = if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                                                }
                                            }
                                            Text("Konuşmayı önce dinle. İstersen tekrar dinleyip sonra metni kontrol et.", color = if (darkTheme) Color(0xFFE5E2FF) else MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                Button(
                                                    onClick = {
                                                        if (listeningReady) {
                                                            listeningTts.language = Locale.US
                                                            listeningTts.setSpeechRate(if (slowListening) 0.75f else 1.0f)
                                                            // Speak the dialogue naturally, without repeated speaker labels.
                                                            val script = dialogue.joinToString(" ") { it.first }
                                                            listeningTts.speak(script, TextToSpeech.QUEUE_FLUSH, null, "lesson_listening_dialogue")
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(14.dp)
                                                , enabled = listeningReady && dialogue.isNotEmpty()
                                                ) { Text(if (listeningReady) "▶ Dinle" else "Ses hazırlanıyor", modifier = Modifier.padding(4.dp)) }
                                                OutlinedButton(
                                                    onClick = { slowListening = !slowListening; listeningTts.stop() },
                                                    shape = RoundedCornerShape(14.dp)
                                                ) { Text(if (slowListening) "🐢 Yavaş" else "1× Normal") }
                                                OutlinedButton(
                                                    onClick = { listeningTts.stop() },
                                                    shape = RoundedCornerShape(14.dp)
                                                ) { Text("■ Durdur") }
                                            }
                                            HorizontalDivider(color = Color(0xFF55516C))
                                            var showListeningTranscript by remember { mutableStateOf(false) }
                                            TextButton(onClick = { showListeningTranscript = !showListeningTranscript }) {
                                                Text(if (showListeningTranscript) "Konuşma metnini gizle" else "Konuşma metnini göster", color = Color(0xFFBDB4FF), fontWeight = FontWeight.Bold)
                                            }
                                            if (showListeningTranscript) {
                                                dialogue.forEachIndexed { index, pair ->
                                                    Row(
                                                        Modifier.fillMaxWidth().background(if (darkTheme) Color(0xFF292541) else Color(0xFFE7E2FA), RoundedCornerShape(12.dp)).padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            Text(if (index % 2 == 0) "A" else "B", color = Color(0xFFFFE500), fontWeight = FontWeight.ExtraBold)
                                                            Text(pair.first, color = if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                                            Text(pair.second, color = if (darkTheme) Color(0xFFD0CBEA) else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                                        }
                                                        IconButton(onClick = {
                                                            if (listeningReady) {
                                                                listeningTts.language = Locale.US
                                                                listeningTts.speak(pair.first, TextToSpeech.QUEUE_FLUSH, null, "lesson_line_$index")
                                                            }
                                                        }) { Text("🔊", fontSize = 20.sp) }
                                                    }
                                                }
                                            }
                                            Text("Dinleme kontrolü", color = if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                            var listeningAnswer by remember(activeLesson) { mutableStateOf<String?>(null) }
                                            val listeningTarget = dialogue.getOrNull(1)
                                            if (listeningTarget != null) {
                                                Text("İkinci konuşmacının cümlesinin Türkçe anlamı hangisi?", color = if (darkTheme) Color(0xFFE5E2FF) else MaterialTheme.colorScheme.onSurfaceVariant)
                                                val listeningOptions = remember(activeLesson, dialogue) {
                                                    val correct = dialogue[1].second
                                                    (listOf(correct) + dialogue.map { it.second }.filter { it != correct }.take(2)).shuffled()
                                                }
                                                listeningOptions.forEach { option ->
                                                    OutlinedButton(
                                                        onClick = { listeningAnswer = option },
                                                        enabled = listeningAnswer == null,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) { Text(option) }
                                                }
                                                if (listeningAnswer != null) {
                                                    Text(
                                                        if (listeningAnswer == listeningTarget.second) "Doğru! Konuşmanın anlamını yakaladın."
                                                        else "Tekrar dinle ve ikinci konuşmacıya odaklan. Doğru anlam: ${listeningTarget.second}",
                                                        color = if (darkTheme) Color(0xFFE5E2FF) else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    TextButton(onClick = { listeningAnswer = null }) { Text("Yeniden dene") }
                                                }
                                            }
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
                                    var practiceScored by remember(activeLesson) { mutableStateOf(false) }
                                    val practiceQuestions = remember(lesson) {
                                        val base = listOf(lesson.question to lesson.options to lesson.correct).map { Triple(it.first.first, it.first.second, it.second) }
                                        val examples = lesson.examples.map { pair ->
                                            Triple("“${pair.first}” cümlesinin Türkçe karşılığı nedir?", lesson.examples.map { it.second }.distinct().shuffled(), pair.second)
                                        }
                                        (base + examples + examples.reversed() + examples).take(10)
                                    }
                                    val practice = practiceQuestions[practiceIndex.coerceIn(0, practiceQuestions.lastIndex)]
                                    // Keep choices stable while the composable recomposes after an answer.
                                    val practiceOptions = remember(practiceIndex, practiceQuestions) { practice.second.distinct().shuffled() }
                                    Text("ALIŞTIRMA • ${practiceIndex + 1}/${practiceQuestions.size}", color = Blue, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            Text("Soru ${practiceIndex + 1} / ${practiceQuestions.size}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                            LinearProgressIndicator(progress = { (practiceIndex + 1) / practiceQuestions.size.toFloat() }, modifier = Modifier.fillMaxWidth(), color = Blue)
                                            Text(if (practiceIndex == 0) "Boşluğu doldur" else "Anlamı veya doğru kullanımı seç", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                            Text(practice.first, fontSize = 18.sp, lineHeight = 26.sp, color = MaterialTheme.colorScheme.onSurface)
                                            practiceOptions.forEach { option ->
                                                OutlinedButton(
                                                    enabled = !practiceScored,
                                                    onClick = {
                                                        practiceChoice = option
                                                        val correct = option == practice.third
                                                        practiceFeedback = if (correct) "Doğru cevap! ✓" else "Tekrar dene."
                                                        if (correct && !practiceScored) {
                                                            practiceCorrect = (practiceCorrect + 1).coerceAtMost(practiceQuestions.size)
                                                            practiceScored = true
                                                        }
                                                        lessonChoice = if (correct) lesson.correct else option
                                                        lessonAnswer = correct
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        containerColor = if (practiceChoice == option && option == practice.third) Mint
                                                        else if (darkTheme) MaterialTheme.colorScheme.surfaceVariant else Color.White
                                                    )
                                                ) { Text(option, modifier = Modifier.padding(4.dp)) }
                                            }
                                            if (practiceFeedback.isNotBlank()) Text(practiceFeedback, color = if (practiceChoice == practice.third) Color(0xFF16845D) else Color(0xFFB45309), fontWeight = FontWeight.Bold)
                                            Text("Doğru cevaplanan soru: $practiceCorrect / ${practiceQuestions.size}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                                            HorizontalDivider()
                                            Text("Dinleme / konu kontrolü", fontWeight = FontWeight.Bold)
                                            Text(lesson.videoQuestion, fontSize = 16.sp, lineHeight = 24.sp)
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedButton(onClick = { lessonStage = 1 }, modifier = Modifier.weight(1f)) { Text("← Videoya dön") }
                                        Button(
                                            enabled = lessonAnswer,
                                            onClick = {
                                                if (practiceIndex < practiceQuestions.lastIndex) {
                                                    practiceIndex++
                                                    practiceChoice = ""
                                                    practiceFeedback = ""
                                                    practiceScored = false
                                                    lessonAnswer = false
                                                    lessonChoice = ""
                                                } else {
                                                    val key = "${selected}_${activeLessonIndex}_${activeLesson}"
                                                    val done = (prefs.getStringSet("completedLessons", emptySet()) ?: emptySet()) + key
                                                    prefs.edit().putStringSet("completedLessons", done).apply()
                                                    val completedCourseId = activeCourseLessonId
                                                    completedCourseId?.let { courseId ->
                                                        completedCourseIds = CourseProgressStore.markCompleted(prefs, courseId)
                                                    }
                                                    val sessionStart = lessonSessionStartedAt
                                                    val activeMillis = sessionStart?.let { startedAt ->
                                                        (System.currentTimeMillis() - startedAt).coerceAtLeast(0L)
                                                    } ?: 0L
                                                    val elapsedMinutes = StudySessionClock.elapsedWholeMinutes(
                                                        0L,
                                                        lessonSessionAccumulatedMillis + activeMillis
                                                    )
                                                    CourseProgressStore.addStudyMinutes(prefs, elapsedMinutes)
                                                    recordStudyDay()
                                                    lessonSessionStartedAt = null
                                                    activeCourseLessonId = null
                                                    currentScreen = lessonReturnScreen
                                                    if (currentScreen == "Dersler") {
                                                        selectedTopic = "Tümü"
                                                        selected = "A1"
                                                    }
                                                    lessonStage = 0
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) { Text(if (practiceIndex < practiceQuestions.lastIndex) "Sonraki soru →" else "Dersi tamamla ✓") }
                                    }
                                }
                            }
                        }
                    }
                }
                if (currentScreen == "Mesleki İngilizce") {
                    val vocationalTracks = listOf("Uçak Bakım", "Yazılım", "Sağlık", "Turizm", "Otomotiv", "İş ve Lojistik")
                    val aviationModules = listOf(
                        Triple("01", "Uçak ve uçuş kontrol yüzeyleri", "Aileron, elevator, rudder, flap ve spoiler: her parçanın temel görevi, hareket yönleri ve teknik İngilizce örnekleri."),
                        Triple("02", "Kontrol yüzeyleri: hareket ve eksenler", "Roll, pitch, yaw, deflection ve control input terimleri; kavramları birbirinden ayırma ve kısa okuma."),
                        Triple("03", "Gövde, kanat ve yapısal parçalar", "Fuselage, wing, rib, spar, skin, fastener ve access panel; parça adları ve konum tarifleri."),
                        Triple("04", "Havaalanı ve yer operasyonları", "Runway, taxiway, apron, stand, gate, boarding ve ground handling; konum ve hareket bildiren cümleler."),
                        Triple("05", "ATC ve telsiz iletişimi", "Readback, clearance, heading, altitude, maintain ve standby; standart ifadeleri dinleme ve anlama."),
                        Triple("06", "Meteoroloji ve uçuş koşulları", "Visibility, crosswind, turbulence, icing, forecast ve ceiling; hava raporlarından temel bilgiyi çıkarma."),
                        Triple("07", "Elektrik sistemi terminolojisi", "Circuit breaker, wiring, connector, voltage, current ve power supply; şema açıklamalarını okumaya giriş."),
                        Triple("08", "Hidrolik ve pnömatik sistemler", "Hydraulic line, pressure, leak, actuator, reservoir ve pneumatic system; arıza bildirim dili."),
                        Triple("09", "Yakıt ve yağlama sistemleri", "Fuel tank, fuel line, lubrication, contamination ve level; teknik metinlerdeki uyarı sözcükleri."),
                        Triple("10", "İniş takımı ve fren sistemi", "Landing gear, wheel, tyre, brake assembly, extension ve retraction; sistem bileşenlerini tanımlama."),
                        Triple("11", "Motor ve yardımcı güç ünitesi", "Engine, compressor, turbine, exhaust ve auxiliary power unit; temel bileşen adları ve açıklamalar."),
                        Triple("12", "Bakım dokümanları ve teknik yazım", "Manual, task card, revision, reference, requirement ve record; doküman bölümlerini doğru yorumlama."),
                        Triple("13", "Aletler, ölçüm ve atölye dili", "Torque wrench, gauge, measurement, tolerance, calibrated ve tool control; ölçüm sonuçlarını ifade etme."),
                        Triple("14", "Muayene, bulgu ve arıza raporu", "Inspection, finding, defect, discrepancy, evidence ve corrective action; tarafsız rapor cümleleri kurma."),
                        Triple("15", "Emniyet, uyarılar ve insan faktörleri", "Caution, warning, hazard, fatigue, distraction ve checklist; emniyet metinlerini anlama."),
                        Triple("16", "Mesleki okuma ve raporlama", "Kısa teknik pasaj, iş emri, teslim notu ve bakım kaydı üzerinden ana fikir ve ayrıntı bulma."),
                        Triple("17", "Teknik dinleme ve telaffuz", "Parça adları, harf-rakam dizileri ve kısa işyeri konuşmaları; dinledikten sonra ana bilgiyi belirleme."),
                        Triple("18", "Senaryo ve seviye değerlendirmesi", "Terim seçimi, bağlam tamamlama ve modül sonu kontrol soruları; öğrenilen bilgiyi tekrar etme.")
                    )
                    val vocationalModules = when (selectedCareerTrack) {
                        "Yazılım" -> listOf(
                            Triple("01", "Sorun açıklama ve çözüm önerme", "Hata raporu, sorunu yeniden üretme, kayıtları inceleme ve çözüm önerisini açıklama."),
                            Triple("02", "Profesyonel e-posta yazımı", "Durum güncellemesi, hata bildirimi, net konu satırı ve nazik teknik yazışma."),
                            Triple("03", "İş yerinde iletişim", "Gereksinimleri netleştirme, kod incelemesi hakkında konuşma ve takip adımları."),
                            Triple("04", "Teknik metin okuma stratejileri", "Dokümantasyon, sürüm notları, ön koşullar ve teknik yönergeleri okuma."),
                            Triple("05", "Veri ve sonuçları raporlama", "Test sonuçlarını, hata sıklığını ve performans değişikliklerini açık biçimde raporlama.")
                        )
                        "Sağlık" -> listOf(
                            Triple("01", "Sağlık ve günlük sorunlar", "Belirti sorma, şikâyeti anlaşılır biçimde tarif etme ve yardım isteme."),
                            Triple("02", "İş yerinde iletişim", "Hasta veya ekip bilgilerini teyit etme, açıklama isteme ve kibar iletişim."),
                            Triple("03", "Sayılar, saatler ve tarihler", "Randevu saati, tarih, süre ve sayıları yanlış anlaşılmadan söyleme."),
                            Triple("04", "Profesyonel e-posta yazımı", "Randevu, vardiya ve iş amaçlı kısa, açık yazılı iletişim."),
                            Triple("05", "Sorun açıklama ve çözüm önerme", "Sorunu, etkisini ve sonraki adımı düzenli şekilde ifade etme.")
                        )
                        "Turizm" -> listOf(
                            Triple("01", "Seyahat ve konaklama", "Rezervasyon, check-in/check-out, oda isteği ve ulaşım bilgileri."),
                            Triple("02", "Yol tarifi ve şehirde yön bulma", "Misafire yön tarif etme, konum sorma ve mesafeyi açıklama."),
                            Triple("03", "Yemek siparişi ve miktarlar", "Menü, sipariş, alerjiyle ilgili açıklama isteme ve hesap konuşmaları."),
                            Triple("04", "İş yerinde iletişim", "Misafir şikâyetini dinleme, nazik cevap verme ve çözüm sunma."),
                            Triple("05", "Sıklık zarfları ve zaman ifadeleri", "Sefer saatleri, çalışma sıklığı ve günlük hizmet programını anlatma.")
                        )
                        "Otomotiv" -> listOf(
                            Triple("01", "Motor ve güç ünitesi terimleri", "Motor, giriş, kompresör, egzoz ve temel teknik terimler."),
                            Triple("02", "Elektrik ve kablolama", "Konnektör, kablo, süreklilik ve gözlenen durumu raporlama."),
                            Triple("03", "Bakım aletleri ve ekipman", "Atölye aletleri, kontrol, ölçüm ve ekipman adları."),
                            Triple("04", "Kontrol, bulgu ve hasar ifadeleri", "Aşınma, çatlak, hasar ve kontrol bulgularını tarafsız tarif etme."),
                            Triple("05", "Ölçüler, toleranslar ve birimler", "Ölçüm değeri, birim, tolerans ve belirtilen sınırları ifade etme.")
                        )
                        "İş ve Lojistik" -> listOf(
                            Triple("01", "Profesyonel e-posta yazımı", "Sipariş, sevkiyat ve durum güncellemesi için açık iş e-postaları."),
                            Triple("02", "Veri ve sonuçları raporlama", "Teslimat süresi, miktar, eğilim ve performans verilerini anlatma."),
                            Triple("03", "Müzakere ve uzlaşma", "Teslim tarihi, seçenek, karşı teklif ve ortak çözüm konuşmaları."),
                            Triple("04", "İş yerinde iletişim", "Görev devri, durum teyidi ve sonraki adımları netleştirme."),
                            Triple("05", "Olay anlatımı ve bağlaçlar", "Gecikme nedenini, sıralı olayları ve sonuçları açıklama.")
                        )
                        else -> aviationModules
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("MESLEKİ AKADEMİ", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                            Text(if (selectedCareerTrack == "Uçak Bakım") "Uçak Bakım İngilizcesi" else "$selectedCareerTrack İngilizcesi", color = MaterialTheme.colorScheme.onSurface, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                            Text(if (selectedCareerTrack == "Uçak Bakım") "Temel terimlerden operasyonel iletişime kadar adım adım ilerle." else "Mesleğine uygun kelimeleri, diyalogları ve işyeri iletişimini öğren.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("KARİYER YOLUNU SEÇ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                vocationalTracks.forEach { track ->
                                    FilterChip(
                                        selected = selectedCareerTrack == track,
                                        onClick = { selectedCareerTrack = track },
                                        label = { Text(track, maxLines = 1) }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = if (darkTheme) Color(0xFF182747) else Color(0xFFE8EDFF))
                        ) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFF5145CD)) {
                                        Text("✈", modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 24.sp)
                                    }
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(if (selectedCareerTrack == "Uçak Bakım") "Aircraft Maintenance English" else "$selectedCareerTrack English Pathway", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                                        Text("Terminoloji · İletişim · Teknik bilgi", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    listOf("A1–A2 Temel", "B1–B2 Operasyon", "C1 İleri").forEach { stage ->
                                        Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = if (darkTheme) Color(0xFF24365B) else Color.White) {
                                            Text(stage, modifier = Modifier.padding(horizontal = 7.dp, vertical = 10.dp), color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                        }
                                    }
                                }
                                Text("Öğrenme yolu; terim, örnek cümle, bağlam ve kısa kontrol sorularıyla ilerleyecek şekilde düzenlenmiştir.", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                            }
                        }
                    }
                    item {
                        Text("EĞİTİM MODÜLLERİ", color = MaterialTheme.colorScheme.onSurface, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    items(vocationalModules) { module ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                lessonReturnScreen = "Mesleki İngilizce"
                                activeLesson = if (selectedCareerTrack != "Uçak Bakım") module.second else when (module.first) {
                                    "01", "02" -> "Uçuş kumandaları"
                                    "13" -> "Bakım aletleri ve ekipman"
                                    "14" -> "Kontrol, bulgu ve hasar ifadeleri"
                                    "15" -> "Uçak bakımına giriş"
                                    else -> module.second
                                }
                                activeCourseLessonId = "${selectedCareerTrack.lowercase(Locale.ROOT)}-module-${module.first}"
                                activeLessonIndex = 0
                                lessonAnswer = false
                                lessonStage = 0
                                lessonChoice = ""
                                currentScreen = "Ders İçeriği"
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(Modifier.padding(15.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                                Surface(shape = RoundedCornerShape(12.dp), color = if (darkTheme) Color(0xFF30275A) else Color(0xFFEAE5FF)) {
                                    Text(module.first, modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp), color = if (darkTheme) Color(0xFFD5CCFF) else Color(0xFF5145CD), fontWeight = FontWeight.ExtraBold)
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(module.second, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(module.third, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                    item {
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Text("Not: Bu modül başlıkları ve örnek terimler eğitim yolunun kapsamını tanımlar. Tam ders kitapları, telaffuz kayıtları ve sınav içerikleri ayrı ayrı genişletilecek; doğrulanmamış içerik gerçek havacılık talimatı yerine kullanılmamalıdır.", modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }
                }

                if (currentScreen == "Sözlük") {
                    item {
                        Text(if (selectedTopic == "Tümü") "Kelime defterim" else "$selectedTopic · Kelimeler", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    item {
                        OutlinedTextField(
                            value = query, onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("İngilizce veya Türkçe ara") },
                            placeholder = { Text("Örn. apple, elma...") },
                            singleLine = true,
                            shape = RoundedCornerShape(17.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (darkTheme) Color(0xFFB8A7FF) else Blue,
                                focusedLabelColor = if (darkTheme) Color(0xFFB8A7FF) else Blue,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    TextButton(onClick = { query = "" }) { Text("Temizle") }
                                }
                            }
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
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                if (query.isBlank()) "Kelime sayısı" else "Arama sonuçları",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Text(
                                "${filtered.size} kelime",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (filtered.isEmpty()) {
                        item {
                            Card(shape = RoundedCornerShape(20.dp)) {
                                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🔎", fontSize = 32.sp)
                                    Text("Kelime bulunamadı", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Aramayı veya seçtiğin filtreyi değiştir.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    itemsIndexed(filtered, key = { _, word -> word.en }) { wordIndex, word ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (darkTheme) MaterialTheme.colorScheme.surface else
                                    listOf(Color(0xFFFFFBF0), Color(0xFFF4F0FF), Color(0xFFEDFAF7), Color(0xFFFFF0F5), Color(0xFFEEF6FF))[wordIndex % 5]
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (darkTheme) MaterialTheme.colorScheme.outlineVariant else Color(0xFFEAE7F4)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (darkTheme) 1.dp else 4.dp)
                        ) {
                            Column(Modifier.padding(if (compactCards) 11.dp else 17.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(word.en.replaceFirstChar { it.uppercase() }, fontSize = 20.sp, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(Modifier.height(3.dp))
                                        Text(word.tr.replaceFirstChar { it.uppercase() }, fontSize = 15.sp, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f))
                                    }
                                    Surface(
                                        color = if (darkTheme) MaterialTheme.colorScheme.primaryContainer
                                        else listOf(Color(0xFFE9DEFF), Color(0xFFD9F7ED), Color(0xFFFFE6D5), Color(0xFFDDEEFF), Color(0xFFFFE0EB))[filtered.indexOf(word).coerceAtLeast(0) % 5],
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            word.level,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            color = if (darkTheme) MaterialTheme.colorScheme.onPrimaryContainer else Blue,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                if (word.example.isNotBlank()) {
                                    Spacer(Modifier.height(10.dp))
                                    Text("Örnek: ${word.example}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f), fontSize = 13.sp, maxLines = 3, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                }
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    TextButton(onClick = {
                                        if (word.en in learned) {
                                            learned.remove(word.en)
                                        } else {
                                            learned.add(word.en)
                                            if (word.en !in learnedRewarded) {
                                                learnedRewarded.add(word.en)
                                                xp += 5
                                            }
                                            recordStudyDay()
                                        }
                                        prefs.edit()
                                            .putInt("xp", xp)
                                            .putStringSet("learned", learned.toSet())
                                            .putStringSet("learnedRewarded", learnedRewarded.toSet())
                                            .apply()
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
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Çalışma araçların",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Card(
                                modifier = Modifier.weight(1f).clickable { toolsDestination = "Günlük görevler"; showTools = true },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (darkTheme) Color(0xFF2B3657) else Color(0xFFEAE7F4))
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 15.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("📅", fontSize = 23.sp)
                                    Column {
                                        Text("Günlük görevler", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Hedefini tamamla", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 11.sp)
                                    }
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f).clickable { toolsDestination = "Rozetler"; showTools = true },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (darkTheme) Color(0xFF2B3657) else Color(0xFFEAE7F4))
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 15.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("🏅", fontSize = 23.sp)
                                    Column {
                                        Text("Rozetler", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Başarılarını gör", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Card(
                                modifier = Modifier.weight(1f).clickable {
                                    matchWords = quickPracticeWords.shuffled().take(matchPairCount)
                                    matchTurkishOrder = matchWords.shuffled()
                                    matched = emptySet()
                                    selectedEnglish = null
                                    matchMessage = "İngilizce kelimeyi oku ve doğru Türkçe karşılığı seç."
                                    showMatchGame = true
                                },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (darkTheme) Color(0xFF2B3657) else Color(0xFFEAE7F4))
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 15.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("🧩", fontSize = 23.sp)
                                    Column {
                                        Text("Eşleştirme", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Kelimelerle oyna", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 11.sp)
                                    }
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f).clickable { toolsDestination = "İstatistikler"; showTools = true },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (darkTheme) Color(0xFF2B3657) else Color(0xFFEAE7F4))
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 15.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("📊", fontSize = 23.sp)
                                    Column {
                                        Text("İstatistikler", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("İlerlemeni takip et", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text("İpucu: Kelimeleri favorilerine ekleyip sonra tekrar çalışabilirsin.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
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
                            Brush.linearGradient(listOf(Color(0xFF4B35A8), Color(0xFF6550D9), Color(0xFF8B5CF6))),
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
                Text("Hoş geldin! ✨", fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        text = {
            Column(modifier = Modifier.heightIn(max = 410.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Sana uygun dersleri hazırlayalım. Seviyeni seçebilir ya da kısa sınavla belirleyebilirsin.", fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (placementCompleted) {
                    Surface(color = if (darkTheme) Color(0xFF15372F) else Color(0xFFE1F8F0), contentColor = if (darkTheme) Color(0xFFB9F5DF) else Color(0xFF087F5B), shape = RoundedCornerShape(14.dp)) {
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
                Text("İngilizceyi hangi amaçla öğreniyorsun?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                listOf("Genel İngilizce", "Seyahat", "Okul ve sınav", "Mesleki İngilizce").forEach { purpose ->
                    Row(Modifier.fillMaxWidth().clickable { learningPurpose = purpose }.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = learningPurpose == purpose, onClick = { learningPurpose = purpose })
                        Text(purpose, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Text("İngilizce seviyen", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                listOf("A1" to "Başlangıç", "A2" to "Temel", "B1" to "Orta", "B2" to "Orta-ileri", "C1" to "İleri").forEach { (code, desc) ->
                    Surface(
                        color = if (darkTheme) { if (onboardingChoice == code) Color(0xFF30275A) else Color(0xFF171F36) } else { if (onboardingChoice == code) Color(0xFFEDE9FE) else Color(0xFFF8FAFC) },
                        shape = RoundedCornerShape(14.dp),
                        onClick = { onboardingChoice = code }
                    ) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = onboardingChoice == code, onClick = { onboardingChoice = code })
                            Column {
                                Text(code, fontWeight = FontWeight.Bold, color = if (darkTheme) Color(0xFFC5B9FF) else Color(0xFF6D28D9))
                                Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        prefs.edit().putString("chosenLevel", chosenLevel).putString("learningPurpose", learningPurpose).apply()
                        if (learningPurpose == "Mesleki İngilizce") currentScreen = "Mesleki İngilizce"
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
        text = { Column(modifier = Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(28.dp), color = PaleBlue) {
                    Text("🧑‍🎓", modifier = Modifier.padding(18.dp), fontSize = 34.sp)
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (darkTheme) Color(0xFF1C2442) else Color(0xFFF0EDFF),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (darkTheme) Color(0xFF39466C) else Color(0xFFDCD5FA))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(displayName, color = MaterialTheme.colorScheme.onSurface, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                    Text(username, color = if (darkTheme) Color(0xFFC5B9FF) else Color(0xFF6D28D9), fontSize = 13.sp)
                    Text("Seviye ${xp / 100 + 1}  •  ${xp} XP  •  ${dailyStreak} günlük seri", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    LinearProgressIndicator(
                        progress = { ((xp % 100).toFloat() / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF8B78EF),
                        trackColor = if (darkTheme) Color(0xFF39466C) else Color(0xFFDCD5FA)
                    )
                    Text("Sonraki seviyeye ${100 - (xp % 100)} XP", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = if (darkTheme) Color(0xFF24213F) else Color(0xFFF1EEFF)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("WORDMASTER ID", color = if (darkTheme) Color(0xFFFFA6B3) else Color(0xFFB91C1C), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 1.sp)
                    Text(userId, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Bu cihazdaki profil kimliğin. Kullanıcı adı uygunluğu ve arkadaş ekleme için çevrim içi hesap sunucusu henüz bağlı değil.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (darkTheme) Color(0xFF171F36) else Color(0xFFF8F7FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (darkTheme) Color(0xFF343D5B) else Color(0xFFE7E2FA))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("HESAP BİLGİLERİ", color = if (darkTheme) Color(0xFFC5B9FF) else Color(0xFF6D28D9), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 1.sp)
                    Text("E-posta ve telefon bilgilerini profilinde sakla.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp)
                    OutlinedTextField(
                        value = emailDraft,
                        onValueChange = { emailDraft = it.take(120); profileError = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("E-posta adresi") },
                        placeholder = { Text("ornek@eposta.com") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email),
                        leadingIcon = { Text("✉️") }
                    )
                    OutlinedTextField(
                        value = phoneDraft,
                        onValueChange = { phoneDraft = it.filter { ch -> ch.isDigit() || ch == '+' || ch == ' ' || ch == '-' || ch == '(' || ch == ')' }.take(24); profileError = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Telefon numarası (isteğe bağlı)") },
                        placeholder = { Text("+90 5xx xxx xx xx") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                        leadingIcon = { Text("📱") }
                    )
                    Text("Önemli: Bu sürümde bilgiler bu cihazda saklanır. E-posta doğrulama, SMS kodu ve cihazlar arası hesap kurtarma için güvenli çevrim içi hesap sunucusu henüz bağlanmadı.", color = if (darkTheme) Color(0xFFFFC1C8) else Color(0xFF7A5260), fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
            OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it.take(30) }, modifier = Modifier.fillMaxWidth(), label = { Text("Görünen ad") }, singleLine = true)
            OutlinedTextField(value = usernameDraft, onValueChange = { value -> usernameDraft = value.removePrefix("@").filter { ch -> ch.isLetterOrDigit() || ch == '_' }.take(20).let { clean -> if (clean.isBlank()) "" else "@$clean" }; profileError = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Kullanıcı adı") }, supportingText = { Text("3-20 karakter; harf, rakam ve _ kullan.") }, singleLine = true)
            profileError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            Text("📚 Öğrenilen: ${learned.size} kelime   ⭐ Favori: ${favorites.size}", color = MaterialTheme.colorScheme.onSurface)
            Text("Bu sayılar farklı ölçümleri gösterir: öğrenilenler tamamlanan kelimeler, favoriler kaydedilenler, tekrar istatistikleri ise çalışılmış kelimelerdir.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, lineHeight = 16.sp)
            Text("📖 Tamamlanan ders: ${CourseProgressStore.completedCount(prefs)}", color = MaterialTheme.colorScheme.onSurface)
            Text("⏱️ Günlük çalışma: ${CourseProgressStore.studyMinutes(prefs)} / ${CourseProgressStore.dailyGoalMinutes(prefs)} dakika", color = MaterialTheme.colorScheme.onSurface)
            val todayWordCount = if (prefs.getString("todayWordsDate", "") == today) prefs.getInt("todayWords", 0) else 0
            val learningSummary = learningStore.summary()
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = if (darkTheme) Color(0xFF15372F) else Color(0xFFEAF7F1)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("BUGÜNKÜ ÖĞRENME PLANI", color = if (darkTheme) Color(0xFF8DE0BE) else Color(0xFF24785A), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 0.7.sp)
                    Text("$todayWordCount / $dailyGoal kelime hedefi", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { (todayWordCount.toFloat() / dailyGoal.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF319B73),
                        trackColor = Color(0xFFD2E9DF)
                    )
                    Text("Tekrar bekleyen: ${learningSummary.dueWords} kelime", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    Text("Kelime doğruluğu: ${learningSummary.accuracyPercent}%  •  Öğrenilen toplam: ${learningSummary.totalWords}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                }
            }
            Text("🏆 En iyi seri: $bestCorrectStreak   🎯 Test doğrusu: $totalCorrect", color = MaterialTheme.colorScheme.onSurface)
        } },
        confirmButton = { TextButton(onClick = {
            val cleanUsername = usernameDraft.trim().removePrefix("@")
            if (cleanUsername.length !in 3..20 || !cleanUsername.matches(Regex("[A-Za-z0-9_]+"))) {
                profileError = "Kullanıcı adı 3-20 karakter olmalı; İngilizce harf, rakam ve _ kullan."
            } else {
                val cleanEmail = emailDraft.trim()
                if (cleanEmail.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                    profileError = "Geçerli bir e-posta adresi gir."
                } else {
                    displayName = nameDraft.trim().ifBlank { "Kelime Kaşifi" }
                    username = "@$cleanUsername"
                    accountEmail = cleanEmail
                    accountPhone = phoneDraft.trim()
                    prefs.edit()
                        .putString("displayName", displayName)
                        .putString("username", username)
                        .putString("accountEmail", accountEmail)
                        .putString("accountPhone", accountPhone)
                        .apply()
                    showProfile = false
                }
            }
        }) { Text("Kaydet") } },
        dismissButton = { TextButton(onClick = { showProfile = false }) { Text("Vazgeç") } }
    )

    if (showSettings) AlertDialog(
        onDismissRequest = { showSettings = false },
        title = { Text("Görünüm ve ayarlar ⚙️", fontWeight = FontWeight.ExtraBold) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Görünüm modu", fontWeight = FontWeight.Bold)
            Text("İstediğin zaman değiştir; seçimin bu cihazda hatırlanır.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            listOf("Açık tema", "Koyu tema", "Sistem ayarını izle").forEachIndexed { index, label ->
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(
                        if (themeChoice == index) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                    ).clickable { themeChoice = index; prefs.edit().putInt("themeChoice", index).apply() }.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = themeChoice == index, onClick = { themeChoice = index; prefs.edit().putInt("themeChoice", index).apply() })
                    Text(when (index) { 0 -> "☀️  Açık tema"; 1 -> "🌙  Koyu tema"; else -> "📱  Sistem ayarını izle" }, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            HorizontalDivider()
            Text("Günlük kelime hedefi", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15, 20).forEach { goal ->
                    FilterChip(selected = dailyGoal == goal, onClick = { dailyGoal = goal; prefs.edit().putInt("dailyGoal", goal).apply() }, label = { Text("$goal") })
                }
            }
            HorizontalDivider()
            Text("Günlük çalışma süresi hedefi", fontWeight = FontWeight.Bold)
            Text("Ders ekranında geçirilen süre kaydedilir; tamamlanan kısa oturumlar en az 1 dakika sayılır.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(5, 10, 15, 20, 30, 45, 60).forEach { goalMinutes ->
                    FilterChip(
                        selected = dailyStudyGoal == goalMinutes,
                        onClick = {
                            dailyStudyGoal = goalMinutes
                            CourseProgressStore.setDailyGoalMinutes(prefs, goalMinutes)
                        },
                        label = { Text("$goalMinutes") }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Kompakt kelime kartları", fontWeight = FontWeight.SemiBold)
                    Text("Daha fazla kelimeyi ekranda göster", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = compactCards, onCheckedChange = { compactCards = it; prefs.edit().putBoolean("compactCards", it).apply() })
            }
            Text("Seçimler bu cihazda saklanır.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } },
        confirmButton = { TextButton(onClick = {
            prefs.edit().putInt("themeChoice", themeChoice).putInt("dailyGoal", dailyGoal).putBoolean("compactCards", compactCards).apply()
            CourseProgressStore.setDailyGoalMinutes(prefs, dailyStudyGoal)
            showSettings = false
        }) { Text("Uygula") } },
        dismissButton = { TextButton(onClick = { showSettings = false }) { Text("Kapat") } }
    )

    if (quizWord != null) {
        AlertDialog(
            onDismissRequest = { quizWord = null; quizMessage = null; sessionActive = false; placementMode = false },
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
                                    val savedToday = prefs.getString("todayWordsDate", "") == today
                                    val todayAnswered = (if (savedToday) prefs.getInt("todayWords", 0) else 0) + 1
                                    prefs.edit().putString("todayWordsDate", today).putInt("todayWords", todayAnswered).apply()
                                    recordStudyDay()
                                    if (placementMode) {
                                        placementAnswered++
                                        if (choice.en == quizWord!!.en) placementCorrect++
                                    }
                                    val answerWasCorrect = choice.en == quizWord!!.en
                                    // Her cevap gerçek aralıklı tekrar kaydına yazılır; doğru/yanlış aralığı günceller.
                                    scope.launch {
                                        val updatedDueWords = withContext(Dispatchers.IO) {
                                            learningStore.rateRecall(quizWord!!.en, if (answerWasCorrect) 4 else 1)
                                            learningStore.recordStudyDay()
                                            learningStore.dueForReview()
                                        }
                                        dueReviewWords = updatedDueWords
                                        dueReviewCount = updatedDueWords.size
                                    }
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
                    Text("Bu test: $sessionCorrect / $sessionAnswered   •   Genel: $quizCorrect / $quizCount", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text("Akıllı tekrar: $dueReviewCount kelime hazır • Cevapların tekrar tarihini günceller", color = Blue, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (quizMessage == null) { quizWord = null; sessionActive = false; placementMode = false }
                    else if (sessionActive && sessionAnswered >= 10) { quizWord = null; sessionActive = false; showQuizSummary = true }
                    else startQuiz()
                }) { Text(if (quizMessage == null) "Kapat" else if (sessionActive && sessionAnswered >= 10) "Sonucu gör" else "Sonraki soru") }
            },
            dismissButton = { TextButton(onClick = { quizWord = null; quizMessage = null; sessionActive = false; placementMode = false }) { Text("Bitir") }
            }
        )
    }

    if (showTools) AlertDialog(
        onDismissRequest = { showTools = false },
        title = { Text(toolsDestination, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (toolsDestination) {
                    "Günlük görevler" -> {
                        Text("BUGÜNÜN HEDEFLERİ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 1.sp)
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
                        ) { Text(if (dailyRewardClaimed) "Bugünkü ödül alındı" else "Hedef ödülünü al (+20 XP, +10 coin)") }
                        if (!dailyRewardClaimed && learned.size < 10) {
                            Text("Ödülün açılması için toplam 10 kelime öğrenmelisin.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                    "Rozetler" -> {
                        Text("Kazandığın rozetler ve açılacak sonraki başarılar.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        listOf(
                            Triple("İlk kelime", learned.isNotEmpty(), "En az 1 kelime öğren"),
                            Triple("100 XP", xp >= 100, "100 deneyim puanına ulaş"),
                            Triple("50 kelime", learned.size >= 50, "50 kelime öğren"),
                            Triple("5 doğru üst üste", bestCorrectStreak >= 5, "Bir testte 5 doğruyu arka arkaya yap"),
                            Triple("7 günlük seri", dailyStreak >= 7, "7 günlük çalışma serisi tamamla")
                        ).forEach { badge ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(if (badge.second) "🏅" else "🔒", fontSize = 24.sp)
                                    Column(Modifier.weight(1f)) {
                                        Text(badge.first, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                        Text(if (badge.second) "Tamamlandı" else badge.third, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    }
                                    if (badge.second) Text("✓", color = Color(0xFF20B486), fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                    else -> {
                        Text("Öğrenme ilerlemen", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text("Öğrenilen kelime: ${learned.size} / ${words.size}")
                        Text("XP: $xp  •  Seviye: ${xp / 100 + 1}  •  Coin: $coins")
                        Text("Doğru cevap: $totalCorrect / $totalAnswered")
                        Text("Başarı oranı: ${if (totalAnswered == 0) 0 else totalCorrect * 100 / totalAnswered}%")
                        Text("En uzun doğru seri: $bestCorrectStreak")
                        Text("Tekrar kuyruğu: ${reviewWords.size} kelime")
                        Text("Tamamlanan mesleki ders: ${completedCourseIds.size}")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { showTools = false }) { Text("Kapat") } }
    )

    if (showMatchGame) {
        val targetWord = matchWords.firstOrNull { it.en !in matched }
        val answerOptions = remember(targetWord?.en, matchWords, matched, quickPracticeWords) {
            if (targetWord == null) emptyList()
            else {
                // Use the full practice pool first, then the current round as a fallback.
                // This keeps answer buttons useful even when the external pool is small.
                val distractors = (quickPracticeWords + matchWords)
                    .filter {
                        !it.en.equals(targetWord.en, ignoreCase = true) &&
                            it.tr.isNotBlank() &&
                            !it.tr.equals(targetWord.tr, ignoreCase = true)
                    }
                    .distinctBy { it.tr.trim().lowercase(Locale.ROOT) }
                    .shuffled()
                    .take(3)
                (listOf(targetWord) + distractors).shuffled()
            }
        }
        AlertDialog(
            onDismissRequest = { showMatchGame = false },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            title = { Text("Kelime Arenası", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(Modifier.fillMaxWidth().heightIn(max = 680.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("GÜNLÜK ANTRENMAN", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                Text("${matched.size}/${matchWords.size}", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                            }
                            Text("Anlamını bul!", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                            Text("İngilizce kelimeyi oku ve doğru Türkçe karşılığı seç.", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f), fontSize = 13.sp)
                            LinearProgressIndicator(
                                progress = { if (matchWords.isEmpty()) 0f else matched.size.toFloat() / matchWords.size },
                                modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                    if (targetWord != null) {
                        Text("İNGİLİZCE KELİME", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 1.sp)
                        Surface(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = if (darkTheme) Color(0xFF28215A) else Color(0xFFEAE5FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
                        ) {
                            Column(Modifier.padding(vertical = 22.dp, horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Text("Aa", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                                Text(targetWord.en.replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Text("Doğru anlamı seç", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f), fontSize = 12.sp)
                            }
                        }
                        Text("DOĞRU ANLAMI SEÇ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 1.sp)
                        answerOptions.forEachIndexed { index, option ->
                            OutlinedButton(
                                onClick = {
                                    if (option.en == targetWord.en) {
                                        // Guard against rapid repeated taps before Compose redraws.
                                        if (targetWord.en !in matchRewarded) {
                                            matchRewarded.add(targetWord.en)
                                            matched = matched + targetWord.en
                                            xp += 5
                                            recordStudyDay()
                                            prefs.edit().putInt("xp", xp).apply()
                                            matchMessage = if (matched.size == matchWords.size) "Harika! Tüm kelimeler tamamlandı. +${matchWords.size * 5} XP 🎉" else "Doğru cevap! +5 XP. Sıradaki kelimeye geçtin."
                                        }
                                    } else {
                                        matchMessage = "Tekrar dene! ${targetWord.en} kelimesinin anlamını düşün."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Surface(shape = RoundedCornerShape(9.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                        Text(('A'.code + index).toChar().toString(), Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.ExtraBold)
                                    }
                                    Text(option.tr.replaceFirstChar { it.uppercase() }, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                        }
                    } else {
                        Text("🏆 Antrenman tamamlandı!", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("${matchWords.size} kelimeyi bitirdin ve ${matchWords.size * 5} XP kazandın.")
                    }
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f), modifier = Modifier.fillMaxWidth()) {
                        Text(matchMessage, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                    }
                    Text("ZORLUK", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(4, 6, 8).forEach { pairCount ->
                            val active = matchPairCount == pairCount
                            OutlinedButton(
                                onClick = {
                                    matchPairCount = pairCount
                                    prefs.edit().putInt("matchPairCount", pairCount).apply()
                                    matchWords = quickPracticeWords.shuffled().take(pairCount)
                                    matchTurkishOrder = matchWords.shuffled()
                                    matched = emptySet()
                                    selectedEnglish = null
                                    matchMessage = "Yeni zorluk seçildi. İlk kelimenin anlamını bul."
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) { Text("$pairCount çift", fontSize = 11.sp, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    matchWords = quickPracticeWords.shuffled().take(matchPairCount)
                    matchTurkishOrder = matchWords.shuffled()
                    matchRound++
                    matched = emptySet()
                    selectedEnglish = null
                    matchMessage = "Yeni tur! İlk kelimenin doğru anlamını bul."
                }) { Text("Yeni tur") }
            },
            dismissButton = { TextButton(onClick = { showMatchGame = false }) { Text("Bitir") } }
        )
    }

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

private suspend fun findHumanPronunciationAudio(word: String): String? = withContext(Dispatchers.IO) {
    val safeWord = word.trim().lowercase(Locale.ROOT)
    if (safeWord.isBlank() || !safeWord.matches(Regex("[a-z][a-z' -]{0,79}"))) return@withContext null
    try {
        val encoded = java.net.URLEncoder.encode(safeWord, "UTF-8")
        val connection = java.net.URL("https://api.dictionaryapi.dev/api/v2/entries/en/$encoded").openConnection() as java.net.HttpURLConnection
        connection.connectTimeout = 6000
        connection.readTimeout = 6000
        try {
            if (connection.responseCode !in 200..299) return@withContext null
            val entries = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
            for (i in 0 until entries.length()) {
                val phonetics = entries.getJSONObject(i).optJSONArray("phonetics") ?: continue
                for (j in 0 until phonetics.length()) {
                    val audio = phonetics.optJSONObject(j)?.optString("audio").orEmpty()
                    if (audio.startsWith("https://") && audio.contains(".mp3")) return@withContext audio
                }
            }
            null
        } finally { connection.disconnect() }
    } catch (_: Exception) { null }
}

@Composable
private fun PronunciationPracticeDialog(words: List<Word>, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var current by remember { mutableStateOf(words.firstOrNull() ?: Word("hello", "merhaba", "A1")) }
    var searchText by remember { mutableStateOf("") }
    var pronunciationLevel by remember { mutableStateOf("Tümü") }
    var choices by remember {
        mutableStateOf(
            (listOf(current) + words.filter { !it.en.equals(current.en, ignoreCase = true) }
                .distinctBy { it.en.trim().lowercase(Locale.ROOT) }
                .shuffled().take(3)).shuffled()
        )
    }
    var message by remember { mutableStateOf("Kayıtlı insan sesleri kullanılır; ses bulunamazsa yapay sesle değiştirilmez.") }
    var isLoading by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var recognizedCorrectly by remember { mutableStateOf(false) }
    var currentQuestionRecorded by remember { mutableStateOf(false) }
    var pronunciationCorrect by remember { mutableIntStateOf(0) }
    var pronunciationAnswered by remember { mutableIntStateOf(0) }

    fun playRecorded(term: String) {
        scope.launch {
            isLoading = true
            message = "Kayıtlı telaffuz aranıyor…"
            mediaPlayer?.let { old -> try { old.stop() } catch (_: Exception) { }; old.release() }
            mediaPlayer = null
            val url = findHumanPronunciationAudio(term)
            if (url == null) {
                message = "Bu kelime için kayıtlı ses bulunamadı. Başka bir kelime dene."
                isLoading = false
                return@launch
            }
            try {
                val player = MediaPlayer()
                mediaPlayer = player
                player.setDataSource(url)
                player.setOnPreparedListener {
                    isLoading = false
                    message = "Kayıtlı telaffuz çalınıyor: $term"
                    it.start()
                }
                player.setOnCompletionListener { message = "Dinleme tamamlandı." }
                player.setOnErrorListener { _, _, _ ->
                    isLoading = false
                    message = "Ses oynatılamadı. İnternet bağlantını kontrol et."
                    true
                }
                player.prepareAsync()
            } catch (_: Exception) {
                isLoading = false
                message = "Ses kaydı açılamadı. İnternet bağlantını kontrol et."
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.let { player -> try { player.stop() } catch (_: Exception) { }; player.release() } }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = { Text("🎧 Kelime Telaffuzu") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 680.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("LISTENING LAB  •  PRONUNCIATION", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        Text("Dinle · Tanı · Öğren", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Kayıtlı insan telaffuzu aranır; ses bulunamazsa yapay sesle değiştirilmez.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
                OutlinedTextField(
                    value = searchText, onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Kelime ara") },
                    placeholder = { Text("Örn. beautiful") }, singleLine = true
                )
                Button(onClick = {
                    val term = searchText.trim()
                    if (term.isBlank()) message = "Önce bir İngilizce kelime yaz." else playRecorded(term)
                }, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
                    Text(if (isLoading) "Ses aranıyor…" else "🔎 Kayıtlı telaffuzu dinle")
                }
                Text("ÇALIŞMA SEVİYESİ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 1.sp)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Tümü", "A1", "A2", "B1", "B2", "C1").forEach { level ->
                        FilterChip(
                            selected = pronunciationLevel == level,
                            onClick = {
                                pronunciationLevel = level
                                message = if (level == "Tümü") "Tüm seviyeler seçildi. Sonraki kelimeyle devam edebilirsin."
                                else "$level seviyesi seçildi. Sonraki kelime bu seviyeden seçilecek."
                            },
                            label = { Text(level) }
                        )
                    }
                }
                HorizontalDivider()
                Text("KELİME TANIMA", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 1.sp)
                val pronunciationAccuracy = if (pronunciationAnswered == 0) 0 else pronunciationCorrect * 100 / pronunciationAnswered
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(Modifier.weight(1f), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("DOĞRU", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("$pronunciationCorrect", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Surface(Modifier.weight(1f), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("CEVAPLANAN", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("$pronunciationAnswered", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Surface(Modifier.weight(1f), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("BAŞARI", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("$pronunciationAccuracy%", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Oturum başarısı", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("$pronunciationCorrect / $pronunciationAnswered doğru", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f), fontSize = 11.sp)
                    }
                    LinearProgressIndicator(
                        progress = { pronunciationAccuracy / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("KELİME KARTI", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 1.2.sp)
                            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(current.level, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(current.en, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 25.sp)
                        Text(current.tr, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f), fontSize = 15.sp, lineHeight = 21.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Text(topicForWord(current), modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text("Dinle ve anlamı hatırla", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 11.sp)
                        }
                        if (current.example.isNotBlank()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Text("ÖRNEK KULLANIM", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                            Text(current.example, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, lineHeight = 19.sp)
                        }
                        Button(onClick = { playRecorded(current.en) }, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
                            Text(if (isLoading) "Ses aranıyor…" else "🔊 Telaffuzu dinle")
                        }
                    }
                }
                Text("Duyduğun/öğrendiğin kelimeyi tanı", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                choices.forEach { option ->
                    OutlinedButton(
                        onClick = {
                            if (!recognizedCorrectly) {
                                if (!currentQuestionRecorded) {
                                    pronunciationAnswered++
                                    currentQuestionRecorded = true
                                }
                                if (option.en == current.en) {
                                    pronunciationCorrect++
                                    recognizedCorrectly = true
                                    message = "Doğru! Kelimeyi tekrar dinleyip sesli tekrar etmeyi dene. ✓"
                                } else {
                                    message = "Henüz değil. İpucu: ${current.tr}. Tekrar dinleyip yeniden dene."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !recognizedCorrectly,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (recognizedCorrectly && option.en == current.en) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text(option.en.replaceFirstChar { it.uppercase() }) }
                }
                if (message.isNotBlank()) Text(message, color = if (message.startsWith("Doğru")) Color(0xFF12805C) else Color(0xFFB45309), fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val levelPool = words.filter { pronunciationLevel == "Tümü" || it.level.equals(pronunciationLevel, ignoreCase = true) }
                    .ifEmpty { words }
                val nextWord = levelPool.filter { it.en != current.en }.randomOrNull()
                    ?: levelPool.randomOrNull()
                    ?: Word("hello", "merhaba", "A1")
                current = nextWord
                choices = (listOf(nextWord) + levelPool
                    .filter { !it.en.equals(nextWord.en, ignoreCase = true) }
                    .distinctBy { it.en.trim().lowercase(Locale.ROOT) }
                    .shuffled().take(3)).shuffled()
                recognizedCorrectly = false
                currentQuestionRecorded = false
                message = "Yeni kelime seçildi. Önce dinle, sonra doğru kelimeyi tanı."
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

private fun listeningDialogueFor(title: String, lesson: LessonContent): List<Pair<String, String>> {
    val t = title.lowercase(Locale.ROOT).replace("i\u0307", "i")
    return when {
        "selam" in t || "tanış" in t || "greeting" in t ->
            listOf("Hello, I'm Ece. What's your name?" to "Merhaba, ben Ece. Senin adın ne?",
                "My name is Ali. Nice to meet you." to "Benim adım Ali. Tanıştığıma memnun oldum.",
                "Nice to meet you, too. Are you a new student?" to "Ben de tanıştığıma memnun oldum. Yeni öğrenci misin?",
                "Yes, I am. Where is classroom two?" to "Evet. İkinci sınıf nerede?")
        "alışveriş" in t || "shopping" in t ->
            listOf("Excuse me, how much is this T-shirt?" to "Affedersiniz, bu tişört ne kadar?",
                "It's fifteen pounds." to "On beş sterlin.",
                "Do you have it in a smaller size?" to "Daha küçük bedeni var mı?",
                "Yes, here you are. Would you like a receipt?" to "Evet, buyurun. Fiş ister misiniz?",
                "Yes, please. Thank you." to "Evet, lütfen. Teşekkür ederim.")
        "yol tarifi" in t || "direction" in t ->
            listOf("Excuse me, how can I get to the station?" to "Affedersiniz, istasyona nasıl gidebilirim?",
                "Go straight and turn left at the bank." to "Dümdüz gidin ve bankada sola dönün.",
                "Is it far from here?" to "Buradan uzak mı?",
                "No, it's about five minutes on foot." to "Hayır, yürüyerek yaklaşık beş dakika.")
        "yemek" in t || "food" in t || "sipariş" in t ->
            listOf("Are you ready to order?" to "Sipariş vermeye hazır mısınız?",
                "I'd like a chicken sandwich and water, please." to "Bir tavuklu sandviç ve su istiyorum, lütfen.",
                "Would you like anything else?" to "Başka bir şey ister misiniz?",
                "No, that's all. How much is it?" to "Hayır, hepsi bu. Ne kadar?")
        "rutin" in t || "routine" in t || "alışkanlık" in t ->
            listOf("What time do you get up on weekdays?" to "Hafta içi saat kaçta kalkarsın?",
                "I usually get up at seven." to "Genellikle yedide kalkarım.",
                "Do you have breakfast at home?" to "Evde kahvaltı yapar mısın?",
                "Yes, and then I take the bus to school." to "Evet, sonra okula otobüsle giderim.")
        "aile" in t || "family" in t ->
            listOf("Have you got any brothers or sisters?" to "Erkek veya kız kardeşin var mı?",
                "Yes, I have one older sister." to "Evet, bir ablam var.",
                "What is she like?" to "Nasıl biridir?",
                "She's friendly and very patient." to "Arkadaş canlısı ve çok sabırlıdır.")
        "sağlık" in t || "health" in t ->
            listOf("Good morning. What seems to be the problem?" to "Günaydın. Sorununuz nedir?",
                "I have a headache and a sore throat." to "Başım ağrıyor ve boğazım ağrıyor.",
                "How long have you felt this way?" to "Ne zamandır böyle hissediyorsunuz?",
                "Since yesterday. What should I do?" to "Dünden beri. Ne yapmalıyım?")
        "yazılım" in t || "software" in t || "program" in t ->
            listOf("The application crashes when I open the settings page." to "Ayarlar sayfasını açınca uygulama çöküyor.",
                "Can you reproduce the problem?" to "Sorunu yeniden oluşturabiliyor musun?",
                "Yes. It happens on every attempt." to "Evet. Her denemede oluyor.",
                "Please check the logs and create a bug report." to "Lütfen kayıtları kontrol et ve hata raporu oluştur.")

        "sayılar, saatler" in t || "tarihler" in t ->
            listOf("What time does the lesson start?" to "Ders saat kaçta başlıyor?",
                "It starts at half past eight on Tuesday." to "Salı günü sekiz buçukta başlıyor.",
                "Is the test on the twenty-eighth of October?" to "Sınav yirmi sekiz Ekim'de mi?",
                "Yes, and please arrive ten minutes early." to "Evet, lütfen on dakika erken gel.")
        "karşılaştırmalar" in t || "görüşleri karşılaştırma" in t ->
            listOf("Which route is faster, the train or the bus?" to "Hangi rota daha hızlı, tren mi otobüs mü?",
                "The train is faster, but it is more expensive." to "Tren daha hızlı ama daha pahalı.",
                "That's true. The bus is cheaper than the train." to "Doğru. Otobüs trenden daha ucuz.",
                "So let's take the train today and the bus tomorrow." to "O hâlde bugün trenle, yarın otobüsle gidelim.")
        "şimdiki zaman" in t ->
            listOf("What are you doing right now?" to "Şu anda ne yapıyorsun?",
                "I'm preparing for my English lesson." to "İngilizce dersime hazırlanıyorum.",
                "Is your brother studying too?" to "Erkek kardeşin de ders çalışıyor mu?",
                "No, he's making dinner." to "Hayır, akşam yemeği hazırlıyor.")
        "gelecek planları" in t ->
            listOf("What are you going to do this weekend?" to "Bu hafta sonu ne yapacaksın?",
                "I'm going to visit my grandparents." to "Büyükannemle büyükbabamı ziyaret edeceğim.",
                "I think it will rain on Sunday." to "Sanırım pazar günü yağmur yağacak.",
                "Then we'll stay inside and play a board game." to "O zaman içeride kalıp kutu oyunu oynarız.")
        "can, must" in t || "can / can't" in t ->
            listOf("Can you use this computer?" to "Bu bilgisayarı kullanabilir misin?",
                "Yes, but I must finish the safety training first." to "Evet ama önce güvenlik eğitimini bitirmeliyim.",
                "Do we have to bring our own headphones?" to "Kendi kulaklığımızı getirmek zorunda mıyız?",
                "No, the school provides them." to "Hayır, okul sağlıyor.")
        "sıklık zarfları" in t ->
            listOf("How often do you practise English?" to "Ne sıklıkla İngilizce pratik yaparsın?",
                "I usually practise after dinner." to "Genellikle akşam yemeğinden sonra pratik yaparım.",
                "Do you ever listen to English on the bus?" to "Otobüste hiç İngilizce dinler misin?",
                "Sometimes, but I rarely use subtitles." to "Bazen ama nadiren altyazı kullanırım.")
        "seyahat ve konaklama" in t || "travel" in t ->
            listOf("Hello, I have a reservation under the name Demir." to "Merhaba, Demir adına rezervasyonum var.",
                "Certainly. Could I see your booking confirmation?" to "Elbette. Rezervasyon onayınızı görebilir miyim?",
                "Here it is. What time is check-out?" to "Buyurun. Çıkış saati kaçta?",
                "Check-out is at eleven in the morning." to "Çıkış sabah saat on birde.")
        "yer edatları" in t || "there is / there are" in t ->
            listOf("Where are my keys?" to "Anahtarlarım nerede?",
                "They're on the table, next to your bag." to "Masada, çantanın yanında.",
                "And where is my phone?" to "Peki telefonum nerede?",
                "It's under the notebook." to "Defterin altında.")
        "sayılabilen ve sayılamayan" in t || "yemek siparişi ve miktarlar" in t ->
            listOf("Would you like some water?" to "Biraz su ister misin?",
                "Yes, please. And could I have two apples?" to "Evet, lütfen. İki elma da alabilir miyim?",
                "Of course. Anything else?" to "Elbette. Başka bir şey?",
                "No, that's enough, thank you." to "Hayır, bu yeterli, teşekkürler.")
        "geçmiş zaman: düzenli ve düzensiz" in t ->
            listOf("What did you do yesterday?" to "Dün ne yaptın?",
                "I visited my cousin and we watched a film." to "Kuzenimi ziyaret ettim ve film izledik.",
                "Did you go home late?" to "Eve geç gittin mi?",
                "No, I took the last bus at nine." to "Hayır, dokuzdaki son otobüse bindim.")
        "deneyimleri anlatmak" in t || "present perfect" in t ->
            listOf("Have you ever worked on a group project?" to "Hiç grup projesinde çalıştın mı?",
                "Yes, I have. I have worked on two projects this year." to "Evet. Bu yıl iki projede çalıştım.",
                "Did you enjoy the last one?" to "Sonuncusundan hoşlandın mı?",
                "Yes, but we finished it late last Friday." to "Evet ama geçen cuma geç bitirdik.")
        "koşul cümleleri" in t || "conditional" in t ->
            listOf("What will you do if the bus is late?" to "Otobüs geç kalırsa ne yapacaksın?",
                "If it is late, I'll call the school." to "Geç kalırsa okulu arayacağım.",
                "What would you do if you had a free month?" to "Bir ay boş zamanın olsa ne yapardın?",
                "I'd travel and practise English every day." to "Seyahat eder ve her gün İngilizce pratik yapardım.")
        "phrasal verbs" in t || "yaygın phrasal" in t ->
            listOf("Did you find out why the meeting changed?" to "Toplantının neden değiştiğini öğrendin mi?",
                "Yes, they had to put it off until Thursday." to "Evet, perşembeye ertelemek zorunda kaldılar.",
                "Should I write down the new time?" to "Yeni saati not almalı mıyım?",
                "Yes, and please let the rest of the team know." to "Evet, lütfen ekibin geri kalanına da haber ver.")
        "olay anlatımı ve bağlaçlar" in t ->
            listOf("What happened during the test?" to "Test sırasında ne oldu?",
                "The first attempt failed because one step was missing." to "İlk deneme bir adım eksik olduğu için başarısız oldu.",
                "What happened next?" to "Sonra ne oldu?",
                "We corrected the list; however, we checked it again before continuing." to "Listeyi düzelttik; ancak devam etmeden önce tekrar kontrol ettik.")
        "sorun açıklama ve çözüm" in t ->
            listOf("What is the main issue with the new process?" to "Yeni süreçteki temel sorun nedir?",
                "It takes too long when several requests arrive together." to "Birden fazla istek aynı anda geldiğinde çok uzun sürüyor.",
                "What do you recommend?" to "Ne öneriyorsun?",
                "I suggest reviewing the logs first and then testing one change." to "Önce kayıtları incelemeyi, sonra bir değişikliği test etmeyi öneriyorum.")
        "akademik paragraf" in t || "academic writing" in t ->
            listOf("What is the main claim of your paragraph?" to "Paragrafının temel iddiası nedir?",
                "Regular feedback can improve learning outcomes." to "Düzenli geri bildirim öğrenme sonuçlarını iyileştirebilir.",
                "What evidence supports that claim?" to "Bu iddiayı hangi kanıt destekliyor?",
                "The study reports progress, but it also notes limitations." to "Çalışma ilerleme bildiriyor ancak sınırlamalara da değiniyor.")
        "toplantılarda fikir savunma" in t || "meetings" in t ->
            listOf("Could we review the delivery date?" to "Teslim tarihini gözden geçirebilir miyiz?",
                "Certainly. The current schedule leaves little time for testing." to "Elbette. Mevcut program test için çok az zaman bırakıyor.",
                "Could we move the review to Thursday?" to "İncelemeyi perşembeye alabilir miyiz?",
                "That would work if we share the draft by Tuesday." to "Taslağı salıya kadar paylaşırsak bu uygun olur.")
        "veri ve sonuçları raporlama" in t || "reporting data" in t ->
            listOf("What changed in the latest report?" to "Son raporda ne değişti?",
                "Response time decreased by eight percent compared with last month." to "Yanıt süresi geçen aya kıyasla yüzde sekiz azaldı.",
                "Does that prove the new system caused the change?" to "Bu, değişime yeni sistemin neden olduğunu kanıtlar mı?",
                "Not by itself. We need to check other factors as well." to "Tek başına kanıtlamaz. Diğer etkenleri de kontrol etmeliyiz.")
        "müzakere ve uzlaşma" in t || "negotiation" in t ->
            listOf("We need the report by Wednesday." to "Rapora çarşambaya kadar ihtiyacımız var.",
                "Could we deliver the main findings on Wednesday and the appendix on Thursday?" to "Ana bulguları çarşamba, eki perşembe teslim edebilir miyiz?",
                "That could work. Can you confirm the exact sections?" to "Olabilir. Kesin bölümleri teyit edebilir misin?",
                "Yes, I'll send the agreed outline this afternoon." to "Evet, üzerinde anlaştığımız taslağı bu öğleden sonra göndereceğim.")
        "anlam incelikleri" in t || "nuance" in t ->
            listOf("Would you say the proposal is certain to work?" to "Bu önerinin kesinlikle işe yarayacağını söyler misin?",
                "I would say the evidence suggests it may work, with some limitations." to "Kanıtın bazı sınırlamalarla işe yarayabileceğine işaret ettiğini söylerdim.",
                "So you support it, but cautiously?" to "Yani destekliyorsun ama temkinli biçimde mi?",
                "Exactly. The wording should reflect that level of confidence." to "Aynen. İfade, bu güven düzeyini yansıtmalı.")
        "sunum ve ikna edici konuşma" in t || "presentations" in t ->
            listOf("Today I'll compare two options for reducing delays." to "Bugün gecikmeleri azaltmak için iki seçeneği karşılaştıracağım.",
                "What evidence supports the first option?" to "İlk seçeneği hangi kanıt destekliyor?",
                "The trial showed a shorter response time, although the sample was small." to "Deneme daha kısa yanıt süresi gösterdi ancak örneklem küçüktü.",
                "Please explain how you would test it on a larger scale." to "Lütfen bunu daha geniş ölçekte nasıl test edeceğini açıkla.")
        "birden fazla kaynaktan sentez" in t || "synthesis" in t ->
            listOf("What do the two reports agree on?" to "İki rapor hangi konuda aynı fikirde?",
                "Both identify cost as a major concern." to "İkisi de maliyeti önemli bir endişe olarak belirtiyor.",
                "Do they recommend the same solution?" to "Aynı çözümü mü öneriyorlar?",
                "No. One suggests training, while the other recommends changing the process." to "Hayır. Biri eğitim önerirken diğeri sürecin değiştirilmesini öneriyor.")
        "ima, ton ve örtük anlam" in t || "implicit meaning" in t ->
            listOf("The manager said, 'That is an ambitious deadline.'" to "Yönetici, 'Bu iddialı bir son tarih,' dedi.",
                "Was that praise or a concern?" to "Bu övgü müydü yoksa bir endişe mi?",
                "The context suggests the schedule may be unrealistic." to "Bağlam, programın gerçekçi olmayabileceğine işaret ediyor.",
                "So we should ask which tasks can be prioritised." to "O hâlde hangi görevlere öncelik verilebileceğini sormalıyız.")
        "ileri düzey tartışma" in t || "debate" in t ->
            listOf("Should the team adopt the new system immediately?" to "Ekip yeni sistemi hemen benimsemeli mi?",
                "The pilot results are promising, but the sample was limited." to "Pilot sonuçları umut verici ancak örneklem sınırlıydı.",
                "Could a phased rollout reduce that risk?" to "Aşamalı uygulama bu riski azaltabilir mi?",
                "Possibly. We should compare the cost, evidence, and potential impact first." to "Olabilir. Önce maliyeti, kanıtı ve olası etkiyi karşılaştırmalıyız.")
        "metin düzenleme ve üslup" in t || "editing" in t ->
            listOf("I think this paragraph repeats the same point." to "Bence bu paragraf aynı noktayı tekrarlıyor.",
                "Agreed. We can remove the second sentence without losing the meaning." to "Katılıyorum. Anlamı kaybetmeden ikinci cümleyi kaldırabiliriz.",
                "Does the final sentence make the recommendation clear?" to "Son cümle öneriyi açıkça ifade ediyor mu?",
                "It should state the action and the reason more directly." to "Eylemi ve gerekçeyi daha doğrudan belirtmeli.")
        "profesyonel iletişim ve ikna" in t ->
            listOf("What would make the proposal valuable to the team?" to "Öneriyi ekip için değerli kılacak şey nedir?",
                "It could reduce delays, but we should verify the result with a small trial." to "Gecikmeleri azaltabilir ancak sonucu küçük bir denemeyle doğrulamalıyız.",
                "What risk should we communicate?" to "Hangi riski açıklamalıyız?",
                "The trial may not represent every situation, so we should review the evidence before expanding it." to "Deneme her durumu temsil etmeyebilir; bu yüzden kapsamı büyütmeden önce kanıtı gözden geçirmeliyiz.")
        "fikir belirtme" in t || "iş yerinde iletişim" in t || "toplantılarda fikir savunma" in t ->
            listOf("I think we should update the plan." to "Bence planı güncellemeliyiz.",
                "What is the main reason?" to "Temel gerekçe nedir?",
                "The current process takes too long, and the data supports that." to "Mevcut süreç çok uzun sürüyor ve veriler bunu destekliyor.",
                "That makes sense. Could you share the report with the team?" to "Mantıklı. Raporu ekiple paylaşabilir misin?")
        "profesyonel e-posta" in t || "e-posta yazımı" in t ->
            listOf("Could you send me the updated schedule, please?" to "Güncellenmiş programı bana gönderebilir misiniz lütfen?",
                "Certainly. Do you need it before the meeting?" to "Elbette. Toplantıdan önce mi gerekiyor?",
                "Yes, by three o'clock would be ideal." to "Evet, saat üçe kadar olması ideal olur.",
                "I'll attach it to my reply this afternoon." to "Bu öğleden sonra yanıtıma ekleyeceğim.")
        "dinlemede ayrıntı" in t || "uzun konuşmalarda ana fikir" in t || "ileri dinleme" in t ->
            listOf("The briefing will begin at nine in room four." to "Bilgilendirme saat dokuzda dördüncü odada başlayacak.",
                "Please bring the inspection summary and two copies of the chart." to "Lütfen kontrol özetini ve grafiğin iki kopyasını getir.",
                "After the briefing, the team will review the open actions." to "Bilgilendirmeden sonra ekip açık eylemleri gözden geçirecek.",
                "So, nine o'clock, room four, and two copies of the chart." to "Yani saat dokuz, dördüncü oda ve grafiğin iki kopyası.")
        "gövde ve yapısal parçalar" in t ->
            listOf("Which area is described in the inspection note?" to "Kontrol notunda hangi bölge anlatılıyor?",
                "The note refers to the fuselage skin near a frame." to "Not, bir çerçevenin yakınındaki gövde kaplamasından bahsediyor.",
                "Is the cause confirmed?" to "Neden doğrulandı mı?",
                "No. The report records the observation only." to "Hayır. Rapor yalnızca gözlemi kaydediyor.")
        "motor ve güç ünitesi" in t || "motor kontrolü" in t ->
            listOf("What did the report say about the engine indication?" to "Rapor motor göstergesi hakkında ne söyledi?",
                "It says the value fluctuated during the observation." to "Gözlem sırasında değerin dalgalandığını söylüyor.",
                "Does the report identify the cause?" to "Rapor nedeni belirtiyor mu?",
                "No. The approved troubleshooting procedure must be consulted." to "Hayır. Onaylı arıza giderme prosedürüne başvurulmalı.")
        "hidrolik sistemler" in t ->
            listOf("What was recorded during the inspection?" to "Kontrol sırasında ne kaydedildi?",
                "A possible hydraulic fluid leak was observed." to "Olası bir hidrolik sıvı kaçağı gözlendi.",
                "Did the report include a location?" to "Raporda konum belirtildi mi?",
                "Yes, it records the observed area without guessing the cause." to "Evet, neden tahmin etmeden gözlenen alanı kaydediyor.")
        "elektrik ve kablolama" in t ->
            listOf("What should the report identify?" to "Rapor neyi tanımlamalı?",
                "The connector location and the observed condition." to "Konnektörün konumunu ve gözlenen durumu.",
                "Can we invent a test result?" to "Bir test sonucu uydurabilir miyiz?",
                "No. Record only verified results from the approved procedure." to "Hayır. Yalnızca onaylı prosedürden doğrulanmış sonuçları kaydet.")
        "aviyonik ve iletişim" in t ->
            listOf("The crew reported an intermittent communication issue." to "Ekip aralıklı bir iletişim sorunu bildirdi.",
                "Was a fault code confirmed?" to "Bir arıza kodu doğrulandı mı?",
                "Not yet. We need to follow the aircraft-specific troubleshooting data." to "Henüz değil. Uçağa özgü arıza giderme verisini izlemeliyiz.",
                "Understood. Record the exact report first." to "Anlaşıldı. Önce bildirimi aynen kaydet.")
        "bakım kayıtları ve teknik dokümanlar" in t || "task card" in t || "iş emri okuma" in t ->
            listOf("Which document revision are you using?" to "Hangi doküman revizyonunu kullanıyorsun?",
                "The current approved revision listed for this task." to "Bu iş için listelenen güncel onaylı revizyonu.",
                "Have you checked the task prerequisites?" to "İşin ön koşullarını kontrol ettin mi?",
                "Yes, and I will record completion in the designated field." to "Evet ve tamamlanmayı belirtilen alana kaydedeceğim.")
        "alet kontrolü" in t || "yabancı cisim" in t ->
            listOf("Have all tools been accounted for?" to "Tüm aletlerin sayımı yapıldı mı?",
                "The inventory is complete, and one item needs rechecking." to "Envanter tamamlandı ve bir öğenin yeniden kontrol edilmesi gerekiyor.",
                "What about the work area?" to "Çalışma alanı ne durumda?",
                "It has been checked for foreign object debris." to "Yabancı cisim kalıntıları açısından kontrol edildi.")
        "human factors" in t || "vardiya teslimi" in t ->
            listOf("What needs to be included in the handover?" to "Devir teslimde neler yer almalı?",
                "The open task, current status, and next action." to "Açık iş, mevcut durum ve sonraki adım.",
                "Should we mention the unresolved item?" to "Çözülmemiş maddeyi belirtmeli miyiz?",
                "Yes, clearly and without making assumptions." to "Evet, açıkça ve varsayım yapmadan.")
        "arıza ve bulgu raporu" in t || "kontrol, bulgu" in t || "hasar ifadeleri" in t ->
            listOf("What did you observe?" to "Ne gözlemledin?",
                "A visible crack near the bracket." to "Braket yakınında görünür bir çatlak.",
                "Is the cause known?" to "Nedeni biliniyor mu?",
                "No. I recorded the finding and referred it for assessment." to "Hayır. Bulguyu kaydettim ve değerlendirmeye yönlendirdim.")
        "ölçüler, toleranslar" in t || "bağlantı elemanları" in t || "malzemeler ve korozyon" in t || "pnömatik sistemler" in t ->
            listOf("Where did you verify the required limit?" to "Gerekli limiti nereden doğruladın?",
                "In the current approved technical documentation." to "Güncel onaylı teknik dokümandan.",
                "Should we estimate it from memory?" to "Hafızamızdan tahmin etmeli miyiz?",
                "No. Use the specified data and record the result accurately." to "Hayır. Belirtilen veriyi kullan ve sonucu doğru kaydet.")
        "ileri düzey" in t || "sentez" in t || "akademik paragraf" in t || "eleştirel okuma" in t || "metin düzenleme" in t || "rapor ve öneri" in t || "örtük anlam" in t || "müzakere" in t ->
            listOf("The first source supports the proposal, but the second raises a concern." to "İlk kaynak öneriyi destekliyor ancak ikinci kaynak bir endişe dile getiriyor.",
                "How should we evaluate the difference?" to "Farkı nasıl değerlendirmeliyiz?",
                "We should compare the evidence, limitations, and context." to "Kanıtı, sınırlılıkları ve bağlamı karşılaştırmalıyız.",
                "Agreed. Then we can state a balanced conclusion." to "Katılıyorum. Ardından dengeli bir sonuç belirtebiliriz.")

        "havacılık" in t || "uçak" in t || "maintenance" in t || "flight control" in t ->
            listOf("What did you find during the inspection?" to "Kontrol sırasında ne buldun?",
                "There is visible damage near the access panel." to "Erişim panelinin yakınında görünür hasar var.",
                "Has the finding been recorded?" to "Bulgu kaydedildi mi?",
                "Yes. I recorded the location and the observed condition." to "Evet. Konumu ve gözlenen durumu kaydettim.")
        else -> listOf(
            "Could you explain that one more time, please?" to "Bunu bir kez daha açıklayabilir misiniz, lütfen?",
            (lesson.examples.getOrNull(0)?.first ?: lesson.heading) to (lesson.examples.getOrNull(0)?.second ?: lesson.explanation),
            "Can you give me an example?" to "Bir örnek verebilir misiniz?",
            (lesson.examples.getOrNull(1)?.first ?: lesson.rule) to (lesson.examples.getOrNull(1)?.second ?: lesson.explanation)
        )
    }
}

private fun lessonContent(title: String): LessonContent {
    val t = title.lowercase(Locale.ROOT).replace("i\u0307", "i")
    return when {
        "past continuous" in t -> LessonContent("Geçmişte devam eden eylemler", "Geçmişte belirli bir anda devam etmekte olan işleri anlatırız. Genellikle başka bir olay olduğunda devam eden eylem için kullanılır.", "Özne + was/were + fiil-ing", listOf("I was studying at 8 p.m." to "Saat 8'de ders çalışıyordum.", "They were playing when it started to rain." to "Yağmur başladığında oyun oynuyorlardı.", "She wasn't sleeping." to "Uyumuyordu."), listOf("while" to "-iken", "when" to "-dığı zaman", "study" to "ders çalışmak", "wait" to "beklemek"), "I ___ watching TV when you called.", listOf("was", "were", "am", "did"), "was", "Videoda was/were yardımcı fiillerinin hangi öznelerle kullanıldığını dinle.")
        "simple past" in t || "past events" in t -> LessonContent("Geçmişte tamamlanan olaylar", "Geçmişte belirli bir zamanda bitmiş olayları anlatır. Düzenli fiiller çoğunlukla -ed alır; düzensiz fiillerin ikinci hâli kullanılır.", "Özne + fiilin 2. hâli", listOf("I visited my aunt yesterday." to "Dün teyzemi ziyaret ettim.", "He went to school." to "Okula gitti.", "We didn't watch TV." to "Televizyon izlemedik."), listOf("yesterday" to "dün", "last week" to "geçen hafta", "visited" to "ziyaret etti", "went" to "gitti"), "She ___ to the museum yesterday.", listOf("went", "go", "goes", "going"), "went", "Videodaki örneklerde geçmiş zaman fiilinin ikinci hâline dikkat et.")
        ("present perfect" in t || "life experiences" in t || "deneyimleri anlatmak" in t) -> LessonContent("Deneyimler ve sonucu süren durumlar", "Present Perfect, zamanı kesin belirtilmeyen deneyimler veya geçmişte başlayıp etkisi süren durumlar için kullanılır.", "Özne + have/has + V3", listOf("I have visited London." to "Londra'yı ziyaret ettim.", "She has finished her homework." to "Ödevini bitirdi.", "Have you ever tried sushi?" to "Hiç suşi denedin mi?"), listOf("already" to "çoktan", "yet" to "henüz", "ever" to "hiç", "never" to "asla / hiç"), "He ___ finished his work.", listOf("has", "have", "was", "did"), "has", "Videoda have ve has kullanımını ve fiilin üçüncü hâlini belirle.")
        "passive voice" in t -> LessonContent("Edilgen yapı", "Eylemi kimin yaptığı değil, eylemden etkilenen şey önemliyse edilgen yapı kullanılır.", "Özne + be + V3", listOf("The bridge was built in 1990." to "Köprü 1990'da inşa edildi.", "English is spoken here." to "Burada İngilizce konuşulur.", "The meal was prepared by Ali." to "Yemek Ali tarafından hazırlandı."), listOf("built" to "inşa edildi", "made" to "yapıldı", "spoken" to "konuşulan", "by" to "tarafından"), "The letters ___ sent yesterday.", listOf("were", "was", "are", "did"), "were", "Videoda edilgen yapıda be fiili ile V3 arasındaki ilişkiye dikkat et.")
        "reported speech" in t -> LessonContent("Dolaylı anlatım", "Birinin sözlerini doğrudan alıntılamak yerine aktardığımızda zaman ve kişi zamirleri değişebilir.", "He said (that) + aktarılan cümle", listOf("“I am tired.” → He said he was tired." to "“Yorgunum.” → Yorgun olduğunu söyledi.", "“I can swim.” → She said she could swim." to "“Yüzebilirim.” → Yüzebildiğini söyledi."), listOf("said" to "söyledi", "told" to "anlattı / söyledi", "asked" to "sordu", "promised" to "söz verdi"), "He said he ___ tired.", listOf("was", "is", "were", "be"), "was", "Videoda doğrudan konuşmadan dolaylı anlatıma geçerken zaman değişimini dinle.")
        "sayılar, saatler ve tarihler" in t -> LessonContent(
            "Sayılar, saatler ve tarihler",
            "Saat söylerken o'clock, half past ve quarter past/to kalıpları kullanılır. Tarihlerde gün ve ay adlarını doğru sırayla söylemeye dikkat et.",
            "What time is it? · It is half past seven. · on Monday · in October",
            listOf("My class starts at nine o'clock." to "Dersim saat dokuzda başlıyor.", "Today is the twenty-eighth of October." to "Bugün yirmi sekiz Ekim.", "The meeting is on Friday." to "Toplantı cuma günü."),
            listOf("quarter past" to "çeyrek geçe", "half past" to "buçuk", "date" to "tarih", "calendar" to "takvim"),
            "The lesson starts ___ 8:30.",
            listOf("at", "on", "in", "by"), "at",
            "Saatler için at, günler için on, aylar ve yıllar için in kullanımını karşılaştır."
        )
        "karşılaştırmalar" in t -> LessonContent(
            "Karşılaştırmalar",
            "İki kişi veya şeyi karşılaştırırken kısa sıfatlara çoğunlukla -er, uzun sıfatlara more eklenir. Karşılaştırılan ikinci unsurdan önce than kullanılır.",
            "short adjective + -er + than · more + long adjective + than",
            listOf("A train is faster than a bus." to "Tren otobüsten daha hızlıdır.", "This exercise is more difficult than that one." to "Bu alıştırma şundan daha zordur.", "My bag is cheaper than yours." to "Çantam seninkinden daha ucuz."),
            listOf("faster" to "daha hızlı", "cheaper" to "daha ucuz", "more difficult" to "daha zor", "than" to "-den daha"),
            "A plane is ___ than a car.",
            listOf("fast", "faster", "fastest", "more fast"), "faster",
            "Sıfatın biçimini ve cümlede than bulunup bulunmadığını kontrol et."
        )
        "şimdiki zaman" in t -> LessonContent(
            "Şimdiki zaman",
            "Şu anda devam eden eylemler için am/is/are + fiil-ing kullanılır. He, she ve it ile is; I ile am; you, we ve they ile are gelir.",
            "Özne + am/is/are + verb-ing",
            listOf("I am studying now." to "Şu anda ders çalışıyorum.", "She is reading a book." to "Kitap okuyor.", "They aren't sleeping." to "Uyumuyorlar."),
            listOf("now" to "şimdi", "currently" to "şu anda", "reading" to "okuyor", "working" to "çalışıyor"),
            "They ___ playing football now.",
            listOf("is", "am", "are", "be"), "are",
            "Önce özneye uygun be fiilini, sonra -ing almış ana fiili bul."
        )
        "gelecek planları" in t -> LessonContent(
            "Gelecek planları: going to ve will",
            "Önceden düşünülmüş planlarda be going to, anlık karar ve tahminlerde will sık kullanılır. Will sonrasında fiilin yalın hâli gelir.",
            "Özne + am/is/are going to + V1 · Özne + will + V1",
            listOf("I'm going to study tonight." to "Bu akşam ders çalışacağım.", "I think it will rain." to "Sanırım yağmur yağacak.", "We are going to visit Ankara." to "Ankara'yı ziyaret edeceğiz."),
            listOf("plan" to "plan", "tonight" to "bu akşam", "probably" to "muhtemelen", "tomorrow" to "yarın"),
            "She will ___ you later.",
            listOf("calls", "calling", "call", "called"), "call",
            "Will sonrasında fiilin yalın hâlini kullan."
        )
        "can, must ve have to" in t -> LessonContent(
            "Can, must ve have to",
            "Can yetenek veya izin, must güçlü zorunluluk, have to ise kural ya da dış koşuldan doğan gereklilik anlatabilir. Bu yapılardan sonra fiilin yalın hâli gelir.",
            "can + V1 · must + V1 · have/has to + V1",
            listOf("You must wear a seat belt." to "Emniyet kemeri takmalısın.", "I can speak English." to "İngilizce konuşabilirim.", "He has to work today." to "Bugün çalışmak zorunda."),
            listOf("permission" to "izin", "must" to "-meli / -malı", "have to" to "zorunda olmak", "rule" to "kural"),
            "You must ___ the instructions.",
            listOf("follow", "follows", "following", "followed"), "follow",
            "Modal fiilden sonra -s veya -ing eklemeden fiilin yalın hâlini kullan."
        )
        "sıklık zarfları" in t -> LessonContent(
            "Sıklık zarfları ve zaman ifadeleri",
            "Always, usually, often, sometimes ve rarely eylemin ne sıklıkla yapıldığını belirtir. Be fiilinden sonra; diğer fiillerden önce kullanılırlar.",
            "usually + main verb · be + usually",
            listOf("I usually walk to school." to "Okula genellikle yürürüm.", "She is always on time." to "O her zaman zamanında gelir.", "We rarely eat out." to "Nadiren dışarıda yemek yeriz."),
            listOf("always" to "her zaman", "usually" to "genellikle", "often" to "sık sık", "rarely" to "nadiren"),
            "He ___ drinks coffee in the morning.",
            listOf("usually", "yesterday", "next", "already"), "usually",
            "Sıklık zarfının be fiiliyle ve diğer fiillerle yerini karşılaştır."
        )
        "sağlık ve günlük sorunlar" in t -> LessonContent(
            "Sağlık ve günlük sorunlar",
            "Bir belirtiyi anlatırken I have a headache, I feel sick veya It hurts kalıpları kullanılır. Yardım isterken Could you help me? gibi kibar ifadeler seçilir.",
            "I have a ... · I feel ... · It hurts here.",
            listOf("I have a headache." to "Başım ağrıyor.", "My throat hurts." to "Boğazım ağrıyor.", "Could you help me, please?" to "Lütfen bana yardım edebilir misiniz?"),
            listOf("headache" to "baş ağrısı", "sore throat" to "boğaz ağrısı", "appointment" to "randevu", "hurt" to "ağrımak"),
            "I have a ___; my head hurts.",
            listOf("headache", "ticket", "platform", "receipt"), "headache",
            "Belirtiyi tarif eden kelimeyi ve yardım isteme kalıbını öğren."
        )
        "seyahat ve konaklama" in t -> LessonContent(
            "Seyahat ve konaklama",
            "Rezervasyon, check-in ve ulaşım hakkında soru sorarken kibar kalıplar kullanılır. Rezervasyon bilgilerini tarih ve isimle birlikte net söyle.",
            "I'd like to book ... · I have a reservation. · What time is check-out?",
            listOf("I have a reservation under the name Yılmaz." to "Yılmaz adına rezervasyonum var.", "What time does the train leave?" to "Tren saat kaçta kalkıyor?", "Could I have a room key, please?" to "Oda anahtarını alabilir miyim?"),
            listOf("reservation" to "rezervasyon", "check-in" to "giriş işlemi", "check-out" to "çıkış işlemi", "departure" to "kalkış"),
            "I have a ___ for tonight.",
            listOf("reservation", "headache", "weather", "homework"), "reservation",
            "Seyahat diyaloglarında rezervasyon, saat ve kibar istek ifadelerine dikkat et."
        )
        "yol tarifi" in t || "şehirde yön bulma" in t -> LessonContent(
            "Yol tarifi ve şehirde yön bulma",
            "Yön tarifinde go straight, turn left/right ve next to gibi kalıplar kullanılır. Where is ...? bir yerin konumunu sormak için temel kalıptır.",
            "Go straight. · Turn left. · It is next to ...",
            listOf("Go straight for two blocks." to "İki blok dümdüz git.", "Turn right at the bank." to "Bankada sağa dön.", "The station is opposite the museum." to "İstasyon müzenin karşısında."),
            listOf("opposite" to "karşısında", "between" to "arasında", "corner" to "köşe", "turn left" to "sola dön"),
            "The pharmacy is ___ the bank and the café.",
            listOf("between", "yesterday", "expensive", "usually"), "between",
            "Bir yerin konumunu tarif eden edat ve yön ifadelerini ayırt et."
        )
        "yer edatları" in t -> LessonContent(
            "Yer edatları",
            "In, on, under, behind, in front of ve next to nesnelerin konumunu belirtir. Edatı seçerken iki nesne arasındaki ilişkiye bak.",
            "in = içinde · on = üstünde · under = altında · behind = arkasında",
            listOf("The keys are on the table." to "Anahtarlar masanın üstünde.", "The cat is under the chair." to "Kedi sandalyenin altında.", "The school is next to the park." to "Okul parkın yanında."),
            listOf("behind" to "arkasında", "in front of" to "önünde", "under" to "altında", "next to" to "yanında"),
            "The bag is ___ the chair, not on it.",
            listOf("under", "between", "during", "often"), "under",
            "Edatın, nesnenin konumunu cümleye uygun biçimde tarif ettiğini kontrol et."
        )
        "sayılabilen ve sayılamayan isimler" in t -> LessonContent(
            "Sayılabilen ve sayılamayan isimler",
            "Book ve apple gibi isimler sayılabilir; water ve rice gibi maddeler çoğunlukla sayılamaz. Some olumlu cümlelerde, any soru ve olumsuz cümlelerde sık kullanılır.",
            "many + countable plural · much + uncountable · some/any",
            listOf("We have some apples." to "Biraz elmamız var.", "There isn't much water." to "Çok su yok.", "How many books do you need?" to "Kaç kitaba ihtiyacın var?"),
            listOf("countable" to "sayılabilen", "uncountable" to "sayılamayan", "much" to "çok (sayılamayan)", "many" to "çok (sayılabilen)"),
            "How ___ water do you need?",
            listOf("many", "much", "few", "several"), "much",
            "İsmin sayılabilir olup olmadığını belirleyip many veya much seç."
        )
        "a / an ve tekil isimler" in t -> LessonContent(
            "A / an ve tekil isimler",
            "Tekil sayılabilen isimlerden önce a veya an gelir. Seçim yazılı harfe değil, kelimenin başlangıç sesine göre yapılır.",
            "a + consonant sound · an + vowel sound",
            listOf("a technician" to "bir teknisyen", "an engineer" to "bir mühendis", "an hour" to "bir saat", "a university" to "bir üniversite"),
            listOf("technician" to "teknisyen", "engineer" to "mühendis", "hour" to "saat", "university" to "üniversite"),
            "She is ___ engineer.",
            listOf("a", "an", "the", "no article"), "an",
            "A/an seçerken ilk sesin ünlü olup olmadığına dikkat et."
        )
        "temel sıfatlar ve betimleme" in t -> LessonContent(
            "Temel sıfatlar ve betimleme",
            "Sıfatlar kişi ve nesnelerin özelliklerini anlatır ve çoğunlukla isimden önce gelir. Be fiilinden sonra da kullanılabilir.",
            "a small room · a friendly person · The room is clean.",
            listOf("The engine is noisy." to "Motor gürültülü.", "She is friendly and helpful." to "O arkadaş canlısı ve yardımsever.", "It is a small, clean room." to "Küçük ve temiz bir oda."),
            listOf("large" to "büyük", "small" to "küçük", "noisy" to "gürültülü", "reliable" to "güvenilir"),
            "The room is very ___.",
            listOf("clean", "cleanly", "cleaning", "cleaner than"), "clean",
            "Sıfatın bir ismi mi yoksa özneyi mi tanımladığına dikkat et."
        )
        "geniş zaman: günlük alışkanlıklar" in t -> LessonContent(
            "Geniş zaman: günlük alışkanlıklar",
            "Geniş zaman alışkanlık ve rutinleri anlatır. He/she/it olumlu cümlede fiil genellikle -s/-es alır; soru ve olumsuzlarda do/does kullanılır.",
            "I work. · She works. · Do you work? · She doesn't work.",
            listOf("I study English every day." to "Her gün İngilizce çalışırım.", "He works at an airport." to "Havalimanında çalışır.", "Does she speak English?" to "İngilizce konuşur mu?"),
            listOf("every day" to "her gün", "usually" to "genellikle", "work" to "çalışmak", "study" to "ders çalışmak"),
            "My brother ___ to school by bus.",
            listOf("go", "goes", "going", "went"), "goes",
            "He/she/it öznesiyle olumlu geniş zaman cümlesinde fiil ekini kontrol et."
        )
        "selamlaşma" in t || "tanışma" in t -> LessonContent("Selamlaşma ve kendini tanıtma", "İlk kez karşılaştığımız kişilerle selamlaşır, adımızı söyler ve karşımızdakinin adını sorarız.", "Hello / My name is… / Nice to meet you.", listOf("Hello, my name is Ece." to "Merhaba, benim adım Ece.", "Nice to meet you." to "Tanıştığıma memnun oldum.", "What is your name?" to "Adın ne?"), listOf("hello" to "merhaba", "good morning" to "günaydın", "goodbye" to "hoşça kal", "name" to "isim"), "My name ___ Ece.", listOf("is", "are", "am", "be"), "is", "Selamlaşma ve kendini tanıtma ifadelerini dinle.")
        "kişi zamirleri" in t || "pronouns" in t -> LessonContent("Kişi zamirleri ve be fiili", "İngilizcede kişi zamirleri özneyi belirtir. I ile am, he/she/it ile is, you/we/they ile are kullanılır.", "I am · You are · He/She/It is", listOf("I am a student." to "Ben öğrenciyim.", "She is my friend." to "O benim arkadaşım.", "They are at home." to "Onlar evde."), listOf("I" to "ben", "you" to "sen / siz", "he" to "o (erkek)", "she" to "o (kadın)"), "They ___ happy.", listOf("are", "is", "am", "be"), "are", "Kişi zamirlerine göre am, is ve are kullanımını dinle.")
        "günlük temel kelimeler" in t -> LessonContent("Günlük temel kelimeler", "Günlük yaşamda sık kullanılan kelimeleri öğrenmek, basit cümleleri anlamayı ve kurmayı kolaylaştırır.", "Kelime + anlam + örnek cümle", listOf("I drink water." to "Su içerim.", "This is my book." to "Bu benim kitabım.", "The food is good." to "Yemek güzel."), listOf("water" to "su", "book" to "kitap", "food" to "yemek", "friend" to "arkadaş"), "I drink ___.", listOf("water", "chair", "school", "window"), "water", "Günlük kelimelerin telaffuzuna ve cümledeki anlamına dikkat et.")
        "basit sorular" in t -> LessonContent("Basit sorular sorma", "What bir şeyi, where bir yeri, who ise bir kişiyi sormak için kullanılır.", "What = ne · Where = nerede · Who = kim", listOf("What is your name?" to "Adın ne?", "Where are you from?" to "Nerelisin?", "Who is she?" to "O kim?"), listOf("what" to "ne", "where" to "nerede", "who" to "kim", "name" to "isim"), "___ is your name?", listOf("What", "Where", "Who", "When"), "What", "Soru kelimelerini ve hangi bilgiyi sorduklarını dinle.")
        "geçmiş zaman: was/were" in t -> LessonContent("Was ve were ile geçmiş", "Was ve were, be fiilinin geçmiş zaman biçimleridir. I/he/she/it ile was; you/we/they ile were kullanılır.", "I was · He was · You were · They were", listOf("I was at home." to "Evdeydim.", "They were happy." to "Mutluydular.", "She was not late." to "Geç kalmamıştı."), listOf("yesterday" to "dün", "was" to "idi", "were" to "idiler / idiniz", "at home" to "evde"), "They ___ at school yesterday.", listOf("were", "was", "are", "be"), "were", "Was ve were yardımcı fiillerinin özneye göre kullanımını dinle.")
        "greetings" in t || "introducing" in t -> LessonContent("Selamlaşma ve kendini tanıtma", "İlk kez karşılaştığımız kişilerle selamlaşır, adımızı söyler ve karşımızdakinin adını sorarız.", "Hello / My name is… / Nice to meet you.", listOf("Hello, my name is Ece." to "Merhaba, benim adım Ece.", "Nice to meet you." to "Tanıştığıma memnun oldum.", "What is your name?" to "Adın ne?"), listOf("hello" to "merhaba", "good morning" to "günaydın", "goodbye" to "hoşça kal", "name" to "isim"), "My name ___ Ece.", listOf("is", "are", "am", "be"), "is", "Videoda selamlaşma ve kendini tanıtma ifadelerini dinle.")
        ("shopping" in t || "alışverişte" in t) -> LessonContent("Alışverişte konuşma", "Fiyat sormak, ürün istemek ve fiyatları karşılaştırmak için bu ifadeleri kullanırız.", "How much is it? / I'd like…", listOf("How much is this shirt?" to "Bu gömlek ne kadar?", "I'd like a bottle of water." to "Bir şişe su istiyorum.", "It's too expensive." to "Çok pahalı."), listOf("price" to "fiyat", "cheap" to "ucuz", "expensive" to "pahalı", "receipt" to "fiş"), "How much ___ it?", listOf("is", "are", "do", "be"), "is", "Videoda fiyat sorma ve kibarca bir ürün isteme kalıplarını dinle.")
        "countries" in t || "nationalities" in t -> LessonContent("Ülkeler ve milliyetler", "Nereli olduğumuzu söylerken be fiilini ve ülke/milliyet adlarını kullanırız.", "Where are you from? — I'm from Türkiye.", listOf("Where are you from?" to "Nerelisin?", "I'm from Türkiye." to "Türkiye'denim.", "I'm Turkish." to "Türküm."), listOf("country" to "ülke", "nationality" to "milliyet", "Turkish" to "Türk", "from" to "-den / -dan"), "They ___ from Spain.", listOf("are", "is", "am", "be"), "are", "Videoda ülkeler ve milliyetlerden söz ederken kullanılan ifadeleri dinle.")
        ("daily routines" in t || "günlük rutinim" in t) -> LessonContent("Günlük rutinler", "Günlük yaptığımız işleri anlatırken geniş zaman kullanırız. He/she/it ile olumlu cümlede fiile genellikle -s eklenir.", "I wake up. / She wakes up.", listOf("I have breakfast at seven." to "Saat yedide kahvaltı yaparım.", "He goes to work by bus." to "İşe otobüsle gider.", "They go to bed early." to "Erken yatarlar."), listOf("wake up" to "uyanmak", "get dressed" to "giyinmek", "have breakfast" to "kahvaltı yapmak", "go to bed" to "yatmaya gitmek"), "She ___ up at seven.", listOf("wakes", "wake", "waking", "woke"), "wakes", "Videoda günlük rutinleri anlatan fiilleri ve he/she ile gelen -s ekini dinle.")

        "advanced expressions" in t -> LessonContent("İleri seviye ifadeler", "Daha doğal ve akıcı İngilizce için kalıp ifadeleri bağlama göre kullan. Bu ifadeler kelime kelime çevrilmek yerine bir bütün olarak öğrenilir.", "To be honest = açıkçası • It depends = duruma bağlı • As far as I know = bildiğim kadarıyla", listOf("To be honest, I disagree." to "Açıkçası, katılmıyorum.", "It depends on the situation." to "Duruma bağlı.", "As far as I know, the meeting is at six." to "Bildiğim kadarıyla toplantı altıda."), listOf("to be honest" to "açıkçası", "it depends" to "duruma bağlı", "as far as I know" to "bildiğim kadarıyla", "in other words" to "başka bir deyişle"), "___, I think we need more time.", listOf("To be honest", "By the way", "At least", "For example"), "To be honest", "İfadelerin anlamını tek tek kelimelerden değil, cümlenin bağlamından çıkar.")
        "counting objects" in t || "counting" in t -> LessonContent("Nesneleri sayma", "Sayıları isimlerle birlikte kullanırız. Sayılabilen tekil isimlerde a/an; çoğul isimlerde çoğunlukla -s kullanılır.", "one book • two books • three apples", listOf("I have two books." to "İki kitabım var.", "There are five chairs." to "Beş sandalye var.", "She has an orange." to "Bir portakalı var."), listOf("one" to "bir", "two" to "iki", "three" to "üç", "How many?" to "kaç tane?"), "There are three ___.", listOf("apples", "apple", "an apple", "a apples"), "apples", "Sayıdan sonra çoğul isim kullanımını ve how many kalıbını hatırla.")
        "family members" in t || "family" in t -> LessonContent("Aile üyeleri", "Aile bireylerini tanıtırken sahiplik bildiren my, your, his ve her kelimelerini kullanırız.", "my mother • his brother • their parents", listOf("This is my sister." to "Bu benim kız kardeşim.", "His father is a doctor." to "Onun babası doktor.", "Their parents live in Bursa." to "Onların anne babası Bursa'da yaşıyor."), listOf("mother" to "anne", "father" to "baba", "sister" to "kız kardeş", "brother" to "erkek kardeş"), "This is ___ brother.", listOf("my", "me", "I", "mine"), "my", "Aile bireyleriyle kullanılan sahiplik sıfatlarına dikkat et.")
        "expressing possession" in t -> LessonContent("Sahiplik bildirme", "Bir şeyin kime ait olduğunu have/has veya iyelik sıfatlarıyla anlatabiliriz.", "I have • she has • my/your/his/her", listOf("I have a blue bag." to "Mavi bir çantam var.", "She has two cats." to "İki kedisi var.", "This is their house." to "Bu onların evi."), listOf("my" to "benim", "your" to "senin", "his" to "onun (erkek)", "their" to "onların"), "He ___ a new phone.", listOf("has", "have", "having", "had"), "has", "He/she/it ile has kullanımını ve iyelik sıfatlarını ayırt et.")
        "food and drinks" in t || "food choices" in t -> LessonContent("Yiyecekler ve içecekler", "Yiyecek sipariş ederken some/any ve kibar istek kalıpları işe yarar.", "I'd like… • some water • any questions?", listOf("I'd like some tea, please." to "Bir çay istiyorum, lütfen.", "We need some bread." to "Biraz ekmeğe ihtiyacımız var.", "Do you have any apples?" to "Hiç elmanız var mı?"), listOf("bread" to "ekmek", "water" to "su", "vegetables" to "sebzeler", "I'd like" to "istiyorum"), "I'd like ___ water, please.", listOf("some", "many", "a", "an"), "some", "Yiyecek siparişi ve some/any kullanım örneklerini takip et.")
        "can / can't" in t -> LessonContent("Yetenekler: can / can't", "Can bir yeteneği veya mümkün olan bir şeyi anlatır. Ardından fiilin yalın hâli gelir.", "Özne + can/can't + fiilin yalın hâli", listOf("I can swim." to "Yüzebilirim.", "She can't drive." to "Araba kullanamaz.", "Can you help me?" to "Bana yardım edebilir misin?"), listOf("can" to "-ebilmek", "swim" to "yüzmek", "drive" to "araba kullanmak", "help" to "yardım etmek"), "He can ___ English.", listOf("speak", "speaks", "speaking", "spoke"), "speak", "Can sonrasında fiilin yalın hâlde geldiğine dikkat et.")
        "there is / there are" in t -> LessonContent("Bir şeyin var olduğunu söyleme", "Tekil isimlerle there is, çoğul isimlerle there are kullanılır.", "There is + tekil • There are + çoğul", listOf("There is a park near my home." to "Evimin yakınında bir park var.", "There are two windows." to "İki pencere var.", "Is there a bank nearby?" to "Yakınlarda banka var mı?"), listOf("there is" to "var (tekil)", "there are" to "var (çoğul)", "near" to "yakın", "between" to "arasında"), "There ___ three books on the table.", listOf("are", "is", "am", "be"), "are", "Tekil ve çoğul isimleri ayırt ederek there is/are seç.")
        "asking for directions" in t || "places in town" in t -> LessonContent("Şehirde yerler ve yön sorma", "Bir yere nasıl gidileceğini sorarken kibar soru kalıpları ve yön ifadeleri kullanılır.", "How can I get to…? • Turn left/right • Go straight", listOf("Excuse me, where is the station?" to "Affedersiniz, istasyon nerede?", "Go straight and turn left." to "Dümdüz git ve sola dön.", "It's next to the bank." to "Bankanın yanında."), listOf("turn left" to "sola dön", "turn right" to "sağa dön", "go straight" to "dümdüz git", "next to" to "yanında"), "Turn ___ at the traffic lights.", listOf("left", "breakfast", "expensive", "yesterday"), "left", "Yön tariflerinde kullanılan ifadeleri ve yer belirten kalıpları dinle.")
        "transport" in t || "travel" in t -> LessonContent("Ulaşım ve seyahat", "Ulaşım türlerini ve yolculukla ilgili temel ifadeleri öğren. By bus/train/car ulaşım aracını belirtir.", "go by bus • catch a train • book a ticket", listOf("I go to school by bus." to "Okula otobüsle giderim.", "We need to book a ticket." to "Bilet ayırtmamız gerekiyor.", "The train is late." to "Tren gecikti."), listOf("ticket" to "bilet", "platform" to "peron", "journey" to "yolculuk", "late" to "geç"), "We travel ___ train.", listOf("by", "on", "at", "with"), "by", "Ulaşım aracı belirtirken by bus/by train kalıplarına dikkat et.")
        "conditionals" in t || "koşul cümleleri" in t -> LessonContent("Koşul cümleleri", "First conditional gelecekte gerçekleşmesi mümkün durumları, second conditional ise varsayımsal durumları anlatır. If-clause koşulu, ana cümle sonucu verir.", "First: If + present simple, will + V1. Second: If + past simple, would + V1. If cümlesinde genellikle will kullanmayız.", listOf("If it rains, we will stay home." to "Yağmur yağarsa evde kalacağız.", "If I had more time, I would learn Italian." to "Daha çok zamanım olsa İtalyanca öğrenirdim.", "If she studied, she would pass." to "Çalışsaydı geçerdi."), listOf("condition" to "koşul", "result" to "sonuç", "unless" to "-medikçe", "would" to "-erdi / -ardı"), "If I ___ enough money, I would buy a laptop.", listOf("had", "have", "will have", "has"), "had", "First ve second conditional arasındaki anlam farkına dikkat et.")
        "relative clauses" in t || "who, which, that" in t -> LessonContent("Relative clauses: isimleri ayrıntılı tanımlama", "Relative clause bir kişi veya nesne hakkında ek bilgi verir. Who kişiler, which nesneler; that ise birçok tanımlayıcı cümlecikte kullanılabilir.", "person + who ... · thing + which/that ... · place + where ...", listOf("The woman who called is my teacher." to "Arayan kadın öğretmenim.", "This is the book that I bought." to "Bu, satın aldığım kitap.", "That is the café where we met." to "Burası tanıştığımız kafe."), listOf("who" to "kişi için", "which" to "nesne için", "whose" to "kimin / -in", "where" to "yer için"), "The student ___ won the prize is my friend.", listOf("who", "where", "when", "whose"), "who", "Bağlayıcıyı tanımlanan ismin kişi, nesne ya da yer olmasına göre seç.")
        "phrasal verbs" in t -> LessonContent("Phrasal verbs: fiil öbekleri", "Phrasal verb, fiilin up, off, on veya out gibi bir parçacıkla birleşmesidir. Anlamı bağlam içinde öğrenmek gerekir.", "give up = vazgeçmek · find out = öğrenmek · turn on = açmak · look after = ilgilenmek", listOf("Please turn off the lights." to "Lütfen ışıkları kapat.", "I found out the answer." to "Cevabı öğrendim.", "She looks after her brother." to "Erkek kardeşiyle ilgileniyor."), listOf("give up" to "vazgeçmek", "find out" to "öğrenmek", "look after" to "ilgilenmek", "put off" to "ertelemek"), "We had to ___ the meeting until Friday.", listOf("put off", "look after", "turn on", "give up"), "put off", "Fiil öbeğinin anlamını cümlenin tamamından çıkar.")
        "email writing" in t || "e-posta yazımı" in t -> LessonContent("Profesyonel e-posta yazımı", "İş e-postasında amaç ilk bölümde açık olmalı. Uygun hitap kullan, isteği kısa ve nazik ifade et ve profesyonel kapanış yap.", "Subject → greeting → purpose → details/request → closing", listOf("I am writing to ask about the schedule." to "Program hakkında bilgi almak için yazıyorum.", "Could you please send the updated file?" to "Güncellenmiş dosyayı gönderebilir misiniz?", "Thank you for your time." to "Zaman ayırdığınız için teşekkür ederim."), listOf("subject" to "konu", "attached" to "ekte", "regarding" to "hakkında", "kind regards" to "saygılarımla"), "Which is the most polite request?", listOf("Send it now.", "Could you please send it?", "You must send it.", "Send me that."), "Could you please send it?", "Kibar rica kalıplarını ve e-posta yapısının sırasını hatırla.")
        "academic writing" in t || "akademik paragraf" in t -> LessonContent("Akademik paragraf ve argüman", "Güçlü bir akademik paragraf tek ana fikir etrafında kurulur. Topic sentence iddiayı belirtir; kanıt veya örnek iddiayı destekler; açıklama kanıtın önemini gösterir.", "Topic sentence → evidence/example → explanation → concluding link", listOf("Regular reading improves vocabulary because it exposes learners to words in context." to "Düzenli okuma, kelime bilgisini bağlam içinde karşılaşma sayesinde geliştirir.", "For example, readers meet common words in different texts." to "Örneğin okurlar yaygın kelimelerle farklı metinlerde karşılaşır."), listOf("evidence" to "kanıt", "claim" to "iddia", "whereas" to "oysa", "therefore" to "bu nedenle"), "A topic sentence mainly introduces the paragraph’s ___.", listOf("main idea", "reference list", "title page", "quotation marks"), "main idea", "İddia ile kanıtı ayırt et; bağlaçların mantıksal ilişki kurduğunu fark et.")
        "critical reading" in t || "eleştirel okuma" in t -> LessonContent("Eleştirel okuma ve çıkarım", "Eleştirel okurken yazarın iddiasını, kanıtını, varsayımlarını ve karşı görüşleri değerlendir. Çıkarım, metindeki ipuçlarından mantıklı sonuç üretmektir.", "Claim = iddia · evidence = kanıt · assumption = varsayım · inference = çıkarım", listOf("The writer uses survey results to support the claim." to "Yazar iddiayı desteklemek için anket sonuçları kullanıyor.", "The text suggests a change but does not prove its cause." to "Metin bir değişime işaret ediyor ancak nedenini kanıtlamıyor."), listOf("claim" to "iddia", "evidence" to "kanıt", "bias" to "yanlılık", "imply" to "ima etmek"), "A statistic is most useful when it is relevant and ___.", listOf("reliable", "colourful", "long", "emotional"), "reliable", "Yazarın iddiası ile sunduğu kanıtın aynı şey olmadığını hatırla.")
        "uçuş kumandaları" in t -> LessonContent("Uçuş kumandaları: aileron, elevator, rudder", "Ailerons roll hareketini, elevator pitch hareketini, rudder ise yaw hareketini kontrol etmeye yardımcı olur. Bu bölüm terim ve temel işlev ilişkisini öğretir.", "aileron = roll · elevator = pitch · rudder = yaw", listOf("The ailerons control roll." to "Aileronlar yatış hareketini kontrol eder.", "The elevator controls pitch." to "Elevator yunuslama hareketini kontrol eder.", "The rudder controls yaw." to "Rudder sapma hareketini kontrol eder."), listOf("roll" to "yatış", "pitch" to "yunuslama", "yaw" to "sapma", "flight control" to "uçuş kumandası"), "Which control surface is associated with yaw?", listOf("Rudder", "Aileron", "Elevator", "Flap track"), "Rudder", "Bu ders terim bilgisidir; gerçek uçak işlemleri için onaylı teknik doküman gereklidir.")
        "bakım aletleri" in t || "ekipman" in t -> LessonContent("Bakım aletleri ve ekipman", "Bakım metinlerinde alet adları, eylem fiilleriyle birlikte geçer. Bu ders kelime anlamını tanımaya odaklanır; gerçek işte doğru alet ve işlem onaylı prosedürden belirlenir.", "wrench/spanner = anahtar · screwdriver = tornavida · pliers = pense · torque wrench = tork anahtarı", listOf("Use the specified tool." to "Belirtilen aleti kullan.", "Check the tool identification." to "Alet tanımlamasını kontrol et.", "Return tools to the designated location." to "Aletleri belirlenen konuma geri koy."), listOf("tool" to "alet", "equipment" to "ekipman", "specified" to "belirtilen", "inspection" to "kontrol"), "A torque wrench is associated with measuring or applying ___.", listOf("torque", "temperature", "colour", "distance only"), "torque", "Alet adlarını öğrenirken bakım dokümanındaki tam ifadeyi de oku.")
        "uçak bakımına giriş" in t || "emniyet dili" in t -> LessonContent("Uçak bakımında emniyet dili", "Teknik dokümanlarda WARNING, CAUTION ve NOTE farklı türde bilgi verir. Bu ders İngilizce terimleri tanımaya yöneliktir ve onaylı bakım talimatının yerine geçmez.", "WARNING = tehlike uyarısı · CAUTION = dikkat uyarısı · NOTE = ek bilgi", listOf("Read the warning before proceeding." to "Devam etmeden önce uyarıyı oku.", "Refer to the approved maintenance manual." to "Onaylı bakım el kitabına başvur.", "Do not proceed until the instruction is understood." to "Talimat anlaşılana kadar devam etme."), listOf("warning" to "uyarı", "caution" to "dikkat uyarısı", "approved" to "onaylı", "procedure" to "prosedür"), "Which word introduces a hazard warning?", listOf("WARNING", "NOTE", "INDEX", "CONTENTS"), "WARNING", "Gerçek bakımda yalnızca onaylı doküman ve yetkili prosedür izlenir.")
        "kontrol, bulgu" in t || "hasar ifadeleri" in t -> LessonContent("Kontrol, bulgu ve hasar ifadeleri", "Inspection metinleri gözlemlenen durumu tarafsız biçimde ifade eder. Crack çatlak, corrosion korozyon, damage hasar demektir. Rapor, gözlemle varsayımı karıştırmamalıdır.", "inspect = incelemek · finding = bulgu · crack = çatlak · corrosion = korozyon · damage = hasar", listOf("The inspection report records a visible crack." to "Kontrol raporu görünür bir çatlağı kaydeder.", "Describe the condition clearly." to "Durumu açıkça tarif et."), listOf("inspect" to "incelemek", "finding" to "bulgu", "crack" to "çatlak", "corrosion" to "korozyon"), "Which term means corrosion?", listOf("Corrosion", "Connector", "Pressure", "Rudder"), "Corrosion", "Gözlenen olguyu açık ve tarafsız anlatan İngilizce kalıplara odaklan.")

        "kısa diyalogları anlama" in t -> LessonContent("Kısa diyalogları anlama", "Kısa konuşmalarda önce konuşmanın amacını, ardından kişi, yer ve saat gibi ana bilgileri yakala. Her kelimeyi anlamasan da bağlam ipucu verir.", "Greeting → question → answer → closing", listOf("A: Excuse me, what time is it? B: It is half past three." to "A: Affedersiniz, saat kaç? B: Üç buçuk.", "A: Is this seat free? B: Yes, go ahead." to "A: Bu koltuk boş mu? B: Evet, buyurun.", "A: See you tomorrow. B: See you." to "A: Yarın görüşürüz. B: Görüşürüz."), listOf("excuse me" to "affedersiniz", "go ahead" to "buyurun", "see you" to "görüşürüz"), "What information should you listen for first?", listOf("Main purpose", "Every grammar rule", "The longest word", "The speaker's accent only"), "Main purpose", "Önce konuşmanın amacını belirle, sonra önemli ayrıntıları seç.")
        "fikir belirtme" in t -> LessonContent("Fikir belirtme", "Bir görüş sunarken fikrini açıkça belirt, ardından kısa bir gerekçe veya örnek ekle. Katılmadığında nazik bir ifade kullan.", "I think ... because ... · In my opinion ... · I see your point, but ...", listOf("I think public transport is useful because it reduces traffic." to "Toplu taşımanın yararlı olduğunu düşünüyorum çünkü trafiği azaltır.", "In my opinion, practice is more important than memorising lists." to "Bence pratik, listeleri ezberlemekten daha önemlidir.", "I see your point, but I have a different view." to "Ne demek istediğini anlıyorum ama farklı düşünüyorum."), listOf("opinion" to "görüş", "reason" to "gerekçe", "agree" to "katılmak", "however" to "ancak"), "Which phrase introduces an opinion?", listOf("In my opinion", "As soon as", "By the time", "For instance only"), "In my opinion", "Görüşünü bir gerekçe ve mümkünse somut bir örnekle destekle.")
        "görüşleri karşılaştırma" in t -> LessonContent("Görüşleri karşılaştırma", "Farklı görüşleri karşılaştırırken ortak noktaları ve ayrılıkları ayır. Whereas ve while karşıtlık; similarly ve both benzerlik ifade eder.", "whereas/while = oysa · similarly = benzer şekilde · both ... and ...", listOf("Some people prefer remote work, whereas others value the office." to "Bazıları uzaktan çalışmayı tercih ederken diğerleri ofise önem verir.", "Both plans reduce costs, but the second is easier to implement." to "İki plan da maliyeti düşürür ancak ikincisini uygulamak daha kolaydır."), listOf("whereas" to "oysa", "similarly" to "benzer şekilde", "in contrast" to "buna karşılık", "both" to "her ikisi"), "Which linker signals contrast?", listOf("whereas", "similarly", "also", "likewise"), "whereas", "Karşılaştırmada iki görüşü de doğru ve dengeli biçimde özetle.")
        "aile ve insanları tanımlama" in t -> LessonContent("Aile ve insanları tanımlama", "Aile üyelerini ve kişilik özelliklerini anlatırken my, his, her gibi iyelik sıfatlarını ve be fiilini kullan. Görünüş ve kişilik sıfatlarını ayırt et.", "This is my ... · He is ... · She has ...", listOf("My sister is friendly and patient." to "Kız kardeşim arkadaş canlısı ve sabırlıdır.", "My uncle has short hair." to "Amcamın kısa saçları var.", "Their parents are helpful." to "Onların ebeveynleri yardımseverdir."), listOf("parents" to "ebeveynler", "patient" to "sabırlı", "friendly" to "arkadaş canlısı", "helpful" to "yardımsever"), "My brother ___ very kind.", listOf("is", "are", "am", "be"), "is", "Kişiyi tanımlarken özneye uygun be fiilini ve doğru iyelik sözcüğünü seç.")
        "geçmiş zaman: düzenli ve düzensiz fiiller" in t -> LessonContent("Geçmiş zaman: düzenli ve düzensiz fiiller", "Geçmişte tamamlanan olaylarda düzenli fiiller genellikle -ed alır; düzensiz fiillerin biçimi değişebilir. Olumsuz ve sorularda did kullanıldığında ana fiil yalın hâle döner.", "positive: V2 · negative: didn't + V1 · question: Did + subject + V1?", listOf("We watched a film last night." to "Dün gece film izledik.", "She went to work early." to "İşe erken gitti.", "Did you see the message?" to "Mesajı gördün mü?"), listOf("visited" to "ziyaret etti", "went" to "gitti", "bought" to "satın aldı", "didn't" to "yapmadı"), "They didn't ___ the bus.", listOf("miss", "missed", "missing", "misses"), "miss", "Did/didn't kullanıldığında fiilin ikinci hâlini tekrar kullanma.")
        "yemek siparişi ve miktarlar" in t -> LessonContent("Yemek siparişi ve miktarlar", "Restoranda kibarca sipariş verirken I'd like veya Could I have ...? kullan. Miktar belirtirken a glass of, a piece of, some ve any kalıplarını öğren.", "I'd like ... · Could I have ...? · a bottle of · a piece of", listOf("Could I have a glass of water, please?" to "Bir bardak su alabilir miyim lütfen?", "I'd like the chicken soup." to "Tavuk çorbası istiyorum.", "Can we have the bill, please?" to "Hesabı alabilir miyiz lütfen?"), listOf("menu" to "menü", "bill" to "hesap", "portion" to "porsiyon", "ingredients" to "malzemeler"), "Could I have ___ apple juice, please?", listOf("some", "many", "a few", "much of"), "some", "Sipariş verirken kibar istek kalıbı ve uygun miktar ifadesini birlikte kullan.")
        "edilgen yapı" in t -> LessonContent("Edilgen yapı", "Edilgen yapı, işi yapan kişiden çok eylemden etkilenen şey önemli olduğunda kullanılır. Zamanı be fiili, ana fiilin üçüncü hâli gösterir.", "object + be + past participle (V3)", listOf("The report is checked every morning." to "Rapor her sabah kontrol edilir.", "The parts were delivered yesterday." to "Parçalar dün teslim edildi.", "The issue has been resolved." to "Sorun çözüldü."), listOf("performed" to "gerçekleştirildi", "checked" to "kontrol edildi", "delivered" to "teslim edildi", "resolved" to "çözüldü"), "The documents ___ signed yesterday.", listOf("were", "was", "is", "has"), "were", "Özne tekil/çoğul ve zaman bilgisine göre be fiilini seç; ardından V3 kullan.")
        "dolaylı anlatım" in t -> LessonContent("Dolaylı anlatım", "Birinin söylediğini aktarırken zamirler ve zaman ifadeleri değişebilir. Geçmişteki aktarım fiilinden sonra present simple çoğunlukla past simple'a kayar.", "He said (that) ... · She told me (that) ... · He asked if ...", listOf("“I am busy.” → She said she was busy." to "“Meşgulüm.” → Meşgul olduğunu söyledi.", "“We will call.” → They said they would call." to "“Arayacağız.” → Arayacaklarını söylediler.", "“Are you ready?” → He asked if I was ready." to "“Hazır mısın?” → Hazır olup olmadığımı sordu."), listOf("said" to "söyledi", "told" to "anlattı / söyledi", "asked" to "sordu", "whether" to "olup olmadığını"), "“I can help.” → He said he ___ help.", listOf("could", "can", "will", "has"), "could", "Aktarılan cümlenin zamanını, zamirini ve soru olup olmadığını kontrol et.")
        "dinlemede ayrıntı yakalama" in t -> LessonContent("Dinlemede ayrıntı yakalama", "Bir konuşmada ayrıntı soruları genellikle isim, sayı, tarih, yer veya neden hakkında olur. İlk dinlemede ana fikri, ikinci dinlemede istenen ayrıntıları yakala.", "Who? · Where? · When? · How much? · Why?", listOf("The meeting has been moved to Thursday at 10 a.m." to "Toplantı perşembe saat 10.00'a alındı.", "Please bring two copies of the report." to "Lütfen raporun iki kopyasını getir.", "The train leaves from platform six." to "Tren altıncı perondan kalkıyor."), listOf("moved" to "ertelendi / yeri değişti", "copy" to "kopya", "platform" to "peron", "at 10 a.m." to "sabah 10'da"), "The train leaves from platform ___.", listOf("six", "sixteen", "seven", "five"), "six", "Dinlerken anahtar kelimeleri not et; sayıları ve saatleri bağlamla doğrula.")
        "sorun açıklama ve çözüm önerme" in t -> LessonContent("Sorun açıklama ve çözüm önerme", "Bir sorunu açıklarken ne olduğunu, etkisini ve ne zaman başladığını belirt. Çözüm önerirken could, might veya suggest kullanarak seçenek sun.", "The problem is ... · It affects ... · We could ... · I suggest ...", listOf("The printer keeps stopping, so we cannot finish the report." to "Yazıcı sürekli duruyor, bu yüzden raporu bitiremiyoruz.", "We could restart it and check the connection." to "Yeniden başlatıp bağlantıyı kontrol edebiliriz.", "I suggest contacting technical support." to "Teknik destekle iletişime geçmeyi öneriyorum."), listOf("issue" to "sorun", "affect" to "etkilemek", "suggest" to "önermek", "solution" to "çözüm"), "Which phrase introduces a suggestion?", listOf("I suggest ...", "Yesterday at ...", "It belongs to ...", "I have been ..."), "I suggest ...", "Sorunu ve önerilen çözümü ayrı, açık cümlelerle ifade et.")
        "iş yerinde iletişim" in t -> LessonContent("İş yerinde iletişim", "İş ortamında nazik rica, açıklama isteme ve bilgi doğrulama kalıpları kullanılır. Kısa, net ve saygılı konuşmak yanlış anlaşılmayı azaltır.", "Could you clarify ...? · Let me confirm ... · I will follow up.", listOf("Could you clarify the deadline?" to "Son teslim tarihini açıklayabilir misiniz?", "Let me confirm the part number." to "Parça numarasını doğrulayayım.", "I will follow up by email." to "E-posta ile tekrar bilgi vereceğim."), listOf("deadline" to "son tarih", "clarify" to "açıklığa kavuşturmak", "confirm" to "doğrulamak", "follow up" to "takip etmek"), "Which is the most professional request?", listOf("Could you clarify the deadline?", "Tell me now.", "You got it wrong.", "Whatever."), "Could you clarify the deadline?", "Rica, doğrulama ve takip ifadelerini uygun iş bağlamında kullan.")
        "olay anlatımı ve bağlaçlar" in t -> LessonContent("Olay anlatımı ve bağlaçlar", "Bir olayı sıralı anlatmak için first, then, after that ve finally kullan. Because neden, so sonuç, although ise karşıtlık bağlar.", "first → then → after that → finally · because / so / although", listOf("First, we checked the schedule. Then, we called the supplier." to "Önce programı kontrol ettik. Sonra tedarikçiyi aradık.", "The flight was delayed because of bad weather." to "Uçuş kötü hava nedeniyle gecikti.", "Although it was late, we completed the task." to "Geç olmasına rağmen görevi tamamladık."), listOf("first" to "önce", "afterwards" to "sonrasında", "because" to "çünkü", "although" to "-e rağmen"), "We stayed inside ___ it was raining.", listOf("because", "although", "finally", "unless"), "because", "Bağlacı seçerken cümlelerin neden, sonuç, sıra veya karşıtlık ilişkisini belirle.")
        "perfect continuous" in t -> LessonContent("Perfect continuous yapıları", "Perfect continuous yapıları bir eylemin belirli bir zamana kadar ne kadar süredir devam ettiğini vurgular. Have/has/had been + V-ing yapısına dikkat et.", "present: have/has been + V-ing · past: had been + V-ing", listOf("I have been studying for two hours." to "İki saattir ders çalışıyorum.", "She had been waiting before the bus arrived." to "Otobüs gelmeden önce bekliyordu.", "How long have you been working here?" to "Ne zamandır burada çalışıyorsun?"), listOf("for" to "boyunca", "since" to "-den beri", "duration" to "süre", "ongoing" to "devam eden"), "They have been ___ since morning.", listOf("working", "work", "worked", "works"), "working", "Have/has/had been sonrasında fiilin -ing biçimini kullan.")
        "vurgu ve inversion" in t -> LessonContent("Vurgu ve inversion", "Olumsuz veya sınırlayıcı ifadeler cümle başına geldiğinde resmî İngilizcede yardımcı fiil öznenin önüne geçebilir. Bu yapı vurgu sağlar ve dikkatli kullanılmalıdır.", "Never/Rarely/Not only + auxiliary + subject + verb", listOf("Never have I seen such a clear result." to "Böylesine açık bir sonucu hiç görmedim.", "Not only did she explain the issue, but she also proposed a solution." to "Sorunu açıklamakla kalmadı, çözüm de önerdi.", "Rarely do we receive such detailed feedback." to "Böylesine ayrıntılı geri bildirimi nadiren alırız."), listOf("rarely" to "nadiren", "seldom" to "seyrek", "not only" to "yalnızca ... değil", "inversion" to "devrik yapı"), "Never ___ I heard that expression before.", listOf("have", "did", "am", "was been"), "have", "Başta kullanılan olumsuz ifadenin gerektirdiği yardımcı fiili seç.")
        "deyimler ve dil düzeyi" in t -> LessonContent("Deyimler ve dil düzeyi", "Deyimlerin anlamı çoğu zaman kelimelerin tek tek anlamından çıkarılamaz. Resmî, nötr ve samimi ifadeyi hedef kitleye göre seç.", "formal: obtain · neutral: get · informal idiom: a piece of cake", listOf("The task was a piece of cake." to "Görev çok kolaydı.", "Could you provide further information?" to "Daha fazla bilgi sağlayabilir misiniz?", "Thanks for getting back to me." to "Bana geri döndüğün için teşekkürler."), listOf("idiom" to "deyim", "formal" to "resmî", "casual" to "samimi / gündelik", "context" to "bağlam"), "Which expression is most formal?", listOf("Could you provide further information?", "What's up?", "No big deal.", "Cool!"), "Could you provide further information?", "İfadenin anlamını ve resmiyet düzeyini muhataba göre değerlendir.")
        "toplantılarda fikir savunma" in t -> LessonContent("Toplantılarda fikir savunma", "Toplantıda iddianı açıkla, kanıtla destekle ve itirazları saygılı biçimde ele al. Katılımcıların sözünü kesmeden açıklama iste.", "My main point is ... · The evidence suggests ... · I understand the concern, however ...", listOf("My main point is that the current process takes too long." to "Temel görüşüm mevcut sürecin çok uzun sürdüğü.", "The data suggests that option B is more reliable." to "Veriler B seçeneğinin daha güvenilir olduğunu gösteriyor.", "I understand the concern; however, the risk can be reduced." to "Endişeyi anlıyorum; ancak risk azaltılabilir."), listOf("evidence" to "kanıt", "concern" to "endişe", "trade-off" to "ödünleşim", "reliable" to "güvenilir"), "A strong argument should be supported by ___.", listOf("evidence", "volume", "interruptions", "guesswork"), "evidence", "Fikri savunurken kanıtı, varsayımı ve karşı görüşü birbirinden ayır.")
        "uzun konuşmalarda ana fikir" in t -> LessonContent("Uzun konuşmalarda ana fikir", "Uzun konuşmalarda her ayrıntıyı yazmak yerine konu, konuşmacının amacı ve tekrar edilen anahtar fikirleri takip et. İşaretleyici ifadeler konu değişimini gösterir.", "The main point is ... · To summarise ... · The key issue is ...", listOf("The speaker first describes the problem, then compares two solutions." to "Konuşmacı önce sorunu açıklıyor, sonra iki çözümü karşılaştırıyor.", "The key issue is the cost of implementation." to "Temel mesele uygulama maliyetidir."), listOf("main point" to "ana fikir", "in summary" to "özetle", "in contrast" to "buna karşılık", "issue" to "mesele"), "When listening to a long talk, focus first on the ___.", listOf("main idea", "every minor detail", "speaker's pauses only", "last word"), "main idea", "Başlık, tekrarlar ve geçiş ifadeleri ana fikri bulmana yardım eder.")
        "veri ve sonuçları raporlama" in t -> LessonContent("Veri ve sonuçları raporlama", "Veri raporlarında ölçümü, zaman aralığını ve karşılaştırma temelini açıkça belirt. Increase, decrease, remain stable gibi fiilleri doğru kullan.", "The figure increased from X to Y · remained stable · compared with", listOf("Sales increased by 12% compared with last month." to "Satışlar geçen aya göre yüzde 12 arttı.", "The rate remained stable during the second quarter." to "Oran ikinci çeyrekte sabit kaldı.", "The chart shows a gradual decline." to "Grafik kademeli bir düşüş gösteriyor."), listOf("figure" to "rakam / değer", "increase" to "artış / artmak", "decline" to "düşüş", "compared with" to "ile karşılaştırıldığında"), "The number fell from 80 to 60. This is a ___.", listOf("decline", "rise", "plateau only", "prediction"), "decline", "Yüzde değişimi ile başlangıç ve bitiş değerlerini birbirine karıştırma.")
        "müzakere ve uzlaşma" in t -> LessonContent("Müzakere ve uzlaşma", "Müzakerede ihtiyaçları açıkça belirt, seçenekleri karşılaştır ve ortak noktaya yönel. Uzlaşma, her tarafın tüm taleplerini kabul etmesi anlamına gelmez.", "Would you consider ...? · We could meet halfway. · That works for both sides.", listOf("Could we extend the deadline by two days?" to "Son tarihi iki gün uzatabilir miyiz?", "We could meet halfway on the delivery date." to "Teslim tarihinde orta yolu bulabiliriz.", "That proposal works for both sides." to "Bu öneri iki taraf için de uygun."), listOf("negotiate" to "müzakere etmek", "compromise" to "uzlaşmak / ödün", "proposal" to "öneri", "mutual" to "karşılıklı"), "A compromise aims to find a solution acceptable to ___.", listOf("both sides", "one person only", "nobody", "the audience only"), "both sides", "İhtiyaçları ve sınırları netleştirerek karşılıklı kabul edilebilir seçenek ara.")
        "anlam incelikleri" in t || "register" in t -> LessonContent("Anlam incelikleri ve register", "Yakın anlamlı sözcükler duygu, kesinlik ve resmiyet bakımından farklılaşabilir. Register, dili bağlama ve muhataba göre seçme biçimidir.", "ask → inquire (more formal) · help → assist (more formal) · maybe → possibly", listOf("Could you assist us with the review?" to "İnceleme konusunda bize yardımcı olabilir misiniz?", "The result is likely, but not certain." to "Sonuç olası ancak kesin değil.", "We need to address the issue promptly." to "Sorunu hızla ele almamız gerekiyor."), listOf("register" to "dil düzeyi", "precise" to "kesin / açık", "subtle" to "ince", "tentative" to "kesin olmayan"), "Which word is generally more formal than 'ask'?", listOf("inquire", "grab", "chat", "hang out"), "inquire", "Sözcük seçerken anlamın yanında resmiyet ve kesinlik derecesini de kontrol et.")
        "ileri düzey cümle yapıları" in t -> LessonContent("İleri düzey cümle yapıları", "İleri düzey cümlelerde yan cümleleri, bağlaçları ve noktalama işaretlerini anlam ilişkisine göre kullan. Uzun cümle kurmak tek başına daha iyi yazmak demek değildir.", "Although + clause, main clause · Having reviewed ..., we ...", listOf("Although the sample was small, the results were consistent." to "Örneklem küçük olmasına rağmen sonuçlar tutarlıydı.", "Having reviewed the evidence, the team revised its conclusion." to "Kanıtları inceleyen ekip sonucunu gözden geçirdi."), listOf("although" to "-e rağmen", "whereas" to "oysa", "provided that" to "şartıyla", "having reviewed" to "inceledikten sonra"), "___ the report was brief, it covered the key risks.", listOf("Although", "Despite that", "Because of", "Unless of"), "Although", "Yan cümle ve ana cümle arasındaki mantıksal ilişkiyi koru.")
        "sunum ve ikna edici konuşma" in t -> LessonContent("Sunum ve ikna edici konuşma", "Etkili sunumda amaç ve ana mesaj başta belirtilir, iddialar kanıtlarla desteklenir ve sonunda açık bir sonuç veya eylem çağrısı yapılır.", "Today I will focus on ... · The evidence indicates ... · To conclude ...", listOf("Today I will focus on three practical improvements." to "Bugün üç pratik iyileştirmeye odaklanacağım.", "This example demonstrates the benefit clearly." to "Bu örnek faydayı açıkça gösteriyor.", "To conclude, I recommend a phased approach." to "Sonuç olarak aşamalı bir yaklaşım öneriyorum."), listOf("agenda" to "gündem", "key takeaway" to "ana çıkarım", "evidence" to "kanıt", "recommend" to "önermek"), "A presentation conclusion should ___.", listOf("summarise the message", "introduce many unrelated topics", "hide the recommendation", "repeat every slide word for word"), "summarise the message", "Her bölümün ana mesajını tek cümleyle ifade edebilecek şekilde hazırlan.")
        "birden fazla kaynaktan sentez" in t -> LessonContent("Birden fazla kaynaktan sentez", "Sentez, kaynakları sırayla özetlemekten fazlasıdır: ortak temaları, farklılıkları ve kanıt kalitesini ilişkilendirerek kendi sonucunu kurarsın.", "Source A suggests ... · In contrast, Source B ... · Taken together ...", listOf("Both studies identify cost as a barrier, but they use different samples." to "İki çalışma da maliyeti engel olarak tanımlar ancak farklı örneklemler kullanır.", "Taken together, the findings support further testing." to "Bulgular birlikte değerlendirildiğinde daha fazla test destekleniyor."), listOf("synthesise" to "sentezlemek", "source" to "kaynak", "sample" to "örneklem", "limitation" to "sınırlılık"), "A synthesis should connect sources by their ___.", listOf("relationships and differences", "publication colour", "page count only", "alphabetical order"), "relationships and differences", "Kaynakların iddialarını ayır ve sentezlediğin sonucu kanıtla ilişkilendir.")
        "resmî rapor" in t || "öneri yazımı" in t -> LessonContent("Resmî rapor ve öneri yazımı", "Resmî rapor; amaç, kapsam, bulgular, değerlendirme ve önerileri açık başlıklarla sunar. Her öneri belirli bir bulguya ve uygulanabilir bir adıma dayanmalıdır.", "Purpose → findings → analysis → recommendation → next steps", listOf("The inspection identified three recurring issues." to "İnceleme tekrarlayan üç sorun belirledi.", "We recommend updating the procedure and reviewing it next month." to "Prosedürün güncellenmesini ve gelecek ay gözden geçirilmesini öneriyoruz."), listOf("finding" to "bulgu", "recommendation" to "öneri", "scope" to "kapsam", "implementation" to "uygulama"), "A recommendation should be linked to a ___.", listOf("finding", "greeting only", "personal guess", "unrelated anecdote"), "finding", "Raporun her önerisini bulgu, sorumlu kişi veya izlenecek adımla ilişkilendir.")
        "ima, ton ve örtük anlam" in t -> LessonContent("İma, ton ve örtük anlam", "Bir cümlenin gerçek amacı yalnızca kelimelerinden anlaşılmayabilir. Ton, bağlam ve dolaylı ifadeler konuşmacının nezaketini, şüphesini veya eleştirisini gösterebilir.", "That may be difficult. · I wonder if ... · With respect, ...", listOf("“That's an interesting choice” may express polite doubt depending on tone." to "“İlginç bir seçim” tonuna göre nazik bir şüphe ifade edebilir.", "I wonder if we should review the figures." to "Rakamları gözden geçirsek mi diye düşünüyorum."), listOf("imply" to "ima etmek", "tone" to "ton", "irony" to "ironi", "context" to "bağlam"), "To interpret implied meaning, consider words, tone and ___.", listOf("context", "font size only", "word length", "alphabetical order"), "context", "Örtük anlam çıkarırken tek bir ifadeye değil, konuşmanın tamamına bak.")
        "ileri düzey tartışma" in t -> LessonContent("İleri düzey tartışma", "Tartışmada iddiayı, dayanağı ve sınırlarını belirt. Karşı görüşü çarpıtmadan özetle ve gerektiğinde görüşünü yeni kanıta göre güncelle.", "I would distinguish between ... · The strongest counterargument is ...", listOf("The strongest counterargument concerns the cost of implementation." to "En güçlü karşı argüman uygulama maliyetiyle ilgilidir.", "This conclusion holds only if the data is representative." to "Bu sonuç yalnızca veriler temsil ediciyse geçerlidir."), listOf("counterargument" to "karşı argüman", "qualify" to "sınırlamak / nitelemek", "assumption" to "varsayım", "representative" to "temsil edici"), "A fair discussion should represent opposing views ___.", listOf("accurately", "as weakly as possible", "without evidence", "sarcastically"), "accurately", "Güçlü tartışma, karşı görüşü doğru sunmayı ve iddianın sınırlarını belirtmeyi gerektirir.")
        "metin düzenleme" in t || "üslup" in t -> LessonContent("Metin düzenleme ve üslup", "Düzenleme sırasında önce yapı ve anlamı, sonra cümle açıklığını, son olarak dil bilgisi ve noktalama ayrıntılarını kontrol et. Gereksiz tekrarları çıkar.", "structure → clarity → accuracy → consistency", listOf("Due to the fact that ... → Because ..." to "Due to the fact that ... yerine daha kısa Because ... kullan.", "The results show ... They indicate ... → avoid needless repetition" to "Aynı fikri gereksiz yere tekrar etmekten kaçın."), listOf("concise" to "özlü", "coherent" to "tutarlı", "redundant" to "gereksiz tekrar", "proofread" to "son okuma yapmak"), "Good editing usually makes a text clearer and more ___.", listOf("concise", "complicated", "repetitive", "ambiguous"), "concise", "Önce anlamı ve akışı düzelt, sonra yüzeysel dil hatalarını kontrol et.")
        "ileri dinleme ve not çıkarma" in t -> LessonContent("İleri dinleme ve not çıkarma", "Not alırken tam cümleler yazmak yerine anahtar sözcük, sayı, ilişki ve sonuçları kaydet. Kısaltmalarını tutarlı kullan ve sonrasında notları tamamla.", "topic · claim → evidence · cause → effect · decision → action", listOf("The speaker links the delay to a shortage of parts." to "Konuşmacı gecikmeyi parça eksikliğine bağlıyor.", "The team agreed to review the plan next Tuesday." to "Ekip planı gelecek salı gözden geçirmeyi kabul etti."), listOf("shortage" to "eksiklik", "outcome" to "sonuç", "action item" to "eylem maddesi", "deadline" to "son tarih"), "Useful notes prioritise the main points and key ___.", listOf("details", "filler words", "every pause", "unrelated examples"), "details", "Konuşma sonrasında notlarını ana fikir, kanıt, karar ve eylem maddelerine göre düzenle.")
        "profesyonel iletişim ve ikna" in t -> LessonContent("Profesyonel iletişim ve ikna", "Profesyonel ikna, karşı tarafın hedefini anlamak, güvenilir kanıt sunmak ve uygulanabilir bir öneri yapmaktır. Kesin olmayan bilgiyi kesinmiş gibi sunma.", "audience need → evidence → benefit → clear request", listOf("This change could reduce repeat work and improve traceability." to "Bu değişiklik tekrarlanan işi azaltıp izlenebilirliği artırabilir.", "Would you be willing to review the proposal by Friday?" to "Öneriyi cuma gününe kadar gözden geçirmeyi kabul eder misiniz?"), listOf("traceability" to "izlenebilirlik", "benefit" to "fayda", "proposal" to "öneri", "stakeholder" to "paydaş"), "Professional persuasion should be based on clear benefits and ___.", listOf("credible evidence", "pressure", "unsupported promises", "personal attacks"), "credible evidence", "Önerinin faydasını, kanıtını ve beklenen sonraki adımı açıkla.")
        "gövde ve yapısal parçalar" in t -> LessonContent("Gövde ve yapısal parçalar", "Yapısal incelemede fuselage gövdeyi, frame ve stringer gibi elemanlar yapısal destekleri anlatır. Terimleri doğru bağlamda öğren; belirli bir uçaktaki yapı ve limitler için onaylı dokümana başvur.", "fuselage = gövde · frame = çerçeve · stringer = boyuna yapısal eleman · skin = kaplama", listOf("The fuselage forms the main body of the aircraft." to "Gövde, uçağın ana gövde bölümünü oluşturur.", "Record any observed damage according to the approved procedure." to "Gözlenen hasarı onaylı prosedüre göre kaydet."), listOf("fuselage" to "gövde", "skin" to "kaplama", "frame" to "çerçeve", "stringer" to "boyuna eleman"), "The main body of an aircraft is the ___.", listOf("fuselage", "rudder", "brake pedal", "altimeter"), "fuselage", "Bu içerik terim eğitimidir; yapısal onarım kararı için onaylı bakım verisi gerekir.")
        "motor ve güç ünitesi terimleri" in t -> LessonContent("Motor ve güç ünitesi terimleri", "Motor metinlerinde engine, inlet, exhaust, compressor ve turbine gibi temel parçalar sık geçer. Gösterge değeri veya arıza yorumunu yalnızca uçak tipine uygun onaylı dokümanla yap.", "engine · inlet · compressor · turbine · exhaust", listOf("The inspection covers the engine inlet area." to "Kontrol motor hava giriş bölgesini kapsar.", "Record the observed condition using the approved task instructions." to "Gözlenen durumu onaylı iş talimatına göre kaydet."), listOf("inlet" to "hava girişi", "compressor" to "kompresör", "turbine" to "türbin", "exhaust" to "egzoz çıkışı"), "Which term refers to the part that compresses incoming air?", listOf("compressor", "rudder", "fuselage", "seat track"), "compressor", "Gerçek motor kontrollerinde yalnızca ilgili motor ve uçak için geçerli AMM/TSM verilerini kullan.")
        "hidrolik sistemler" in t -> LessonContent("Hidrolik sistemler", "Hydraulic system terimleri basınçlı sıvı, pompa, rezervuar, hat ve aktüatör gibi bileşenleri kapsar. İngilizce metinde leak, pressure ve inspect ifadelerini tanımayı öğren.", "pump = pompa · reservoir = hazne · pressure = basınç · leak = kaçak · actuator = aktüatör", listOf("The report notes a hydraulic fluid leak." to "Rapor hidrolik sıvı kaçağını kaydeder.", "Check the specified pressure limits in the approved documentation." to "Belirtilen basınç limitlerini onaylı dokümanda kontrol et."), listOf("hydraulic" to "hidrolik", "fluid" to "sıvı", "leak" to "kaçak", "actuator" to "aktüatör"), "A leak is an unintended loss of ___.", listOf("fluid", "paint colour", "passengers", "documents"), "fluid", "Bu ders terim öğrenmeye yöneliktir; basınç limitleri ve bakım adımları uçak dokümanından doğrulanmalıdır.")
        "elektrik ve kablolama" in t -> LessonContent("Elektrik ve kablolama", "Elektrik bakım metinlerinde wire, connector, harness, continuity ve insulation gibi kelimeler görülür. Emniyetli işlem sırası ve ölçüm sınırları modele özgü onaylı talimatla belirlenir.", "wire = tel · connector = konnektör · harness = kablo demeti · continuity = süreklilik · insulation = yalıtım", listOf("Inspect the connector for visible damage." to "Konnektörde görünür hasar olup olmadığını incele.", "Record the result of the specified continuity check." to "Belirtilen süreklilik kontrolünün sonucunu kaydet."), listOf("wire" to "tel", "connector" to "konnektör", "harness" to "kablo demeti", "continuity" to "süreklilik"), "A wire harness is a group of organised ___.", listOf("wires", "hydraulic fluids", "seat cushions", "documents"), "wires", "Elektrik işlemlerinde enerjiyi kesme, ölçüm ve bağlantı talimatları için onaylı prosedür şarttır.")
        "bakım kayıtları ve teknik dokümanlar" in t -> LessonContent("Bakım kayıtları ve teknik dokümanlar", "Bakım kayıtlarında yapılan iş, bulgu, kullanılan referans ve sonuç doğru biçimde belgelenir. AMM, FIM/TSM ve wiring diagram farklı amaçlara hizmet edebilir; doğru doküman ve revizyon doğrulanmalıdır.", "work order · task card · maintenance record · revision · reference", listOf("Refer to the current revision of the approved manual." to "Onaylı el kitabının güncel revizyonuna başvur.", "Record the task completion and any findings accurately." to "İşin tamamlanmasını ve bulguları doğru kaydet."), listOf("revision" to "revizyon", "task card" to "iş kartı", "finding" to "bulgu", "reference" to "referans"), "A revision identifies a version or update of a document. Which word means 'revizyon'?", listOf("revision", "rudder", "reservoir", "runway"), "revision", "Doküman numarası, revizyonu ve uygulanabilirliği gerçek işte mutlaka doğrula.")
        "alet kontrolü ve yabancı cisim önleme" in t -> LessonContent("Alet kontrolü ve yabancı cisim önleme", "Tool control ve FOD prevention emniyet kültürünün önemli parçalarıdır. Aletlerin sayımı, uygun saklanması ve yabancı cisimlerin alandan uzak tutulması için kurum prosedürleri izlenir.", "tool inventory · tool accountability · foreign object debris (FOD)", listOf("Complete the tool inventory after the task." to "İşten sonra alet envanterini tamamla.", "Keep the work area clear of foreign object debris." to "Çalışma alanını yabancı cisimlerden temiz tut."), listOf("inventory" to "envanter", "accountability" to "hesap verilebilirlik / takip", "debris" to "yabancı madde / döküntü", "work area" to "çalışma alanı"), "FOD prevention focuses on preventing foreign objects from causing ___.", listOf("damage", "translation errors", "schedule changes only", "noise in speech"), "damage", "Alet takibi ve FOD adımları için kuruluşun onaylı kontrol listesini uygula.")
        "arıza ve bulgu raporu yazımı" in t -> LessonContent("Arıza ve bulgu raporu yazımı", "İyi bir rapor gözlenen durumu, yeri, koşulu ve ilgili referansı tarafsız biçimde kaydeder. Kanıtlanmamış nedenleri gerçekmiş gibi yazma.", "Observed condition → location → evidence → reference/action", listOf("A visible crack was observed near the bracket." to "Braket yakınında görünür bir çatlak gözlendi.", "No conclusion about the cause was made without further inspection." to "Ek inceleme olmadan neden hakkında sonuca varılmadı."), listOf("observed" to "gözlemlendi", "evidence" to "kanıt", "location" to "konum", "cause" to "neden"), "A technical finding should be recorded ___.", listOf("objectively", "as a guess", "with invented codes", "without location"), "objectively", "Gerçek raporlarda uydurma arıza kodu veya parça bilgisi kullanma.")
        "human factors" in t -> LessonContent("Human factors ve emniyet iletişimi", "Human factors, yorgunluk, dikkat, iletişim, iş yükü ve ekip koordinasyonu gibi insan performansını etkileyen etmenleri kapsar. Açık geri bildirim ve bağımsız kontrol hataları azaltmaya yardımcı olur.", "fatigue · workload · handover · cross-check · situational awareness", listOf("Repeat the instruction to confirm shared understanding." to "Ortak anlayışı doğrulamak için talimatı tekrar et.", "Include unresolved items in the shift handover." to "Çözülmemiş maddeleri vardiya teslimine ekle."), listOf("fatigue" to "yorgunluk", "handover" to "devir teslim", "cross-check" to "çapraz kontrol", "awareness" to "farkındalık"), "A clear handover helps prevent information ___.", listOf("loss", "translation into every language", "extra fuel", "unrelated tasks"), "loss", "Emniyet açısından şüpheyi açıkça belirt ve anlaşılmayan talimatı teyit et.")
        "uyarı, ikaz ve zorunluluk dili" in t -> LessonContent("Uyarı, ikaz ve zorunluluk dili", "Teknik İngilizcede must, shall, should ve may aynı anlamda değildir; anlamları dokümanın bağlamı ve kurallarıyla belirlenir. WARNING, CAUTION ve NOTE etiketlerini de ayırt et.", "must/shall = zorunluluk bağlamı · should = tavsiye bağlamı · may = izin/olasılık", listOf("The specified inspection must be completed." to "Belirtilen kontrol tamamlanmalıdır.", "Refer to the caution before continuing." to "Devam etmeden önce dikkat uyarısına başvur."), listOf("mandatory" to "zorunlu", "recommendation" to "tavsiye", "caution" to "dikkat uyarısı", "permission" to "izin"), "Which word commonly expresses a recommendation?", listOf("should", "yesterday", "because", "under"), "should", "Teknik dokümandaki terimlerin bağlayıcılığını varsayma; doküman tanımlarını izle.")
        "ölçüler, toleranslar ve birimler" in t -> LessonContent("Ölçüler, toleranslar ve birimler", "Ölçüm metinlerinde value, unit, range, limit ve tolerance ifadeleri kullanılır. Birimi ve ondalık gösterimini kontrol et; ölçüm sınırlarını tahmin etme.", "measurement · unit · tolerance · upper/lower limit · within range", listOf("Record the measurement with its unit." to "Ölçümü birimiyle birlikte kaydet.", "The value must be within the specified limits." to "Değer belirtilen limitler içinde olmalıdır."), listOf("measurement" to "ölçüm", "unit" to "birim", "tolerance" to "tolerans", "limit" to "sınır"), "A tolerance describes an acceptable range of ___.", listOf("variation", "passengers", "language levels", "weather forecasts"), "variation", "Gerçek bakımda limitleri hafızadan tahmin etme; geçerli teknik veriden doğrula.")
        "bağlantı elemanları ve donanım" in t -> LessonContent("Bağlantı elemanları ve donanım", "Fastener terimleri bolt, nut, washer, screw ve pin gibi parçaları kapsar. Görünüşleri benzer olsa bile seçim, sıkma ve değiştirme gereklilikleri onaylı dokümana bağlıdır.", "bolt = cıvata · nut = somun · washer = rondela · screw = vida · pin = pim", listOf("Check the fastener identification against the task instructions." to "Bağlantı elemanı tanımını iş talimatıyla karşılaştır.", "Use the specified replacement part." to "Belirtilen yedek parçayı kullan."), listOf("fastener" to "bağlantı elemanı", "washer" to "rondela", "nut" to "somun", "replacement" to "yedek / değiştirme"), "Which item is commonly used with a bolt?", listOf("nut", "rudder", "reservoir", "altimeter"), "nut", "Parça seçimi, tork ve değiştirme kararını yalnızca onaylı bakım verisinden al.")
        "malzemeler ve korozyon" in t -> LessonContent("Malzemeler ve korozyon terminolojisi", "Material, alloy, coating ve corrosion terimleri malzeme ve yüzey durumlarını anlatır. Görsel bulguyu tarafsız tarif et; sınıflandırma ve kabul limitlerini teknik dokümana göre belirle.", "alloy = alaşım · coating = kaplama · corrosion = korozyon · surface = yüzey", listOf("Corrosion was reported on the surface." to "Yüzeyde korozyon bildirildi.", "Record the affected area and refer to the approved criteria." to "Etkilenen alanı kaydet ve onaylı kriterlere başvur."), listOf("alloy" to "alaşım", "coating" to "kaplama", "surface" to "yüzey", "affected area" to "etkilenen alan"), "Corrosion describes deterioration of a material through a chemical or electrochemical ___.", listOf("reaction", "translation", "schedule", "conversation"), "reaction", "Korozyonun kabul edilebilirliği veya onarımı hakkında tahmin yürütme.")
        "pnömatik sistemler" in t -> LessonContent("Pnömatik sistemler ve hava akışı", "Pneumatic terimleri basınçlı hava, hatlar, valfler ve akışla ilgilidir. Pressure, flow, valve ve leak sözcüklerini teknik cümlelerde tanımaya odaklan.", "compressed air · pressure · flow · valve · leak", listOf("The valve controls the airflow." to "Valf hava akışını kontrol eder.", "Record the reported leak according to the maintenance procedure." to "Bildirilen kaçağı bakım prosedürüne göre kaydet."), listOf("compressed air" to "basınçlı hava", "flow" to "akış", "valve" to "valf", "leak" to "kaçak"), "A valve can control or direct the flow of ___.", listOf("air", "paper", "messages only", "passengers"), "air", "Basınç ve işlem limitlerini gerçek uçak için onaylı dokümandan doğrula.")
        "aviyonik ve iletişim" in t -> LessonContent("Aviyonik ve iletişim sistemleri", "Avionics, uçaktaki elektronik sistemleri kapsayan genel terimdir. Display, antenna, transceiver ve communication gibi kelimeleri bağlam içinde öğren; arıza giderme uçak tipine özgü prosedüre bağlıdır.", "avionics · display · antenna · transceiver · communication", listOf("The crew reported an intermittent communication issue." to "Ekip aralıklı bir iletişim sorunu bildirdi.", "Record the message exactly as shown in the approved procedure." to "Mesajı onaylı prosedürde gösterildiği şekilde kaydet."), listOf("display" to "gösterge ekranı", "antenna" to "anten", "intermittent" to "aralıklı", "communication" to "iletişim"), "Avionics primarily refers to aircraft ___.", listOf("electronic systems", "seat upholstery", "paint colours", "catering supplies"), "electronic systems", "Arıza kodu veya test adımı uydurma; ilgili uçak ve sistem dokümanını kullan.")
        "motor kontrolü ve durum ifadeleri" in t -> LessonContent("Motor kontrolü ve durum ifadeleri", "Condition report dilinde normal, abnormal, stable, fluctuating ve within limits gibi ifadeler görülür. Gözlem ile tanıyı ayır ve değerleri yalnızca onaylı limitlerle karşılaştır.", "normal · abnormal · stable · fluctuating · within specified limits", listOf("The indication fluctuated during the observation." to "Gözlem sırasında gösterge değeri dalgalandı.", "The recorded value was compared with the specified limit." to "Kaydedilen değer belirtilen limit ile karşılaştırıldı."), listOf("indication" to "gösterge değeri", "fluctuate" to "dalgalanmak", "stable" to "sabit", "specified limit" to "belirtilen limit"), "If a value changes irregularly, it may be described as ___.", listOf("fluctuating", "polished", "reserved", "translated"), "fluctuating", "Motor parametrelerinin normal aralığını veya arıza nedenini bu genel dil dersinden çıkarma.")
        "task card" in t || "iş emri okuma" in t -> LessonContent("Task card ve iş emri okuma", "Task card veya work order; iş kapsamını, ön koşulları, referansları ve tamamlanma kayıtlarını içerebilir. Her adımı, uyarıyı ve revizyonu dikkatle okuyup kayıt gerekliliklerini ayırt et.", "task description · prerequisites · references · accomplishment · sign-off", listOf("Review the task prerequisites before starting." to "Başlamadan önce işin ön koşullarını incele.", "Record completion in the designated field." to "Tamamlanmayı belirtilen alana kaydet."), listOf("prerequisite" to "ön koşul", "accomplishment" to "tamamlama", "sign-off" to "onay imzası", "reference" to "referans"), "A prerequisite is something that must be met ___.", listOf("before a task begins", "after all records are deleted", "only during translation", "without reading"), "before a task begins", "Bu dil eğitimi gerçek iş emrinin veya yetkili onayının yerine geçmez.")
        "vardiya teslimi" in t -> LessonContent("Vardiya teslimi ve ekip iletişimi", "Vardiya tesliminde açık kalan işler, mevcut durum, alınan önlemler ve sonraki adımlar kısa ve net aktarılır. Bilginin alındığını teyit etmek yanlış anlaşılmayı azaltır.", "completed · outstanding · monitored · next action · handover", listOf("The inspection is complete; the report remains outstanding." to "Kontrol tamamlandı; rapor hâlâ bekliyor.", "The next shift should review the open item." to "Sonraki vardiya açık maddeyi gözden geçirmeli."), listOf("outstanding" to "tamamlanmamış / bekleyen", "handover" to "devir teslim", "monitor" to "izlemek", "next action" to "sonraki adım"), "A good handover should include unresolved ___.", listOf("items", "unrelated stories", "guesses", "personal passwords"), "items", "Açık maddeleri, sorumluyu ve sonraki adımı net olarak belirt.")
        "teknik metin okuma stratejileri" in t -> LessonContent("Teknik metin okuma stratejileri", "Teknik metinde önce doküman başlığını, uygulanabilirlik bilgilerini, uyarıları ve adım sırasını kontrol et. Kısaltmaları bağlam içinde yorumla ve anlamadığın terimi güvenilir sözlük veya dokümandan doğrula.", "document scope → warning → prerequisites → steps → acceptance criteria", listOf("Check the applicability before using the procedure." to "Prosedürü kullanmadan önce uygulanabilirliği kontrol et.", "Do not skip a caution or note." to "Dikkat uyarısını veya notu atlama."), listOf("applicability" to "uygulanabilirlik", "prerequisite" to "ön koşul", "acceptance criteria" to "kabul kriterleri", "abbreviation" to "kısaltma"), "Before applying a technical procedure, check its ___.", listOf("applicability", "cover colour", "page count only", "author's hobbies"), "applicability", "Gerçek bakım işinde güncel, onaylı ve doğru uçak konfigürasyonuna ait dokümanı doğrula.")
        "mesleki terimler ve tekrar" in t -> LessonContent("Mesleki terimler ve tekrar değerlendirmesi", "Mesleki kelimeleri tek başına ezberlemek yerine bakım cümlesi içinde öğren. Component, inspect, replace, record ve verify gibi fiillerin hangi bağlamda kullanıldığını ayırt et.", "component · inspect · replace · record · verify", listOf("Inspect the component and record the finding." to "Bileşeni incele ve bulguyu kaydet.", "Verify the part number against the approved reference." to "Parça numarasını onaylı referansla doğrula."), listOf("component" to "bileşen", "inspect" to "incelemek", "replace" to "değiştirmek", "verify" to "doğrulamak"), "Which verb means to confirm that something is correct?", listOf("verify", "ignore", "delay", "guess"), "verify", "Bu tekrar dersi teknik dil içindir; gerçek bakım kararları onaylı dokümana dayanmalıdır.")

        else -> LessonContent(title, "Bu derste konuya ait temel ifadeleri ve örnek cümleleri öğreneceksin. Kuralı örneklerle incele, sonra videoyu izle ve alıştırmayı tamamla.", "Örnek cümleye bak; özne, fiil ve zaman yapısını belirle.", listOf("Read the example carefully." to "Örnek cümleyi dikkatlice oku.", "Listen and repeat." to "Dinle ve tekrar et.", "Try the exercise." to "Alıştırmayı dene."), listOf("example" to "örnek", "sentence" to "cümle", "meaning" to "anlam", "practice" to "alıştırma"), "She ___ English every day.", listOf("studies", "study", "studying", "studied"), "studies", "Videoda başlıktaki konunun örneklerini ve kullanılan dil yapısını takip et.")
    }
}

