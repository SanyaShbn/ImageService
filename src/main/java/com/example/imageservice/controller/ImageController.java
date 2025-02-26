package com.example.imageservice.controller;

import com.example.imageservice.service.GridFSImageStorageService;
import com.example.imageservice.service.KafkaSender;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpHeaders;
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

    private static final String IMAGE_SERVICE_URL = "http://localhost:8081/api/v1/images/";

    private final GridFSImageStorageService imageService;

    private final KafkaSender kafkaSender;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        ObjectId id = imageService.storeImage(file.getOriginalFilename(), file.getInputStream());
        String imageUrl = IMAGE_SERVICE_URL + id.toString();

        kafkaSender.sendMessage("Image uploaded with URL: " + imageUrl, "image.saved");

        return ResponseEntity.ok().body(imageUrl);
    }

    @GetMapping
    public List<Map<String, Object>> listImages() {
        return imageService.getAllImages().stream()
                .map(this::createImageInfo)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> downloadImage(@PathVariable String id) {
        GridFSFile file = imageService.getImageById(new ObjectId(id));
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> responseBody = createImageInfo(file);

        return ResponseEntity.ok().body(responseBody);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable String id) {
        imageService.deleteImage(new ObjectId(id));
        Map<String, String> response = new HashMap<>();
        response.put("message", "Image deleted successfully");
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createImageInfo(GridFSFile file) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("filename", file.getFilename());
        metadata.put("uploadDate", file.getUploadDate());
        metadata.put("size", file.getLength());

        byte[] imageBytes = imageService.getImageBytes(file);

        Map<String, Object> imageInfo = new HashMap<>();
        imageInfo.put("metadata", metadata);
        imageInfo.put("image", imageBytes);
        return imageInfo;
    }
}
