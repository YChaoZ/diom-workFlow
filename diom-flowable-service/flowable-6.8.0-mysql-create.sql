-- ===========================================
-- Flowable 6.8.0 MySQL 数据库建表脚本
-- ===========================================
-- 说明：
-- 1. Flowable 引擎会自动创建所有必要的表（约180+张）
-- 2. 本脚本仅创建数据库和初始化配置
-- 3. 首次启动时设置 flowable.database-schema-update=true 自动建表
-- 4. 生产环境建议手动执行完整建表脚本后设置为 false
--
-- 表分类：
-- - ACT_RE_* : 流程定义仓库表（Repository）
-- - ACT_RU_* : 运行时数据表（Runtime）
-- - ACT_HI_* : 历史数据表（History）
-- - ACT_ID_* : 身份管理表（Identity）
-- - ACT_GE_* : 通用数据表（General）
-- - ACT_EVT_* : 事件表（Event）
-- - FLW_*     : Flowable 特有表
-- ===========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS diom_flowable 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE diom_flowable;

-- ===========================================
-- 方式 1: 使用 Flowable 自动建表（推荐开发环境）
-- ===========================================
-- 在 application.yml 中设置：
-- flowable:
--   database-schema-update: true  # 首次启动自动建表
--
-- 启动服务后会自动创建所有表，然后修改为 false

-- ===========================================
-- 方式 2: 手动执行完整建表脚本（推荐生产环境）
-- ===========================================
-- 下载完整脚本：
-- https://github.com/flowable/flowable-engine/blob/flowable-6.8.0/modules/flowable-engine/src/main/resources/org/flowable/db/create/flowable.mysql.create.engine.sql
-- https://github.com/flowable/flowable-engine/blob/flowable-6.8.0/modules/flowable-engine/src/main/resources/org/flowable/db/create/flowable.mysql.create.history.sql
-- https://github.com/flowable/flowable-engine/blob/flowable-6.8.0/modules/flowable-engine/src/main/resources/org/flowable/db/create/flowable.mysql.create.identity.sql
--
-- 或使用以下命令导入（需要下载完整脚本）：
-- mysql -uroot -p1qaz2wsx diom_flowable < flowable.mysql.all.create.sql

-- ===========================================
-- 核心表结构概览（仅供参考）
-- ===========================================

-- 1. 流程定义仓库表（6张）
-- ACT_RE_DEPLOYMENT         - 部署信息
-- ACT_RE_MODEL              - 模型信息
-- ACT_RE_PROCDEF            - 流程定义

-- 2. 运行时数据表（16张）
-- ACT_RU_EXECUTION          - 流程实例/执行
-- ACT_RU_TASK               - 任务
-- ACT_RU_VARIABLE           - 流程变量
-- ACT_RU_IDENTITYLINK       - 身份关联
-- ACT_RU_JOB                - 作业

-- 3. 历史数据表（13张）
-- ACT_HI_PROCINST           - 历史流程实例
-- ACT_HI_TASKINST           - 历史任务
-- ACT_HI_ACTINST            - 历史活动
-- ACT_HI_VARINST            - 历史变量
-- ACT_HI_IDENTITYLINK       - 历史身份关联

-- 4. 身份管理表（6张）
-- ACT_ID_USER               - 用户
-- ACT_ID_GROUP              - 用户组
-- ACT_ID_MEMBERSHIP         - 用户组关系
-- ACT_ID_INFO               - 用户扩展信息

-- 5. 通用数据表（3张）
-- ACT_GE_BYTEARRAY          - 二进制数据
-- ACT_GE_PROPERTY           - 属性配置

-- ===========================================
-- 初始化完成提示
-- ===========================================
-- 数据库 diom_flowable 已创建
-- 
-- 下一步：
-- 1. 修改 application.yml 中的数据库连接配置
-- 2. 首次启动设置 database-schema-update: true
-- 3. 启动服务，Flowable 会自动创建所有表
-- 4. 验证表创建完成后，修改为 database-schema-update: false
--
-- 验证命令：
-- SELECT COUNT(*) FROM information_schema.tables 
-- WHERE table_schema = 'diom_flowable';
-- 
-- 预期结果：180+ 张表
-- ===========================================

