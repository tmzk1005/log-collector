package zk.logcollector.plugins.input;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import zk.logcollector.plugin.api.LogRecord;

public class FileInputTest {

    private final Path workPath;

    private final FileInput fileInput;

    public FileInputTest() throws IOException {
        workPath = Files.createTempDirectory("file-input-test");
        fileInput = new FileInput();
        String pattern = workPath.resolve("*.log").toString();
        fileInput.setFilePatterns(List.of(pattern));
        fileInput.setNewFileDetectInterval(500);
        fileInput.setNewLineDetectInterval(500);
    }

    @Test
    public void testFileInput() throws Exception {
        fileInput.start();
        Assert.assertTrue(fileInput.getDetectedFiles().isEmpty());
        Path log1 = workPath.resolve("1.log");
        Path log2 = workPath.resolve("2.log");
        Files.createFile(log1);
        Files.createFile(log2);
        Thread.sleep(fileInput.getNewFileDetectInterval());
        Assert.assertEquals(2, fileInput.getDetectedFiles().size());
        Set<String> detectedFileNames = fileInput.getDetectedFiles().stream().map(Path::toString).collect(Collectors.toSet());
        Assert.assertTrue(detectedFileNames.contains(log1.toString()));
        Assert.assertTrue(detectedFileNames.contains(log2.toString()));

        String line = "hello world 1";
        writeLine(log1, line);
        Thread.sleep(fileInput.getNewLineDetectInterval());
        Assert.assertEquals(line, ((String) fileInput.emit().getField(LogRecord.MESSAGE)).trim());

        line = "hello world 2";
        writeLine(log2, line);
        Thread.sleep(fileInput.getNewLineDetectInterval());
        Assert.assertEquals(line, ((String) fileInput.emit().getField(LogRecord.MESSAGE)).trim());

        fileInput.stop();
    }

    private static void writeLine(Path path, String content) throws IOException {
        OutputStream outputStream = Files.newOutputStream(path);
        outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        outputStream.write('\n');
        outputStream.flush();
        outputStream.close();
    }

    @After
    public void clean() {
        try {
            FileUtils.deleteDirectory(workPath.toFile());
        } catch (IOException ignore) {
            // ignore
        }
    }

}
