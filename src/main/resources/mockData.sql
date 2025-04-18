-- Insert Interest Rules
INSERT INTO interest_rules (rule_id, date, rate)
VALUES
    ('RULE01', '2023-06-26', 4.20),
    ('RULE02', '2023-06-27', 3.20),
    ('RULE03', '2023-06-28', 1.20),
    ('RULE04', '2023-06-01', 1.20);

-- Insert Transactions
INSERT INTO transaction_details (txn_id, account, date, amount, type)
VALUES
    ('2023-06-26-01', 'AC001', '2023-06-26', 250.00, 'D'),
    ('2023-06-27-02', 'AC001', '2023-06-27', 100000.00, 'D'),
    ('2023-06-28-03', 'AC001', '2023-06-28', 250.00, 'D'),
    ('2023-06-29-04', 'AC001', '2023-06-29', 250.00, 'W');