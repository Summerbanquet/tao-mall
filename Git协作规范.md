# 小组 Git 协作规范

写给第一次用 Git 协作的组员，照着做就行，不想敲命令的话每个步骤在 IDEA 里都有对应的图形界面按钮。

## 0. 一次性准备（只做一次）

1. 注册 GitHub 账号，把用户名发给仓库创建者；收到邀请邮件后点 Accept 成为 Collaborator。
2. 安装 Git：https://git-scm.com ，一路默认下一步。
3. 配置身份（打开命令行执行，名字用中文或拼音都行）：

   ```bash
   git config --global user.name "你的名字"
   git config --global user.email "你注册GitHub用的邮箱"
   ```

4. 克隆项目，然后用 IDEA 打开：

   ```bash
   git clone https://github.com/Summerbanquet/tao-mall.git
   ```

   IDEA 中 File → Open，选中项目根目录下的 `pom.xml`，点 Open as Project，等 Maven 索引跑完。

## 1. 日常开发流程（每天都是这个循环）

```bash
# ① 开工第一件事：同步最新代码
git checkout main
git pull

# ② 从 main 切出自己的功能分支（命名见第 2 节）
git checkout -b feature/你的功能名

# ③ 开发，小步提交（做完一小块就提交一次，别攒一星期）
git add .
git commit -m "feat: 完成xxx功能"

# ④ 推送自己的分支到 GitHub
git push -u origin feature/你的功能名

# ⑤ 到 GitHub 仓库页面提 Pull Request，请一名组员 review

# ⑥ review 通过 → 合并进 main → 删掉功能分支 → 回到①做下一个任务
```

IDEA 对应操作：右上角分支切换菜单、左下角 Git 窗口看历史、顶部 Git 菜单做 pull/push/commit。

## 2. 分支命名

| 前缀 | 用途 | 示例 |
| --- | --- | --- |
| `feature/` | 新功能 | `feature/user-login` |
| `fix/` | 修 bug | `fix/order-total-null` |
| `docs/` | 只改文档 | `docs/readme` |

规则：一律从 `main` 切出；一个分支只做一件事；合并后立刻删除，不留长期分支。

## 3. Commit 信息规范

格式：`类型: 干了什么`，一句话说清，中文即可。

- `feat:` 新功能
- `fix:` 修 bug
- `docs:` 文档
- `refactor:` 重构（功能不变）
- `style:` 格式调整
- `test:` 测试
- `chore:` 构建、依赖等杂项

好的例子：`feat: 用户注册接口`　坏例子：`update`、`修改`、`111`

## 4. 红线（不要碰）

- 禁止直接向 `main` push 代码。仓库已设分支保护，只能通过 PR 合并——这是为了防止有人把跑不起来的代码推上去坑全组。
- 禁止 `git push -f`（强制推送会吃掉别人的提交）。
- 不要动 `.gitignore`，它挡住了 `target/`、`.idea/`、`*.iml` 这些不该进仓库的文件。
- 数据库密码不要写进 `application.properties` 提交，写到 `application-local.properties`（已在忽略名单里）。
- 别改别人负责的模块目录；确实要动公共模块（`tao-common`），先在群里说一声。

## 5. 冲突了怎么办

冲突不是事故，只是两个人改了同一处，Git 需要人来做决定：

1. `git pull` 时提示冲突，或 PR 页面显示 conflict。
2. 用 IDEA 打开标红的冲突文件，对每一处选择保留哪边，或手动融合两边（IDEA 的三栏对比界面很直观）。
3. 重新 commit + push，PR 会自动更新。
4. 拿不准就把组员叫过来一起看，别硬猜。

## 6. 出问题找谁

- 仓库权限、邀请 Collaborator → 仓库创建者
- 合并冲突、分支操作 → 群里吼一声
