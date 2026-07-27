# PortOne 결제 연동 요청 스크립트 (Cursor용)

아래 블록 전체를 새 Cursor 채팅에 붙여넣으세요.
필요하면 `[수정]` 표시된 부분만 바꿔서 쓰면 됩니다.

---

## 역할

당신은 시니어 풀스택 엔지니어입니다.
아래 요구사항대로 PortOne(포트원) V2 테스트 결제 연동을 구현하세요.
목표는 "테스트 결제가 정상 동작하는지 확인"이며, 실제 서비스로 확장 가능한 구조를 유지하세요.

## 기술스택

### Frontend
- Next.js (App Router)
- TypeScript
- React
- TailwindCSS
- shadcn/ui

### Backend
- Spring Boot 3
- Java 17
- Gradle
- REST API

## 반드시 지킬 원칙

### 공통
- SOLID, OOP, 책임 분리, DRY, KISS, Clean Code
- 테스트/운영 환경 분리
- 시크릿은 코드에 하드코딩 금지 → 환경변수 주입 (`.env`는 gitignore)
- `any` 금지, 매직넘버 금지, 의미 있는 이름
- 함수는 하나의 역할만
- 의존성 주입
- 주석은 "왜" 중심으로만

### Frontend
폴더 구조를 역할별로 분리:

```text
/app
/components/payment
/hooks
/services
/apis
/types
/utils
```

- 컴포넌트 안에서 `fetch` 직접 호출 금지
- API 호출은 `apis` / `services` 계층에서만
- PortOne SDK는 Hook 또는 Service로 분리
- 비즈니스 로직을 JSX에 넣지 말 것
- interface/type 적극 사용
- 로딩/에러/성공/취소 상태 구현
- 결제 버튼은 재사용 컴포넌트

### Backend
계층 분리:

```text
Controller / Service / Repository / DTO / Entity / Config / Exception / Client
```

- Controller는 요청/응답만
- Service에 비즈니스 로직
- DTO 사용
- Global Exception Handler
- 공통 `ApiResponse` 사용
- PortOne API Key는 환경변수로 관리
- Repository는 인터페이스 + 구현체 (지금은 인메모리, 나중에 DB 교체 가능)
- 외부 PortOne 연동은 Client 인터페이스로 추상화 (DIP)

## 구현 기능

1. `GET /payment/ready` — 테스트 결제에 필요한 정보 반환
2. `POST /payment/verify` — PortOne 결제 검증
3. Frontend
   - "테스트 결제" 버튼
   - 클릭 → PortOne 테스트 결제창
   - 완료 → backend verify
   - 성공/실패 출력

### UI
간단한 테스트 페이지 `PaymentTestPage`
- 상품명
- 금액
- 테스트 결제 버튼
- 결제 결과

디자인: shadcn/ui

## 범위 (하지 말 것)

- 주문 생성, DB 저장, 환불, 쿠폰, 포인트 구현 금지
- 하지만 나중에 붙일 수 있게 확장 가능한 구조로 작성

## PortOne V2 주의사항 (중요)

- Store ID는 `store-...` 형식 (V1의 `iamporttest_3` 같은 PG MID 사용 금지)
- Channel Key는 `channel-key-...`
- API Secret은 PortOne 콘솔의 **V2 API Secret** 사용
  - 토스 `test_sk_...` 같은 PG Secret Key 사용 금지
- `windowType`을 `POPUP`으로 강제하지 말 것 (토스페이먼츠 PC에서 실패함)
- 다수 PG는 `customer.phoneNumber` 등 구매자 정보 필요
- 테스트 채널은 `TEST`, 운영은 `LIVE`만 허용
- verify는 PortOne 단건 조회로 상태(`PAID`)/금액/주문명/통화/채널 검증

## 시크릿 관리

```text
로컬  → backend/.env (gitignore, 개발자 PC 전용)
CI    → 더미 env
운영  → AWS / Docker / GitHub Secrets / Jenkins 환경변수 주입
```

- 코드/이미지/Git에 실제 키 넣지 말 것
- `.env.example`만 커밋
- `prod` 프로파일에서는 dotenv 비활성화
- 기동 시 필수 환경변수 검증

## 결제 흐름

1. 페이지 진입 → `GET /payment/ready`
2. 테스트 결제 클릭
3. PortOne 결제창 호출
4. 완료 후 `POST /payment/verify`
5. 성공/실패 UI 표시

예외가 나도 UI가 LOADING에 고착되지 않게 try/catch 처리할 것.

## 산출물

```text
PaymentApiTest/
├── backend/     # Spring Boot
├── frontend/    # Next.js
├── docker-compose.yml
├── .github/workflows/
└── README.md
```

README에는 실행 방법 + 시크릿 주입 방법 포함.

## [수정] 이번 요청에서 추가로 원하는 것

- (예: 환불 API 추가 / 웹훅 추가 / 주문 DB 연동 / UI 개선 등)
- 

---

위 기준을 守り 구현을 시작하세요.
이미 프로젝트가 있으면 기존 구조를 파악한 뒤, 깨지 않고 확장하세요.
처음부터면 위 구조로 생성하세요.
