package com.spring.api.API.services;

import com.spring.api.API.security.Exceptions.StorageException;
import com.spring.api.API.security.StorageProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    private final Path rootLocation;

    private final List<String> EXTENSIONS = List.of("jpg", "png");

    public StorageService(@NonNull StorageProperties properties){
        if(properties.getUploadDir().trim().length() == 0){
            throw new StorageException("File upload location can not be Empty.");
        }
        this.rootLocation = Paths.get(properties.getUploadDir());
    }

    public String save(@NonNull MultipartFile file){
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());

            if(!EXTENSIONS.contains(extension))
                throw new RuntimeException("Invalid file Extension");

            String filename = UUID.randomUUID() + "." + extension;

            Path destinationFile = this.rootLocation.resolve(
                            Paths.get(filename))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new StorageException(
                        "Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return filename;
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }
}
