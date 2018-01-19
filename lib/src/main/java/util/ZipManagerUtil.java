package util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Maksim Bezrukov
 */
public class ZipManagerUtil {

    public static File zipDirectory(File dirToZip, String fileName) throws IOException {
        if (!dirToZip.isDirectory()) {
            throw new IllegalArgumentException("Argument shall be an existing directory");
        }
        File res = new File(dirToZip.getParent(), fileName + ".zip");
        try (FileOutputStream fos = new FileOutputStream(res);
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            for (File file : dirToZip.listFiles()) {
                zipFile(file, file.getName(), zipOut);
            }
        }

        return res;
    }

    public static File unzipFile(File zipFile, String dirName) throws IOException {
        if (dirName == null) {
            dirName = getDirName(zipFile.getName());
        }
        File dir = new File(zipFile.getParentFile(), dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (FileInputStream fis = new FileInputStream(zipFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                if (!ze.isDirectory()) {
                    String fileName = ze.getName();
                    File newFile = new File(dir, fileName);
                    File parentFile = newFile.getParentFile();
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        byte[] buffer = new byte[1024];
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
        }
        return dir;
    }

    private static String getDirName(String zipName) {
        return zipName.substring(0, zipName.lastIndexOf("."));
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }
}
