package com.example.wordmaster

/** Turns recent recall results into a simple, explainable next-step recommendation. */
data class AdaptiveLearningAdvice(
    val title: String,
    val message: String,
    val focusWords: List<String>
)

object AdaptiveLearningAdvisor {
    fun recommend(words: List<WordProgress>): AdaptiveLearningAdvice {
        val due = WordMasterLearningEngine.dueForReview(words)
        val weak = due.filter { it.correctCount + it.mistakeCount >= 2 && it.accuracyPercent < 60 }
        return when {
            due.isEmpty() -> AdaptiveLearningAdvice(
                title = "Yeni konulara hazırsın",
                message = "Zamanı gelen kelime tekrarı yok. Bir sonraki dersine devam edebilirsin.",
                focusWords = emptyList()
            )
            weak.isNotEmpty() -> AdaptiveLearningAdvice(
                title = "Önce zorlandığın kelimeler",
                message = "Başarı oranı düşük kelimeleri kısa aralıklarla yeniden çalış. Ardından yeni derse geç.",
                focusWords = weak.take(5).map { it.word }
            )
            due.any { it.streak == 0 || it.mistakeCount > 0 } -> AdaptiveLearningAdvice(
                title = "Tekrarla ve pekiştir",
                message = "Bazı kelimelerde hata geçmişin var. Önce tekrarları tamamla, sonra yeni konuya geç.",
                focusWords = due.take(5).map { it.word }
            )
            else -> AdaptiveLearningAdvice(
                title = "Bilgini kalıcılaştır",
                message = "Zamanı gelen kelimeleri tekrar et; doğru hatırladıkların daha uzun aralıklarla karşına çıkar.",
                focusWords = due.take(5).map { it.word }
            )
        }
    }
}
