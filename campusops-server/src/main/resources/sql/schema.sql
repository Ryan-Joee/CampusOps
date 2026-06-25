-- CampusOps 数据库建表脚本
-- 适用数据库：MySQL 8.x
-- 使用方式：先创建数据库，再在 Navicat 或 MySQL 客户端中执行本脚本

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS ai_recommendation;
DROP TABLE IF EXISTS ai_analysis_record;
DROP TABLE IF EXISTS knowledge_article_tag;
DROP TABLE IF EXISTS knowledge_article;
DROP TABLE IF EXISTS knowledge_category;
DROP TABLE IF EXISTS ticket_evaluation;
DROP TABLE IF EXISTS ticket_attachment;
DROP TABLE IF EXISTS ticket_comment;
DROP TABLE IF EXISTS ticket_record;
DROP TABLE IF EXISTS ticket;
DROP TABLE IF EXISTS ticket_category;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(64) NOT NULL COMMENT '登录名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
    email VARCHAR(128) NOT NULL COMMENT '邮箱',
    phone VARCHAR(32) NOT NULL COMMENT '手机号',
    department VARCHAR(128) NULL COMMENT '院系或部门',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '用户状态：enabled、disabled',
    last_login_at DATETIME NULL COMMENT '最近登录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_email (email),
    UNIQUE KEY uk_sys_user_phone (phone),
    KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) NULL COMMENT '角色说明',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
    UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    role_id BIGINT NOT NULL COMMENT '角色 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_sys_user_role (user_id, role_id),
    KEY idx_sys_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    permission_code VARCHAR(128) NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(128) NOT NULL COMMENT '权限名称',
    permission_type VARCHAR(32) NOT NULL COMMENT '权限类型：menu、api、button',
    parent_id BIGINT NULL COMMENT '父级权限 ID',
    path VARCHAR(255) NULL COMMENT '菜单路径或接口路径',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
    UNIQUE KEY uk_sys_permission_code (permission_code),
    KEY idx_sys_permission_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限表';

CREATE TABLE sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色 ID',
    permission_id BIGINT NOT NULL COMMENT '权限 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_sys_role_permission (role_id, permission_id),
    KEY idx_sys_role_permission_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关联表';

CREATE TABLE ticket_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    category_code VARCHAR(64) NOT NULL COMMENT '分类编码',
    category_name VARCHAR(64) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) NULL COMMENT '分类说明',
    parent_id BIGINT NULL COMMENT '父级分类 ID',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0 否，1 是',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
    UNIQUE KEY uk_ticket_category_code (category_code),
    KEY idx_ticket_category_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单分类表';

CREATE TABLE ticket (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    ticket_no VARCHAR(64) NOT NULL COMMENT '工单编号',
    title VARCHAR(200) NOT NULL COMMENT '工单标题',
    description TEXT NOT NULL COMMENT '问题描述',
    category_id BIGINT NULL COMMENT '工单分类 ID',
    priority VARCHAR(32) NOT NULL DEFAULT 'medium' COMMENT '优先级：low、medium、high、urgent',
    status VARCHAR(32) NOT NULL DEFAULT 'pending_assignment' COMMENT '工单状态',
    submitter_id BIGINT NOT NULL COMMENT '提交人 ID',
    assignee_id BIGINT NULL COMMENT '当前处理人 ID',
    assigned_at DATETIME NULL COMMENT '分派时间',
    resolved_at DATETIME NULL COMMENT '标记解决时间',
    closed_at DATETIME NULL COMMENT '关闭时间',
    due_at DATETIME NULL COMMENT '期望处理截止时间',
    source VARCHAR(32) NOT NULL DEFAULT 'web' COMMENT '来源：web、mobile、admin、ai_draft',
    ai_summary VARCHAR(1000) NULL COMMENT '当前 AI 摘要',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
    UNIQUE KEY uk_ticket_no (ticket_no),
    KEY idx_ticket_submitter_id (submitter_id),
    KEY idx_ticket_assignee_id (assignee_id),
    KEY idx_ticket_status (status),
    KEY idx_ticket_category_id (category_id),
    KEY idx_ticket_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单主表';

CREATE TABLE ticket_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    ticket_id BIGINT NOT NULL COMMENT '工单 ID',
    operator_id BIGINT NOT NULL COMMENT '操作人 ID',
    record_type VARCHAR(32) NOT NULL COMMENT '记录类型：status_change、assign、process、reply、system',
    from_status VARCHAR(32) NULL COMMENT '变更前状态',
    to_status VARCHAR(32) NULL COMMENT '变更后状态',
    content TEXT NOT NULL COMMENT '记录内容',
    visible_to_user TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否对提交人可见：0 否，1 是',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_ticket_record_ticket_id (ticket_id),
    KEY idx_ticket_record_operator_id (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单处理记录表';

CREATE TABLE ticket_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    ticket_id BIGINT NOT NULL COMMENT '工单 ID',
    user_id BIGINT NOT NULL COMMENT '评论人 ID',
    content TEXT NOT NULL COMMENT '评论内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
    KEY idx_ticket_comment_ticket_id (ticket_id),
    KEY idx_ticket_comment_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单评论表';

CREATE TABLE ticket_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    ticket_id BIGINT NOT NULL COMMENT '工单 ID',
    uploader_id BIGINT NOT NULL COMMENT '上传人 ID',
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_size BIGINT NOT NULL COMMENT '文件大小，单位字节',
    content_type VARCHAR(128) NULL COMMENT '文件类型',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
    KEY idx_ticket_attachment_ticket_id (ticket_id),
    KEY idx_ticket_attachment_uploader_id (uploader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单附件表';

CREATE TABLE ticket_evaluation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    ticket_id BIGINT NOT NULL COMMENT '工单 ID',
    user_id BIGINT NOT NULL COMMENT '评价人 ID',
    score INT NOT NULL COMMENT '评分，1 到 5',
    comment VARCHAR(500) NULL COMMENT '评价内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_ticket_evaluation_ticket_id (ticket_id),
    KEY idx_ticket_evaluation_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单评价表';

CREATE TABLE knowledge_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    category_code VARCHAR(64) NOT NULL COMMENT '分类编码',
    category_name VARCHAR(64) NOT NULL COMMENT '分类名称',
    parent_id BIGINT NULL COMMENT '父级分类 ID',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0 否，1 是',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
    UNIQUE KEY uk_knowledge_category_code (category_code),
    KEY idx_knowledge_category_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库分类表';

CREATE TABLE knowledge_article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    summary VARCHAR(1000) NULL COMMENT '摘要',
    content TEXT NOT NULL COMMENT '正文',
    category_id BIGINT NULL COMMENT '分类 ID',
    status VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '状态：draft、published、archived',
    source_ticket_id BIGINT NULL COMMENT '来源工单 ID',
    author_id BIGINT NOT NULL COMMENT '作者 ID',
    reviewer_id BIGINT NULL COMMENT '审核人 ID',
    published_at DATETIME NULL COMMENT '发布时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
    KEY idx_knowledge_article_status (status),
    KEY idx_knowledge_article_category_id (category_id),
    KEY idx_knowledge_article_source_ticket_id (source_ticket_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库文章表';

CREATE TABLE knowledge_article_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    article_id BIGINT NOT NULL COMMENT '文章 ID',
    tag_name VARCHAR(64) NOT NULL COMMENT '标签名称',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_knowledge_article_tag_article_id (article_id),
    KEY idx_knowledge_article_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库文章标签表';

CREATE TABLE ai_analysis_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    business_type VARCHAR(64) NOT NULL COMMENT '业务类型：ticket、knowledge',
    business_id BIGINT NOT NULL COMMENT '业务对象 ID',
    analysis_type VARCHAR(64) NOT NULL COMMENT '分析类型：classification、summary、recommendation、draft',
    input_summary TEXT NULL COMMENT '输入摘要',
    output_content TEXT NULL COMMENT '输出内容',
    model_name VARCHAR(128) NULL COMMENT '模型名称',
    status VARCHAR(32) NOT NULL COMMENT '状态：success、failed',
    latency_ms INT NULL COMMENT '调用耗时，单位毫秒',
    error_message VARCHAR(1000) NULL COMMENT '错误信息',
    created_by BIGINT NULL COMMENT '触发人 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_ai_analysis_business (business_type, business_id),
    KEY idx_ai_analysis_type (analysis_type),
    KEY idx_ai_analysis_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 分析记录表';

CREATE TABLE ai_recommendation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    ticket_id BIGINT NOT NULL COMMENT '当前工单 ID',
    target_type VARCHAR(32) NOT NULL COMMENT '推荐目标类型：ticket、knowledge',
    target_id BIGINT NOT NULL COMMENT '推荐目标 ID',
    score DECIMAL(5,4) NULL COMMENT '相似度或置信度',
    reason VARCHAR(500) NULL COMMENT '推荐理由',
    analysis_record_id BIGINT NULL COMMENT '关联 AI 分析记录 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_ai_recommendation_ticket_id (ticket_id),
    KEY idx_ai_recommendation_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 推荐结果表';

CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    operator_id BIGINT NULL COMMENT '操作人 ID',
    operation_type VARCHAR(64) NOT NULL COMMENT '操作类型',
    business_type VARCHAR(64) NULL COMMENT '业务类型',
    business_id BIGINT NULL COMMENT '业务对象 ID',
    description VARCHAR(1000) NULL COMMENT '操作说明',
    ip_address VARCHAR(64) NULL COMMENT 'IP 地址',
    user_agent VARCHAR(500) NULL COMMENT 'User-Agent',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_operation_log_operator_id (operator_id),
    KEY idx_operation_log_business (business_type, business_id),
    KEY idx_operation_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

CREATE TABLE notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    receiver_id BIGINT NOT NULL COMMENT '接收人 ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content VARCHAR(1000) NOT NULL COMMENT '通知内容',
    business_type VARCHAR(64) NULL COMMENT '业务类型',
    business_id BIGINT NULL COMMENT '业务对象 ID',
    read_status VARCHAR(32) NOT NULL DEFAULT 'unread' COMMENT '状态：unread、read',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    read_at DATETIME NULL COMMENT '阅读时间',
    KEY idx_notification_receiver_id (receiver_id),
    KEY idx_notification_read_status (read_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内通知表';

SET FOREIGN_KEY_CHECKS = 1;
