-- OncoResi 数据库初始化脚本 (openGauss 5.1.0)
-- DDD 架构 + 数据权限设计

-- ============================================================
-- 1. 组织架构表
-- ============================================================

-- 医院表
CREATE TABLE IF NOT EXISTS sys_hospital (
    id BIGSERIAL PRIMARY KEY,
    hospital_name VARCHAR(100) NOT NULL,
    hospital_code VARCHAR(50) UNIQUE,
    address VARCHAR(200),
    contact_phone VARCHAR(20),
    director VARCHAR(50) COMMENT '院长',
    create_time TIMESTAMP NOT NULL DEFAULT now(),
    update_time TIMESTAMP NOT NULL DEFAULT now()
);
COMMENT ON TABLE sys_hospital IS '医院表';
COMMENT ON COLUMN sys_hospital.id IS '医院ID';
COMMENT ON COLUMN sys_hospital.hospital_name IS '医院名称';
COMMENT ON COLUMN sys_hospital.hospital_code IS '医院编码';

-- 科室表（直属医院，无基地层级）
CREATE TABLE IF NOT EXISTS sys_department (
    id BIGSERIAL PRIMARY KEY,
    hospital_id BIGINT NOT NULL,
    dept_name VARCHAR(100) NOT NULL,
    dept_code VARCHAR(50) UNIQUE,
    dept_type VARCHAR(50) COMMENT '科室类型：肿瘤科、内科、外科等',
    director_id BIGINT COMMENT '科室主任用户ID',
    description VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT now(),
    update_time TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_dept_hospital FOREIGN KEY (hospital_id) REFERENCES sys_hospital(id) ON DELETE CASCADE
);
COMMENT ON TABLE sys_department IS '科室表';
COMMENT ON COLUMN sys_department.dept_type IS '科室类型：肿瘤科、内科、外科、儿科等';

CREATE INDEX idx_dept_hospital ON sys_department(hospital_id);
CREATE INDEX idx_dept_code ON sys_department(dept_code);

-- ============================================================
-- 2. 用户权限表
-- ============================================================

-- 用户表（新增组织字段）
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),

    -- 组织架构字段
    hospital_id BIGINT COMMENT '所属医院ID',
    dept_id BIGINT COMMENT '所属科室ID',

    status INT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    create_time TIMESTAMP NOT NULL DEFAULT now(),
    update_time TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_user_hospital FOREIGN KEY (hospital_id) REFERENCES sys_hospital(id) ON DELETE SET NULL,
    CONSTRAINT fk_user_dept FOREIGN KEY (dept_id) REFERENCES sys_department(id) ON DELETE SET NULL
);
COMMENT ON TABLE sys_user IS '用户表';

CREATE INDEX idx_user_username ON sys_user(username);
CREATE INDEX idx_user_status ON sys_user(status);
CREATE INDEX idx_user_dept ON sys_user(dept_id);
CREATE INDEX idx_user_hospital ON sys_user(hospital_id);

-- 角色表（6种角色，删除BASE_ADMIN）
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    create_time TIMESTAMP NOT NULL DEFAULT now()
);
COMMENT ON TABLE sys_role IS '角色表';

CREATE INDEX idx_role_code ON sys_role(code);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
);
COMMENT ON TABLE sys_user_role IS '用户角色关联表';

-- ============================================================
-- 3. 数据权限表
-- ============================================================

-- 用户数据权限范围表
CREATE TABLE IF NOT EXISTS sys_user_data_scope (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    scope_type VARCHAR(20) NOT NULL COMMENT '权限类型: ALL(全院), DEPT(科室), SUPERVISED(带教学员), SELF(个人)',
    dept_id BIGINT COMMENT '科室ID（DEPT类型时必填）',
    create_time TIMESTAMP NOT NULL DEFAULT now(),
    update_time TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_scope_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_scope_dept FOREIGN KEY (dept_id) REFERENCES sys_department(id) ON DELETE CASCADE
);
COMMENT ON TABLE sys_user_data_scope IS '用户数据权限范围表';
COMMENT ON COLUMN sys_user_data_scope.scope_type IS 'ALL-全院, DEPT-科室, SUPERVISED-带教学员, SELF-个人';

CREATE INDEX idx_scope_user ON sys_user_data_scope(user_id);
CREATE INDEX idx_scope_type ON sys_user_data_scope(scope_type);

-- 导师-学员关联表（支持跨科室带教）
CREATE TABLE IF NOT EXISTS sys_supervisor_trainee (
    supervisor_id BIGINT NOT NULL,
    trainee_id BIGINT NOT NULL,
    start_date DATE NOT NULL COMMENT '带教起始日期',
    end_date DATE COMMENT '带教结束日期',
    create_time TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (supervisor_id, trainee_id),
    CONSTRAINT fk_st_supervisor FOREIGN KEY (supervisor_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_st_trainee FOREIGN KEY (trainee_id) REFERENCES sys_user(id) ON DELETE CASCADE
);
COMMENT ON TABLE sys_supervisor_trainee IS '导师-学员关联表';

CREATE INDEX idx_st_trainee ON sys_supervisor_trainee(trainee_id);

-- ============================================================
-- 4. 初始化基础数据
-- ============================================================

-- 初始化医院数据
INSERT INTO sys_hospital (hospital_name, hospital_code, address, contact_phone, director) VALUES
('肿瘤医院', 'HOSP001', '北京市海淀区XX路XX号', '010-12345678', '王院长');

-- 初始化科室数据
INSERT INTO sys_department (hospital_id, dept_name, dept_code, dept_type) VALUES
(1, '肿瘤内科', 'DEPT001', '肿瘤科'),
(1, '肿瘤外科', 'DEPT002', '肿瘤科'),
(1, '放射治疗科', 'DEPT003', '肿瘤科'),
(1, '病理科', 'DEPT004', '医技科室');

-- 初始化角色数据（6种角色，无BASE_ADMIN）
INSERT INTO sys_role (code, name, description) VALUES
('HOSPITAL_ADMIN', '医院管理员', '医院级别管理员，拥有全局管理权限，数据权限：ALL'),
('DEPT_ADMIN', '科室管理员', '科室级别管理员，管理科室相关事务，数据权限：DEPT'),
('SUPERVISOR', '责任导师', '负责学员指导与评价，数据权限：SUPERVISED'),
('TEACHER', '带教老师', '负责日常带教工作，数据权限：DEPT'),
('TRAINEE', '学员', '参加规培的学员，数据权限：SELF'),
('NURSE_EVALUATOR', '护士评价', '护士角色，可对学员进行评价'),
('PATIENT_EVALUATOR', '病人评价', '患者角色，可对学员进行评价');

-- 初始化测试用户
-- 密码: admin123 (BCrypt加密后的值，需要实际运行后替换)
INSERT INTO sys_user (username, password, real_name, phone, email, hospital_id, dept_id, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKMuOQ6.', '系统管理员', '13800138000', 'admin@oncoresi.com', 1, NULL, 1),
('teacher', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKMuOQ6.', '张老师', '13800138001', 'teacher@oncoresi.com', 1, 1, 1),
('student', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKMuOQ6.', '李学员', '13800138002', 'student@oncoresi.com', 1, 1, 1);

-- 分配角色
-- admin用户: 医院管理员
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- teacher用户: 责任导师 + 带教老师
INSERT INTO sys_user_role (user_id, role_id) VALUES
(2, 3),  -- SUPERVISOR
(2, 4);  -- TEACHER

-- student用户: 学员
INSERT INTO sys_user_role (user_id, role_id) VALUES (3, 5);

-- 初始化数据权限
INSERT INTO sys_user_data_scope (user_id, scope_type, dept_id) VALUES
(1, 'ALL', NULL),        -- 医院管理员：全院权限
(2, 'SUPERVISED', 1),    -- 导师：带教学员权限
(3, 'SELF', NULL);       -- 学员：个人权限

-- 初始化导师-学员关系
INSERT INTO sys_supervisor_trainee (supervisor_id, trainee_id, start_date, end_date) VALUES
(2, 3, '2024-01-01', '2025-12-31');  -- 张老师带教李学员

-- ============================================================
-- 5. 创建视图（可选，用于快速查询）
-- ============================================================

-- 用户完整信息视图
CREATE OR REPLACE VIEW v_user_full_info AS
SELECT
    u.id,
    u.username,
    u.real_name,
    u.phone,
    u.email,
    h.hospital_name,
    d.dept_name,
    d.dept_type,
    u.status,
    string_agg(r.code, ',') as role_codes,
    string_agg(r.name, ',') as role_names,
    ds.scope_type as data_scope_type
FROM sys_user u
LEFT JOIN sys_hospital h ON u.hospital_id = h.id
LEFT JOIN sys_department d ON u.dept_id = d.id
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
LEFT JOIN sys_user_data_scope ds ON u.id = ds.user_id
GROUP BY u.id, u.username, u.real_name, u.phone, u.email,
         h.hospital_name, d.dept_name, d.dept_type, u.status, ds.scope_type;

COMMENT ON VIEW v_user_full_info IS '用户完整信息视图（包含组织架构、角色、数据权限）';
