package com.techzone.peru.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("No se puede guardar un archivo vacío.");
        }

        try {
            // Generamos un nombre de archivo único para evitar colisiones
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            Path uploadPath = Paths.get(this.uploadDir);
            Path destinationFile = uploadPath.resolve(uniqueFilename);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Files.copy(file.getInputStream(), destinationFile);

            return uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo subido.", e);
        }
    }
}