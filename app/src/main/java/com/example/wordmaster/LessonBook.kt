package com.example.wordmaster

data class LessonBookChapter(
    val purpose: String,
    val explanation: String,
    val formation: String,
    val examples: List<Pair<String, String>>,
    val commonMistakes: List<String>,
    val checkpoints: List<String>
)

/**
 * Extended, book-style explanations that complement the lesson's existing examples and quiz.
 * The title is used to select a focused chapter; unknown titles receive a safe study method.
 */
object LessonBook {
    fun chapter(title: String): LessonBookChapter {
        val t = title.lowercase()
        return when {
            "zamir" in t || "pronoun" in t || "be" in t ->
                LessonBookChapter("Cümlede özneyi doğru belirlemek",
                    "İngilizcede çoğu tam cümlede özne ve fiil bulunur. I, you, he, she, it, we ve they özneyi gösterir. Be fiili özneye göre am, is veya are biçimini alır. Olumsuzluk için be fiilinden sonra not gelir; soru oluştururken be fiili öznenin önüne geçer.",
                    "Olumlu: Özne + am/is/are + tamamlayıcı\nOlumsuz: Özne + am/is/are + not + tamamlayıcı\nSoru: Am/Is/Are + özne + tamamlayıcı?",
                    listOf("I am ready." to "Hazırım.", "She is not at home." to "Evde değil.", "Are they students?" to "Onlar öğrenci mi?"),
                    listOf("I is demek yerine I am kullan.", "He/She/It ile are değil is kullan.", "Soru cümlesinde be fiilini öznenin önüne getir."),
                    listOf("Özne ile be fiilini eşleştir.", "Olumsuz cümlede not konumunu kontrol et.", "Soruda yardımcı fiilin başa geldiğini doğrula."))
            "past" in t || "geçmiş" in t ->
                LessonBookChapter("Geçmiş zamanı bağlam içinde kullanmak",
                    "Geçmiş zaman, konuşma anından önce gerçekleşmiş olayları veya geçmişteki durumları anlatır. Zaman ifadesi bağlamı netleştirir. Düzenli fiiller çoğunlukla -ed alırken go/went ve see/saw gibi düzensiz fiillerin biçimleri öğrenilmelidir. Did kullanılan olumsuz ve soru cümlelerinde ana fiil yalın hâle döner.",
                    "Olumlu: Özne + V2\nOlumsuz: Özne + did not + V1\nSoru: Did + özne + V1?",
                    listOf("We visited the museum yesterday." to "Dün müzeyi ziyaret ettik.", "He didn't go to school." to "Okula gitmedi.", "Did you see the sign?" to "İşareti gördün mü?"),
                    listOf("Didn't went değil didn't go kullan.", "Was/were ile normal fiilin ikinci hâlini aynı yapıda gereksiz yere birleştirme.", "Geçmiş zaman ifadesi varsa fiilin zamanını kontrol et."),
                    listOf("Cümlenin geçmişte mi olduğunu belirle.", "Did varsa fiili yalın hâlde yaz.", "Düzensiz fiilin doğru biçimini seç."))
            "present perfect" in t || "deneyim" in t ->
                LessonBookChapter("Geçmiş ile şimdi arasındaki bağlantı",
                    "Present Perfect, kesin zamanı söylenmeyen deneyimlerde ve geçmişte başlayıp bugüne etkisi ulaşan durumlarda kullanılır. Have/has yardımcı fiilini fiilin üçüncü hâli izler. Yesterday gibi bitmiş ve belirli geçmiş zaman ifadeleriyle genellikle Simple Past tercih edilir.",
                    "Olumlu: Özne + have/has + V3\nOlumsuz: Özne + have/has not + V3\nSoru: Have/Has + özne + V3?",
                    listOf("I have finished my homework." to "Ödevimi bitirdim.", "She has never flown abroad." to "Hiç yurt dışına uçmadı.", "Have you ever seen snow?" to "Hiç kar gördün mü?"),
                    listOf("He has went değil he has gone kullan.", "I has değil I have kullan.", "Kesin geçmiş zaman belirtilmişse Simple Past gerekip gerekmediğini kontrol et."),
                    listOf("Özneye göre have/has seç.", "Fiilin üçüncü hâlini kullan.", "Cümlenin zaman bağlamını kontrol et."))
            "question" in t || "soru" in t ->
                LessonBookChapter("Bilgi almak için doğru soru kurmak",
                    "Wh- soruları belirli bir bilgi ister: what (ne), where (nerede), when (ne zaman), who (kim), why (neden), how (nasıl). Be fiili varsa öznenin önüne gelir. Diğer geniş zaman sorularında do/does kullanılır; does ile birlikte ana fiil yalın kalır.",
                    "Wh-word + do/does + özne + V1?\nBe sorusu: Am/Is/Are + özne ...?",
                    listOf("Where do you live?" to "Nerede yaşıyorsun?", "What does she want?" to "O ne istiyor?", "Why are they late?" to "Neden geç kaldılar?"),
                    listOf("Does she likes değil does she like kullan.", "What/where kelimesinden sonra her zaman doğrudan fiil gelmediğini unutma.", "Be fiili olan cümleye gereksiz do ekleme."),
                    listOf("Sorunun hangi bilgiyi istediğini belirle.", "Do/does veya be yapısından uygun olanı seç.", "Does sonrasında ana fiili yalın tut."))
            "future" in t || "gelecek" in t ->
                LessonBookChapter("Plan, karar ve tahmini ayırt etmek",
                    "Will çoğunlukla anlık karar, söz verme veya tahminlerde kullanılır. Be going to önceden yapılmış planlarda ve mevcut kanıta dayalı tahminlerde sık görülür. Günlük kullanımda bağlam önemlidir; bazı durumlarda iki yapı da mümkün olabilir.",
                    "Will: Özne + will + V1\nGoing to: Özne + am/is/are + going to + V1",
                    listOf("I'll help you." to "Sana yardım edeceğim.", "We are going to study tonight." to "Bu akşam ders çalışacağız.", "Look at the clouds; it is going to rain." to "Bulutlara bak; yağmur yağacak."),
                    listOf("Will sonrası fiile -s veya to ekleme.", "Going to yapısında be fiilini unutma.", "Plan ile anlık karar arasındaki bağlamı oku."),
                    listOf("Kararın ne zaman verildiğini düşün.", "Yapıyı özneye göre kur.", "Ana fiili yalın kullan."))
            "conditional" in t || "koşul" in t ->
                LessonBookChapter("Koşul ve sonuç ilişkisi kurmak",
                    "Koşul cümleleri bir durum gerçekleşirse ne olacağını anlatır. First Conditional gerçekçi gelecek olasılıkları için if + Present Simple ve will + V1 kullanır. Second Conditional şu an veya gelecek için varsayımsal durumları if + Past Simple ve would + V1 ile anlatır.",
                    "First: If + Present Simple, will + V1\nSecond: If + Past Simple, would + V1",
                    listOf("If it rains, we will stay home." to "Yağmur yağarsa evde kalacağız.", "If I had more time, I would travel." to "Daha çok zamanım olsa seyahat ederdim."),
                    listOf("If bölümünde gelecek anlamı olsa bile genellikle will kullanma.", "Would sonrasında fiili yalın kullan.", "First ve Second Conditional arasındaki gerçekçilik farkını gözet."),
                    listOf("Koşulun gerçekçi olup olmadığını belirle.", "If bölümündeki zamanı seç.", "Sonuç bölümündeki yardımcı fiili kontrol et."))
            "passive" in t || "edilgen" in t ->
                LessonBookChapter("Eylemden etkilenen unsuru öne çıkarmak",
                    "Edilgen yapı, eylemi yapan kişiden çok eylemden etkilenen kişi veya şey önemli olduğunda kullanılır. Be fiili cümlenin zamanına göre değişir; ana fiil V3 biçimindedir. Eylemi yapan kişi gerekliyse by ile eklenebilir.",
                    "Özne + uygun be biçimi + V3 (+ by + yapan)",
                    listOf("The runway was repaired." to "Pist onarıldı.", "English is spoken here." to "Burada İngilizce konuşulur.", "The report will be checked." to "Rapor kontrol edilecek."),
                    listOf("Be fiilini unutma.", "V3 yerine V1 kullanma.", "Özne tekil/çoğul olduğu için be biçimini uyumlu seç."),
                    listOf("Nesnenin yeni özne olduğunu bul.", "Zamana uygun be biçimini seç.", "Ana fiilin V3 biçimini kullan."))
            "compar" in t || "karşılaştır" in t ->
                LessonBookChapter("İki veya daha fazla şeyi karşılaştırmak",
                    "Kısa sıfatlar genellikle -er alır ve than ile karşılaştırılır. Uzun sıfatlarda more kullanılır. Üstünlük bildirirken the -est veya the most yapısı tercih edilir. Good/bad gibi bazı sıfatların düzensiz biçimleri vardır.",
                    "Kısa sıfat: adjective-er + than\nUzun sıfat: more + adjective + than\nÜstünlük: the + adjective-est / the most + adjective",
                    listOf("This route is shorter than that one." to "Bu rota diğerinden daha kısa.", "This task is more difficult." to "Bu görev daha zor.", "It is the safest option." to "En güvenli seçenek bu."),
                    listOf("More easier gibi iki karşılaştırma işaretini birlikte kullanma.", "Than ile the most yapılarını karıştırma.", "Good → better → best değişimini öğren."),
                    listOf("Sıfatın kısa mı uzun mu olduğunu değerlendir.", "Than veya üstünlük yapısını seç.", "Düzensiz sıfatları kontrol et."))
            else ->
                LessonBookChapter("Konuyu adım adım öğren",
                    "Önce konunun ne zaman ve neden kullanıldığını anla. Ardından temel kalıbı incele, örnek cümleleri İngilizce ve Türkçe karşılaştır, son olarak kalıbı kendi cümlende kullan. Bir kelimenin anlamı kadar cümledeki görevi ve diğer kelimelerle ilişkisi de önemlidir.",
                    "1. Kullanım amacını belirle\n2. Cümle kalıbını incele\n3. Örneği çözümle\n4. Kendi cümleni kur",
                    listOf("Read the example carefully." to "Örneği dikkatle oku.", "Write your own sentence." to "Kendi cümleni yaz."),
                    listOf("Kalıbı ezberleyip bağlamı görmezden gelme.", "Örneği yalnızca çevirmekle yetinme; yapıyı da incele.", "Yeni yapıyı kendi cümlende uygulamadan konuyu bitirme."),
                    listOf("Kullanım amacını kendi sözlerinle açıkla.", "Bir olumlu ve bir olumsuz örnek oluştur.", "Kendini kısa bir soruyla kontrol et."))
        }
    }
}
