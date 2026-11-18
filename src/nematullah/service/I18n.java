package nematullah.service;

import nematullah.database.Database;

/**
 * Very small two-language helper (English / Persian). The chosen language is saved in the
 * database (app_setting "language"), so it is remembered the next time the program starts.
 * To translate another screen: add its words to WORDS, then call I18n.t("key").
 */
public final class I18n {

    public static final String ENGLISH = "en";
    public static final String PERSIAN = "fa";

    /** key, English, Persian */
    private static final String[][] WORDS = {
        {"edit_employee", "Edit Employee", "ویرایش کارمند"},
        {"first_name", "First Name", "نام"},
        {"last_name", "Last Name", "تخلص"},
        {"father_name", "Father Name", "نام پدر"},
        {"phone", "Phone Number", "شماره تماس"},
        {"job", "Job", "وظیفه"},
        {"residence", "Permanent Residence", "سکونت اصلی"},
        {"hire_date", "Hire Date (yyyy-MM-dd)", "تاریخ استخدام (yyyy-MM-dd)"},
        {"language", "Language", "زبان"},
        {"save_changes", "Save Changes", "ذخیره تغییرات"},
        {"cancel", "Cancel", "لغو"},
        {"saved", "Changes saved.", "تغییرات ذخیره شد."},
        {"cannot_save", "Cannot save", "ذخیره ممکن نیست"},
        {"job_Manager", "Manager", "مدیر"},
        {"job_Accountant", "Accountant", "حسابدار"},
        {"job_Cook", "Cook", "آشپز"},
        {"job_Guard", "Guard", "نگهبان"},
        {"job_Cleaner", "Cleaner", "نظافت‌چی"},
        {"job_Other", "Other", "دیگر"},
    };

    private static String current;

    private I18n() {
    }

    public static String getLanguage() {
        if (current == null) {
            current = PERSIAN.equals(Database.getSetting("language", ENGLISH)) ? PERSIAN : ENGLISH;
        }
        return current;
    }

    public static boolean isPersian() {
        return PERSIAN.equals(getLanguage());
    }

    public static void setLanguage(String language) {
        current = PERSIAN.equals(language) ? PERSIAN : ENGLISH;
        Database.setSetting("language", current);
    }

    /** Text for the key in the current language (the key itself if it is unknown). */
    public static String t(String key) {
        for (String[] word : WORDS) {
            if (word[0].equals(key)) {
                return isPersian() ? word[2] : word[1];
            }
        }
        return key;
    }

    /** Name of a job in the current language; unknown jobs are shown as they are. */
    public static String job(String englishJob) {
        String key = "job_" + englishJob;
        String text = t(key);
        return text.equals(key) ? englishJob : text;
    }
}
