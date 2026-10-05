# Energy Service

Bir tesisin farklı bölgelerindeki akıllı enerji sayaçlarından alınan saatlik okuma
verilerini işleyen ve raporlayan REST servisi.

## Doğrulama Özeti

| Kontrol | Sonuç |
|---|---|
| Derleme (`mvn clean verify`) | ✅ BUILD SUCCESS |
| Testler | ✅ 30/30 geçti (unit + `@WebMvcTest` + Testcontainers entegrasyon) |
| JaCoCo satır kapsamı | ✅ ≥%70 (eşik karşılandı) |
| SonarQube | ✅ 0 Blocker, 0 High/Critical (bkz. [Statik Kod Analizi](#statik-kod-analizi-sonarqube)) |
| `docker compose up -d --build` | ✅ Tek komutla ayağa kalkıyor |

## Teknoloji Yığını

| Katman         | Teknoloji                              |
|----------------|-----------------------------------------|
| Dil / Framework| Java 17, Spring Boot 3.3.5              |
| Veritabanı     | PostgreSQL 16                           |
| Erişim         | Spring Data JPA / Hibernate (native SQL yok) |
| Dokümantasyon  | Springdoc OpenAPI (Swagger UI)          |
| Test           | JUnit 5, Mockito, Testcontainers        |
| Kod kapsamı    | JaCoCo (min. %70 satır kapsamı)         |

## Mimari

Katmanlı mimari: `Controller -> Service -> Repository`.

```
controller/   REST endpoint'leri, giriş doğrulama, HTTP durum kodları
service/      İş kuralları, transaction sınırları
repository/   Spring Data JPA repository'leri (derived query / JPQL)
entity/       JPA entity'leri (API dışına açılmaz)
dto/          Request / response DTO'ları (record)
mapper/       Entity <-> DTO dönüşümleri
exception/    Özel exception sınıfları ve @RestControllerAdvice
```

## Veritabanı Modeli

```
BuildingZone (1) ----< (N) Meter (1) ----< (N) ConsumptionReading >---- (N) (1) Tariff
```

- Bir `BuildingZone` birden fazla `Meter` içerir (cascade: zone silinince sayaçları da silinir).
- Bir `Meter`'ın birden fazla `ConsumptionReading`'i vardır.
- Her `ConsumptionReading`, o anda geçerli bir `Tariff`'e bağlanır.

ER diyagramı: [`database-model.drawio`](./database-model.drawio)

## İş Kuralları

1. **Sayaç doğrulama:** `ACTIVE` durumunda olmayan bir sayaçtan gelen okuma reddedilir
   ve `InactiveMeterException` (HTTP 422) fırlatılır.
2. **Tüketim anomalisi:** Yeni okuma, aynı sayacın bir önceki okumasının en az 3 katı
   (`>= previous * 3`) ise kayıt reddedilir ve `AnomalousReadingException` (HTTP 422)
   fırlatılır. Çarpan `energy.anomaly-multiplier` property'si ile ayarlanabilir (varsayılan: 3).
3. **Tarife eşleme:** Okuma zamanına (tarih + saat) uyan aktif bir `Tariff` bulunamazsa
   `TariffNotFoundException` (HTTP 422) fırlatılır. Bulunursa
   `toplam tutar = tüketim (kWh) x birim fiyat` hesaplanıp okuma ile birlikte kaydedilir.

## Tasarım Kararları

- **422 Unprocessable Entity:** `InactiveMeterException`, `AnomalousReadingException` ve
  `TariffNotFoundException` için 400 (bozuk istek) ya da 404 (kaynak yok) yerine 422
  seçildi; istek biçimce geçerli ama bir iş kuralı yüzünden işlenemiyor.
- **Anomali eşiğinde `>=`:** "En az 3 kat" ifadesi eşitliği de kapsayacak şekilde
  yorumlandı (`yeni değer >= önceki değer x 3`). Önceki okuma `0` ise (ilk okuma ya
  da sıfır tüketim) kontrol atlanır; aksi halde sıfırın her katı yine sıfır olacağından
  yanlış pozitif üretirdi.
- **BigDecimal, çarpan property:** Para ve tüketim alanlarında `double` yerine
  `BigDecimal` kullanıldı. Anomali çarpanı (`3`) `application.yml`'de
  `energy.anomaly-multiplier` olarak dışarıdan verilir, koda sabit yazılmadı.
- **Cascade yönü:** `BuildingZone -> Meter` ilişkisi `cascade = ALL` (bölge silinince
  sayaçları da silinir), `Meter -> ConsumptionReading` ve `Tariff -> ConsumptionReading`
  ilişkilerinde cascade yok; okuma kayıtları sayaç/tarife silinirken korunur.

## Çalıştırma

### Seçenek A — Docker Compose ile hepsi bir arada

```bash
docker compose up -d --build
```

PostgreSQL ve uygulama birlikte ayağa kalkar. Uygulama `8080` portunda açılır.

### Seçenek B — Veritabanı Docker'da, uygulama yerelde

```bash
docker compose up -d postgres
mvn spring-boot:run
```

Uygulama varsayılan olarak `8080` portunda açılır.

### Örnek verilerle başlatma (opsiyonel)

Swagger'da elle veri girmeden hemen deneme yapmak için `dev` profiliyle başlat;
veritabanı boşsa 2 bölge, 4 sayaç (biri `INACTIVE`, biri `FAULTY`), 3 tarife ve
birkaç örnek okuma otomatik oluşturulur (bkz. `config/DataSeeder.java`):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### API dokümantasyonu

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Testler ve Kod Kapsamı

```bash
mvn verify
```

`verify` adımı testleri çalıştırır, JaCoCo raporunu üretir
(`target/site/jacoco/index.html`) ve satır kapsamı %70'in altındaysa build'i
başarısız yapar. Integration testler gerçek bir PostgreSQL örneğini
Testcontainers ile ayağa kaldırır; bu nedenle **Docker'ın çalışır durumda
olması gerekir**.

`EnergyApplication` (main metodu) ve `DataSeeder` (sadece `dev` profilinde
çalışan, test edilecek iş mantığı içermeyen yardımcı sınıf) JaCoCo kapsam
hesabından hariç tutulmuştur; bu ikisi dışındaki tüm kod ölçülür.

**Doğrulama durumu:** `mvn clean verify` ile 30 test (unit + `@WebMvcTest`
controller testleri + Testcontainers ile çalışan 2 entegrasyon testi)
başarıyla geçti, JaCoCo satır kapsama kontrolü %70 eşiğini karşıladı:

```
Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
All coverage checks have been met.
BUILD SUCCESS
```

## Statik Kod Analizi (SonarQube)

Proje, yerel bir SonarQube Community sunucusuna karşı tarandı:

```bash
mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:3.10.0.2594:sonar \
  -Dsonar.projectKey=energy-service \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<SONARQUBE_TOKEN>
```

(SonarQube sunucusu için: `docker run -d --name sonarqube -p 9000:9000 sonarqube:community`)

**Sonuç:** 0 Blocker, 0 High (eski adıyla Critical) bulgu. 2 Medium (eski adıyla
Major) seviyeli bulgu var — ikisi de `ConsumptionReadingIntegrationTest.java`
içinde `java:S5778` kuralına ait stil önerisi (assertion lambda'sında tek metot
çağrısı önerisi), fonksiyonel bir hata değil. Security: 0 açık issue,
Duplications: %0.0.

## Örnek API Akışı

1. `POST /api/v1/zones` — bir bölge oluştur
2. `POST /api/v1/meters` — bölgeye bağlı bir sayaç oluştur (`status: ACTIVE`)
3. `POST /api/v1/tariffs` — okuma saatini kapsayan bir tarife oluştur
4. `POST /api/v1/readings` — okuma gönder, maliyet otomatik hesaplanır
5. `GET /api/v1/readings/meters/{meterId}/report` — sayaç bazlı tüketim/maliyet raporu
6. `GET /api/v1/readings/zones/{zoneId}/report` — bölge bazlı tüketim/maliyet raporu

Hata durumunda tüm endpoint'ler aşağıdaki standart formatta yanıt döner:

```json
{
  "timestamp": "2026-09-28T11:30:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "code": "ANOMALOUS_READING",
  "message": "Reading 300 kWh for meter SM-0001 is at least 3 times the previous reading of 90 kWh",
  "path": "/api/v1/readings",
  "fieldErrors": []
}
```

## Notlar

`docker-compose.yml` içindeki veritabanı kullanıcı adı/şifresi (`energy`/`energy`)
yalnızca yerel geliştirme amaçlıdır; prod ortamında ortam değişkenleri üzerinden
(`DB_URL`, `DB_USER`, `DB_PASSWORD`) değiştirilmelidir.