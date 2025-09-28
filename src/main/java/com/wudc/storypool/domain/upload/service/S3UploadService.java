package com.wudc.storypool.domain.upload.service;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.domain.user.entity.User;
import com.wudc.storypool.domain.user.repository.UserRepository;
import com.wudc.storypool.global.config.S3Config;
import de.huxhorn.sulky.ulid.ULID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;
import org.springframework.core.io.ClassPathResource;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3UploadService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Config s3Config;
    private final UserRepository userRepository;

    public PresignedUrlData generatePresignedUrl(String userId, String fileName, String contentType) {
        try {
            // Generate unique object key
            String objectKey = generateObjectKey(userId, fileName);
            
            // Validate content type
            validateContentType(contentType);
            
            // Create put object request
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            // Create presigned request
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(s3Config.getPresignedUrlExpiration()))
                    .putObjectRequest(putObjectRequest)
                    .build();

            // Generate presigned URL
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            
            String presignedUrl = presignedRequest.url().toString();
            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                    s3Config.getBucketName(), s3Config.getRegion(), objectKey);

            log.info("Generated presigned URL for user: {} with object key: {}", userId, objectKey);
            
            return new PresignedUrlData(presignedUrl, fileUrl, objectKey, s3Config.getPresignedUrlExpiration());
            
        } catch (S3Exception e) {
            log.error("Failed to generate presigned URL for user: {}, error: {}", userId, e.getMessage());
            throw new BaseException(ErrorCode.S3_UPLOAD_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error generating presigned URL for user: {}, error: {}", userId, e.getMessage());
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    private String generateObjectKey(String userId, String fileName) {
        String fileExtension = getFileExtension(fileName);
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        return String.format("uploads/%s/%s", userId, uniqueFileName);
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }

    private void validateContentType(String contentType) {
        if (!isValidImageContentType(contentType)) {
            log.warn("Invalid content type provided: {}", contentType);
            throw new BaseException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private boolean isValidImageContentType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp")
        );
    }

    private String extractObjectKeyFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        
        // Extract object key from S3 URL
        // URL format: https://bucket.s3.region.amazonaws.com/uploads/userId/filename
        try {
            String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", 
                    s3Config.getBucketName(), s3Config.getRegion());
            
            if (imageUrl.startsWith(baseUrl)) {
                return imageUrl.substring(baseUrl.length());
            }
            
            // If it doesn't match expected format, return null
            log.warn("Profile image URL doesn't match expected S3 format: {}", imageUrl);
            return null;
            
        } catch (Exception e) {
            log.warn("Failed to extract object key from URL: {}, error: {}", imageUrl, e.getMessage());
            return null;
        }
    }

    public String uploadTestImageToS3(String fileName) {
        try {
            // Load test image from resources
            ClassPathResource resource = new ClassPathResource("static/test-images/" + fileName);
            if (!resource.exists()) {
                log.error("Test image not found: {}", fileName);
                throw new BaseException(ErrorCode.FILE_NOT_FOUND);
            }

            // Generate object key for test images
            String objectKey = new ULID().nextULID() + ": " + fileName;
            
            // Get file extension and content type
            String contentType = getContentTypeFromFileName(fileName);
            
            try (InputStream inputStream = resource.getInputStream()) {
                // Upload to S3
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(s3Config.getBucketName())
                        .key(objectKey)
                        .contentType(contentType)
                        .build();

                s3Client.putObject(putObjectRequest, 
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, resource.contentLength()));
                
                // Generate public URL
                String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                        s3Config.getBucketName(), s3Config.getRegion(), objectKey);
                
                log.info("Successfully uploaded test image: {} to S3 with URL: {}", fileName, fileUrl);
                return fileUrl;
                
            }
        } catch (IOException e) {
            log.error("Failed to read test image file: {}, error: {}", fileName, e.getMessage());
            throw new BaseException(ErrorCode.S3_UPLOAD_ERROR);
        } catch (S3Exception e) {
            log.error("Failed to upload test image to S3: {}, error: {}", fileName, e.getMessage());
            throw new BaseException(ErrorCode.S3_UPLOAD_ERROR);
        } catch (Exception e) {
            if (e instanceof BaseException) throw e;
            log.error("Unexpected error uploading test image: {}, error: {}", fileName, e.getMessage());
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteS3Object(String objectKey) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted S3 object: {}", objectKey);
            
        } catch (S3Exception e) {
            log.error("Failed to delete S3 object: {}, error: {}", objectKey, e.getMessage());
            throw new BaseException(ErrorCode.S3_DELETE_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error deleting S3 object: {}, error: {}", objectKey, e.getMessage());
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String getContentTypeFromFileName(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerFileName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFileName.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    public void deleteByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            log.warn("Cannot delete S3 object: imageUrl is null or empty");
            throw new BaseException(ErrorCode.FILE_NOT_FOUND);
        }

        String objectKey = extractObjectKeyFromUrl(imageUrl);
        if (objectKey == null) {
            log.warn("Cannot extract object key from URL: {}", imageUrl);
            throw new BaseException(ErrorCode.FILE_NOT_FOUND);
        }

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .build();

            s3Client.headObject(headObjectRequest);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted S3 object by URL: {} (objectKey: {})", imageUrl, objectKey);
            
        } catch (NoSuchKeyException e) {
            log.warn("S3 object not found for URL: {} (objectKey: {})", imageUrl, objectKey);
            throw new BaseException(ErrorCode.FILE_NOT_FOUND);
        } catch (S3Exception e) {
            log.error("Failed to delete S3 object by URL: {} (objectKey: {}), error: {}", imageUrl, objectKey, e.getMessage());
            throw new BaseException(ErrorCode.S3_DELETE_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error deleting S3 object by URL: {} (objectKey: {}), error: {}", imageUrl, objectKey, e.getMessage());
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public record PresignedUrlData(
            String presignedUrl,
            String fileUrl,
            String objectKey,
            int expirationTime
    ) {}
}