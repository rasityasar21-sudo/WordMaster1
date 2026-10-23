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
            listOf("b1-opinions")),
        CourseLesson("a1-numbers-time", "Sayılar, saatler ve tarihler", "A1", "Kelime", 10,
            listOf("Sayıları ve sıra sayılarını tanı", "Saat ve tarih söyle"), listOf("a1-daily-words")),
        CourseLesson("a1-there-is-are", "There is / There are", "A1", "Dil bilgisi", 10,
            listOf("Bir yerde ne olduğunu anlat", "Tekil ve çoğul yapıyı ayırt et"), listOf("a1-pronouns")),
        CourseLesson("a1-present-simple", "Geniş zaman: günlük alışkanlıklar", "A1", "Dil bilgisi", 12,
            listOf("Olumlu ve olumsuz rutin cümleleri kur", "Do / does sorularını tanı"), listOf("a1-routine")),
        CourseLesson("a1-prepositions", "Yer edatları", "A1", "Dil bilgisi", 10,
            listOf("In, on, under, next to kullanımını öğren", "Konum tarif et"), listOf("a1-questions")),
        CourseLesson("a1-countable", "Sayılabilen ve sayılamayan isimler", "A1", "Dil bilgisi", 12,
            listOf("Some ve any kullanımını tanı", "Temel miktarları ifade et"), listOf("a1-daily-words")),
        CourseLesson("a1-family", "Aile ve insanları tanımlama", "A1", "Kelime", 10,
            listOf("Aile üyelerini tanıt", "Temel sıfatlarla kişileri anlat"), listOf("a1-greetings")),
        CourseLesson("a1-articles", "A / an ve tekil isimler", "A1", "Dil bilgisi", 10,
            listOf("A ve an arasındaki farkı uygula", "Tekil sayılabilen isimleri doğru kullan"), listOf("a1-countable")),
        CourseLesson("a1-adjectives", "Temel sıfatlar ve betimleme", "A1", "Kelime", 10,
            listOf("Renk, boyut ve kişilik sıfatlarını kullan", "Bir kişiyi veya nesneyi iki cümleyle tarif et"), listOf("a1-family")),
        CourseLesson("a1-can-cant", "Can / can't ile yetenekler", "A1", "Dil bilgisi", 10,
            listOf("Yetenek ve yapabilme durumunu ifade et", "Can ile soru ve olumsuz cümle kur"), listOf("a1-pronouns")),
        CourseLesson("a2-present-continuous", "Şimdiki zaman", "A2", "Dil bilgisi", 12,
            listOf("Şu anda olan eylemleri anlat", "Present simple ile farkını ayırt et"), listOf("a1-present-simple")),
        CourseLesson("a2-past-simple", "Geçmiş zaman: düzenli ve düzensiz fiiller", "A2", "Dil bilgisi", 14,
            listOf("Geçmiş olayları sırala", "Sık kullanılan düzensiz fiilleri uygula"), listOf("a2-past")),
        CourseLesson("a2-future", "Gelecek planları: going to / will", "A2", "Dil bilgisi", 14,
            listOf("Plan ve tahminleri ifade et", "İki gelecek yapısını karşılaştır"), listOf("a2-past")),
        CourseLesson("a2-modals", "Can, must ve have to", "A2", "Dil bilgisi", 12,
            listOf("Yetenek ve zorunluluk anlat", "Kural ve izin cümleleri kur"), listOf("a2-shopping")),
        CourseLesson("a2-directions", "Yol tarifi ve şehirde yön bulma", "A2", "Konuşma", 12,
            listOf("Kibarca yön sor", "Bir yere nasıl gidileceğini anlat"), listOf("a2-shopping")),
        CourseLesson("a2-food", "Yemek siparişi ve miktarlar", "A2", "Konuşma", 10,
            listOf("Restoranda sipariş ver", "Miktar ve tercih belirt"), listOf("a2-shopping")),
        CourseLesson("a2-adverbs", "Sıklık zarfları ve zaman ifadeleri", "A2", "Dil bilgisi", 10,
            listOf("Always, usually ve rarely kullan", "Alışkanlık sıklığını anlat"), listOf("a2-present-continuous")),
        CourseLesson("a2-health", "Sağlık ve günlük sorunlar", "A2", "Konuşma", 12,
            listOf("Belirti ve basit ihtiyaçları anlat", "Eczane veya klinikte kısa konuşma kur"), listOf("a2-food")),
        CourseLesson("a2-travel", "Seyahat ve konaklama", "A2", "Konuşma", 12,
            listOf("Rezervasyon ve check-in ifadelerini kullan", "Seyahat planındaki temel bilgileri anla"), listOf("a2-directions")),
        CourseLesson("b1-conditionals", "Birinci ve ikinci koşul cümleleri", "B1", "Dil bilgisi", 16,
            listOf("Gerçekçi olasılıkları ifade et", "Varsayımsal durumları tartış"), listOf("b1-experiences")),
        CourseLesson("b1-passive", "Edilgen yapı", "B1", "Dil bilgisi", 16,
            listOf("Eylem ve eylemi yapanı ayırt et", "Present ve past passive kur"), listOf("b1-experiences")),
        CourseLesson("b1-reported-speech", "Dolaylı anlatım", "B1", "Dil bilgisi", 16,
            listOf("Söylenenleri aktar", "Zamir ve zaman uyumunu uygula"), listOf("b1-opinions")),
        CourseLesson("b1-relative-clauses", "Relative clauses: who, which, that", "B1", "Dil bilgisi", 14,
            listOf("İsimleri ayrıntılı tanımla", "İlgi cümleciklerini doğru bağla"), listOf("b1-opinions")),
        CourseLesson("b1-phrasal-verbs", "Yaygın phrasal verbs", "B1", "Kelime", 15,
            listOf("Bağlama göre fiil anlamını çıkar", "Günlük ifadelerde kullan"), listOf("b1-experiences")),
        CourseLesson("b1-email-writing", "Profesyonel e-posta yazımı", "B1", "Yazma", 15,
            listOf("Uygun selamlama ve kapanış kullan", "Kısa ve net e-posta yaz"), listOf("b1-opinions")),
        CourseLesson("b1-listening-details", "Dinlemede ayrıntı yakalama", "B1", "Dinleme", 15,
            listOf("Ana fikir ve ayrıntıyı ayırt et", "Sayı, tarih ve isimleri not al"), listOf("a1-listening")),
        CourseLesson("b1-problem-solving", "Sorun açıklama ve çözüm önerme", "B1", "Konuşma", 15,
            listOf("Sorunu açık ve sıralı biçimde anlat", "Olası çözümü gerekçesiyle öner"), listOf("b1-opinions")),
        CourseLesson("b1-workplace", "İş yerinde iletişim", "B1", "Mesleki iletişim", 15,
            listOf("Görev ve sorumlulukları açıklamak", "Kibarca açıklama ve tekrar istemek"), listOf("b1-email-writing")),
        CourseLesson("b1-narratives", "Olay anlatımı ve bağlaçlar", "B1", "Yazma", 14,
            listOf("Olayları kronolojik sıraya koy", "Although, however ve therefore kullan"), listOf("b1-experiences")),
        CourseLesson("b2-perfect-continuous", "Perfect continuous yapıları", "B2", "Dil bilgisi", 18,
            listOf("Süreç ve süre vurgusunu anlat", "Perfect yapılar arasındaki farkı seç"), listOf("b1-experiences")),
        CourseLesson("b2-inversion", "Vurgu ve inversion", "B2", "Dil bilgisi", 18,
            listOf("Resmî metinlerde vurgulu yapıları tanı", "Inversion örneklerini yorumla"), listOf("b2-arguments")),
        CourseLesson("b2-academic-writing", "Akademik paragraf ve argüman", "B2", "Yazma", 20,
            listOf("Topic sentence ve kanıt kullan", "Karşı görüşe dengeli yanıt ver"), listOf("b2-arguments")),
        CourseLesson("b2-idioms-register", "Deyimler ve dil düzeyi", "B2", "Kelime", 16,
            listOf("Resmî ve gündelik dili ayırt et", "Deyimleri bağlam içinde yorumla"), listOf("b1-phrasal-verbs")),
        CourseLesson("b2-critical-reading", "Eleştirel okuma ve çıkarım", "B2", "Okuma", 20,
            listOf("Yazarın varsayımını belirle", "Kanıt ile görüşü ayır"), listOf("b2-arguments")),
        CourseLesson("b2-meetings", "Toplantılarda fikir savunma", "B2", "Konuşma", 18,
            listOf("Söz alıp görüşünü desteklemek", "Karşı görüşe yapıcı yanıt vermek"), listOf("b1-opinions")),
        CourseLesson("b2-listening", "Uzun konuşmalarda ana fikir", "B2", "Dinleme", 18,
            listOf("Konuşmanın amacını ve tutumunu çıkar", "Önemli ayrıntıları notlarla özetle"), listOf("b1-listening-details")),
        CourseLesson("b2-reporting", "Veri ve sonuçları raporlama", "B2", "Yazma", 20,
            listOf("Veriyi ölçülü bir dille yorumla", "Sonuç ve önerileri yapılandır"), listOf("b2-academic-writing")),
        CourseLesson("b2-negotiation", "Müzakere ve uzlaşma", "B2", "Konuşma", 18,
            listOf("Öneri ve taviz ifadelerini kullan", "Ortak çözümü net biçimde özetle"), listOf("b2-arguments")),
        CourseLesson("c1-nuance", "Anlam incelikleri ve register", "C1", "Kelime", 20,
            listOf("Yakın anlamlı kelimelerin tonunu karşılaştır", "Bağlama uygun kelime seç"), listOf("b2-idioms-register")),
        CourseLesson("c1-advanced-grammar", "İleri düzey cümle yapıları", "C1", "Dil bilgisi", 22,
            listOf("Karmaşık cümleleri çözümle", "Vurgu ve bilgi akışını düzenle"), listOf("b2-perfect-continuous")),
        CourseLesson("c1-presentations", "Sunum ve ikna edici konuşma", "C1", "Konuşma", 20,
            listOf("Sunum akışını yapılandır", "Sorulara tutarlı yanıt ver"), listOf("b2-academic-writing")),
        CourseLesson("c1-synthesis", "Birden fazla kaynaktan sentez", "C1", "Okuma", 22,
            listOf("Kaynaklar arasındaki ortak noktaları bul", "Bilgiyi özgün biçimde sentezle"), listOf("b2-critical-reading")),
        CourseLesson("c1-formal-writing", "Resmî rapor ve öneri yazımı", "C1", "Yazma", 22,
            listOf("Raporu bölümlere ayır", "Önerileri gerekçeleriyle sun"), listOf("b2-academic-writing")),
        CourseLesson("c1-implicit-meaning", "İma, ton ve örtük anlam", "C1", "Okuma", 20,
            listOf("Doğrudan ifade ile imayı ayırt et", "Yazarın tutumunu bağlamdan çıkar"), listOf("c1-nuance")),
        CourseLesson("c1-debate", "İleri düzey tartışma", "C1", "Konuşma", 22,
            listOf("Karmaşık bir görüşü tutarlı savun", "Karşı savı değerlendirerek yanıtla"), listOf("c1-presentations")),
        CourseLesson("c1-editing", "Metin düzenleme ve üslup", "C1", "Yazma", 20,
            listOf("Gereksiz tekrarı ve belirsizliği gider", "Hedef kitleye uygun resmiyet seç"), listOf("c1-formal-writing")),
        CourseLesson("c1-listening", "İleri dinleme ve not çıkarma", "C1", "Dinleme", 22,
            listOf("Konuşmacının tutum ve çıkarımlarını ayırt et", "Yoğun bilgiyi kısa notlara dönüştür"), listOf("c1-synthesis")),
        CourseLesson("c1-professional-communication", "Profesyonel iletişim ve ikna", "C1", "Konuşma", 22,
            listOf("İkna edici fakat ölçülü dil kullan", "Beklenmedik sorulara yapılandırılmış yanıt ver"), listOf("c1-presentations")),
        CourseLesson("aviation-maintenance-basics", "Uçak bakımına giriş ve emniyet dili", "Mesleki", "Uçak Bakım İngilizcesi", 20,
            listOf("Bakım dokümanlarında temel emniyet ifadelerini tanı", "Talimatı okumadan önce uyarı ve ikazları ayırt et")),
        CourseLesson("aviation-tools", "Bakım aletleri ve ekipman", "Mesleki", "Uçak Bakım İngilizcesi", 20,
            listOf("Yaygın el aletlerini İngilizce adlandır", "Alet ile kullanım amacını eşleştir"), listOf("aviation-maintenance-basics")),
        CourseLesson("aviation-airframe", "Gövde ve yapısal parçalar", "Mesleki", "Uçak Bakım İngilizcesi", 22,
            listOf("Fuselage, skin, rib ve spar terimlerini tanı", "Basit teknik açıklamayı anla"), listOf("aviation-maintenance-basics")),
        CourseLesson("aviation-flight-controls", "Uçuş kumandaları", "Mesleki", "Uçak Bakım İngilizcesi", 22,
            listOf("Aileron, elevator ve rudder terimlerini ayırt et", "Kumanda yüzeylerinin işlevini İngilizce açıkla"), listOf("aviation-airframe")),
        CourseLesson("aviation-powerplant", "Motor ve güç ünitesi terimleri", "Mesleki", "Uçak Bakım İngilizcesi", 22,
            listOf("Engine, compressor, turbine ve exhaust terimlerini tanı", "Motor açıklamalarındaki temel bilgiyi çıkar"), listOf("aviation-maintenance-basics")),
        CourseLesson("aviation-hydraulics", "Hidrolik sistemler", "Mesleki", "Uçak Bakım İngilizcesi", 22,
            listOf("Pressure, reservoir, pump ve leak terimlerini öğren", "Sistem durumunu basit İngilizceyle tarif et"), listOf("aviation-tools")),
        CourseLesson("aviation-electrical", "Elektrik ve kablolama", "Mesleki", "Uçak Bakım İngilizcesi", 22,
            listOf("Wire, connector, circuit ve continuity terimlerini tanı", "Teknik metinde elektrikle ilgili ifadeleri anla"), listOf("aviation-tools")),
        CourseLesson("aviation-inspection", "Kontrol, bulgu ve hasar ifadeleri", "Mesleki", "Uçak Bakım İngilizcesi", 25,
            listOf("Inspect, check, crack, corrosion ve damage terimlerini ayırt et", "Gözlemi açık ve tarafsız biçimde raporla"), listOf("aviation-airframe")),
        CourseLesson("aviation-documentation", "Bakım kayıtları ve teknik dokümanlar", "Mesleki", "Uçak Bakım İngilizcesi", 25,
            listOf("Work order, logbook ve maintenance manual ifadelerini tanı", "Yapılan işi kısa ve net İngilizceyle kaydet"), listOf("aviation-inspection")),
        CourseLesson("aviation-tool-control", "Alet kontrolü ve yabancı cisim önleme", "Mesleki", "Uçak Bakım İngilizcesi", 20,
            listOf("Tool inventory ve foreign object debris terimlerini tanı", "Kontrol listesi dilini anla"), listOf("aviation-tools")),
        CourseLesson("aviation-defect-report", "Arıza ve bulgu raporu yazımı", "Mesleki", "Uçak Bakım İngilizcesi", 25,
            listOf("Belirti, bulgu ve eylemi birbirinden ayır", "Varsayım eklemeden teknik rapor yaz"), listOf("aviation-inspection", "aviation-documentation"))

        ,
        CourseLesson("aviation-human-factors", "Human factors ve emniyet iletişimi", "Mesleki", "Emniyet İngilizcesi", 25,
            listOf("Fatigue, workload ve situational awareness terimlerini ayırt et", "Endişeyi açık ve saygılı biçimde bildir"), listOf("aviation-maintenance-basics")),
        CourseLesson("aviation-warning-language", "Uyarı, ikaz ve zorunluluk dili", "Mesleki", "Teknik doküman okuma", 20,
            listOf("Warning, caution ve note ifadelerini ayırt et", "Must, should ve may sözcüklerinin bağlamdaki işlevini tanı"), listOf("aviation-documentation")),
        CourseLesson("aviation-measurements", "Ölçüler, toleranslar ve birimler", "Mesleki", "Teknik İngilizce", 25,
            listOf("Length, diameter, clearance ve tolerance terimlerini öğren", "Ölçüm sonuçlarını birim belirterek ifade et"), listOf("aviation-tools")),
        CourseLesson("aviation-fasteners", "Bağlantı elemanları ve donanım", "Mesleki", "Parça terminolojisi", 22,
            listOf("Bolt, nut, washer, fastener ve thread terimlerini tanı", "Parça adlarını teknik açıklamada kullan"), listOf("aviation-tools")),
        CourseLesson("aviation-materials", "Malzemeler ve korozyon terminolojisi", "Mesleki", "Yapısal İngilizce", 22,
            listOf("Alloy, composite, corrosion ve coating terimlerini tanı", "Malzeme durumunu tarafsız biçimde açıkla"), listOf("aviation-airframe")),
        CourseLesson("aviation-pneumatics", "Pnömatik sistemler ve hava akışı", "Mesleki", "Sistem terminolojisi", 22,
            listOf("Air supply, duct, valve ve pressure terimlerini öğren", "Sistem açıklamalarında akış ve basınç sözcüklerini tanı"), listOf("aviation-hydraulics")),
        CourseLesson("aviation-avionics", "Aviyonik ve iletişim sistemleri", "Mesleki", "Aviyonik İngilizcesi", 25,
            listOf("Sensor, display, signal ve circuit terimlerini ayırt et", "Teknik açıklamadaki sistem ilişkilerini özetle"), listOf("aviation-electrical")),
        CourseLesson("aviation-engine-inspection", "Motor kontrolü ve durum ifadeleri", "Mesleki", "Motor İngilizcesi", 25,
            listOf("Wear, leak, debris ve abnormal noise terimlerini öğren", "Gözlenen bulguyu tahmin eklemeden ifade et"), listOf("aviation-powerplant")),
        CourseLesson("aviation-task-cards", "Task card ve iş emri okuma", "Mesleki", "Dokümantasyon", 25,
            listOf("Task, reference, sign-off ve record alanlarını tanı", "İş emrindeki bilgi türlerini birbirinden ayır"), listOf("aviation-documentation")),
        CourseLesson("aviation-handover", "Vardiya teslimi ve ekip iletişimi", "Mesleki", "Mesleki konuşma", 20,
            listOf("Work completed, pending item ve handover ifadelerini kullan", "Tamamlanan ve bekleyen işleri net biçimde özetle"), listOf("aviation-defect-report")),
        CourseLesson("aviation-technical-reading", "Teknik metin okuma stratejileri", "Mesleki", "Teknik okuma", 25,
            listOf("Başlık, uyarı, adım ve referansları sırayla incele", "Bilmediğin terimi bağlamdan çıkarıp doğrulama ihtiyacını belirle"), listOf("aviation-warning-language")),
        CourseLesson("aviation-review-assessment", "Mesleki terimler ve tekrar değerlendirmesi", "Mesleki", "Değerlendirme", 20,
            listOf("Bakım, yapı, motor ve dokümantasyon terimlerini tekrar et", "Terimleri kısa teknik cümlelerde doğru kullan"), listOf("aviation-human-factors", "aviation-measurements", "aviation-avionics"))

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
    /** Lesson estimates plus one minute for each scheduled vocabulary review. */
    val estimatedMinutes: Int
        get() = lessonIds.mapNotNull(WordMasterCourseCatalog::find).sumOf { it.estimatedMinutes } + reviewWords.size

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
            // Reserve up to one third of the target (maximum five minutes) for due words.
            // This keeps the plan review-first without letting reviews consume the whole session.
            val plannedReviewWords = dueWords.take(minOf(5, safeTarget / 3, dueWords.size)).map { it.word }
            val lessonBudget = (safeTarget - plannedReviewWords.size).coerceAtLeast(5)
            val chosen = mutableListOf<String>()
            val targetLessons = WordMasterCourseCatalog.forLevel(level)
                .filter { it.id !in completedLessonIds }

            // If the selected level is blocked by earlier lessons, guide the learner
            // to the missing prerequisites instead of showing an empty daily plan.
            fun addPrerequisites(lesson: CourseLesson) {
                lesson.prerequisiteIds.forEach { prerequisiteId ->
                    if (prerequisiteId !in completedLessonIds && prerequisiteId !in chosen) {
                        WordMasterCourseCatalog.find(prerequisiteId)
                            ?.takeIf { !it.level.equals(level, ignoreCase = true) }
                            ?.let { prerequisite ->
                                addPrerequisites(prerequisite)
                                if (prerequisite.id !in completedLessonIds && prerequisite.id !in chosen) {
                                    chosen += prerequisite.id
                                }
                            }
                    }
                }
            }
            targetLessons.forEach(::addPrerequisites)

            // Keep prerequisite lessons first, then consider selected-level lessons
            // in catalog order. A lesson is added only after its prerequisites are
            // completed or already included earlier in today's plan.
            val candidates = (chosen.toList() + targetLessons.map { it.id }).distinct()
            val selectedLessons = candidates.mapNotNull(WordMasterCourseCatalog::find)
            val chosenForPlan = mutableListOf<String>()
            val targetLessonIds = targetLessons.map { it.id }.toSet()
            var minutes = 0
            var targetLessonAdded = false
            for (lesson in selectedLessons) {
                if (targetLessonAdded && minutes >= lessonBudget) break
                val prerequisitesSatisfied = lesson.prerequisiteIds.all {
                    it in completedLessonIds || it in chosenForPlan
                }
                if (!prerequisitesSatisfied) continue
                chosenForPlan += lesson.id
                minutes += lesson.estimatedMinutes
                if (lesson.id in targetLessonIds) targetLessonAdded = true
            }
            return DailyStudyPlan(
                date = date,
                targetMinutes = safeTarget,
                lessonIds = chosenForPlan,
                reviewWords = plannedReviewWords
            )
        }
    }
}
