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
                "Temel bakım ve atölye terimleri",
                "Gövde (airframe) ve motor (powerplant) sistemleri",
                "Aletler, ölçüm ve bakım dokümanları",
                "Inspection, defect reporting and work orders",
                "Safety instructions, warnings and human factors",
                "Teknik metin okuma ve Türkçe açıklama",
                "İş yeri diyalogları ve teknik rapor yazımı"
            ),
            sampleTerms = listOf(
                ProfessionalTerm("maintenance", "bakım", "Maintenance must follow the approved procedure."),
                ProfessionalTerm("inspection", "muayene / kontrol", "The inspection is required before release."),
                ProfessionalTerm("airframe", "uçak gövdesi ve yapısal bölümleri", "Inspect the airframe for visible damage."),
                ProfessionalTerm("powerplant", "motor / güç grubu", "The powerplant chapter explains engine systems."),
                ProfessionalTerm("fastener", "sabitleme elemanı", "Check that the fastener is secure."),
                ProfessionalTerm("corrosion", "korozyon", "Report any sign of corrosion."),
                ProfessionalTerm("work order", "iş emri", "Record the task in the work order."),
                ProfessionalTerm("defect", "arıza / kusur", "Document the defect clearly."),
                ProfessionalTerm("torque", "tork", "Use the value specified in the approved manual."),
                ProfessionalTerm("serviceable", "kullanıma elverişli", "Confirm serviceability using the approved criteria.")
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
            topics = listOf("Hukuki terimler", "Sözleşmeler", "Resmî yazışma", "Metin okuma")
        ),
        ProfessionalTrack(
            id = "education",
            title = "Eğitim İngilizcesi",
            titleEnglish = "Education English",
            description = "Sınıf içi iletişim, eğitim terminolojisi ve akademik beceriler.",
            topics = listOf("Sınıf dili", "Ders anlatımı", "Ölçme ve değerlendirme", "Akademik kelimeler")
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
