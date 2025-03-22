package kg16;

import java.util.regex.Pattern;

public class Utils {
    private Utils() {}

    private static final Pattern MAC_PATTERN = Pattern.compile("^[0-9A-Fa-f]{2}(-[0-9A-Fa-f]{2}){5}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    public static boolean isValidMacAddress(String mac) {
        return mac != null && MAC_PATTERN.matcher(mac).matches();
    }

    public static boolean isValidEmail(String mac) {
        return mac != null && EMAIL_PATTERN.matcher(mac).matches();
    }

    public static boolean isValidPhone(String mac) {
        return mac != null && PHONE_PATTERN.matcher(mac).matches();
    }

    public static boolean isEmpty(String... strings) {
        for (String str : strings) {
            if (str == null || str.isEmpty()) return true;
        }
        return false;
    }

    public static boolean isOneOf(String s, String... strings) {
        for (String str : strings) {
            if (s.equals(str)) return true;
        }
        return false;
    }
}
