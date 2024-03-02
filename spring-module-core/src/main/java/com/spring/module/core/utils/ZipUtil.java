package com.spring.module.core.utils;

import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * zip工具类
 * <p>
 * 1.压缩：文件、文件夹的压缩
 * <br/>
 * 2.解压：文件、文件夹的解压
 *
 * @author DearYang
 * @date 2022-10-11
 * @see <a href="https://springboot.io/t/topic/2869">ZipUtil</a>
 * @since 1.0
 */
public abstract class ZipUtil {

    private static final String SLASH = "/";

    /**
     * 指定多个文件压缩为zip
     *
     * @param srcPath 需要被压缩的文件
     * @param zipFile 压缩后的目标zip文件
     * @throws IOException io异常
     */
    public static void zip(Path[] srcPath, Path zipFile) throws IOException {
        if (!Files.exists(zipFile.getParent())) {
            Files.createDirectories(zipFile.getParent());
        }

        try (OutputStream os = Files.newOutputStream(zipFile, StandardOpenOption.CREATE_NEW);
             ZipOutputStream zos = new ZipOutputStream(os)) {
            Arrays.stream(srcPath).forEach(file -> compressPath(file, zos));
            zos.closeEntry();
        }
    }

    public static Set<Path> unzip(InputStream is, Path destDirectory) throws IOException {
        Set<Path> paths = new HashSet<>();
        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                try {
                    unzip(destDirectory, entry, zis, paths);
                } catch (Exception e) {
                    zis.closeEntry();
                    throw e;
                }
                entry = zis.getNextEntry();
            }
        }
        return paths;
    }

    private static void unzip(Path destDirectory, ZipEntry entry, ZipInputStream zis, Set<Path> paths) throws IOException {
        Path filePath = destDirectory.resolve(entry.getName());
        if (Files.exists(filePath)) {
            for (Path path : paths) {
                if (Files.exists(path)) {
                    FileSystemUtils.deleteRecursively(path);
                }
            }
            throw new FileAlreadyExistsException("路径[" + filePath + "]已经存在");
        }

        paths.add(filePath);
        if (entry.isDirectory()) {
            Files.createDirectories(filePath);
        } else {
            try (OutputStream os = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)) {
                zis.transferTo(os);
            }
        }
    }

    /**
     * 压缩路径
     *
     * @param srcPath 路径
     * @param zos     zip输出流
     */
    private static void compressPath(Path srcPath, ZipOutputStream zos) {
        if (!Files.exists(srcPath)) {
            return;
        }

        try {
            if (Files.isDirectory(srcPath)) {
                compressPacket(srcPath, zos);
                return;
            }
            compressFile(srcPath, zos);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * 文件添加到压缩包中
     *
     * @param srcFile 文件
     * @param zos     zip输出流
     * @throws IOException io异常
     */
    private static void compressFile(Path srcFile, ZipOutputStream zos) throws IOException {
        try (InputStream is = Files.newInputStream(srcFile)) {
            is.transferTo(zos);
        }
    }

    /**
     * 目录添加到压缩包中
     *
     * @param srcDir 目录
     * @param zos    zip输出流
     * @throws IOException io异常
     */
    private static void compressPacket(Path srcDir, ZipOutputStream zos) throws IOException {
        LinkedList<String> path = new LinkedList<>();
        Files.walkFileTree(srcDir, new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                // 开始遍历目录
                String folder = dir.getFileName().toString();
                path.addLast(folder);
                // 写入目录
                ZipEntry zipEntry = new ZipEntry(String.join(SLASH, path) + SLASH);
                try {
                    zos.putNextEntry(zipEntry);
                    zos.flush();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                // 开始遍历文件
                try (InputStream is = Files.newInputStream(file)) {
                    // 创建一个压缩项，指定名称
                    String fileName = !path.isEmpty()
                            ? (String.join(SLASH, path) + SLASH + file.getFileName().toString())
                            : file.getFileName().toString();
                    ZipEntry zipEntry = new ZipEntry(fileName);

                    // 添加到压缩流
                    zos.putNextEntry(zipEntry);
                    // 写入数据
                    is.transferTo(zos);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                // 结束遍历目录
                if (!path.isEmpty()) {
                    path.removeLast();
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
