# 校园淘（tao_svr）

面向高校场景的**二手交易平台**，采用 Spring Boot 3 多模块架构 + 原生前端（零构建工具），覆盖商品发布、分类筛选、收藏、下单、订单流转，以及完整的管理后台。

---

## 一、功能特性

**用户端**
- 注册（用户名 / 密码 / 姓名 / 手机 / 邮箱，昵称留空自动生成随机昵称，如 `云端过客7671`）
- JWT 登录，token 有效期 7 天
- 个人资料维护（姓名、昵称、手机、邮箱、头像上传）
- 商品发布（支持本地图片上传 + 实时预览）
- 首页**关键词 + 分类**组合筛选、分页
- 商品收藏 / 取消收藏 / 我的收藏
- 下单、我的订单、取消订单

**管理端**（需管理员身份）
- 用户管理：分页查询、禁用/启用、删除
- 商品管理：分页查询、上架/下架、删除
- 订单管理：分页查询、修改订单状态、删除
- 分类管理：列表、新增、删除

---

## 二、技术栈

| 技术 | 版本 | 说明 |
|---|---|---|
| JDK | 18 | Spring Boot 3 要求 17+ |
| Spring Boot | 3.5.4 | Web / 拦截器 / 资源映射 |
| MyBatis-Plus | 3.5.9 | ORM + 分页插件 + 逻辑删除 |
| MySQL | 8.0 | 数据库 |
| JWT (jjwt) | 0.12.3 | 登录令牌 |
| Maven | 3.x | 构建工具（多模块） |
| 前端 | 原生 HTML + CSS + JS | 无 npm、无构建步骤 |

---

## 三、项目结构

```
tao_svr_mutile/                 # 父工程（pom 聚合）
├── tao-common/                 # 公共模块
│   ├── entity/AjaxResult       # 统一响应体 {code, msg, data}
│   ├── entity/PageResult       # 分页结果 {total, rows}
│   ├── util/EncryptUtil        # MD5 加盐加密（盐$$摘要）
│   ├── util/NickNameUtil       # 随机昵称生成
│   ├── util/TokenUtil          # JWT 生成/解析（密钥由配置注入）
│   └── exception/              # 业务异常 + 全局异常处理
├── tao-user/                   # 用户模块（User 实体 / Service / Mapper）
├── tao-order/                  # 订单模块（Order 实体 / Service / Mapper）
├── tao-goods/                  # 商品模块（Goods / Category / Collect）
├── tao-web/                    # 接口层（唯一可启动模块）
│   ├── controller/web/         # 用户端接口 /web/**
│   ├── controller/adm/         # 管理端接口 /adm/**
│   ├── controller/common/      # 通用接口（文件上传）
│   ├── interceptor/            # LoginInterceptor / AdminInterceptor
│   ├── config/                 # WebConfig / MybatisPlusConfig / JwtConfig
│   ├── TaoApplication.java     # 启动类
│   └── resources/static/       # 前端页面
└── sql/init.sql                # 建库脚本（7 张表 + 初始分类）
```

模块依赖关系：`tao-web` → `tao-goods` / `tao-order` / `tao-user` → `tao-common`

---

## 四、快速开始

### 环境要求
- JDK 18+（Spring Boot 3 要求 17 以上）
- Maven 3.6+
- MySQL 8.0（已启动）

### 1. 初始化数据库

```bash
mysql -uroot -p123456 < sql/init.sql
```

脚本会创建 `tao` 库及 7 张表，并写入 4 条初始分类（电子产品 / 书籍文具 / 服饰鞋包 / 生活用品）。

### 2. 修改配置

`tao-web/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/tao?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowMultiQueries=true&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:root}       # 可用环境变量覆盖
    password: ${DB_PASSWORD:123456}     # 可用环境变量覆盖

tao:
  jwt:
    secret: ${JWT_SECRET:默认值...}      # 建议生产环境用环境变量注入
    expire-millis: 604800000            # token 有效期 7 天

file:
  upload:
    path: D:/tao/uploads                # 上传目录，需自行创建
```

> 密码与 JWT 密钥都支持环境变量，默认值仅供本地开发。生产环境请设置 `DB_PASSWORD`、`JWT_SECRET`。

### 3. 启动

方式一（推荐，IDEA）：运行 `tao-web/src/main/java/com/tao/web/TaoApplication.java`

方式二（命令行）：

```bash
mvn -pl tao-web -am spring-boot:run
```

或打包后运行：

```bash
mvn clean package -DskipTests
java -jar tao-web/target/tao-web-*.jar
```

### 4. 访问

- 首页：<http://localhost:8080/>
- 管理后台：<http://localhost:8080/admin.html>

### 测试账号

| 用户名 | 密码 | 身份 | 说明 |
|---|---|---|---|
| `test01` | `123456` | 管理员（`type=1`） | 可进管理后台 |
| `test02` | `123456` | 普通用户（`type=2`） | 调 `/adm/**` 会被 403 拦截 |

> 若库里账号的 `type` 都是 2，需手工提权：
> `UPDATE t_user SET type = 1 WHERE user_name = 'test01';`

---

## 五、数据库设计

| 表名 | 说明 |
|---|---|
| `t_user` | 用户表（密码 MD5 加盐；`type` 1运营 2用户；逻辑删除） |
| `t_order` | 订单表（订单号、商品快照、收货信息、状态） |
| `goods` | 商品表（标题、价格、封面、分类、状态；逻辑删除） |
| `category` | 商品分类表 |
| `collect` | 收藏表（用户 + 商品；逻辑删除） |

> 表名用 `t_user` / `t_order` 而非 `user` / `order`，因为 `user`、`order` 是 MySQL 保留字。

**关键状态枚举**

| 字段 | 取值 |
|---|---|
| `t_user.type` | 1 运营（管理员） 2 普通用户 |
| `t_user.status` | 0 禁用 1 正常 |
| `goods.status` | 0 下架 1 在售 |
| `t_order.status` | 0 待付款 1 已付款 2 已发货 3 已完成 4 已取消 |

删除操作**全部为逻辑删除**（置 `deleted=1`），数据不物理丢失。

---

## 六、API 接口清单

共 **43 个接口**。完整测试用例见 [`docs/tao-api-collection.json`](docs/tao-api-collection.json)，可直接导入 Apifox / Postman（登录后 token 自动填充）。

### 用户端 `/web/**`

| 方法 | 路径 | 说明 | 需登录 |
|---|---|---|---|
| POST | `/web/user/login` | 登录，返回 token | - |
| POST | `/web/user/register` | 注册 | - |
| GET | `/web/user/checkUserName/{userName}` | 用户名是否可用 | - |
| GET | `/web/user/info` | 当前用户信息 | ✓ |
| PUT | `/web/user/info` | 修改资料 | ✓ |
| POST | `/web/user/logout` | 退出登录 | ✓ |
| POST | `/web/goods/page` | 商品分页（支持 `title`、`categoryId`） | - |
| GET | `/web/goods/getInfo/{id}` | 商品详情 | - |
| GET | `/web/category/list` | 分类列表 | - |
| POST | `/web/goods/add` | 发布商品 | ✓ |
| PUT | `/web/goods/edit` | 编辑自己的商品 | ✓ |
| POST | `/web/goods/my` | 我发布的 | ✓ |
| PUT | `/web/goods/status/{id}` | 上架/下架 | ✓ |
| DELETE | `/web/goods/dele/{id}` | 删除商品 | ✓ |
| POST | `/web/collect/add/{goodsId}` | 收藏 | ✓ |
| DELETE | `/web/collect/cancel/{goodsId}` | 取消收藏 | ✓ |
| GET | `/web/collect/my` | 我的收藏 | ✓ |
| GET | `/web/collect/check/{goodsId}` | 是否已收藏 | ✓ |
| POST | `/web/order/add` | 提交订单 | ✓ |
| POST | `/web/order/page` | 我的订单 | ✓ |
| GET | `/web/order/getInfo/{id}` | 订单详情 | ✓ |
| POST | `/web/order/cancel/{id}` | 取消订单 | ✓ |
| POST | `/web/common/upload` | 图片上传（form-data，字段名 `file`） | ✓ |

### 管理端 `/adm/**`（需登录 + 管理员身份）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/adm/user/page` | 用户分页（密码脱敏） |
| GET | `/adm/user/getInfo/{id}` | 用户详情 |
| POST | `/adm/user/add` | 新增用户 |
| PUT | `/adm/user/edit` | 修改用户 |
| DELETE | `/adm/user/dele/{id}` | 删除用户 |
| DELETE | `/adm/user/deles` | 批量删除 |
| POST | `/adm/user/listSystem` | 运营人员列表 |
| POST | `/adm/user/listUser` | 普通用户列表 |
| POST | `/adm/goods/page` | 商品分页 |
| GET | `/adm/goods/getInfo/{id}` | 商品详情 |
| PUT | `/adm/goods/status/{id}` | 上架/下架 |
| DELETE | `/adm/goods/dele/{id}` | 删除商品 |
| POST | `/adm/order/page` | 订单分页 |
| GET | `/adm/order/getInfo/{id}` | 订单详情 |
| PUT | `/adm/order/edit` | 修改订单 |
| DELETE | `/adm/order/dele/{id}` | 删除订单 |
| GET | `/adm/category/list` | 分类列表 |
| POST | `/adm/category/add` | 新增分类 |
| PUT | `/adm/category/edit` | 修改分类 |
| DELETE | `/adm/category/dele/{id}` | 删除分类 |

**统一响应格式**

```json
{ "code": 200, "msg": "操作成功", "data": { } }
```

`code`：200 成功，401 未登录，403 无权限，500 服务器异常。

---

## 七、前端页面

页面位于 `tao-web/src/main/resources/static/`，原生 HTML + JS，浏览器直接打开，**无需 npm 或构建工具**（修改后刷新即可生效）。

| 页面 | 路径 | 功能 |
|---|---|---|
| 首页 | `index.html` | 商品列表、关键词搜索、分类筛选、分页 |
| 登录 | `login.html` | 登录后 token 存 localStorage，自动回跳 |
| 注册 | `register.html` | 用户名实时查重、密码二次确认、格式校验 |
| 商品详情 | `detail.html?id=1` | 详情、收藏/取消、下单 |
| 发布闲置 | `publish.html` | 标题/描述/价格/分类/封面上传 |
| 我的 | `my.html` | 我发布的 / 我的订单 / 我的收藏 |
| 个人资料 | `profile.html` | 修改姓名、昵称、手机、邮箱、头像 |
| 管理后台 | `admin.html` | 用户 / 商品 / 订单 / 分类管理 |

公共能力（`js/api.js`）：统一携带 token、401 自动跳登录、toast 提示、金额与时间格式化。

---

## 八、安全设计

| 措施 | 说明 |
|---|---|
| 密码加盐哈希 | MD5 + 随机盐，存储格式 `盐$$摘要`，不明文存储 |
| 密码脱敏 | 登录、个人信息、管理端用户列表**均不返回密码字段** |
| JWT 密钥外置 | 密钥从配置读取，支持 `JWT_SECRET` 环境变量注入，不硬编码 |
| 登录鉴权 | `LoginInterceptor` 拦截受保护接口，token 无效返回 401 |
| 管理员鉴权 | `AdminInterceptor` 二次校验 `type=1`，普通用户访问 `/adm/**` 返回 403 |
| 越权防护 | 商品编辑/删除、订单查询均按当前登录用户过滤，只能操作自己的数据 |
| 上传校验 | 限制扩展名（jpg/png/gif/webp/bmp）与大小（20MB），文件名用 UUID 重命名 |
| XSS 防护 | 前端渲染统一做 HTML 转义 |

---

## 九、常见问题

**Q：启动时报 `Access denied for user 'root'`。**
检查 `application.yml` 的数据库账号密码，或设置环境变量 `DB_PASSWORD`。

**Q：上传图片后访问 404。**
确认 `file.upload.path` 指向的目录存在（默认 `D:/tao/uploads`），`WebConfig` 已把 `/uploads/**` 映射到该目录。

**Q：进不了管理后台。**
当前登录用户 `type` 必须是 1。执行 `UPDATE t_user SET type = 1 WHERE user_name = '你的账号';`

**Q：命令行用 curl 测 POST 接口返回 500。**
PowerShell 5.x 会把 JSON 里的双引号吃掉，导致服务端收到非法 JSON。建议改为文件传参：

```bash
curl -X POST http://localhost:8080/web/user/login -H "Content-Type: application/json" -d @body.json
```

或直接使用 `docs/tao-api-collection.json` 导入 Apifox 测试。

**Q：中文乱码。**
项目已统一 UTF-8（pom、`.mvn/jvm.config`、数据库连接参数）。日志文件在 `D:/tao/logs/tao.log`。

---

## 十、团队协作：数据库如何同步

### 核心原则

> **同步「表结构」，不同步「数据」。**

每个人的数据库都是本地环境，账号和商品数据各人自己造。反复互拷全量 SQL 会覆盖对方数据、引发主键冲突。

### 三种场景对应三种做法

| 场景 | 做法 |
|---|---|
| **新人第一次拿到项目** | 执行 `sql/init.sql`（或用项目根目录 `sync.bat` 一键完成） |
| **别人改了表结构，我要跟上** | `git pull` 后双击 `sync.bat` |
| **想给组员看你已有的商品数据** | 导出全量 SQL 发给他（见下） |

### 我改了表结构，怎么让别人同步

1. 在 `sql/migration/` 下**新建**下一个编号的脚本，例如 `002_add_order_remark.sql`
2. 脚本必须写成**幂等**的（可重复执行不报错），参考 `001_add_goods_category_id.sql`：

```sql
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'tao' AND TABLE_NAME = 'goods' AND COLUMN_NAME = 'category_id'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE goods ADD COLUMN category_id BIGINT NULL COMMENT ''分类ID''',
    'SELECT ''字段已存在，跳过'' AS skip_reason');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
```

3. `git add / commit / push`
4. 通知组员 `git pull`，然后双击根目录的 **`sync.bat`**（或 `sync.bat 你的密码`）即可自动按序执行所有迁移

> 不要修改或删除已提交的迁移脚本，别人的环境可能已经执行过了。

### 临时给组员拷贝数据

只想把当前数据分享给对方一次性导入时用：

```bash
mysqldump -uroot -p123456 --default-character-set=utf8mb4 --complete-insert \
  tao t_user t_order goods category collect > tao_full.sql
```

对方导入：

```bash
mysql -uroot -p
source D:/tao_full.sql
```

> 注意：这会让对方的数据被你的覆盖，仅用于初次演示，不要反复使用。
> 另外商品图片在 `file.upload.path` 指定的目录（默认 `D:/tao/uploads`）里，**不在数据库中**，需要另外打包给对方。

---

## 十一、开发说明

- 日志目录：`D:/tao/logs/tao.log`
- 上传目录：`D:/tao/uploads/`（按日期分目录，UUID 重命名）
- 静态资源修改后**无需重启**，Spring Boot 每次请求实时读取；Java 代码改动需重启
- 打包：`mvn clean package -DskipTests`
