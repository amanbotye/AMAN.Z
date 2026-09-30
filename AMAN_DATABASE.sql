-- =============================================================================
-- AMAN | أمان — DATABASE SCHEMA (AMAN_DATABASE.sql)
-- النسخة التنفيذية المعتمدة لقاعدة البيانات المشتركة لتطبيقي العميل والإدارة
-- =============================================================================

-- تفعيل الامتدادات المطلوبة
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- 1. الأنواع والتصنيفات (ENUMS & CUSTOM TYPES)
-- =============================================================================

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_type_enum') THEN
        CREATE TYPE user_type_enum AS ENUM ('admin', 'customer');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_status_enum') THEN
        CREATE TYPE user_status_enum AS ENUM ('active', 'suspended', 'disabled');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_method_type_enum') THEN
        CREATE TYPE payment_method_type_enum AS ENUM ('wallet', 'bank_transfer', 'cash', 'other');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'request_type_enum') THEN
        CREATE TYPE request_type_enum AS ENUM ('new_protection', 'renewal');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'request_status_enum') THEN
        CREATE TYPE request_status_enum AS ENUM ('pending', 'approved', 'rejected', 'conflict', 'cancelled');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_transaction_status_enum') THEN
        CREATE TYPE payment_transaction_status_enum AS ENUM ('pending', 'verified', 'rejected');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'protection_status_enum') THEN
        CREATE TYPE protection_status_enum AS ENUM ('active', 'expired', 'renewed', 'cancelled');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_status_enum') THEN
        CREATE TYPE task_status_enum AS ENUM ('due', 'completed');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'notification_type_enum') THEN
        CREATE TYPE notification_type_enum AS ENUM (
            'request_submitted', 'request_approved', 'request_rejected',
            'protection_started', 'protection_expiring', 'protection_expired',
            'renewal_submitted', 'renewal_approved', 'renewal_rejected', 'system'
        );
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'financial_tx_type_enum') THEN
        CREATE TYPE financial_tx_type_enum AS ENUM ('income', 'expense');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'financial_tx_status_enum') THEN
        CREATE TYPE financial_tx_status_enum AS ENUM ('posted', 'voided');
    END IF;
END $$;

-- =============================================================================
-- 2. جداول الهوية والأمان والصلاحيات (IDENTITY & SECURITY)
-- =============================================================================

-- جدول الأدوار الإدارية والأمنية
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- جدول الصلاحيات
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    module TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- جدول ربط الأدوار بالصلاحيات
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- جدول المستخدمين المركزي (users)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_id UUID UNIQUE,
    user_type user_type_enum NOT NULL DEFAULT 'customer',
    full_name TEXT NOT NULL,
    username TEXT UNIQUE,
    username_normalized TEXT UNIQUE,
    email TEXT UNIQUE NOT NULL,
    phone TEXT,
    status user_status_enum NOT NULL DEFAULT 'active',
    role_id UUID REFERENCES roles(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =============================================================================
-- 3. جداول الاتصالات والشركات والأرقام (TELECOM & PHONE NUMBERS)
-- =============================================================================

-- جدول شركات الاتصالات (telecom_providers)
CREATE TABLE IF NOT EXISTS telecom_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT UNIQUE NOT NULL,
    code TEXT UNIQUE NOT NULL,
    number_length INTEGER NOT NULL CHECK (number_length > 0),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- جدول بادئات شركات الاتصالات (provider_prefixes)
CREATE TABLE IF NOT EXISTS provider_prefixes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id UUID NOT NULL REFERENCES telecom_providers(id) ON DELETE RESTRICT,
    prefix TEXT UNIQUE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- جدول الأرقام المركزية (phone_numbers)
CREATE TABLE IF NOT EXISTS phone_numbers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    normalized_number TEXT UNIQUE NOT NULL,
    display_number TEXT NOT NULL,
    provider_id UUID NOT NULL REFERENCES telecom_providers(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- جدول علاقة العميل بالأرقام (customer_numbers)
CREATE TABLE IF NOT EXISTS customer_numbers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    phone_number_id UUID NOT NULL REFERENCES phone_numbers(id) ON DELETE RESTRICT,
    custom_label TEXT,
    added_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_customer_phone UNIQUE (customer_id, phone_number_id)
);

-- =============================================================================
-- 4. جداول الخدمة والدفع (PACKAGES & PAYMENT METHODS)
-- =============================================================================

-- جدول باقات الحماية (packages)
CREATE TABLE IF NOT EXISTS packages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id UUID REFERENCES telecom_providers(id) ON DELETE RESTRICT,
    name TEXT NOT NULL,
    description TEXT,
    duration_days INTEGER NOT NULL CHECK (duration_days > 0),
    price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    currency TEXT NOT NULL DEFAULT 'YER',
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_visible BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- جدول وسائل الدفع (payment_methods)
CREATE TABLE IF NOT EXISTS payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type payment_method_type_enum NOT NULL DEFAULT 'wallet',
    name TEXT NOT NULL,
    recipient_name TEXT NOT NULL,
    account_number TEXT NOT NULL,
    instructions TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =============================================================================
-- 5. جداول الحماية والطلبات وإثبات الدفع (PROTECTION & REQUESTS)
-- =============================================================================

-- جدول الحمايات الفعالة والتاريخية (protections)
CREATE TABLE IF NOT EXISTS protections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    phone_number_id UUID NOT NULL REFERENCES phone_numbers(id) ON DELETE RESTRICT,
    request_id UUID, -- يُربط لاحقاً بجدول الطلبات
    package_id UUID NOT NULL REFERENCES packages(id) ON DELETE RESTRICT,
    provider_id UUID NOT NULL REFERENCES telecom_providers(id) ON DELETE RESTRICT,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    status protection_status_enum NOT NULL DEFAULT 'active',
    -- Snapshots المحفوظة وقت إنشاء الحماية
    package_name_snapshot TEXT NOT NULL,
    package_duration_days_snapshot INTEGER NOT NULL,
    package_price_snapshot NUMERIC(12,2) NOT NULL,
    package_currency_snapshot TEXT NOT NULL,
    provider_name_snapshot TEXT NOT NULL,
    provider_code_snapshot TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- قيد: حماية واحدة فعالة فقط للرقم الواحد
CREATE UNIQUE INDEX IF NOT EXISTS idx_protections_single_active_per_phone
ON protections (phone_number_id) WHERE (status = 'active');

-- جدول طلبات الحماية والتجديد (protection_requests)
CREATE TABLE IF NOT EXISTS protection_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    phone_number_id UUID NOT NULL REFERENCES phone_numbers(id) ON DELETE RESTRICT,
    provider_id UUID NOT NULL REFERENCES telecom_providers(id) ON DELETE RESTRICT,
    package_id UUID NOT NULL REFERENCES packages(id) ON DELETE RESTRICT,
    payment_method_id UUID NOT NULL REFERENCES payment_methods(id) ON DELETE RESTRICT,
    request_type request_type_enum NOT NULL DEFAULT 'new_protection',
    previous_protection_id UUID REFERENCES protections(id) ON DELETE SET NULL,
    parent_request_id UUID REFERENCES protection_requests(id) ON DELETE SET NULL,
    payment_reference TEXT NOT NULL,
    status request_status_enum NOT NULL DEFAULT 'pending',
    rejection_reason TEXT,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    reviewed_at TIMESTAMPTZ,
    reviewed_by UUID REFERENCES users(id) ON DELETE SET NULL,
    -- Snapshots وقت تقديم الطلب
    package_name_snapshot TEXT NOT NULL,
    package_duration_days_snapshot INTEGER NOT NULL,
    package_price_snapshot NUMERIC(12,2) NOT NULL,
    package_currency_snapshot TEXT NOT NULL,
    provider_name_snapshot TEXT NOT NULL,
    provider_code_snapshot TEXT NOT NULL,
    payment_method_type_snapshot TEXT NOT NULL,
    payment_method_name_snapshot TEXT NOT NULL,
    payment_recipient_snapshot TEXT NOT NULL,
    payment_account_snapshot TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ربط المفتاح الأجنبي لـ request_id في جدول protections
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_protections_request'
    ) THEN
        ALTER TABLE protections
        ADD CONSTRAINT fk_protections_request
        FOREIGN KEY (request_id) REFERENCES protection_requests(id) ON DELETE RESTRICT;
    END IF;
END $$;

-- جدول معاملات إثبات الدفع (payment_transactions)
CREATE TABLE IF NOT EXISTS payment_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID NOT NULL REFERENCES protection_requests(id) ON DELETE RESTRICT,
    customer_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    payment_method_id UUID NOT NULL REFERENCES payment_methods(id) ON DELETE RESTRICT,
    amount NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    currency TEXT NOT NULL DEFAULT 'YER',
    reference TEXT NOT NULL,
    status payment_transaction_status_enum NOT NULL DEFAULT 'pending',
    verified_by UUID REFERENCES users(id) ON DELETE SET NULL,
    verified_at TIMESTAMPTZ,
    rejection_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- سجل التحقق اليدوي من الدفع (manual_payment_logs)
CREATE TABLE IF NOT EXISTS manual_payment_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_transaction_id UUID NOT NULL REFERENCES payment_transactions(id) ON DELETE RESTRICT,
    verified_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    verification_status TEXT NOT NULL CHECK (verification_status IN ('verified', 'rejected')),
    notes TEXT,
    verified_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- جدول تاريخ الحماية (protection_history)
CREATE TABLE IF NOT EXISTS protection_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    protection_id UUID NOT NULL REFERENCES protections(id) ON DELETE RESTRICT,
    customer_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    phone_number_id UUID NOT NULL REFERENCES phone_numbers(id) ON DELETE RESTRICT,
    request_id UUID REFERENCES protection_requests(id) ON DELETE SET NULL,
    event_type TEXT NOT NULL,
    package_snapshot JSONB NOT NULL,
    provider_snapshot JSONB NOT NULL,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =============================================================================
-- 6. جداول التشغيل والمهام (OPERATIONS & TASKS)
-- =============================================================================

-- جدول إعدادات المهام للشركات (task_settings)
CREATE TABLE IF NOT EXISTS task_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id UUID UNIQUE NOT NULL REFERENCES telecom_providers(id) ON DELETE RESTRICT,
    first_task_enabled BOOLEAN NOT NULL DEFAULT true,
    first_task_amount NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (first_task_amount >= 0),
    recurring_tasks_enabled BOOLEAN NOT NULL DEFAULT true,
    recurring_task_amount NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (recurring_task_amount >= 0),
    repeat_interval_days INTEGER NOT NULL DEFAULT 30 CHECK (repeat_interval_days > 0),
    days_visible_before_due INTEGER NOT NULL DEFAULT 7 CHECK (days_visible_before_due >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- جدول تصنيفات توقيت المهام (task_time_classifications)
CREATE TABLE IF NOT EXISTS task_time_classifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_settings_id UUID NOT NULL REFERENCES task_settings(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    min_days_remaining INTEGER NOT NULL,
    max_days_remaining INTEGER NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- جدول خطط المهام الدورية (periodic_payment_plans)
CREATE TABLE IF NOT EXISTS periodic_payment_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    protection_id UUID NOT NULL REFERENCES protections(id) ON DELETE RESTRICT,
    provider_id UUID NOT NULL REFERENCES telecom_providers(id) ON DELETE RESTRICT,
    interval_days INTEGER NOT NULL CHECK (interval_days > 0),
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- جدول المهام التشغيلية (payment_tasks)
CREATE TABLE IF NOT EXISTS payment_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    protection_id UUID NOT NULL REFERENCES protections(id) ON DELETE RESTRICT,
    periodic_payment_plan_id UUID NOT NULL REFERENCES periodic_payment_plans(id) ON DELETE RESTRICT,
    provider_id UUID NOT NULL REFERENCES telecom_providers(id) ON DELETE RESTRICT,
    sequence_no INTEGER NOT NULL DEFAULT 1,
    due_at TIMESTAMPTZ NOT NULL,
    original_due_at TIMESTAMPTZ NOT NULL,
    status task_status_enum NOT NULL DEFAULT 'due',
    amount NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (amount >= 0),
    rescheduled_at TIMESTAMPTZ,
    rescheduled_by UUID REFERENCES users(id) ON DELETE SET NULL,
    reschedule_reason TEXT,
    completed_at TIMESTAMPTZ,
    completed_by UUID REFERENCES users(id) ON DELETE SET NULL,
    result TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =============================================================================
-- 7. جداول الإشعارات والمالية وإعدادات النظام والتدقيق
-- =============================================================================

-- جدول إشعارات العميل (client_notifications)
CREATE TABLE IF NOT EXISTS client_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type notification_type_enum NOT NULL,
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    related_type TEXT,
    related_id UUID,
    is_read BOOLEAN NOT NULL DEFAULT false,
    read_at TIMESTAMPTZ,
    deduplication_key TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- جدول السجل المالي الداخلي (financial_transactions)
CREATE TABLE IF NOT EXISTS financial_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tx_type financial_tx_type_enum NOT NULL,
    amount NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    currency TEXT NOT NULL DEFAULT 'YER',
    status financial_tx_status_enum NOT NULL DEFAULT 'posted',
    source_type TEXT NOT NULL, -- 'protection_request', 'renewal_request', 'payment_task', 'manual'
    source_id UUID,
    customer_id UUID REFERENCES users(id) ON DELETE SET NULL,
    phone_number_id UUID REFERENCES phone_numbers(id) ON DELETE SET NULL,
    payment_method_id UUID REFERENCES payment_methods(id) ON DELETE SET NULL,
    reference TEXT,
    description TEXT NOT NULL,
    metadata JSONB DEFAULT '{}'::jsonb,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    idempotency_key TEXT UNIQUE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- جدول إعدادات النظام المركزية (system_settings)
CREATE TABLE IF NOT EXISTS system_settings (
    key TEXT PRIMARY KEY,
    value JSONB NOT NULL,
    description TEXT,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- جدول سجل العمليات والتدقيق (audit_logs)
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id UUID,
    before_data JSONB,
    after_data JSONB,
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =============================================================================
-- 8. الفهارس (INDEXES)
-- =============================================================================

CREATE INDEX IF NOT EXISTS idx_users_auth_id ON users (auth_id);
CREATE INDEX IF NOT EXISTS idx_users_type_status ON users (user_type, status);
CREATE INDEX IF NOT EXISTS idx_users_normalized_username ON users (username_normalized);

CREATE INDEX IF NOT EXISTS idx_provider_prefixes_prefix ON provider_prefixes (prefix) WHERE (is_active = true);
CREATE INDEX IF NOT EXISTS idx_phone_numbers_normalized ON phone_numbers (normalized_number);

CREATE INDEX IF NOT EXISTS idx_customer_numbers_customer ON customer_numbers (customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_numbers_phone ON customer_numbers (phone_number_id);

CREATE INDEX IF NOT EXISTS idx_packages_provider ON packages (provider_id);
CREATE INDEX IF NOT EXISTS idx_packages_active_visible ON packages (is_active, is_visible);

CREATE INDEX IF NOT EXISTS idx_protection_requests_customer ON protection_requests (customer_id);
CREATE INDEX IF NOT EXISTS idx_protection_requests_phone ON protection_requests (phone_number_id);
CREATE INDEX IF NOT EXISTS idx_protection_requests_status ON protection_requests (status);
CREATE INDEX IF NOT EXISTS idx_protection_requests_type ON protection_requests (request_type);

CREATE INDEX IF NOT EXISTS idx_payment_transactions_request ON payment_transactions (request_id);
CREATE INDEX IF NOT EXISTS idx_payment_transactions_status ON payment_transactions (status);

CREATE INDEX IF NOT EXISTS idx_protections_customer ON protections (customer_id);
CREATE INDEX IF NOT EXISTS idx_protections_phone ON protections (phone_number_id);
CREATE INDEX IF NOT EXISTS idx_protections_status ON protections (status);
CREATE INDEX IF NOT EXISTS idx_protections_end_at ON protections (end_at);

CREATE INDEX IF NOT EXISTS idx_payment_tasks_due ON payment_tasks (due_at) WHERE (status = 'due');
CREATE INDEX IF NOT EXISTS idx_payment_tasks_protection ON payment_tasks (protection_id);
CREATE INDEX IF NOT EXISTS idx_payment_tasks_status ON payment_tasks (status);

CREATE INDEX IF NOT EXISTS idx_client_notifications_customer ON client_notifications (customer_id, is_read);
CREATE INDEX IF NOT EXISTS idx_client_notifications_dedup ON client_notifications (deduplication_key) WHERE (deduplication_key IS NOT NULL);

CREATE INDEX IF NOT EXISTS idx_financial_transactions_source ON financial_transactions (source_type, source_id);
CREATE INDEX IF NOT EXISTS idx_financial_transactions_idempotency ON financial_transactions (idempotency_key);

CREATE INDEX IF NOT EXISTS idx_audit_logs_actor ON audit_logs (actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs (entity_type, entity_id);

-- =============================================================================
-- 9. قيد منع حذف البيانات التاريخية (DELETE PREVENTION TRIGGERS)
-- =============================================================================

CREATE OR REPLACE FUNCTION fn_prevent_delete_historical()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'DELETE_NOT_ALLOWED_USE_DEACTIVATION: Cannot delete historical or configured system entity (table: %)', TG_TABLE_NAME;
END;
$$ LANGUAGE plpgsql;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_no_delete_telecom_providers') THEN
        CREATE TRIGGER trg_no_delete_telecom_providers
        BEFORE DELETE ON telecom_providers
        FOR EACH ROW EXECUTE FUNCTION fn_prevent_delete_historical();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_no_delete_packages') THEN
        CREATE TRIGGER trg_no_delete_packages
        BEFORE DELETE ON packages
        FOR EACH ROW EXECUTE FUNCTION fn_prevent_delete_historical();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_no_delete_payment_methods') THEN
        CREATE TRIGGER trg_no_delete_payment_methods
        BEFORE DELETE ON payment_methods
        FOR EACH ROW EXECUTE FUNCTION fn_prevent_delete_historical();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_no_delete_protections') THEN
        CREATE TRIGGER trg_no_delete_protections
        BEFORE DELETE ON protections
        FOR EACH ROW EXECUTE FUNCTION fn_prevent_delete_historical();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_no_delete_protection_requests') THEN
        CREATE TRIGGER trg_no_delete_protection_requests
        BEFORE DELETE ON protection_requests
        FOR EACH ROW EXECUTE FUNCTION fn_prevent_delete_historical();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_no_delete_payment_transactions') THEN
        CREATE TRIGGER trg_no_delete_payment_transactions
        BEFORE DELETE ON payment_transactions
        FOR EACH ROW EXECUTE FUNCTION fn_prevent_delete_historical();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_no_delete_payment_tasks') THEN
        CREATE TRIGGER trg_no_delete_payment_tasks
        BEFORE DELETE ON payment_tasks
        FOR EACH ROW EXECUTE FUNCTION fn_prevent_delete_historical();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_no_delete_audit_logs') THEN
        CREATE TRIGGER trg_no_delete_audit_logs
        BEFORE DELETE ON audit_logs
        FOR EACH ROW EXECUTE FUNCTION fn_prevent_delete_historical();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_no_delete_financial_transactions') THEN
        CREATE TRIGGER trg_no_delete_financial_transactions
        BEFORE DELETE ON financial_transactions
        FOR EACH ROW EXECUTE FUNCTION fn_prevent_delete_historical();
    END IF;
END $$;

-- =============================================================================
-- 10. الرؤى المعتمدة (VIEWS)
-- =============================================================================

-- رؤية تفاصيل أرقام العميل مع حالة الحماية الحالية
CREATE OR REPLACE VIEW v_customer_numbers_details AS
SELECT 
    cn.id AS customer_number_id,
    cn.customer_id,
    cn.custom_label,
    cn.added_at,
    cn.is_active AS customer_number_active,
    pn.id AS phone_number_id,
    pn.normalized_number,
    pn.display_number,
    tp.id AS provider_id,
    tp.name AS provider_name,
    tp.code AS provider_code,
    p.id AS protection_id,
    p.status AS protection_status,
    p.start_at AS protection_start_at,
    p.end_at AS protection_end_at,
    CASE 
        WHEN p.id IS NOT NULL AND p.status = 'active' AND p.end_at > now() THEN true
        ELSE false
    END AS is_protected,
    CASE 
        WHEN p.id IS NOT NULL AND p.status = 'active' AND p.end_at > now() THEN 
            GREATEST(0, EXTRACT(DAY FROM (p.end_at - now()))::INTEGER)
        ELSE 0
    END AS days_remaining,
    p.package_name_snapshot,
    p.package_price_snapshot,
    p.package_currency_snapshot
FROM customer_numbers cn
JOIN phone_numbers pn ON cn.phone_number_id = pn.id
JOIN telecom_providers tp ON pn.provider_id = tp.id
LEFT JOIN protections p ON p.phone_number_id = pn.id AND p.status = 'active';

-- رؤية تفاصيل طلبات الحماية مع جميع البيانات المرتبطة
CREATE OR REPLACE VIEW v_protection_requests_details AS
SELECT 
    pr.id AS request_id,
    pr.customer_id,
    u.full_name AS customer_name,
    u.email AS customer_email,
    u.phone AS customer_phone,
    pr.phone_number_id,
    pn.normalized_number,
    pn.display_number,
    pr.provider_id,
    pr.provider_name_snapshot,
    pr.provider_code_snapshot,
    pr.package_id,
    pr.package_name_snapshot,
    pr.package_duration_days_snapshot,
    pr.package_price_snapshot,
    pr.package_currency_snapshot,
    pr.payment_method_id,
    pr.payment_method_name_snapshot,
    pr.payment_method_type_snapshot,
    pr.payment_recipient_snapshot,
    pr.payment_account_snapshot,
    pr.payment_reference,
    pr.request_type,
    pr.previous_protection_id,
    pr.parent_request_id,
    pr.status AS request_status,
    pr.rejection_reason,
    pr.submitted_at,
    pr.reviewed_at,
    pr.reviewed_by,
    reviewer.full_name AS reviewer_name,
    pt.id AS payment_transaction_id,
    pt.status AS payment_status,
    pt.amount AS payment_amount,
    pt.currency AS payment_currency
FROM protection_requests pr
JOIN users u ON pr.customer_id = u.id
JOIN phone_numbers pn ON pr.phone_number_id = pn.id
LEFT JOIN users reviewer ON pr.reviewed_by = reviewer.id
LEFT JOIN payment_transactions pt ON pt.request_id = pr.id;

-- رؤية المهام التشغيلية المصنفة زمنياً
CREATE OR REPLACE VIEW v_payment_tasks_details AS
SELECT 
    pt.id AS task_id,
    pt.protection_id,
    pt.periodic_payment_plan_id,
    pt.provider_id,
    tp.name AS provider_name,
    tp.code AS provider_code,
    pn.id AS phone_number_id,
    pn.normalized_number,
    pn.display_number,
    u.id AS customer_id,
    u.full_name AS customer_name,
    pt.sequence_no,
    pt.due_at,
    pt.original_due_at,
    pt.status AS task_status,
    pt.amount,
    pt.rescheduled_at,
    pt.rescheduled_by,
    pt.reschedule_reason,
    pt.completed_at,
    pt.completed_by,
    pt.result,
    ts.days_visible_before_due,
    -- تصنيف الوقت المشتق للعرض (due today, upcoming, overdue, completed)
    CASE 
        WHEN pt.status = 'completed' THEN 'completed'
        WHEN pt.due_at < now() THEN 'overdue'
        WHEN DATE(pt.due_at) = CURRENT_DATE THEN 'today'
        ELSE 'upcoming'
    END AS time_classification,
    -- هل المهمة ضمن نافذة الرؤية
    CASE 
        WHEN pt.status = 'completed' THEN true
        WHEN pt.due_at <= (now() + (COALESCE(ts.days_visible_before_due, 7) * INTERVAL '1 day')) THEN true
        ELSE false
    END AS is_visible_in_window
FROM payment_tasks pt
JOIN protections p ON pt.protection_id = p.id
JOIN phone_numbers pn ON p.phone_number_id = pn.id
JOIN users u ON p.customer_id = u.id
JOIN telecom_providers tp ON pt.provider_id = tp.id
LEFT JOIN task_settings ts ON ts.provider_id = pt.provider_id;

-- =============================================================================
-- 11. العمليات الموثوقة والدوال التشغيلية (TRUSTED RPC FUNCTIONS)
-- =============================================================================

-- دالة مساعدة للتحقق من هوية المدير
CREATE OR REPLACE FUNCTION fn_is_admin(p_user_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM users 
        WHERE id = p_user_id 
          AND user_type = 'admin' 
          AND status = 'active'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 11.1 دالة إضافة رقم لعميل (rpc_add_customer_number)
CREATE OR REPLACE FUNCTION rpc_add_customer_number(
    p_phone_number TEXT,
    p_custom_label TEXT DEFAULT NULL
)
RETURNS JSONB AS $$
DECLARE
    v_user_id UUID;
    v_normalized TEXT;
    v_provider RECORD;
    v_phone_id UUID;
    v_customer_number_id UUID;
BEGIN
    -- التحقق من المستخدم الحالي (من سياق auth أو المعرف)
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'UNAUTHORIZED: Authentication session required';
    END IF;

    -- التحقق من حالة المستخدم
    IF NOT EXISTS (SELECT 1 FROM users WHERE id = v_user_id AND status = 'active') THEN
        RAISE EXCEPTION 'UNAUTHORIZED: User account is inactive or not found';
    END IF;

    -- تنظيف وتطبيع الرقم
    v_normalized := regexp_replace(trim(p_phone_number), '[^0-9]', '', 'g');
    IF length(v_normalized) = 0 THEN
        RAISE EXCEPTION 'INVALID_NUMBER: Phone number cannot be empty';
    END IF;

    -- اكتشاف الشركة من البادئة
    SELECT tp.id, tp.name, tp.number_length INTO v_provider
    FROM provider_prefixes pp
    JOIN telecom_providers tp ON pp.provider_id = tp.id
    WHERE pp.is_active = true
      AND tp.is_active = true
      AND v_normalized LIKE (pp.prefix || '%')
    ORDER BY length(pp.prefix) DESC
    LIMIT 1;

    IF v_provider.id IS NULL THEN
        RAISE EXCEPTION 'UNSUPPORTED_PREFIX: No active provider matches this phone number prefix';
    END IF;

    -- التحقق من طول الرقم
    IF length(v_normalized) != v_provider.number_length THEN
        RAISE EXCEPTION 'INVALID_LENGTH: Expected length % but got %', v_provider.number_length, length(v_normalized);
    END IF;

    -- الحصول على سجل الرقم المركزي أو إنشاؤه
    INSERT INTO phone_numbers (normalized_number, display_number, provider_id)
    VALUES (v_normalized, v_normalized, v_provider.id)
    ON CONFLICT (normalized_number) DO UPDATE
    SET updated_at = now()
    RETURNING id INTO v_phone_id;

    -- التحقق من عدم التكرار لنفس العميل
    IF EXISTS (
        SELECT 1 FROM customer_numbers 
        WHERE customer_id = v_user_id 
          AND phone_number_id = v_phone_id
    ) THEN
        RAISE EXCEPTION 'DUPLICATE_NUMBER: This number is already added to your account';
    END IF;

    -- إضافة الرقم للعميل
    INSERT INTO customer_numbers (customer_id, phone_number_id, custom_label, is_active)
    VALUES (v_user_id, v_phone_id, trim(p_custom_label), true)
    RETURNING id INTO v_customer_number_id;

    RETURN jsonb_build_object(
        'success', true,
        'customer_number_id', v_customer_number_id,
        'phone_number_id', v_phone_id,
        'normalized_number', v_normalized,
        'provider_id', v_provider.id,
        'provider_name', v_provider.name
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 11.2 دالة إنشاء طلب حماية جديد (rpc_create_protection_request)
CREATE OR REPLACE FUNCTION rpc_create_protection_request(
    p_customer_number_id UUID,
    p_package_id UUID,
    p_payment_method_id UUID,
    p_payment_reference TEXT
)
RETURNS JSONB AS $$
DECLARE
    v_user_id UUID;
    v_cn RECORD;
    v_pkg RECORD;
    v_pm RECORD;
    v_request_id UUID;
    v_tx_id UUID;
    v_clean_ref TEXT;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'UNAUTHORIZED: Authentication session required';
    END IF;

    v_clean_ref := trim(p_payment_reference);
    IF length(v_clean_ref) = 0 THEN
        RAISE EXCEPTION 'INVALID_REFERENCE: Payment reference cannot be empty';
    END IF;

    -- التحقق من ملكية الرقم
    SELECT cn.id, cn.customer_id, cn.phone_number_id, pn.normalized_number, tp.id AS provider_id, tp.name AS provider_name, tp.code AS provider_code
    INTO v_cn
    FROM customer_numbers cn
    JOIN phone_numbers pn ON cn.phone_number_id = pn.id
    JOIN telecom_providers tp ON pn.provider_id = tp.id
    WHERE cn.id = p_customer_number_id AND cn.customer_id = v_user_id AND cn.is_active = true;

    IF v_cn.id IS NULL THEN
        RAISE EXCEPTION 'FORBIDDEN: Number not found or not owned by you';
    END IF;

    -- التحقق من عدم وجود حماية فعالة للرقم
    IF EXISTS (
        SELECT 1 FROM protections 
        WHERE phone_number_id = v_cn.phone_number_id 
          AND status = 'active'
    ) THEN
        RAISE EXCEPTION 'CONFLICT_ACTIVE_PROTECTION: This number already has an active protection';
    END IF;

    -- ملاحظة: يُسمح بوجود أكثر من طلب معلق (pending) لنفس الرقم وفق قواعد النظام،
    -- ولا يُمنع إنشاء الطلب لمجرد وجود طلب آخر معلق؛ وحسم التعارض يكون حصرياً عند عملية القبول الذري (First valid atomic acceptance wins).

    -- التحقق من الباقة
    SELECT id, name, duration_days, price, currency, is_active, is_visible
    INTO v_pkg
    FROM packages
    WHERE id = p_package_id AND is_active = true;

    IF v_pkg.id IS NULL THEN
        RAISE EXCEPTION 'PACKAGE_UNAVAILABLE: Package not found or inactive';
    END IF;

    -- التحقق من وسيلة الدفع
    SELECT id, type, name, recipient_name, account_number, is_active
    INTO v_pm
    FROM payment_methods
    WHERE id = p_payment_method_id AND is_active = true;

    IF v_pm.id IS NULL THEN
        RAISE EXCEPTION 'PAYMENT_METHOD_UNAVAILABLE: Payment method not found or inactive';
    END IF;

    -- إنشاء طلب الحماية مع أخذ لقطة البيانات (Snapshots)
    INSERT INTO protection_requests (
        customer_id, phone_number_id, provider_id, package_id, payment_method_id,
        request_type, payment_reference, status,
        package_name_snapshot, package_duration_days_snapshot, package_price_snapshot, package_currency_snapshot,
        provider_name_snapshot, provider_code_snapshot,
        payment_method_type_snapshot, payment_method_name_snapshot, payment_recipient_snapshot, payment_account_snapshot
    ) VALUES (
        v_user_id, v_cn.phone_number_id, v_cn.provider_id, v_pkg.id, v_pm.id,
        'new_protection', v_clean_ref, 'pending',
        v_pkg.name, v_pkg.duration_days, v_pkg.price, v_pkg.currency,
        v_cn.provider_name, v_cn.provider_code,
        v_pm.type::TEXT, v_pm.name, v_pm.recipient_name, v_pm.account_number
    ) RETURNING id INTO v_request_id;

    -- إنشاء معاملة إثبات الدفع
    INSERT INTO payment_transactions (
        request_id, customer_id, payment_method_id, amount, currency, reference, status
    ) VALUES (
        v_request_id, v_user_id, v_pm.id, v_pkg.price, v_pkg.currency, v_clean_ref, 'pending'
    ) RETURNING id INTO v_tx_id;

    -- إنشاء إشعار للعميل
    INSERT INTO client_notifications (
        customer_id, type, title, body, related_type, related_id
    ) VALUES (
        v_user_id, 'request_submitted',
        'تم تقديم طلب الحماية',
        'تم استلام طلب الحماية لرقمك ' || v_cn.normalized_number || ' وهو قيد المراجعة حالياً.',
        'protection_request', v_request_id
    );

    RETURN jsonb_build_object(
        'success', true,
        'request_id', v_request_id,
        'status', 'pending',
        'phone_number', v_cn.normalized_number,
        'amount', v_pkg.price,
        'currency', v_pkg.currency
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 11.3 دالة إنشاء طلب تجديد حماية (rpc_create_renewal_request)
CREATE OR REPLACE FUNCTION rpc_create_renewal_request(
    p_protection_id UUID,
    p_package_id UUID,
    p_payment_method_id UUID,
    p_payment_reference TEXT
)
RETURNS JSONB AS $$
DECLARE
    v_user_id UUID;
    v_prot RECORD;
    v_pkg RECORD;
    v_pm RECORD;
    v_request_id UUID;
    v_tx_id UUID;
    v_clean_ref TEXT;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'UNAUTHORIZED: Authentication session required';
    END IF;

    v_clean_ref := trim(p_payment_reference);
    IF length(v_clean_ref) = 0 THEN
        RAISE EXCEPTION 'INVALID_REFERENCE: Payment reference cannot be empty';
    END IF;

    -- التحقق من الحماية وملكية العميل لها
    SELECT p.id, p.customer_id, p.phone_number_id, p.provider_id, p.status, p.end_at,
           pn.normalized_number, tp.name AS provider_name, tp.code AS provider_code
    INTO v_prot
    FROM protections p
    JOIN phone_numbers pn ON p.phone_number_id = pn.id
    JOIN telecom_providers tp ON p.provider_id = tp.id
    WHERE p.id = p_protection_id AND p.customer_id = v_user_id;

    IF v_prot.id IS NULL THEN
        RAISE EXCEPTION 'FORBIDDEN: Protection not found or you are not the owner';
    END IF;

    IF v_prot.status != 'active' THEN
        RAISE EXCEPTION 'INVALID_STATE: Protection is not active and cannot be renewed';
    END IF;

    -- التحقق من عدم وجود طلب تجديد معلق مسبقاً
    IF EXISTS (
        SELECT 1 FROM protection_requests 
        WHERE previous_protection_id = v_prot.id 
          AND request_type = 'renewal' 
          AND status = 'pending'
    ) THEN
        RAISE EXCEPTION 'DUPLICATE_REQUEST: A pending renewal request already exists for this protection';
    END IF;

    -- التحقق من الباقة
    SELECT id, name, duration_days, price, currency, is_active
    INTO v_pkg
    FROM packages
    WHERE id = p_package_id AND is_active = true;

    IF v_pkg.id IS NULL THEN
        RAISE EXCEPTION 'PACKAGE_UNAVAILABLE: Package not found or inactive';
    END IF;

    -- التحقق من وسيلة الدفع
    SELECT id, type, name, recipient_name, account_number, is_active
    INTO v_pm
    FROM payment_methods
    WHERE id = p_payment_method_id AND is_active = true;

    IF v_pm.id IS NULL THEN
        RAISE EXCEPTION 'PAYMENT_METHOD_UNAVAILABLE: Payment method not found or inactive';
    END IF;

    -- إنشاء طلب التجديد
    INSERT INTO protection_requests (
        customer_id, phone_number_id, provider_id, package_id, payment_method_id,
        request_type, previous_protection_id, payment_reference, status,
        package_name_snapshot, package_duration_days_snapshot, package_price_snapshot, package_currency_snapshot,
        provider_name_snapshot, provider_code_snapshot,
        payment_method_type_snapshot, payment_method_name_snapshot, payment_recipient_snapshot, payment_account_snapshot
    ) VALUES (
        v_user_id, v_prot.phone_number_id, v_prot.provider_id, v_pkg.id, v_pm.id,
        'renewal', v_prot.id, v_clean_ref, 'pending',
        v_pkg.name, v_pkg.duration_days, v_pkg.price, v_pkg.currency,
        v_prot.provider_name, v_prot.provider_code,
        v_pm.type::TEXT, v_pm.name, v_pm.recipient_name, v_pm.account_number
    ) RETURNING id INTO v_request_id;

    -- إنشاء معاملة إثبات الدفع
    INSERT INTO payment_transactions (
        request_id, customer_id, payment_method_id, amount, currency, reference, status
    ) VALUES (
        v_request_id, v_user_id, v_pm.id, v_pkg.price, v_pkg.currency, v_clean_ref, 'pending'
    ) RETURNING id INTO v_tx_id;

    -- إنشاء إشعار
    INSERT INTO client_notifications (
        customer_id, type, title, body, related_type, related_id
    ) VALUES (
        v_user_id, 'renewal_submitted',
        'تم تقديم طلب التجديد',
        'تم استلام طلب تجديد الحماية لرقمك ' || v_prot.normalized_number || ' وهو قيد المراجعة حالياً.',
        'protection_request', v_request_id
    );

    RETURN jsonb_build_object(
        'success', true,
        'request_id', v_request_id,
        'status', 'pending',
        'phone_number', v_prot.normalized_number,
        'current_end_at', v_prot.end_at
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 11.4 دالة قبول طلب الحماية الإدارية (rpc_approve_protection_request)
CREATE OR REPLACE FUNCTION rpc_approve_protection_request(
    p_request_id UUID
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_req RECORD;
    v_prot_id UUID;
    v_plan_id UUID;
    v_task_setting RECORD;
    v_start_at TIMESTAMPTZ;
    v_end_at TIMESTAMPTZ;
    v_first_task_due TIMESTAMPTZ;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    -- قفل الطلب لمنع التعارض الذري
    SELECT * INTO v_req
    FROM protection_requests
    WHERE id = p_request_id
    FOR UPDATE;

    IF v_req.id IS NULL THEN
        RAISE EXCEPTION 'NOT_FOUND: Protection request not found';
    END IF;

    IF v_req.status != 'pending' THEN
        RAISE EXCEPTION 'INVALID_STATE: Request is no longer pending (current status: %)', v_req.status;
    END IF;

    IF v_req.request_type != 'new_protection' THEN
        RAISE EXCEPTION 'INVALID_TYPE: This function handles new protection requests only';
    END IF;

    -- التحقق من عدم وجود حماية فعالة سابقة لنفس الرقم
    IF EXISTS (
        SELECT 1 FROM protections 
        WHERE phone_number_id = v_req.phone_number_id 
          AND status = 'active'
    ) THEN
        RAISE EXCEPTION 'CONFLICT_ACTIVE_PROTECTION: Phone number already has an active protection';
    END IF;

    v_start_at := now();
    v_end_at := v_start_at + (v_req.package_duration_days_snapshot * INTERVAL '1 day');

    -- إنشاء الحماية
    INSERT INTO protections (
        customer_id, phone_number_id, request_id, package_id, provider_id,
        start_at, end_at, status,
        package_name_snapshot, package_duration_days_snapshot, package_price_snapshot, package_currency_snapshot,
        provider_name_snapshot, provider_code_snapshot
    ) VALUES (
        v_req.customer_id, v_req.phone_number_id, v_req.id, v_req.package_id, v_req.provider_id,
        v_start_at, v_end_at, 'active',
        v_req.package_name_snapshot, v_req.package_duration_days_snapshot, v_req.package_price_snapshot, v_req.package_currency_snapshot,
        v_req.provider_name_snapshot, v_req.provider_code_snapshot
    ) RETURNING id INTO v_prot_id;

    -- تحديث حالة الطلب
    UPDATE protection_requests
    SET status = 'approved',
        reviewed_at = now(),
        reviewed_by = v_admin_id,
        updated_at = now()
    WHERE id = v_req.id;

    -- تحديث حالة إثبات الدفع
    UPDATE payment_transactions
    SET status = 'verified',
        verified_at = now(),
        verified_by = v_admin_id,
        updated_at = now()
    WHERE request_id = v_req.id;

    -- حسم التعارض ذرياً: إغلاق ورفض أي طلبات معلقة أخرى لنفس الرقم (First valid atomic acceptance wins)
    UPDATE protection_requests
    SET status = 'conflict',
        rejection_reason = 'تم تفعيل حماية معتمدة أخرى لهذا الرقم (حسم التعارض بالقبول الأول)',
        reviewed_at = now(),
        reviewed_by = v_admin_id,
        updated_at = now()
    WHERE phone_number_id = v_req.phone_number_id
      AND id != v_req.id
      AND status = 'pending';

    -- تحديث معاملات الدفع للطلبات المغلقة بالتعارض
    UPDATE payment_transactions
    SET status = 'rejected',
        rejection_reason = 'تم إغلاق الطلب بسبب تفعيل حماية معتمدة أخرى لنفس الرقم',
        updated_at = now()
    WHERE request_id IN (
        SELECT id FROM protection_requests
        WHERE phone_number_id = v_req.phone_number_id
          AND id != v_req.id
          AND status = 'conflict'
    );

    -- إرسال إشعارات للعملاء المتأثرين بالتعارض
    INSERT INTO client_notifications (
        customer_id, type, title, body, related_type, related_id
    )
    SELECT 
        customer_id, 'request_rejected',
        'تعارض في طلب الحماية',
        'نود إفادتك بأنه تم تفعيل حماية أخرى معتمدة للرقم من قبل الإدارة، وتم إغلاق الطلب المتعارض.',
        'protection_request', id
    FROM protection_requests
    WHERE phone_number_id = v_req.phone_number_id
      AND id != v_req.id
      AND status = 'conflict';

    -- حفظ سجل التاريخ
    INSERT INTO protection_history (
        protection_id, customer_id, phone_number_id, request_id, event_type,
        package_snapshot, provider_snapshot, start_at, end_at
    ) VALUES (
        v_prot_id, v_req.customer_id, v_req.phone_number_id, v_req.id, 'initial_activation',
        jsonb_build_object(
            'name', v_req.package_name_snapshot,
            'duration_days', v_req.package_duration_days_snapshot,
            'price', v_req.package_price_snapshot,
            'currency', v_req.package_currency_snapshot
        ),
        jsonb_build_object(
            'name', v_req.provider_name_snapshot,
            'code', v_req.provider_code_snapshot
        ),
        v_start_at, v_end_at
    );

    -- جلب إعدادات المهام للشركة
    SELECT * INTO v_task_setting
    FROM task_settings
    WHERE provider_id = v_req.provider_id;

    -- إنشاء خطة المهام الدورية
    INSERT INTO periodic_payment_plans (
        protection_id, provider_id, interval_days, start_at, end_at, is_active
    ) VALUES (
        v_prot_id, v_req.provider_id,
        COALESCE(v_task_setting.repeat_interval_days, 30),
        v_start_at, v_end_at, true
    ) RETURNING id INTO v_plan_id;

    -- إنشاء أول مهمة تشغيلية إن كانت مفعلة
    IF v_task_setting.first_task_enabled IS NOT FALSE THEN
        v_first_task_due := v_start_at + (COALESCE(v_task_setting.repeat_interval_days, 30) * INTERVAL '1 day');
        INSERT INTO payment_tasks (
            protection_id, periodic_payment_plan_id, provider_id, sequence_no,
            due_at, original_due_at, status, amount
        ) VALUES (
            v_prot_id, v_plan_id, v_req.provider_id, 1,
            v_first_task_due, v_first_task_due, 'due',
            COALESCE(v_task_setting.first_task_amount, 0)
        );
    END IF;

    -- تسجيل القيد المالي للدخل
    INSERT INTO financial_transactions (
        tx_type, amount, currency, status, source_type, source_id,
        customer_id, phone_number_id, payment_method_id, reference,
        description, created_by, idempotency_key
    ) VALUES (
        'income', v_req.package_price_snapshot, v_req.package_currency_snapshot, 'posted',
        'protection_request', v_req.id,
        v_req.customer_id, v_req.phone_number_id, v_req.payment_method_id, v_req.payment_reference,
        'رسوم اشتراك حماية رقم ' || v_req.package_name_snapshot,
        v_admin_id, 'income_req_' || v_req.id::TEXT
    ) ON CONFLICT (idempotency_key) DO NOTHING;

    -- إشعار للعميل
    INSERT INTO client_notifications (
        customer_id, type, title, body, related_type, related_id
    ) VALUES (
        v_req.customer_id, 'request_approved',
        'تم قبول طلب الحماية',
        'تهانينا! تم قبول طلب الحماية وتفعيل الخدمة لرقمك بنجاح حتى ' || to_char(v_end_at, 'YYYY-MM-DD'),
        'protection', v_prot_id
    );

    -- تسجيل في سجل العمليات Audit
    INSERT INTO audit_logs (
        actor_id, action, entity_type, entity_id, before_data, after_data
    ) VALUES (
        v_admin_id, 'approve_protection_request', 'protection_requests', v_req.id,
        jsonb_build_object('status', 'pending'),
        jsonb_build_object('status', 'approved', 'protection_id', v_prot_id, 'end_at', v_end_at)
    );

    RETURN jsonb_build_object(
        'success', true,
        'request_id', v_req.id,
        'protection_id', v_prot_id,
        'start_at', v_start_at,
        'end_at', v_end_at
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 11.5 دالة رفض طلب الحماية (rpc_reject_protection_request)
CREATE OR REPLACE FUNCTION rpc_reject_protection_request(
    p_request_id UUID,
    p_rejection_reason TEXT
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_req RECORD;
    v_reason TEXT;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    v_reason := trim(p_rejection_reason);
    IF length(v_reason) = 0 THEN
        RAISE EXCEPTION 'INVALID_REASON: Rejection reason is required';
    END IF;

    SELECT * INTO v_req
    FROM protection_requests
    WHERE id = p_request_id
    FOR UPDATE;

    IF v_req.id IS NULL THEN
        RAISE EXCEPTION 'NOT_FOUND: Request not found';
    END IF;

    IF v_req.status != 'pending' THEN
        RAISE EXCEPTION 'INVALID_STATE: Request is no longer pending (current status: %)', v_req.status;
    END IF;

    -- تحديث الطلب
    UPDATE protection_requests
    SET status = 'rejected',
        rejection_reason = v_reason,
        reviewed_at = now(),
        reviewed_by = v_admin_id,
        updated_at = now()
    WHERE id = v_req.id;

    -- تحديث حالة الدفع
    UPDATE payment_transactions
    SET status = 'rejected',
        rejection_reason = v_reason,
        updated_at = now()
    WHERE request_id = v_req.id;

    -- إشعار للعميل
    INSERT INTO client_notifications (
        customer_id, type, title, body, related_type, related_id
    ) VALUES (
        v_req.customer_id, 'request_rejected',
        'تم رفض طلب الحماية',
        'نأسف لإبلاغك بأنه تم رفض طلب الحماية. السبب: ' || v_reason,
        'protection_request', v_req.id
    );

    -- تسجيل في سجل العمليات Audit
    INSERT INTO audit_logs (
        actor_id, action, entity_type, entity_id, before_data, after_data
    ) VALUES (
        v_admin_id, 'reject_protection_request', 'protection_requests', v_req.id,
        jsonb_build_object('status', 'pending'),
        jsonb_build_object('status', 'rejected', 'reason', v_reason)
    );

    RETURN jsonb_build_object(
        'success', true,
        'request_id', v_req.id,
        'status', 'rejected'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 11.6 دالة قبول طلب التجديد (rpc_approve_renewal)
CREATE OR REPLACE FUNCTION rpc_approve_renewal(
    p_request_id UUID
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_req RECORD;
    v_prot RECORD;
    v_new_end_at TIMESTAMPTZ;
    v_old_end_at TIMESTAMPTZ;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    SELECT * INTO v_req
    FROM protection_requests
    WHERE id = p_request_id
    FOR UPDATE;

    IF v_req.id IS NULL THEN
        RAISE EXCEPTION 'NOT_FOUND: Renewal request not found';
    END IF;

    IF v_req.status != 'pending' THEN
        RAISE EXCEPTION 'INVALID_STATE: Request is no longer pending';
    END IF;

    IF v_req.request_type != 'renewal' THEN
        RAISE EXCEPTION 'INVALID_TYPE: This request is not a renewal request';
    END IF;

    -- التحقق من الحماية الأصلية
    SELECT * INTO v_prot
    FROM protections
    WHERE id = v_req.previous_protection_id
    FOR UPDATE;

    IF v_prot.id IS NULL THEN
        RAISE EXCEPTION 'NOT_FOUND: Original protection record not found';
    END IF;

    IF v_prot.status != 'active' THEN
        RAISE EXCEPTION 'INVALID_STATE: Target protection is not active';
    END IF;

    v_old_end_at := v_prot.end_at;
    -- قاعدة حساب تاريخ التجديد: تضاف المدة لتاريخ الانتهاء الحالي
    v_new_end_at := v_old_end_at + (v_req.package_duration_days_snapshot * INTERVAL '1 day');

    -- تحديث الحماية
    UPDATE protections
    SET end_at = v_new_end_at,
        updated_at = now()
    WHERE id = v_prot.id;

    -- تحديث الطلب
    UPDATE protection_requests
    SET status = 'approved',
        reviewed_at = now(),
        reviewed_by = v_admin_id,
        updated_at = now()
    WHERE id = v_req.id;

    -- تحديث حالة الدفع
    UPDATE payment_transactions
    SET status = 'verified',
        verified_at = now(),
        verified_by = v_admin_id,
        updated_at = now()
    WHERE request_id = v_req.id;

    -- حفظ لقطة التاريخ السابقة
    INSERT INTO protection_history (
        protection_id, customer_id, phone_number_id, request_id, event_type,
        package_snapshot, provider_snapshot, start_at, end_at
    ) VALUES (
        v_prot.id, v_prot.customer_id, v_prot.phone_number_id, v_req.id, 'renewed',
        jsonb_build_object(
            'name', v_req.package_name_snapshot,
            'duration_days', v_req.package_duration_days_snapshot,
            'price', v_req.package_price_snapshot,
            'currency', v_req.package_currency_snapshot
        ),
        jsonb_build_object(
            'name', v_req.provider_name_snapshot,
            'code', v_req.provider_code_snapshot
        ),
        v_old_end_at, v_new_end_at
    );

    -- تحديث خطة المهام لتمديد نهايتها
    UPDATE periodic_payment_plans
    SET end_at = v_new_end_at,
        updated_at = now()
    WHERE protection_id = v_prot.id AND is_active = true;

    -- تسجيل القيد المالي
    INSERT INTO financial_transactions (
        tx_type, amount, currency, status, source_type, source_id,
        customer_id, phone_number_id, payment_method_id, reference,
        description, created_by, idempotency_key
    ) VALUES (
        'income', v_req.package_price_snapshot, v_req.package_currency_snapshot, 'posted',
        'renewal_request', v_req.id,
        v_req.customer_id, v_req.phone_number_id, v_req.payment_method_id, v_req.payment_reference,
        'رسوم تجديد حماية رقم ' || v_req.package_name_snapshot,
        v_admin_id, 'income_renew_' || v_req.id::TEXT
    ) ON CONFLICT (idempotency_key) DO NOTHING;

    -- إشعار للعميل
    INSERT INTO client_notifications (
        customer_id, type, title, body, related_type, related_id
    ) VALUES (
        v_req.customer_id, 'renewal_approved',
        'تم قبول طلب التجديد',
        'تم تمديد حماية رقمك بنجاح حتى تاريخ ' || to_char(v_new_end_at, 'YYYY-MM-DD'),
        'protection', v_prot.id
    );

    -- تسجيل في سجل التدقيق Audit
    INSERT INTO audit_logs (
        actor_id, action, entity_type, entity_id, before_data, after_data
    ) VALUES (
        v_admin_id, 'approve_renewal_request', 'protection_requests', v_req.id,
        jsonb_build_object('old_end_at', v_old_end_at),
        jsonb_build_object('new_end_at', v_new_end_at, 'protection_id', v_prot.id)
    );

    RETURN jsonb_build_object(
        'success', true,
        'request_id', v_req.id,
        'protection_id', v_prot.id,
        'new_end_at', v_new_end_at
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- الاسم البديل للتطابق مع المراجع:
CREATE OR REPLACE FUNCTION approve_renewal_request(p_request_id UUID)
RETURNS JSONB AS $$
BEGIN
    RETURN rpc_approve_renewal(p_request_id);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 11.7 دالة رفض طلب التجديد (rpc_reject_renewal)
CREATE OR REPLACE FUNCTION rpc_reject_renewal(
    p_request_id UUID,
    p_rejection_reason TEXT
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_req RECORD;
    v_reason TEXT;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    v_reason := trim(p_rejection_reason);
    IF length(v_reason) = 0 THEN
        RAISE EXCEPTION 'INVALID_REASON: Rejection reason is required';
    END IF;

    SELECT * INTO v_req
    FROM protection_requests
    WHERE id = p_request_id
    FOR UPDATE;

    IF v_req.id IS NULL THEN
        RAISE EXCEPTION 'NOT_FOUND: Request not found';
    END IF;

    IF v_req.status != 'pending' THEN
        RAISE EXCEPTION 'INVALID_STATE: Request is no longer pending';
    END IF;

    UPDATE protection_requests
    SET status = 'rejected',
        rejection_reason = v_reason,
        reviewed_at = now(),
        reviewed_by = v_admin_id,
        updated_at = now()
    WHERE id = v_req.id;

    UPDATE payment_transactions
    SET status = 'rejected',
        rejection_reason = v_reason,
        updated_at = now()
    WHERE request_id = v_req.id;

    INSERT INTO client_notifications (
        customer_id, type, title, body, related_type, related_id
    ) VALUES (
        v_req.customer_id, 'renewal_rejected',
        'تم رفض طلب التجديد',
        'تم رفض طلب تجديد الحماية. السبب: ' || v_reason,
        'protection_request', v_req.id
    );

    INSERT INTO audit_logs (
        actor_id, action, entity_type, entity_id, before_data, after_data
    ) VALUES (
        v_admin_id, 'reject_renewal_request', 'protection_requests', v_req.id,
        jsonb_build_object('status', 'pending'),
        jsonb_build_object('status', 'rejected', 'reason', v_reason)
    );

    RETURN jsonb_build_object(
        'success', true,
        'request_id', v_req.id,
        'status', 'rejected'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 11.8 دالة تنفيذ وإكمال المهمة التشغيلية (rpc_execute_task / complete_payment_task)
CREATE OR REPLACE FUNCTION rpc_execute_task(
    p_task_id UUID,
    p_result TEXT DEFAULT 'تم التنفيذ بنجاح'
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_task RECORD;
    v_plan RECORD;
    v_ts RECORD;
    v_next_due TIMESTAMPTZ;
    v_next_task_id UUID;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    SELECT * INTO v_task
    FROM payment_tasks
    WHERE id = p_task_id
    FOR UPDATE;

    IF v_task.id IS NULL THEN
        RAISE EXCEPTION 'NOT_FOUND: Task not found';
    END IF;

    IF v_task.status = 'completed' THEN
        RAISE EXCEPTION 'INVALID_STATE: Task is already completed';
    END IF;

    -- إكمال المهمة الحالية
    UPDATE payment_tasks
    SET status = 'completed',
        completed_at = now(),
        completed_by = v_admin_id,
        result = COALESCE(trim(p_result), 'تم التنفيذ بنجاح'),
        updated_at = now()
    WHERE id = v_task.id;

    -- جلب الخطة وإعدادات الشركة
    SELECT * INTO v_plan
    FROM periodic_payment_plans
    WHERE id = v_task.periodic_payment_plan_id;

    SELECT * INTO v_ts
    FROM task_settings
    WHERE provider_id = v_task.provider_id;

    -- قيد المصروف التشغيلي إن كان محدداً في الإعداد
    IF v_ts.recurring_task_amount > 0 THEN
        INSERT INTO financial_transactions (
            tx_type, amount, currency, status, source_type, source_id,
            description, created_by, idempotency_key
        ) VALUES (
            'expense', v_ts.recurring_task_amount, 'YER', 'posted',
            'payment_task', v_task.id,
            'مصروف تشغيلي لتنفيذ مهمة دفع رقم ' || v_task.sequence_no::TEXT,
            v_admin_id, 'expense_task_' || v_task.id::TEXT
        ) ON CONFLICT (idempotency_key) DO NOTHING;
    END IF;

    -- التحقق مما إذا كانت الخطة ما زالت فعالة والحماية لم تنته بعد لإنشاء المهمة التالية
    IF v_plan.is_active = true AND (v_task.due_at + (v_plan.interval_days * INTERVAL '1 day')) <= v_plan.end_at THEN
        v_next_due := v_task.due_at + (v_plan.interval_days * INTERVAL '1 day');
        INSERT INTO payment_tasks (
            protection_id, periodic_payment_plan_id, provider_id, sequence_no,
            due_at, original_due_at, status, amount
        ) VALUES (
            v_task.protection_id, v_task.periodic_payment_plan_id, v_task.provider_id,
            v_task.sequence_no + 1, v_next_due, v_next_due, 'due',
            COALESCE(v_ts.recurring_task_amount, 0)
        ) RETURNING id INTO v_next_task_id;
    END IF;

    -- تسجيل في سجل التدقيق Audit
    INSERT INTO audit_logs (
        actor_id, action, entity_type, entity_id, before_data, after_data
    ) VALUES (
        v_admin_id, 'complete_payment_task', 'payment_tasks', v_task.id,
        jsonb_build_object('status', 'due'),
        jsonb_build_object('status', 'completed', 'next_task_id', v_next_task_id)
    );

    RETURN jsonb_build_object(
        'success', true,
        'task_id', v_task.id,
        'status', 'completed',
        'next_task_id', v_next_task_id
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- الاسم البديل للتطابق مع المراجع:
CREATE OR REPLACE FUNCTION complete_payment_task(p_task_id UUID, p_result TEXT DEFAULT 'تم التنفيذ بنجاح')
RETURNS JSONB AS $$
BEGIN
    RETURN rpc_execute_task(p_task_id, p_result);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 11.9 دالة إعادة جدولة المهمة (rpc_reschedule_task / reschedule_payment_task)
CREATE OR REPLACE FUNCTION rpc_reschedule_task(
    p_task_id UUID,
    p_new_due_at TIMESTAMPTZ,
    p_reason TEXT
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_task RECORD;
    v_clean_reason TEXT;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    v_clean_reason := trim(p_reason);
    IF length(v_clean_reason) = 0 THEN
        RAISE EXCEPTION 'INVALID_REASON: Reschedule reason is required';
    END IF;

    IF p_new_due_at IS NULL OR p_new_due_at <= now() THEN
        RAISE EXCEPTION 'INVALID_DATE: New due date must be in the future';
    END IF;

    SELECT * INTO v_task
    FROM payment_tasks
    WHERE id = p_task_id
    FOR UPDATE;

    IF v_task.id IS NULL THEN
        RAISE EXCEPTION 'NOT_FOUND: Task not found';
    END IF;

    IF v_task.status = 'completed' THEN
        RAISE EXCEPTION 'INVALID_STATE: Cannot reschedule an already completed task';
    END IF;

    UPDATE payment_tasks
    SET original_due_at = COALESCE(original_due_at, due_at),
        due_at = p_new_due_at,
        rescheduled_at = now(),
        rescheduled_by = v_admin_id,
        reschedule_reason = v_clean_reason,
        updated_at = now()
    WHERE id = v_task.id;

    INSERT INTO audit_logs (
        actor_id, action, entity_type, entity_id, before_data, after_data
    ) VALUES (
        v_admin_id, 'reschedule_payment_task', 'payment_tasks', v_task.id,
        jsonb_build_object('old_due_at', v_task.due_at),
        jsonb_build_object('new_due_at', p_new_due_at, 'reason', v_clean_reason)
    );

    RETURN jsonb_build_object(
        'success', true,
        'task_id', v_task.id,
        'new_due_at', p_new_due_at
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- الاسم البديل للتطابق:
CREATE OR REPLACE FUNCTION reschedule_payment_task(p_task_id UUID, p_new_due_at TIMESTAMPTZ, p_reason TEXT)
RETURNS JSONB AS $$
BEGIN
    RETURN rpc_reschedule_task(p_task_id, p_new_due_at, p_reason);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 11.10 دالة تحديث فاصل المهام للشركة (rpc_update_task_interval)
CREATE OR REPLACE FUNCTION rpc_update_task_interval(
    p_provider_id UUID,
    p_new_interval_days INTEGER
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_old_interval INTEGER;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    IF p_new_interval_days <= 0 THEN
        RAISE EXCEPTION 'INVALID_INTERVAL: Interval must be greater than zero';
    END IF;

    SELECT repeat_interval_days INTO v_old_interval
    FROM task_settings
    WHERE provider_id = p_provider_id;

    -- تحديث إعدادات الشركة
    UPDATE task_settings
    SET repeat_interval_days = p_new_interval_days,
        updated_at = now()
    WHERE provider_id = p_provider_id;

    -- تحديث الخطط الفعالة المستقبلية دون المساس بالتاريخ السابق
    UPDATE periodic_payment_plans
    SET interval_days = p_new_interval_days,
        updated_at = now()
    WHERE provider_id = p_provider_id AND is_active = true;

    INSERT INTO audit_logs (
        actor_id, action, entity_type, entity_id, before_data, after_data
    ) VALUES (
        v_admin_id, 'update_task_interval', 'task_settings', p_provider_id,
        jsonb_build_object('old_interval_days', v_old_interval),
        jsonb_build_object('new_interval_days', p_new_interval_days)
    );

    RETURN jsonb_build_object(
        'success', true,
        'provider_id', p_provider_id,
        'new_interval_days', p_new_interval_days
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 11.11 دالة التحقق اليدوي من الدفع (rpc_verify_manual_payment)
CREATE OR REPLACE FUNCTION rpc_verify_manual_payment(
    p_transaction_id UUID,
    p_status TEXT,
    p_notes TEXT DEFAULT NULL
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_tx RECORD;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    IF p_status NOT IN ('verified', 'rejected') THEN
        RAISE EXCEPTION 'INVALID_STATUS: Status must be verified or rejected';
    END IF;

    SELECT * INTO v_tx
    FROM payment_transactions
    WHERE id = p_transaction_id
    FOR UPDATE;

    IF v_tx.id IS NULL THEN
        RAISE EXCEPTION 'NOT_FOUND: Transaction not found';
    END IF;

    UPDATE payment_transactions
    SET status = p_status::payment_transaction_status_enum,
        verified_at = now(),
        verified_by = v_admin_id,
        rejection_reason = CASE WHEN p_status = 'rejected' THEN p_notes ELSE rejection_reason END,
        updated_at = now()
    WHERE id = v_tx.id;

    -- إضافة سجل تدقيق يدوي
    INSERT INTO manual_payment_logs (
        payment_transaction_id, verified_by, verification_status, notes
    ) VALUES (
        v_tx.id, v_admin_id, p_status, p_notes
    );

    INSERT INTO audit_logs (
        actor_id, action, entity_type, entity_id, before_data, after_data
    ) VALUES (
        v_admin_id, 'verify_manual_payment', 'payment_transactions', v_tx.id,
        jsonb_build_object('old_status', v_tx.status),
        jsonb_build_object('new_status', p_status, 'notes', p_notes)
    );

    RETURN jsonb_build_object(
        'success', true,
        'transaction_id', v_tx.id,
        'status', p_status
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 11.12 دالة قراءة الإشعار للعميل (rpc_mark_notification_read)
CREATE OR REPLACE FUNCTION rpc_mark_notification_read(
    p_notification_id UUID
)
RETURNS JSONB AS $$
DECLARE
    v_user_id UUID;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'UNAUTHORIZED: Authentication session required';
    END IF;

    UPDATE client_notifications
    SET is_read = true,
        read_at = now()
    WHERE id = p_notification_id AND customer_id = v_user_id;

    RETURN jsonb_build_object('success', true, 'notification_id', p_notification_id);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- دالة قراءة جميع إشعارات العميل (rpc_mark_all_notifications_read)
CREATE OR REPLACE FUNCTION rpc_mark_all_notifications_read()
RETURNS JSONB AS $$
DECLARE
    v_user_id UUID;
    v_count INTEGER;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'UNAUTHORIZED: Authentication session required';
    END IF;

    UPDATE client_notifications
    SET is_read = true,
        read_at = now()
    WHERE customer_id = v_user_id AND is_read = false;

    GET DIAGNOSTICS v_count = ROW_COUNT;

    RETURN jsonb_build_object('success', true, 'marked_count', v_count);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 11.13 ملخص لوحة تحكم الإدارة (get_admin_dashboard_summary)
CREATE OR REPLACE FUNCTION get_admin_dashboard_summary()
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_customers_count BIGINT;
    v_total_numbers_count BIGINT;
    v_protected_numbers_count BIGINT;
    v_not_protected_numbers_count BIGINT;
    v_pending_requests_count BIGINT;
    v_approved_requests_count BIGINT;
    v_rejected_requests_count BIGINT;
    v_due_today_tasks_count BIGINT;
    v_upcoming_tasks_count BIGINT;
    v_overdue_tasks_count BIGINT;
    v_completed_tasks_count BIGINT;
    v_active_protections_count BIGINT;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    -- العملاء
    SELECT COUNT(*) INTO v_customers_count
    FROM users WHERE user_type = 'customer' AND status = 'active';

    -- الأرقام
    SELECT COUNT(*) INTO v_total_numbers_count FROM phone_numbers;
    
    SELECT COUNT(DISTINCT phone_number_id) INTO v_protected_numbers_count
    FROM protections WHERE status = 'active' AND end_at > now();

    v_not_protected_numbers_count := GREATEST(0, v_total_numbers_count - v_protected_numbers_count);

    -- طلبات الحماية
    SELECT COUNT(*) INTO v_pending_requests_count FROM protection_requests WHERE status = 'pending';
    SELECT COUNT(*) INTO v_approved_requests_count FROM protection_requests WHERE status = 'approved';
    SELECT COUNT(*) INTO v_rejected_requests_count FROM protection_requests WHERE status = 'rejected';

    -- المهام
    SELECT COUNT(*) INTO v_completed_tasks_count FROM payment_tasks WHERE status = 'completed';
    SELECT COUNT(*) INTO v_overdue_tasks_count FROM payment_tasks WHERE status = 'due' AND due_at < now();
    SELECT COUNT(*) INTO v_due_today_tasks_count FROM payment_tasks WHERE status = 'due' AND DATE(due_at) = CURRENT_DATE;
    SELECT COUNT(*) INTO v_upcoming_tasks_count FROM payment_tasks WHERE status = 'due' AND due_at > now() AND DATE(due_at) != CURRENT_DATE;

    -- الحمايات
    SELECT COUNT(*) INTO v_active_protections_count FROM protections WHERE status = 'active' AND end_at > now();

    RETURN jsonb_build_object(
        'customers_count', v_customers_count,
        'total_numbers_count', v_total_numbers_count,
        'protected_numbers_count', v_protected_numbers_count,
        'not_protected_numbers_count', v_not_protected_numbers_count,
        'pending_requests_count', v_pending_requests_count,
        'approved_requests_count', v_approved_requests_count,
        'rejected_requests_count', v_rejected_requests_count,
        'due_today_tasks_count', v_due_today_tasks_count,
        'upcoming_tasks_count', v_upcoming_tasks_count,
        'overdue_tasks_count', v_overdue_tasks_count,
        'completed_tasks_count', v_completed_tasks_count,
        'active_protections_count', v_active_protections_count
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 11.14 دوال إعدادات الإدارة (ADMIN UPSERTS)

-- إضافة / تعديل شركة اتصالات (admin_upsert_provider)
CREATE OR REPLACE FUNCTION admin_upsert_provider(
    p_id UUID DEFAULT NULL,
    p_name TEXT DEFAULT NULL,
    p_code TEXT DEFAULT NULL,
    p_number_length INTEGER DEFAULT NULL,
    p_is_active BOOLEAN DEFAULT true,
    p_sort_order INTEGER DEFAULT 0
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_provider_id UUID;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    IF p_id IS NULL THEN
        -- إضافة شركة جديدة
        INSERT INTO telecom_providers (name, code, number_length, is_active, sort_order)
        VALUES (trim(p_name), upper(trim(p_code)), p_number_length, p_is_active, p_sort_order)
        RETURNING id INTO v_provider_id;

        -- إنشاء إعدادات افتراضية للمهام للشركة الجديدة
        INSERT INTO task_settings (provider_id, first_task_enabled, recurring_tasks_enabled, repeat_interval_days)
        VALUES (v_provider_id, true, true, 30)
        ON CONFLICT (provider_id) DO NOTHING;
    ELSE
        -- تعديل شركة موجودة
        UPDATE telecom_providers
        SET name = COALESCE(trim(p_name), name),
            code = COALESCE(upper(trim(p_code)), code),
            number_length = COALESCE(p_number_length, number_length),
            is_active = COALESCE(p_is_active, is_active),
            sort_order = COALESCE(p_sort_order, sort_order),
            updated_at = now()
        WHERE id = p_id
        RETURNING id INTO v_provider_id;
    END IF;

    INSERT INTO audit_logs (actor_id, action, entity_type, entity_id, after_data)
    VALUES (v_admin_id, 'admin_upsert_provider', 'telecom_providers', v_provider_id,
            jsonb_build_object('name', p_name, 'code', p_code, 'is_active', p_is_active));

    RETURN jsonb_build_object('success', true, 'provider_id', v_provider_id);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- إضافة / تعديل بادئة شركة (admin_upsert_provider_prefix)
CREATE OR REPLACE FUNCTION admin_upsert_provider_prefix(
    p_id UUID DEFAULT NULL,
    p_provider_id UUID DEFAULT NULL,
    p_prefix TEXT DEFAULT NULL,
    p_is_active BOOLEAN DEFAULT true
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_prefix_id UUID;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    IF p_id IS NULL THEN
        INSERT INTO provider_prefixes (provider_id, prefix, is_active)
        VALUES (p_provider_id, trim(p_prefix), p_is_active)
        RETURNING id INTO v_prefix_id;
    ELSE
        UPDATE provider_prefixes
        SET prefix = COALESCE(trim(p_prefix), prefix),
            is_active = COALESCE(p_is_active, is_active)
        WHERE id = p_id
        RETURNING id INTO v_prefix_id;
    END IF;

    RETURN jsonb_build_object('success', true, 'prefix_id', v_prefix_id);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- إضافة / تعديل باقة (admin_upsert_package)
CREATE OR REPLACE FUNCTION admin_upsert_package(
    p_id UUID DEFAULT NULL,
    p_provider_id UUID DEFAULT NULL,
    p_name TEXT DEFAULT NULL,
    p_description TEXT DEFAULT NULL,
    p_duration_days INTEGER DEFAULT 365,
    p_price NUMERIC DEFAULT 0,
    p_currency TEXT DEFAULT 'YER',
    p_is_active BOOLEAN DEFAULT true,
    p_is_visible BOOLEAN DEFAULT true,
    p_sort_order INTEGER DEFAULT 0
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_package_id UUID;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    IF p_id IS NULL THEN
        INSERT INTO packages (
            provider_id, name, description, duration_days, price, currency, is_active, is_visible, sort_order
        ) VALUES (
            p_provider_id, trim(p_name), p_description, p_duration_days, p_price, COALESCE(trim(p_currency), 'YER'),
            p_is_active, p_is_visible, p_sort_order
        ) RETURNING id INTO v_package_id;
    ELSE
        UPDATE packages
        SET provider_id = COALESCE(p_provider_id, provider_id),
            name = COALESCE(trim(p_name), name),
            description = COALESCE(p_description, description),
            duration_days = COALESCE(p_duration_days, duration_days),
            price = COALESCE(p_price, price),
            currency = COALESCE(trim(p_currency), currency),
            is_active = COALESCE(p_is_active, is_active),
            is_visible = COALESCE(p_is_visible, is_visible),
            sort_order = COALESCE(p_sort_order, sort_order),
            updated_at = now()
        WHERE id = p_id
        RETURNING id INTO v_package_id;
    END IF;

    INSERT INTO audit_logs (actor_id, action, entity_type, entity_id, after_data)
    VALUES (v_admin_id, 'admin_upsert_package', 'packages', v_package_id,
            jsonb_build_object('name', p_name, 'price', p_price, 'is_active', p_is_active));

    RETURN jsonb_build_object('success', true, 'package_id', v_package_id);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- إضافة / تعديل وسيلة دفع (admin_upsert_payment_method)
CREATE OR REPLACE FUNCTION admin_upsert_payment_method(
    p_id UUID DEFAULT NULL,
    p_type TEXT DEFAULT 'wallet',
    p_name TEXT DEFAULT NULL,
    p_recipient_name TEXT DEFAULT NULL,
    p_account_number TEXT DEFAULT NULL,
    p_instructions TEXT DEFAULT NULL,
    p_is_active BOOLEAN DEFAULT true,
    p_sort_order INTEGER DEFAULT 0
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_method_id UUID;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    IF p_id IS NULL THEN
        INSERT INTO payment_methods (
            type, name, recipient_name, account_number, instructions, is_active, sort_order
        ) VALUES (
            p_type::payment_method_type_enum, trim(p_name), trim(p_recipient_name), trim(p_account_number),
            p_instructions, p_is_active, p_sort_order
        ) RETURNING id INTO v_method_id;
    ELSE
        UPDATE payment_methods
        SET type = COALESCE(p_type::payment_method_type_enum, type),
            name = COALESCE(trim(p_name), name),
            recipient_name = COALESCE(trim(p_recipient_name), recipient_name),
            account_number = COALESCE(trim(p_account_number), account_number),
            instructions = COALESCE(p_instructions, instructions),
            is_active = COALESCE(p_is_active, is_active),
            sort_order = COALESCE(p_sort_order, sort_order),
            updated_at = now()
        WHERE id = p_id
        RETURNING id INTO v_method_id;
    END IF;

    INSERT INTO audit_logs (actor_id, action, entity_type, entity_id, after_data)
    VALUES (v_admin_id, 'admin_upsert_payment_method', 'payment_methods', v_method_id,
            jsonb_build_object('name', p_name, 'account', p_account_number, 'is_active', p_is_active));

    RETURN jsonb_build_object('success', true, 'payment_method_id', v_method_id);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- تعديل إعدادات المهام للشركة (admin_upsert_task_settings)
CREATE OR REPLACE FUNCTION admin_upsert_task_settings(
    p_provider_id UUID,
    p_first_task_enabled BOOLEAN DEFAULT true,
    p_first_task_amount NUMERIC DEFAULT 0,
    p_recurring_tasks_enabled BOOLEAN DEFAULT true,
    p_recurring_task_amount NUMERIC DEFAULT 0,
    p_repeat_interval_days INTEGER DEFAULT 30,
    p_days_visible_before_due INTEGER DEFAULT 7
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_setting_id UUID;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    INSERT INTO task_settings (
        provider_id, first_task_enabled, first_task_amount, recurring_tasks_enabled,
        recurring_task_amount, repeat_interval_days, days_visible_before_due
    ) VALUES (
        p_provider_id, p_first_task_enabled, p_first_task_amount, p_recurring_tasks_enabled,
        p_recurring_task_amount, p_repeat_interval_days, p_days_visible_before_due
    )
    ON CONFLICT (provider_id) DO UPDATE
    SET first_task_enabled = EXCLUDED.first_task_enabled,
        first_task_amount = EXCLUDED.first_task_amount,
        recurring_tasks_enabled = EXCLUDED.recurring_tasks_enabled,
        recurring_task_amount = EXCLUDED.recurring_task_amount,
        repeat_interval_days = EXCLUDED.repeat_interval_days,
        days_visible_before_due = EXCLUDED.days_visible_before_due,
        updated_at = now()
    RETURNING id INTO v_setting_id;

    RETURN jsonb_build_object('success', true, 'task_settings_id', v_setting_id);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ضبط تصنيف توقيت المهام (admin_upsert_task_classification)
CREATE OR REPLACE FUNCTION admin_upsert_task_classification(
    p_id UUID DEFAULT NULL,
    p_task_settings_id UUID DEFAULT NULL,
    p_name TEXT DEFAULT NULL,
    p_min_days_remaining INTEGER DEFAULT 0,
    p_max_days_remaining INTEGER DEFAULT 0,
    p_sort_order INTEGER DEFAULT 0,
    p_is_active BOOLEAN DEFAULT true
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_class_id UUID;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    IF p_id IS NULL THEN
        INSERT INTO task_time_classifications (
            task_settings_id, name, min_days_remaining, max_days_remaining, sort_order, is_active
        ) VALUES (
            p_task_settings_id, trim(p_name), p_min_days_remaining, p_max_days_remaining, p_sort_order, p_is_active
        ) RETURNING id INTO v_class_id;
    ELSE
        UPDATE task_time_classifications
        SET name = COALESCE(trim(p_name), name),
            min_days_remaining = COALESCE(p_min_days_remaining, min_days_remaining),
            max_days_remaining = COALESCE(p_max_days_remaining, max_days_remaining),
            sort_order = COALESCE(p_sort_order, sort_order),
            is_active = COALESCE(p_is_active, is_active),
            updated_at = now()
        WHERE id = p_id
        RETURNING id INTO v_class_id;
    END IF;

    RETURN jsonb_build_object('success', true, 'classification_id', v_class_id);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- تعيين إعداد نظام مركزي (admin_set_system_setting)
CREATE OR REPLACE FUNCTION admin_set_system_setting(
    p_key TEXT,
    p_value JSONB,
    p_description TEXT DEFAULT NULL
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    INSERT INTO system_settings (key, value, description, updated_by, updated_at)
    VALUES (trim(p_key), p_value, p_description, v_admin_id, now())
    ON CONFLICT (key) DO UPDATE
    SET value = EXCLUDED.value,
        description = COALESCE(EXCLUDED.description, system_settings.description),
        updated_by = v_admin_id,
        updated_at = now();

    INSERT INTO audit_logs (actor_id, action, entity_type, entity_id, after_data)
    VALUES (v_admin_id, 'admin_set_system_setting', 'system_settings', NULL,
            jsonb_build_object('key', p_key, 'value', p_value));

    RETURN jsonb_build_object('success', true, 'key', p_key);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- تعديل حالة مستخدم (admin_set_user_status)
CREATE OR REPLACE FUNCTION admin_set_user_status(
    p_user_id UUID,
    p_status TEXT
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
    v_old_status user_status_enum;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    IF p_status NOT IN ('active', 'suspended', 'disabled') THEN
        RAISE EXCEPTION 'INVALID_STATUS: Allowed values are active, suspended, disabled';
    END IF;

    SELECT status INTO v_old_status FROM users WHERE id = p_user_id;
    IF v_old_status IS NULL THEN
        RAISE EXCEPTION 'NOT_FOUND: User not found';
    END IF;

    UPDATE users
    SET status = p_status::user_status_enum,
        updated_at = now()
    WHERE id = p_user_id;

    INSERT INTO audit_logs (actor_id, action, entity_type, entity_id, before_data, after_data)
    VALUES (v_admin_id, 'admin_set_user_status', 'users', p_user_id,
            jsonb_build_object('status', v_old_status),
            jsonb_build_object('status', p_status));

    RETURN jsonb_build_object('success', true, 'user_id', p_user_id, 'new_status', p_status);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- تعيين دور مستخدم (admin_set_user_role)
CREATE OR REPLACE FUNCTION admin_set_user_role(
    p_user_id UUID,
    p_role_id UUID
)
RETURNS JSONB AS $$
DECLARE
    v_admin_id UUID;
BEGIN
    v_admin_id := auth.uid();
    IF v_admin_id IS NULL OR NOT fn_is_admin(v_admin_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Admin privileges required';
    END IF;

    UPDATE users
    SET role_id = p_role_id,
        updated_at = now()
    WHERE id = p_user_id;

    RETURN jsonb_build_object('success', true, 'user_id', p_user_id, 'role_id', p_role_id);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =============================================================================
-- 12. سياسات أمان مستوى الصف (ROW LEVEL SECURITY - RLS)
-- =============================================================================

ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE telecom_providers ENABLE ROW LEVEL SECURITY;
ALTER TABLE provider_prefixes ENABLE ROW LEVEL SECURITY;
ALTER TABLE phone_numbers ENABLE ROW LEVEL SECURITY;
ALTER TABLE customer_numbers ENABLE ROW LEVEL SECURITY;
ALTER TABLE packages ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_methods ENABLE ROW LEVEL SECURITY;
ALTER TABLE protections ENABLE ROW LEVEL SECURITY;
ALTER TABLE protection_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE manual_payment_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE protection_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE task_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE task_time_classifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE periodic_payment_plans ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE client_notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE financial_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE system_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- سياسات users
CREATE POLICY users_self_read ON users
FOR SELECT TO authenticated
USING (id = auth.uid() OR fn_is_admin(auth.uid()));

CREATE POLICY users_self_update ON users
FOR UPDATE TO authenticated
USING (id = auth.uid() OR fn_is_admin(auth.uid()))
WITH CHECK (id = auth.uid() OR fn_is_admin(auth.uid()));

-- سياسات telecom_providers
CREATE POLICY telecom_providers_read ON telecom_providers
FOR SELECT TO authenticated
USING (is_active = true OR fn_is_admin(auth.uid()));

-- سياسات provider_prefixes
CREATE POLICY provider_prefixes_read ON provider_prefixes
FOR SELECT TO authenticated
USING (is_active = true OR fn_is_admin(auth.uid()));

-- سياسات phone_numbers
CREATE POLICY phone_numbers_read ON phone_numbers
FOR SELECT TO authenticated
USING (
    fn_is_admin(auth.uid()) OR
    EXISTS (
        SELECT 1 FROM customer_numbers 
        WHERE customer_numbers.phone_number_id = phone_numbers.id 
          AND customer_numbers.customer_id = auth.uid()
    )
);

-- سياسات customer_numbers
CREATE POLICY customer_numbers_read ON customer_numbers
FOR SELECT TO authenticated
USING (customer_id = auth.uid() OR fn_is_admin(auth.uid()));

-- سياسات packages
CREATE POLICY packages_read ON packages
FOR SELECT TO authenticated
USING ((is_active = true AND is_visible = true) OR fn_is_admin(auth.uid()));

-- سياسات payment_methods
CREATE POLICY payment_methods_read ON payment_methods
FOR SELECT TO authenticated
USING (is_active = true OR fn_is_admin(auth.uid()));

-- سياسات protections
CREATE POLICY protections_read ON protections
FOR SELECT TO authenticated
USING (customer_id = auth.uid() OR fn_is_admin(auth.uid()));

-- سياسات protection_requests
CREATE POLICY protection_requests_read ON protection_requests
FOR SELECT TO authenticated
USING (customer_id = auth.uid() OR fn_is_admin(auth.uid()));

-- سياسات payment_transactions
CREATE POLICY payment_transactions_read ON payment_transactions
FOR SELECT TO authenticated
USING (customer_id = auth.uid() OR fn_is_admin(auth.uid()));

-- سياسات manual_payment_logs (إدارية فقط)
CREATE POLICY manual_payment_logs_read ON manual_payment_logs
FOR SELECT TO authenticated
USING (fn_is_admin(auth.uid()));

-- سياسات protection_history
CREATE POLICY protection_history_read ON protection_history
FOR SELECT TO authenticated
USING (customer_id = auth.uid() OR fn_is_admin(auth.uid()));

-- سياسات task_settings (إدارية)
CREATE POLICY task_settings_read ON task_settings
FOR SELECT TO authenticated
USING (fn_is_admin(auth.uid()));

-- سياسات task_time_classifications (إدارية)
CREATE POLICY task_time_classifications_read ON task_time_classifications
FOR SELECT TO authenticated
USING (fn_is_admin(auth.uid()));

-- سياسات periodic_payment_plans (إدارية)
CREATE POLICY periodic_payment_plans_read ON periodic_payment_plans
FOR SELECT TO authenticated
USING (fn_is_admin(auth.uid()));

-- سياسات payment_tasks (إدارية)
CREATE POLICY payment_tasks_read ON payment_tasks
FOR SELECT TO authenticated
USING (fn_is_admin(auth.uid()));

-- سياسات client_notifications
CREATE POLICY client_notifications_read ON client_notifications
FOR SELECT TO authenticated
USING (customer_id = auth.uid());

CREATE POLICY client_notifications_update ON client_notifications
FOR UPDATE TO authenticated
USING (customer_id = auth.uid())
WITH CHECK (customer_id = auth.uid());

-- سياسات financial_transactions (إدارية)
CREATE POLICY financial_transactions_read ON financial_transactions
FOR SELECT TO authenticated
USING (fn_is_admin(auth.uid()));

-- سياسات system_settings
CREATE POLICY system_settings_read ON system_settings
FOR SELECT TO authenticated
USING (true);

-- سياسات audit_logs (إدارية)
CREATE POLICY audit_logs_read ON audit_logs
FOR SELECT TO authenticated
USING (fn_is_admin(auth.uid()));


-- البيانات الأولية المعتمدة لإعدادات النظام والاشتراك
INSERT INTO system_settings (key, value, description)
VALUES 
    ('default_currency', 'YER'::jsonb, 'العملة الافتراضية المعتمدة للنظام'),
    ('subscription_settings', '{duration_days: 365, renewal_allowed_window_days: 30, grace_period_days: 0, standard_fee: 1000}'::jsonb, 'إعدادات الاشتراك والتجديد المركزية')
ON CONFLICT (key) DO NOTHING;

-- =============================================================================
-- تم اكتمال ملف AMAN_DATABASE.sql المعتمد لجميع متطلبات العميل والإدارة
-- =============================================================================
