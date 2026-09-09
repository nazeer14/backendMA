package com.pack.service.impl;

import com.pack.cofig.AwsProperties;
import com.pack.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageServiceImpl
        implements S3StorageService {

    private final S3Client s3Client;
    private final AwsProperties properties;

    @Override
    public String upload(MultipartFile file,String role) {

        try {

            String extension =
                    FilenameUtils.getExtension(
                            file.getOriginalFilename()
                    );

            String key =
                            role
                            +"/"
                            + UUID.randomUUID()
                            + "."
                            + extension;

            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(
                                    properties
                                            .getS3()
                                            .getBucketName()
                            )
                            .key(key)
                            .contentType(
                                    file.getContentType()
                            )
                            .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(
                            file.getBytes()
                    )
            );

            return key;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to upload image",
                    e
            );
        }
    }

    @Override
    public byte[] download(String key) {

        try {

            GetObjectRequest request =
                    GetObjectRequest.builder()
                            .bucket(
                                    properties
                                            .getS3()
                                            .getBucketName()
                            )
                            .key(key)
                            .build();

            ResponseBytes<GetObjectResponse> bytes =
                    s3Client.getObjectAsBytes(
                            request
                    );

            return bytes.asByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to retrieve image",
                    e
            );
        }
    }

    @Override
    public void delete(String key) {

        DeleteObjectRequest request =
                DeleteObjectRequest.builder()
                        .bucket(
                                properties
                                        .getS3()
                                        .getBucketName()
                        )
                        .key(key)
                        .build();

        s3Client.deleteObject(request);
    }
}
