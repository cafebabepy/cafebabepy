package org.cafebabepy.util;

import java.util.regex.Pattern;

/**
 * Created by yotchang4s on 2017/06/02.
 */
public final class StringUtils {

    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    private StringUtils() {
    }

    public static int codePointCount(String target) {
        return target.codePointCount(0, target.length());
    }

    public static String codePointAt(String target, int index) {
        return new String(new int[]{target.codePointAt(index)}, 0, 1);
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

    public static String[] splitLastDot(String str) {
        String[] splitStr = new String[2];

        int index = str.lastIndexOf('.');
        if (index == -1) {
            splitStr[0] = "";
            splitStr[1] = str;

        } else {
            splitStr[0] = str.substring(0, index);
            splitStr[1] = str.substring(index + 1, str.length());
        }

        return splitStr;
    }

    public static String[] splitDot(String str) {
        return DOT_PATTERN.split(str);
    }

    public static boolean isEmpty(String str) {
        return (str == null || str.isEmpty());
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
