package com.example.backend.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
	String uploadFile(MultipartFile file);
	void deleteFile(String fileUrl);
}
