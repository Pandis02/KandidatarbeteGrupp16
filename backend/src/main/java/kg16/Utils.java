package kg16;

public class Utils {
    private Utils() {}

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

    public static String sqlValues(Object... values) {
        StringBuilder total = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            Object val = values[i];
            if (val instanceof String s) {
                total.append("'");
                total.append(s);
                total.append("'");
            } else if (val instanceof Integer v) {
                total.append(v);
            } else total.append(val);
            
            if (i != values.length-1) {
                total.append(',');
            }
        }
        return total.toString();
    }
}
