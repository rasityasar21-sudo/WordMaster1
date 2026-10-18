package com.example.wordmaster

/** Curated lesson model for WordMaster's structured course roadmap. */
data class CourseLesson(
    val id: String,
    val title: String,
    val level: String,
    val category: String,
    val estimatedMinutes: Int,
    val objectives: List<String>,
    val prerequisiteIds: List<String> = emptyList()
)

object WordMasterCourseCatalog {
    val lessons = listOf(
        CourseLesson("a1-greetings", "Selamlaşma ve tanışma", "A1", "Konuşma", 8,
            listOf("Temel selamlaşmaları kullan", "Kendini kısa cümlelerle tanıt")),
        CourseLesson("a1-pronouns", "Kişi zamirleri ve be", "A1", "Dil bilgisi", 10,
            listOf("I, you, he, she zamirlerini ayırt et", "Am/is/are ile olumlu cümle kur")),
        CourseLesson("a1-daily-words", "Günlük temel kelimeler", "A1", "Kelime", 10,
            listOf("Günlük hayatta kullanılan kelimeleri tanı", "Kelimeyi örnek cümlede kullan")),
        CourseLesson("a1-questions", "Basit sorular sorma", "A1", "Dil bilgisi", 12,
            listOf("What, where ve who sorularını tanı", "Kısa sorulara cevap ver"),
            listOf("a1-pronouns")),
        CourseLesson("a1-routine", "Günlük rutinim", "A1", "Okuma", 12,
            listOf("Günlük eylemleri anlat", "Kısa bir metinden bilgi çıkar"),
            listOf("a1-daily-words")),
        CourseLesson("a1-listening", "Kısa diyalogları anlama", "A1", "Dinleme", 10,
            listOf("Diyalogdaki ana bilgiyi yakala"),
            listOf("a1-greetings")),
        CourseLesson("a2-past", "Geçmiş zaman: was/were", "A2", "Dil bilgisi", 12,
            listOf("Geçmiş durumları anlat", "Olumlu ve olumsuz cümle kur"),
            listOf("a1-pronouns")),
        CourseLesson("a2-shopping", "Alışverişte İngilizce", "A2", "Konuşma", 12,
            listOf("Fiyat sor", "Kibar isteklerde bulun"),
            listOf("a1-questions")),
        CourseLesson("a2-comparatives", "Karşılaştırmalar", "A2", "Dil bilgisi", 12,
            listOf("İki nesneyi karşılaştır", "Than kullanımını öğren"),
            listOf("a1-daily-words")),
        CourseLesson("b1-experiences", "Deneyimleri anlatmak", "B1", "Dil bilgisi", 15,
            listOf("Present perfect yapısını tanı", "Deneyimlerle ilgili cümle kur"),
            listOf("a2-past")),
        CourseLesson("b1-opinions", "Fikir belirtme", "B1", "Yazma", 15,
            listOf("Bir fikri gerekçesiyle ifade et", "Bağlaçlarla cümleleri birleştir"),
            listOf("a2-shopping")),
        CourseLesson("b2-arguments", "Görüşleri karşılaştırma", "B2", "Okuma", 18,
            listOf("Bir metindeki karşıt görüşleri bul", "Daha ayrıntılı argüman kur"),
            listOf("b1-opinions"))
    )

    fun forLevel(level: String): List<CourseLesson> =
        lessons.filter { it.level.equals(level, ignoreCase = true) }

    fun find(id: String): CourseLesson? = lessons.firstOrNull { it.id == id }

    fun available(completedIds: Set<String>, level: String): List<CourseLesson> =
        forLevel(level).filter { lesson ->
            lesson.id !in completedIds && completedIds.containsAll(lesson.prerequisiteIds)
        }

    fun nextRecommended(completedIds: Set<String>, level: String): CourseLesson? =
        available(completedIds, level).firstOrNull()
}

/** A small local study plan that stays deterministic and can be persisted by the UI. */
data class DailyStudyPlan(
    val date: String,
    val targetMinutes: Int,
    val lessonIds: List<String>,
    val reviewWords: List<String>
) {
    val estimatedMinutes: Int
        get() = lessonIds.mapNotNull(WordMasterCourseCatalog::find).sumOf { it.estimatedMinutes }

    val isGoalMet: Boolean
        get() = estimatedMinutes >= targetMinutes

    companion object {
        fun create(
            level: String,
            completedLessonIds: Set<String>,
            dueWords: List<WordProgress>,
            targetMinutes: Int = 15,
            date: String = java.time.LocalDate.now().toString()
        ): DailyStudyPlan {
            val safeTarget = targetMinutes.coerceIn(5, 120)
            val available = WordMasterCourseCatalog.available(completedLessonIds, level)
            val chosen = mutableListOf<String>()
            var minutes = 0
            for (lesson in available) {
                if (chosen.isNotEmpty() && minutes >= safeTarget) break
                chosen += lesson.id
                minutes += lesson.estimatedMinutes
            }
            return DailyStudyPlan(
                date = date,
                targetMinutes = safeTarget,
                lessonIds = chosen,
                reviewWords = dueWords.take(10).map { it.word }
            )
        }
    }
}
