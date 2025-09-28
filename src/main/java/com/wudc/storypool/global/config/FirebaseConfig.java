package com.wudc.storypool.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account-json}")
    private String serviceAccountJson;

    @Value("${firebase.project-id}")
    private String projectId;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                // 서비스 계정 JSON이 올바른 형태인지 확인
                if (serviceAccountJson == null || serviceAccountJson.trim().isEmpty()) {
                    log.error("FIREBASE_SERVICE_ACCOUNT_JSON environment variable is not set or empty");
                    log.error("Please download the Firebase service account key JSON file from Firebase Console:");
                    log.error("1. Go to Firebase Console → Project Settings → Service accounts");
                    log.error("2. Click 'Generate new private key'");
                    log.error("3. Download the JSON file and set its content to FIREBASE_SERVICE_ACCOUNT_JSON");
                    throw new IllegalStateException("Firebase service account JSON is required but not provided");
                }
                
                if (!serviceAccountJson.contains("\"type\":\"service_account\"")) {
                    log.error("FIREBASE_SERVICE_ACCOUNT_JSON contains invalid JSON format");
                    log.error("Current JSON appears to be a client configuration (google-services.json) instead of service account key");
                    log.error("Please download the correct Firebase Admin SDK service account key JSON file:");
                    log.error("1. Go to Firebase Console → Project Settings → Service accounts");
                    log.error("2. Click 'Generate new private key' (NOT the google-services.json)");
                    log.error("3. Download the JSON file and set its content to FIREBASE_SERVICE_ACCOUNT_JSON");
                    throw new IllegalStateException("Invalid Firebase service account JSON format - must be service account key, not client config");
                }

                GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new ByteArrayInputStream(serviceAccountJson.getBytes()));

                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("Firebase App initialized successfully with project ID: {}", projectId);
                return app;
            } catch (IOException e) {
                log.error("Failed to initialize Firebase App due to invalid service account JSON: {}", e.getMessage());
                log.error("Please verify FIREBASE_SERVICE_ACCOUNT_JSON contains valid service account key JSON:");
                log.error("1. The JSON should contain 'type': 'service_account'");
                log.error("2. The JSON should contain 'private_key', 'client_email', etc.");
                log.error("3. Download from Firebase Console → Project Settings → Service accounts → Generate new private key");
                throw new IllegalStateException("Invalid Firebase service account JSON", e);
            } catch (Exception e) {
                log.error("Unexpected error during Firebase initialization: {}", e.getMessage());
                log.error("Please check FIREBASE_SERVICE_ACCOUNT_JSON environment variable contains valid service account key JSON");
                throw new IllegalStateException("Firebase initialization failed", e);
            }
        } else {
            log.info("Firebase App already initialized");
            return FirebaseApp.getInstance();
        }
    }
}