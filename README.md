# coliving-platform

ibm 05기 3팀 Cloud Native 기반 공유 주거(Co-Living) 플랫폼 개발

## 개발 환경

| 구분 | 기술 스택 |
|------|-----------|
| RDBMS | PostgreSQL |
| NoSQL | MongoDB, Redis |
| 서비스 API | Spring Boot 4.1.1 + Java 21<br>GraphQL & RESTful API 설계 및 구현 |
| API 게이트웨이 | Spring Cloud Gateway + Spring Security (Gateway용 reactive) |
| 서비스 간 통신 | gRPC (동기), Kafka (비동기) |
| Frontend | React (Node.js v22.23.3) + Vite |
| CI/CD 파이프라인 | ArgoCD |
| 컨테이너화 및 배포 | Docker + Kubernetes |
| 컨테이너 오케스트레이션 | OpenShift |
| 배포 모니터링 시스템 | Prometheus + Grafana |
| Git 저장소 | Github |