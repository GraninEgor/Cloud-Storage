package org.example.cloudstorage.util;

public class FilePathFormatter {

    public static String getResourceName(String path){
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public static String getPath(String path){
        return path.substring(0,path.lastIndexOf('/'));
    }
}
