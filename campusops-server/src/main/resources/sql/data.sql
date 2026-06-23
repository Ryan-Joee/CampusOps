-- CampusOps 初始化数据脚本
-- 适用数据库：MySQL 8.x
-- 使用方式：请先执行 schema.sql，再执行本脚本

SET NAMES utf8mb4;

INSERT INTO sys_role (id, role_code, role_name, description) VALUES
(1, 'normal_user', '普通用户', '提交工单、查看自己的工单、确认问题解决'),
(2, 'technician', 'IT 处理人', '处理分配给自己的工单并维护处理记录'),
(3, 'service_admin', '服务管理员', '负责工单分派、服务分类、统计分析和知识库审核'),
(4, 'system_admin', '系统管理员', '负责用户、角色、权限和系统配置');

-- 密码占位说明：以下密码哈希仅用于开发占位，正式开发时请使用后端 PasswordEncoder 重新生成
INSERT INTO sys_user (id, username, password_hash, real_name, email, phone, department, status) VALUES
(1, 'admin', '{noop}admin123', '系统管理员', 'admin@campusops.local', '13800000001', '信息化办公室', 'enabled'),
(2, 'service_admin', '{noop}admin123', '服务管理员', 'service@campusops.local', '13800000002', '信息化办公室', 'enabled'),
(3, 'tech_network', '{noop}admin123', '网络运维老师', 'network@campusops.local', '13800000003', '网络运维组', 'enabled'),
(4, 'tech_system', '{noop}admin123', '系统运维老师', 'system@campusops.local', '13800000004', '系统运维组', 'enabled'),
(5, 'student001', '{noop}123456', '张三', 'student001@campusops.local', '13800000005', '计算机学院', 'enabled'),
(6, 'teacher001', '{noop}123456', '李老师', 'teacher001@campusops.local', '13800000006', '软件学院', 'enabled');

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 4),
(2, 3),
(3, 2),
(4, 2),
(5, 1),
(6, 1);

INSERT INTO ticket_category (id, category_code, category_name, description, enabled) VALUES
(1, 'network', '网络问题', '校园网、宿舍网、VPN、认证网络等问题', 1),
(2, 'account', '账号权限', '统一身份认证、账号锁定、权限申请等问题', 1),
(3, 'system_error', '系统故障', '教务系统、门户系统、业务系统异常', 1),
(4, 'software', '软件环境', '软件安装、环境配置、客户端异常等问题', 1),
(5, 'device', '设备故障', '机房设备、投影仪、打印机等设备问题', 1),
(6, 'consultation', '咨询类问题', '使用咨询、流程咨询、信息查询等问题', 1);

INSERT INTO knowledge_category (id, category_code, category_name, enabled) VALUES
(1, 'network', '网络与 VPN', 1),
(2, 'account', '账号与权限', 1),
(3, 'system', '业务系统使用', 1),
(4, 'software', '软件与环境', 1),
(5, 'device', '设备与机房', 1);

INSERT INTO ticket (id, ticket_no, title, description, category_id, priority, status, submitter_id, assignee_id, assigned_at, due_at, source, ai_summary) VALUES
(1, 'INC-2026-0001', '校园网无法连接', '宿舍网络一直断开，认证页面无法打开，重启电脑后仍然无效。', 1, 'high', 'processing', 5, 3, NOW(), DATE_ADD(NOW(), INTERVAL 8 HOUR), 'web', '用户反馈宿舍网络无法连接，认证页面无法打开，需网络运维排查。'),
(2, 'INC-2026-0002', '统一身份认证提示密码错误', '登录统一身份认证时提示账号或密码错误，但昨天还能正常登录。', 2, 'medium', 'pending_process', 6, 4, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 'web', '用户反馈统一身份认证登录失败，疑似账号状态或密码问题。'),
(3, 'INC-2026-0003', '教务系统页面加载很慢', '进入教务系统后页面一直转圈，课程查询页面加载很慢。', 3, 'medium', 'pending_assignment', 5, NULL, NULL, DATE_ADD(NOW(), INTERVAL 1 DAY), 'web', NULL);

INSERT INTO ticket_record (ticket_id, operator_id, record_type, from_status, to_status, content, visible_to_user) VALUES
(1, 5, 'system', NULL, 'pending_assignment', '用户提交工单。', 1),
(1, 2, 'assign', 'pending_assignment', 'pending_process', '服务管理员将工单分派给网络运维老师。', 1),
(1, 3, 'status_change', 'pending_process', 'processing', '处理人已接单，开始排查网络问题。', 1),
(2, 6, 'system', NULL, 'pending_assignment', '用户提交工单。', 1),
(2, 2, 'assign', 'pending_assignment', 'pending_process', '服务管理员将工单分派给系统运维老师。', 1),
(3, 5, 'system', NULL, 'pending_assignment', '用户提交工单。', 1);

INSERT INTO ticket_comment (ticket_id, user_id, content) VALUES
(1, 5, '问题主要出现在晚上，白天偶尔可以连接。'),
(1, 3, '已收到反馈，请补充宿舍楼栋和房间号。'),
(2, 6, '我确认密码没有修改过，也没有收到异常提醒。');

INSERT INTO knowledge_article (id, title, summary, content, category_id, status, source_ticket_id, author_id, reviewer_id, published_at) VALUES
(1, '校园网认证页面无法打开的常见处理方法', '介绍校园网认证页面无法打开时的基础排查步骤。', '1. 确认设备已连接校园网。\n2. 尝试访问认证页面。\n3. 清理浏览器缓存。\n4. 重启网络适配器。\n5. 如果仍无法连接，请提交工单并附上错误截图。', 1, 'published', NULL, 3, 2, NOW()),
(2, '统一身份认证登录失败处理建议', '介绍账号密码错误、账号锁定等常见情况的处理方式。', '1. 确认账号和密码输入正确。\n2. 检查大小写和输入法状态。\n3. 如果多次失败，账号可能被临时锁定。\n4. 如需重置密码，请联系信息化办公室或提交工单。', 2, 'published', NULL, 4, 2, NOW());

INSERT INTO knowledge_article_tag (article_id, tag_name) VALUES
(1, '校园网'),
(1, '认证页面'),
(1, '网络故障'),
(2, '统一身份认证'),
(2, '密码错误'),
(2, '账号锁定');

INSERT INTO ai_analysis_record (business_type, business_id, analysis_type, input_summary, output_content, model_name, status, latency_ms, created_by) VALUES
('ticket', 1, 'summary', '校园网无法连接', '用户反馈宿舍网络无法连接，认证页面无法打开，需网络运维排查。', 'mock-ai', 'success', 120, 2),
('ticket', 2, 'classification', '统一身份认证提示密码错误', '{"category":"account","confidence":0.92,"reason":"描述中包含统一身份认证和密码错误"}', 'mock-ai', 'success', 95, 2);

INSERT INTO ai_recommendation (ticket_id, target_type, target_id, score, reason, analysis_record_id) VALUES
(1, 'knowledge', 1, 0.8800, '该知识库文章包含认证页面无法打开的常见排查步骤。', 1),
(2, 'knowledge', 2, 0.9100, '该知识库文章介绍统一身份认证登录失败处理建议。', 2);

INSERT INTO operation_log (operator_id, operation_type, business_type, business_id, description, ip_address, user_agent) VALUES
(5, 'ticket_create', 'ticket', 1, '用户创建工单 INC-2026-0001', '127.0.0.1', 'seed-data'),
(2, 'ticket_assign', 'ticket', 1, '服务管理员分派工单 INC-2026-0001', '127.0.0.1', 'seed-data'),
(3, 'ticket_accept', 'ticket', 1, '处理人接单 INC-2026-0001', '127.0.0.1', 'seed-data');

INSERT INTO notification (receiver_id, title, content, business_type, business_id, read_status) VALUES
(3, '新工单待处理', '你有一个新的网络问题工单待处理。', 'ticket', 1, 'unread'),
(4, '新工单待处理', '你有一个新的账号权限工单待处理。', 'ticket', 2, 'unread');

