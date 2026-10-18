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
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF202044)), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✈️ SENİN İÇİN ÖZEL", color = Color(0xFFC9C0FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text("Uçak Bakım İngilizcesi", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Airframe, powerplant, bakım kayıtları, arıza raporları ve emniyet terminolojisi.", color = Color(0xFFE2E1F2), fontSize = 13.sp, lineHeight = 19.sp)
                    Button(onClick = { selectedTrackId = "aviation-maintenance"; showAnswers = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B8AFF))) { Text("Uçak Bakım Kursuna Başla", color = Color(0xFF191633), fontWeight = FontWeight.Bold) }
                }
            }
            tracks.filter { it.id != "aviation-maintenance" }.forEach { track ->
                Card(Modifier.fillMaxWidth().clickable { selectedTrackId = track.id; showAnswers = false }, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(track.title, color = Color(0xFF202044), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(track.description, color = Color(0xFF687086), fontSize = 12.sp, lineHeight = 17.sp)
                        }
                        Text("✦", color = Color(0xFF6957E8), fontSize = 22.sp)
                    }
                }
            }
            Text("Not: Premium rozeti tek başına ödeme alındığı veya satın alma doğrulandığı anlamına gelmez.", color = Color(0xFF77738B), fontSize = 11.sp, lineHeight = 16.sp)
        } else {
            TextButton(onClick = { selectedTrackId = null; showAnswers = false }) { Text("← Meslek kataloğuna dön") }
            Text(selectedTrack!!.title, color = Color(0xFF202044), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
            Text(selectedTrack.description, color = Color(0xFF687086), fontSize = 14.sp, lineHeight = 20.sp)
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
                            Text(term.exampleEnglish, color = Color(0xFF30384B), fontSize = 13.sp)
                        }
                    }
                }
                Button(onClick = { showAnswers = !showAnswers }, modifier = Modifier.fillMaxWidth()) { Text(if (showAnswers) "Mini testi gizle" else "Mini testi aç") }
                if (showAnswers) Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Mini tekrar", color = Color(0xFF202044), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        selectedTrack.sampleTerms.take(5).forEach { term -> Text("${term.english} = ${term.turkish}", color = Color(0xFF30384B), fontSize = 14.sp) }
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
