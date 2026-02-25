-- #########################################################
-- 创建表 api_log                                         已创建
CREATE TABLE api_log (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ip VARCHAR(45) NOT NULL,
    api VARCHAR(255) NOT NULL,
    states INTEGER NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE api_log IS '访问日志表';

COMMENT ON COLUMN api_log.id IS '主键ID，自增';

COMMENT ON COLUMN api_log.ip IS '访问者IP地址';

COMMENT ON COLUMN api_log.api IS '访问路径';

COMMENT ON COLUMN api_log.states IS '状态';

COMMENT ON COLUMN api_log.create_time IS '访问时间';
-- 表注释
COMMENT ON TABLE "api_log" IS '访问日志表';

-- 11111111111111111111111111111111111111111111111111111111111111111111111111
-- 用户表
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY, -- 用户ID，自增主键
    name VARCHAR(50) NOT NULL, -- 用户姓名
    cover BYTEA, -- 用户头像二进制数据
    phone VARCHAR(20) UNIQUE, -- 手机号（唯一）
    mail VARCHAR(100) UNIQUE, -- 邮箱（唯一）
    password_hash VARCHAR(100) NOT NULL, -- 密码哈希
    place VARCHAR(100), -- 用户所在地区
    achievement_json JSONB NOT NULL DEFAULT '{}'::jsonb, -- 成就 JSON 数据
    expired_time TIMESTAMP -- 登录过期时间
);

-- 表注释
COMMENT ON TABLE users IS '用户信息表';

-- 字段注释
COMMENT ON COLUMN users.user_id IS '用户ID，自增主键';

COMMENT ON COLUMN users.name IS '用户姓名';

COMMENT ON COLUMN users.cover IS '用户头像二进制数据';

COMMENT ON COLUMN users.phone IS '手机号';

COMMENT ON COLUMN users.mail IS '邮箱';

COMMENT ON COLUMN users.password_hash IS '用户密码哈希';

COMMENT ON COLUMN users.place IS '用户所在地区';

COMMENT ON COLUMN users.achievement_json IS '成就 JSON 数据';

COMMENT ON COLUMN users.expired_time IS '登录过期时间';

-- 111111111111111111111111111111111111111111111111111111111111111111111
-- 创建表 article                                         已创建
CREATE TABLE article (
    article_id BIGSERIAL PRIMARY KEY, -- 自增主键
    title VARCHAR(200) NOT NULL, -- 标题
    cover VARCHAR(300), -- 封面图片 URL或FileURI
    info VARCHAR(500), -- 简介
    texts TEXT NOT NULL, -- 正文内容
    tags VARCHAR(200), -- 标签（可用逗号分隔）
    create_time TIMESTAMP NOT NULL DEFAULT NOW(), -- 创建时间，默认当前时间
    update_time TIMESTAMP NOT NULL DEFAULT NOW() -- 更新时间，默认当前时间
);

-- 字段注释
COMMENT ON COLUMN article.article_id IS '文章ID，自增主键';

COMMENT ON COLUMN article.title IS '文章标题';

COMMENT ON COLUMN article.cover IS '封面图片地址';

COMMENT ON COLUMN article.info IS '文章简介';

COMMENT ON COLUMN article.texts IS '文章正文内容';

COMMENT ON COLUMN article.tags IS '文章标签（可用逗号分隔）';

COMMENT ON COLUMN article.create_time IS '创建时间';

COMMENT ON COLUMN article.update_time IS '更新时间';

-- 表注释
COMMENT ON TABLE article IS '文章表';

-- #########################################################
-- 创建表 wiki                                           已创建
CREATE TABLE wiki (
    wiki_id BIGSERIAL PRIMARY KEY, -- 自增主键
    key_name VARCHAR(200) NOT NULL UNIQUE, -- 唯一键名（必须唯一）
    texts TEXT NOT NULL, -- 文本内容
    tags TEXT [], -- 标签
    version NUMERIC(5, 2) NOT NULL DEFAULT 1.00, -- 版本号，默认1
    create_time TIMESTAMP NOT NULL DEFAULT NOW(), -- 创建时间
    create_user VARCHAR(100), -- 创建用户
    update_time TIMESTAMP NOT NULL DEFAULT NOW(), -- 更新时间
    update_user VARCHAR(100) -- 最后更新用户
);

-- 字段注释
COMMENT ON COLUMN wiki.wiki_id IS 'Wiki ID，自增主键';

COMMENT ON COLUMN wiki.key_name IS 'Wiki键名（建议唯一）';

COMMENT ON COLUMN wiki.texts IS 'Wiki内容文本';

COMMENT ON COLUMN wiki.tags IS '标签';

COMMENT ON COLUMN wiki.version IS '版本号';

COMMENT ON COLUMN wiki.create_time IS '创建时间';

COMMENT ON COLUMN wiki.create_user IS '创建用户id';

COMMENT ON COLUMN wiki.update_time IS '更新时间';

COMMENT ON COLUMN wiki.update_user IS '更新用户id';

-- 表注释
COMMENT ON TABLE wiki IS 'Wiki内容表';

-- #######################################################
-- 创建 wiki审核表                                        已创建
CREATE TABLE wiki_review (
    wikireview_id BIGSERIAL PRIMARY KEY, -- 自增主键
    wiki_id BIGINT NOT NULL REFERENCES wiki (wiki_id) ON DELETE CASCADE, -- 关联wiki表
    texts TEXT NOT NULL, -- 文本内容
    tags TEXT [], -- 标签
    version NUMERIC(5, 2) NOT NULL DEFAULT 1.00, -- 版本号，默认1
    update_time TIMESTAMP NOT NULL DEFAULT NOW(), -- 更新时间
    update_user VARCHAR(100), -- 最后更新用户
    wiki_states INTEGER NOT NULL DEFAULT 0 -- 审核状态，0待审核，1通过，2拒绝
);

-- 字段注释
COMMENT ON COLUMN wiki_review.wikireview_id IS 'Wiki审核ID，自增主键';

COMMENT ON COLUMN wiki_review.wiki_id IS '关联的Wiki ID';

COMMENT ON COLUMN wiki_review.texts IS 'Wiki审核内容文本';

COMMENT ON COLUMN wiki_review.tags IS '标签';

COMMENT ON COLUMN wiki_review.version IS '版本号';

COMMENT ON COLUMN wiki_review.update_time IS '更新时间';

COMMENT ON COLUMN wiki_review.update_user IS '更新用户id';

COMMENT ON COLUMN wiki_review.wiki_states IS '审核状态，0待审核，1通过，2拒绝';

-- 表注释
COMMENT ON TABLE wiki_review IS 'Wiki审核修改内容表';

-- #########################################################
-- 创建wiki新增表                                            已创建
CREATE TABLE wiki_new (
    wikinew_id BIGSERIAL PRIMARY KEY, -- 自增主键
    key_name VARCHAR(200) NOT NULL UNIQUE, -- 唯一键名（必须唯一）
    texts TEXT NOT NULL, -- 文本内容
    tags TEXT [], -- 标签
    version NUMERIC(5, 2) NOT NULL DEFAULT 1.00, -- 版本号，默认1
    create_time TIMESTAMP NOT NULL DEFAULT NOW(), -- 创建时间
    create_user VARCHAR(100), -- 创建用户
    update_time TIMESTAMP NOT NULL DEFAULT NOW(), -- 更新时间
    update_user VARCHAR(100), -- 最后更新用户
    wiki_states INTEGER NOT NULL DEFAULT 0 -- 审核状态，0待审核，1通过，2拒绝
);

-- 字段注释
COMMENT ON COLUMN wiki_new.wikinew_id IS 'Wiki ID，自增主键';

COMMENT ON COLUMN wiki_new.key_name IS 'Wiki键名（建议唯一）';

COMMENT ON COLUMN wiki_new.texts IS 'Wiki内容文本';

COMMENT ON COLUMN wiki_new.tags IS '标签';

COMMENT ON COLUMN wiki_new.version IS '版本号';

COMMENT ON COLUMN wiki_new.create_time IS '创建时间';

COMMENT ON COLUMN wiki_new.create_user IS '创建用户id';

COMMENT ON COLUMN wiki_new.update_time IS '更新时间';

COMMENT ON COLUMN wiki_new.update_user IS '更新用户id';

COMMENT ON COLUMN wiki_new.wiki_states IS '审核状态，0待审核，1通过，2拒绝';

-- 表注释
COMMENT ON TABLE wiki_new IS 'Wiki审核新增内容表';

-- #########################################################
-- 创建 wiki 历史表                                       已创建
CREATE TABLE wiki_history (
    history_id BIGSERIAL PRIMARY KEY, -- 历史记录ID，自增主键
    wiki_id BIGINT NOT NULL, -- 来源 wiki 主表ID（外键）
    key_name VARCHAR(200) NOT NULL, -- 不唯一
    texts TEXT NOT NULL, -- 文本内容
    tags TEXT [], -- 标签
    version NUMERIC(5, 2) NOT NULL, -- 历史版本号
    create_time TIMESTAMP NOT NULL, -- 原创建时间
    create_user VARCHAR(100), -- 原创建用户
    update_time TIMESTAMP NOT NULL, -- 原更新时间
    update_user VARCHAR(100), -- 原更新用户
    backup_time TIMESTAMP NOT NULL DEFAULT NOW() -- 备份时间（历史记录创建时间）
);

-- 字段注释
COMMENT ON COLUMN wiki_history.history_id IS '历史记录ID，自增主键';

COMMENT ON COLUMN wiki_history.wiki_id IS '来源wiki主表ID';

COMMENT ON COLUMN wiki_history.key_name IS 'Wiki键名（可重复）';

COMMENT ON COLUMN wiki_history.texts IS '历史Wiki内容文本';

COMMENT ON COLUMN wiki_history.tags IS '历史标签';

COMMENT ON COLUMN wiki_history.version IS '历史版本号';

COMMENT ON COLUMN wiki_history.create_time IS '原创建时间';

COMMENT ON COLUMN wiki_history.create_user IS '原创建用户id';

COMMENT ON COLUMN wiki_history.update_time IS '原更新时间';

COMMENT ON COLUMN wiki_history.update_user IS '原更新用户id';

COMMENT ON COLUMN wiki_history.backup_time IS '备份入历史表的时间';

-- 表注释
COMMENT ON TABLE wiki_history IS 'Wiki内容历史记录表';

-- #########################################################
-- 创建表 spacex_lunch
CREATE TABLE spacex_lunch (
    lunch_id BIGSERIAL PRIMARY KEY, -- 自增主键
    link VARCHAR(500), -- 链接
    launch_site VARCHAR(200), -- 发射地点
    launch_date TIMESTAMP, -- 发射日期时间
    vehicle VARCHAR(200), -- 运载工具
    title VARCHAR(300), -- 发射任务标题
    url_s VARCHAR(500) -- 任务相关的备用URL或缩略图链接
);

-- 字段注释
COMMENT ON COLUMN spacex_lunch.lunch_id IS '发射ID，自增主键';

COMMENT ON COLUMN spacex_lunch.link IS '链接';

COMMENT ON COLUMN spacex_lunch.launch_site IS '发射地点';

COMMENT ON COLUMN spacex_lunch.launch_date IS '发射日期时间';

COMMENT ON COLUMN spacex_lunch.vehicle IS '舰体';

COMMENT ON COLUMN spacex_lunch.title IS '标题';

COMMENT ON COLUMN spacex_lunch.url_s IS 'URL链接';

COMMENT ON COLUMN spacex_lunch.create_time IS '创建时间';

-- 表注释
COMMENT ON TABLE spacex_lunch IS '空叉航天任务表';

-- #########################################################
-- 创建表 nasa_daily_image                                 已创建
CREATE TABLE nasa_daily_image (
    apod_id BIGSERIAL PRIMARY KEY, -- 自增主键
    copyright VARCHAR(200), -- 图片大标题
    explanation TEXT NOT NULL, -- 图片说明文字
    media_type VARCHAR(50) NOT NULL, -- 媒体类型（image 或 video）
    title VARCHAR(300) NOT NULL, -- 图片标题
    url VARCHAR(500) NOT NULL, -- 图片或视频的链接
    create_time TIMESTAMP NOT NULL DEFAULT NOW() -- 创建时间
);

-- 字段注释
COMMENT ON COLUMN nasa_daily_image.apod_id IS '图片记录ID，自增主键';

COMMENT ON COLUMN nasa_daily_image.copyright IS '图片大标题';

COMMENT ON COLUMN nasa_daily_image.explanation IS '图片说明文字，来自NASA APOD接口';

COMMENT ON COLUMN nasa_daily_image.media_type IS '媒体类型（如 image 或 video）';

COMMENT ON COLUMN nasa_daily_image.title IS '每日图片标题';

COMMENT ON COLUMN nasa_daily_image.url IS '图片或视频的访问链接';

COMMENT ON COLUMN nasa_daily_image.create_time IS '记录创建时间';

-- 表注释
COMMENT ON TABLE nasa_daily_image IS 'APOD每日图片信息表';

-- #########################################################
-- 创建表 api_raw_logs                                         已创建
CREATE TABLE api_raw_logs (
    api_raw_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    raw_json JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE api_raw_logs IS 'API原始日志存储表';

COMMENT ON COLUMN api_raw_logs.api_raw_id IS '自增主键';

COMMENT ON COLUMN api_raw_logs.raw_json IS '原始JSON内容';

COMMENT ON COLUMN api_raw_logs.created_at IS '记录创建时间';
-- 表注释
COMMENT ON TABLE "api_raw_logs" IS 'API原始日志存储表';

-- #########################################################
-- 创建表 falcon_stats                            已创建
CREATE TABLE falcon_stats (
    falcon_id BIGSERIAL PRIMARY KEY, -- 自增主键                   
    document_id TEXT NOT NULL, -- SpaceX 文档 ID
    total_launches INTEGER NOT NULL, -- 发射次数
    total_landings INTEGER NOT NULL, -- 着陆次数
    total_reflights INTEGER NOT NULL, -- 复用次数
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP -- 本地插入时间
);

-- 为字段添加注释（PostgreSQL 原生 COMMENT）
COMMENT ON TABLE falcon_stats IS 'SpaceX Falcon 系列火箭统计数据表';

COMMENT ON COLUMN falcon_stats.document_id IS 'SpaceX API 返回的 documentId';

COMMENT ON COLUMN falcon_stats.total_launches IS '总发射次数';

COMMENT ON COLUMN falcon_stats.total_landings IS '总着陆次数';

COMMENT ON COLUMN falcon_stats.total_reflights IS '总复用次数';

COMMENT ON COLUMN falcon_stats.created_at IS '数据写入数据库的本地时间';

-- #########################################################

-- #########################################################