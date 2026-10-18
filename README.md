# coliving-platform

ibm 05기 3팀 Cloud Native 기반 공유 주거(Co-Living) 플랫폼 개발

로컬 설치 순서는 [설치 가이드](docs/설치-가이드.md)를 따릅니다.

## 개발 환경

| 구분 | 기술 스택 |
|------|-----------|
| 언어 / 런타임 | Java 21, Node.js 22 |
| 서비스 API | Spring Boot 4.1.1, Spring MVC |
| Spring Cloud | 2025.1.3 - Eureka, Config Server, Gateway Server WebFlux |
| 보안 | Spring Security 7 |
| Config Server | Spring Cloud Bus + RabbitMQ 4.3.6 |
| RDBMS | PostgreSQL 17 |
| NoSQL | MongoDB 7, Redis |
| 서비스 간 통신 | gRPC (동기), Kafka (비동기, Zookeeper) |
| Frontend | React + Vite + React Router + Tailwind CSS 4 |
| CI/CD 파이프라인 | ArgoCD |
| 컨테이너화 및 배포 | Docker + Kubernetes |
| 컨테이너 오케스트레이션 | OpenShift |
| 배포 모니터링 시스템 | Prometheus + Grafana |
| Git 저장소 | Github |