# LuckyDoki AI API

## 프로젝트 개요

LuckyDoki AI API는 쇼핑몰 도메인 지식을 기반으로 고도화된 AI 챗봇, 이미지 분석, 음성 처리 기능을 제공하는 Spring Boot 기반의 백엔드 서비스입니다. OpenAI의 최신 LLM 모델과 RAG(Retrieval Augmented Generation) 기술을 활용하여 사용자에게 인공지능 기반의 상품 추천 및 상담 서비스를 제공합니다.

## 주요 기능

### 1. AI 챗봇 (RAG 기반)

- **벡터 검색 기반 컨텍스트 인식**: PGVector를 활용한 벡터 DB에서 사용자 질문과 관련된 상품 정보를 검색하여 정확한 응답 생성
- **스트리밍 응답**: 대화형 UI에 적합한 실시간 응답 스트리밍 처리
- **세션 관리**: MongoDB를 활용한 채팅방 및 메시지 이력 관리
- **유저 인증**: 로그인/비로그인 사용자 모두 지원하는 유연한 세션 관리

### 2. 이미지 분석

- 사용자가 업로드한 이미지를 AI가 분석하여 상세 정보 제공
- 이미지 특성에 맞는 상품 추천 가능
- OpenAI의 gpt-4o 모델 활용한 정확한 이미지 이해

### 3. 음성 처리 (Speech-to-Text)

- 사용자 음성 입력을 텍스트로 변환
- 한국어 음성 인식 지원
- OpenAI의 오디오 API 활용

### 4. 상품 데이터 벡터화

- 쇼핑몰 상품 정보를 자동으로 벡터화하여 DB에 저장
- 정기적인 데이터 업데이트 지원
- 상품 메타데이터 활용한 정확한 검색

### 5. 다중 환경 설정

- Local/Production 환경 구분 설정
- 환경 변수 기반 안전한 API 키 관리
- Docker Compose를 통한 개발 환경 구성

## 기술 스택

### 백엔드

- **Framework**: Spring Boot 3.4.3
- **Language**: Java 17
- **API**: RESTful API, 반응형 프로그래밍(Reactor)

### AI/ML

- **LLM**: OpenAI GPT-4o
- **AI Framework**: Spring AI 1.0.0-M4
- **Vector DB**: PostgreSQL + pgvector 확장
- **Vector Search**: 코사인 유사도 기반 검색

### 데이터베이스

- **Document DB**: MongoDB (채팅 이력 저장)
- **Relational DB**: PostgreSQL (벡터 데이터 저장)

### 인프라/배포

- **Container**: Docker, Docker Compose
- **Configuration**: 환경 변수, .env 파일
- **External APIs**: OpenAI API (Chat, Image, Audio)

## 시스템 아키텍처

### 1. 데이터 계층

- **벡터 저장소**: pgvector를 활용하여 상품 정보 임베딩 저장
- **MongoDB**: 채팅 이력, 세션 정보 저장
- **파일 시스템**: 이미지, 오디오 파일 임시 저장

### 2. 서비스 계층

- **ChatBotService**: 사용자 질문 처리 및 응답 생성
- **ImageAnalysisService**: 이미지 분석 및 정보 추출
- **AudioController**: 음성-텍스트 변환 처리
- **LuckyDokiLoader**: 상품 데이터 벡터화 및 DB 관리

### 3. 컨트롤러 계층

- **API 엔드포인트**: REST API 제공
- **스트리밍 처리**: 비동기 응답 처리
- **파일 업로드**: 멀티파트 요청 처리

## API 엔드포인트

### 챗봇 API

```
GET /api/chatbot/ask?question={질문}&userEmail={이메일}
POST /api/chatbot/room/start?userEmail={이메일}
POST /api/chatbot/room/close?sessionId={세션ID}&userEmail={이메일}
```

### 이미지 분석 API

```
POST /api/ai/image/analyze
```

### 음성 처리 API

```
GET /api/audio/transcribe
POST /api/audio/transcribe
```

## 설치 및 실행 방법

### 필수 요구사항

- JDK 17+
- Docker & Docker Compose
- OpenAI API 키
- MongoDB 계정

### 로컬 개발 환경 설정

1. 저장소 클론

```bash
git clone https://github.com/yourusername/luckydoki-ai-api.git
cd luckydoki-ai-api
```

2. .env 파일 생성

```
# BACKEND_API URL
API_LOCAL_URL=http://localhost:8080
API_PROD_URL=https://api.luckydoki.shop

# openai
OPENAI_MODEL=gpt-4o
OPENAI_API_KEY=your_openai_api_key

# mongoDB
MONGODB_URI=your_mongodb_uri
MONGODB_DATABASE=chatdb
```

3. Docker Compose로 데이터베이스 실행

```bash
docker-compose up -d
```

4. 애플리케이션 빌드 및 실행

```bash
./gradlew bootRun
```

## 개발 과정 및 문제 해결

### 벡터 검색 최적화

- 상품 정보 임베딩시 토큰 분할 문제 해결
- HNSW 인덱스 활용한 검색 성능 향상
- 유사도 임계값 조정으로 관련성 높은 결과 제공

### 실시간 스트리밍 응답

- Reactor와 Flux를 활용한 비동기 스트리밍 구현
- 채팅 메시지 축적 및 저장 로직 구현
- 클라이언트 연결 종료 후에도 메시지 저장 보장

### MongoDB 연결 문제 해결

- SSL/TLS 인증 문제 해결
- 타임아웃 설정 및 연결 풀 최적화
- 네트워크 보안 설정 최적화

## 향후 개선 계획

1. **AI 모델 다변화**:

   - 로컬 모델 활용 옵션 추가
   - 도메인 특화 파인튜닝 모델 적용

2. **성능 최적화**:

   - 캐싱 레이어 추가
   - 대화 컨텍스트 최적화

3. **기능 확장**:
   - 이미지 생성 기능 추가
   - 다국어 지원 강화
