# Abdolhashem Dormitory Management System (Java Swing + SQLite)

## راهنمای اجرا

1. JDK/JRE 11 یا بالاتر نصب باشد.
2. **Windows:** روی `run.bat` دوبار کلیک کنید. **Linux/macOS:** `./run.sh`
3. ورود: کاربران قبلی شما حفظ شده‌اند (مثلاً `admin` با رمز قبلی). در دیتابیس جدید، حساب پیش‌فرض `admin` / `admin123` ساخته می‌شود؛ بعد از اولین ورود رمز را از **Setting > Security** عوض کنید.
4. برای کامپایل دوباره: `build.bat` یا `./build.sh` (نیاز به JDK). در NetBeans هم پروژه مثل قبل باز می‌شود.

پوشه‌ها: `src` کد، `lib` کتابخانه‌ها (از جمله sqlite-jdbc)، `database` دیتابیس، `dist` فایل jar، `test` تست‌ها.

## دیتابیس SQLite

- فایل: `database/abdolhashem.db` (مسیر نسبی؛ کل پوشه قابل انتقال است. باید برنامه از داخل پوشهٔ پروژه اجرا شود، `run.bat/run.sh` این کار را می‌کنند).
- اتصال و تراکنش‌ها فقط در `nematullah/database/Database.java` هستند. هر عملیات چندمرحله‌ای با `Database.inTransaction(...)` یا همه با هم ذخیره می‌شود یا هیچ‌کدام. Foreign Keys در هر اتصال فعال است و همهٔ Queryها `PreparedStatement` هستند.
- در اولین اجرا جدول‌ها ساخته و ۱۵ اتاق ثبت می‌شود (`INSERT OR IGNORE`، یعنی اجرای مجدد اتاق تکراری نمی‌سازد).
- اگر دیتابیس قدیمی (نسخهٔ ۱) پیدا شود، خودکار مهاجرت می‌شود؛ پشتیبان در `database/backup_before_migration.db` و گزارش ردشده‌ها در `database/migration_report.txt` است.
- پشتیبان‌گیری: **Setting > Database > Backup Database** (پوشهٔ `database/backups`).
