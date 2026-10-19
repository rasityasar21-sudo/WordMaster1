package com.example.wordmaster

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CourseCatalogSection(
    level: String,
    completedIds: Set<String>,
    onLaunch: (CourseLesson) -> Unit
) {
    var selectedLevel by remember(level) { mutableStateOf(level) }
    val lessons = WordMasterCourseCatalog.forLevel(selectedLevel)
    val completed = lessons.count { it.id in completedIds }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("YAPILANDIRILMIŞ İNGİLİZCE KURSU", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
        Text("A1’den C1’e ders yolu", color = MaterialTheme.colorScheme.onSurface, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
        Text("Konu anlatımı, hedefler ve ön koşullarla ilerle. Tamamlanan dersler bu cihazda saklanır.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp, lineHeight = 19.sp)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            listOf("A1", "A2", "B1", "B2", "C1", "Mesleki").forEach { option ->
                FilterChip(selected = selectedLevel == option, onClick = { selectedLevel = option }, label = { Text(option) })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$completed / ${lessons.size} ders tamamlandı", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
            Spacer(Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = { if (lessons.isEmpty()) 0f else completed.toFloat() / lessons.size },
                modifier = Modifier.weight(1f),
                color = Color(0xFF13A879)
            )
        }
        lessons.forEach { lesson ->
            val isDone = lesson.id in completedIds
            val levelOrder = listOf("A1", "A2", "B1", "B2", "C1")
            val currentLevelIndex = levelOrder.indexOf(selectedLevel)
            val missing = lesson.prerequisiteIds.filter { prerequisiteId ->
                if (prerequisiteId in completedIds) {
                    false
                } else {
                    val prerequisite = WordMasterCourseCatalog.find(prerequisiteId)
                    prerequisite == null ||
                        levelOrder.indexOf(prerequisite.level) >= currentLevelIndex
                }
            }
            val unlocked = missing.isEmpty()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDone) (if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF17382F) else Color(0xFFE8F8F0)) else MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(lesson.title, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(if (isDone) "✓" else if (unlocked) "→" else "🔒", fontSize = 17.sp)
                    }
                    Text("${lesson.category} · ${lesson.estimatedMinutes} dk", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f), fontSize = 11.sp)
                    lesson.objectives.forEach { objective ->
                        Text("• $objective", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), fontSize = 12.sp, lineHeight = 17.sp)
                    }
                    if (!unlocked) {
                        val prerequisiteTitles = missing.mapNotNull(WordMasterCourseCatalog::find).joinToString { it.title }
                        Text("Önce tamamla: $prerequisiteTitles", color = Color(0xFF926C30), fontSize = 11.sp)
                    }
                    TextButton(
                        onClick = { onLaunch(lesson) },
                        enabled = unlocked && !isDone,
                        modifier = Modifier.align(Alignment.End)
                    ) { Text(if (isDone) "Tamamlandı" else if (unlocked) "Derse başla" else "Kilitli") }
                }
            }
        }
    }
}
