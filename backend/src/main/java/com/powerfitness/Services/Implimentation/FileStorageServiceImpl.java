package com.powerfitness.Services.Implimentation;

import com.powerfitness.Config.AppProperties;
import com.powerfitness.Exception.BadRequestException;
import com.powerfitness.Services.Interface.FileStorageService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif");

    private final AppProperties properties;

    public FileStorageServiceImpl(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public String storeProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file was uploaded.");
        }
        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new BadRequestException("Only JPEG, PNG, WEBP or GIF images are allowed.");
        }

        Path dir = Path.of(properties.uploads().dir(), "products").toAbsolutePath().normalize();
        String filename = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the uploaded file.", e);
        }
        return "/uploads/products/" + filename;
    }
}
