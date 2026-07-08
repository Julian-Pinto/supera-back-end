package com.supera.Super.A.service;

import com.supera.Super.A.config.AwsS3Config;
import com.supera.Super.A.dto.ImageResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CreateInvalidationRequest;
import software.amazon.awssdk.services.cloudfront.model.InvalidationBatch;
import software.amazon.awssdk.services.cloudfront.model.Paths;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class S3ImageService {

    private final AwsS3Config awsS3Config;
    private final S3Client s3Client;
    private final CloudFrontClient cloudFrontClient;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    public S3ImageService(AwsS3Config awsS3Config, S3Client s3Client, CloudFrontClient cloudFrontClient) {
        this.awsS3Config = awsS3Config;
        this.s3Client = s3Client;
        this.cloudFrontClient = cloudFrontClient;
    }

    public ImageResponse uploadImage(MultipartFile file) throws IOException {
        validateFile(file);

        String fileName = generateFileName(file);
        byte[] fileContent = file.getBytes();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsS3Config.getBucket())
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileContent));

        return new ImageResponse(file.getOriginalFilename(), buildImageUrl(fileName), fileName);
    }

    public String uploadImageUrl(MultipartFile file) throws IOException {
        return uploadImage(file).getUrl();
    }

    public List<ImageResponse> uploadImages(MultipartFile[] files) throws IOException {
        List<ImageResponse> uploaded = new ArrayList<>();
        if (files == null) {
            return uploaded;
        }
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                uploaded.add(uploadImage(file));
            }
        }
        return uploaded;
    }

    public List<ImageResponse> listImages() {
        List<ImageResponse> images = new ArrayList<>();
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(awsS3Config.getBucket())
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        for (S3Object object : response.contents()) {
            String key = object.key();
            String name = extractFileNameFromKey(key);
            images.add(new ImageResponse(name, buildImageUrl(key), key));
        }
        return images;
    }

    public String getImageUrl(String imageNameOrKey) {
        if (imageNameOrKey == null || imageNameOrKey.isBlank()) {
            return null;
        }

        // Try treat the value as an object key first
        if (existsObject(imageNameOrKey)) {
            return buildImageUrl(imageNameOrKey);
        }

        List<ImageResponse> images = listImages();
        return images.stream()
                .filter(image -> imageNameOrKey.equals(image.getName()) || imageNameOrKey.equals(image.getKey()))
                .map(ImageResponse::getUrl)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada en S3: " + imageNameOrKey));
    }

    private boolean existsObject(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(awsS3Config.getBucket())
                    .key(key)
                    .build();
            s3Client.headObject(headRequest);
            return true;
        } catch (Exception e) {
            return false;
        }
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
        String fileName = null;

        if (originalFileName != null && !originalFileName.isBlank()) {
            fileName = new java.io.File(originalFileName).getName();
        }

        if (fileName == null || fileName.isBlank()) {
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            fileName = UUID.randomUUID().toString() + extension;
        }

        return "products/" + fileName;
    }

    private String buildImageUrl(String fileName) {
        String encodedPath = encodePathForUrl(fileName);
        if (awsS3Config.getCloudFrontUrl() != null && !awsS3Config.getCloudFrontUrl().isEmpty()) {
            return awsS3Config.getCloudFrontUrl().replaceAll("/*$", "") + "/" + encodedPath;
        }
        return "https://" + awsS3Config.getBucket() + ".s3." + awsS3Config.getRegion() + ".amazonaws.com/" + encodedPath;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            String fileName = extractFileNameFromUrl(imageUrl);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(awsS3Config.getBucket())
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            System.err.println("Error al eliminar archivo de S3: " + e.getMessage());
        }
    }

    public void deleteImageAndInvalidate(String fileName) {
        String key = resolveImageKey(fileName);
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("No se encontró el archivo en S3: " + fileName);
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(awsS3Config.getBucket())
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);

        String invalidationPath = "/" + encodePathForUrl(key);
        CreateInvalidationRequest invalidationRequest = CreateInvalidationRequest.builder()
                .distributionId(awsS3Config.getCloudFrontDistributionId())
                .invalidationBatch(InvalidationBatch.builder()
                        .paths(Paths.builder()
                                .quantity(1)
                                .items(invalidationPath)
                                .build())
                        .callerReference(UUID.randomUUID().toString())
                        .build())
                .build();

        cloudFrontClient.createInvalidation(invalidationRequest);
    }

    private String resolveImageKey(String fileName) {
        if (existsObject(fileName)) {
            return fileName;
        }

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(awsS3Config.getBucket())
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        return response.contents().stream()
                .filter(object -> fileName.equals(extractFileNameFromKey(object.key())))
                .map(S3Object::key)
                .findFirst()
                .orElse(null);
    }

    private String extractFileNameFromUrl(String imageUrl) {
        // Extrae el nombre del archivo de la URL
        String[] parts = imageUrl.split("/");
        if (parts.length >= 2) {
            return parts[parts.length - 2] + "/" + parts[parts.length - 1];
        }
        return imageUrl;
    }

    private String extractFileNameFromKey(String key) {
        if (key == null || key.isEmpty()) {
            return key;
        }
        String[] parts = key.split("/");
        return parts[parts.length - 1];
    }

    private String encodePathForUrl(String value) {
        String[] parts = value.split("/");
        return String.join("/", java.util.Arrays.stream(parts)
                .map(segment -> URLEncoder.encode(segment, StandardCharsets.UTF_8))
                .toList());
    }
}
