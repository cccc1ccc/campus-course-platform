# 校园代课对接平台

> 基于 Java + Spring Boot + MySQL + Redis 的课程供需撮合系统

## 项目简介

学生可发布代课需求，其他学生可浏览并接单，完成后支持互相评价。系统设计了完整的订单状态机和站内消息通知，使用 Redis 缓存热门课程数据以减少数据库访问。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7 |
| 安全 | Spring Security + JWT + BCrypt |
| ORM | Spring Data JPA + Hibernate |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis + Lettuce |
| 构建工具 | Maven |
| 工具库 | Lombok |

## 核心功能

### 1. 代课需求发布与接单
- 学生发布代课需求（课程名、时间、地点、报酬）
- 其他学生浏览并接单
- 支持按状态筛选和模糊搜索

### 2. 订单状态机
```
PUBLISHED → TAKEN → IN_PROGRESS → COMPLETED → REVIEWED
   ↓          ↓
 CANCELLED  CANCELLED
```
- 每次状态转换都有权限校验和业务逻辑保护
- 不可逆状态转换，防止非法操作

### 3. 评价体系
- 订单完成后双方可互相评分（1~5 星）
- 防止重复评价
- 评价完成后订单自动流转至 REVIEWED 状态

### 4. 消息通知
- 接单、开始、完成、取消、评价等关键操作均触发站内通知
- 支持未读计数和批量已读

### 5. 热门课程缓存（Redis）
- 按课程发布频次统计热门课程
- 结果缓存至 Redis，TTL 2 小时
- 发布新订单时自动失效缓存

### 6. 认证与安全
- 用户注册 / 登录，密码 BCrypt 加密存储
- JWT 无状态鉴权，所有业务接口需携带 `Authorization: Bearer <token>`
- 从 SecurityContext 取当前用户，杜绝伪造用户 ID

### 7. 并发保护
- 订单实体使用 `@Version` 乐观锁
- 接单通过原子条件更新（仅 `PUBLISHED` 状态可接），防止并发下多人抢单成功

## 项目结构

```
campus-course-platform/
├── pom.xml
├── src/main/java/com/campus/course/
│   ├── CampusCourseApplication.java      # 启动类
│   ├── config/
│   │   ├── RedisConfig.java              # Redis 序列化配置
│   │   └── GlobalExceptionHandler.java   # 全局异常处理
│   ├── common/
│   │   ├── Result.java                   # 统一响应封装
│   │   ├── PageRequest.java              # 分页参数
│   │   ├── ErrorCode.java                # 业务错误码枚举
│   │   └── BizException.java             # 业务异常
│   ├── security/
│   │   ├── SecurityConfig.java           # Spring Security 配置
│   │   ├── JwtUtil.java                  # JWT 生成/解析
│   │   ├── JwtAuthFilter.java            # JWT 鉴权过滤器
│   │   └── SecurityUtils.java            # 当前登录用户工具
│   ├── model/
│   │   ├── entity/
│   │   │   ├── User.java                 # 用户实体
│   │   │   ├── CourseOrder.java           # 代课订单实体（含乐观锁 version）
│   │   │   ├── Review.java               # 评价实体
│   │   │   └── Notification.java          # 通知实体
│   │   └── enum_/
│   │       └── OrderStatus.java          # 订单状态枚举
│   ├── dto/
│   │   ├── CourseOrderCreateDTO.java     # 发布订单请求
│   │   ├── ReviewCreateDTO.java          # 评价请求
│   │   ├── RegisterDTO.java              # 注册请求
│   │   ├── LoginDTO.java                 # 登录请求
│   │   └── AuthResponse.java             # 登录/注册响应
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── CourseOrderRepository.java
│   │   ├── ReviewRepository.java
│   │   └── NotificationRepository.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── CourseOrderService.java
│   │   ├── ReviewService.java
│   │   └── NotificationService.java
│   ├── service/impl/
│   │   ├── AuthServiceImpl.java           # 注册登录 + BCrypt
│   │   ├── CourseOrderServiceImpl.java    # 核心业务（含状态机+Redis缓存+乐观锁）
│   │   ├── ReviewServiceImpl.java
│   │   └── NotificationServiceImpl.java
│   └── controller/
│       ├── AuthController.java            # 注册 / 登录 API
│       ├── CourseOrderController.java     # 订单 RESTful API
│       ├── ReviewController.java          # 评价 API
│       └── NotificationController.java    # 通知 API
├── src/test/java/com/campus/course/service/
│   ├── CourseOrderServiceImplTest.java    # 状态机 + 并发接单测试
│   ├── ReviewServiceImplTest.java         # 评价体系测试
│   └── AuthServiceImplTest.java           # 注册登录 + 密码加密测试
└── src/main/resources/
    ├── application.yml                    # 应用配置（含 JWT）
    └── schema.sql                         # 建表脚本 + 示例数据（BCrypt 密码）
```

## 快速启动

### 前置条件
- JDK 11 或 17（推荐 17）
- MySQL 8.0
- Redis 6.0+
- Maven 3.6+

### 启动步骤

1. **初始化数据库**
```bash
mysql -u root -p < src/main/resources/schema.sql
```

2. **修改配置**

编辑 `src/main/resources/application.yml`，修改数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_course?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password   # ← 改成你的密码
```

3. **启动 Redis**
```bash
redis-server
```

4. **编译运行**
```bash
mvn clean package -DskipTests
java -jar target/campus-course-platform-1.0.0.jar
```

## API 接口文档

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 注册（返回 JWT） |
| POST | `/api/auth/login` | 登录（返回 JWT） |

> 除 `/api/auth/**` 外的所有接口都需要在请求头携带 `Authorization: Bearer <token>`。
> 登录/注册成功后返回的 `token` 用于后续接口鉴权，服务端从 token 中解析用户身份，不再信任客户端传入的用户 ID。

### 订单接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/orders` | 发布代课需求 |
| PUT | `/api/orders/{id}/take` | 接单 |
| PUT | `/api/orders/{id}/start` | 开始代课 |
| PUT | `/api/orders/{id}/complete` | 完成代课 |
| PUT | `/api/orders/{id}/cancel` | 取消订单 |
| GET | `/api/orders?status=PUBLISHED` | 按状态查询 |
| GET | `/api/orders/search?keyword=高数` | 搜索课程 |
| GET | `/api/orders/published` | 我发布的 |
| GET | `/api/orders/taken` | 我接的 |
| GET | `/api/orders/{id}` | 订单详情 |
| GET | `/api/orders/hot?limit=10` | 热门课程 |

### 评价接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/reviews` | 提交评价 |
| GET | `/api/reviews/user/{userId}` | 某用户的评价 |
| GET | `/api/reviews/order/{orderId}` | 某订单的评价 |

### 通知接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/notifications` | 通知列表 |
| GET | `/api/notifications/unread-count` | 未读数量 |
| PUT | `/api/notifications/{id}/read` | 标记已读 |
| PUT | `/api/notifications/read-all` | 全部已读 |

> 所有业务接口通过 JWT 鉴权，从 `Authorization: Bearer <token>` 中解析当前用户身份。

### 请求示例

**注册：**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "zhangsan", "password": "123456", "phone": "13800001111"}'
# 返回 { "code": 200, "data": { "token": "eyJhbGci...", "userId": 1, "username": "zhangsan" } }
```

**登录：**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "zhangsan", "password": "123456"}'
```

**发布代课需求（携带 token）：**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <你的token>" \
  -d '{
    "courseName": "高等数学A",
    "courseCode": "MATH1001",
    "classTime": "周一 3-4节",
    "classLocation": "教学楼A301",
    "reward": 50.00,
    "description": "本周一3-4节高数课需要代课"
  }'
```

**接单：**
```bash
curl -X PUT http://localhost:8080/api/orders/1/take \
  -H "Authorization: Bearer <你的token>"
```

## 运行测试

```bash
mvn test
```

## License

MIT
