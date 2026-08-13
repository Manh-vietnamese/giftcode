package dev.chanhne.giftcode.core;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NumberFomat {
    private static final DecimalFormat FORMAT = new DecimalFormat("#.#");

    public static double parseNumber(String input) {
        input = input.trim().toLowerCase().replace(",", "");
        double multiplier = 1;

        if (input.endsWith("k")) {
            multiplier = 1_000D;
            input = input.substring(0, input.length() - 1);
        } else if (input.endsWith("m")) {
            multiplier = 1_000_000D;
            input = input.substring(0, input.length() - 1);
        } else if (input.endsWith("b")) {
            multiplier = 1_000_000_000D;
            input = input.substring(0, input.length() - 1);
        } else if (input.endsWith("t")) {
            multiplier = 1_000_000_000_000D;
            input = input.substring(0, input.length() - 1);
        }

        return Double.parseDouble(input) * multiplier;
    }

    public static String formatNumber(double value) {
        if (value >= 1_000_000_000_000D)
            return FORMAT.format(value / 1_000_000_000_000D) + "T";
        if (value >= 1_000_000_000D)
            return FORMAT.format(value / 1_000_000_000D) + "B";
        if (value >= 1_000_000D)
            return FORMAT.format(value / 1_000_000D) + "M";
        if (value >= 1_000D)
            return FORMAT.format(value / 1_000D) + "K";

        return String.valueOf((long) value);
    }

    public static long parseDuration(String input) {
        input = input.trim().toLowerCase();

        if (input.equals("never") || input.equals("0"))
            return 0;

        long total = 0;

        Matcher matcher = Pattern.compile("(\\d+)([smhd])").matcher(input);

        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));

            switch (matcher.group(2)) {
                case "s":
                    total += value * 1000L;
                    break;
                case "m":
                    total += value * 60_000L;
                    break;
                case "h":
                    total += value * 3_600_000L;
                    break;
                case "d":
                    total += value * 86_400_000L;
                    break;
            }
        }

        return total == 0 ? 0 : System.currentTimeMillis() + total;
    }

    public static String formatDuration(long expireAt) {
        long diff = expireAt - System.currentTimeMillis();

        if (expireAt <= 0) return "Never";
        if (diff <= 0) return "Expired";

        long days = diff / 86400000;
        diff %= 86400000;

        long hours = diff / 3600000;
        diff %= 3600000;

        long minutes = diff / 60000;
        diff %= 60000;

        long seconds = diff / 1000;
        StringBuilder sb = new StringBuilder();

        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }
}