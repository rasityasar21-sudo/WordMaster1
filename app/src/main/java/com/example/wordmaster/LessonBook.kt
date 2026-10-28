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
        val t = title.lowercase().replace("i\u0307", "i")
        if ("uçuş kumandaları" in t) {
            return LessonBookChapter(
                "Uçuş kumanda yüzeylerinin İngilizce adlarını, hareketlerini ve bakım dilini öğrenmek",
                "Primary flight controls ana uçuş kumandalarını ifade eder: ailerons yatış (roll), elevator veya stabilator yunuslama (pitch), rudder sapma (yaw) hareketiyle ilişkilidir. Secondary flight controls arasında flaps, slats ve spoilers gibi yüzeyler bulunur; ancak sistem mimarisi uçak tipine göre değişir. Aileron, wing üzerindeki hareketli yüzeydir; flap ile aynı görevde değildir. Elevator genellikle horizontal stabilizer bölgesinde, rudder ise vertical stabilizer üzerinde bulunur. Bakım dokümanlarında deflection yüzeyin sapmasını, hinge bağlantı eksenini, actuator hareket sağlayan birimi, linkage mekanik bağlantıyı, rigging ise uygun ayar/hizalama işlemlerini ifade eder. Inspection, discrepancy, limitation ve rectification sözcükleri bir arıza kaydını anlamada önemlidir. Bir teknisyen İngilizce rapor okurken gözlenen durum, doğrulanmış bulgu ve varsayımı birbirinden ayırmalıdır. Bu içerik terim ve teknik okuma eğitimi içindir; belirli uçak üzerinde kontrol, ayar veya bakım yapmak için tip ve seri numarasına uygun güncel AMM, FIM/TSM, yetkili prosedürler ve eğitim gerekir.",
                "Flight axis: roll → aileron; pitch → elevator/stabilator; yaw → rudder.\nVocabulary: flap/slat → lift-related high-lift devices; spoiler → airflow/lift management; actuator → movement; hinge → pivot; linkage → mechanical connection.\nTechnical report: observed condition → affected component → document reference → verified finding (if available).",
                listOf(
                    "The ailerons control roll about the aircraft's longitudinal axis." to "Aileronlar uçağın boylamasına ekseni etrafındaki yatış hareketini kontrol eder.",
                    "The rudder is mounted on the vertical stabilizer." to "Rudder, dikey stabilize üzerinde bulunur.",
                    "The technician recorded the observed discrepancy without assuming its cause." to "Teknisyen, nedenini varsaymadan gözlenen uygunsuzluğu kaydetti.",
                    "The maintenance team must consult the applicable approved manual before any adjustment." to "Bakım ekibi herhangi bir ayardan önce geçerli onaylı el kitabına başvurmalıdır.",
                    "Flaps and slats are not interchangeable with primary flight controls." to "Flaplar ve slatlar ana uçuş kumandalarıyla birbirinin yerine geçmez."
                ),
                listOf("Aileron ile flap görevlerini birbirine karıştırma.", "Rudder ile elevator hareket eksenlerini karıştırma.", "Bir gözlemi doğrulanmış arıza nedeni gibi raporlama.", "Genel eğitim açıklamasını uçak tipine özgü bakım prosedürü yerine kullanma."),
                listOf("Roll, pitch ve yaw için ilgili kumandayı İngilizce açıkla.", "Primary ve secondary flight control örneklerini sınıflandır.", "Actuator, hinge, linkage ve deflection terimlerini tanımla.", "Gözlem ile varsayımı ayıran iki cümlelik İngilizce rapor yaz.", "Gerçek bakım için tip bazlı onaylı doküman gerekliliğini açıkla.")
            )
        }
        if ("bakım aletleri ve ekipman" in t) {
            return LessonBookChapter(
                "Uçak bakım aletlerini ve teknik doküman dilini doğru anlamak",
                "Aircraft maintenance tools sözcükleri günlük İngilizcedeki genel alet adlarından daha kesin anlamlar taşıyabilir. Torque wrench belirlenmiş torkun uygulanmasıyla ilgili bir alettir; screwdriver vida başına uygun uç gerektirir; socket ve ratchet birlikte kullanılabilen farklı parçalardır. Multimeter elektriksel büyüklüklerin ölçümü için kullanılan cihazdır; ancak ölçümün güvenli ve geçerli yöntemi uçak sistemine, ekipmana ve onaylı talimata bağlıdır. Calibrated, serviceable, inspection due ve tool control terimleri aletin uygunluk ve izlenebilirlik durumunu anlatır. Missing tool report gibi ifadeler alet kontrolünün emniyet boyutunu gösterir. Teknik talimatta inspect, verify, record, replace ve refer to farklı eylemleri belirtir; bunları birbirinin yerine çevirmemek gerekir. Bu bölüm aletleri tanıma ve bakım İngilizcesi okuma eğitimi sunar; gerçek uçak bakım işlemleri için onaylı tip bazlı prosedür ve yetkilendirme zorunludur.",
                "Tool → purpose → suitability status → document reference.\nVocabulary: torque wrench / socket / ratchet / multimeter / caliper / calibrated / serviceable / tool control.",
                listOf(
                    "Check that the required tool is identified in the applicable maintenance document." to "Gerekli aletin geçerli bakım dokümanında belirtildiğini kontrol edin.",
                    "The torque wrench must have a valid calibration status." to "Tork anahtarının geçerli kalibrasyon durumu olmalıdır.",
                    "The technician recorded the missing tool and informed the supervisor." to "Teknisyen eksik aleti kaydetti ve amirine bildirdi.",
                    "A multimeter is a measuring instrument, not a substitute for an approved test procedure." to "Multimetre bir ölçüm cihazıdır; onaylı test prosedürünün yerine geçmez."
                ),
                listOf("Socket ve ratchet terimlerini eş anlamlı sanma.", "Kalibrasyon durumunu tahmin ederek uygunluk bildirme.", "Genel alet tanımından gerçek bakım uygulama adımı çıkarma."),
                listOf("Altı aleti İngilizce adı ve amacıyla eşleştir.", "Calibrated ve serviceable arasındaki farkı açıkla.", "Bir alet kontrol kaydını İngilizce özetle.", "Gerçek kullanım için doküman ve yetki gerekliliğini belirt.")
            )
        }
        if ("gövde ve yapısal parçalar" in t) {
            return LessonBookChapter(
                "Airframe yapısının temel parçalarını ve hasar raporu dilini öğrenmek",
                "Fuselage uçağın ana gövdesini, wing kanadı, empennage kuyruk takımını ifade eder. Skin dış kaplama, frame gövde enine yapı elemanı, stringer boyuna destek elemanı, rib kanat profilini şekillendiren iç eleman ve spar kanadın temel taşıyıcı elemanlarından biridir. Fastener bağlantı elemanı, corrosion korozyon, crack çatlak ve dent göçük anlamına gelir. Bir hasar gözleminde location, extent, visible condition ve reference gibi ifadeler kullanılabilir. Görsel bir gözlem tek başına hasarın kabul edilebilir olduğunu göstermez; değerlendirme için uçak tipine uygun güncel SRM ve diğer yetkili bakım dokümanları gerekir. İngilizce raporda gözlemi açıkça yazmak, sınır değer veya tamir kararı uydurmaktan daha önemlidir.",
                "Structure: fuselage → frame/stringer/skin; wing → spar/rib/skin.\nReport language: location → observed condition → reference → assessment status.",
                listOf(
                    "The inspection report describes a dent on the fuselage skin." to "Muayene raporu gövde kaplamasındaki bir göçüğü tanımlar.",
                    "The wing spar is a major structural member." to "Kanat sparı önemli bir yapısal elemandır.",
                    "The technician documented the visible corrosion without assessing its acceptability." to "Teknisyen, kabul edilebilirliğini değerlendirmeden görülen korozyonu belgeledi.",
                    "Any structural assessment must follow the applicable approved data." to "Her yapısal değerlendirme geçerli onaylı verilere dayanmalıdır."
                ),
                listOf("Rib ile spar görevlerini karıştırma.", "Dent, crack ve corrosion terimlerini aynı hasar gibi çevirme.", "Gözlemi yetkili yapısal değerlendirme yerine koyma."),
                listOf("Fuselage ve wing içindeki temel elemanları adlandır.", "Dört hasar terimini ayırt et.", "Tarafsız bir İngilizce gözlem cümlesi yaz.", "SRM'nin neden uçak tipine özgü olduğunu açıkla.")
            )
        }
        if ("motor ve güç ünitesi terimleri" in t) {
            return LessonBookChapter(
                "Turbofan motorun ana bölümlerini ve teknik rapor dilini anlamak",
                "Aircraft powerplant terminology, motorun çalışma ilkelerini İngilizce kaynaklardan doğru okumayı sağlar. Modern turbofan motorlarda fan, compressor, combustor, turbine ve exhaust gibi bölümler bulunur. Compressor havanın basıncını artırır; combustor yakıt-hava karışımının yanmasıyla enerji üretir; turbine sıcak gaz akışından enerji alarak ilgili döner bileşenleri tahrik eder. N1 ve N2, motor tasarımına göre farklı rotor gruplarının dönüş hızlarıyla ilişkili göstergelerdir; bunların kesin tanımı ve sınırları motor tipine özgüdür. Engine indication, vibration, abnormal indication, discrepancy ve troubleshooting terimlerini ayırt etmek gerekir. Bir göstergenin anormal olması tek başına belirli bir sensör veya kablo arızasını kanıtlamaz. Uçak tipi, motor modeli ve konfigürasyonuna uygun güncel AMM, FIM/TSM ve diğer onaylı kaynaklar olmadan bakım adımı veya arıza kodu çıkarılmamalıdır.",
                "Airflow vocabulary: inlet → fan/compressor → combustor → turbine → exhaust.\nReporting: observed indication → operating context → approved reference → verified finding.",
                listOf(
                    "The compressor raises the pressure of the incoming air." to "Kompresör gelen havanın basıncını artırır.",
                    "The turbine extracts energy from the hot gas stream." to "Türbin sıcak gaz akışından enerji alır.",
                    "The reported N1 indication requires assessment using the applicable maintenance data." to "Bildirilen N1 göstergesi geçerli bakım verileriyle değerlendirilmelidir.",
                    "An abnormal indication does not, by itself, identify a failed component." to "Anormal bir gösterge tek başına arızalı parçayı belirlemez."
                ),
                listOf("N1 ve N2 tanımlarını bütün motorlarda aynı kabul etme.", "Bir göstergeden doğrulanmamış arıza nedeni üretme.", "Genel açıklamayı motor tipine özgü bakım talimatı yerine kullanma."),
                listOf("Motorun beş ana bölümünü İngilizce tanımla.", "Compressor ile turbine işlevini karşılaştır.", "Indication ve confirmed fault farkını açıkla.", "Belirsizliği koruyan kısa bir teknik rapor yaz.")
            )
        }
        if ("hidrolik sistemler" in t) {
            return LessonBookChapter(
                "Hidrolik sistemin temel bileşenlerini ve durum bildirimlerini okumak",
                "Aircraft hydraulic systems, basınçlı akışkan üzerinden kuvvet aktarımı sağlayabilir. Reservoir akışkan deposu, pump pompa, pressure line basınç hattı, return line dönüş hattı, actuator hareket sağlayan eleman, valve ise akışı yönlendiren veya kontrol eden bileşendir. Pressure, flow, leakage, contamination ve isolation gibi kelimeler teknik belgelerde farklı durumları anlatır. Pressure düşüşü, tek başına hangi parçanın arızalı olduğunu belirlemez. Bir raporda observed leakage ile suspected source ifadeleri ayrı tutulmalıdır. Hidrolik sistemler depolanmış enerji ve basınç içerebilir; bu eğitim bölümü hiçbir şekilde uçakta hat açma, basınç boşaltma veya bakım yapma talimatı değildir. Gerçek işlemler yalnızca yetkili personelce güncel ve uçak tipine uygun onaylı dokümanlarla yapılır.",
                "System vocabulary: reservoir → pump → lines/valves → actuator → return.\nObservation language: pressure indication / visible leakage / contamination / verified condition.",
                listOf(
                    "The hydraulic pump supplies pressurized fluid to the system." to "Hidrolik pompa sisteme basınçlı akışkan sağlar.",
                    "The actuator converts hydraulic energy into movement." to "Aktüatör hidrolik enerjiyi harekete dönüştürür.",
                    "The report notes visible leakage but does not identify its source." to "Rapor görünür sızıntıyı belirtir ancak kaynağını belirlemez.",
                    "Hydraulic maintenance requires the applicable approved procedure." to "Hidrolik bakım geçerli onaylı prosedürü gerektirir."
                ),
                listOf("Pressure ile flow kavramlarını eş anlamlı sanma.", "Görülen sızıntının kaynağını kanıtsız kesinleştirme.", "Basınçlı sistemler için genel ders metnini uygulama talimatı sayma."),
                listOf("Reservoir, pump, valve ve actuator işlevlerini açıkla.", "Leakage ve contamination terimlerini ayır.", "Gözlemi varsayımdan ayıran rapor yaz.", "Onaylı prosedürün emniyet açısından önemini açıkla.")
            )
        }
        // C1-specific chapters must precede broad grammar and writing title matches.
        if ("anlam incelikleri ve register" in t) {
            return LessonBookChapter(
                "Yakın anlamlı ifadeler arasında anlam, tutum ve resmiyet farkını seçmek",
                "C1 düzeyinde doğru sözcüğü seçmek yalnızca sözlük anlamını bilmek değildir. Claim, argue, maintain ve suggest aynı görüş bildirme işlevine sahip olabilir; ancak claim bazen kanıtı tartışmalı bir iddiayı, maintain ise konuşanın görüşünü ısrarla sürdürdüğünü hissettirir. Resmî bir raporda get yerine obtain, fix yerine resolve kullanılabilir; fakat gereksiz ağır sözcükler açıklığı azaltır. Bir ifadenin hedef kitlesini, kanıt düzeyini ve duygusal tonunu birlikte değerlendir. Kesin kanıt olmadığında proves yerine suggests veya indicates daha ölçülüdür. Aynı metinde üslubu tutarlı koru.",
                "Audience + purpose + evidence strength → word choice → register check.\nTentative: may suggest / appears to indicate\nNeutral: reports / states / explains\nStrong: demonstrates / establishes (only with sufficient evidence)",
                listOf(
                    "The findings suggest a possible link, but do not establish causation." to "Bulgular olası bir ilişkiye işaret ediyor ancak nedenselliği kanıtlamıyor.",
                    "The committee maintained that the evidence was insufficient." to "Komite, kanıtların yetersiz olduğu görüşünü sürdürdü.",
                    "We need to resolve the issue before publishing the report." to "Raporu yayımlamadan önce sorunu çözmemiz gerekiyor.",
                    "The proposal is feasible, although its long-term impact remains uncertain." to "Öneri uygulanabilir; ancak uzun vadeli etkisi belirsizliğini koruyor."
                ),
                listOf("Suggests yerine proves yazarak kanıtı olduğundan güçlü gösterme.", "Resmî bir raporda gündelik ve resmî ifadeleri rastgele karıştırma.", "Yakın anlamlı kelimelerin her bağlamda birbirinin yerine geçeceğini varsayma.", "Uzun kelimenin her zaman daha iyi bir seçim olduğunu düşünme."),
                listOf("Bir görüşün ne kadar kesin ifade edildiğini belirle.", "Aynı cümleyi resmî ve gündelik hedef kitle için yeniden yaz.", "Claim, suggest ve demonstrate arasındaki kanıt farkını açıkla.", "Bir paragraftaki üslup tutarsızlıklarını düzelt.", "Seçtiğin sözcüğün okuyucuda yaratacağı tutumu gerekçelendir.")
            )
        }
        if ("ileri düzey cümle yapıları" in t) {
            return LessonBookChapter(
                "C1 cümlelerinde vurgu, koşul ve bilgi akışını yönetmek",
                "İleri düzey yazıda karmaşıklık amaç değil araçtır. Inversion, sınırlayıcı ifadeyi başa taşıyarak vurgular: Not only did the team identify the problem, but it also proposed a solution. Cleft sentence, önemli bilgiyi ayırır: What matters most is the reliability of the evidence. Participle clause, öznesi ana cümleyle aynı olduğunda tekrarı azaltır: Having reviewed the data, the team revised its conclusion. Reduced relative clause ve nominalisation resmî üslupta yoğun bilgi taşır; fakat özne ilişkisini belirsizleştirmemelidir. Önce açık bir temel cümle kur, ardından yalnızca anlamı güçlendiren dönüşümü uygula.",
                "Not only + auxiliary + subject + verb, but also ...\nWhat + clause + be + focus\nHaving + past participle, main clause (shared subject)\nOnly after + clause + auxiliary + subject + verb",
                listOf(
                    "Not only did the review reveal gaps, but it also identified practical solutions." to "İnceleme yalnızca eksikleri ortaya çıkarmadı, uygulanabilir çözümler de belirledi.",
                    "What the report fails to address is the cost of implementation." to "Raporun ele almadığı nokta uygulama maliyetidir.",
                    "Having examined both proposals, the panel recommended a pilot study." to "Her iki öneriyi de inceledikten sonra kurul bir pilot çalışma önerdi.",
                    "Only after the results were verified did the researchers publish their conclusions." to "Araştırmacılar ancak sonuçlar doğrulandıktan sonra çıkarımlarını yayımladı."
                ),
                listOf("Not only sonrası yardımcı fiil ile özneyi ters çevirmeyi unutma.", "Having reviewed ... yan cümlesinin öznesini ana cümlenin öznesinden farklı kurma.", "Vurgulu yapıyı yalnızca daha uzun göründüğü için kullanma.", "Nominalisation yüzünden eylemi kimin yaptığını belirsizleştirme."),
                listOf("Düz cümleyi anlamı değiştirmeden inversion ile yeniden kur.", "Bir cleft sentence içinde odaklanan bilgiyi göster.", "Participle clause ile ana cümlenin öznesini eşleştir.", "Gereksiz karmaşık cümleyi açık ve doğru biçime sadeleştir.", "İki farklı vurgu biçiminin okuyucu üzerindeki etkisini karşılaştır.")
            )
        }
        if ("birden fazla kaynaktan sentez" in t) {
            return LessonBookChapter(
                "Kaynakları karşılaştırıp özgün ve kanıta dayalı sentez üretmek",
                "Sentez, iki metni sırayla özetlemekten farklıdır: ortak soruyu belirler, bulguların nerede birleşip ayrıldığını gösterir ve bu karşılaştırmadan sınırlı bir sonuç çıkarır. Önce her kaynağın iddiasını, yöntemini, kanıtını ve sınırlılığını not et. Ardından Both sources acknowledge ..., Whereas the first study ..., the second ... gibi karşılaştırma yapılarıyla ilişki kur. Bir kaynağın yorumunu doğrulanmış gerçek gibi aktarma. Korelasyon ile nedenselliği ayır; örneklem veya yöntem farklılıklarının sonucu nasıl etkileyebileceğini belirt. Son paragrafta yeni bir iddia icat etmek yerine kanıtların birlikte neyi desteklediğini yaz.",
                "Shared question → source A claim/evidence → source B claim/evidence → agreement or tension → qualified synthesis",
                listOf(
                    "Both reports identify delays, but they attribute them to different factors." to "İki rapor da gecikmeleri belirliyor; ancak bunları farklı etkenlere bağlıyor.",
                    "Whereas the first study focuses on cost, the second examines long-term reliability." to "İlk çalışma maliyete odaklanırken ikincisi uzun vadeli güvenilirliği inceliyor.",
                    "Taken together, the findings suggest a need for further testing." to "Bulgular birlikte değerlendirildiğinde ek testlere ihtiyaç olabileceğine işaret ediyor.",
                    "This conclusion is tentative because the samples differ considerably." to "Örneklemler önemli ölçüde farklı olduğu için bu sonuç geçicidir."
                ),
                listOf("İki ayrı özeti yan yana koyup buna sentez deme.", "Kaynakların çeliştiği noktaları gizleme.", "Kanıtın desteklemediği kesin nedensel sonuç çıkarma.", "Kaynak fikrini kendi bulgun gibi sunma."),
                listOf("İki kaynağın ortak araştırma sorusunu yaz.", "Her kaynak için iddia ve kanıtı ayrı not et.", "En az bir uzlaşma ve bir ayrışma noktası belirle.", "Karşılaştırmayı tek bir sentez paragrafına dönüştür.", "Sonucun sınırlarını ve doğrulanması gereken noktayı belirt.")
            )
        }
        if ("sunum ve ikna edici konuşma" in t) {
            return LessonBookChapter(
                "C1 sunumunda iddia, kanıt ve itirazları yönetmek",
                "Etkili bir C1 sunumu, yalnızca akıcı konuşma değil, dinleyicinin neden ikna olması gerektiğini açıkça göstermektir. Açılışta problemi ve ana iddiayı belirt; ardından üç ana noktayı kanıtla destekle. Signposting ifadeleri dinleyicinin yapıyı takip etmesini sağlar: Let me begin by ..., Turning to ..., To put this into perspective .... Verinin sınırlılıklarını saklama; özellikle kesin sonuç çıkarmaya yetmeyen bulgularda cautiously ve may gibi ifadeler kullan. Soru-cevap bölümünde önce soruyu kendi sözcüklerinle doğrula, doğrudan cevap ver, sonra gerekçe ve gerekirse belirsizlik ekle. Bilmediğin sayıyı uydurmak yerine doğrulamayı teklif et.",
                "Opening → claim → evidence → limitation → recommendation → questions\nQuestion response: acknowledge → direct answer → evidence → next step",
                listOf(
                    "Let me begin by outlining the problem we are trying to solve." to "Çözmeye çalıştığımız sorunu ana hatlarıyla açıklayarak başlayayım.",
                    "Turning to the evidence, the results are promising but not conclusive." to "Kanıtlara geçersek sonuçlar umut verici ancak kesin değil.",
                    "That is a fair concern; our recommendation accounts for the additional cost." to "Bu yerinde bir endişe; önerimiz ek maliyeti hesaba katıyor.",
                    "I cannot confirm that figure yet, but I can verify it after the session." to "Bu rakamı henüz doğrulayamam; ancak oturumdan sonra kontrol edebilirim."
                ),
                listOf("Sunuma amaç ve ana iddia belirtmeden başlama.", "Bir grafiğin korelasyonunu kesin nedensellik olarak sunma.", "İtirazı yanıtlamak yerine aynı iddiayı tekrarlama.", "Doğrulanmamış sayı veya kaynağı kesin bilgi gibi aktarma."),
                listOf("Bir dakikalık problem–kanıt–öneri açılışı hazırla.", "Üç geçiş ifadesiyle sunumun bölümlerini bağla.", "Kanıtın en az bir sınırlılığını açıkla.", "Zor bir soruya önce kısa ve doğrudan yanıt ver.", "Sonuç bölümünde tek bir uygulanabilir sonraki adım öner.")
            )
        }
        if ("resmî rapor ve öneri yazımı" in t) {
            return LessonBookChapter(
                "Kanıta dayalı C1 raporu ve uygulanabilir öneri hazırlamak",
                "C1 düzeyinde resmî rapor, okuyucunun karar verebilmesi için bulguları izlenebilir bir yapıda sunar. Executive summary ana sonuçları ve öneriyi kısa biçimde verir; background konuyu ve kapsamı tanımlar; findings doğrulanabilir gözlemleri aktarır; discussion olası açıklamaları ve sınırlılıkları tartışır; recommendations uygulanabilir adımları gerekçelendirir. Bulguyla yorumu ayır: The survey recorded ... bir bulgu, This may indicate ... bir yorumdur. Öneriler için sorumlu, öncelik ve doğrulama ölçütü belirt. Üslubu ölçülü, açık ve tutarlı tut; belirsiz fiiller yerine yapılabilir eylemler yaz.",
                "Executive summary → scope/method → findings → discussion/limitations → recommendations\nRecommendation = action + rationale + owner + success measure",
                listOf(
                    "The report evaluates three options against cost, reliability and feasibility." to "Rapor üç seçeneği maliyet, güvenilirlik ve uygulanabilirlik açısından değerlendiriyor.",
                    "The available evidence indicates a reduction in delays, although the sample is limited." to "Mevcut kanıtlar gecikmelerde azalmaya işaret ediyor; ancak örneklem sınırlı.",
                    "We recommend a four-week pilot to assess operational impact." to "Operasyonel etkiyi değerlendirmek için dört haftalık bir pilot uygulama öneriyoruz.",
                    "The team should review the results against agreed success criteria." to "Ekip sonuçları üzerinde uzlaşılan başarı ölçütlerine göre incelemelidir."
                ),
                listOf("Executive summary bölümünü ayrıntılı yöntem anlatımıyla doldurma.", "Bulguyu olası açıklamasıyla karıştırma.", "Öneriyi sorumlusu ve ölçütü olmayan belirsiz bir dilekle bitirme.", "Kanıt sınırlılıklarını rapordan çıkarma."),
                listOf("Raporun okuyucusunu ve karar sorusunu belirle.", "Bulguları yorumlardan ayrı başlıklarda sun.", "Bir sınırlılığı açıkça belirt.", "Eylem, gerekçe ve ölçüt içeren iki öneri yaz.", "Yönetici özetini yalnızca temel sonuç ve öneriyle sınırla.")
            )
        }
        if ("ima, ton ve örtük anlam" in t) {
            return LessonBookChapter(
                "C1 okumasında söylenmeyen anlamı kanıttan ayırmak",
                "Örtük anlam, metinde doğrudan yazılmayan ancak sözcük seçimi, bağlam ve karşıtlıkla desteklenen yorumdur. Apparently, supposedly ve even gibi işaretler yazarın mesafesini veya şaşkınlığını gösterebilir; yine de tek bir sözcük kesin niyet kanıtı değildir. Önce yazarın açıkça söylediğini belirle, ardından olası imayı metindeki ifadeyle gerekçelendir. The author seems to imply ... gibi ölçülü yorumlar kullan. İroniyi tanırken söylenen ile bağlamın işaret ettiği sonuç arasındaki farkı ara. Okurun kendi varsayımını yazarın tutumuymuş gibi sunma.",
                "Explicit statement → contextual clue → plausible inference → alternative reading",
                listOf(
                    "The plan is ambitious, although its funding remains unclear." to "Plan iddialı; ancak finansmanı hâlâ belirsiz.",
                    "The author appears sceptical about the proposed timeline." to "Yazar önerilen takvime kuşkuyla yaklaşıyor gibi görünüyor.",
                    "Interestingly, the report omits the most recent figures." to "İlginç biçimde rapor en güncel rakamları dışarıda bırakıyor.",
                    "This wording may imply concern rather than outright rejection." to "Bu ifade doğrudan ret yerine endişe ima ediyor olabilir."
                ),
                listOf("Yazarın niyetini tek bir kelimeye dayanarak kesinleştirme.", "Olumsuz tonu doğrudan düşmanlık olarak yorumlama.", "Metinde olmayan bilgiyi çıkarım diye ekleme."),
                listOf("Açık iddiayı ve örtük yorumu ayır.", "Çıkarımını iki bağlam ipucuyla destekle.", "Alternatif bir okuma öner.", "Kesin olmayan yorumda ihtiyatlı dil kullan.")
            )
        }
        if ("ileri düzey tartışma" in t) {
            return LessonBookChapter(
                "Karşı görüşü adil biçimde ele alarak sav geliştirmek",
                "C1 tartışmasında güçlü argüman, karşı tarafın görüşünü çarpıtmadan özetler ve hangi noktada ayrıldığını kanıtla açıklar. İlk olarak tartışılabilir bir tez kur. Ardından gerekçe, örnek ve sınırları belirt. I take your point that ... ile karşı tarafın haklı yönünü kabul edip However, this overlooks ... ile eksik kalan boyutu gösterebilirsin. Straw man, yani karşı görüşün zayıflatılmış biçimine cevap verme hatasından kaçın. Uzlaşma olasılığı varsa hangi koşulda ortak çözümün mümkün olduğunu açıkla.",
                "Position → reason → evidence → fair counterargument → rebuttal → qualified conclusion",
                listOf(
                    "I accept that the proposal may reduce costs in the short term." to "Önerinin kısa vadede maliyetleri azaltabileceğini kabul ediyorum.",
                    "However, this argument overlooks the long-term maintenance burden." to "Ancak bu argüman uzun vadeli bakım yükünü göz ardı ediyor.",
                    "A more balanced approach would test both options before scaling up." to "Daha dengeli bir yaklaşım, genişletmeden önce her iki seçeneği de test etmek olurdu.",
                    "Our disagreement concerns the evidence, not the overall objective." to "Anlaşmazlığımız genel amaçla değil, kanıtlarla ilgili."
                ),
                listOf("Karşı görüşü olduğundan daha uç gösterme.", "Kanıtsız kesin hükümler verme.", "İtirazı kişiye yöneltme."),
                listOf("Tezini tek cümlede açıkla.", "Karşı savı adil biçimde özetle.", "Kanıtla desteklenen bir yanıt oluştur.", "Ortak zemin veya çözüm koşulunu belirt.")
            )
        }
        if ("metin düzenleme ve üslup" in t) {
            return LessonBookChapter(
                "C1 metninde açıklık, tutarlılık ve hedef kitle uyumu",
                "İyi düzenleme yalnızca dil bilgisi hatalarını düzeltmez. Her paragrafın bir ana düşünce taşıdığını, zamirlerin açık bir gönderimi olduğunu ve terimlerin tutarlı kullanıldığını kontrol et. İlk okumada mantık akışını, ikincide cümlelerin açıklığını, üçüncüde üslup ve dil bilgisi ayrıntılarını değerlendir. Due to the fact that yerine because kullanmak çoğu zaman daha açıktır. Resmî üslup, her cümleyi uzun yapmayı gerektirmez. Düzenleme sırasında anlamı değiştirmemeye ve kaynağın kesinlik düzeyini korumaya dikkat et.",
                "Structure pass → clarity pass → register pass → accuracy pass",
                listOf(
                    "The results were inconclusive, so further testing is required." to "Sonuçlar kesin değildi; bu nedenle ek test gerekiyor.",
                    "Because the deadline changed, the team revised its schedule." to "Son tarih değiştiği için ekip takvimini güncelledi.",
                    "The revised policy applies to all departments." to "Güncellenen politika bütün departmanlara uygulanır.",
                    "The evidence suggests improvement, but the effect remains uncertain." to "Kanıtlar iyileşmeye işaret ediyor, ancak etkinin derecesi belirsiz."
                ),
                listOf("Gereksiz uzunluğu akademik kalite sanma.", "Belirsiz it/this zamirlerini açıklamadan bırakma.", "Düzenlerken ihtiyatlı bir iddiayı kesinleştirme."),
                listOf("Bir paragrafın ana düşüncesini belirle.", "Gereksiz tekrarları çıkar.", "Belirsiz zamirleri somut adlarla değiştir.", "Metnin resmiyet düzeyini tutarlı hale getir.")
            )
        }
        if ("ileri dinleme ve not çıkarma" in t) {
            return LessonBookChapter(
                "Uzun konuşmada tutum, yapı ve kanıtı notlarla yakalamak",
                "C1 dinlemede her kelimeyi yazmaya çalışmak yerine konuşmanın amacını, ana iddialarını ve bunları destekleyen kanıtları ayırt et. However, nevertheless ve in contrast karşıtlığa; to sum up ve ultimately sonuca işaret eder. Konuşmacının tutumunu kesinlik, çekince ve vurgu ifadelerinden çıkar. Cornell benzeri iki sütunlu notlarda solda anahtar soru veya kavram, sağda kanıt ve örnek tut; sonunda üç cümlelik özet yaz. Ses kaydı yoksa bu bölümün metin ve not alma çalışması olduğunu açıkça belirt; gerçek dinleme yapıldığını varsayma.",
                "Purpose → claim → evidence → contrast → conclusion\nNotes: key idea | supporting detail | uncertainty",
                listOf(
                    "The speaker questions whether the benefits justify the cost." to "Konuşmacı faydaların maliyeti haklı çıkarıp çıkarmadığını sorguluyor.",
                    "Nevertheless, the findings warrant further investigation." to "Buna rağmen bulgular daha fazla araştırmayı gerektiriyor.",
                    "The main point is supported by two examples and one limitation." to "Ana fikir iki örnek ve bir sınırlılıkla destekleniyor.",
                    "In summary, the proposal is promising but requires validation." to "Özetle öneri umut verici ancak doğrulama gerektiriyor."
                ),
                listOf("Her kelimeyi not almaya çalışıp ana fikri kaçırma.", "Konuşmacının aktardığı görüşü kendi görüşüyle karıştırma.", "Metin çalışmasını gerçek sesli dinleme olarak sunma."),
                listOf("Ana iddiayı tek cümlede özetle.", "İki kanıtı ve bir sınırlılığı not et.", "Konuşmacının tutumunu bir ifadeyle gerekçelendir.", "Notlardan üç cümlelik özet üret.")
            )
        }
        if ("profesyonel iletişim ve ikna" in t) {
            return LessonBookChapter(
                "C1 profesyonel görüşmelerde netlik ve ölçülü ikna",
                "Karmaşık profesyonel görüşmelerde amaç, görüşünü güçlü biçimde savunurken belirsizlikleri dürüstçe açıklamaktır. Önce karşı tarafın önceliğini doğrula; sonra önerini yarar, maliyet ve risk dengesiyle anlat. I appreciate your concern ... ifadesi itirazı kabul ederken Our recommendation is based on ... kanıta dönüş sağlar. Beklenmedik sorularda bilmediğin ayrıntıyı tahmin etme; doğrulanacak bilgiyi ve sonraki adımı belirt. Görüşme sonunda karar, sorumlu ve takip tarihini açıkça özetle.",
                "Acknowledge → clarify → recommendation → evidence → risk → agreed next step",
                listOf(
                    "I understand your concern about the implementation timeline." to "Uygulama takvimiyle ilgili endişenizi anlıyorum.",
                    "Our recommendation balances reliability with operational cost." to "Önerimiz güvenilirlikle işletme maliyetini dengeliyor.",
                    "I would prefer to verify that estimate before making a commitment." to "Taahhütte bulunmadan önce bu tahmini doğrulamayı tercih ederim.",
                    "To confirm, we will review the results and report back on Friday." to "Teyit etmek için sonuçları inceleyip cuma günü geri bildirim vereceğiz."
                ),
                listOf("Karşı tarafın önceliğini sormadan varsayma.", "Kesin olmayan tahmini taahhüt gibi sunma.", "Görüşmeyi somut sonraki adım olmadan bitirme."),
                listOf("Muhatabın endişesini kendi sözlerinle doğrula.", "Önerini iki gerekçeyle destekle.", "Bir risk veya belirsizlik belirt.", "Sorumlu ve takip adımıyla görüşmeyi kapat.")
            )
        }
        // Match the B1 problem-solving chapter before general catalog rules.
        if ("sorun" in t && "çözüm" in t && ("öner" in t || "oneri" in t)) {
            return LessonBookChapter(
                "Sorunu açıkça tanımlayıp çözüm önermek",
                "Bir sorunu açıklarken gözlenen durumu, etkisini ve bilinen kanıtı birbirinden ayır. B1 düzeyinde önce problemi somut bir cümleyle tanımla, sonra kimleri veya hangi işi etkilediğini açıkla; varsa tarih, tekrar sıklığı veya örnek gibi doğrulanabilir kanıt ekle. Çözüm önerirken I recommend ..., One option would be ... ve We could ... gibi kalıplar kullan ve seçeneğin neden işe yarayabileceğini belirt. Kök nedeni bilmiyorsan kesinmiş gibi sunma; önce kontrol edilmesi gereken bilgiyi söyle.",
                "Observed issue → impact → evidence → options → recommendation/next step",
                listOf(
                    "The system responds slowly when several users sign in." to "Birden fazla kullanıcı giriş yaptığında sistem yavaş yanıt veriyor.",
                    "I recommend checking the logs before changing the configuration." to "Yapılandırmayı değiştirmeden önce kayıtları kontrol etmeyi öneriyorum.",
                    "One option would be to test the update with a small group first." to "Bir seçenek, güncellemeyi önce küçük bir grupla test etmek olurdu.",
                    "We cannot confirm the cause yet, so the next step is to collect more data." to "Nedeni henüz doğrulayamıyoruz; bu yüzden sonraki adım daha fazla veri toplamak."
                ),
                listOf(
                    "Gözlemi doğrulanmamış nedenden ayır.",
                    "Çözümü gerekçesiz sunma.",
                    "Kesin kanıt yoksa may/might/could gibi ölçülü ifadeler kullan.",
                    "Bir sonraki adımı ve sorumlusunu belirsiz bırakma."
                ),
                listOf(
                    "Sorunu somut ve ölçülebilir biçimde tanımla.",
                    "Etkisini ve mevcut kanıtı açıkla.",
                    "En az iki çözüm seçeneği üret.",
                    "Önerini gerekçeyle destekle.",
                    "Gerekliyse hangi bilginin hâlâ doğrulanmadığını belirt."
                )
            )
        }
        return when {


            ("geçmiş zaman" in t && ("düzenli" in t || "düzensiz" in t)) || "past simple" in t ->
                LessonBookChapter("Past Simple ile bitmiş olayları anlatmak",
                    "Past Simple, geçmişte başlayıp bitmiş olayları ve geçmişteki sıralı eylemleri anlatır. Yesterday, last week, two days ago ve in 2024 gibi zaman ifadeleri sık görülür. Düzenli fiillere genellikle -ed eklenir: work → worked; e harfiyle biten live → lived; ünsüz + y ile biten study → studied. Düzensiz fiillerin ikinci hâli ayrı öğrenilir: go → went, take → took. Olumsuz ve soru cümlelerinde did/didn't kullanılır ve ana fiil yalın biçimde kalır. Be fiili was/were ile kurulur; bu cümlelerde did kullanılmaz.",
                    "Positive: subject + V2\nNegative: subject + didn't + V1\nQuestion: Did + subject + V1?\nRegular: work → worked · live → lived · study → studied\nIrregular: go → went · take → took",
                    listOf("We visited the museum last Saturday." to "Geçen cumartesi müzeyi ziyaret ettik.", "She didn't take the early bus." to "Erken otobüse binmedi.", "Did you finish the assignment?" to "Ödevi bitirdin mi?", "They went home after the lesson." to "Dersten sonra eve gittiler.", "I studied English two days ago." to "İki gün önce İngilizce çalıştım."),
                    listOf("Did you went değil Did you go kullan.", "Didn't sonrası V2 değil V1 kullan.", "Study → studyed değil studied yaz.", "Was/were ile eylem fiillerinin geçmiş biçimlerini karıştırma.", "Her düzensiz fiilin -ed aldığını varsayma."),
                    listOf("Eylemin geçmişte bitip bitmediğini belirle.", "Olumlu cümlede V2 kullan.", "Soru ve olumsuzda did/didn't + V1 kur.", "Zaman ifadesini cümleyle eşleştir.", "Düzenli fiilin yazımını ve düzensiz fiilin ikinci hâlini kontrol et."))
            "geçmiş zaman: was/were" in t ->
                LessonBookChapter("Was ve were ile geçmişteki durumları anlatmak",
                    "Was ve were, be fiilinin geçmiş biçimleridir. I, he, she ve it ile was; you, we ve they ile were kullanılır. Olumsuzda was not/wasn't ve were not/weren't gelir. Soruda was/were öznenin önüne geçer. Bu yapı geçmişteki durumları anlatır; eylem fiillerinin geçmiş hâliyle karıştırılmamalıdır.",
                    "I/he/she/it + was · you/we/they + were\nWas/Were + özne ...? · wasn't/weren't\nEylem fiilleri: Did + özne + V1? · didn't + V1",
                    listOf("I was at home yesterday." to "Dün evdeydim.", "They weren't ready." to "Hazır değillerdi.", "Were you tired after school?" to "Okuldan sonra yorgun muydun?"),
                    listOf("They was değil they were kullan.", "Was/were ile did yardımcı fiilini aynı be sorusunda gereksiz yere birleştirme.", "Geçmişteki durum ile geçmişte yapılan eylemi ayırt et."),
                    listOf("Özneye göre was veya were seç.", "Olumsuzda not ekle.", "Soruda was/were fiilini öznenin önüne getir."))
            "alışverişte ingilizce" in t ->
                LessonBookChapter("Mağazada ürün ve fiyat sormak",
                    "Alışverişte fiyatı How much is it? ile sorabilir, ürün istemek için I'd like ... veya Can I have ...? kullanabilirsin. Beden, renk ve miktar sorarken kibar ton seç. Fiyatı duyduktan sonra para birimini ve miktarı tekrar ederek doğrulamak yanlış anlamayı azaltır.",
                    "How much is ...? · Do you have ... in another size? · I'd like ...",
                    listOf("How much is this jacket?" to "Bu ceket ne kadar?", "Do you have it in a smaller size?" to "Bunun daha küçük bedeni var mı?", "I'd like to pay by card." to "Kartla ödemek istiyorum."),
                    listOf("How many is it yerine fiyat için how much kullan.", "Kibar istekte I'd like kullanımı I want'tan daha nazik olabilir.", "Fiyatı ve para birimini duyduğundan emin ol."),
                    listOf("Bir ürünün fiyatını sor.", "Beden veya renk hakkında soru kur.", "Ürün istemek için kibar bir kalıp seç."))
            "şimdiki zaman" in t ->
                LessonBookChapter("Şu anda devam eden eylemler: Present Continuous",
                    "Present Continuous konuşma anında devam eden veya geçici olarak süren eylemleri anlatır. Be fiili (am/is/are) ve -ing biçimindeki ana fiil birlikte kullanılır. He/she/it ile is, I ile am, you/we/they ile are gelir. Stative verbs gibi bazı fiiller genellikle bu yapıda kullanılmaz.",
                    "Özne + am/is/are + V-ing\nOlumsuz: be + not + V-ing · Soru: Am/Is/Are + özne + V-ing?",
                    listOf("I am studying English now." to "Şu anda İngilizce çalışıyorum.", "She isn't working today." to "Bugün çalışmıyor.", "Are they waiting outside?" to "Dışarıda mı bekliyorlar?"),
                    listOf("She is study değil she is studying kullan.", "I am ile -ing fiilini birlikte kullan.", "Every day gibi alışkanlık işaretleri varsa Present Simple gerekip gerekmediğini değerlendir."),
                    listOf("Eylemin şu anda sürüp sürmediğini belirle.", "Özneye uygun am/is/are seç.", "Ana fiile uygun -ing biçimini getir."))
            "yol tarifi" in t || "şehirde yön bulma" in t ->
                LessonBookChapter("Şehirde yer tarif etmek ve yön sormak",
                    "Bir yere ulaşmayı sormak için How can I get to ...? veya Where is ...? kullan. Go straight, turn left/right ve cross the street gibi ifadeler hareketi; next to, opposite ve between gibi edatlar konumu anlatır. Tarif verirken adımları sıralı ve net söyle.",
                    "How can I get to ...? · Go straight · Turn left/right · It is opposite ...",
                    listOf("Go straight and turn left at the bank." to "Dümdüz git ve bankada sola dön.", "The pharmacy is opposite the station." to "Eczane istasyonun karşısında.", "Cross the street at the traffic lights." to "Trafik ışıklarında karşıya geç."),
                    listOf("Left ve right yönlerini karıştırma.", "Konum tarifinde next to ile opposite arasındaki farkı öğren.", "Uzun tarifi tek cümle yerine adımlara böl."),
                    listOf("Bir yerin nasıl bulunacağını sor.", "İki yön talimatını sırayla söyle.", "Bir yerin konumunu edatla tarif et."))
            "sıklık zarfları" in t ->
                LessonBookChapter("Sıklık zarfları ve rutinler",
                    "Always, usually, often, sometimes, rarely ve never eylemin ne sıklıkta yapıldığını anlatır. Be fiilinden sonra (She is usually early), diğer ana fiillerden önce gelir (She usually walks). Every day ve once a week gibi zaman ifadeleri sıklığı daha kesin belirtir.",
                    "subject + usually/often + main verb\nsubject + be + usually/often · once/twice a week",
                    listOf("I usually study after dinner." to "Genellikle akşam yemeğinden sonra ders çalışırım.", "He is often late." to "O sık sık geç kalır.", "We practise twice a week." to "Haftada iki kez pratik yaparız."),
                    listOf("Sıklık zarfını her zaman cümlenin sonuna koyma.", "Be fiili ile ana fiilin yerleşimini ayırt et.", "Never zaten olumsuz anlam verdiği için standart cümlede ayrıca don't ekleme."),
                    listOf("Bir alışkanlığını usually ile anlat.", "Be fiili içeren bir cümlede sıklık zarfı kullan.", "Once/twice a week kalıbıyla sıklık belirt."))
            "sağlık ve günlük sorunlar" in t ->
                LessonBookChapter("Belirtiyi anlatmak ve yardım istemek",
                    "Bir sağlık sorununu anlatırken I have a headache, I feel dizzy veya My throat hurts gibi net ifadeler kullan. Ne zamandır sürdüğünü since yesterday veya for two days ile belirt. Yardım isterken Could you help me? gibi nazik kalıplar seç. Bu bölüm dil öğrenimi içindir, tıbbi tanı yerine geçmez.",
                    "I have a ... · I feel ... · It hurts ... · I've had it for/since ...",
                    listOf("I have a headache." to "Başım ağrıyor.", "I have felt dizzy since this morning." to "Bu sabahtan beri başım dönüyor.", "Could you help me, please?" to "Lütfen bana yardım edebilir misiniz?"),
                    listOf("Belirtiyi abartmadan, açık ve doğru anlat.", "For süreyi, since başlangıç noktasını belirtir.", "Ağrıyan yeri ve ne zamandır sürdüğünü ayırt et."),
                    listOf("İki belirtiyi İngilizce ifade et.", "For ve since ile birer örnek kur.", "Kibar bir yardım isteği oluştur."))
            "seyahat ve konaklama" in t ->
                LessonBookChapter("Rezervasyon ve konaklama konuşmaları",
                    "Konaklama sırasında I'd like to book ..., I have a reservation ve What time is check-out? gibi kalıplar kullanılır. İsim, tarih, kişi sayısı ve oda türünü netleştir. Ulaşımda departure/kalkış, arrival/varış ve platform/peron gibi kelimeleri ayırt et.",
                    "I'd like to book ... · I have a reservation under ... · What time is ...?",
                    listOf("I have a reservation under the name Demir." to "Demir adına rezervasyonum var.", "Could I have the room key, please?" to "Oda anahtarını alabilir miyim lütfen?", "What time does the train leave?" to "Tren saat kaçta kalkıyor?"),
                    listOf("Rezervasyon sahibinin adını açık söyle.", "Check-in giriş, check-out çıkış işlemidir.", "Saat ve tarih bilgisini tekrar ederek doğrula."),
                    listOf("Bir rezervasyon yaptırma cümlesi kur.", "Check-out saatini sor.", "Bir ulaşım aracının kalkış saatini öğren."))
            "yemek siparişi ve miktarlar" in t ->
                LessonBookChapter("Restoranda sipariş ve miktar ifadeleri",
                    "Sipariş verirken I'd like ... veya Could I have ...? kalıpları kullanılır. A glass of water, a piece of cake ve a bottle of juice gibi ölçü ifadeleri sayılamayan isimlerle miktar belirtir. Menü, hesap ve porsiyon kelimelerini öğren; özel bir isteği kibarca açıklayabil.",
                    "I'd like ... · Could I have ...? · a glass/bottle/piece of ...",
                    listOf("Could I have a glass of water, please?" to "Bir bardak su alabilir miyim lütfen?", "I'd like the vegetable soup." to "Sebze çorbası istiyorum.", "Could we have the bill, please?" to "Hesabı alabilir miyiz lütfen?"),
                    listOf("Water gibi sayılamayan isimleri doğrudan sayı ile kullanma; a bottle of water de.", "Siparişte please ile nazik ol.", "Bill hesap, menu menü, order sipariş anlamına gelir."),
                    listOf("Kibar bir yemek siparişi ver.", "Sayılamayan bir içecek için ölçü ifadesi kullan.", "Hesabı istemek için bir cümle kur."))

            "selamlaşma" in t || "tanışma" in t ->
                LessonBookChapter("Selamlaşma ve kendini tanıtma",
                    "İlk karşılaşmada selamlaş, adını söyle ve karşındakinin adını sor. Hello ve Hi gündelik selamlaşmalardır; Good morning sabah kullanılır. Nice to meet you tanışma sırasında söylenir. Resmî ve samimi durumlarda uygun ifadeyi seç.",
                    "Hello, I'm ... · My name is ... · What's your name? · Nice to meet you.",
                    listOf("Hello, I'm Ece." to "Merhaba, ben Ece.", "What is your name?" to "Adın ne?", "Nice to meet you, too." to "Ben de tanıştığıma memnun oldum."),
                    listOf("My name Ece değil My name is Ece kullan.", "İlk tanışmada karşı tarafın adını doğru duymak için gerekirse tekrar iste.", "Good night genellikle vedalaşırken veya gece uyumadan önce kullanılır; selamlaşma değildir."),
                    listOf("Kendini iki cümleyle tanıt.", "Karşındaki kişinin adını sor.", "Selamlaşma ve vedalaşma ifadelerini ayırt et."))
            "günlük temel kelimeler" in t ->
                LessonBookChapter("Günlük kelimeleri cümle içinde öğrenmek",
                    "Kelimeyi yalnızca Türkçe karşılığıyla ezberlemek yerine bir örnek cümleyle öğren. Book, water, food, school ve friend gibi kelimeler günlük bağlamlarda sık kullanılır. A/an, çoğul -s ve temel sıfatlarla birlikte kullanıma dikkat et.",
                    "This is a book. · I drink water. · My friends are at school.",
                    listOf("I have a notebook." to "Bir defterim var.", "The food is hot." to "Yemek sıcak.", "My friend is here." to "Arkadaşım burada."),
                    listOf("Her kelimeyi tek başına ezberleyip cümlede kullanmamazlık yapma.", "Water gibi sayılamayan isimleri doğrudan çoğul yapma.", "İsimden önceki a/an seçimini ilk sese göre kontrol et."),
                    listOf("Beş günlük kelime seç ve her biriyle cümle kur.", "İsmin tekil, çoğul veya sayılamayan olup olmadığını belirle.", "Cümledeki kelimenin anlamını bağlamdan doğrula."))
            "günlük rutinim" in t || "geniş zaman: günlük alışkanlıklar" in t ->
                LessonBookChapter("Günlük rutinleri Present Simple ile anlatmak",
                    "Present Simple alışkanlıklar, rutinler ve genel gerçekler için kullanılır. I/you/we/they ile fiilin yalın hâli; he/she/it ile olumlu cümlede çoğunlukla -s veya -es kullanılır. Do/does soru, don't/doesn't olumsuz yapı kurar. Every day, usually ve at seven gibi zaman ifadeleri anlamı netleştirir.",
                    "I/You/We/They + V1 · He/She/It + V-s\nDo/Does + özne + V1? · don't/doesn't + V1",
                    listOf("I get up at seven every day." to "Her gün saat yedide kalkarım.", "She goes to school by bus." to "Okula otobüsle gider.", "He doesn't drink coffee." to "Kahve içmez.", "Do you study in the evening?" to "Akşamları ders çalışır mısın?"),
                    listOf("She go değil she goes kullan.", "Does he works değil does he work kullan.", "Usually gibi sıklık zarflarını be fiilinden sonra, çoğu ana fiilden önce kullan."),
                    listOf("Cümlenin rutin mi şu anki eylem mi anlattığını belirle.", "He/she/it ile olumlu fiil ekini kontrol et.", "Does/did gibi yardımcı fiilden sonra ana fiili yalın tut."))
            "kısa diyalogları anlama" in t ->
                LessonBookChapter("Kısa diyaloglarda ana bilgi ve bağlam",
                    "Kısa konuşmayı dinlerken önce kimlerin konuştuğunu ve amaçlarını anlamaya çalış. Sonra isim, yer, saat ve istek gibi ayrıntılara odaklan. Bilmediğin bir kelime tüm konuşmayı anlamanı engellemesin; soru, cevap ve bağlam ipuçlarını kullan.",
                    "1. Ana amaç · 2. Kim/nerede/ne zaman · 3. Anahtar kelimeler · 4. Cevabı doğrula",
                    listOf("A: What time is the lesson? B: At nine." to "A: Ders saat kaçta? B: Dokuzda.", "A: Is this seat free? B: Yes, it is." to "A: Bu koltuk boş mu? B: Evet, boş."),
                    listOf("Her kelimeyi anlamaya çalışıp konuşmanın ana fikrini kaçırma.", "Sayıları ve saatleri duyduğunda sorunun ne istediğini kontrol et.", "Metni görmeden önce ilk dinlemede ana fikri bulmayı dene."),
                    listOf("Konuşmanın amacını bir cümleyle söyle.", "İsim, yer veya saati not et.", "İkinci dinlemede cevabını metinle karşılaştır."))
            "sayılar, saatler ve tarihler" in t ->
                LessonBookChapter("Sayı, saat ve tarihleri doğru söylemek",
                    "Saatlerde at nine o'clock, half past seven ve quarter to six gibi kalıplar kullanılır. Günlerle on, ay ve yıllarla in, belirli saatlerle at yaygındır. Tarihlerin söylenişi İngiliz İngilizcesi ve Amerikan İngilizcesinde farklı sırada olabilir; duyduğun biçimi bağlamıyla anlamaya çalış.",
                    "at 8:30 · on Monday · in October · on 28 October / October 28",
                    listOf("The class starts at half past eight." to "Ders sekiz buçukta başlar.", "My appointment is on Friday." to "Randevum cuma günü.", "Her birthday is in May." to "Onun doğum günü mayısta."),
                    listOf("Saat için on, gün için at kullanma.", "Quarter past çeyrek geçe, quarter to ise bir sonraki saate çeyrek kala demektir.", "Tarihte gün ve ay sırasının bölgeye göre değişebileceğini unutma."),
                    listOf("Üç farklı saati İngilizce söyle.", "Bir gün ve bir ayı cümlede kullan.", "At/on/in seçimini zaman ifadesine göre kontrol et."))
            "there is / there are" in t ->
                LessonBookChapter("Bir yerde ne bulunduğunu anlatmak",
                    "There is tekil isim veya sayılamayan isimle, there are çoğul isimlerle kullanılır. Soru oluştururken is/are başa gelir; olumsuzda isn't/aren't kullanılır. A/an ve some/any ifadelerini isim türüne göre seç.",
                    "There is + tekil/sayılamayan isim\nThere are + çoğul isim\nIs there ...? / Are there ...?",
                    listOf("There is a pharmacy near the school." to "Okulun yakınında bir eczane var.", "There are two chairs in the room." to "Odada iki sandalye var.", "Is there any water?" to "Biraz su var mı?"),
                    listOf("There is two books değil there are two books kullan.", "Bir yerin konumunu belirtirken in, on, next to gibi edatları ekle.", "Soru cümlesinde is/are yardımcı fiilini başa getir."),
                    listOf("İsmin tekil mi çoğul mu olduğunu belirle.", "There is veya there are seç.", "Bir olumlu, bir soru ve bir olumsuz örnek kur."))
            "aile ve insanları tanımlama" in t ->
                LessonBookChapter("Aile üyelerini ve kişilik özelliklerini anlatmak",
                    "Family kelimeleri aile ilişkilerini, possessive adjectives ise kime ait olduklarını anlatır: my, your, his, her, our ve their. Kişilik özelliklerinde friendly, kind ve patient; görünüşte tall, short ve curly hair gibi ifadeler kullanılabilir. Be fiilini özneyle eşleştir.",
                    "This is my ... · He/She is ... · He/She has ...",
                    listOf("This is my older sister." to "Bu benim ablam.", "My father is patient and kind." to "Babam sabırlı ve naziktir.", "She has curly hair." to "Onun kıvırcık saçları var."),
                    listOf("He is hair curly gibi kelime sırasını bozma; She has curly hair kullan.", "His ve her seçiminde eşyanın sahibini kontrol et.", "Be fiilini özneye göre seç."),
                    listOf("Üç aile üyesini İngilizce adlandır.", "Bir kişiyi iki sıfatla anlat.", "My/his/her/our kullanımını kontrol et."))
            "temel sıfatlar ve betimleme" in t ->
                LessonBookChapter("Kişi ve nesneleri sıfatlarla betimlemek",
                    "Sıfatlar bir kişinin, nesnenin veya yerin özelliğini anlatır. Çoğunlukla isimden önce (a small room) veya be fiilinden sonra (the room is small) gelir. Sıfatlar İngilizcede genellikle isimle çoğul uyumu almaz: two small rooms.",
                    "adjective + noun · subject + be + adjective",
                    listOf("It is a quiet street." to "Sessiz bir sokak.", "The engine is noisy." to "Motor gürültülü.", "They are friendly people." to "Onlar arkadaş canlısı insanlar."),
                    listOf("Two smalls rooms değil two small rooms kullan.", "Be fiilini atlama: The room clean değil The room is clean.", "Sıfatı zarftan ayırt et; slowly bir zarf, slow bir sıfattır."),
                    listOf("Bir nesneyi üç sıfatla tarif et.", "Sıfatı isimden önce ve be fiilinden sonra kullan.", "Sıfatın çoğul isimle biçiminin değişmediğini kontrol et."))
            "a / an ve tekil isimler" in t ->
                LessonBookChapter("Tekil sayılabilen isimlerde a ve an",
                    "A ve an, tekil sayılabilen isimlerden önce gelir. A sessiz sesle başlayan, an sesli sesle başlayan sözcüklerden önce kullanılır. Karar yazılı harfe değil ilk sese dayanır: an hour ama a university. Belirli bir şeyden ikinci kez söz ederken the kullanılabilir.",
                    "a + consonant sound · an + vowel sound · a/an + singular countable noun",
                    listOf("a technician" to "bir teknisyen", "an engineer" to "bir mühendis", "an hour" to "bir saat", "a university" to "bir üniversite"),
                    listOf("An university değil a university kullan.", "Tekil sayılabilen ismi çoğunlukla a/an veya the olmadan bırakma.", "Çoğul ve sayılamayan isimlerle a/an kullanma."),
                    listOf("İsmin tekil ve sayılabilir olduğunu doğrula.", "İlk sesin ünlü olup olmadığını belirle.", "İlk söz ediş ile bilinen şey arasındaki farkı düşün."))
            "can / can't" in t ->
                LessonBookChapter("Can ve can't ile yetenek ve izin",
                    "Can bir yeteneği veya izin verilen bir şeyi anlatır. Can't, yapamama veya izin olmaması anlamına gelebilir; bağlam belirleyicidir. Can bütün öznelerle aynı kalır ve ardından to almayan yalın fiil gelir. Soru cümlesinde can öznenin önüne geçer.",
                    "Özne + can/can't + V1\nCan + özne + V1?",
                    listOf("I can swim." to "Yüzebilirim.", "She can't drive." to "Araba kullanamaz.", "Can you help me?" to "Bana yardım edebilir misin?"),
                    listOf("He cans değil he can kullan.", "Can to swim değil can swim kullan.", "Can't bazen yeteneksizlik, bazen yasak anlamı taşır; bağlama bak."),
                    listOf("Bir yeteneğini can ile anlat.", "Yapamadığın bir şeyi can't ile söyle.", "Can ile bir soru kur ve kısa cevap ver."))

            "zamir" in t || "pronoun" in t || "to be" in t || "am/is/are" in t || "am is are" in t ->
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
                    "Gelecek hakkında konuşurken yalnızca Türkçe çeviriye değil, konuşma durumuna bak. Will çoğunlukla konuşma anında verilen karar, söz verme ve kişisel tahminde kullanılır. Be going to önceden yapılmış planlarda ve görülen kanıta dayalı tahminlerde sık görülür. Olumsuzda will not/won't veya be not going to; soruda will öznenin önüne gelir, going to sorusunda am/is/are öznenin önüne geçer. Zaman çizelgesi veya plan varsa going to sıkça uygundur; ancak gerçek konuşmada bağlam iki yapının da kullanılmasına izin verebilir.",
                    "Will: Özne + will + V1 · Olumsuz: won't + V1 · Soru: Will + özne + V1?\nGoing to: Özne + am/is/are + going to + V1 · Soru: Am/Is/Are + özne + going to + V1?",
                    listOf("I'll help you with that bag." to "O çantayla sana yardım edeceğim. (anlık karar)", "We are going to visit our aunt on Saturday." to "Cumartesi teyzemizi ziyaret edeceğiz. (plan)", "Look at those dark clouds; it is going to rain." to "Şu koyu bulutlara bak; yağmur yağacak. (kanıt)", "I won't forget your advice." to "Tavsiyeni unutmayacağım. (söz)", "Are they going to travel by train?" to "Trenle seyahat edecekler mi?"),
                    listOf("Will sonrası fiile -s veya to ekleme: will go kullan.", "Going to yapısında özneye uygun am/is/are kullan.", "Olumsuzda won't go veya isn't going to go de; won't to go deme.", "Soru yaparken yardımcı fiili öznenin önüne getir.", "Her gelecek cümlesinin yalnızca tek bir doğru biçimi olduğunu varsayma; niyet ve bağlamı değerlendir."),
                    listOf("Anlık karar, söz, önceden plan ve kanıta dayalı tahmini ayırt et.", "Will ile olumlu, olumsuz ve soru cümlesi kur.", "Going to yapısında am/is/are biçimini seç.", "Her iki yapıda da ana fiili V1 olarak kullan.", "Bir planını ve bir tahminini farklı yapılarla anlat."))
            ("conditional" in t || "koşul" in t) && !("ileri koşul cümleleri" in t || "advanced-conditionals" in t || "mixed conditional" in t) ->
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
            "görüşleri karşılaştırma" in t || "comparing arguments" in t ->
                LessonBookChapter("Karşıt görüşleri kanıtla karşılaştırmak",
                    "B2 düzeyinde iki görüşü karşılaştırırken yalnızca hangisine katıldığını söyleme. Her görüşün ana iddiasını, dayandığı kanıtı, varsayımlarını ve sınırlamalarını belirle. Although ve while karşıtlığı kurabilir; whereas iki durumu doğrudan karşılaştırır; nevertheless önceki noktaya rağmen geçerli kalan sonucu gösterir. Karşı görüşün en güçlü tarafını adil biçimde özetle, ardından kanıtın hangi iddiayı daha iyi desteklediğini açıkla. Sonuçta belirsizliği saklama ve kanıtın izin verdiğinden daha kesin konuşma.",
                    "Claim A + supporting evidence → Claim B + supporting evidence → comparison/limitation → balanced conclusion\nWhereas/while = contrast · although = concession · nevertheless = qualification",
                    listOf("Whereas the first proposal is cheaper, the second offers stronger long-term reliability." to "İlk öneri daha ucuzken ikincisi uzun vadede daha yüksek güvenilirlik sunuyor.", "Although the survey supports the change, its small sample limits the conclusion." to "Anket değişikliği desteklese de küçük örneklem sonucu sınırlar.", "Both options could work; however, the available evidence favours a limited trial first." to "İki seçenek de işe yarayabilir; ancak mevcut kanıt önce sınırlı bir denemeyi destekliyor.", "The opposing view raises a valid concern, nevertheless it does not address the latest results." to "Karşıt görüş geçerli bir endişe dile getiriyor; yine de son sonuçları ele almıyor."),
                    listOf("Karşıt görüşü zayıflatmak için çarpıtarak aktarma.", "Ucuz olmayı tek başına en iyi seçenek olmanın kanıtı sayma.", "Although ile nevertheless ifadelerini aynı dilbilgisel yapıda kullanma.", "Korelasyonu neden-sonuç kanıtı gibi sunma.", "Kanıt sınırlıysa kesin sonuç ilan etme."),
                    listOf("Her görüşün ana iddiasını bir cümleyle yaz.", "İki görüşün dayandığı kanıtı karşılaştır.", "En az bir sınırlama veya varsayım belirt.", "Karşıt görüşün güçlü yanını adil biçimde açıkla.", "Kanıtla uyumlu ve dengeli bir sonuç oluştur."))
            "compar" in t || "karşılaştır" in t ->
                LessonBookChapter("İki veya daha fazla şeyi karşılaştırmak",
                    "Kısa sıfatlar genellikle -er alır ve than ile karşılaştırılır. Uzun sıfatlarda more kullanılır. Üstünlük bildirirken the -est veya the most yapısı tercih edilir. Good/bad gibi bazı sıfatların düzensiz biçimleri vardır.",
                    "Kısa sıfat: adjective-er + than\nUzun sıfat: more + adjective + than\nÜstünlük: the + adjective-est / the most + adjective",
                    listOf("This route is shorter than that one." to "Bu rota diğerinden daha kısa.", "This task is more difficult." to "Bu görev daha zor.", "It is the safest option." to "En güvenli seçenek bu."),
                    listOf("More easier gibi iki karşılaştırma işaretini birlikte kullanma.", "Than ile the most yapılarını karıştırma.", "Good → better → best değişimini öğren."),
                    listOf("Sıfatın kısa mı uzun mu olduğunu değerlendir.", "Than veya üstünlük yapısını seç.", "Düzensiz sıfatları kontrol et."))
            "article" in t || "a/an" in t || "the " in t || "articles" in t || "tanımlık" in t -> 
                LessonBookChapter("A, an ve the seçimini anlamak",
                    "A ve an, tekil ve sayılabilir bir isimden ilk kez veya herhangi bir örnekten söz ederken kullanılır. A/an seçimi yazılı harfe değil, sonraki kelimenin sesine bağlıdır: an hour ama a university. The, konuşan ve dinleyenin hangi şeyden söz edildiğini bildiği durumlarda kullanılır. Çoğul veya sayılamayan isimler genel anlamda kullanıldığında çoğu zaman tanımlık almaz.",
                    "a + sessiz sesle başlayan tekil isim\nan + sesli sesle başlayan tekil isim\nthe + belirli isim",
                    listOf("I saw a bird. The bird was blue." to "Bir kuş gördüm. Kuş maviydi.", "She is an engineer." to "O bir mühendis.", "Water is important." to "Su önemlidir."),
                    listOf("An university değil a university kullan; başlangıç sesi /juː/ şeklindedir.", "Tekil sayılabilir ismi çoğunlukla tek başına bırakma.", "Her isimden önce the getirme."),
                    listOf("İsmin tekil ve sayılabilir olup olmadığını belirle.", "İlk söz ediş mi, bilinen bir şey mi olduğunu sor.", "A/an için ilk harfi değil ilk sesi dinle."))
            "modal" in t || "kip" in t || "must" in t || "should" in t || "can/could" in t -> 
                LessonBookChapter("Yetenek, izin, zorunluluk ve tavsiye",
                    "Modal yardımcı fiiller ana fiile anlam katar. Can yetenek veya izin, could geçmişteki yetenek ya da nazik rica, must güçlü zorunluluk, have to gereklilik, should ise tavsiye anlatabilir. Modal fiilden sonra ana fiil yalın biçimde gelir; üçüncü tekil şahısta -s almaz.",
                    "Özne + can/could/must/should + V1\nOlumsuz: modal + not + V1\nSoru: Modal + özne + V1?",
                    listOf("You should check the instructions." to "Talimatları kontrol etmelisin.", "Passengers must wear seat belts." to "Yolcular emniyet kemeri takmalıdır.", "Can I ask a question?" to "Bir soru sorabilir miyim?"),
                    listOf("She can speaks değil she can speak kullan.", "Mustn't yasak anlamı verebilir; don't have to ise zorunlu olmama anlamındadır.", "Modal fiilden sonra to kullanma; have to bunun istisnai farklı yapısıdır."),
                    listOf("Cümlenin yetenek, tavsiye, izin veya zorunluluk anlattığını seç.", "Modal fiili özneye göre değiştirme.", "Ardından V1 kullandığını doğrula."))
            "relative" in t || "ilgi cümle" in t || "who/which" in t -> 
                LessonBookChapter("İsimleri ayrıntılandıran relative clauses",
                    "Relative clause, bir kişi veya nesne hakkında ek bilgi verir. Who kişiler, which nesneler ve hayvanlar, that ise birçok tanımlayıcı cümlede kişi veya nesneler için kullanılabilir. Where yerleri belirtir. Zamirin görevi, cümlede özne mi nesne mi olduğuna bağlıdır; nesne zamiri bazı tanımlayıcı yapılarda çıkarılabilir.",
                    "person + who/that + fiil\nthing + which/that + fiil\nplace + where + özne + fiil",
                    listOf("The pilot who helped us was kind." to "Bize yardım eden pilot nazikti.", "This is the tool that I need." to "Bu, ihtiyacım olan araç.", "The city where I was born is small." to "Doğduğum şehir küçük."),
                    listOf("Kişi için who kullanımı çoğunlukla daha doğaldır.", "Where sonrasında yerle ilgili cümle yapısını tamamla.", "Tanımlayıcı olmayan ek bilgide virgül kullanımına dikkat et."),
                    listOf("Hangi isim hakkında bilgi verildiğini bul.", "Kişi, nesne veya yer için uygun bağlacı seç.", "Relative clause içindeki fiil ve özne bağlantısını kontrol et."))
            "phrasal" in t || "öbek fiil" in t -> 
                LessonBookChapter("Phrasal verbs: fiil ve parçacık birlikte",
                    "Phrasal verb, fiilin bir edat veya zarfla birleşerek yeni ya da özel bir anlam kazanmasıdır. Anlam çoğu zaman kelimelerin tek tek anlamından tahmin edilemez. Bazı ayrılabilen yapılarda nesne araya girebilir; zamir kullanıldığında ayrılabilen fiillerde zamir genellikle araya gelir.",
                    "verb + particle: find out, give up, carry on\nAyrılabilen: turn the light off / turn it off",
                    listOf("Please find out the answer." to "Lütfen cevabı öğren.", "Don't give up." to "Vazgeçme.", "Turn it off." to "Onu kapat."),
                    listOf("Phrasal verb'i yalnızca kelime kelime çevirme.", "Ayrılabilen fiilde zamirin konumunu kontrol et.", "Her phrasal verb'in aynı dilbilgisi davranışını gösterdiğini varsayma."),
                    listOf("Fiili parçacığıyla birlikte öğren.", "Kısa örnek cümle yaz.", "Ayrılabilir olup olmadığını örnekten kontrol et."))
            "reported" in t || "dolaylı anlatım" in t -> 
                LessonBookChapter("Söylenenleri reported speech ile aktarmak",
                    "Reported speech, birinin sözünü birebir alıntılamadan aktarmaya yarar. Geçmişte söylenmiş bir cümleyi aktarırken, bağlama göre zaman bir adım geriye kayabilir: present simple → past simple, will → would, can → could. Zamirler ve today/tomorrow gibi zaman ifadeleri de anlatıcının bağlamına göre değişebilir; her durumda mekanik değişim gerekmez.",
                    "said (that) + aktarılan cümle\ntell + kişi + (that) + cümle\nwill → would; can → could (bağlama göre)",
                    listOf("“I am tired.” → She said (that) she was tired." to "“Yorgunum.” → Yorgun olduğunu söyledi.", "“I will call.” → He said he would call." to "“Arayacağım.” → Arayacağını söyledi."),
                    listOf("Tell kullanırken çoğunlukla kime söylendiğini belirt.", "Zamirleri konuşan kişiye göre güncelle.", "Zaman kaymasını her bağlamda zorunlu kural sanma."),
                    listOf("Kimin konuştuğunu belirle.", "Zamir ve zaman ifadelerini bağlama uyarla.", "Aktarılan cümlenin anlamını koru."))
            "gerund" in t || "infinitive" in t || "gerund" in t || "to + verb" in t -> 
                LessonBookChapter("Gerund ve infinitive seçimini öğrenmek",
                    "Gerund, -ing biçiminin isim gibi kullanılmasıdır; infinitive ise çoğunlukla to + V1 yapısıdır. Bazı fiillerden sonra belirli bir biçim tercih edilir: enjoy doing, want to do. Bazı fiiller iki yapıyı da alabilir ama anlam değişebilir; bu nedenle fiili örnekle birlikte öğrenmek önemlidir.",
                    "enjoy + V-ing\nwant/decide/hope + to + V1\npreposition + V-ing",
                    listOf("I enjoy reading." to "Okumaktan hoşlanırım.", "They decided to leave." to "Ayrılmaya karar verdiler.", "She is interested in learning." to "Öğrenmekle ilgileniyor."),
                    listOf("Enjoy to read değil enjoy reading kullan.", "Edatlardan sonra çoğunlukla -ing biçimini kullan.", "Her fiilin ardından aynı biçimin geldiğini varsayma."),
                    listOf("Ana fiilin hangi yapıyı istediğini öğren.", "Edat varsa ardından -ing gerekip gerekmediğini kontrol et.", "Örnek cümleyle kalıbı pekiştir."))
            "preposition" in t || "edat" in t || "in/on/at" in t -> 
                LessonBookChapter("Yer ve zaman edatlarını bağlama göre seçmek",
                    "In, on ve at hem zaman hem yer anlatımında kullanılır. Zaman için in months/years ve uzun dönemlerde, on gün ve tarihlerde, at ise belirli saatlerde sık kullanılır. Yer için in kapalı alan veya bölge, on yüzey, at ise belirli nokta/konum fikrini verebilir. Bunlar genel örüntülerdir; kalıplaşmış ifadeler ayrıca öğrenilmelidir.",
                    "Time: in July / on Monday / at 8:00\nPlace: in a room / on the table / at the station",
                    listOf("The meeting is on Friday." to "Toplantı cuma günü.", "The keys are on the table." to "Anahtarlar masanın üzerinde.", "We arrived at noon." to "Öğlen vardık."),
                    listOf("Gün için genellikle on, saat için at kullanılır.", "Türkçedeki ekleri doğrudan İngilizce edata çevirmeye çalışma.", "Depend on ve interested in gibi sabit eşleşmeleri ayrı öğren."),
                    listOf("Zaman mı yer mi anlatıldığını belirle.", "İfadenin nokta, yüzey veya alan anlamına bak.", "Sabit kelime eşleşmelerini not et."))
            "deneyimleri anlatmak" in t || "present perfect" in t ->
                LessonBookChapter("Deneyimleri Present Perfect ile anlatmak",
                    "Present Perfect geçmişteki bir deneyimi, sonucu bugüne uzanan bir durumu veya henüz tamamlanmamış bir zaman dilimindeki olayı anlatır. Have/has yardımcı fiili ve fiilin üçüncü hâli (V3) kullanılır. Belirli ve bitmiş bir geçmiş zamanı vurguluyorsan Past Simple çoğunlukla daha uygundur.",
                    "I/you/we/they + have + V3 · he/she/it + has + V3\nHave/Has + subject + V3? · ever/never/already/yet",
                    listOf("I have visited London twice." to "Londra'yı iki kez ziyaret ettim.", "She has not finished the report yet." to "Raporu henüz bitirmedi.", "Have you ever used this tool?" to "Bu aleti hiç kullandın mı?"),
                    listOf("Have went değil have gone kullan.", "Yesterday gibi bitmiş zaman ifadesiyle Present Perfect'i gelişigüzel birleştirme.", "Have/has seçimini özneye göre yap."),
                    listOf("Deneyim mi, bitmiş geçmiş olay mı anlattığını belirle.", "Have/has seç ve V3 kullan.", "Ever, never, already ve yet ifadelerinin yerini kontrol et."))
            "fikir belirtme" in t || "opinions" in t ->
                LessonBookChapter("Fikir belirtmek ve gerekçelendirmek",
                    "Bir görüşü açıkça belirt, ardından gerekçe ve mümkünse kanıt ver. I think ve In my view görüşü tanıtır; because gerekçe verir; however karşıtlık, therefore sonuç bildirir. Daha profesyonel bir tonda iddianın kesinliğini kanıta göre ayarla.",
                    "In my view, ... · The main reason is ... · However, ... · Therefore, ...",
                    listOf("In my view, the revised plan is more practical because it saves time." to "Bana göre güncellenmiş plan daha uygulanabilir çünkü zaman kazandırıyor.", "However, we should check the evidence first." to "Ancak önce kanıtı kontrol etmeliyiz."),
                    listOf("Görüşü kanıt gibi sunma.", "However ile because aynı ilişkiyi kurmaz.", "Gerekçeyi açıklamadan yalnızca I agree/I disagree deme."),
                    listOf("Görüşünü bir cümleyle belirt.", "En az bir gerekçe ekle.", "Karşı görüş varsa saygılı biçimde ele al."))
            ("ihtiyat ve kesinlik" in t || "hedging" in t) ->
                LessonBookChapter("Akademik dilde ihtiyat ve kesinlik düzeyini ayarlamak",
                    "Akademik ve profesyonel İngilizcede iddianın gücü, mevcut kanıtın gücüyle uyumlu olmalıdır. May, might, could, appears to, seems to, likely ve suggests gibi ifadeler belirsizliği veya kanıtın sınırını gösterir. Bu ifadeler metni zayıflatmak için değil, neyin bilindiğini ve neyin henüz kesinleşmediğini dürüstçe ayırmak için kullanılır. Kesin sonuçlar için güçlü kanıt gerekir; sınırlı örneklem veya gözlemsel veri tek başına neden-sonuç ilişkisini kanıtlamaz.",
                    "Strong evidence: the results demonstrate ... (only when justified)\nQualified claim: the results suggest/appear to ...\nPossibility: may/might/could + V1\nLikelihood: is likely to / is unlikely to",
                    listOf("The findings suggest a possible improvement, although the sample was small." to "Bulgular olası bir iyileşmeye işaret ediyor, ancak örneklem küçüktü.", "The delay may be related to the revised process." to "Gecikme güncellenen süreçle ilişkili olabilir.", "Further testing is likely to clarify the cause." to "Ek testlerin nedeni açıklığa kavuşturması muhtemel."),
                    listOf("May veya might ifadesini kesinlik gibi çevirme.", "Suggests ifadesini proves ile eş anlamlı kullanma.", "Sınırlamaları gizleyip kanıtın desteklediğinden güçlü sonuç çıkarma."),
                    listOf("İddianın dayandığı kanıtı belirt.", "Kesinlik düzeyini kanıta göre seç.", "Belirsizliğin nedenini açıkla.", "Sonucu sınırlayan örneklem veya yöntem bilgisini ekle."))
            ("tutarlılık ve akademik bağlaçlar" in t || "cohesion" in t) ->
                LessonBookChapter("Akademik metinde tutarlılık ve bağlaçlar",
                    "Bağlaçlar yalnızca cümleleri birbirine eklemez; fikirler arasındaki mantıksal ilişkiyi gösterir. However ve nevertheless karşıtlık, therefore ve consequently sonuç, whereas karşılaştırma, moreover ek bilgi, whereas/but karşıtlık ve for example örnek verir. Buna ek olarak zamirlerin neye gönderme yaptığı, aynı terimin tutarlı kullanılması ve paragraf sırası metnin akışını etkiler. Bağlacı Türkçe karşılığına göre ezberlemek yerine iki fikir arasındaki ilişkiye göre seç.",
                    "Addition: moreover/furthermore · Contrast: however/whereas · Result: therefore/consequently · Example: for example · Concession: although/nevertheless",
                    listOf("The sample was limited; therefore, the findings should be interpreted cautiously." to "Örneklem sınırlıydı; bu nedenle bulgular dikkatle yorumlanmalıdır.", "The first option is faster, whereas the second is less expensive." to "İlk seçenek daha hızlıyken ikincisi daha ucuz.", "The method is useful. However, it requires additional training." to "Yöntem yararlıdır. Ancak ek eğitim gerektirir."),
                    listOf("However ile therefore ifadelerini aynı anlamda kullanma.", "Although yan cümlesiyle nevertheless bağlacını gelişigüzel tek yapıda birleştirme.", "Zamirin hangi isim veya fikre gönderme yaptığını belirsiz bırakma."),
                    listOf("İki fikir arasındaki mantıksal ilişkiyi belirle.", "Bu ilişkiye uygun bağlaç seç.", "Gönderme yapan zamirlerin referansını kontrol et.", "Paragrafın ana fikre mantıklı sırayla ilerlediğini doğrula."))
            ("parafraz" in t || "paraphras" in t) ->
                LessonBookChapter("Parafraz yaparken anlamı ve kaynak sınırlarını korumak",
                    "Parafraz, bir fikri yalnızca birkaç kelimeyi eş anlamlılarıyla değiştirerek değil, cümle yapısını ve anlatım yolunu değiştirerek kendi sözlerinle ifade etmektir. Ana anlamı, önemli koşulları ve belirsizlik düzeyini koru. Kaynağın fikrini kendi yorumundan ayır; belirli bir kaynağa dayanan düşünceyi uygun biçimde kaynak göster. Parafraz, kaynak metni taklit etmek veya sonucu olduğundan daha kesin sunmak için kullanılmamalıdır.",
                    "Read for meaning → identify key claim and limits → restate in a new structure → compare with source → attribute the idea",
                    listOf("Source: The results may indicate a link, but more research is needed." to "Kaynak: Sonuçlar bir bağlantıya işaret ediyor olabilir, ancak daha fazla araştırma gerekiyor.", "Paraphrase: The findings point to a possible relationship; further study is still required." to "Parafraz: Bulgular olası bir ilişkiye işaret ediyor; ek araştırma hâlâ gerekli."),
                    listOf("Yalnızca birkaç kelimeyi değiştirip cümle yapısını kopyalama.", "May veya might gibi belirsizlik işaretlerini kaldırma.", "Kaynağın iddiasını kendi bulgunmuş gibi sunma."),
                    listOf("Kaynağın ana fikrini belirle.", "Önemli koşul ve sınırlamaları koru.", "Farklı cümle yapısı kullan.", "Kaynağa atıf gerekip gerekmediğini kontrol et.", "Parafrazı kaynakla karşılaştırıp anlam kaymasını önle."))
            ("ileri koşul cümleleri" in t || "advanced-conditionals" in t || "mixed conditional" in t) ->
                LessonBookChapter("İleri koşul cümleleri ve mixed conditionals",
                    "B2 düzeyinde koşul cümleleri yalnızca gerçekçi gelecek olasılıklarını değil, geçmişteki farklı bir kararın bugünkü sonucunu veya bugünkü bir durumun geçmişteki etkisini de anlatabilir. Mixed conditional yapısında if bölümü ile sonuç bölümü farklı zamanlara gönderme yapar. Yapıyı seçerken koşulun hangi zamanda olduğunu ve sonucun hangi zamanda gerçekleştiğini önce belirle. Bu yapılar varsayımsaldır; geçmişi değiştirdiğini değil, alternatif bir senaryoyu anlattığını gösterir.",
                    "Past condition → present result: If + past perfect, would + V1\nPresent condition → past result: If + past simple, would have + V3",
                    listOf("If I had accepted the offer, I would live in London now." to "Teklifi kabul etmiş olsaydım, şimdi Londra'da yaşıyor olurdum.", "If she were more organised, she would have submitted the form on time." to "Daha düzenli biri olsaydı, formu zamanında teslim etmiş olurdu."),
                    listOf("If bölümündeki zamanı sonuç bölümündeki zamanla otomatik olarak aynı yapma.", "Would have sonrasında V1 değil V3 kullan.", "Varsayımsal durumu gerçek bir geçmiş olayı gibi aktarma."),
                    listOf("Koşulun geçmişe mi bugüne mi ait olduğunu belirle.", "Sonucun zamanını belirle.", "İki bölümü uygun mixed conditional kalıbıyla eşleştir.", "Varsayım ile gerçek bilgiyi ayır."))
            "perfect continuous" in t ->
                LessonBookChapter("Perfect Continuous ile süreyi vurgulamak",
                    "Perfect Continuous yapıları bir eylemin süresini veya devam eden sürecini öne çıkarır. Present Perfect Continuous have/has been + V-ing, Past Perfect Continuous had been + V-ing ile kurulur. Hangi zamanın seçileceği, referans alınan zaman ve eylemin bağlamına bağlıdır.",
                    "Present: have/has been + V-ing\nPast: had been + V-ing",
                    listOf("I have been studying for two hours." to "İki saattir ders çalışıyorum.", "They had been waiting before the bus arrived." to "Otobüs gelmeden önce bekliyorlardı."),
                    listOf("Been sonrasında V-ing kullan.", "For süreyi, since başlangıç noktasını belirtir.", "Her fiilin continuous biçimde doğal olmadığını unutma."),
                    listOf("Referans zamanını belirle.", "Have/has veya had seç.", "Been + V-ing biçimini ve süre ifadesini kontrol et."))
            "inversion" in t || "vurgu ve inversion" in t ->
                LessonBookChapter("Inversion ile vurgu oluşturmak",
                    "Inversion, özellikle resmî veya edebî İngilizcede belirli olumsuz ya da sınırlayıcı ifadeler cümle başına geldiğinde yardımcı fiilin öznenin önüne geçmesidir. Her cümlede kullanılmaz; vurgu ve üslup amacıyla sınırlı kullanılır.",
                    "Rarely/Never/Seldom + auxiliary + subject + verb",
                    listOf("Rarely have we seen such a clear result." to "Bu kadar açık bir sonucu nadiren gördük.", "Not only did she explain the issue, but she also suggested a solution." to "Sorunu açıklamakla kalmadı, çözüm de önerdi."),
                    listOf("Yardımcı fiili atlama.", "Past Simple'da did kullanıldığında ana fiili yalın tut.", "Inversion'ı gündelik her cümleye uygulama."),
                    listOf("Cümle başındaki sınırlayıcı ifadeyi belirle.", "Gerekli yardımcı fiili öznenin önüne getir.", "Ana fiilin biçimini kontrol et."))
            "deyimler ve dil düzeyi" in t || "idioms" in t || "register" in t ->
                LessonBookChapter("Deyimleri bağlam ve dil düzeyine göre seçmek",
                    "Deyimlerin anlamı çoğu zaman kelimelerin tek tek anlamından çıkarılamaz. Register ise ifadenin resmî, nötr veya samimi oluşunu anlatır. Bir deyimi kullanmadan önce bağlamın, dinleyicinin ve iletişim amacının uygun olup olmadığını kontrol et.",
                    "formal: assist · neutral: help · informal: give someone a hand",
                    listOf("Could you give me a hand with this task?" to "Bu görevde bana yardım edebilir misin?", "We need to resolve the issue before the meeting." to "Toplantıdan önce sorunu çözmemiz gerekiyor."),
                    listOf("Deyimi kelimesi kelimesine çevirme.", "Samimi deyimleri resmî raporlarda gelişigüzel kullanma.", "Register seçimini yalnızca kelimenin zorluğuna göre yapma."),
                    listOf("İletişim ortamını belirle.", "İfadenin resmiyet düzeyini seç.", "Deyimin anlamını cümle bağlamında doğrula."))
            "eleştirel okuma" in t || "critical reading" in t ->
                LessonBookChapter("Eleştirel okuma ve çıkarım yapma",
                    "Eleştirel okumada yazarın iddiasını, sunduğu kanıtı ve varsayımlarını birbirinden ayır. Bir metindeki bilgi, yorum ve çıkarım aynı şey değildir. Kaynağın kapsamını ve kanıtın iddiayı gerçekten destekleyip desteklemediğini sorgula.",
                    "Claim → evidence → assumption → limitation → conclusion",
                    listOf("The survey suggests a trend, but it does not prove the cause." to "Anket bir eğilime işaret ediyor ancak nedeni kanıtlamıyor.", "The conclusion is stronger than the evidence provided." to "Sonuç, sunulan kanıttan daha iddialı."),
                    listOf("Birlikte görülmeyi tek başına neden-sonuç kanıtı sayma.", "Yazarın görüşünü doğrulanmış bilgiyle karıştırma.", "Kaynağın sınırlamalarını göz ardı etme."),
                    listOf("Ana iddiayı bul.", "İddiayı destekleyen kanıtı işaretle.", "Varsayım ve sınırlamaları not et."))
            "toplantılarda fikir savunma" in t || "meetings" in t ->
                LessonBookChapter("Toplantıda görüş sunmak ve yanıtlamak",
                    "Toplantıda söz alırken görüşünü kısa bir cümleyle belirt, ardından gerekçeyi açıkla. Katılma, nazikçe itiraz etme ve konuşmayı netleştirme ifadeleri kullan. Başkasının sözünü kesmek yerine uygun bir geçiş ifadesi seç.",
                    "I'd like to add ... · I see your point, but ... · Could we clarify ...?",
                    listOf("I'd like to add one point about the timeline." to "Zaman çizelgesi hakkında bir nokta eklemek istiyorum.", "I see your point, but the latest figures suggest another option." to "Ne demek istediğinizi anlıyorum ancak son rakamlar başka bir seçeneğe işaret ediyor."),
                    listOf("Katılmadığında kişiyi değil fikri değerlendir.", "Gerekçesiz itirazı uzatma.", "Önerilen kararı ve sonraki adımı özetle."),
                    listOf("Görüşünü net ifade et.", "Bir gerekçe veya kanıt sun.", "Nazik bir karşı görüş ve öneri oluştur."))
            "veri ve sonuçları raporlama" in t || "reporting data" in t ->
                LessonBookChapter("Veriyi dikkatli yorumlayıp raporlamak",
                    "Raporlarda verinin neyi gösterdiğini ve neyi göstermediğini ayır. Increase, decrease, remain stable ve compared with gibi ifadeler değişimi anlatır. Yaklaşık değerler için yaklaşık dil kullan; kanıtın desteklemediği kesin sonuçlar çıkarma.",
                    "The figure increased by ... · compared with ... · the data suggests ...",
                    listOf("Sales increased by eight percent compared with the previous quarter." to "Satışlar önceki çeyreğe kıyasla yüzde sekiz arttı.", "The data suggests a change, but further analysis is needed." to "Veriler bir değişime işaret ediyor ancak ek analiz gerekiyor."),
                    listOf("By ile to arasındaki farkı kontrol et.", "Yüzde değişimini toplam değerle karıştırma.", "Correlation'ı tek başına causation olarak sunma."),
                    listOf("Karşılaştırılan dönemleri belirt.", "Değişimin yönünü ve miktarını doğru ifade et.", "Sonucu verinin sınırlarına uygun yaz."))
            "müzakere ve uzlaşma" in t || "negotiation" in t ->
                LessonBookChapter("Müzakere etmek ve ortak çözüm bulmak",
                    "Müzakerede ihtiyaçları açıklamak, seçenek önermek ve karşı tarafın koşullarını anlamak önemlidir. Could we consider..., If we..., we could... gibi kalıplar seçenek sunar. Uzlaşmayı kabul ederken hangi koşullarda anlaşıldığını açıkça özetle.",
                    "Could we consider ...? · If we ..., we could ... · We can agree on ...",
                    listOf("Could we move the deadline to Friday?" to "Son tarihi cumaya alabilir miyiz?", "If we split the work, we could finish on time." to "İşi bölersek zamanında bitirebiliriz."),
                    listOf("Tek taraflı talebi uzlaşma gibi sunma.", "Koşulları ve son tarihi belirsiz bırakma.", "Anlaşılan noktaları teyit etmeden görüşmeyi bitirme."),
                    listOf("Tarafların ihtiyaçlarını belirle.", "En az bir uygulanabilir seçenek öner.", "Uzlaşılan şartları özetle."))
            "anlam incelikleri" in t || "nuance" in t ->
                LessonBookChapter("Yakın anlamlı kelimelerin tonunu ayırt etmek",
                    "Yakın anlamlı kelimeler aynı durumu anlatsa da resmiyet, kesinlik veya duygusal ton bakımından farklı olabilir. Örneğin suggest, recommend ve insist aynı güçte değildir. Kelime seçimini cümlenin amacı ve kanıt düzeyiyle uyumlu yap.",
                    "suggest = öneri sunmak · recommend = tavsiye etmek · insist = ısrar etmek",
                    listOf("The evidence suggests a possible link." to "Kanıt olası bir bağlantıya işaret ediyor.", "I recommend reviewing the latest version." to "Son sürümü gözden geçirmenizi tavsiye ederim."),
                    listOf("Yakın anlamlı kelimeleri her bağlamda birbirinin yerine kullanma.", "Kesinlik derecesini gereğinden fazla yükseltme.", "Resmiyet ve muhatabı dikkate al."),
                    listOf("Cümlenin amacını belirle.", "Kelimenin tonunu ve kesinliğini karşılaştır.", "Bağlama en uygun seçeneği seç."))
            "sunum ve ikna edici konuşma" in t || "presentations" in t ->
                LessonBookChapter("Sunum akışını kurmak ve soruları yanıtlamak",
                    "Etkili sunumda açılış, amaç, ana noktalar, kanıt ve sonuç anlaşılır sırayla gelir. Signposting ifadeleri dinleyiciye yapıyı gösterir. Soru geldiğinde önce soruyu doğrula, ardından doğrudan ve kanıta dayalı yanıt ver.",
                    "Today I'll focus on ... · First ... · Turning to ... · To conclude ...",
                    listOf("Today I'll focus on two practical options." to "Bugün iki uygulanabilir seçeneğe odaklanacağım.", "To conclude, the evidence supports a cautious approach." to "Sonuç olarak kanıt temkinli bir yaklaşımı destekliyor."),
                    listOf("Slayttaki her kelimeyi okumakla yetinme.", "Ana mesajı belirsiz bırakma.", "Bilmediğin soruya kesin olmayan bir yanıtı kesinmiş gibi verme."),
                    listOf("Amacı bir cümleyle tanıt.", "Ana noktaları geçiş ifadeleriyle sırala.", "Sonuç ve sonraki adımı belirt."))
            "birden fazla kaynaktan sentez" in t || "synthesis" in t ->
                LessonBookChapter("Kaynakları karşılaştırıp sentezlemek",
                    "Sentez, kaynakları art arda özetlemekten farklıdır: ortak noktaları, çelişkileri ve kanıt kalitesini karşılaştırarak birleşik bir sonuç oluşturur. Kaynakların ne konuda aynı fikirde olduğunu ve hangi noktada ayrıldığını açıkça göster.",
                    "Both sources indicate ... · In contrast, ... · Taken together, ...",
                    listOf("Both sources identify cost as a concern, but they propose different solutions." to "İki kaynak da maliyeti bir endişe olarak görüyor ancak farklı çözümler öneriyor.", "Taken together, the findings support further testing." to "Birlikte değerlendirildiğinde bulgular ek test yapılmasını destekliyor."),
                    listOf("Kaynakları yalnızca sırayla özetleme.", "Çelişen sonuçları gizleme.", "Kaynağın desteklemediği ortak bir sonuç üretme."),
                    listOf("Her kaynağın ana iddiasını çıkar.", "Ortaklık ve çelişkileri eşleştir.", "Kanıtların desteklediği dengeli bir sonuç yaz."))
            "ima, ton ve örtük anlam" in t || "implicit meaning" in t ->
                LessonBookChapter("İma ve konuşmacı tonunu yorumlamak",
                    "Örtük anlam doğrudan söylenmeyen ancak bağlam, kelime seçimi veya tonla ima edilen bilgidir. Bir cümle nazik bir rica, kuşku veya dolaylı eleştiri taşıyabilir. Çıkarım yaparken metindeki ipuçlarına dayan; tek bir ifadeden kesin niyet sonucu çıkarma.",
                    "Literal meaning + context + tone + supporting clues",
                    listOf("That's an interesting solution." to "Bu ilginç bir çözüm.", "Given the missed deadline, we may need a different plan." to "Kaçırılan son tarih nedeniyle farklı bir plana ihtiyacımız olabilir."),
                    listOf("İroni veya eleştiri olduğunu kanıtsız varsayma.", "Kelime anlamıyla konuşma amacının farklı olabileceğini değerlendir.", "Çıkarımın hangi ipuçlarına dayandığını açıkla."),
                    listOf("Cümlenin doğrudan anlamını yaz.", "Bağlamdaki ipuçlarını bul.", "En makul çıkarımı kanıtıyla belirt."))
            "ileri düzey tartışma" in t || "debate" in t ->
                LessonBookChapter("Karmaşık bir görüşü tartışmak",
                    "İleri tartışmada bir iddia ortaya koy, gerekçesini ve kanıtını sun, ardından güçlü bir karşı savı adil biçimde ele al. Concede, nevertheless ve on balance gibi bağlayıcılar görüşler arasındaki ilişkiyi netleştirir.",
                    "Claim → evidence → counterargument → response → balanced conclusion",
                    listOf("Although the proposal is costly, it may reduce long-term delays." to "Öneri maliyetli olsa da uzun vadeli gecikmeleri azaltabilir.", "On balance, the benefits appear to outweigh the risks." to "Genel değerlendirmede faydalar risklerden ağır basıyor gibi görünüyor."),
                    listOf("Karşı savı çarpıtarak aktarma.", "Kanıtsız mutlak ifadeler kullanma.", "Sonuçta önemli sınırlamaları saklama."),
                    listOf("Ana iddianı açıkla.", "Kanıt ve karşı sav sun.", "Karşı savı yanıtlayıp dengeli sonuç çıkar."))
            "metin düzenleme ve üslup" in t || "editing" in t ->
                LessonBookChapter("Metni açıklık, tutarlılık ve üslup için düzenlemek",
                    "Düzenleme yalnızca dilbilgisi hatalarını düzeltmek değildir. Gereksiz tekrarları azalt, belirsiz zamirleri açıklaştır, cümleler arasındaki mantıksal bağı kontrol et ve hedef okuyucuya uygun resmiyet düzeyi seç.",
                    "Meaning → structure → clarity → grammar → tone",
                    listOf("The team reviewed the draft and removed repeated information." to "Ekip taslağı gözden geçirip tekrarlanan bilgileri kaldırdı.", "Please clarify what 'it' refers to in the second paragraph." to "Lütfen ikinci paragraftaki 'it' ifadesinin neye gönderme yaptığını açıklayın."),
                    listOf("Düzenlerken yazarın asıl anlamını değiştirme.", "Her uzun cümleyi otomatik olarak bölme.", "Terimleri metin boyunca tutarlı kullan."),
                    listOf("Metnin amacını ve hedef kitlesini belirle.", "Tekrar ve belirsiz göndermeleri düzelt.", "Son kontrolde anlam, yapı ve tonu birlikte değerlendir."))
            "ileri düzey cümle yapıları" in t ->
                LessonBookChapter("Karmaşık cümleleri çözümlemek",
                    "İleri düzey cümlelerde yan cümlecikler, bağlaçlar ve vurgu bilgiyi nasıl düzenlediğini belirler. Önce ana cümlenin özne ve yüklemini bul, ardından relative, conditional veya concessive clause gibi ek yapıları ayır. Uzun cümle mutlaka daha iyi cümle demek değildir.",
                    "Main clause + subordinate clause · although/even though · which/that · if/unless",
                    listOf("Although the task was complex, the team completed it on time." to "Görev karmaşık olmasına rağmen ekip zamanında tamamladı.", "The report, which was published yesterday, includes the revised figures." to "Dün yayımlanan rapor güncellenmiş rakamları içeriyor."),
                    listOf("Ana cümleyi bulmadan tüm cümleyi çevirmeye çalışma.", "Bağlaçların kurduğu mantıksal ilişkiyi atlama.", "Yan cümleciğin hangi ismi veya fikri açıkladığını kontrol et."),
                    listOf("Ana cümlenin özne ve yüklemini belirle.", "Yan cümlecikleri parantezleyerek ayır.", "Bağlacın anlam ilişkisini açıkla."))
            "akademik paragraf ve argüman" in t ->
                LessonBookChapter("Akademik paragrafta iddia ve kanıt kurmak",
                    "Akademik paragraf genellikle ana iddiayı sunan bir topic sentence, bunu destekleyen gerekçe veya kanıt ve iddiayla bağlantıyı açıklayan yorum içerir. Kaynak kullanıyorsan kanıtı doğru aktar ve kendi yorumunu kaynak bulgusundan ayır.",
                    "Topic sentence → evidence/example → explanation → link to argument",
                    listOf("Regular feedback can improve performance because it helps learners identify specific gaps." to "Düzenli geri bildirim performansı artırabilir çünkü öğrenenlerin belirli eksikleri fark etmesine yardım eder.", "However, the effect depends on how the feedback is used." to "Bununla birlikte etki, geri bildirimin nasıl kullanıldığına bağlıdır."),
                    listOf("Paragrafı birbirinden kopuk örneklerle doldurma.", "Kaynağın söylediğinden daha güçlü bir iddia kurma.", "Kanıtın ana iddiayı nasıl desteklediğini açıklamayı unutma."),
                    listOf("Paragrafın tek ana iddiasını yaz.", "İlgili kanıt ekle.", "Kanıt ile iddia arasındaki bağı açıkla."))
            "olay anlatımı ve bağlaçlar" in t ->
                LessonBookChapter("Olayları kronolojik ve tutarlı anlatmak",
                    "B1 düzeyinde bir olayı anlatırken girişte nerede ve ne zaman olduğunu söyle; ardından olayları kronolojik sırayla aktar. Past Simple tamamlanmış ana olayları, Past Continuous ise o sırada sürmekte olan arka plan eylemini anlatabilir. When/while zaman ilişkisi kurar; before/after sıralamayı, however karşıtlığı, therefore sonucu, although beklenmeyen karşıt durumu belirtir. Bağlaçları yalnızca cümleyi uzatmak için değil, olaylar arasındaki gerçek ilişkiyi göstermek için seç.",
                    "Context → event sequence → result/reflection\nPast Continuous + when + Past Simple · while + ongoing action\nFirst/then/after that · however/therefore/although",
                    listOf("While I was checking the list, the supervisor called." to "Listeyi kontrol ederken sorumlu kişi aradı.", "After we had checked the details, we sent the report." to "Ayrıntıları kontrol ettikten sonra raporu gönderdik.", "The first attempt failed; however, the second one worked." to "İlk deneme başarısız oldu; ancak ikincisi işe yaradı.", "We missed the bus, so we arrived late." to "Otobüsü kaçırdık, bu yüzden geç kaldık."),
                    listOf("Then ve therefore aynı işlevde değildir: biri sıralama, diğeri sonuç bildirir.", "While çoğunlukla süren arka plan eylemini, when ise belirli olayı tanıtabilir; bağlamı kontrol et.", "Past Continuous biçiminde was/were + V-ing gerekir.", "Bağlaç eklemek için olaylar arasındaki mantığı bozma."),
                    listOf("Olayın zamanını ve yerini belirten bir giriş yaz.", "En az üç olayı sıraya koy.", "Arka plan eylemi ile ana olayı ayırt et.", "Sonuç veya karşıtlık için doğru bağlacı seç.", "Anlatımın zaman kiplerini ve noktalamasını kontrol et."))
            "sorun açıklama ve çözüm önerme" in t ->
                LessonBookChapter("Sorunu açıkça tanımlayıp çözüm önermek",
                    "Bir sorunu açıklarken gözlenen durumu, etkisini ve bilinen kanıtı birbirinden ayır. B1 düzeyinde önce problemi somut bir cümleyle tanımla, sonra kimleri veya hangi işi etkilediğini açıkla; varsa tarih, tekrar sıklığı veya örnek gibi doğrulanabilir kanıt ekle. Çözüm önerirken I recommend ..., One option would be ... ve We could ... gibi kalıplar kullan ve seçeneğin neden işe yarayabileceğini belirt. Kök nedeni bilmiyorsan kesinmiş gibi sunma; önce kontrol edilmesi gereken bilgiyi söyle.",
                    "Observed issue → impact → evidence → options → recommendation/next step",
                    listOf("The system responds slowly when several users sign in." to "Birden fazla kullanıcı giriş yaptığında sistem yavaş yanıt veriyor.", "I recommend checking the logs before changing the configuration." to "Yapılandırmayı değiştirmeden önce kayıtları kontrol etmeyi öneriyorum.", "One option would be to test the update with a small group first." to "Bir seçenek, güncellemeyi önce küçük bir grupla test etmek olurdu.", "We cannot confirm the cause yet, so the next step is to collect more data." to "Nedeni henüz doğrulayamıyoruz; bu yüzden sonraki adım daha fazla veri toplamak."),
                    listOf("Gözlemi doğrulanmamış nedenden ayır.", "Çözümü gerekçesiz sunma.", "Kesin kanıt yoksa may/might/could gibi ölçülü ifadeler kullan.", "Bir sonraki adımı ve sorumlusunu belirsiz bırakma."),
                    listOf("Sorunu somut ve ölçülebilir biçimde tanımla.", "Etkisini ve mevcut kanıtı açıkla.", "En az iki çözüm seçeneği üret.", "Önerini gerekçeyle destekle.", "Gerekliyse hangi bilginin hâlâ doğrulanmadığını belirt."))
            "iş yerinde iletişim" in t ->
                LessonBookChapter("İş yerinde net ve nazik iletişim",
                    "İş yerinde görev, sorumluluk ve süreleri açık ifade et. Talimatı anlamadığında kibarca tekrar veya açıklama iste; devir teslimde tamamlanan işi, açık kalan maddeleri ve sonraki adımı belirt.",
                    "Could you clarify ...? · My understanding is ... · The next step is ...",
                    listOf("Could you clarify which version we should use?" to "Hangi sürümü kullanmamız gerektiğini açıklayabilir misiniz?", "My understanding is that the report is due on Thursday." to "Anladığım kadarıyla rapor perşembe günü teslim edilecek."),
                    listOf("Belirsiz bir talimatı anladığını varsayma.", "Süre ve sorumluyu belirtmeden görev devretme.", "Nazikliği açıklığın yerine koyma."),
                    listOf("Görev ve sorumluyu netleştir.", "Anladığını kendi sözlerinle teyit et.", "Süreyi ve sonraki adımı belirt."))
            "dinlemede ayrıntı yakalama" in t ->
                LessonBookChapter("Dinlemede ayrıntı ve ana fikri ayırmak",
                    "B1 düzeyinde dinlerken önce konuşmanın amacını ve genel konusunu yakala; ikinci dinlemede sorunun istediği ayrıntıya odaklan: isim, tarih, saat, yer, gerekçe veya sonraki adım. Konuşmacı aynı bilgiyi farklı kelimelerle yeniden söyleyebilir; bu yüzden yalnızca tek bir kelimeyi duymaya değil anlam eşleşmesine bak. Her cümleyi yazmak yerine kısa anahtar notlar al ve cevabını konuşmadaki kanıtla doğrula.",
                    "1) First listen: purpose/topic · 2) Second listen: requested detail · 3) Check: evidence/paraphrase",
                    listOf("The briefing starts at 9:15 in room 204." to "Bilgilendirme 9.15'te 204 numaralı odada başlıyor.", "The team postponed the test because one document was missing." to "Ekip bir belge eksik olduğu için testi erteledi.", "Please send the revised file by Thursday afternoon, not Wednesday." to "Lütfen güncellenmiş dosyayı çarşamba değil perşembe öğleden sonra gönder.", "The supervisor asked Maya to confirm the delivery date." to "Sorumlu kişi Maya'dan teslim tarihini doğrulamasını istedi."),
                    listOf("Benzer sayıları veya saatleri duyunca soruyla eşleştir; yakın seçenekleri dikkatle ayırt et.", "Kendi tahminini konuşmada duyduğun bilgiyle karıştırma.", "İlk dinlemede tüm ayrıntıları yakalamaya çalışma.", "Not alırken olumsuzlukları ve düzeltmeleri (not, instead, rather than) kaçırma."),
                    listOf("Ana amacı tek cümleyle özetle.", "İstenen ayrıntıyı anahtar kelimelerle not et.", "Konuşmacının aynı anlamı başka sözlerle ifade edip etmediğini kontrol et.", "Yanıtını duyduğun kanıtla doğrula."))
            "uzun konuşmalarda ana fikir" in t ->
                LessonBookChapter("Uzun konuşmada amaç, tutum ve not çıkarma",
                    "Uzun konuşmada başlık değişimlerini, vurgulanan noktaları ve konuşmacının amacını izle. Notlarda her cümleyi yazmak yerine ana fikir, destekleyici kanıt, karşıtlık ve eylem maddelerini kısaltmalarla kaydet.",
                    "Topic → speaker's purpose → key points → contrast → action items",
                    listOf("The speaker supports the proposal but asks for a cost review before approval." to "Konuşmacı öneriyi destekliyor ancak onaydan önce maliyet incelemesi istiyor.", "The main concern is the effect on the delivery schedule." to "Temel endişe teslimat programına etkisi."),
                    listOf("Her kelimeyi yazmaya çalışıp ana fikri kaçırma.", "Konuşmacının görüşüyle aktardığı başkasının görüşünü ayır.", "Önemli karşıtlık ve koşulları atlama."),
                    listOf("Konuşmacının amacını belirle.", "Ana noktaları ve karşıtlıkları not et.", "Sonunda kısa ve dengeli bir özet çıkar."))
            "resmî rapor ve öneri yazımı" in t ->
                LessonBookChapter("Resmî rapor ve öneriyi yapılandırmak",
                    "Resmî rapor amaç ve kapsamı tanımlar, bulguları kanıta dayalı sunar ve sonuçları önerilerden ayırır. Başlıklar, kısa paragraflar ve tutarlı terimler okuyucunun bilgiyi taramasını kolaylaştırır. Önerinin dayanağını ve beklenen etkisini açıkça belirt.",
                    "Purpose/scope → findings → analysis → conclusion → recommendations",
                    listOf("The review identified three recurring delays." to "İnceleme tekrarlanan üç gecikme belirledi.", "We recommend a limited trial before full implementation." to "Tam uygulamadan önce sınırlı bir deneme öneriyoruz."),
                    listOf("Bulguyu öneriyle karıştırma.", "Kanıtlanmamış kesinlik kullanma.", "Önerinin kime ve hangi adım için olduğunu belirsiz bırakma."),
                    listOf("Raporun amacını belirt.", "Bulguları kanıtla destekle.", "Uygulanabilir ve ölçülebilir öneri yaz."))
            "profesyonel iletişim ve ikna" in t ->
                LessonBookChapter("Profesyonel iletişimde ikna ve güven",
                    "Profesyonel ikna, muhatabın ihtiyacını anlamayı, önerinin faydasını kanıtla açıklamayı ve sınırlamaları dürüstçe belirtmeyi gerektirir. İddiaları ölçülü kur; itiraz geldiğinde önce kaygıyı netleştir, sonra ilgili kanıt ve seçeneklerle yanıtla.",
                    "Audience need → proposal → evidence → limitation → next step",
                    listOf("This option could reduce delays, based on the last three reviews." to "Son üç incelemeye dayanarak bu seçenek gecikmeleri azaltabilir.", "I understand the concern; we can test the change on a small scale first." to "Endişeyi anlıyorum; önce değişikliği küçük ölçekte deneyebiliriz."),
                    listOf("Kanıt olmadan garanti verme.", "Karşı tarafın endişesini görmezden gelme.", "Sonraki adımı veya karar ölçütünü belirsiz bırakma."),
                    listOf("Muhatabın ihtiyacını belirle.", "Öneriyi kanıt ve sınırlamayla açıkla.", "Net bir sonraki adım öner."))
            "ileri dinleme ve not çıkarma" in t ->
                LessonBookChapter("İleri dinlemede çıkarım ve yoğun not alma",
                    "İleri düzey dinlemede yalnızca açıkça söylenen bilgiyi değil, konuşmacının tutumunu, koşulları ve makul çıkarımları da izlersin. Notları ana fikir, kanıt, çekince ve eylem maddeleri şeklinde grupla; emin olmadığın çıkarımı gerçek bilgi gibi kaydetme.",
                    "Claim · evidence · qualification · implication · action",
                    listOf("The speaker cautiously supports the plan, provided that costs remain within the agreed limit." to "Konuşmacı, maliyetler kararlaştırılan sınırda kaldığı sürece planı temkinli biçimde destekliyor.", "The deadline may need to change if the review identifies further risks." to "İnceleme ek riskler belirlerse son tarihin değişmesi gerekebilir."),
                    listOf("Could/may gibi ihtimal bildiren ifadeleri kesinlik gibi aktarma.", "Konuşmacının çekincelerini notlardan çıkarma.", "Çıkarım ile doğrudan söylenen bilgiyi ayır."),
                    listOf("Ana iddiayı yakala.", "Çekince ve koşulları not et.", "Çıkarımı destekleyen ipucunu belirt."))
            "email" in t || "e-mail" in t || "e-posta" in t || "writing" in t || "yazma" in t -> 
                LessonBookChapter("Amaçlı ve düzenli İngilizce yazmak",
                    "İyi bir e-posta veya kısa yazı okuyucunun amacını hızlıca anlamasını sağlar. Resmî e-postada uygun selamlama, ilk paragrafta amaç, gövdede açık ayrıntılar ve sonunda nazik kapanış bulunur. Akademik yazıda iddia, gerekçe ve örnek arasında açık bağ kurulur. Paragrafları tek ana fikir etrafında düzenle ve son kontrolde dilbilgisi kadar tonu da incele.",
                    "Greeting → purpose → details/reasons → request or conclusion → closing",
                    listOf("Dear Ms Taylor, I am writing to ask about the course." to "Sayın Bayan Taylor, kurs hakkında bilgi istemek için yazıyorum.", "Could you please send me the details?" to "Ayrıntıları gönderebilir misiniz?"),
                    listOf("Resmî yazıda aşırı samimi kısaltmalar kullanma.", "Her şeyi tek paragrafta toplama.", "İstek veya ana fikri belirsiz bırakma."),
                    listOf("Okuyucu ve yazma amacını belirle.", "Her paragrafa tek ana fikir ver.", "Ton, noktalama ve yazım kontrolü yap."))
            "aviation" in t || "havacılık" in t || "aircraft" in t || "pilot" in t || "maintenance" in t -> 
                LessonBookChapter("Havacılık İngilizcesinde açık ve güvenli iletişim",
                    "Havacılık bağlamında kelimeyi yalnızca Türkçe karşılığıyla öğrenmek yeterli değildir; teknik anlamı, hangi durumda kullanıldığı ve benzer terimlerden farkı da bilinmelidir. Talimatlar kısa, net ve bağlama uygun olmalıdır. Gerçek operasyonlarda yalnızca onaylı prosedürler, eğitim ve yetkili talimatlar izlenir; bu ders genel dil öğrenimi içindir ve operasyonel kontrol listesi yerine geçmez.",
                    "Term → plain-English definition → context → example → safety note",
                    listOf("The technician inspected the landing gear." to "Teknisyen iniş takımını kontrol etti.", "Report any visible damage." to "Görülebilen hasarı bildirin.", "The aircraft must remain grounded until authorized release." to "Yetkili serbest bırakma verilene kadar hava aracı yerde kalmalıdır."),
                    listOf("Benzer teknik terimleri eş anlamlı sanma.", "Kısaltmayı açılımı ve bağlamıyla öğren.", "Dil örneğini gerçek bakım veya uçuş talimatı olarak kullanma."),
                    listOf("Terimin tanımını kendi sözlerinle açıkla.", "Örnek cümlede terimin görevini belirle.", "Operasyonel kararlar için onaylı dokümana başvur."))
            "countable" in t || "uncountable" in t || "sayılabil" in t || "some/any" in t -> 
                LessonBookChapter("Sayılabilen ve sayılamayan isimler",
                    "Sayılabilen isimler tekil ve çoğul olabilir; a/an ve many ile kullanılabilir. Sayılamayan isimler genellikle doğrudan çoğul yapılmaz ve much ile kullanılabilir. Some olumlu cümlelerde sık görülür; any ise soru ve olumsuz cümlelerde sık kullanılır, ancak bağlama göre istisnalar vardır.",
                    "Countable: a book / many books\nUncountable: some water / much information",
                    listOf("I have some information." to "Biraz bilgim/bilgi var.", "Are there any apples?" to "Hiç elma var mı?", "We need a bottle of water." to "Bir şişe suya ihtiyacımız var."),
                    listOf("Informations değil information kullan.", "Sayılamayan ismin önüne doğrudan a/an getirme.", "Much/many seçiminde isim türünü kontrol et."),
                    listOf("İsmin sayılabilir olup olmadığını belirle.", "Tekil-çoğul biçimini kontrol et.", "Some/any/much/many seçimini cümle türüyle birlikte yap."))
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
