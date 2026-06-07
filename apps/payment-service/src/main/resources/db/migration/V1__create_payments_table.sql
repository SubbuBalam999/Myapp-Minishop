CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL CHECK (order_id > 0),
    user_id BIGINT NOT NULL CHECK (user_id > 0),
    amount NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    payment_method VARCHAR(50) NOT NULL
        CHECK (length(trim(payment_method)) > 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_order_id_created_at
    ON payments (order_id, created_at DESC);

CREATE INDEX idx_payments_user_id_created_at
    ON payments (user_id, created_at DESC);

