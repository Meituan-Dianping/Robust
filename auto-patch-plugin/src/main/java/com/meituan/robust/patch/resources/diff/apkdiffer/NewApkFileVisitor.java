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

public class NewApkFileVisitor extends SimpleFileVisitor<Path> {
    APKDiffer apkDiffer;
    RobustResourceConfig config;
    Path newApkPath;
    Path oldApkPath;
    //相对路径
    private HashSet<String> visitedPathes;

    NewApkFileVisitor(RobustResourceConfig config, Path newPath, Path oldPath, APKDiffer apkDiffer, HashSet<String> visitedPathes) {
        this.config = config;
        this.apkDiffer = apkDiffer;
        this.newApkPath = newPath;
        this.oldApkPath = oldPath;
        this.visitedPathes = visitedPathes;
    }

    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {

        Path relativePath = newApkPath.relativize(filePath);
        if (visitedPathes.contains(relativePath.toString())){
            return FileVisitResult.CONTINUE;
        }
        visitedPathes.add(relativePath.toString());

        try {
            apkDiffer.diffNewFile(filePath);
        } catch (Throwable throwable) {

        }
        return FileVisitResult.CONTINUE;
    }
}
