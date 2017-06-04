package org.cafebabepy.util;

/**
 * Created by yotchang4s on 2017/06/02.
 */
public final class StringUtils {

    private StringUtils() {
    }

    public static String substringCodePoint(String target, int startIndex, int endIndex) {
        char[] charArray = target.toCharArray();

        int offsetStart = target.offsetByCodePoints(0, startIndex);
        int offsetEnd = target.offsetByCodePoints(0, endIndex);
        int codePoint;

        StringBuilder sb = new StringBuilder();

        for (int i = offsetStart; i < offsetEnd; i += Character.charCount(codePoint)) {
            codePoint = Character.codePointAt(charArray, i);
            sb.append(String.valueOf(Character.toChars(codePoint)));
        }

        return sb.toString();
    }
}
