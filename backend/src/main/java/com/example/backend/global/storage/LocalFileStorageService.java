package com.example.backend.global.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LocalFileStorageService {

	private static final String UPLOAD_DIR = "C:/Users/jylee/Desktop/nginx-1.27.4/html/uploads/"; // NGINX가 제공할 경로

	public String uploadFile(MultipartFile file) {
		try {
			// 파일 확장자 확인
			String originalFilename = file.getOriginalFilename();
			String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
			if (!isValidExtension(extension)) {
				throw new IllegalArgumentException("허용되지 않은 파일 형식입니다.");
			}

			// 랜덤 파일명 생성
			String storedFileName = UUID.randomUUID() + extension;
			File destFile = new File(UPLOAD_DIR + storedFileName);

			// 디렉토리 없으면 생성
			Files.createDirectories(Paths.get(UPLOAD_DIR));

			// 파일 저장
			file.transferTo(destFile);

			// NGINX에서 접근할 수 있는 URL 반환
			// DB에 저장할 값은 경로 없이 파일명만 위에서 NGINX가 /uploads/로 매핑됨
			return storedFileName;

		} catch (IOException e) {
			throw new RuntimeException("파일 저장 실패", e);
		}
	}

	public void deleteFile(String fileUrl) {
		try {
			String filePath = UPLOAD_DIR + fileUrl.replace("/uploads/", "");
			Files.deleteIfExists(Paths.get(filePath));
		} catch (IOException e) {
			throw new RuntimeException("파일 삭제 실패", e);
		}
	}

	private boolean isValidExtension(String extension) {
		return extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".png");
	}
}
