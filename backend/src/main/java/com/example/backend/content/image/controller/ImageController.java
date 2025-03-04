package com.example.backend.content.image.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.content.image.service.ImageService;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    // 이미지 단건 조회 (URL만 반환)
    @GetMapping("/{imageId}")
    public ResponseEntity<String> getImageUrl(@PathVariable Long imageId) {
        String imageUrl = imageService.getImageUrlById(imageId);
        return ResponseEntity.ok(imageUrl);  // imageUrl만 반환
    }
}
