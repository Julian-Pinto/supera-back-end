package com.supera.Super.A.controller;

import com.supera.Super.A.dto.ImageResponse;
import com.supera.Super.A.service.S3ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final S3ImageService s3ImageService;

    public ImageController(S3ImageService s3ImageService) {
        this.s3ImageService = s3ImageService;
    }

    @GetMapping
    public ResponseEntity<List<ImageResponse>> listImages() {
        return ResponseEntity.ok(s3ImageService.listImages());
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImageResponse>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        try {
            return ResponseEntity.ok(s3ImageService.uploadImages(files));
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }
}
