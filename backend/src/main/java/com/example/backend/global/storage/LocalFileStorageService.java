package com.example.backend.global.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final String UPLOAD_DIR = "uploads/"; // 로컬 저장 경로

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalFilename;
            String filePath = Paths.get(UPLOAD_DIR, fileName).toString();

            File dest = new File(filePath);
            file.transferTo(dest);

            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        String fileName = fileUrl.replace("/uploads/", "");
        String filePath = Paths.get(UPLOAD_DIR, fileName).toString();
        File file = new File(filePath);

        if (file.exists()) {
            file.delete();
        }
    }
}
