package com.example.imageservice.controller;

import com.example.imageservice.service.GridFSImageStorageService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final GridFSImageStorageService imageService;

    private static final String IMAGE_SERVICE_URL = "http://localhost:8081/api/v1/images/";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        ObjectId id = imageService.storeImage(file.getOriginalFilename(), file.getInputStream());
        String imageUrl = IMAGE_SERVICE_URL + id.toString();
        return ResponseEntity.ok().body(imageUrl);
    }

    @GetMapping
    public List<String> listImages() {
        return imageService.getAllImages().stream()
                .map(file -> file.getFilename() + " (" + file.getObjectId() + ")")
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> downloadImage(@PathVariable String id) {
        imageService.getImageById(new ObjectId(id));
        return ResponseEntity.ok().body("Image with ID: " + id);
    }
}
