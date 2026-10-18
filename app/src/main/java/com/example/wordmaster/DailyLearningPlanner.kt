package com.example.wordmaster

/**
 * Deterministic, offline-first planner. It recommends lessons already present in
 * the curated catalog; it does not claim to diagnose a learner's true CEFR level.
 */
data class DailyLearningPlan(
    val level: String,
    val availableMinutes: Int,
    val lessons: List<CourseLesson>,
    val totalMinutes: Int
)

object DailyLearningPlanner {
    private val levels = listOf("A1", "A2", "B1", "B2", "C1")

    fun create(
        preferredLevel: String,
        completedLessonIds: Set<String>,
        availableMinutes: Int = 20,
        catalog: List<CourseLesson> = WordMasterCourseCatalog.lessons
    ): DailyLearningPlan {
        val level = preferredLevel.uppercase().takeIf { it in levels } ?: "A1"
        val budget = availableMinutes.coerceIn(5, 180)
        val byId = catalog.associateBy { it.id }
        val completed = completedLessonIds.intersect(byId.keys)
        val eligible = catalog.filter { lesson ->
            lesson.level == level &&
                lesson.id !in completed &&
                lesson.prerequisiteIds.all { prerequisiteId ->
                    prerequisiteId in completed || (byId[prerequisiteId]?.level?.let { levels.indexOf(it) < levels.indexOf(level) } == true)
                }
        }.sortedWith(compareBy<CourseLesson> { it.category }.thenBy { it.estimatedMinutes }.thenBy { it.id })

        val selected = mutableListOf<CourseLesson>()
        var used = 0
        for (lesson in eligible) {
            val minutes = lesson.estimatedMinutes.coerceAtLeast(1)
            if (used + minutes <= budget) {
                selected += lesson
                used += minutes
            }
        }
        // If every lesson exceeds the budget, still offer one small next step.
        if (selected.isEmpty()) {
            eligible.minByOrNull { it.estimatedMinutes }?.let { selected += it; used = it.estimatedMinutes.coerceAtLeast(1) }
        }
        return DailyLearningPlan(level, budget, selected, used)
    }
}
