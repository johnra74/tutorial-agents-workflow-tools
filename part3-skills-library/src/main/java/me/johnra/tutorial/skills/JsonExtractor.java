package me.johnra.tutorial.skills;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class JsonExtractor {

    private static final Pattern FENCED = Pattern.compile("(?s)```[a-z]*\\n?(.*?)```");

    private JsonExtractor() {}

    static String extract(String text) {
        String s = text.strip();
        s = s.replaceAll("(?s)<think>.*?</think>", "").strip();
        Matcher fenced = FENCED.matcher(s);
        if (fenced.find()) {
            return fenced.group(1).strip();
        }
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start != -1 && end > start) {
            return s.substring(start, end + 1);
        }
        return s;
    }
}
