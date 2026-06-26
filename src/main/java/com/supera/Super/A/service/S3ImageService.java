package com.supera.Super.A.service;

import com.supera.Super.A.config.AwsS3Config;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3ImageService {

    private final AwsS3Config awsS3Config;
    private final S3Client s3Client;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    public S3ImageService(AwsS3Config awsS3Config) {
        this.awsS3Config = awsS3Config;
        this.s3Client = initializeS3Client();
    }

    private S3Client initializeS3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                awsS3Config.getAccessKey(),
                awsS3Config.getSecretKey()
        );

        return S3Client.builder()
                .region(Region.of(awsS3Config.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public String uploadImage(MultipartFile file) throws IOException {
        validateFile(file);
        
        String fileName = generateFileName(file);
        byte[] fileContent = file.getBytes();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsS3Config.getBucket())
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileContent));

        return buildImageUrl(fileName);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido (5MB)");
        }

        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : ALLOWED_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            throw new IllegalArgumentException("El tipo de archivo no es válido. Solo se permiten imágenes (JPEG, PNG, GIF, WebP)");
        }
    }

    private String generateFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        return "products/" + UUID.randomUUID().toString() + extension;
    }

    private String buildImageUrl(String fileName) {
        if (awsS3Config.getCloudFrontUrl() != null && !awsS3Config.getCloudFrontUrl().isEmpty()) {
            return awsS3Config.getCloudFrontUrl() + "/" + fileName;
        }
        return "https://" + awsS3Config.getBucket() + ".s3." + awsS3Config.getRegion() + ".amazonaws.com/" + fileName;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            String fileName = extractFileNameFromUrl(imageUrl);
            software.amazon.awssdk.services.s3.model.DeleteObjectRequest deleteObjectRequest =
                    software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
                            .bucket(awsS3Config.getBucket())
                            .key(fileName)
                            .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            System.err.println("Error al eliminar archivo de S3: " + e.getMessage());
        }
    }

    private String extractFileNameFromUrl(String imageUrl) {
        // Extrae el nombre del archivo de la URL
        // https://bucket.s3.region.amazonaws.com/products/uuid.jpg -> products/uuid.jpg
        String[] parts = imageUrl.split("/");
        if (parts.length >= 2) {
            return parts[parts.length - 2] + "/" + parts[parts.length - 1];
        }
        return imageUrl;
    }
}
