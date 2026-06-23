# CampusOps Development Guide

CampusOps is the English name of this project. The Chinese project name is "面向校园 IT 服务的 AI 增强型工单管理平台".

## Project Structure

- `docs/`: requirements, architecture, permissions, AI module design, testing, and development documents.
- `campusops-server/`: backend project, based on Java and Spring Boot.
- `campusops-front/`: frontend project.

## Required Reading Before Development

Before backend or frontend development, read these documents first:

- `docs/00-项目总览.md`
- `docs/01-需求说明.md`
- `docs/02-用户角色与权限边界.md`
- `docs/03-业务流程.md`
- `docs/04-系统架构设计.md`
- `docs/05-数据库设计.md`
- `docs/06-接口设计.md`
- `docs/07-权限设计.md`
- `docs/08-AI模块设计.md`
- `docs/09-测试方案.md`
- `docs/10-工程启动计划.md`
- `docs/11-AI开发工作流.md`
- `docs/12-AI-Agent协作规范.md`

For major technical choices, also read `docs/adr/`.

## Working Boundaries

- Backend code must stay under `campusops-server/`.
- Frontend code must stay under `campusops-front/`.
- Project documents must stay under `docs/`.
- Do not make large cross-directory changes without explaining the reason.
- Do not let the AI module directly modify ticket status, user permissions, or other core business data.
- All critical business operations must go through permission checks and backend Service-layer logic.

## Backend Baseline

- Spring Boot version: `3.5.15`.
- Java version: `17`.
- Build tool: Maven.
- ORM choice: MyBatis Plus.
- JPA is not used in this project.
- Database: MySQL, unless a later ADR changes this decision.

## Frontend Baseline

- Vue 3.
- Vite.
- JavaScript, not TypeScript in the first stage.
- Vue Router.
- Pinia.
- Element Plus.
- Axios.
- ECharts.
- The frontend should be a management console and ticket workbench, not a landing page or visual showcase.

## API Prefix

- Public API paths use the project prefix `/campusops`.
- Do not add an extra `/api/v1` layer in the first stage.
- Authentication APIs use paths such as `/campusops/auth/login`.
- Ticket-related APIs use paths such as `/campusops/tickets`.

## Maven Dependency Rules

- Prefer dependencies managed by the Spring Boot parent BOM.
- Do not manually set versions for Spring Boot managed dependencies unless there is a documented reason.
- Third-party dependencies not managed by Spring Boot must have versions compatible with Spring Boot `3.5.15` and Java `17`.
- Add new backend dependencies only when they are needed by the current implementation stage.
- Do not add Redis, Spring AI, LangChain4j, message queues, or vector database dependencies before their corresponding module is planned.
- Do not add Flyway or Liquibase in the first stage. Database initialization uses plain SQL files unless a later ADR changes this decision.

## Development Requirements

- Follow the design in `docs/` first.
- Update related documents when changing features, APIs, database schema, permissions, or AI behavior.
- Add tests for new core business logic.
- If requirements are unclear, state the uncertainty instead of expanding the scope silently.
