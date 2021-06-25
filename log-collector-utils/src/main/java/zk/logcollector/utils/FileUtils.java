package zk.logcollector.utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileUtils {

    public static List<Path> glob(String globPattern) throws IOException {
        List<Path> fileNames = new ArrayList<>();
        Path rootDir = getRootDir(globPattern);
        if (Files.isRegularFile(rootDir)) {
            fileNames.add(rootDir.toAbsolutePath());
            return fileNames;
        }
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
        Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Objects.requireNonNull(file);
                Objects.requireNonNull(attrs);
                if (pathMatcher.matches(file)) {
                    fileNames.add(file.toAbsolutePath());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return fileNames;
    }

    /**
     * 根据一个glob模式的路径字符串，找到其路径上第一个存在的目录 如果本身就是一个确定的文件，
     * 返回文件本身 eg：/tmp/test*.java --> /tmp eg: /tmp/abc/{1, 2}/test.java ->
     * /tmp/abc
     *
     * @param globPattern glob pattern
     * @return Path directory
     */
    public static Path getRootDir(String globPattern) {
        Path globPath = Paths.get(globPattern).toAbsolutePath();
        Path result = globPath.getRoot();
        List<Path> pathSegments = new ArrayList<>();
        globPath.spliterator().forEachRemaining(pathSegments::add);
        for (Path pathSegment : pathSegments) {
            Path next = result.resolve(pathSegment);
            if (!Files.exists(next)) {
                break;
            } else {
                result = next;
            }
        }
        return result;
    }

}
