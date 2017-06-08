package com.meituan.robust.patch.resources.diff.apkdiffer;

import com.meituan.robust.patch.resources.config.RobustResourceConfig;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;

/**
 * Created by hedingxu on 17/5/31.
 */

public class OldApkFileVisitor extends SimpleFileVisitor<Path> {
    APKDiffer apkDiffer;
    RobustResourceConfig config;
    Path newApkPath;
    Path oldApkPath;
    //相对路径
    private HashSet<Path> visitedPathes;

    OldApkFileVisitor(RobustResourceConfig config, Path newPath, Path oldPath, APKDiffer apkDiffer, HashSet<Path> visitedPathes) {
        this.config = config;
        this.apkDiffer = apkDiffer;
        this.newApkPath = newPath;
        this.oldApkPath = oldPath;
        this.visitedPathes = visitedPathes;
    }

    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {

        Path newFilePath = filePath;
        //new File 相对路径
        Path relativePath = newApkPath.relativize(newFilePath);
        if (visitedPathes.contains(relativePath)) {
            return FileVisitResult.CONTINUE;
        }
        visitedPathes.add(relativePath);

        try {
            apkDiffer.diffOldFile(filePath);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return FileVisitResult.CONTINUE;
    }
}
