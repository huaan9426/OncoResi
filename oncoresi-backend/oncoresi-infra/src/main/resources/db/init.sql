-- 初始化数据库脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS oncoresi DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE oncoresi;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码(加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    status INT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代码',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) COMMENT '描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 初始化角色数据(8种角色)
INSERT INTO sys_role (code, name, description) VALUES
('HOSPITAL_ADMIN', '医院管理员', '医院级别管理员,拥有全局管理权限'),
('BASE_ADMIN', '专业基地管理员', '专业基地管理员,管理专业基地相关事务'),
('DEPT_ADMIN', '科室管理员', '科室级别管理员,管理科室相关事务'),
('SUPERVISOR', '责任导师', '负责学员指导与评价'),
('TEACHER', '带教老师', '负责日常带教工作'),
('TRAINEE', '学员', '参加规培的学员'),
('NURSE_EVALUATOR', '护士评价', '护士角色,可对学员进行评价'),
('PATIENT_EVALUATOR', '病人评价', '患者角色,可对学员进行评价');

-- 初始化测试用户
-- 密码: admin123 (BCrypt加密后)
-- 注意: 下面的密码哈希需要运行 PasswordEncoderTest.java 生成后替换
INSERT INTO sys_user (username, password, real_name, phone, email, status) VALUES
('admin', '$2a$10$8Zw9Y1dR6pBvU7X5QqGZeO5h7RfI3kC1mW8Nj2Lp4Aq6Tr5Hs8VwK', '系统管理员', '13800138000', 'admin@oncoresi.com', 1),
('teacher', '$2a$10$8Zw9Y1dR6pBvU7X5QqGZeO5h7RfI3kC1mW8Nj2Lp4Aq6Tr5Hs8VwK', '张老师', '13800138001', 'teacher@oncoresi.com', 1),
('student', '$2a$10$8Zw9Y1dR6pBvU7X5QqGZeO5h7RfI3kC1mW8Nj2Lp4Aq6Tr5Hs8VwK', '李学员', '13800138002', 'student@oncoresi.com', 1);

-- 分配角色
-- admin用户: 医院管理员
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1);

-- teacher用户: 责任导师 + 带教老师
INSERT INTO sys_user_role (user_id, role_id) VALUES
(2, 4),
(2, 5);

-- student用户: 学员
INSERT INTO sys_user_role (user_id, role_id) VALUES
(3, 6);
