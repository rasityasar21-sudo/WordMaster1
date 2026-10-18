package com.example.wordmaster

/**
 * Profession-specific English learning tracks.
 *
 * Premium is a product-access flag only; it does not process payments.
 * Actual purchases must be verified with Google Play Billing before access is granted.
 */
data class ProfessionalTrack(
    val id: String,
    val title: String,
    val titleEnglish: String,
    val description: String,
    val premiumOnly: Boolean = true,
    val sourceGuidance: List<String> = emptyList(),
    val topics: List<String> = emptyList(),
    val sampleTerms: List<ProfessionalTerm> = emptyList()
)

data class ProfessionalTerm(
    val english: String,
    val turkish: String,
    val exampleEnglish: String,
    val note: String = ""
)

object ProfessionalCourseCatalog {
    val tracks: List<ProfessionalTrack> = listOf(
        ProfessionalTrack(
            id = "aviation-maintenance",
            title = "Uçak Bakım İngilizcesi",
            titleEnglish = "Aircraft Maintenance English",
            description = "Uçak bakım terminolojisi, teknik dokümanları anlama, iş emri dili ve emniyet odaklı iletişim.",
            sourceGuidance = listOf(
                "FAA Aviation Maintenance Technician Handbook – General",
                "FAA Aviation Maintenance Technician Handbook – Airframe",
                "FAA Aviation Maintenance Technician Handbook – Powerplant",
                "EASA Part-66 learning objectives and approved training material",
                "Aircraft and component manufacturer documentation, where lawfully available"
            ),
            topics = listOf(
                "Temel bakım İngilizcesi ve atölye iletişimi",
                "Airframe: gövde ve yapısal parçalar",
                "Powerplant: motor ve güç grubu terimleri",
                "Bakım el kitapları, task card ve teknik kayıtlar",
                "Inspection: kontrol bulgularını ifade etme",
                "Defect reporting: arıza ve hasar raporlama dili",
                "Ölçüm, tolerans, tork ve alet kontrolü terimleri",
                "Safety instructions, warnings ve human factors",
                "Bakım ekibiyle iletişim ve iş teslimi",
                "Teknik metin okuma ve Türkçe anlamlandırma",
                "İş emri ve bakım kayıtları için yazılı İngilizce",
                "Tekrar testi ve mesleki kelime değerlendirmesi"
            ),
            sampleTerms = listOf(
                ProfessionalTerm("maintenance", "bakım", "Maintenance must follow the approved procedure.", note = "Türkçesi: Bakım, onaylı prosedüre uygun yapılmalıdır."),
                ProfessionalTerm("inspection", "muayene / kontrol", "The inspection is required before release.", note = "Türkçesi: Hizmete verme öncesinde kontrol gereklidir."),
                ProfessionalTerm("airframe", "uçak gövdesi ve yapısal bölümleri", "Inspect the airframe for visible damage.", note = "Türkçesi: Uçak gövdesini görünür hasarlara karşı kontrol edin."),
                ProfessionalTerm("powerplant", "motor / güç grubu", "The powerplant chapter explains engine systems.", note = "Türkçesi: Motor bölümü, motor sistemlerini açıklar."),
                ProfessionalTerm("fastener", "sabitleme elemanı", "Check that the fastener is secure.", note = "Türkçesi: Sabitleme elemanının sağlam olduğunu kontrol edin."),
                ProfessionalTerm("corrosion", "korozyon", "Report any sign of corrosion.", note = "Türkçesi: Herhangi bir korozyon belirtisini bildirin."),
                ProfessionalTerm("work order", "iş emri", "Record the task in the work order.", note = "Türkçesi: İşi iş emrine kaydedin."),
                ProfessionalTerm("defect", "arıza / kusur", "Document the defect clearly.", note = "Türkçesi: Arızayı açık bir şekilde kaydedin."),
                ProfessionalTerm("torque", "tork", "Use the value specified in the approved manual.", note = "Türkçesi: Onaylı el kitabında belirtilen değeri kullanın."),
                ProfessionalTerm("serviceable", "kullanıma elverişli", "Confirm serviceability using the approved criteria.", note = "Türkçesi: Kullanıma elverişliliği onaylı ölçütlere göre doğrulayın."),
                ProfessionalTerm("airworthiness", "uçuşa elverişlilik", "Airworthiness records must be kept up to date."),
                ProfessionalTerm("maintenance manual", "bakım el kitabı", "Always consult the current approved maintenance manual."),
                ProfessionalTerm("technical log", "teknik kayıt defteri", "Record the reported defect in the technical log."),
                ProfessionalTerm("component", "bileşen / parça", "Identify the component using its part number."),
                ProfessionalTerm("part number", "parça numarası", "Check the part number against the approved documentation."),
                ProfessionalTerm("serial number", "seri numarası", "Write the serial number on the record."),
                ProfessionalTerm("workshop", "atölye", "The component was sent to the workshop for inspection."),
                ProfessionalTerm("lubrication", "yağlama", "The procedure specifies the lubrication points."),
                ProfessionalTerm("leakage", "sızıntı", "Report any evidence of fluid leakage."),
                ProfessionalTerm("wear", "aşınma", "The inspection found signs of wear."),
                ProfessionalTerm("crack", "çatlak", "Document any crack according to the approved procedure."),
                ProfessionalTerm("clearance", "boşluk / açıklık", "Measure the clearance as described in the manual."),
                ProfessionalTerm("measurement", "ölçüm", "Record the measurement and the specified limit."),
                ProfessionalTerm("tool control", "alet kontrolü", "Complete tool control before closing the task."),
                ProfessionalTerm("safety wire", "emniyet teli", "The illustrated procedure shows the safety wire arrangement."),
                ProfessionalTerm("fastening", "sabitleme", "Inspect the fastening in accordance with the task card."),
                ProfessionalTerm("task card", "iş kartı", "Sign the task card only after completing the required checks."),
                ProfessionalTerm("non-routine task", "rutin dışı iş", "Raise a non-routine task when additional work is identified."),
                ProfessionalTerm("release to service", "servise verme / hizmete dönüş onayı", "Release to service requires the applicable authorized process."),
                ProfessionalTerm("hazard", "tehlike", "Report the hazard through the approved safety process."),
                ProfessionalTerm("warning", "uyarı", "Read every warning before starting a task."),
                ProfessionalTerm("personal protective equipment", "kişisel koruyucu donanım", "Use the personal protective equipment required by the procedure."),
                ProfessionalTerm("foreign object debris", "yabancı cisim kaynaklı kalıntı/hasar", "Follow the local process for foreign object debris prevention."),
                ProfessionalTerm("troubleshooting", "arıza teşhisi", "Use the approved troubleshooting procedure to investigate the reported fault.")
            )
        ),
        ProfessionalTrack(
            id = "healthcare",
            title = "Sağlık İngilizcesi",
            titleEnglish = "Healthcare English",
            description = "Hasta iletişimi, temel tıbbi terimler ve klinik ortamda İngilizce.",
            topics = listOf("Hasta karşılama", "Belirti ve şikâyetler", "Tıbbi terimler", "Hasta güvenliği"),
            sampleTerms = listOf(
            ProfessionalTerm("symptom", "belirti", "Tell me about your symptoms."),
            ProfessionalTerm("appointment", "randevu", "Your appointment is at ten o’clock."),
            ProfessionalTerm("patient", "hasta", "The patient is waiting in the room."),
            ProfessionalTerm("recovery", "iyileşme", "The patient is making a good recovery.")
            )
        ),
        ProfessionalTrack(
            id = "software-it",
            title = "Yazılım ve Bilişim İngilizcesi",
            titleEnglish = "Software and IT English",
            description = "Teknik dokümantasyon, yazılım ekipleri ve hata raporları için İngilizce.",
            topics = listOf("Programlama terimleri", "Hata raporlama", "Teknik doküman okuma", "Toplantı ve kod inceleme"),
            sampleTerms = listOf(
            ProfessionalTerm("bug", "yazılım hatası", "We found a bug in the application."),
            ProfessionalTerm("deploy", "yayına almak", "We will deploy the update tonight."),
            ProfessionalTerm("database", "veritabanı", "The database stores user information."),
            ProfessionalTerm("debug", "hata ayıklamak", "We need to debug this function.")
            )
        ),
        ProfessionalTrack(
            id = "engineering",
            title = "Mühendislik İngilizcesi",
            titleEnglish = "Engineering English",
            description = "Teknik çizimler, ölçüler, süreçler ve mühendislik raporları.",
            topics = listOf("Teknik çizimler", "Ölçüm ve toleranslar", "Malzeme ve üretim", "Teknik raporlama"),
            sampleTerms = listOf(
            ProfessionalTerm("measurement", "ölçüm", "Record the measurement in the report."),
            ProfessionalTerm("tolerance", "tolerans", "The part is within the specified tolerance."),
            ProfessionalTerm("material", "malzeme", "Check the material specification."),
            ProfessionalTerm("design", "tasarım", "The design must meet the requirements.")
            )
        ),
        ProfessionalTrack(
            id = "business",
            title = "İş ve Ofis İngilizcesi",
            titleEnglish = "Business English",
            description = "E-posta, toplantı, sunum ve profesyonel iletişim.",
            topics = listOf("Profesyonel e-posta", "Toplantılar", "Sunumlar", "Müşteri iletişimi"),
            sampleTerms = listOf(
            ProfessionalTerm("deadline", "son teslim tarihi", "The deadline is Friday."),
            ProfessionalTerm("meeting", "toplantı", "The meeting starts at nine."),
            ProfessionalTerm("invoice", "fatura", "Please send the invoice by email."),
            ProfessionalTerm("schedule", "program / takvim", "I will check the schedule.")
            )
        ),
        ProfessionalTrack(
            id = "hospitality",
            title = "Turizm ve Otelcilik İngilizcesi",
            titleEnglish = "Hospitality English",
            description = "Misafir karşılama, rezervasyon ve hizmet iletişimi.",
            topics = listOf("Misafir karşılama", "Rezervasyon", "Şikâyet çözme", "Yön tarif etme"),
            sampleTerms = listOf(
            ProfessionalTerm("reservation", "rezervasyon", "I would like to confirm my reservation."),
            ProfessionalTerm("guest", "misafir", "The guest needs an extra towel."),
            ProfessionalTerm("checkout", "çıkış işlemi", "Checkout is before noon."),
            ProfessionalTerm("available", "müsait / mevcut", "A single room is available.")
            )
        ),
        ProfessionalTrack(
            id = "logistics",
            title = "Lojistik İngilizcesi",
            titleEnglish = "Logistics English",
            description = "Sevkiyat, depo, teslimat ve tedarik zinciri terminolojisi.",
            topics = listOf("Sevkiyat belgeleri", "Depo işlemleri", "Teslimat durumları", "Tedarik zinciri"),
            sampleTerms = listOf(
            ProfessionalTerm("shipment", "sevkiyat", "The shipment will arrive tomorrow."),
            ProfessionalTerm("warehouse", "depo", "The goods are stored in the warehouse."),
            ProfessionalTerm("delivery", "teslimat", "The delivery is delayed."),
            ProfessionalTerm("inventory", "stok / envanter", "Update the inventory after receiving goods.")
            )
        ),
        ProfessionalTrack(
            id = "law",
            title = "Hukuk İngilizcesi",
            titleEnglish = "Legal English",
            description = "Hukuki metinleri ve profesyonel yazışmaları anlamaya yönelik dil çalışmaları.",
            topics = listOf("Hukuki terimler", "Sözleşmeler", "Resmî yazışma", "Metin okuma"),
            sampleTerms = listOf(
            ProfessionalTerm("agreement", "anlaşma", "Both parties signed the agreement."),
            ProfessionalTerm("clause", "madde", "Read the clause carefully."),
            ProfessionalTerm("evidence", "delil", "The lawyer reviewed the evidence."),
            ProfessionalTerm("claim", "iddia / talep", "The claim was reviewed by the court.")
            )
        ),
        ProfessionalTrack(
            id = "education",
            title = "Eğitim İngilizcesi",
            titleEnglish = "Education English",
            description = "Sınıf içi iletişim, eğitim terminolojisi ve akademik beceriler.",
            topics = listOf("Sınıf dili", "Ders anlatımı", "Ölçme ve değerlendirme", "Akademik kelimeler"),
            sampleTerms = listOf(
            ProfessionalTerm("assignment", "ödev", "Please submit your assignment tomorrow."),
            ProfessionalTerm("grade", "not", "Your grade is available online."),
            ProfessionalTerm("curriculum", "öğretim programı", "The curriculum includes language practice."),
            ProfessionalTerm("explain", "açıklamak", "Can you explain the answer?")
            )
        ),
        ProfessionalTrack(
            id = "automotive",
            title = "Otomotiv İngilizcesi",
            titleEnglish = "Automotive English",
            description = "Araç sistemleri, servis iletişimi ve teknik arıza açıklamaları.",
            topics = listOf("Araç parçaları", "Servis kabul", "Arıza tanımlama", "Bakım kayıtları"),
            sampleTerms = listOf(
            ProfessionalTerm("brake", "fren", "The mechanic inspected the brakes."),
            ProfessionalTerm("engine", "motor", "The engine needs an inspection."),
            ProfessionalTerm("tire", "lastik", "Check the tire pressure."),
            ProfessionalTerm("repair", "onarım", "The repair is complete.")
            )
        )
    )

    fun find(id: String): ProfessionalTrack? = tracks.firstOrNull { it.id == id }

    fun premiumTracks(): List<ProfessionalTrack> = tracks.filter { it.premiumOnly }
}
