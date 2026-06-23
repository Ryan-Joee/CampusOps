# CampusOps Frontend

CampusOps 前端工程，基于 Vue 3、Vite、JavaScript、Vue Router、Pinia、Element Plus、Axios 和 ECharts。

## 启动命令

```bash
npm install
npm run dev
```

开发服务端口固定为：

```text
3017
```

## 目录说明

```text
src/
  api/       后端接口封装
  layouts/   页面整体布局
  router/    路由配置
  stores/    Pinia 状态管理
  views/     页面组件
```

## 接口约定

前端请求统一使用 `/campusops` 作为 `baseURL`。例如：

- 登录：`/campusops/auth/login`
- 当前用户：`/campusops/auth/me`
- 工单列表：`/campusops/tickets`

## 当前阶段

当前只完成前端基础工程骨架和页面占位，真实业务数据需要后续接入后端接口。
