package com.example.wordmaster

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfessionalCoursesScreen(onBackToLessons: () -> Unit) {
    var selectedTrackId by remember { mutableStateOf<String?>(null) }
    var showAnswers by remember { mutableStateOf(false) }
    var quizIndex by remember { mutableIntStateOf(0) }
    var quizAnswer by remember { mutableStateOf<String?>(null) }
    var quizChecked by remember { mutableStateOf(false) }
    var quizScore by remember { mutableIntStateOf(0) }
    var missedTerms by remember { mutableStateOf(listOf<String>()) }
    var trackSearch by remember { mutableStateOf("") }
    var revealedLessonAnswers by remember { mutableStateOf(setOf<String>()) }
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("wordmaster", Context.MODE_PRIVATE) }
    var completedProfessionalLessons by remember {
        mutableStateOf(prefs.getStringSet("professionalCompletedLessons", emptySet())?.toSet() ?: emptySet())
    }
    val tracks = ProfessionalCourseCatalog.tracks
    val selectedTrack = tracks.firstOrNull { it.id == selectedTrackId }
    Column(Modifier.fillMaxWidth().background(Color(0xFFF7F5FF)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = {
                if (selectedTrackId != null) {
                    selectedTrackId = null
                    showAnswers = false
                    revealedLessonAnswers = emptySet()
                    quizIndex = 0
                    quizAnswer = null
                    quizChecked = false
                    quizScore = 0
                    missedTerms = emptyList()
                } else onBackToLessons()
            }) {
                Text("‹", color = Color(0xFF5943C5), fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text("WORDMASTER • CAREER ENGLISH", color = Color(0xFF6957E8), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp)
                Text("Mesleki İngilizce", color = Color(0xFF202044), fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
            }
            Surface(color = Color(0xFFE8E4FF), shape = RoundedCornerShape(14.dp)) { Text("UZMANLIK YOLU", Modifier.padding(horizontal = 10.dp, vertical = 8.dp), color = Color(0xFF5943C5), fontWeight = FontWeight.Bold, fontSize = 11.sp) }
        }
        Text("Mesleğine özel terimler, teknik okuma ve örnek cümlelerle İngilizceni geliştir.", color = Color(0xFF687086), fontSize = 14.sp, lineHeight = 20.sp)
        if (selectedTrack == null) {
            Surface(color = Color(0xFFEDEAFF), shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("✦", color = Color(0xFF6957E8), fontSize = 22.sp)
                    Column {
                        Text("Mesleğini seç, kendi alanında öğren", color = Color(0xFF202044), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${tracks.size} uzmanlık alanı • Terimler • Örnek cümleler • Mini testler", color = Color(0xFF687086), fontSize = 11.sp)
                    }
                }
            }
            OutlinedTextField(
                value = trackSearch,
                onValueChange = { trackSearch = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Meslek ara") },
                placeholder = { Text("Örn. uçak bakım, yazılım, sağlık") },
                shape = RoundedCornerShape(14.dp)
            )
            val visibleTracks = tracks.filter { it.title.contains(trackSearch, ignoreCase = true) || it.description.contains(trackSearch, ignoreCase = true) }
            if (visibleTracks.isEmpty()) Text("Bu aramayla eşleşen meslek bulunamadı.", color = Color(0xFF687086), fontSize = 13.sp)
            visibleTracks.forEach { track ->
                Card(Modifier.fillMaxWidth().clickable { selectedTrackId = track.id; showAnswers = false; revealedLessonAnswers = emptySet(); quizIndex = 0; quizAnswer = null; quizChecked = false; quizScore = 0 }, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(color = Color(0xFFEDEAFF), shape = RoundedCornerShape(14.dp)) {
                            Text(when (track.id) {
                                "aviation-maintenance" -> "✈"
                                "healthcare" -> "✚"
                                "software-it" -> "</>"
                                "engineering" -> "⚙"
                                "business" -> "▣"
                                "hospitality" -> "✦"
                                "logistics" -> "⇄"
                                "law" -> "§"
                                "education" -> "✎"
                                "automotive" -> "◈"
                                else -> "✦"
                            }, Modifier.padding(12.dp), color = Color(0xFF5943C5), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(track.title, color = Color(0xFF202044), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(track.description, color = Color(0xFF687086), fontSize = 12.sp, lineHeight = 17.sp, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = Color(0xFFF0EDFF), shape = RoundedCornerShape(7.dp)) { Text("${track.topics.size} konu", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF5943C5), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                if (track.sampleTerms.isNotEmpty()) Surface(color = Color(0xFFE7F7EF), shape = RoundedCornerShape(7.dp)) { Text("${track.sampleTerms.size} terim", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF18794E), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            }
                        }
                        Text("›", color = Color(0xFF6957E8), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text("Not: Premium rozeti tek başına ödeme alındığı veya satın alma doğrulandığı anlamına gelmez.", color = Color(0xFF77738B), fontSize = 11.sp, lineHeight = 16.sp)
        } else {
            TextButton(onClick = { selectedTrackId = null; showAnswers = false }) { Text("← Meslek kataloğuna dön") }
            Text(selectedTrack!!.title, color = Color(0xFF202044), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
            Text(selectedTrack.description, color = Color(0xFF687086), fontSize = 14.sp, lineHeight = 20.sp)
            if (selectedTrack.lessons.isNotEmpty()) {
                val trackLessonIds = selectedTrack.lessons.map { "${selectedTrack.id}:${it.id}" }
                val completedInTrack = trackLessonIds.count { it in completedProfessionalLessons }
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text("KURS İLERLEMEN", color = Color(0xFF6957E8), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
                        Text("$completedInTrack / ${selectedTrack.lessons.size} ders tamamlandı", color = Color(0xFF202044), fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = { completedInTrack.toFloat() / selectedTrack.lessons.size.coerceAtLeast(1) },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF319B73),
                            trackColor = Color(0xFFE8E4FF)
                        )
                        Text("İlerlemen bu cihazda saklanır.", color = Color(0xFF687086), fontSize = 11.sp)
                    }
                }
            }
            Surface(color = Color(0xFFE7F7EF), shape = RoundedCornerShape(12.dp)) {
                Text(if (selectedTrack.id == "aviation-maintenance") "Önemli: Bu kurs İngilizce öğrenimi içindir. Gerçek bakım işlemlerinde güncel, onaylı bakım dokümanları ve yetkili prosedürler esas alınmalıdır." else "Öğrenme yolu: konu başlıklarını incele, terimleri örnek cümlelerle öğren ve mini testle kendini kontrol et.", Modifier.fillMaxWidth().padding(12.dp), color = Color(0xFF245B46), fontSize = 12.sp, lineHeight = 17.sp)
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DERS YOL HARİTASI", color = Color(0xFF6957E8), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    selectedTrack.topics.forEachIndexed { index, topic ->
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(color = Color(0xFFE8E4FF), shape = RoundedCornerShape(8.dp)) { Text("${index + 1}", Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = Color(0xFF5943C5), fontWeight = FontWeight.Bold) }
                            Text(topic, color = Color(0xFF202044), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
            if (selectedTrack.lessons.isNotEmpty()) {
                Text("KONU ANLATIMLI DERSLER", color = Color(0xFF6957E8), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Text("Her derste hedef, kısa anlatım, mesleki ifadeler ve kontrol sorusu bulunur.", color = Color(0xFF687086), fontSize = 13.sp, lineHeight = 18.sp)
                selectedTrack.lessons.forEach { lesson ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            val progressId = "${selectedTrack.id}:${lesson.id}"
                            val isLessonCompleted = progressId in completedProfessionalLessons
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(lesson.title, modifier = Modifier.weight(1f), color = Color(0xFF202044), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                if (isLessonCompleted) {
                                    Surface(color = Color(0xFFE7F7EF), shape = RoundedCornerShape(8.dp)) {
                                        Text("✓ Tamamlandı", Modifier.padding(horizontal = 8.dp, vertical = 5.dp), color = Color(0xFF18794E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text("BU DERSTE", color = Color(0xFF6957E8), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                            Text(lesson.objective, color = Color(0xFF30384B), fontSize = 13.sp, lineHeight = 18.sp)
                            Text(lesson.explanation, color = Color(0xFF687086), fontSize = 13.sp, lineHeight = 19.sp)
                            Divider(color = Color(0xFFECEAF4))
                            lesson.phrases.forEach { (english, turkish) ->
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(english, color = Color(0xFF202044), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(turkish, color = Color(0xFF6957E8), fontSize = 12.sp)
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    revealedLessonAnswers = if (lesson.id in revealedLessonAnswers) revealedLessonAnswers - lesson.id else revealedLessonAnswers + lesson.id
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(if (lesson.id in revealedLessonAnswers) "Yanıtı gizle" else "Kontrol sorusunu yanıtla") }
                            Text(lesson.question, color = Color(0xFF202044), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            if (lesson.id in revealedLessonAnswers) {
                                Surface(color = Color(0xFFE7F7EF), shape = RoundedCornerShape(10.dp)) {
                                    Text(lesson.answer, Modifier.fillMaxWidth().padding(11.dp), color = Color(0xFF245B46), fontSize = 12.sp, lineHeight = 18.sp)
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    completedProfessionalLessons = if (isLessonCompleted) {
                                        completedProfessionalLessons - progressId
                                    } else {
                                        completedProfessionalLessons + progressId
                                    }
                                    prefs.edit().putStringSet("professionalCompletedLessons", completedProfessionalLessons).apply()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isLessonCompleted) "Tamamlandı olarak işaretlendi · geri al" else "Dersi tamamlandı olarak işaretle")
                            }
                        }
                    }
                }
            }
            if (selectedTrack.sampleTerms.isNotEmpty()) {
                Text("TERİM KARTLARI", color = Color(0xFF6957E8), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Text("İngilizce terimi ve Türkçe anlamını örnekle birlikte öğren.", color = Color(0xFF687086), fontSize = 13.sp)
                selectedTrack.sampleTerms.forEach { term ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(term.english, color = Color(0xFF202044), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            Text(term.turkish, color = Color(0xFF6957E8), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(term.exampleEnglish, color = Color(0xFF30384B), fontSize = 13.sp, lineHeight = 18.sp)
                            if (term.note.isNotBlank()) Text(term.note, color = Color(0xFF687086), fontSize = 12.sp)
                        }
                    }
                }
                Button(onClick = { showAnswers = !showAnswers; quizIndex = 0; quizAnswer = null; quizChecked = false; quizScore = 0; missedTerms = emptyList() }, modifier = Modifier.fillMaxWidth()) { Text(if (showAnswers) "Mini testi kapat" else "Kendini test et") }
                if (showAnswers) {
                    val quizTerms = selectedTrack.sampleTerms.take(5)
                    if (quizTerms.isNotEmpty() && quizIndex < quizTerms.size) {
                        val currentTerm = quizTerms[quizIndex]
                        val choices = remember(selectedTrack.id, quizIndex) { (listOf(currentTerm) + quizTerms.filter { it != currentTerm }.shuffled().take(3)).shuffled() }
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("MİNİ TEST · ${quizIndex + 1}/${quizTerms.size}", color = Color(0xFF6957E8), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                Text("\"${currentTerm.english}\" ne demek?", color = Color(0xFF202044), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                choices.forEach { option ->
                                    OutlinedButton(onClick = { if (!quizChecked) quizAnswer = option.turkish }, modifier = Modifier.fillMaxWidth(), enabled = !quizChecked, colors = ButtonDefaults.outlinedButtonColors(containerColor = if (quizAnswer == option.turkish) Color(0xFFE8E4FF) else Color.White)) { Text(option.turkish, color = Color(0xFF202044)) }
                                }
                                if (quizChecked) {
                                    val correct = quizAnswer == currentTerm.turkish
                                    Text(if (correct) "Doğru cevap! ✓" else "Doğru cevap: ${currentTerm.turkish}", color = if (correct) Color(0xFF18794E) else Color(0xFFB42318), fontWeight = FontWeight.Bold)
                                    Text("Kelime: ${currentTerm.english}  •  Türkçesi: ${currentTerm.turkish}", color = Color(0xFF687086), fontSize = 12.sp, lineHeight = 17.sp)
                                    Button(onClick = {
                                        if (correct) quizScore++ else missedTerms = (missedTerms + currentTerm.english).distinct()
                                        quizIndex++
                                        quizAnswer = null
                                        quizChecked = false
                                    }, modifier = Modifier.fillMaxWidth()) { Text(if (quizIndex == quizTerms.lastIndex) "Sonucu gör" else "Sonraki soru") }
                                } else { Button(onClick = { if (quizAnswer != null) quizChecked = true }, enabled = quizAnswer != null, modifier = Modifier.fillMaxWidth()) { Text("Cevabı kontrol et") } }
                            }
                        }
                    } else if (quizTerms.isNotEmpty()) {
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Test tamamlandı!", color = Color(0xFF202044), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Doğru cevap: $quizScore / ${quizTerms.size}  •  Başarı: ${(quizScore * 100) / quizTerms.size}%", color = Color(0xFF6957E8), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                if (missedTerms.isNotEmpty()) {
                                    Text("TEKRAR ETMEN GEREKEN TERİMLER", color = Color(0xFFB42318), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp)
                                    missedTerms.forEach { missed ->
                                        val missedTerm = quizTerms.firstOrNull { it.english == missed }
                                        if (missedTerm != null) {
                                            Text("• ${missedTerm.english} — ${missedTerm.turkish}", color = Color(0xFF30384B), fontSize = 13.sp)
                                        }
                                    }
                                } else {
                                    Text("Harika! Bu testte tüm cevaplar doğru. Bir sonraki oturumda yeni terimlerle tekrar yap.", color = Color(0xFF18794E), fontSize = 13.sp, lineHeight = 18.sp)
                                }
                                Button(onClick = { quizIndex = 0; quizAnswer = null; quizChecked = false; quizScore = 0; missedTerms = emptyList() }, modifier = Modifier.fillMaxWidth()) { Text("Tekrar çöz") }
                            }
                        }
                    }
                }
            } else Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Kurs hazırlanıyor", color = Color(0xFF202044), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Bu meslek için ders başlıkları katalogda tanımlandı. Terim setleri, alıştırmalar ve kaynak doğrulaması sonraki adımlarda genişletilecek.", color = Color(0xFF687086), fontSize = 13.sp, lineHeight = 19.sp)
                }
            }
            TextButton(onClick = onBackToLessons, modifier = Modifier.fillMaxWidth()) { Text("Genel İngilizce derslerine dön") }
        }
    }
}
