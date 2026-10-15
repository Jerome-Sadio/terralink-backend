-- Terra-Link Database Schema Migration V1

-- 1. Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    pin_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'PROPRIETAIRE', 'CONTROLEUR', 'ADMIN'
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Farms / Exploitations Table
CREATE TABLE farms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    size_hectares NUMERIC(10, 2) NOT NULL DEFAULT 0.0,
    type VARCHAR(50) NOT NULL, -- 'AGRICULTURE', 'ELEVAGE', 'MIXTE'
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Controller Assignments
CREATE TABLE controller_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    controller_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    unassigned_at TIMESTAMP WITH TIME ZONE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 4. Visit Reports
CREATE TABLE visit_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    controller_id UUID NOT NULL REFERENCES users(id),
    validated_by_admin_id UUID REFERENCES users(id),
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED', -- 'DRAFT', 'SUBMITTED', 'VALIDATED', 'REJECTED'
    visit_date TIMESTAMP WITH TIME ZONE NOT NULL,
    overall_health VARCHAR(20) NOT NULL, -- 'EXCELLENT', 'GOOD', 'AVERAGE', 'POOR'
    observations TEXT,
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Visit Photos (Geotagged & Timestamped Proofs)
CREATE TABLE visit_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    visit_report_id UUID NOT NULL REFERENCES visit_reports(id) ON DELETE CASCADE,
    photo_url TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    taken_at TIMESTAMP WITH TIME ZONE NOT NULL,
    metadata_hash VARCHAR(64) NOT NULL
);

-- 6. Visit Expenses & Receipts
CREATE TABLE visit_expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    visit_report_id UUID NOT NULL REFERENCES visit_reports(id) ON DELETE CASCADE,
    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'XOF',
    category VARCHAR(50) NOT NULL, -- 'ENGRAIS', 'SEMENCES', 'MATERIEL', 'SALAIRES', 'AUTRES'
    description TEXT,
    receipt_photo_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Notifications
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'VISIT_PUBLISHED', 'VISIT_VALIDATED', 'VISIT_REJECTED', 'SYSTEM'
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Performance Indexes
CREATE INDEX idx_farms_owner ON farms(owner_id);
CREATE INDEX idx_assignments_farm ON controller_assignments(farm_id);
CREATE INDEX idx_assignments_controller ON controller_assignments(controller_id);
CREATE INDEX idx_visit_reports_farm ON visit_reports(farm_id);
CREATE INDEX idx_visit_reports_controller ON visit_reports(controller_id);
CREATE INDEX idx_visit_photos_report ON visit_photos(visit_report_id);
CREATE INDEX idx_visit_expenses_report ON visit_expenses(visit_report_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
