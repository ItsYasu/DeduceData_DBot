package bot.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Utils {
    public static boolean isValidDateFormat(String dateStr) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        formatter.setLenient(false); // Esto hace que el formato sea estricto

        try {
            formatter.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
