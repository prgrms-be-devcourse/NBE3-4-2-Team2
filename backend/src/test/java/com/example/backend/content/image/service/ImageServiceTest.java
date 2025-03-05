package com.example.backend.content.image.service;

import com.example.backend.entity.ImageEntity;
import com.example.backend.entity.ImageRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.global.storage.LocalFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private LocalFileStorageService fileStorageService;

    private PostEntity post;

    @BeforeEach
    void setUp() {
        // 게시물 엔티티 초기화
        post = new PostEntity();
        post.setContent("테스트 게시물");
        postRepository.save(post);
    }

    @Test
    @DisplayName("t1: 이미지 업로드 테스트")
    void testUploadImages() throws Exception {
        // 테스트로 사용할 이미지 파일 생성
        Path path1 = Paths.get("src/test/resources/test-image-1.jpg");
        Path path2 = Paths.get("src/test/resources/test-image-2.jpg");

        // MockMultipartFile 생성 (이미지 파일 2개 업로드)
        MockMultipartFile file1 = new MockMultipartFile("files", "test-image-1.jpg", "image/jpeg", Files.readAllBytes(path1));
        MockMultipartFile file2 = new MockMultipartFile("files", "test-image-2.jpg", "image/jpeg", Files.readAllBytes(path2));

        // 이미지 업로드
        List<String> uploadedUrls = imageService.uploadImages(post, List.of(file1, file2));

        // 업로드된 URL 확인
        assertNotNull(uploadedUrls);
        assertEquals(2, uploadedUrls.size());
        assertTrue(uploadedUrls.get(0).startsWith("https://localhost:8080/api-v1/image"));
        assertTrue(uploadedUrls.get(1).startsWith("https://localhost:8080/api-v1/image"));

        // 업로드된 이미지가 데이터베이스에 저장되었는지 확인
        List<ImageEntity> images = imageRepository.findAll();
        assertEquals(2, images.size());

        // 이미지 엔티티 URL 확인
        assertTrue(images.get(0).getImageUrl().startsWith("https://localhost:8080/api-v1/image"));
        assertTrue(images.get(1).getImageUrl().startsWith("https://localhost:8080/api-v1/image"));
    }

    @Test
    @DisplayName("t2: 이미지 조회 테스트")
    void testGetImage() throws Exception {
        // 테스트로 사용할 이미지 파일 생성
        Path path = Paths.get("src/test/resources/test-image-1.jpg");
        MockMultipartFile file = new MockMultipartFile("file", "test-image-1.jpg", "image/jpeg", Files.readAllBytes(path));

        // 이미지 업로드
        List<String> uploadedUrls = imageService.uploadImages(post, List.of(file));

        // 업로드된 이미지 URL에서 UUID 추출
        String imageUrl = uploadedUrls.get(0);
        String imageId = imageUrl.split("/")[5];  // URL에서 UUID 추출 (예시: photo-1514888286974-6c03e2ca1dba)

        // 실제 이미지 조회
        byte[] imageBytes = fileStorageService.loadFile(imageId);

        // 이미지 데이터가 반환되는지 확인
        assertNotNull(imageBytes);
        assertTrue(imageBytes.length > 0);
    }

    @Test
    @DisplayName("t3: 이미지 삭제 테스트")
    void testDeleteImage() throws Exception {
        // 테스트로 사용할 이미지 파일 생성
        Path path = Paths.get("src/test/resources/test-image-1.jpg");
        MockMultipartFile file = new MockMultipartFile("file", "test-image-1.jpg", "image/jpeg", Files.readAllBytes(path));

        // 이미지 업로드
        List<String> uploadedUrls = imageService.uploadImages(post, List.of(file));
        String imageUrl = uploadedUrls.get(0);
        String imageId = imageUrl.split("/")[5];  // URL에서 UUID 추출

        // 업로드된 이미지가 데이터베이스에 저장되었는지 확인
        List<ImageEntity> imagesBeforeDelete = imageRepository.findAll();
        assertEquals(1, imagesBeforeDelete.size());

        // 이미지 삭제
        imageService.deleteImage(post.getId(), imagesBeforeDelete.get(0).getId());

        // 이미지가 데이터베이스에서 삭제되었는지 확인
        List<ImageEntity> imagesAfterDelete = imageRepository.findAll();
        assertEquals(0, imagesAfterDelete.size());
    }
}
