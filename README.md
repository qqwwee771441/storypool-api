
---

````markdown
# Storypool

Storypool은 Docker Compose 기반으로 실행되는 웹 애플리케이션입니다.  
MySQL, Redis, 애플리케이션 서버가 함께 실행되며 기본적인 API 기능을 바로 사용할 수 있습니다.  

⚠️ 단, 이메일 발송, AWS S3, Firebase 관련 기능은 `.env`에 Secret 값이 비워져 있어 제한됩니다.  

---

## 🚀 실행 방법

사전에 **Docker**와 **Docker Compose**가 설치되어 있어야 합니다.

### 1. 이미지 다운로드
```bash
docker-compose pull
```

### 2. 컨테이너 실행

```bash
docker compose up
```

### 3. 접속

애플리케이션은 아래 주소에서 접근할 수 있습니다:

* **App**: [http://localhost:8080](http://localhost:8080)
* **MySQL**: localhost:3333

  * root 계정: `root / 1234`
  * 일반 계정: `storypool / 1234`
* **Redis**: localhost:6380 (password: `1234`)

---

## ⚠️ 제한 사항

현재 저장소의 `.env`에는 일부 Secret 값이 설정되지 않았습니다.
따라서 아래 기능은 동작하지 않습니다:

* **이메일 발송 (SMTP 연동)**
* **AWS S3 업로드 / Presigned URL 생성**
* **Firebase 인증 / 푸시 알림**

DB, Redis, JWT 기반 로그인/인증, 기본 API는 정상 동작합니다.

---

## 🛠️ 서비스 구성

* `app`: 메인 애플리케이션 (포트 8080)
* `storypool_mysql`: MySQL 8.0 데이터베이스 (포트 3333)
* `storypool_redis`: Redis 7 (포트 6380)

---

## 🗄️ 데이터 영속화

* MySQL 데이터 → `mysql_data` 볼륨
* Redis 데이터 → `redis_data` 볼륨

---

## 🔧 네트워크

모든 컨테이너는 `storypool-network` (bridge) 네트워크에서 통신합니다.

---

## 📂 환경 변수 예시 (.env)

```env
REDIS_PORT=6379
REDIS_HOST=storypool_redis
REDIS_PASSWORD=1234

DB_URL=jdbc:mysql://storypool_db:3306/storypool
DB_USERNAME=storypool
DB_PASSWORD=1234

JWT_ACCESS_SECRET=...
JWT_REFRESH_SECRET=...
JWT_TOKEN_EXPIRE=30
JWT_REFRESH_TOKEN_EXPIRE=20160
JWT_IS_SECURE=false
JWT_SAME_SITE=Lax

# 이메일 (제한됨)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=

# AWS S3 (제한됨)
AWS_S3_REGION=ap-northeast-2
AWS_S3_BUCKET_NAME=wudc-storypool
AWS_S3_ACCESS_KEY=
AWS_S3_SECRET_KEY=

# Firebase (제한됨)
FIREBASE_PROJECT_ID=wudc-storypool
FIREBASE_SERVICE_ACCOUNT_JSON=
```

---

## ✅ 요약

* `docker compose pull && docker compose up` → 바로 실행 가능
* 기본 API, DB, Redis는 정상 작동
* 이메일 / AWS S3 / Firebase 기능은 Secret 미설정으로 제한됨

---
