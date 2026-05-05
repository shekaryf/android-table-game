# بررسي پروژه baranPack

> آخرين به‌روزرساني: 2026-05-02

## خلاصه سريع
- پروژه يک اپ اندرويد چندماژوله است با سه ماژول `app`، `BaranBook` و `framework`.
- `app` فقط نقش بسته‌بندي نهايي را دارد و اکتيويتي‌هاي اصلي را از دو ماژول ديگر در مانيفست معرفي مي‌کند.
- منطق اصلي محصول و سناريوهاي اختصاصي کتاب در `BaranBook` قرار دارد (34 فايل Java).
- زيرساخت UI، فرم‌ها، مديريت پايگاه داده و ابزارهاي عمومي در `framework` قرار دارد (87 فايل Java).
- پرداخت درون‌برنامه‌اي کافه‌بازار با SDK `Poolakey 2.1.0` پياده‌سازي شده است.
- قفل دسترسي از مقدار محلي جدول `settings` استفاده مي‌کند و هنگام اتصال به بازار، خريدهاي قبلي استعلام مي‌شوند.

## ساختار ماژول‌ها

### 1) ماژول `app`
- **فايل اصلي**: `app/build.gradle.kts`
- **نقش**: ماژول اپليکيشن نهايي (application module)
- **وابستگي‌ها**: به `:framework` و `:BaranBook` وابسته است
- **لانچر**: `ir.baran.bookPack.FirstSplashPage` در `app/src/main/AndroidManifest.xml`
- **شناسه اپ**: `ir.baran.Sticher`
- **SDK Versions**:
  - `compileSdk = 34`
  - `targetSdk = 34`
  - `minSdk = 24`
- **ويژگي‌ها**:
  - فايل‌هاي صوتي (mp3, wav, ogg, m4a) به صورت uncompressed در assets نگهداري مي‌شوند
  - Kotlin plugin غيرفعال است (پروژه فقط Java است)

### 2) ماژول `BaranBook`
- **فايل اصلي**: `BaranBook/build.gradle.kts`
- **نقش**: منطق اختصاصي اپ و business logic
- **SDK Versions**: `compileSdk = 35` (بالاتر از app module)
- **تعداد کلاس‌ها**: 34 فايل Java
- **کلاس‌هاي اصلي**:
  - `FirstSplashPage.java` - صفحه اسپلش و نقطه ورود
  - `FirstPage1.java` - منوي اصلي نوع 1
  - `FirstPage2.java` - منوي اصلي نوع 2
  - `FirstPage4.java` - منوي اصلي نوع 4
  - `FirstPage5.java` - منوي اصلي نوع 5
  - `FirstPage6.java` - منوي اصلي نوع 6 (جديد)
  - `BazaarPay.java` - مديريت پرداخت کافه‌بازار
  - `BgMediaPlayer.java` - پخش صوت در پس‌زمينه
  - `ContentSearch.java` - جستجوي محتوا
  - `FavList.java` - ليست علاقه‌مندي‌ها
  - `SticherPage.java` - صفحه دوخت تصاوير
  - `ProductGrid.java` - نمايش محصولات به صورت گريد
  - `StoryListPage.java` - ليست داستان‌ها
  - `SContent.java` / `SImageContent.java` - نمايش محتواي داستان
- **وابستگي‌هاي مهم**:
  - `implementation(libs.poolakey)` - SDK پرداخت کافه‌بازار نسخه 2.1.0
- **منابع**: 
  - کليد عمومي بازار و SKU در `BaranBook/src/main/res/values/strings.xml`

### 3) ماژول `framework`
- **فايل اصلي**: `framework/build.gradle.kts`
- **نقش**: زيرساخت مشترک و کتابخانه داخلي
- **تعداد کلاس‌ها**: 87 فايل Java
- **مسئوليت‌ها**:
  - فرم‌ها و صفحات پايه
  - ابزارهاي UI و کامپوننت‌هاي سفارشي
  - لايه پايگاه داده SQLite
  - مديريت تنظيمات، علاقه‌مندي‌ها، محتوا و شبکه
- **کلاس‌هاي کليدي**:
  - `DataBaseHelper.java` - مديريت ديتابيس SQLite
  - `DBM.java` - Database Manager
  - `SettingsManager.java` - مديريت تنظيمات محلي
  - `GridManager.java` - مديريت داده‌هاي گريد منو
  - `ContentManager.java` - مديريت محتواي کتاب
  - `ConfigurationUtils.java` - ابزارهاي پيکربندي

## جريان اجراي برنامه

### 1. شروع برنامه
- **نقطه ورود**: `FirstSplashPage` (LAUNCHER activity)
- **عمليات اوليه**:
  - خواندن نام اپ از سيستم
  - کپي فايل ديتابيس `dt` از assets به حافظه داخلي
  - خواندن `MenuType` از منابع
  - انتخاب صفحه اصلي بر اساس `MenuType`:
    - Type 1 → `FirstPage1`
    - Type 2 → `FirstPage2`
    - Type 4 → `FirstPage4`
    - Type 5 → `FirstPage5`
    - Type 6 → `FirstPage6` (جديد)

### 2. صفحات اصلي
- در هر صفحه اصلي، `BazaarPay.getInstance(this).init()` فراخواني مي‌شود
- منوي اصلي از جدول `grid` در ديتابيس خوانده مي‌شود
- هر آيتم منو مي‌تواند به انواع مختلف محتوا اشاره کند

### 3. کنترل دسترسي
- هنگام انتخاب محتواي قفل‌شده:
  - اگر `ACTIVED = 1` → نمايش محتوا
  - اگر `ACTIVED ≠ 1` → نمايش ديالوگ خريد

## ديتابيس و محتوا

### مسير فايل
- **Asset**: `app/src/main/assets/dt`
- **اندازه**: ~6.3 MB
- **نوع**: SQLite database

### جداول اصلي
1. **`Products`** - اطلاعات محصولات قابل خريد
2. **`data`** - محتواي اصلي کتاب
   - `id`, `title`, `content`, `categoryId`
3. **`favorites`** - علاقه‌مندي‌هاي کاربر
4. **`grid`** - آيتم‌هاي منوي اصلي
   - شامل `contentType` براي تعيين نوع محتوا
5. **`settings`** - تنظيمات محلي
   - `id`, `value`
   - کليد `ACTIVED` براي وضعيت فعال‌سازي

### فونت‌هاي فارسي
فايل‌هاي فونت در `app/src/main/assets/`:
- `BNazanin.ttf`
- `BZAR.TTF`
- `Vazir-Bold.ttf`
- `Vazir-Light.ttf`
- `Weblogma_Yekan.ttf`
- `YEKAN.TTF`

## سيستم پرداخت کافه‌بازار

### معماري
- **کلاس اصلي**: `BazaarPay.java` (Singleton pattern)
- **SDK**: Poolakey 2.1.0
- **نوع محصول**: In-App Purchase (خريد يکباره)

### جريان کار

#### 1. مقداردهي اوليه (`init()`)
```
1. خواندن SKU از strings.xml
2. بررسي وضعيت محلي (ACTIVED)
3. ساخت PaymentConfiguration با کليد عمومي
4. اتصال به بازار با Payment.connect()
5. در صورت موفقيت:
   - فراخواني getPurchasedProducts()
   - بررسي خريدهاي قبلي
   - به‌روزرساني وضعيت محلي
```

#### 2. نمايش ديالوگ خريد (`aboutClick()`)
```
1. بررسي وضعيت فعال‌سازي
2. اگر فعال نيست:
   - نمايش BottomSheetDialog
   - نمايش اطلاعات محصول
   - دکمه خريد
```

#### 3. شروع خريد (`launchPurchaseFlow()`)
```
1. ساخت PurchaseRequest با SKU و payload
2. فراخواني payment.purchaseProduct()
3. مديريت نتيجه:
   - موفق → ذخيره ACTIVED=1
   - ناموفق → نمايش پيام خطا
   - لغو → بستن ديالوگ
```

#### 4. بازيابي خريد (`queryPurchases()`)
```
1. فراخواني getPurchasedProducts()
2. بررسي ليست خريدها
3. اگر SKU_PREMIUM موجود باشد:
   - ذخيره ACTIVED=1
   - به‌روزرساني UI
```

### پيام‌هاي خطا
آرايه `_ARR_MESSAGES` شامل پيام‌هاي فارسي براي خطاهاي مختلف:
- Index 2: "کاربر عملیات را متوقف کرده است"
- Index 3: "API برای درخواست ارسال شده پشتیبانی نمی‌شود"
- Index 4: "این محصول برای فروش موجود نیست"
- Index 5: "پارامترهای ارسالی به API معتبر نیستند"
- Index 6: "خطا در هنگام عملیات پرداخت"
- Index 7: "این محصول قبلا برای این حساب خریداری شده است"
- Index 8: "این خرید متعلق به کاربر فعلی نیست"

### کلاس‌هاي کمکي قديمي
فايل‌هاي زير از SDK قديمي IAB هستند و ديگر استفاده نمي‌شوند:
- `com/util/IabResult.java`
- `com/util/Inventory.java`
- `com/util/Purchase.java`
- `com/util/Security.java`

**توصيه**: اين فايل‌ها را مي‌توان حذف کرد چون SDK فعلي Poolakey جايگزين آنها شده است.

## تاريخچه Git

آخرين commit‌ها:
```
baa0bcc - good favorite page
894bc73 - search page at first and content
8ace0e1 - search page at first
cafa04b - search page at first
10ea6de - insert favorite
8a8b5a3 - search page at first
```

**نکته**: commit `db7bc6a` نقطه اضافه شدن سيستم پرداخت بازار است.

## پيکربندي Gradle

### Root `build.gradle.kts`
- Android Gradle Plugin: 8.3.2
- Kotlin: 1.9.20 (غيرفعال در پروژه)
- Java Version: 1.8

### Version Catalog (`gradle/libs.versions.toml`)
وابستگي‌هاي کليدي:
- `poolakey = "2.1.0"`
- `appcompat = "1.6.1"`
- `material = "1.10.0"`
- `constraintlayout = "2.1.4"`
- `picasso = "2.8"`
- `volley = "1.2.1"`

### Repositories
- Google Maven
- Maven Central
- JitPack
- SciJava Maven

## نکات فني

### 1. تفاوت SDK Versions
- `app` module: compileSdk = 34
- `BaranBook` و `framework`: compileSdk = 35

**توصيه**: يکسان‌سازي نسخه SDK در همه ماژول‌ها براي جلوگيري از مشکلات احتمالي.

### 2. Kotlin غيرفعال
پروژه به صورت کامل با Java نوشته شده و plugin Kotlin در `app/build.gradle.kts` کامنت شده است.

### 3. مديريت حافظه صوت
فايل‌هاي صوتي به صورت uncompressed نگهداري مي‌شوند تا `MediaPlayer.openFd()` بتواند مستقيم از assets پخش کند.

### 4. Orientation قفل
همه Activity‌ها با `android:screenOrientation="portrait"` قفل شده‌اند.

## مشکلات و بهبودهاي پيشنهادي

### 1. تميزسازي کد
- [ ] حذف کلاس‌هاي قديمي IAB در `com/util/`
- [ ] حذف کامنت‌هاي غيرضروري در Gradle files
- [ ] يکسان‌سازي compileSdk در همه ماژول‌ها

### 2. امنيت
- [ ] بررسي امنيت کليد عمومي بازار (نبايد در کد هاردکد باشد)
- [ ] اضافه کردن ProGuard rules براي محافظت از کد در release build

### 3. مديريت خطا
- [ ] بهبود مديريت خطاهاي شبکه در BazaarPay
- [ ] اضافه کردن retry mechanism براي اتصال به بازار
- [ ] لاگ‌گذاري بهتر براي debug

### 4. تست
- [ ] تست سناريوي نصب تازه و خريد اوليه
- [ ] تست بازيابي خريد قبلي
- [ ] تست رفتار در حالت آفلاين
- [ ] تست لغو خريد توسط کاربر

### 5. UI/UX
- [ ] بررسي responsive بودن ديالوگ خريد در سايزهاي مختلف
- [ ] اضافه کردن loading indicator هنگام اتصال به بازار
- [ ] بهبود پيام‌هاي خطا براي کاربر

## سناريوهاي تست پيشنهادي

### سناريو 1: خريد موفق
1. نصب تازه اپ
2. باز کردن محتواي قفل‌شده
3. کليک روي دکمه خريد
4. تکميل خريد در بازار
5. بررسي فعال شدن محتوا

### سناريو 2: بازيابي خريد
1. خريد محصول
2. حذف و نصب مجدد اپ
3. باز کردن اپ
4. بررسي فعال بودن خودکار محتوا

### سناريو 3: لغو خريد
1. شروع فرآيند خريد
2. لغو در صفحه پرداخت بازار
3. بررسي بازگشت به اپ بدون خطا

### سناريو 4: خريد تکراري
1. خريد موفق محصول
2. تلاش براي خريد مجدد
3. بررسي نمايش پيام "قبلا خريداري شده"

## وضعيت فعلي پروژه

### آماده براي توليد
- ✅ سيستم پرداخت پياده‌سازي شده
- ✅ UI خريد کامل است
- ✅ بازيابي خريد قبلي فعال است
- ✅ مديريت خطاهاي اصلي وجود دارد

### نياز به بررسي
- ⚠️ تست روي دستگاه واقعي با بازار نصب‌شده
- ⚠️ بررسي امنيت کليد عمومي
- ⚠️ تست سناريوهاي مختلف خريد

### اختياري
- 💡 حذف کدهاي قديمي IAB
- 💡 بهبود مديريت خطا
- 💡 اضافه کردن analytics

## نتيجه‌گيري

پروژه baranPack يک اپ کتاب اندرويدي با معماري چندماژوله است که ساختار قابل قبولي دارد. سيستم پرداخت کافه‌بازار با SDK Poolakey پياده‌سازي شده و در کنار تغييرات جديد (جستجو و علاقه‌مندي) نياز به يک دور تست رگرسيون کامل دارد. همچنين به‌روزرساني مقادير مستندات داخلي (مثل شمار کلاس‌ها و تاريخچه commit) انجام شد.
