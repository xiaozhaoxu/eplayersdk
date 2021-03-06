package com.ibrightech.eplayer.sdk.common.util;

import org.apache.http.util.EncodingUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: yangjunhai
 * Date: 11-6-21
 * Time: 下午2:58
 * copy from apache-io
 */
public class FileUtils {

    private static final char SYSTEM_SEPARATOR = File.separatorChar;
    public static String getFileSizeByLength(long size){
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb) {
            return String.format("%.1fGB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0fMB" : "%.1fMB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0fKB" : "%.1fKB", f);
        } else {
            return String.format("%dB", size);
        }
    }
    public static boolean fileIsWord (String filename){
        boolean isWord=false;
        if(CheckUtil.isEmpty(filename)){
            return isWord;
        }
        String prefix=getExtensionName(filename);
        if("doc".equalsIgnoreCase(prefix)
                ||"docx".equalsIgnoreCase(prefix)
                ||"dot".equalsIgnoreCase(prefix)
                ||"dotx".equalsIgnoreCase(prefix)
                ||"docm".equalsIgnoreCase(prefix)
                ||"dotm".equalsIgnoreCase(prefix)){
            isWord=true;
        }
        return isWord;
    }
    public static boolean fileIsPPT(String filename){
        boolean isPPT=false;
        if(CheckUtil.isEmpty(filename)){
            return isPPT;
        }
        String prefix=getExtensionName(filename);
        if("ppt".equalsIgnoreCase(prefix)||"pps".equalsIgnoreCase(prefix)||"pptx".equalsIgnoreCase(prefix)){
            isPPT=true;
        }
        return isPPT;
    }
    public static boolean fileIsPDF(String filename){
        boolean isPDF=false;
        if(CheckUtil.isEmpty(filename)){
            return isPDF;
        }
        String prefix=getExtensionName(filename);
        if("pdf".equalsIgnoreCase(prefix)){
            isPDF=true;
        }
        return isPDF;
    }
    public static void copyFile(File srcFile, File destFile)
            throws IOException {
        copyFile(srcFile, destFile, true);
    }

    public static void copyFile(File srcFile, File destFile, boolean preserveFileDate)
            throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destFile == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!srcFile.exists()) {
            throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
        }
        if (srcFile.isDirectory()) {
            throw new IOException("Source '" + srcFile + "' exists but is a directory");
        }
        if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
        }
        if ((destFile.getParentFile() != null) && (!destFile.getParentFile().exists()) &&
                (!destFile.getParentFile().mkdirs())) {
            throw new IOException("Destination '" + destFile + "' directory cannot be created");
        }

        if ((destFile.exists()) && (!destFile.canWrite())) {
            throw new IOException("Destination '" + destFile + "' exists but is read-only");
        }
        doCopyFile(srcFile, destFile, preserveFileDate);
    }

    private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate)
            throws IOException {
        if ((destFile.exists()) && (destFile.isDirectory())) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            input = fis.getChannel();
            output = fos.getChannel();
            long size = input.size();
            long pos = 0L;
            long count = 0L;
            while (pos < size) {
                count = size - pos > 52428800L ? 52428800L : size - pos;
                pos += output.transferFrom(input, pos, count);
            }
        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(fis);
        }

        if (srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
        }

        if (preserveFileDate)
            destFile.setLastModified(srcFile.lastModified());
    }

    public static boolean deleteQuietly(File file) {
        if (file == null)
            return false;
        try {
            if (file.isDirectory())
                cleanDirectory(file);
        } catch (Exception ignored) {
        }
        try {
            return file.delete();
        } catch (Exception ignored) {
        }
        return false;
    }

    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) {
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception)
            throw exception;
    }

    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                String message = "Unable to delete file: " + file;

                throw new IOException(message);
            }
        }
    }

    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        if (!isSymlink(directory)) {
            cleanDirectory(directory);
        }

        if (!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";

            throw new IOException(message);
        }
    }

    public static boolean isSymlink(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        if (isSystemWindows()) {
            return false;
        }
        File fileInCanonicalDir = null;
        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }

        return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    static boolean isSystemWindows() {
        return SYSTEM_SEPARATOR == '\\';
    }

    public static void forceMkdir(File directory) throws IOException {
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                String message = "File " + directory + " exists and is " + "not a directory. Unable to create directory.";

                throw new IOException(message);
            }
        } else if (!directory.mkdirs()) {
            if (!directory.isDirectory()) {
                String message = "Unable to create directory " + directory;

                throw new IOException(message);
            }
        }
    }

    public static FileOutputStream openOutputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite())
                throw new IOException("File '" + file + "' cannot be written to");
        } else {
            File parent = file.getParentFile();
            if ((parent != null) && (!parent.exists()) &&
                    (!parent.mkdirs())) {
                throw new IOException("File '" + file + "' could not be created");
            }
        }
        return new FileOutputStream(file);
    }

    public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canRead())
                throw new IOException("File '" + file + "' cannot be read");
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    public static void copyInputStreamToFile(InputStream source, File destination) throws IOException {
        try {
            FileOutputStream output = openOutputStream(destination);
            try {
                IOUtils.copy(source, output);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } finally {
            IOUtils.closeQuietly(source);
        }
    }

    private static void innerListFiles(Collection<File> files, File directory, FileFilter filter) {
        File[] found = directory.listFiles(filter);
        if (found != null)
            for (File file : found)
                if (file.isDirectory())
                    innerListFiles(files, file, filter);
                else
                    files.add(file);
    }


    public static Collection<File> listFiles(File directory, String suffix) {
        final String _suffix = "." + suffix;
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory())
                    return true;
                String name = file.getName();
                int endLen = _suffix.length();
                if (name.regionMatches(true, name.length() - endLen, _suffix, 0, endLen)) {
                    return true;
                }
                return false;
            }
        };
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Parameter 'directory' is not a directory");
        }

        if (filter == null) {
            throw new NullPointerException("Parameter 'fileFilter' is null");
        }
        Collection<File> files = new LinkedList<File>();
        innerListFiles(files, directory, filter);
        return files;
    }

    //读在/sdcard/目录下面的文件
    public static String readFileSdcard(String fileName) {
        String res = "";
        try {
            FileInputStream fin = new FileInputStream(fileName);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void writeFileSdcard(String fileName, String message, boolean append) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(fileName, append);
            byte[] bytes = message.getBytes();
            fout.write(bytes);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeByteFileSdcard(String fileName, byte[] bytes, boolean append) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(fileName, append);
            fout.write(bytes,0,bytes.length-1);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean isExist(String path) {
        File file = new File(path);
        return file.exists();
    }


    /**
     * 根据文件路径获取文件大小,单位为kb
     *
     * @param filePath
     * @return
     */
    public static long getFileSize(String filePath) {
        long fileSize = -1;
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            fileSize = file.length() / 1024;
        }
        return fileSize;
    }

    /*
    得到文件夹大小
    */
    public static long getDirSize(File file) {
        if (file.exists()) {
            //如果是目录则递归计算其内容的总大小
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                long size = 0;
                for (File f : children)
                    size += getDirSize(f);
                return size;
            } else {//如果是文件则直接返回其大小,以“兆”为单位
                long size =file.length();
                return size;
            }
        } else {
            return 0;
        }
    }
    /**
     * 根据文件路径获取文件夹大小
     * @param filePath
     * @return
     */

    public static String getFileSizeByPath(String filePath) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        String sizeresult = "0B";
        long size = getDirSize(new File(filePath));
        if (size >= gb) {
            return String.format("%.1fGB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0fMB" : "%.1fMB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0fKB" : "%.1fKB", f);
        } else {
            return String.format("%dB", size);
        }

    }

    /*
* Java文件操作 获取文件扩展名
*
*  Created on: 2011-8-2
*      Author: blueeagle
*/
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }
    /*
     * Java文件操作 获取不带扩展名的文件名
     *
     *  Created on: 2011-8-2
     *      Author: blueeagle
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
}
