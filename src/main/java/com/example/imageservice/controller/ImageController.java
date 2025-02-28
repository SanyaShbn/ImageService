package com.example.imageservice.controller;

import com.example.imageservice.service.GridFSImageStorageService;
import com.example.imageservice.service.KafkaSender;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    public static final String FILENAME_KEY = "filename";

    public static final String UPLOAD_DATE_KEY = "uploadDate";

    public static final String SIZE_KEY = "size";

    public static final String IMAGE_KEY = "image";

    private final GridFSImageStorageService imageService;

    private final KafkaSender kafkaSender;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        GridFSFile existingFile = imageService.getImageByFilename(file.getOriginalFilename());
        if (existingFile != null) {
            return ResponseEntity.ok().body(file.getOriginalFilename());
        }

        imageService.storeImage(file.getOriginalFilename(), file.getInputStream());
        kafkaSender.sendMessage(
                "Image uploaded with URL: " + file.getOriginalFilename(), "image.saved"
        );
        return ResponseEntity.ok().body(file.getOriginalFilename());
    }

    @GetMapping
    public List<Map<String, Object>> listImages() {
        return imageService.getAllImages().stream()
                .map(this::createImageInfo)
                .collect(Collectors.toList());
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Map<String, Object>> downloadImage(@PathVariable String filename) {
        GridFSFile file = imageService.getImageByFilename(filename);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageBytes = imageService.getImageBytes(filename);
        if (imageBytes == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> responseBody = createImageInfo(file);
        responseBody.put(IMAGE_KEY, imageBytes);

        return ResponseEntity.ok().body(responseBody);
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable String filename) {
        imageService.deleteImage(filename);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Image deleted successfully");
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createImageInfo(GridFSFile file) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(FILENAME_KEY, file.getFilename());
        metadata.put(UPLOAD_DATE_KEY, file.getUploadDate());
        metadata.put(SIZE_KEY, file.getLength());
        return metadata;
    }
}
