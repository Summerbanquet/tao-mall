# Campus Mall

> 校园电商/交易平台 —— 基于 Spring Boot 的单体多模块项目。一句话简介可以再打磨：这个平台卖什么、给谁用、解决什么问题。

## 功能特性

- 功能点 1（例：基于 JWT 的登录与权限控制）
- 功能点 2
- 功能点 3

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 核心框架 | Spring Boot 3.x |
| 持久层 | MyBatis-Plus / Spring Data JPA（二选一，删掉不用的） |
| 数据库 | MySQL 8.x |
| 缓存 | Redis（没用到就删掉这行） |
| 构建工具 | Maven（多模块） |
| 其他 | Lombok、Knife4j / Swagger |

## 项目结构

单体多模块，父工程统一管理依赖版本：

```
campus-mall
├── mall-common     # 通用工具类、常量、统一返回体、全局异常处理
├── mall-entity     # 实体类与数据访问层（Mapper/Repository）
├── mall-service    # 业务逻辑层
└── mall-web        # Web 入口（Controller），Spring Boot 启动模块
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x

### 步骤

```bash
# 1. 克隆
git clone https://github.com/Summerbanquet/campus-mall.git

# 2. 建库：执行 sql/ 目录下的初始化脚本

# 3. 改配置：修改 mall-web/src/main/resources/application.yml 中的数据库连接

# 4. 启动（IDEA 里直接运行 mall-web 的启动类，或命令行）：
mvn spring-boot:run -pl mall-web
```

访问 http://localhost:8080 即可。

## 团队成员

| 成员 | 负责模块 |
| --- | --- |
| [@Summerbanquet](https://github.com/Summerbanquet) | mall-web |
| [@xxx](https://github.com/xxx) | mall-service |

## 协作规范

见 [Git协作规范.md](Git协作规范.md)。

## 开源协议

本项目基于 [MIT License](LICENSE) 开源。
