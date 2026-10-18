package com.example.wordmaster

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
    var trackSearch by remember { mutableStateOf("") }
    val tracks = ProfessionalCourseCatalog.tracks
    val selectedTrack = tracks.firstOrNull { it.id == selectedTrackId }
    Column(Modifier.fillMaxWidth().background(Color(0xFFF7F5FF)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("WORDMASTER PREMIUM", color = Color(0xFF6957E8), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp)
                Text("Mesleki İngilizce", color = Color(0xFF202044), fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
            }
            Surface(color = Color(0xFFE8E4FF), shape = RoundedCornerShape(14.dp)) { Text("✦ PREMIUM", Modifier.padding(horizontal = 10.dp, vertical = 8.dp), color = Color(0xFF5943C5), fontWeight = FontWeight.Bold, fontSize = 11.sp) }
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
                Card(Modifier.fillMaxWidth().clickable { selectedTrackId = track.id; showAnswers = false; quizIndex = 0; quizAnswer = null; quizChecked = false; quizScore = 0 }, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
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
                            Text(track.description, color = Color(0xFF687086), fontSize = 12.sp, lineHeight = 17.sp)
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
                Button(onClick = { showAnswers = !showAnswers; quizIndex = 0; quizAnswer = null; quizChecked = false; quizScore = 0 }, modifier = Modifier.fillMaxWidth()) { Text(if (showAnswers) "Mini testi kapat" else "Kendini test et") }
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
                                    Text(if (correct) "Doğru cevap!" else "Doğru cevap: ${currentTerm.turkish}", color = if (correct) Color(0xFF18794E) else Color(0xFFB42318), fontWeight = FontWeight.Bold)
                                    Button(onClick = { if (correct) quizScore++; quizIndex++; quizAnswer = null; quizChecked = false }, modifier = Modifier.fillMaxWidth()) { Text(if (quizIndex == quizTerms.lastIndex) "Sonucu gör" else "Sonraki soru") }
                                } else { Button(onClick = { if (quizAnswer != null) quizChecked = true }, enabled = quizAnswer != null, modifier = Modifier.fillMaxWidth()) { Text("Cevabı kontrol et") } }
                            }
                        }
                    } else if (quizTerms.isNotEmpty()) {
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Test tamamlandı!", color = Color(0xFF202044), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Doğru cevap: $quizScore / ${quizTerms.size}", color = Color(0xFF6957E8), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Button(onClick = { quizIndex = 0; quizAnswer = null; quizChecked = false; quizScore = 0 }, modifier = Modifier.fillMaxWidth()) { Text("Tekrar çöz") }
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
