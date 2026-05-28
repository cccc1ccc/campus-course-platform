# 校园代课对接平台

> 基于 Java + Spring Boot + MySQL + Redis 的课程供需撮合系统

## 项目简介

学生可发布代课需求，其他学生可浏览并接单，完成后支持互相评价。系统设计了完整的订单状态机和站内消息通知，使用 Redis 缓存热门课程数据以减少数据库访问。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7 |
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
│   │   └── PageRequest.java              # 分页参数
│   ├── model/
│   │   ├── entity/
│   │   │   ├── User.java                 # 用户实体
│   │   │   ├── CourseOrder.java           # 代课订单实体
│   │   │   ├── Review.java               # 评价实体
│   │   │   └── Notification.java          # 通知实体
│   │   └── enum_/
│   │       └── OrderStatus.java          # 订单状态枚举
│   ├── dto/
│   │   ├── CourseOrderCreateDTO.java     # 发布订单请求
│   │   └── ReviewCreateDTO.java          # 评价请求
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── CourseOrderRepository.java
│   │   ├── ReviewRepository.java
│   │   └── NotificationRepository.java
│   ├── service/
│   │   ├── CourseOrderService.java
│   │   ├── ReviewService.java
│   │   └── NotificationService.java
│   ├── service/impl/
│   │   ├── CourseOrderServiceImpl.java    # 核心业务（含状态机+Redis缓存）
│   │   ├── ReviewServiceImpl.java
│   │   └── NotificationServiceImpl.java
│   └── controller/
│       ├── CourseOrderController.java     # 订单 RESTful API
│       ├── ReviewController.java          # 评价 API
│       └── NotificationController.java    # 通知 API
└── src/main/resources/
    ├── application.yml                    # 应用配置
    └── schema.sql                         # 建表脚本 + 示例数据
```

## 快速启动

### 前置条件
- JDK 11+
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

> 所有接口通过 `X-User-Id` 请求头传递当前用户ID（简化鉴权，生产环境请替换为 JWT）

### 请求示例

**发布代课需求：**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
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
  -H "X-User-Id: 2"
```

## License

MIT
