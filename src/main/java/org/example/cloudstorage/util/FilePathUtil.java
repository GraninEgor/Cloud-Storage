package org.example.cloudstorage.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePathUtil {

    private static String validPathRegex = "^(?!/)(?!.*//)(?:[A-Za-z0-9._-]+(?:/[A-Za-z0-9._-]+)*/?)$";
    private static String validQueryRegex = "^[A-Za-z0-9._]+$";


    public static String getResourceName(String path){
        if(getType(path).equals("FILE")){
            return path.substring(path.lastIndexOf('/') + 1);
        }
        else{
            int lastSlash = path.lastIndexOf('/');
            int prevSlash = path.lastIndexOf('/', lastSlash - 1);
            return path.substring(prevSlash + 1, lastSlash);
        }
    }

    public static String getPath(String path){
        if(getType(path).equals("FILE")){
            return path.substring(path.lastIndexOf('/') + 1);
        }
        else{
            int lastSlash = path.lastIndexOf('/');
            int prevSlash = path.lastIndexOf('/', lastSlash - 1);
            return path.substring(0,prevSlash);
        }
    }

    public static Boolean isValidPath(String path){
        return Objects.equals(path, "") ? false : path.matches(validPathRegex);
    }

    public static Boolean isValidQuery(String path){
        return Objects.equals(path, "") ? false : path.matches(validQueryRegex);
    }

    public static String getType(String path) {
        return path.endsWith("/") ? "DIRECTORY" : "FILE";
    }
}
