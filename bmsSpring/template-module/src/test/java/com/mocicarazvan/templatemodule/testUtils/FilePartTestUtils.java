package com.mocicarazvan.templatemodule.testUtils;

import com.mocicarazvan.templatemodule.utils.FileSystemFilePart;
import org.springframework.http.codec.multipart.FilePart;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

public class FilePartTestUtils {


    public static FilePart createFilePart(String fileName) throws IOException {
        File tempFile = File.createTempFile(fileName, ".png");
        byte[] randomBytes = new byte[1024];
        new Random().nextBytes(randomBytes);
        Files.write(tempFile.toPath(), randomBytes);
        return new FileSystemFilePart(tempFile, "files");
    }
}
