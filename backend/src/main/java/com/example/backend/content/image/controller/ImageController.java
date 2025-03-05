package com.example.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.content.image.service.ImageService;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;

@RestController
@RequestMapping("/api/posts")
public class ImageController {

    private final PostRepository postRepository;
    private final ImageService imageService;

    @Autowired
    public ImageController(PostRepository postRepository ,ImageService imageService) {
        this.postRepository = postRepository;
        this.imageService = imageService;
    }

    /**
     * 게시물에 이미지 업로드
     *
     * @param postId 게시물 ID
     * @param files 업로드할 이미지 파일 목록
     * @return 업로드된 이미지 URL 목록
     */
    @PostMapping("/{postId}/images")
    public ResponseEntity<List<String>> uploadImages(@PathVariable Long postId,
    @RequestParam("files") List<MultipartFile> files) {

        PostEntity post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));

        List<String> uploadedUrls = imageService.uploadImages(post, files);
        return ResponseEntity.ok(uploadedUrls);
    }

    /**
     * 게시물에서 이미지 삭제
     *
     * @param postId 게시물 ID
     * @param imageId 이미지 ID
     * @return 성공 메시지
     */
    @DeleteMapping("/{postId}/images/{imageId}")
    public ResponseEntity<String> deleteImage(@PathVariable Long postId, @PathVariable Long imageId) {
        imageService.deleteImage(postId, imageId);
        return ResponseEntity.ok("이미지가 삭제되었습니다.");
    }
}
