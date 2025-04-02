package kg16;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

public class Utils {
    private Utils() {
    }

    private static final Pattern MAC_PATTERN = Pattern.compile("^[0-9A-Fa-f]{2}(-[0-9A-Fa-f]{2}){5}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    public static boolean isValidMacAddress(String input) {
        return input != null && MAC_PATTERN.matcher(input).matches();
    }

    public static boolean isValidEmail(String input) {
        return input != null && EMAIL_PATTERN.matcher(input).matches();
    }

    public static boolean isValidPhone(String input) {
        return input != null && PHONE_PATTERN.matcher(input).matches();
    }

    public static String timeAgo(Timestamp timestamp) {
        Instant now = Instant.now();
        Instant past = timestamp.toInstant();
        Duration duration = Duration.between(past, now);

        long seconds = duration.getSeconds();
        if (seconds < 60)
            return seconds + " seconds ago";
        long minutes = seconds / 60;
        if (minutes < 60)
            return minutes + " minutes ago";
        long hours = minutes / 60;
        if (hours < 24)
            return hours + " hours ago";
        long days = hours / 24;
        if (days < 30)
            return days + " days ago";
        long months = days / 30;
        if (months < 12)
            return months + " months ago";
        long years = months / 12;
        return years + " years ago";
    }

    public static boolean isEmpty(String... strings) {
        for (String str : strings) {
            if (str == null || str.isEmpty())
                return true;
        }
        return false;
    }

    public static boolean isOneOf(String s, String... strings) {
        for (String str : strings) {
            if (s.equals(str))
                return true;
        }
        return false;
    }
}
