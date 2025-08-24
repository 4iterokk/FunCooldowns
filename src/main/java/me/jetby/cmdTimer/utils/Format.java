package me.jetby.cmdTimer.utils;

public class Format {
    public static String formatSeconds(int seconds) {
        String formatted;
        if (seconds % 10 == 1 && seconds % 100 != 11) {
            formatted = seconds + " секунду";
        } else if (seconds % 10 >= 2 && seconds % 10 <= 4 &&
                (seconds % 100 < 10 || seconds % 100 >= 20)) {
            formatted = seconds + " секунды";
        } else {
            formatted = seconds + " секунд";
        }
        return formatted;
    }
}