-- Table for Interest Rules
CREATE TABLE interest_rules (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        rule_id VARCHAR(50) ,
        date DATE NOT NULL,
        rate DECIMAL(5, 2) NOT NULL CHECK (rate > 0 AND rate < 100)
);

-- Table for Transactions
CREATE TABLE transaction_details (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        txn_id VARCHAR(50) NOT NULL,
        account VARCHAR(50) NOT NULL,
        date DATE NOT NULL,
        amount DECIMAL(12, 2) NOT NULL,
        type VARCHAR(20) NOT NULL CHECK (type IN ('D', 'W', 'I'))
);