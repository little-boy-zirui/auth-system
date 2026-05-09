# 用户指令记忆

本文件记录了用户的指令、偏好和教导，用于在未来的交互中提供参考。

## 格式

### 用户指令条目
用户指令条目应遵循以下格式：

[用户指令摘要]
- Date: [YYYY-MM-DD]
- Context: [提及的场景或时间]
- Instructions:
  - [用户教导或指示的内容，逐行描述]

### 项目知识条目
Agent 在任务执行过程中发现的条目应遵循以下格式：

[项目知识摘要]
- Date: [YYYY-MM-DD]
- Context: Agent 在执行 [具体任务描述] 时发现
- Category: [代码结构|代码模式|代码生成|构建方法|测试方法|依赖关系|环境配置]
- Instructions:
  - [具体的知识点，逐行描述]

## 去重策略
- 添加新条目前，检查是否存在相似或相同的指令
- 若发现重复，跳过新条目或与已有条目合并
- 合并时，更新上下文或日期信息
- 这有助于避免冗余条目，保持记忆文件整洁

## 条目

[用户输出语言偏好]
- Date: 2026-05-09
- Context: 用户在当前仓库中发起开发任务
- Instructions:
  - 所有回复使用简体中文。

[当前后端基础结构]
- Date: 2026-05-09
- Context: Agent 在执行登录认证系统搭建任务时发现
- Category: 代码结构
- Instructions:
  - 项目根目录当前包含 `backend/` 子项目。
  - 后端已存在 Spring Boot Maven 工程，核心配置在 `backend/pom.xml` 与 `backend/src/main/resources/application.yaml`。
  - 后端当前已声明 Spring Security、OAuth2 Client、OAuth2 Resource Server、MyBatis-Plus、MySQL 与 JustAuth 依赖。

[当前前端构建方式]
- Date: 2026-05-09
- Context: Agent 在执行登录认证系统搭建任务时发现
- Category: 构建方法
- Instructions:
  - 前端使用 Vue 3 + Vite，项目目录为 `frontend/`。
  - 前端构建命令为 `npm run build`，已可在当前环境成功执行。
  - `frontend/vite.config.js` 已配置 `/api` 反向代理到 `http://localhost:8080`，并包含 `.monkeycode-ai.online` 的 `allowedHosts` 配置。

[当前后端运行约束]
- Date: 2026-05-09
- Context: Agent 在执行登录认证系统搭建任务时发现
- Category: 环境配置
- Instructions:
  - 当前工作区环境未提供 `mvn` 命令，且 `backend/` 暂无 Maven Wrapper，导致无法直接在此环境执行 Maven 测试或启动命令。
