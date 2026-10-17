# WordMaster — Android projesi

Bu depo, WordMaster için Android Studio/Gradle projesidir. APK dosyası bu ZIP'in içinde hazır değildir.

## İçerik
- Çevrimdışı JSON sözlüğü (şu an 107 kelime)
- İngilizce/Türkçe arama ve A1–C1 seviye filtresi
- Kelime telaffuzunu cihazın metin-ses motoruyla dinleme
- Favoriler ve öğrenilen kelimeler
- Quiz, yanlışları tekrar, özel seviye ve zor sınav modları
- XP, günlük seri ve coin sistemi
- Cihazda yerel ilerleme kaydı

## APK oluşturma
GitHub Actions iş akışı `.github/workflows/build-apk.yml` içinde bulunur. Projeyi GitHub'da yeni bir depoya yükledikten sonra **Actions → Build WordMaster APK → Run workflow** seçilebilir. İşlem başarılı olursa `WordMaster-debug-apk` adlı artifact içinden `app-debug.apk` indirilebilir. Alternatif olarak projeyi Android Studio'da açıp `Build > Build APK(s)` kullanılabilir.

## Dürüstçe belirtilmesi gereken sınırlar
- Bu ortamda Android SDK/Gradle derlemesi çalıştırılmadığı için proje henüz derlenmiş veya test edilmiş değildir.
- Sözlük şu an 107 kelimedir; kapsamlı bir sözlük değildir.
- Premium ekranı yalnızca bilgilendirme yer tutucusudur. 199,99 TL/ay tutarında gerçek abonelik tahsilatı yapılmaz; bunun için Google Play Console ürünü, Play Billing entegrasyonu ve yayınlama yapılandırması gerekir.
- İlerleme cihazda yerel saklanır; hesap, bulut senkronizasyonu ve cihazlar arası yedekleme yoktur.
- Üretilen debug APK test amaçlıdır; Play Store yayını için imzalama ve yayın süreci ayrıca gerekir.
