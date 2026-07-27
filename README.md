# PortOne 테스트 결제 연동

테스트 결제가 정상적으로 동작하는지 확인하기 위한 샘플 프로젝트입니다.

- Frontend: Next.js (App Router) + TypeScript + Tailwind CSS + shadcn/ui
- Backend: Spring Boot 3 + Java 17 + Gradle

## 구조

```text
PaymentApiTest/
├── backend/          # Spring Boot REST API
└── frontend/         # Next.js 결제 테스트 UI
```

### Backend 계층

- `controller` — 요청/응답
- `service` — 비즈니스 로직
- `repository` — 결제 세션 저장(현재 인메모리, 이후 DB 교체 가능)
- `client` — PortOne REST API 연동
- `dto` / `domain/entity` / `exception` / `config`

### Frontend 계층

- `components/payment` — UI
- `hooks` — 결제 흐름 오케스트레이션
- `services` — 유스케이스
- `apis` — HTTP 호출
- `types` / `utils`

컴포넌트 내부에서 `fetch`를 직접 호출하지 않습니다.

## 사전 준비

1. [PortOne 관리자 콘솔](https://admin.portone.io/) **V2 연동 정보**에서 테스트 채널을 생성합니다.
2. 아래 값을 확인합니다.
   - Store ID (`store-...` 형식, V1의 `iamporttest_3` 같은 코드는 사용 불가)
   - Channel Key (`channel-key-...`) — 일반 결제용
   - Billing Channel Key (`channel-key-...`) — **정기결제/빌링키용** (토스는 정기결제 MID 채널)
   - V2 API Secret

## 시크릿 관리 (중요)

운영 키는 **코드/이미지/Git에 넣지 않습니다.**  
개발자는 보통 운영 키를 직접 보지 않고, AWS·Docker·GitHub Secrets·Jenkins 등이 런타임에 주입합니다.

```text
로컬 개발  → backend/.env (개인 PC 전용, gitignore)
CI 테스트  → GitHub Actions env (더미 값)
운영 배포  → AWS Secrets Manager / Docker env / GitHub Secrets / Jenkins Credentials
```

### 로컬

```bash
cd backend
cp .env.example .env
# .env에 테스트용 키만 입력 (운영 키 금지)
./gradlew bootRun
```

### Docker

호스트/CI에 환경변수를 준비한 뒤:

```bash
export PORTONE_API_SECRET=...
export PORTONE_STORE_ID=...
export PORTONE_CHANNEL_KEY=...
export PORTONE_BILLING_CHANNEL_KEY=...
docker compose up --build
```

`docker-compose.yml`에는 시크릿 값을 하드코딩하지 않습니다.

### GitHub Secrets

Repository Settings → Secrets and variables → Actions 에 등록:

- `PORTONE_API_SECRET`
- `PORTONE_STORE_ID`
- `PORTONE_CHANNEL_KEY`
- `PORTONE_BILLING_CHANNEL_KEY`
- `CORS_ALLOWED_ORIGINS`

배포 예시는 `.github/workflows/deploy.example.yml` 참고.

### 프로파일

| 프로파일 | dotenv(.env) | 채널 |
|---------|--------------|------|
| `local` | 허용 | TEST |
| `test`  | 비활성 | TEST |
| `prod`  | 비활성 | LIVE |

기동 시 `SecretEnvironmentValidator`가 필수 환경변수를 검사합니다.

### Frontend (`frontend/.env.local`)

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

## 실행 방법

### 1) Backend

```bash
cd backend
./gradlew bootRun
```

Windows:

```powershell
cd backend
.\gradlew.bat bootRun
```

### 2) Frontend

```bash
cd frontend
npm install
npm run dev
```

브라우저에서 [http://localhost:3000](http://localhost:3000) 접속

## API

### 일회성 결제

- `GET /payment/ready` — 테스트 결제에 필요한 정보 반환
- `POST /payment/verify` — PortOne 결제 검증 (`paymentId`)

### 빌링키(등록 결제수단)

- `GET /billing-keys/ready` — 빌링키 발급용 storeId/channelKey/customer
- `POST /billing-keys` — 발급된 billingKey 검증 후 등록 (첫 카드는 기본 결제수단)
- `GET /billing-keys` — 등록된 결제수단 목록 (기본 여부 포함)
- `POST /billing-keys/default` — 기본 결제수단 변경
- `GET /billing-keys/default` — 기본 결제수단 조회
- `POST /payment/billing` — 선택한 billingKey로 서버 결제 + 검증

## 결제 흐름

### 일회성 테스트 결제

1. 페이지 진입 시 `GET /payment/ready`
2. **테스트 결제** 버튼 클릭
3. PortOne 테스트 결제창 호출
4. 결제 완료 후 `POST /payment/verify`
5. 성공/실패 결과 표시

### 등록 결제수단으로 결제

1. **결제수단 등록** → PortOne 빌링키 발급 창 (여러 장 등록 가능)
2. `POST /billing-keys`로 서버에 등록 → 목록 표시 (첫 카드는 자동 기본)
3. **기본으로 설정**으로 기본 결제수단 변경
4. **기본 수단으로 결제** → `POST /payment/billing` (결제창 없음)

> 빌링키 목록/기본수단은 인메모리 저장이라 서버 재시작 시 초기됩니다.

## 의도적으로 제외한 기능

주문 생성, DB 저장, 환불, 쿠폰, 포인트, 빌링키 삭제 UI는 구현하지 않았습니다.  
다만 `PaymentSessionRepository` / `BillingKeyRepository` / `PortOnePaymentClient` 추상화로 실제 결제 도메인으로 확장할 수 있게 구성했습니다.
