// package com.example.backend.content.image.service;
//
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;
//
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.mock.web.MockMultipartFile;
// import org.springframework.web.multipart.MultipartFile;
//
// import com.example.backend.entity.ImageEntity;
// import com.example.backend.entity.ImageRepository;
// import com.example.backend.entity.MemberEntity;
// import com.example.backend.entity.PostEntity;
// import com.example.backend.entity.PostRepository;
// import com.example.backend.global.storage.LocalFileStorageService;
//
// @ExtendWith(MockitoExtension.class)
// class ImageServiceDetailedTest {
//
// 	@Mock
// 	private ImageRepository imageRepository;
//
// 	@Mock
// 	private PostRepository postRepository;
//
// 	@Mock
// 	private LocalFileStorageService fileStorageService;
//
// 	@InjectMocks
// 	private ImageService imageService;
//
// 	private PostEntity mockPost;
// 	private MemberEntity mockMember;
// 	private MultipartFile mockMultipartFile;
// 	private ImageEntity mockImageEntity;
//
// 	@BeforeEach
// 	void setUp() {
// 		mockMember = MemberEntity.builder()
// 			.username("testUser")
// 			.email("test@example.com")
// 			.password("testPassword")
// 			.build();
// 		mockMember.setId(1L);
//
// 		mockPost = PostEntity.builder()
// 			.content("Test Content")
// 			.member(mockMember)
// 			.build();
// 		mockPost.setId(2L);
//
// 		mockMultipartFile = new MockMultipartFile(
// 			"file",
// 			"test-image.jpg",
// 			"image/jpeg",
// 			"test image content".getBytes()
// 		);
//
// 		mockImageEntity = ImageEntity.builder()
// 			.imageUrl("/uploads/test-image.jpg")
// 			.post(mockPost)
// 			.build();
// 		mockImageEntity.setId(3L);
// 	}
//
// 	@Test
// 	@DisplayName("t1: 다중 이미지 업로드 성공")
// 	void t1() {
// 		// Given
// 		List<MultipartFile> files = Arrays.asList(
// 			mockMultipartFile,
// 			new MockMultipartFile(
// 				"file2",
// 				"test-image2.jpg",
// 				"image/jpeg",
// 				"test image content 2".getBytes()
// 			)
// 		);
//
// 		when(fileStorageService.uploadFile(any(MultipartFile.class)))
// 			.thenReturn("/uploads/test-image.jpg", "/uploads/test-image2.jpg");
//
// 		// When
// 		List<String> uploadedUrls = imageService.uploadImages(mockPost, files);
//
// 		// Then
// 		assertEquals(2, uploadedUrls.size());
// 		verify(fileStorageService, times(2)).uploadFile(any(MultipartFile.class));
// 		verify(imageRepository, times(2)).save(any(ImageEntity.class));
// 	}
//
// 	@Test
// 	@DisplayName("t2: 이미지 업로드 시 파일 저장 실패")
// 	void t2() {
// 		// Given
// 		when(fileStorageService.uploadFile(any(MultipartFile.class)))
// 			.thenThrow(new RuntimeException("파일 저장 실패"));
//
// 		// When & Then
// 		assertThrows(RuntimeException.class, () -> {
// 			imageService.uploadImages(mockPost, Arrays.asList(mockMultipartFile));
// 		});
// 	}
//
// 	@Test
// 	@DisplayName("t3: 특정 게시물의 이미지 삭제 성공")
// 	void t3() {
// 		// Given
// 		when(postRepository.findById(mockPost.getId())).thenReturn(Optional.of(mockPost));
// 		when(imageRepository.findById(mockImageEntity.getId())).thenReturn(Optional.of(mockImageEntity));
//
// 		// When
// 		imageService.deleteImage(mockPost.getId(), mockImageEntity.getId());
//
// 		// Then
// 		verify(postRepository).findById(mockPost.getId());
// 		verify(imageRepository).findById(mockImageEntity.getId());
// 		verify(fileStorageService).deleteFile(mockImageEntity.getImageUrl());
// 		verify(imageRepository).delete(mockImageEntity);
// 	}
//
// 	@Test
// 	@DisplayName("t4: 존재하지 않는 게시물의 이미지 삭제 시 예외 발생")
// 	void t4() {
// 		// Given
// 		when(postRepository.findById(anyLong())).thenReturn(Optional.empty());
//
// 		// When & Then
// 		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
// 			imageService.deleteImage(999L, 1L);
// 		});
//
// 		assertEquals("게시물을 찾을 수 없습니다.", exception.getMessage());
// 	}
//
// 	@Test
// 	@DisplayName("t5: 존재하지 않는 이미지 삭제 시 예외 발생")
// 	void t5() {
// 		// Given
// 		when(postRepository.findById(anyLong())).thenReturn(Optional.of(mockPost));
// 		when(imageRepository.findById(anyLong())).thenReturn(Optional.empty());
//
// 		// When & Then
// 		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
// 			imageService.deleteImage(mockPost.getId(), 999L);
// 		});
//
// 		assertEquals("이미지를 찾을 수 없습니다.", exception.getMessage());
// 	}
//
// 	@Test
// 	@DisplayName("t6: 이미지 URL에서 이미지 ID 추출")
// 	void t6() {
// 		// Given
// 		String fullUrl = "/uploads/uuid-example-image.jpg";
// 		String expectedImageId = "uuid-example-image.jpg";
//
// 		// When
// 		String extractedImageId = imageService.extractImageId(fullUrl);
//
// 		// Then
// 		assertEquals(expectedImageId, extractedImageId);
// 	}
//
// 	@Test
// 	@DisplayName("t7: 잘못된 형식의 이미지 URL에서 ID 추출")
// 	void t7() {
// 		// Given
// 		String invalidUrl = "invalid-url";
//
// 		// When
// 		String extractedImageId = imageService.extractImageId(invalidUrl);
//
// 		// Then
// 		assertEquals(invalidUrl, extractedImageId);
// 	}
// }