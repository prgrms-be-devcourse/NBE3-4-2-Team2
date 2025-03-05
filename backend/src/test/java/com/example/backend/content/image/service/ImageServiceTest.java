package com.example.backend.content.image.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.global.storage.LocalFileStorageService;

class LocalFileStorageServiceTest {

	private LocalFileStorageService fileStorageService;
	private static final String TEST_DIR = "uploads-test/";

	@BeforeEach
	void setUp() {
		fileStorageService = new LocalFileStorageService() {
			@Override
			public String uploadFile(MultipartFile file) {
				try {
					// Override default UPLOAD_DIR to TEST_DIR
					String originalFilename = file.getOriginalFilename();
					String fileName = java.util.UUID.randomUUID() + "_" + originalFilename;
					String filePath = Paths.get(TEST_DIR, fileName).toString();

					File dest = new File(filePath);
					dest.getParentFile().mkdirs(); // 폴더가 없으면 생성
					file.transferTo(dest);

					return "/uploads-test/" + fileName;
				} catch (Exception e) {
					throw new RuntimeException("File Upload Failed", e);
				}
			}

			@Override
			public void deleteFile(String fileUrl) {
				String fileName = fileUrl.replace("/uploads-test/", "");
				String filePath = Paths.get(TEST_DIR, fileName).toString();
				File file = new File(filePath);

				if (file.exists()) {
					file.delete();
				}
			}
		};
	}

	@Test
	@DisplayName("이미지 파일 업로드 성공")
	void t1() {
		// given
		MockMultipartFile mockFile = new MockMultipartFile(
			"file",
			"testImage.png",
			"image/png",
			"FakeImageData".getBytes()
		);

		// when
		String fileUrl = fileStorageService.uploadFile(mockFile);

		// then
		assertNotNull(fileUrl);
		assertTrue(fileUrl.contains("/uploads-test/"));

		// 실제 파일 존재 확인
		String fileName = fileUrl.replace("/uploads-test/", "");
		File file = new File(TEST_DIR + fileName);
		assertTrue(file.exists());
		System.out.println("이미지 파일 업로드 경로: " + file.getAbsolutePath());
	}

	@Test
	@DisplayName("이미지 파일 삭제 성공")
	void t2() {
		// given
		MockMultipartFile mockFile = new MockMultipartFile(
			"file",
			"testImage.png",
			"image/png",
			"FakeImageData".getBytes()
		);
		String fileUrl = fileStorageService.uploadFile(mockFile);

		// when
		fileStorageService.deleteFile(fileUrl);

		// then
		String fileName = fileUrl.replace("/uploads-test/", "");
		File file = new File(TEST_DIR + fileName);
		assertFalse(file.exists());
		System.out.println("이미지 파일 삭제 완료");
	}
}
