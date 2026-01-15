package org.example.cloudstorage.core.util;

import java.util.Objects;

public class FilePathUtil {

    private static final String validPathRegex = "^(?!/)(?!.*//)(?:[A-Za-z0-9._-]+(?:/[A-Za-z0-9._-]+)*/?)$";
    private static final String VALID_FILE_NAME = "^(?!\\/)(?!.*\\/$)[A-Za-z0-9._\\/-]+$";

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

    public static String getResourcePath(String path){
        if(getType(path).equals("FILE")){
            return path.substring(path.lastIndexOf('/') + 1);
        }
        else{
            int lastSlash = path.lastIndexOf('/');
            int prevSlash = path.lastIndexOf('/', lastSlash - 1);
            return path.substring(0,prevSlash);
        }
    }

    public static boolean isFileNameValid(String fileName){
        if (fileName == null){
            return false;
        }
        return fileName.matches(VALID_FILE_NAME);
    }

    public static ResourceType getType(String path) {
        return path.endsWith("/") ? ResourceType.DIRECTORY : ResourceType.FILE;
    }
}
