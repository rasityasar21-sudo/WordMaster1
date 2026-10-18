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
                    listOf("Terimin tanımını kendi sözlerinle açıkla.", "Örnek cümlede terimin görevini belirle.", "Operasyonel kararlar için onaylı dokümana başvur.")),
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
