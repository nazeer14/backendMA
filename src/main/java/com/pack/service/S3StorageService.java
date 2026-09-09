package com.pack.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3StorageService {

    String upload(MultipartFile file,String role);

    byte[] download(String key);

    void delete(String key);
}
