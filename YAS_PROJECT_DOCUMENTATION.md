# YAS: Yet Another Shop - Tài liệu chi tiết dự án

## 📋 Mục lục

1. [Giới thiệu](#1-giới-thiệu)
2. [Kiến trúc tổng quan](#2-kiến-trúc-tổng-quan)
3. [Công nghệ và Framework](#3-công-nghệ-và-framework)
4. [Cấu trúc thư mục dự án](#4-cấu-trúc-thư-mục-dự-án)
5. [Chi tiết các Microservices](#5-chi-tiết-các-microservices)
6. [Frontend Applications](#6-frontend-applications)
7. [Infrastructure & DevOps](#7-infrastructure--devops)
8. [Observability Stack](#8-observability-stack)
9. [Data & Messaging](#9-data--messaging)
10. [Hướng dẫn cài đặt](#10-hướng-dẫn-cài-đặt)
11. [API Documentation](#11-api-documentation)

---

## 1. Giới thiệu

**YAS (Yet Another Shop)** là một dự án mã nguồn mở được phát triển bởi NashTech Garage nhằm mục đích thực hành và học tập việc xây dựng ứng dụng microservices hoàn chỉnh bằng Java. Đây là một hệ thống thương mại điện tử (E-commerce) với đầy đủ các tính năng cần thiết.

### Đặc điểm chính:
- **Monorepo Structure**: Tất cả microservices được quản lý trong một repository duy nhất
- **Domain-Driven Design**: Các service được thiết kế theo từng nghiệp vụ cụ thể
- **Production-Ready**: Có đầy đủ CI/CD, monitoring, logging, tracing
- **Cloud-Native**: Hỗ trợ triển khai trên Kubernetes

---

## 2. Kiến trúc tổng quan

### 2.1 Kiến trúc Microservices

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              NGINX (Reverse Proxy)                       │
│                    api.yas.local / storefront / backoffice               │
└─────────────────────────────────────────────────────────────────────────┘
                                      │
          ┌───────────────────────────┼───────────────────────────┐
          ▼                           ▼                           ▼
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│  Storefront UI  │         │  Backoffice UI  │         │  Swagger UI     │
│   (Next.js)     │         │   (Next.js)     │         │   (API Docs)    │
└─────────────────┘         └─────────────────┘         └─────────────────┘
          │                           │
          ▼                           ▼
┌─────────────────┐         ┌─────────────────┐
│ Storefront BFF  │         │ Backoffice BFF  │
│  (Spring Boot)  │         │  (Spring Boot)  │
└─────────────────┘         └─────────────────┘
          │                           │
          └───────────────┬───────────┘
                          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        BACKEND MICROSERVICES                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ Product  │ │  Order   │ │   Cart   │ │ Customer │ │ Inventory│       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │  Media   │ │ Payment  │ │  Rating  │ │   Tax    │ │ Promotion│       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ Location │ │  Search  │ │ Webhook  │ │ Delivery │ │Recomm.   │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
└─────────────────────────────────────────────────────────────────────────┘
          │                           │                         │
          ▼                           ▼                         ▼
┌─────────────────┐         ┌─────────────────┐       ┌─────────────────┐
│   PostgreSQL    │         │     Kafka       │       │  Elasticsearch  │
│   (Database)    │         │   (Messaging)   │       │    (Search)     │
└─────────────────┘         └─────────────────┘       └─────────────────┘
```

### 2.2 Pattern Backend-for-Frontend (BFF)

Dự án sử dụng pattern BFF để:
- **Storefront BFF**: Aggregation layer cho giao diện người dùng cuối
- **Backoffice BFF**: Aggregation layer cho giao diện quản trị

Mỗi BFF đóng vai trò:
- Gateway routing requests đến các microservices
- Xử lý authentication/authorization với Keycloak
- Aggregation dữ liệu từ nhiều services
- Token relay cho các downstream services

---

## 3. Công nghệ và Framework

### 3.1 Backend Technologies

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| Java | 21 | Ngôn ngữ lập trình chính |
| Spring Boot | 4.0.2 | Framework backend |
| Spring Cloud | 2025.1.1 | Cloud-native capabilities |
| Spring Security | OAuth2 | Authentication & Authorization |
| Spring Data JPA | - | Data persistence |
| Hibernate | - | ORM |
| Lombok | 1.18.42 | Giảm boilerplate code |
| MapStruct | 1.6.3 | Object mapping |
| Liquibase | - | Database migration |

### 3.2 Frontend Technologies

| Công nghệ | Mục đích |
|-----------|----------|
| Next.js | React framework cho SSR/SSG |
| TypeScript | Type-safe JavaScript |
| React | UI library |

### 3.3 Infrastructure

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| Docker | - | Containerization |
| Kubernetes | - | Container orchestration |
| Nginx | 1.27.2 | Reverse proxy |
| Keycloak | 26.0.2 | Identity & Access Management |
| PostgreSQL | - | Relational database |
| Kafka | - | Message broker |
| Elasticsearch | 8.6.2 | Full-text search |
| Debezium | - | Change Data Capture (CDC) |

### 3.4 Observability Stack

| Công nghệ | Mục đích |
|-----------|----------|
| OpenTelemetry | Instrumentation chuẩn |
| Grafana | Visualization dashboards |
| Prometheus | Metrics collection |
| Loki | Log aggregation |
| Tempo | Distributed tracing |

### 3.5 Testing & Quality

| Công nghệ | Mục đích |
|-----------|----------|
| JUnit 5 | Unit testing |
| Testcontainers | Integration testing |
| Rest Assured | API testing |
| Instancio | Test data generation |
| SonarCloud | Code quality analysis |
| JaCoCo | Code coverage |
| Checkstyle | Code style enforcement |

---

## 4. Cấu trúc thư mục dự án

```
yas/
├── 📁 automation-ui/          # UI automation tests
│   ├── automation-base/       # Base test framework
│   ├── backoffice/           # Backoffice UI tests
│   └── storefront/           # Storefront UI tests
│
├── 📁 backoffice/            # Admin UI (Next.js)
│   ├── modules/              # Feature modules
│   ├── pages/                # Next.js pages
│   └── common/               # Shared components
│
├── 📁 backoffice-bff/        # Backend-for-Frontend (Admin)
│   └── src/main/java/        # Spring Boot application
│
├── 📁 storefront/            # Customer UI (Next.js)
│   ├── modules/              # Feature modules
│   ├── pages/                # Next.js pages
│   └── common/               # Shared components
│
├── 📁 storefront-bff/        # Backend-for-Frontend (Customer)
│   └── src/main/java/        # Spring Boot application
│
├── 📁 common-library/        # Shared Java library
│   └── src/main/java/
│       └── com/yas/commonlibrary/
│           ├── config/       # Common configurations
│           ├── exception/    # Exception handling
│           ├── kafka/        # Kafka utilities
│           └── viewmodel/    # Shared DTOs
│
├── 📁 [microservice]/        # Each microservice folder
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/java/
│       │   └── com/yas/[service]/
│       │       ├── config/       # Configuration classes
│       │       ├── controller/   # REST controllers
│       │       ├── model/        # Entity classes
│       │       ├── repository/   # Data access layer
│       │       ├── service/      # Business logic
│       │       ├── viewmodel/    # DTOs
│       │       └── validation/   # Custom validators
│       └── test/java/            # Unit tests
│           └── it/               # Integration tests
│
├── 📁 docker/                # Docker configurations
│   ├── grafana/              # Grafana dashboards
│   ├── prometheus/           # Prometheus config
│   ├── loki/                 # Loki config
│   ├── tempo/                # Tempo config
│   └── otel-collector/       # OpenTelemetry collector
│
├── 📁 k8s/                   # Kubernetes deployment
│   ├── charts/               # Helm charts
│   └── deploy/               # Deployment scripts
│
├── 📁 kafka/                 # Kafka configurations
│   └── connects/             # Debezium connectors
│
├── 📁 nginx/                 # Nginx configuration
│   └── templates/            # Nginx server blocks
│
├── 📁 identity/              # Keycloak configuration
│   ├── realm-export.json     # Realm configuration
│   └── themes/               # Custom themes
│
├── 📄 docker-compose.yml     # Core services
├── 📄 docker-compose.search.yml    # Search service
├── 📄 docker-compose.o11y.yml      # Observability stack
├── 📄 pom.xml               # Parent Maven POM
└── 📄 postgres_init.sql     # Database initialization
```

---

## 5. Chi tiết các Microservices

### 5.1 Product Service
**Mục đích**: Quản lý thông tin sản phẩm, danh mục, thương hiệu

**Chức năng chính**:
- CRUD sản phẩm (products)
- Quản lý danh mục (categories)
- Quản lý thương hiệu (brands)
- Quản lý thuộc tính sản phẩm (attributes)
- Quản lý biến thể sản phẩm (variations)
- Export dữ liệu sản phẩm

**Endpoints chính**:
- `GET /backoffice/products` - Danh sách sản phẩm (admin)
- `POST /backoffice/products` - Tạo sản phẩm mới
- `PUT /backoffice/products/{id}` - Cập nhật sản phẩm
- `GET /storefront/products` - Sản phẩm cho storefront

**Database**: `product`

---

### 5.2 Order Service
**Mục đích**: Quản lý đơn hàng và checkout

**Chức năng chính**:
- Tạo đơn hàng từ giỏ hàng
- Quản lý trạng thái đơn hàng
- Lịch sử đơn hàng người dùng
- Export đơn hàng (CSV)
- Tích hợp với Payment service

**Endpoints chính**:
- `POST /storefront/orders` - Tạo đơn hàng
- `GET /storefront/orders/my-orders` - Đơn hàng của user
- `GET /backoffice/orders` - Danh sách đơn hàng (admin)
- `PUT /storefront/orders/status` - Cập nhật trạng thái

**Database**: `order`

---

### 5.3 Cart Service
**Mục đích**: Quản lý giỏ hàng người dùng

**Chức năng chính**:
- Thêm/xóa/cập nhật sản phẩm trong giỏ hàng
- Tính toán tổng giá trị giỏ hàng
- Lưu trữ giỏ hàng theo user

**Database**: `cart`

---

### 5.4 Customer Service
**Mục đích**: Quản lý thông tin khách hàng

**Chức năng chính**:
- Quản lý profile khách hàng
- Lưu trữ địa chỉ giao hàng
- Đồng bộ với Keycloak user

**Database**: `customer`

---

### 5.5 Inventory Service
**Mục đích**: Quản lý tồn kho và warehouse

**Chức năng chính**:
- Quản lý kho hàng (warehouses)
- Theo dõi số lượng tồn kho
- Kiểm tra availability sản phẩm
- Stock management

**Database**: `inventory`

---

### 5.6 Media Service
**Mục đích**: Quản lý file media (hình ảnh, video)

**Chức năng chính**:
- Upload/download media files
- Lưu trữ metadata
- Serve static files

**Database**: `media`

---

### 5.7 Payment Service
**Mục đích**: Xử lý thanh toán đơn hàng

**Chức năng chính**:
- Quản lý payment providers
- Xử lý giao dịch thanh toán
- Payment status tracking

**Database**: `payment`

---

### 5.8 Payment PayPal Service
**Mục đích**: Tích hợp thanh toán PayPal

**Chức năng chính**:
- PayPal checkout integration
- PayPal SDK integration
- Payment callback handling

---

### 5.9 Rating Service
**Mục đích**: Quản lý đánh giá và review sản phẩm

**Chức năng chính**:
- Đánh giá sản phẩm (stars)
- Review sản phẩm (comments)
- Average rating calculation

**Database**: `rating`

---

### 5.10 Location Service
**Mục đích**: Quản lý thông tin địa lý

**Chức năng chính**:
- Quản lý quốc gia (countries)
- Quản lý tỉnh/thành phố (states/provinces)
- Quản lý quận/huyện (districts)
- Quản lý địa chỉ (addresses)

**Database**: `location`

---

### 5.11 Tax Service
**Mục đích**: Quản lý thuế

**Chức năng chính**:
- Quản lý tax classes
- Tính toán thuế theo vùng
- Tax rates configuration

**Database**: `tax`

---

### 5.12 Promotion Service
**Mục đích**: Quản lý khuyến mãi và giảm giá

**Chức năng chính**:
- Quản lý promotion campaigns
- Coupon/voucher management
- Discount calculation
- Promotion rules engine

**Database**: `promotion`

---

### 5.13 Search Service
**Mục đích**: Full-text search sản phẩm

**Chức năng chính**:
- Index sản phẩm vào Elasticsearch
- Full-text search
- Faceted search
- Search suggestions

**Tích hợp**:
- Elasticsearch cho indexing và search
- Kafka consumer để nhận product updates (CDC)

---

### 5.14 Webhook Service
**Mục đích**: Quản lý webhook cho third-party integrations

**Chức năng chính**:
- Webhook registration
- Event dispatching
- Retry mechanism

**Database**: `webhook`

---

### 5.15 Recommendation Service
**Mục đích**: AI-powered product recommendations

**Chức năng chính**:
- Product recommendations
- Vector search
- Personalization

**Database**: `recommendation`

---

### 5.16 Delivery Service
**Mục đích**: Quản lý vận chuyển

**Chức năng chính**:
- Shipping providers management
- Delivery tracking
- Shipping fee calculation

---

### 5.17 Sample Data Service
**Mục đích**: Load sample data cho development/testing

**Chức năng chính**:
- Populate sample products
- Populate sample categories
- Demo data generation

---

## 6. Frontend Applications

### 6.1 Storefront (Customer-facing)
**URL**: `http://storefront/`

**Modules chính**:
| Module | Chức năng |
|--------|-----------|
| `home` | Trang chủ, featured products |
| `catalog` | Danh sách sản phẩm, categories |
| `cart` | Giỏ hàng |
| `order` | Checkout, order history |
| `customer` | Profile người dùng |
| `address` | Quản lý địa chỉ |
| `payment` | Thanh toán |
| `rating` | Đánh giá sản phẩm |
| `search` | Tìm kiếm sản phẩm |
| `promotion` | Khuyến mãi |

### 6.2 Backoffice (Admin Panel)
**URL**: `http://backoffice/`  
**Đăng nhập**: admin/password

**Modules chính**:
| Module | Chức năng |
|--------|-----------|
| `catalog` | Quản lý products, categories, brands |
| `order` | Quản lý đơn hàng |
| `inventory` | Quản lý kho |
| `customer` | Quản lý khách hàng |
| `promotion` | Quản lý khuyến mãi |
| `rating` | Quản lý đánh giá |
| `tax` | Quản lý thuế |
| `location` | Quản lý địa lý |
| `payment` | Quản lý thanh toán |
| `webhook` | Quản lý webhooks |

---

## 7. Infrastructure & DevOps

### 7.1 Docker Compose Files

| File | Mô tả |
|------|-------|
| `docker-compose.yml` | Core services (databases, auth, microservices) |
| `docker-compose.search.yml` | Search service + Elasticsearch |
| `docker-compose.o11y.yml` | Observability stack (Grafana, Prometheus, Loki, Tempo) |

### 7.2 Nginx Configuration
- **Reverse proxy** cho tất cả services
- **Virtual hosts**:
  - `api.yas.local` - API Gateway
  - `storefront` - Customer UI
  - `backoffice` - Admin UI
  - `pgadmin.yas.local` - Database admin
  - `identity` - Keycloak
  - `grafana` - Monitoring

### 7.3 Kubernetes Deployment
Helm charts có sẵn trong `k8s/charts/` cho mỗi service:
- Deployment configurations
- Service definitions
- ConfigMaps
- Secrets management

### 7.4 CI/CD với GitHub Actions
**Workflows** (`.github/workflows/`):
- Build và test mỗi service
- SonarCloud analysis
- Docker image build & push
- Automated deployment

**Pipeline steps**:
1. Checkout code
2. Setup Java 21
3. Maven build
4. Run unit tests
5. Run integration tests
6. SonarCloud scan
7. Build Docker image
8. Push to GitHub Container Registry

---

## 8. Observability Stack

### 8.1 Logging với Loki
- Centralized log aggregation
- Log correlation với trace IDs
- Query logs qua Grafana

### 8.2 Metrics với Prometheus
- Application metrics
- JVM metrics
- Custom business metrics
- Alerting rules

### 8.3 Tracing với Tempo
- Distributed tracing
- Request flow visualization
- Performance analysis
- Error tracking

### 8.4 OpenTelemetry Integration
Tất cả services được instrument với OpenTelemetry:
- Java Agent auto-instrumentation
- Trace context propagation
- Metrics export
- Log correlation

### 8.5 Grafana Dashboards
**URL**: `http://grafana/`
- Service health monitoring
- Request latency metrics
- Error rate tracking
- Resource utilization

---

## 9. Data & Messaging

### 9.1 Database Architecture
Mỗi microservice có database riêng (Database-per-Service pattern):

| Service | Database |
|---------|----------|
| Keycloak | keycloak |
| Product | product |
| Order | order |
| Cart | cart |
| Customer | customer |
| Inventory | inventory |
| Media | media |
| Rating | rating |
| Location | location |
| Tax | tax |
| Promotion | promotion |
| Payment | payment |
| Webhook | webhook |
| Recommendation | recommendation |

### 9.2 Kafka Messaging
**Topics chính**:
- Product updates (CDC via Debezium)
- Order events
- Payment events

### 9.3 Change Data Capture (CDC)
Debezium connectors để đồng bộ data:
- `debezium-product.json` - Sync product changes
- `debezium-order.json` - Sync order changes

**Flow**:
```
PostgreSQL (Product DB) → Debezium → Kafka → Search Service → Elasticsearch
```

---

## 10. Hướng dẫn cài đặt

### 10.1 Yêu cầu hệ thống
- **RAM**: Tối thiểu 16GB (khuyến nghị 32GB)
- **Docker Desktop**: Phiên bản mới nhất
- **Docker Compose**: v2+

### 10.2 Cấu hình hosts file
Thêm vào `C:\Windows\System32\drivers\etc\hosts` (Windows) hoặc `/etc/hosts` (Linux/Mac):
```
127.0.0.1 identity
127.0.0.1 api.yas.local
127.0.0.1 pgadmin.yas.local
127.0.0.1 storefront
127.0.0.1 backoffice
127.0.0.1 loki
127.0.0.1 tempo
127.0.0.1 grafana
127.0.0.1 elasticsearch
127.0.0.1 kafka
```

### 10.3 Khởi động ứng dụng

**Chạy core services**:
```bash
docker compose up
```

**Chạy với search**:
```bash
docker compose -f docker-compose.yml -f docker-compose.search.yml up
```

**Chạy với observability**:
```bash
docker compose -f docker-compose.yml -f docker-compose.o11y.yml up
```

**Chạy tất cả**:
```bash
docker compose -f docker-compose.yml -f docker-compose.search.yml -f docker-compose.o11y.yml up
```

### 10.4 Khởi tạo Debezium connectors
```bash
./start-source-connectors.sh
```

### 10.5 Access URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| Storefront | http://storefront/ | - |
| Backoffice | http://backoffice/ | admin/password |
| Keycloak | http://identity/ | admin/admin |
| Swagger UI | http://api.yas.local/swagger-ui/ | - |
| PgAdmin | http://pgadmin.yas.local/ | admin@yas.com/admin |
| Grafana | http://grafana/ | anonymous |
| Elasticsearch | http://elasticsearch:9200/ | - |

---

## 11. API Documentation

### 11.1 Swagger UI
Truy cập: `http://api.yas.local/swagger-ui/`

Tất cả REST APIs được document với OpenAPI 3.0

### 11.2 API Patterns

**Naming conventions**:
- `/backoffice/*` - Admin APIs (cần authentication)
- `/storefront/*` - Customer APIs
- Internal APIs giữa các services

**Response format**:
```json
{
  "data": {...},
  "error": null
}
```

**Error response**:
```json
{
  "statusCode": 400,
  "title": "Bad Request",
  "detail": "Validation failed",
  "fieldErrors": [...]
}
```

---

## 12. Development Guidelines

### 12.1 Code Style
- Tuân thủ Google Java Style Guide
- Sử dụng Checkstyle plugin
- Run `npx prettier -w .` cho frontend code

### 12.2 Testing Strategy
- **Unit tests**: `src/test/java/` - Chạy bởi Surefire plugin
- **Integration tests**: `src/test/java/it/` - Chạy bởi Failsafe plugin
- Sử dụng Testcontainers cho integration tests

### 12.3 Git Workflow
- Pull request nên nhỏ gọn
- Include #issueId trong commit message
- Delete branch sau khi merge

### 12.4 Database Migrations
- Sử dụng Liquibase
- DDL changesets: `db/changelog/ddl/`
- Data changesets: `db/changelog/data/`
- **Không** update changesets đã chạy

---

## 13. Tổng kết

YAS là một dự án microservices hoàn chỉnh với:

✅ **20+ microservices** được thiết kế theo domain  
✅ **Modern tech stack** (Java 21, Spring Boot 4, Next.js)  
✅ **Full observability** (Metrics, Logging, Tracing)  
✅ **Event-driven architecture** với Kafka  
✅ **Full-text search** với Elasticsearch  
✅ **CI/CD pipeline** với GitHub Actions  
✅ **Kubernetes-ready** deployment  
✅ **Comprehensive testing** với Testcontainers  

Dự án này là một excellent reference cho việc học tập và thực hành xây dựng hệ thống microservices trong thực tế.

---

**Repository**: https://github.com/nashtech-garage/yas  
**License**: MIT
