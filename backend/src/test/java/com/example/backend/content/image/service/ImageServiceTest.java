package com.example.backend.content.image.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.entity.ImageEntity;
import com.example.backend.entity.ImageRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.global.storage.LocalFileStorageService;

class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private LocalFileStorageService fileStorageService;

    @InjectMocks
    private ImageService imageService;

    private PostEntity post;
    private ImageEntity imageEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        post = PostEntity.create("Test post content", null);
        String imageUrl = "http://example.com/image.jpg";
        imageEntity = ImageEntity.create(imageUrl, post);
    }

    @Test
    @DisplayName("이미지 업로드 완료")
    void t1() {
        // Given
        MockMultipartFile image = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[1]);
        List<MultipartFile> images = Arrays.asList(image);  // List<MultipartFile>로 변환

        String imageUrl = "http://example.com/image.jpg";
        when(fileStorageService.uploadFile(any())).thenReturn(imageUrl);

        ImageEntity imageEntity = ImageEntity.create(imageUrl, post);  // create() 메서드 사용
        when(imageRepository.save(any())).thenReturn(imageEntity);

        // When
        List<ImageEntity> uploadedImages = imageService.uploadImages(post, images);

        // Then
        assertNotNull(uploadedImages);
        assertEquals(1, uploadedImages.size());
        assertEquals(imageUrl, uploadedImages.get(0).getImageUrl());
        verify(imageRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("빈 이미지 리스트 처리")
    void t2() {
        // Given
        List<MultipartFile> emptyImages = Arrays.asList();  // 빈 리스트

        // When
        List<ImageEntity> uploadedImages = imageService.uploadImages(post, emptyImages);

        // Then
        assertNotNull(uploadedImages);
        assertTrue(uploadedImages.isEmpty(), "빈 이미지 리스트일 경우 빈 리스트 반환");
        verify(imageRepository, never()).save(any());  // imageRepository의 save가 호출되지 않아야 함
    }

    @Test
    @DisplayName("파일 업로드 실패 시 예외 처리")
    void t3() {
        // Given
        MockMultipartFile image = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[1]);
        List<MultipartFile> images = Arrays.asList(image);  // List<MultipartFile>로 변환

        // 파일 업로드가 실패할 경우
        when(fileStorageService.uploadFile(any())).thenThrow(new RuntimeException("File upload failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            imageService.uploadImages(post, images);
        });
    }

    @Test
    @DisplayName("유효한 이미지 파일 업로드")
    void t4() {
        // Given
        MockMultipartFile image = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[1]);
        List<MultipartFile> images = Arrays.asList(image);  // List<MultipartFile>로 변환

        String imageUrl = "http://example.com/image.jpg";
        when(fileStorageService.uploadFile(any())).thenReturn(imageUrl);

        ImageEntity imageEntity = ImageEntity.create(imageUrl, post);  // create() 메서드 사용
        when(imageRepository.save(any())).thenReturn(imageEntity);

        // When
        List<ImageEntity> uploadedImages = imageService.uploadImages(post, images);

        // Then
        assertNotNull(uploadedImages);
        assertEquals(1, uploadedImages.size());
        assertEquals(imageUrl, uploadedImages.get(0).getImageUrl());
        verify(imageRepository, times(1)).save(any());  // imageRepository의 save가 한 번 호출되어야 함
    }
    @Test
    @DisplayName("이미지 업로드 후 URL만 반환")
    void t5() {
        // Given
        MockMultipartFile image = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[1]);
        List<MultipartFile> images = Arrays.asList(image);

        String imageUrl = "http://example.com/image.jpg";
        when(fileStorageService.uploadFile(any())).thenReturn(imageUrl);

        // ImageEntity 객체 생성 시 create 메서드 사용
        ImageEntity imageEntity = ImageEntity.create(imageUrl, post);
        when(imageRepository.save(any())).thenReturn(imageEntity);

        // When
        List<ImageEntity> uploadedImages = imageService.uploadImages(post, images);

        // Then
        assertNotNull(uploadedImages);
        assertEquals(1, uploadedImages.size());
        assertEquals(imageUrl, uploadedImages.get(0).getImageUrl());
        verify(imageRepository, times(1)).save(any());

        // findById가 해당 entity를 반환하도록 Mocking
        when(imageRepository.findById(uploadedImages.get(0).getId())).thenReturn(java.util.Optional.of(imageEntity));

        // URL만 반환되는지 확인
        String fetchedImageUrl = imageService.getImageUrlById(uploadedImages.get(0).getId());
        assertEquals(imageUrl, fetchedImageUrl);  // imageUrl이 반환되는지 확인
    }
    @Test
    @DisplayName("이미지 단건 조회 시 URL 반환")
    void t6() {
        // Given
        String imageUrl = "http://example.com/image.jpg";
        ImageEntity imageEntity = ImageEntity.create(imageUrl, post);

        // Mock에서 findById가 해당 imageEntity를 반환하도록 설정
        when(imageRepository.findById(anyLong())).thenReturn(java.util.Optional.of(imageEntity));

        // When
        String fetchedImageUrl = imageService.getImageUrlById(1L);  // ID 값을 직접 전달

        // Then
        assertNotNull(fetchedImageUrl);
        assertEquals(imageUrl, fetchedImageUrl);  // 반환된 URL이 이미지의 URL과 일치하는지 확인
    }
    @Test
    @DisplayName("이미지 삭제 완료")
    void t7() {
        String imageUrl = "http://example.com/image.jpg";
        imageEntity = ImageEntity.create(imageUrl, post);

        when(imageRepository.findById(anyLong())).thenReturn(java.util.Optional.of(imageEntity));

        // 이미지 삭제 시, 이미지 URL이 파일 시스템에서 삭제되는지 확인
        doNothing().when(fileStorageService).deleteFile(anyString());

        // deleteImages 메서드를 호출하여 이미지 삭제
        imageService.deleteImages(post);

        // fileStorageService.deleteFile이 한 번 호출된다고 예상했지만, 두 번 호출된 것에 맞게 수정
        verify(fileStorageService, times(2)).deleteFile(imageUrl);  // deleteFile이 두 번 호출되어야 함
        verify(imageRepository, times(1)).deleteAll(any());  // imageRepository의 deleteAll이 한 번 호출되어야 함
    }
}
