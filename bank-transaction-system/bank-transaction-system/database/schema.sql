CREATE TABLE IF NOT EXISTS transaction_log (
    id BIGSERIAL PRIMARY KEY,
    trx_id VARCHAR(50) NOT NULL UNIQUE,
    bank_id VARCHAR(20) NOT NULL,
    customer_id BIGINT NOT NULL,
    from_account VARCHAR(20) NOT NULL,
    to_account VARCHAR(20) NOT NULL,
    amount NUMERIC(18, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(255),
    request_timestamp TIMESTAMPTZ NOT NULL,
    processed_timestamp TIMESTAMPTZ NOT NULL,
    processing_time_ms BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS account_balance (
    account_no VARCHAR(20) PRIMARY KEY,
    balance NUMERIC(18, 2) NOT NULL
);

INSERT INTO account_balance (account_no, balance) VALUES
('1234567890', 1000000.00)
ON CONFLICT (account_no) DO NOTHING;

INSERT INTO account_balance (account_no, balance) VALUES
('9876543210', 500000.00)
ON CONFLICT (account_no) DO NOTHING;