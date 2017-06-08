package com.meituan.robust.common;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

public class PatternUtils {
    public static String convertToPatternString(String input) {
        //convert \\.
        if (input.contains(".")) {
            input = input.replaceAll("\\.", "\\\\.");
        }
        //convert ？to .
        if (input.contains("?")) {
            input = input.replaceAll("\\?", "\\.");
        }
        //convert * to.*
        if (input.contains("*")) {
            input = input.replace("*", ".*");
        }
        return input;
    }

    public static boolean isStringMatchesPatterns(String str, Collection<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(str).matches()) {
                return true;
            }
        }
        return false;
    }

    public static <T> String collectionToString(Collection<T> collection) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean isFirstElement = true;
        for (T element : collection) {
            if (isFirstElement) {
                isFirstElement = false;
            } else {
                sb.append(',');
            }
            sb.append(element);
        }
        sb.append('}');
        return sb.toString();
    }

    public static boolean checkFileInPatternCompatOs(HashSet<Pattern> patterns, Path relativePath) {

        //兼容 linux mac windows
        if (!patterns.isEmpty()) {
            for (Iterator<Pattern> it = patterns.iterator(); it.hasNext(); ) {
                Pattern p = it.next();
                String linux_mac_key = relativePath.toString().replace("\\", "/");
                String windows_key = relativePath.toString().replace("/", "\\");
                if (p.matcher(linux_mac_key).matches() || p.matcher(windows_key).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean checkFileInPatternCompatOs(HashSet<Pattern> patterns, String relativePath) {

        //兼容 linux mac windows
        if (!patterns.isEmpty()) {
            for (Iterator<Pattern> it = patterns.iterator(); it.hasNext(); ) {
                Pattern p = it.next();
                String linux_mac_key = relativePath.toString().replace("\\", "/");
                String windows_key = relativePath.toString().replace("/", "\\");
                if (p.matcher(linux_mac_key).matches() || p.matcher(windows_key).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

}
