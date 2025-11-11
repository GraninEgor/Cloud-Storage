package org.example.cloudstorage.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePathUtil {

    private static String regex = "^(?!/)(?!.*//)(?:[A-Za-z0-9._-]+(?:/[A-Za-z0-9._-]+)*/?)$";

    public static String getResourceName(String path){
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public static String getPath(String path){
        return path.lastIndexOf('/') == -1 ? "/" : path.substring(0, path.lastIndexOf('/'));
    }

    public static Boolean isValid(String path){
        return Objects.equals(path, "") ? false : path.matches(regex);
    }

    public static String getType(String path) {
        return path.endsWith("/") ? "DIRECTORY" : "FILE";
    }
}
