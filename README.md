# 🧑‍🍳⏱️🧾 Wait4Eat

> **Wait에서 Eat까지, 스마트하게 줄 서는 법**

---

## 🍽️ 프로젝트 소개

**Wait4Eat**은 인기 맛집의 예약과 대기를 앱으로 간편하게 처리할 수 있는 원격 웨이팅 서비스입니다.  
줄 서는 번거로움 없이, 예약금으로 노쇼 걱정 없이, 실시간 알림으로 놓치지 않고!

---

## 🎯 핵심 기능

- **예약금 기반 웨이팅 등록**
- **실시간 호출 및 자동 알림**
- **가게 검색 및 쿠폰 마케팅**
- **운영자용 대시보드 제공**

---

## 🌟 차별점 및 기대 효과

- **노쇼 최소화** : 예약금 제도 도입
- **운영자 편의성** : 실시간 알림 + 자동화된 관리 시스템
- **데이터 분석** : 매출 및 인기 지표 분석 가능한 대시보드
- **커뮤니티 관리** : 리뷰 필터링으로 건강한 이용 환경 제공

---

## ⚙️ System Architecture

### ☁️ Cloud Architecture
![wait4eat 아키텍처 (1)](https://github.com/user-attachments/assets/6f19fa3b-c0e9-40df-bfc2-d2d632d7d97c)

---

## 🗃️ ERD

[DBDiagram 보기](https://dbdiagram.io/e/67e9f4744f7afba184bfb6b1/68132ffc1ca52373f5137e26)

---

## 📌 주요 기능

- **사용자 관리** : 회원가입, 로그인
- **가게 관리** : 가게 등록, 이미지 등록, 검색
- **쿠폰 기능** : 이벤트 생성, 발급 및 관리
- **웨이팅 시스템** : 웨이팅 등록/취소/호출
- **결제 기능** : 예약금 결제 및 환불
- **알림 시스템** : 웨이팅 호출, 쿠폰 이벤트, 알림 내역 조회
- **리뷰 기능** : 리뷰 작성, 조회, 혐오 표현 필터링
- **운영자 대시보드** :
  - 유저 수 및 로그인 수
  - 가게 수 및 신규 등록 수
  - 총 매출, 인기 가게 TOP 10
  - 가게별 매출 순위

---

## 🛠 사용 기술

### 🖥 Backend
- [Java](https://www.notion.so/Java-00501ae474254ca99cbb83374652ba51?pvs=21)
- [Spring Boot](https://www.notion.so/Spring-Boot-159b0d0b5aca4cea8af0678b0b9a89d2?pvs=21)
- [Spring Data JPA](https://www.notion.so/Spring-Data-JPA-129b3376f3a5471ea714346985b1cc9d?pvs=21)
- [Spring Batch](https://www.notion.so/Spring-Batch-1e92dc3ef51480ea81d5efdd77062f8d?pvs=21)
- [SSE](https://www.notion.so/SSE-3cc01593c66143d19c574c06ed3b3562?pvs=21)
- [Gradle](https://www.notion.so/Gradle-224e8da36beb4cbbb98d113f776b599f?pvs=21)

### ☁ Infra & CI/CD
- [Docker](https://www.notion.so/Docker-8697ac1cec864a8cbfac06c9fd0d32ad?pvs=21)
- [Github Actions](https://www.notion.so/Github-Actions-08a362afa3524f999513791bdc3e37e4?pvs=21)
- [Amazon EC2](https://www.notion.so/Amazon-EC2-2a452e22e5da47269d682705e3a69cce?pvs=21)
- [Amazon S3](https://www.notion.so/Amazon-S3-1e92dc3ef51480a2ae36e83459894689?pvs=21)
- [Amazon Route53](https://www.notion.so/Amazon-Route53-1e92dc3ef51480528a88c0309be4abe4?pvs=21)
- [Amazon SQS](https://www.notion.so/Amazon-SQS-1e92dc3ef51480199e8ec0bc0a3ba421?pvs=21)
- [Amazon RDS](https://www.notion.so/Amazon-RDS-1e92dc3ef514806392caf3413c41c360?pvs=21)
- [Elastic Load Balancer](https://www.notion.so/Elastic-Load-Balancer-1e92dc3ef514809a9c75c297ad76dd0c?pvs=21)
- [Amazon ECR](https://www.notion.so/Amazon-ECR-1e92dc3ef514804ea544c98944f3fe0f?pvs=21)

### 🗄 Database
- [MySQL](https://www.notion.so/MySQL-983ae933fe1b42b2abdffd42398ae768?pvs=21)
- [Redis](https://www.notion.so/Redis-ee50ac5e11194aa0b2adb5e03b71d5c6?pvs=21)
- [QueryDSL](https://www.notion.so/QueryDSL-87b79acab5d148948570dd5f9d05b351?pvs=21)

### 🛠 Tools
- [IntelliJ](https://www.notion.so/IntelliJ-1e92dc3ef51481fea8eddcbd79daceb4?pvs=21)
- [Git / Github](https://www.notion.so/Github-1e92dc3ef51481938689fc6f3315cbd2?pvs=21)
- [Slack](https://www.notion.so/Slack-9162d2b7aa624b0686c11423040ab36b?pvs=21)
- [Postman](https://www.notion.so/Postman-1e92dc3ef514815a94bdccd83c0f4854?pvs=21)
- [Elastic Search](https://www.notion.so/Elastic-Search-1e92dc3ef51480ae9484c3369a9d61a4?pvs=21)
- [Toss Payments](https://www.notion.so/Toss-1e92dc3ef51480c99740f316b2dc6c54?pvs=21)
- [OpenAI](https://www.notion.so/OpenAI-1e92dc3ef51480569ad9f78a30d6fe97?pvs=21)

### 🔐 Security
- [Spring Security](https://www.notion.so/Spring-Security-1e92dc3ef514805484a9dec0ea10be19?pvs=21)
- [JWT](https://www.notion.so/JWT-1e92dc3ef51480788d35f3188df0fbfd?pvs=21)

---

## 📌 기술적 의사결정

[기술적 의사결정 문서 보기](https://www.notion.so/1e52dc3ef5148073b5aefce3d9fb8e31?pvs=21)

---

## 🚨 트러블 슈팅

[트러블슈팅 문서 보기](https://www.notion.so/1e52dc3ef5148043809afeca3b9cb7df?pvs=21)

---

## 👥 팀원 소개

|name | tasks   |
|-----------------|-----------------|
| 오수빈   |<ul><li>알림 시스템 설계 및 구현</li><li>결제 시스템 설계 및 구현</li><li>가게 이미지 업로드 로직 구현</li></ul>     |
| 박은지   |<ul><li>쿠폰이벤트 & 쿠폰 설계 및 구현</li><li>CICD, 인프라 구축</li></ul> |
| 이희수   |<ul><li>회원가입 및 로그인 기능 구현</li><li>가게 도메인 설계 및 구현</li><li>가게 검색 기능 구현</li></ul>  |
| 윤예진   |<ul><li>웨이팅 시스템 설계 및 구현</li></ul>    |
| 이현수   |<ul><li>대시보드 기능 설계 및 구현</li><li>리뷰 기능 설계 및 구현</li></ul> |

---

## 📝 협업 & 기록

- GitHub: [https://github.com/Wait4Eat/wait4eat](https://github.com/Wait4Eat/wait4eat)



