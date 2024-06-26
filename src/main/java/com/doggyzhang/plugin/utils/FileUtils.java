package com.doggyzhang.plugin.utils;

import com.doggyzhang.plugin.FileFinder;
import com.doggyzhang.plugin.configs.Configs;

import java.io.File;
import java.util.List;

public class FileUtils {
    /**
     * 扫描文件
     *
     * @param rootDir
     * @param includesPattern
     * @param exIncludesPattern
     * @return
     */
    public static List<String> scanFiles(File rootDir, String includesPattern, String exIncludesPattern) {
        FileFinder mainFolderFinder = new FileFinder(includesPattern, exIncludesPattern);
        return mainFolderFinder.findIncludeFile(rootDir);
    }

    public static List<String> scanFiles(File rootDir, String includesPattern) {
        return scanFiles(rootDir, includesPattern, "");
    }

    /**
     * 扫描文件夹
     *
     * @param rootDir
     * @param includesPattern
     * @param exIncludesPattern
     * @return
     */
    public static List<String> scanFolder(File rootDir, String includesPattern, String exIncludesPattern) {
        FileFinder mainFolderFinder = new FileFinder(includesPattern, exIncludesPattern);
        return mainFolderFinder.findIncludeDir(rootDir);
    }

    public static List<String> scanFolder(File rootDir, String includesPattern) {
        return scanFolder(rootDir, includesPattern, "");
    }

    public static List<String> scanMainResFiles(File rootDir) {
        return scanMainResFiles(rootDir, "");
    }

    public static List<String> scanMainResFiles(File rootDir, String exIncludesPattern) {
        FileFinder mainFolderFinder = new FileFinder(Configs.STRINGS_MAIN_RES_PATH_INCLUDE, exIncludesPattern);
        return mainFolderFinder.findIncludeFile(rootDir);
    }

    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int i = fileName.lastIndexOf(".");
        if (i >= 0) {
            return fileName.substring(0, i);
        }
        return fileName;
    }

}
