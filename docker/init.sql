CREATE DATABASE IF NOT EXISTS ppob_db;
USE ppob_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    balance DECIMAL(15,2) DEFAULT 0.00,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_number VARCHAR(50) UNIQUE,
    user_id BIGINT,
    product_type VARCHAR(50),
    customer_number VARCHAR(50),
    amount DECIMAL(15,2),
    admin_fee DECIMAL(15,2),
    total_amount DECIMAL(15,2),
    status VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Data user contoh (john_doe: "john123", jane_doe: "jane123")
INSERT INTO users (username, password, full_name, balance) VALUES
('john_doe', '$2b$12$9q3wBdm.aUthcu6Fnh3zs.nb7geEZUjJXFW8tcNia8V5dEB/9iUTW', 'John Doe', 500000.00),
('jane_doe', '$2b$12$TZG5BEwlW4kZAGoos1WJ9uq.spjnJUBD28uma4Q2XoBLGwaAM/4fm', 'Jane Doe', 1000000.00);