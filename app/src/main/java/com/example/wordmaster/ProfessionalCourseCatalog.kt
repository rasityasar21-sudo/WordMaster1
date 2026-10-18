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
    val sampleTerms: List<ProfessionalTerm> = emptyList(),
    val lessons: List<ProfessionalLesson> = emptyList()
)

data class ProfessionalTerm(
    val english: String,
    val turkish: String,
    val exampleEnglish: String,
    val note: String = ""
)

data class ProfessionalLesson(
    val id: String,
    val title: String,
    val objective: String,
    val explanation: String,
    val phrases: List<Pair<String, String>>,
    val question: String,
    val answer: String
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
            lessons = listOf(
                ProfessionalLesson(
                    id = "maintenance-documentation",
                    title = "1. Bakım dokümanlarını anlama",
                    objective = "Bir bakım talimatının amacı, kapsamı ve kayıt gerekliliklerini ayırt etmek.",
                    explanation = "Teknik metinlerde must zorunluluğu, should tavsiyeyi, refer to ise başka bir dokümana başvurmayı ifade eder. Bir terimi anlamak tek başına işlem yapmak için yeterli değildir; ilgili uçak/komponent, doküman revizyonu ve yetkili prosedür kontrol edilmelidir.",
                    phrases = listOf(
                        "Refer to the current maintenance manual." to "Güncel bakım el kitabına başvurun.",
                        "Record the result in the task card." to "Sonucu iş kartına kaydedin.",
                        "The procedure must be followed." to "Prosedür izlenmelidir."
                    ),
                    question = "Teknik talimatta “must” genellikle ne ifade eder?",
                    answer = "Zorunlu bir gerekliliği ifade eder; gerçek bakımda daima geçerli onaylı doküman izlenmelidir."
                ),
                ProfessionalLesson(
                    id = "inspection-findings",
                    title = "2. Kontrol bulgularını tarif etme",
                    objective = "Görsel kontrol bulgusunu açık ve tarafsız İngilizceyle bildirmek.",
                    explanation = "Bulguyu yazarken konumu, gözlenen durumu ve ilgili referansı açıkça belirtmek gerekir. Tahmin yürütmek yerine gözlemleneni kaydetmek ve kurumun raporlama sürecini izlemek önemlidir.",
                    phrases = listOf(
                        "A crack was found near the bracket." to "Braketin yakınında bir çatlak bulundu.",
                        "Signs of corrosion were observed." to "Korozyon belirtileri gözlemlendi.",
                        "No visible damage was observed." to "Görünür bir hasar gözlemlenmedi."
                    ),
                    question = "Bir kontrol bulgusu raporlanırken en önemli dil ilkesi nedir?",
                    answer = "Konumu ve gözlemi açık, tarafsız ve doğrulanabilir biçimde yazmak; varsayımı gerçek gibi sunmamak."
                ),
                ProfessionalLesson(
                    id = "defect-reporting",
                    title = "3. Arıza ve iş emri dili",
                    objective = "Bir bildirilen arıza ile yapılan işi dil açısından birbirinden ayırmak.",
                    explanation = "Reported defect bildirilen arızayı, corrective action ise uygulanan düzeltici işlemi anlatır. Bu alanları karıştırmamak, kaydın izlenebilirliği için önemlidir. Öğrenci örnek cümleleri dil pratiği olarak kullanmalı; gerçek kayıtlar yetkili iş akışına göre hazırlanmalıdır.",
                    phrases = listOf(
                        "The pilot reported an intermittent fault." to "Pilot aralıklı bir arıza bildirdi.",
                        "The defect was entered in the technical log." to "Arıza teknik kayıt defterine işlendi.",
                        "The corrective action is recorded separately." to "Düzeltici işlem ayrı olarak kaydedilir."
                    ),
                    question = "“Reported defect” ile “corrective action” arasındaki fark nedir?",
                    answer = "Reported defect bildirilen sorundur; corrective action ise soruna yönelik yapılan düzeltici işlemdir."
                ),
                ProfessionalLesson(
                    id = "safety-language",
                    title = "4. Emniyet uyarıları ve ekip iletişimi",
                    objective = "Uyarı, tehlike ve açıklama ifadelerini doğru anlamak.",
                    explanation = "Warning, dikkat edilmezse ciddi sonuç doğurabilecek tehlikeye; caution ekipman hasarı veya prosedür riski gibi konulara; note ise ek bilgiye dikkat çeker. Kurum ve üretici dokümanlarındaki tanımlar esas alınmalıdır.",
                    phrases = listOf(
                        "Read the warning before proceeding." to "Devam etmeden önce uyarıyı okuyun.",
                        "Report the hazard to the supervisor." to "Tehlikeyi sorumlu kişiye bildirin.",
                        "Confirm the task status during handover." to "İş devri sırasında işin durumunu doğrulayın."
                    ),
                    question = "Bir emniyet uyarısını anlamadığında doğru yaklaşım nedir?",
                    answer = "İşleme devam etmek yerine açıklama istemek ve onaylı prosedürü/yetkili sorumluyu takip etmektir."
                ),
                ProfessionalLesson(
                    id = "measurement-language",
                    title = "5. Ölçüm ve tolerans ifadeleri",
                    objective = "Ölçüm değerleri, birimler ve kabul sınırları hakkında İngilizce cümleleri anlamak.",
                    explanation = "Measured value ölçülen değeri, specified limit belirtilen sınırı, within tolerance ise ilgili tolerans aralığında olmayı anlatır. Sayısal değer tek başına yeterli değildir; birim ve karşılaştırılacak güncel onaylı teknik referans da gerekir.",
                    phrases = listOf(
                        "Record the measured value." to "Ölçülen değeri kaydedin.",
                        "The value is outside the specified limit." to "Değer belirtilen sınırın dışındadır.",
                        "Refer to the approved data for the tolerance." to "Tolerans için onaylı teknik veriye başvurun."
                    ),
                    question = "Bir ölçüm değerini kaydederken sayıyla birlikte ne belirtilmelidir?",
                    answer = "Ölçü birimi ve değerin karşılaştırılacağı onaylı teknik referans."
                ),
                ProfessionalLesson(
                    id = "corrosion-and-wear",
                    title = "6. Korozyon ve aşınmayı tarif etme",
                    objective = "Gözlenen korozyon veya aşınmayı varsayım eklemeden ifade etmek.",
                    explanation = "Corrosion korozyon, wear aşınma, surface damage yüzey hasarı demektir. Raporlama dilinde gözlenen durum, konum ve varsa ilgili kayıt referansı açıkça belirtilir; neden veya ciddiyet hakkında kanıtsız çıkarım yapılmaz.",
                    phrases = listOf(
                        "Signs of corrosion were observed on the surface." to "Yüzeyde korozyon belirtileri gözlemlendi.",
                        "The area shows signs of wear." to "Bölgede aşınma belirtileri görülüyor.",
                        "Record the location of the finding." to "Bulgunun konumunu kaydedin."
                    ),
                    question = "Bir bulgu raporunda neden hakkında tahmin yürütmek yerine ne yazılmalıdır?",
                    answer = "Gözlemlenen durum, açık konum ve gerekli referans bilgisi."
                ),
                ProfessionalLesson(
                    id = "maintenance-handover",
                    title = "7. İş devri ve teknik iletişim",
                    objective = "Devam eden işin durumunu açık, izlenebilir ifadelerle aktarmak.",
                    explanation = "Handover iş devri, outstanding task tamamlanmamış iş, recorded finding kaydedilmiş bulgu anlamına gelir. Devir sırasında tamamlanan ve bekleyen işler birbirinden ayrılır; kritik bilgi yalnızca sözlü bırakılmayıp kurumun kayıt sürecine göre belgelenir.",
                    phrases = listOf(
                        "The inspection is complete; the record is pending." to "Kontrol tamamlandı; kayıt işlemi bekliyor.",
                        "This task remains outstanding." to "Bu iş henüz tamamlanmadı.",
                        "Please review the recorded findings." to "Lütfen kaydedilen bulguları gözden geçirin."
                    ),
                    question = "İş devrinde hangi iki durum açıkça ayrılmalıdır?",
                    answer = "Tamamlanan işler ile henüz tamamlanmamış veya takip bekleyen işler."
                )
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
            lessons = listOf(
                ProfessionalLesson("patient-communication", "Hasta iletişiminin temelleri", "Nazik ve açık sorularla temel bilgi istemek.", "Sağlık ortamında kısa, açık ve saygılı cümleler kullanılır. Öğrenme örnekleri gerçek klinik karar veya tanı yerine geçmez.", listOf("How are you feeling today?" to "Bugün nasıl hissediyorsunuz?", "When did the symptoms start?" to "Belirtiler ne zaman başladı?", "Could you please repeat that?" to "Lütfen tekrar edebilir misiniz?"), "Hasta bir ifadeyi anlamadığında ne yapılmalı?", "Nazikçe tekrar etmesi istenmeli; bilgi tahmin edilmemelidir."),
                ProfessionalLesson("patient-history", "Belirti ve öykü hakkında soru sorma", "Zamanı ve belirtiyi tarafsız sorularla öğrenmek.", "Sorular kısa ve anlaşılır olmalı; hastanın kendi ifadeleri mümkün olduğunca doğru aktarılmalıdır. Bilinmeyen bilgiler varsayımla tamamlanmamalıdır.", listOf("When did the pain begin?" to "Ağrı ne zaman başladı?", "Does anything make it better or worse?" to "Herhangi bir şey ağrıyı artırıyor veya azaltıyor mu?", "Are you taking any medication?" to "Herhangi bir ilaç kullanıyor musunuz?"), "Yanıt bilinmiyorsa nasıl kayıt tutulmalı?", "Bilinmeyen bilgi uydurulmamalı; uygun biçimde bilinmediği belirtilmelidir."),
                ProfessionalLesson("patient-instructions", "Açık ve nazik talimatlar", "Basit yönergeleri anlaşılır İngilizceyle ifade etmek.", "Talimatlar tek tek ve açık verilir; hastanın anladığını teyit etmek için geri anlatım istenebilir. Dil pratiği klinik protokolün yerini tutmaz.", listOf("Please sit here." to "Lütfen buraya oturun.", "Take a slow, deep breath." to "Yavaşça derin bir nefes alın.", "Could you tell me what you understood?" to "Ne anladığınızı söyleyebilir misiniz?"), "Anlaşılırlığı kontrol etmek için ne yapılabilir?", "Kişiden talimatı kendi sözleriyle tekrar anlatması nazikçe istenebilir.")
            ),
            sampleTerms = listOf(
            ProfessionalTerm("symptom", "belirti", "Tell me about your symptoms."),
            ProfessionalTerm("appointment", "randevu", "Your appointment is at ten o’clock."),
            ProfessionalTerm("patient", "hasta", "The patient is waiting in the room."),
            ProfessionalTerm("recovery", "iyileşme", "The patient is making a good recovery."),
            ProfessionalTerm("medical history", "tıbbi öykü", "Please tell me about your medical history."),
            ProfessionalTerm("allergy", "alerji", "Do you have any known allergies?"),
            ProfessionalTerm("prescription", "reçete", "Please bring your prescription."),
            ProfessionalTerm("follow-up", "kontrol randevusu / takip", "A follow-up appointment has been scheduled."),
            ProfessionalTerm("consent", "onam", "Please explain the form before asking for consent."),
            ProfessionalTerm("vital signs", "yaşam bulguları", "The nurse records the vital signs.")
            )
        ),
        ProfessionalTrack(
            id = "software-it",
            title = "Yazılım ve Bilişim İngilizcesi",
            titleEnglish = "Software and IT English",
            description = "Teknik dokümantasyon, yazılım ekipleri ve hata raporları için İngilizce.",
            topics = listOf("Programlama terimleri", "Hata raporlama", "Teknik doküman okuma", "Toplantı ve kod inceleme"),
            lessons = listOf(
                ProfessionalLesson("bug-reporting", "Hata raporu yazma", "Sorunu yeniden üretilebilir biçimde anlatmak.", "İyi bir hata raporu beklenen davranışı, gerçekleşen davranışı, tekrar adımlarını ve ilgili ortamı belirtir. Gizli anahtarlar veya kişisel veriler rapora eklenmemelidir.", listOf("The issue occurs when I sign in." to "Sorun oturum açarken oluşuyor.", "Steps to reproduce are listed below." to "Yeniden oluşturma adımları aşağıda listelenmiştir.", "The expected result is..." to "Beklenen sonuç ... şeklindedir."), "Hata raporunda hangi iki davranış karşılaştırılmalıdır?", "Beklenen davranış ile gerçekleşen davranış."),
                ProfessionalLesson("code-review", "Kod incelemesinde iletişim", "Geri bildirimi yapıcı ve somut biçimde ifade etmek.", "Kod incelemesinde kişiyi değil değişikliği eleştir; gözlemi, olası etkiyi ve öneriyi açıkça yaz. Emin olunmayan konular soru biçiminde sorulabilir.", listOf("Could we add a test for this case?" to "Bu durum için bir test ekleyebilir miyiz?", "This change may affect performance." to "Bu değişiklik performansı etkileyebilir.", "I suggest extracting this into a function." to "Bunu ayrı bir fonksiyona çıkarmayı öneriyorum."), "İyi bir kod inceleme yorumu neye odaklanır?", "Kişiye değil, değişikliğe, etkisine ve uygulanabilir öneriye."),
                ProfessionalLesson("technical-documentation", "Teknik dokümantasyon", "Kurulum ve kullanım adımlarını sıralı anlatmak.", "Dokümantasyon hedef kullanıcıyı, ön koşulları, adımları ve beklenen sonucu belirtmelidir. Komutlar ve örnekler doğrulanmalı; gizli bilgiler paylaşılmamalıdır.", listOf("Install the required dependencies." to "Gerekli bağımlılıkları yükleyin.", "Run the tests before merging." to "Birleştirmeden önce testleri çalıştırın.", "The following steps reproduce the issue." to "Aşağıdaki adımlar sorunu yeniden oluşturur."), "Bir teknik rehberde adımlar nasıl yazılmalı?", "Sıralı, doğrulanabilir ve beklenen sonucu belirtecek şekilde.")
            ),
            sampleTerms = listOf(
            ProfessionalTerm("bug", "yazılım hatası", "We found a bug in the application."),
            ProfessionalTerm("deploy", "yayına almak", "We will deploy the update tonight."),
            ProfessionalTerm("database", "veritabanı", "The database stores user information."),
            ProfessionalTerm("debug", "hata ayıklamak", "We need to debug this function."),
            ProfessionalTerm("dependency", "bağımlılık", "Update the project dependency."),
            ProfessionalTerm("code review", "kod incelemesi", "The change is ready for code review."),
            ProfessionalTerm("test case", "test senaryosu", "Add a test case for the edge condition."),
            ProfessionalTerm("release", "sürüm / yayımlama", "The release includes several bug fixes."),
            ProfessionalTerm("requirement", "gereksinim", "Document each requirement clearly."),
            ProfessionalTerm("repository", "kod deposu", "The repository contains the project source.")
            )
        ),
        ProfessionalTrack(
            id = "engineering",
            title = "Mühendislik İngilizcesi",
            titleEnglish = "Engineering English",
            description = "Teknik çizimler, ölçüler, süreçler ve mühendislik raporları.",
            topics = listOf("Teknik çizimler", "Ölçüm ve toleranslar", "Malzeme ve üretim", "Teknik raporlama", "Kalite kontrol", "Problem çözme"),
            lessons = listOf(
                ProfessionalLesson("engineering-specs", "Teknik şartname okuma", "Ölçü, birim ve tolerans ifadelerini ayırt etmek.", "Teknik raporlarda ölçüm birimini ve kabul sınırını birlikte belirtmek gerekir. Bir değerin uygun olup olmadığı, ilgili çizim veya şartnamedeki gerekliliklere göre değerlendirilir.", listOf("The part is within tolerance." to "Parça tolerans sınırları içindedir.", "Record the measured value." to "Ölçülen değeri kaydedin.", "Refer to the drawing for dimensions." to "Boyutlar için teknik çizime başvurun."), "Bir ölçüm raporunda sayısal değerle birlikte ne belirtilmelidir?", "Ölçü birimi ve karşılaştırılacağı ilgili şartname/tolerans."),
                ProfessionalLesson("quality-inspection", "Kalite kontrol bulguları", "Kontrol sonucunu kanıta dayalı ve açık biçimde raporlamak.", "Quality inspection kalite kontrol, non-conformance ise şartnameye uymayan durumdur. Rapor, gözlemleneni ve ilgili gerekliliği ayırmalı; kök neden kanıt olmadan kesinleştirilmemelidir.", listOf("The part does not meet the specification." to "Parça şartnameyi karşılamıyor.", "Please document the inspection result." to "Lütfen kontrol sonucunu belgeleyin.", "A non-conformance was identified." to "Bir uygunsuzluk tespit edildi."), "Uygunsuzluk raporunda ne birbirinden ayrılmalıdır?", "Gözlenen bulgu, ilgili gereklilik ve doğrulanmış sonuç."),
                ProfessionalLesson("engineering-handover", "Teknik durum ve proje devri", "İlerleme, risk ve açık işleri ekip arkadaşlarına aktarmak.", "Teknik devirde tamamlanan işler, açık konular, sorumlu kişiler ve sonraki adım belirtilir. Tahmini tarih kesin taahhüt gibi sunulmamalıdır.", listOf("The prototype is ready for testing." to "Prototip test için hazır.", "This issue is still under investigation." to "Bu konu hâlâ inceleniyor.", "The next review is scheduled for Monday." to "Bir sonraki gözden geçirme pazartesi planlandı."), "Teknik devirde açık bir konu nasıl aktarılmalıdır?", "Mevcut durum, sorumlu/sonraki adım ve varsa tahmini zaman açıkça belirtilmelidir.")
            ),
            sampleTerms = listOf(
            ProfessionalTerm("measurement", "ölçüm", "Record the measurement in the report."),
            ProfessionalTerm("tolerance", "tolerans", "The part is within the specified tolerance."),
            ProfessionalTerm("material", "malzeme", "Check the material specification."),
            ProfessionalTerm("design", "tasarım", "The design must meet the requirements."),
            ProfessionalTerm("specification", "şartname", "Check the specification before approval."),
            ProfessionalTerm("dimension", "boyut / ölçü", "Refer to the drawing for dimensions."),
            ProfessionalTerm("prototype", "prototip", "The prototype is ready for testing."),
            ProfessionalTerm("quality inspection", "kalite kontrolü", "Document the quality inspection result."),
            ProfessionalTerm("non-conformance", "uygunsuzluk", "Report any non-conformance clearly."),
            ProfessionalTerm("root cause", "kök neden", "The team is investigating the root cause.")
            )
        ),
        ProfessionalTrack(
            id = "business",
            title = "İş ve Ofis İngilizcesi",
            titleEnglish = "Business English",
            description = "E-posta, toplantı, sunum ve profesyonel iletişim.",
            topics = listOf("Profesyonel e-posta", "Toplantılar", "Sunumlar", "Müşteri iletişimi", "Müzakere", "İş takibi"),
            lessons = listOf(
                ProfessionalLesson("business-email", "Profesyonel e-posta yazımı", "Kısa, nazik ve amacı açık e-posta yazmak.", "İyi bir iş e-postası konu satırı, kısa bağlam, net istek ve uygun kapanış içerir. Alıcıdan beklenen eylem ve son tarih gerekiyorsa açıkça belirtilmelidir.", listOf("I am writing to follow up on..." to "...konusunu takip etmek için yazıyorum.", "Could you please confirm receipt?" to "Aldığınızı teyit edebilir misiniz?", "Thank you for your time." to "Zaman ayırdığınız için teşekkür ederim."), "İş e-postasında isteğin anlaşılır olması için ne yapılmalı?", "İstenen eylem ve gerekiyorsa son tarih açıkça yazılmalı."),
                ProfessionalLesson("business-meetings", "Toplantılara katılma ve fikir belirtme", "Toplantıda söz almak, görüş belirtmek ve kararları netleştirmek.", "Toplantı dilinde görüşü nazikçe sunmak, anlamadığında açıklama istemek ve kararları özetlemek önemlidir. Karar ile öneri birbirine karıştırılmamalıdır.", listOf("Could I add a point?" to "Bir nokta ekleyebilir miyim?", "I agree with that suggestion." to "Bu öneriye katılıyorum.", "Could we summarize the next steps?" to "Sonraki adımları özetleyebilir miyiz?"), "Toplantı sonunda neden sonraki adımlar özetlenmelidir?", "Sorumlulukları ve yapılacak işleri herkesin aynı anlaması için."),
                ProfessionalLesson("business-negotiation", "Teklif ve müzakere dili", "Teklif, koşul ve karşı önerileri profesyonelce ifade etmek.", "Müzakerede koşulların kapsamı açıkça söylenmeli; fiyat, teslim tarihi ve dahil olan hizmetler netleştirilmelidir. Kesinleşmemiş bir öneri anlaşma gibi sunulmamalıdır.", listOf("Could you provide a revised quotation?" to "Güncellenmiş bir fiyat teklifi sağlayabilir misiniz?", "This offer is valid until Friday." to "Bu teklif cuma gününe kadar geçerlidir.", "Let us review the terms." to "Koşulları gözden geçirelim."), "Bir teklifin yanlış anlaşılmasını önlemek için ne net yazılmalıdır?", "Kapsam, koşullar, geçerlilik süresi ve ilgili tarihler.")
            ),
            sampleTerms = listOf(
            ProfessionalTerm("deadline", "son teslim tarihi", "The deadline is Friday."),
            ProfessionalTerm("meeting", "toplantı", "The meeting starts at nine."),
            ProfessionalTerm("invoice", "fatura", "Please send the invoice by email."),
            ProfessionalTerm("schedule", "program / takvim", "I will check the schedule."),
            ProfessionalTerm("quotation", "fiyat teklifi", "Please review the attached quotation."),
            ProfessionalTerm("agenda", "gündem", "The agenda is attached to the invitation."),
            ProfessionalTerm("minutes", "toplantı tutanağı", "Please share the meeting minutes."),
            ProfessionalTerm("negotiate", "müzakere etmek", "We need to negotiate the delivery date."),
            ProfessionalTerm("stakeholder", "paydaş", "Keep the stakeholders informed."),
            ProfessionalTerm("follow-up", "takip / geri dönüş", "I am sending a follow-up email.")
            )
        ),
        ProfessionalTrack(
            id = "hospitality",
            title = "Turizm ve Otelcilik İngilizcesi",
            titleEnglish = "Hospitality English",
            description = "Misafir karşılama, rezervasyon ve hizmet iletişimi.",
            topics = listOf("Misafir karşılama", "Rezervasyon", "Şikâyet çözme", "Yön tarif etme"),
            lessons = listOf(ProfessionalLesson("guest-service", "Misafir talebini karşılama", "Kibarca bilgi almak ve çözüm seçeneklerini anlatmak.", "Hizmet dilinde sakin bir ton, teyit soruları ve gerçekçi beklentiler önemlidir. Yapılamayacak bir işlem için kesin söz vermek yerine uygun ekipten destek istenmelidir.", listOf("How may I help you?" to "Size nasıl yardımcı olabilirim?", "Let me check that for you." to "Sizin için kontrol edeyim.", "I apologize for the inconvenience." to "Yaşanan aksaklık için özür dilerim."), "Bir isteği hemen çözemiyorsan nasıl yanıt vermek uygundur?", "Kontrol edeceğini söylemek ve gerçekçi bir sonraki adım sunmak.")),
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
            lessons = listOf(ProfessionalLesson("shipment-status", "Sevkiyat durumunu bildirme", "Durum, gecikme ve tahmini varış bilgisini net ifade etmek.", "Lojistik iletişiminde gönderi numarası, güncel durum ve tahmini tarih birbirinden ayrılmalıdır. Kesinleşmemiş teslimat tarihi garanti gibi sunulmamalıdır.", listOf("The shipment is in transit." to "Sevkiyat yolda.", "The delivery has been delayed." to "Teslimat gecikti.", "The estimated arrival date is..." to "Tahmini varış tarihi ..."), "Tahmini varış tarihi kesin teslimat garantisi midir?", "Hayır; tahmini bilgidir ve güncel durumla birlikte belirtilmelidir.")),
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
            lessons = listOf(ProfessionalLesson("contract-language", "Sözleşme dilini anlama", "Taraf, yükümlülük ve madde ifadelerini ayırt etmek.", "Hukuki İngilizcede shall, may ve must gibi ifadelerin anlamı bağlama ve belgeye göre değişebilir. Öğrenme örnekleri hukuki tavsiye değildir; gerçek belgeler uzman tarafından değerlendirilmelidir.", listOf("The parties agree to..." to "Taraflar ... konusunda anlaşır.", "This clause applies to..." to "Bu madde ... için geçerlidir.", "The agreement takes effect on..." to "Anlaşma ... tarihinde yürürlüğe girer."), "Gerçek bir sözleşmenin anlamı yalnızca tek bir kelimeye bakılarak kesinleştirilebilir mi?", "Hayır; tüm bağlam ve ilgili hukuk dikkate alınmalıdır.")),
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
            lessons = listOf(ProfessionalLesson("classroom-instructions", "Sınıf içi yönergeler", "Talimat ve açıklamaları basit İngilizceyle ifade etmek.", "Eğitim ortamında kısa yönergeler, kontrol soruları ve anlaşılmayı doğrulama ifadeleri kullanılır. Öğrenciden anlamadığında soru sorması istenebilir.", listOf("Please work in pairs." to "Lütfen ikili çalışın.", "Can you explain your answer?" to "Cevabını açıklayabilir misin?", "Do you have any questions?" to "Sorunuz var mı?"), "Bir yönergenin anlaşıldığını nasıl kontrol edebilirsin?", "Öğrenciden kendi sözleriyle açıklamasını istemek veya kısa kontrol sorusu sormak.")),
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
            lessons = listOf(ProfessionalLesson("vehicle-complaint", "Araç şikâyetini tarif etme", "Müşteri bildirimini açık ve tarafsız kaydetmek.", "Servis kabulünde müşterinin bildirdiği belirti ile teknisyenin kontrol sonucu ayrı tutulur. Ses, zaman, koşul ve gösterge uyarıları net biçimde sorulup kaydedilmelidir.", listOf("When did the noise start?" to "Ses ne zaman başladı?", "The warning light is on." to "Uyarı ışığı yanıyor.", "The customer reports a vibration." to "Müşteri titreşim olduğunu bildiriyor."), "Müşteri bildirimi ile doğrulanmış teknik bulgu neden ayrı yazılır?", "Bildirilen şikâyet ile kontrol sonucu birbirine karıştırılmamalıdır.")),
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
