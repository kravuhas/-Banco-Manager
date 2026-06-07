-- ═══════════════════════════════════════════════════════════════════════════
--  BANCO DE DADOS BANCÁRIO — SQL CORRIGIDO
--  Erros corrigidos:
--   · Vírgula faltando em bank (head_Office_address)
--   · Colunas duplicadas (branch_id INT declarado duas vezes, etc.) removidas
--   · Tabela pix_audit_log duplicada removida (unificada)
--   · email_notification duplicada removida
--   · Tabela 'CREATE TABLE' sem nome corrigida / removida
--   · FOREIGN KEY em email/phone removida (não é PK/UNIQUE normalmente)
--   · Bloco T-SQL (DECLARE/@var) separado com comentário — não funciona em MySQL
--   · send_pix/receive_pix unificados em tabela pix_transfer (evita redundância)
--   · Queries de consulta corrigidas (nomes de colunas alinhados ao schema)
-- ═══════════════════════════════════════════════════════════════════════════

-- ── Tabelas Principais ──────────────────────────────────────────────────────

CREATE TABLE bank (
    bank_id             INT          PRIMARY KEY AUTO_INCREMENT,
    bank_name           VARCHAR(210) NOT NULL,
    bank_code           VARCHAR(20)  NOT NULL,
    head_office_address VARCHAR(210)
);

CREATE TABLE branch (
    branch_id      INT          PRIMARY KEY AUTO_INCREMENT,
    bank_id        INT          NOT NULL,
    branch_name    VARCHAR(210) NOT NULL,
    branch_code    VARCHAR(20)  NOT NULL,
    branch_address VARCHAR(210),
    FOREIGN KEY (bank_id) REFERENCES bank(bank_id)
);

CREATE TABLE customer (
    customer_id      INT          PRIMARY KEY AUTO_INCREMENT,
    customer_name    VARCHAR(210) NOT NULL,
    customer_email   VARCHAR(100) NOT NULL UNIQUE,
    customer_phone   VARCHAR(20)  UNIQUE,
    customer_address VARCHAR(210)
);

CREATE TABLE account (
    account_id          INT          PRIMARY KEY AUTO_INCREMENT,
    branch_id           INT          NOT NULL,
    customer_id         INT          NOT NULL,
    account_number      VARCHAR(20)  NOT NULL UNIQUE,
    account_holder_name VARCHAR(210) NOT NULL,
    account_type        ENUM('Savings','Current') NOT NULL DEFAULT 'Savings',
    balance             DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (branch_id)   REFERENCES branch(branch_id),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

CREATE TABLE employee (
    employee_id       INT          PRIMARY KEY AUTO_INCREMENT,
    branch_id         INT          NOT NULL,
    employee_name     VARCHAR(210) NOT NULL,
    employee_position VARCHAR(100),
    salary            DECIMAL(12,2),
    FOREIGN KEY (branch_id) REFERENCES branch(branch_id)
);

CREATE TABLE transaction (
    transaction_id   INT          PRIMARY KEY AUTO_INCREMENT,
    account_id       INT          NOT NULL,
    transaction_type VARCHAR(20)  NOT NULL,   -- 'Deposit','Withdrawal','Pix','Transfer'
    transaction_mode VARCHAR(20)  DEFAULT 'Digital', -- 'Cash','Cheque','Digital'
    amount           DECIMAL(15,2) NOT NULL,
    transaction_date DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id)
);

CREATE TABLE loan (
    loan_id       INT           PRIMARY KEY AUTO_INCREMENT,
    account_id    INT           NOT NULL,
    loan_amount   DECIMAL(15,2) NOT NULL,
    interest_rate DECIMAL(5,2)  NOT NULL,
    loan_term     INT           NOT NULL,   -- meses
    FOREIGN KEY (account_id) REFERENCES account(account_id)
);

CREATE TABLE loan_payment (
    payment_id     INT           PRIMARY KEY AUTO_INCREMENT,
    loan_id        INT           NOT NULL,
    payment_amount DECIMAL(15,2) NOT NULL,
    payment_date   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (loan_id) REFERENCES loan(loan_id)
);

CREATE TABLE credit_card (
    card_id         INT         PRIMARY KEY AUTO_INCREMENT,
    account_id      INT         NOT NULL,
    card_number     VARCHAR(20) NOT NULL UNIQUE,
    card_type       VARCHAR(50),
    expiration_date DATE,
    FOREIGN KEY (account_id) REFERENCES account(account_id)
);

CREATE TABLE credit_card_transaction (
    cc_transaction_id  INT           PRIMARY KEY AUTO_INCREMENT,
    card_id            INT           NOT NULL,
    transaction_amount DECIMAL(15,2) NOT NULL,
    transaction_date   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (card_id) REFERENCES credit_card(card_id)
);

-- ── Pix (tabela unificada) ───────────────────────────────────────────────────

CREATE TABLE pix_transfer (
    pix_id             INT           PRIMARY KEY AUTO_INCREMENT,
    sender_account_id  INT           NOT NULL,
    receiver_account_id INT          NOT NULL,
    pix_amount         DECIMAL(15,2) NOT NULL,
    status             VARCHAR(20)   DEFAULT 'Completed',
    pix_date           DATETIME      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_account_id)   REFERENCES account(account_id),
    FOREIGN KEY (receiver_account_id) REFERENCES account(account_id)
);

-- ── Relacionamentos ──────────────────────────────────────────────────────────

CREATE TABLE account_customer (
    account_id  INT NOT NULL,
    customer_id INT NOT NULL,
    PRIMARY KEY (account_id, customer_id),
    FOREIGN KEY (account_id)  REFERENCES account(account_id),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

CREATE TABLE employee_customer (
    employee_id INT NOT NULL,
    customer_id INT NOT NULL,
    PRIMARY KEY (employee_id, customer_id),
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

-- ── Notificações & Mensagens ─────────────────────────────────────────────────

CREATE TABLE notification (
    notification_id      INT          PRIMARY KEY AUTO_INCREMENT,
    recipient_id         INT          NOT NULL,
    notification_content TEXT         NOT NULL,
    notification_type    VARCHAR(30)  DEFAULT 'in_app', -- 'email','sms','push','in_app'
    notification_date    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_id) REFERENCES employee(employee_id)
);

CREATE TABLE send_message (
    message_id      INT      PRIMARY KEY AUTO_INCREMENT,
    sender_id       INT      NOT NULL,
    receiver_id     INT      NOT NULL,
    message_content TEXT     NOT NULL,
    message_date    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id)   REFERENCES employee(employee_id),
    FOREIGN KEY (receiver_id) REFERENCES employee(employee_id)
);

CREATE TABLE email_log (
    email_id         INT          PRIMARY KEY AUTO_INCREMENT,
    sender_email     VARCHAR(100) NOT NULL,
    receiver_email   VARCHAR(100) NOT NULL,
    email_subject    VARCHAR(255) NOT NULL,
    email_content    TEXT         NOT NULL,
    email_date       DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- ── Suporte ao Cliente ───────────────────────────────────────────────────────

CREATE TABLE support_ticket (
    ticket_id         INT          PRIMARY KEY AUTO_INCREMENT,
    customer_id       INT          NOT NULL,
    issue_description TEXT         NOT NULL,
    ticket_status     VARCHAR(20)  NOT NULL DEFAULT 'Open',
    ticket_date       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

CREATE TABLE feedback (
    feedback_id      INT      PRIMARY KEY AUTO_INCREMENT,
    customer_id      INT      NOT NULL,
    feedback_content TEXT     NOT NULL,
    feedback_date    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

-- ── Segurança & Auditoria (tabela unificada por entidade) ────────────────────

CREATE TABLE audit_log (
    log_id      INT          PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(50)  NOT NULL,   -- 'account','branch','employee','pix', etc.
    entity_id   INT          NOT NULL,
    action      VARCHAR(255) NOT NULL,
    log_date    DATETIME     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE security_log (
    log_id      INT          PRIMARY KEY AUTO_INCREMENT,
    account_id  INT          NOT NULL,
    action      VARCHAR(255) NOT NULL,   -- 'login','password_change','2fa','lock', etc.
    log_date    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id)
);

-- ── Segurança de Dados (Criptografia) ───────────────────────────────────────

CREATE TABLE secure_data (
    id             INT          PRIMARY KEY AUTO_INCREMENT,
    data_name      VARCHAR(255) NOT NULL,
    encrypted_data BLOB         NOT NULL,    -- armazenar dados cifrados
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- ── Agendamento ──────────────────────────────────────────────────────────────

CREATE TABLE employee_schedule (
    schedule_id  INT     PRIMARY KEY AUTO_INCREMENT,
    employee_id  INT     NOT NULL,
    work_date    DATE    NOT NULL,
    shift_start  TIME    NOT NULL,
    shift_end    TIME    NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- ── Verificação de Conta & Reset de Senha ────────────────────────────────────

CREATE TABLE account_verification (
    verification_id   INT          PRIMARY KEY AUTO_INCREMENT,
    account_id        INT          NOT NULL,
    verification_code VARCHAR(20)  NOT NULL,
    verification_date DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id)
);

CREATE TABLE password_reset (
    reset_id    INT          PRIMARY KEY AUTO_INCREMENT,
    account_id  INT          NOT NULL,
    reset_token VARCHAR(255) NOT NULL,
    reset_date  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id)
);


-- ═══════════════════════════════════════════════════════════════════════════
--  QUERIES — 22 consultas corrigidas (alinhadas ao schema acima)
-- ═══════════════════════════════════════════════════════════════════════════

-- 1. Listar todos os funcionários
SELECT * FROM employee;

-- 2. Listar clientes de uma filial específica (ex: branch_id = 101)
SELECT c.*
FROM customer c
JOIN account a ON c.customer_id = a.customer_id
WHERE a.branch_id = 101;

-- 3. Contas com saldo maior que 50.000
SELECT * FROM account WHERE balance > 50000;

-- 4. Empréstimos com valor menor que 500.000
SELECT * FROM loan WHERE loan_amount < 500000;

-- 5. Transações realizadas em dinheiro
SELECT * FROM transaction WHERE transaction_mode = 'Cash';

-- 6. Nome do funcionário com nome da filial
SELECT e.employee_name, b.branch_name
FROM employee e
JOIN branch b ON e.branch_id = b.branch_id;

-- 7. Clientes e seus números de conta
SELECT c.customer_name, a.account_number
FROM customer c
JOIN account a ON c.customer_id = a.customer_id;

-- 8. Transações com nome do cliente e tipo de conta
SELECT t.transaction_id, c.customer_name, a.account_type, t.amount, t.transaction_date
FROM transaction t
JOIN account a   ON t.account_id = a.account_id
JOIN customer c  ON a.customer_id = c.customer_id;

-- 9. Total de clientes por filial
SELECT b.branch_name, COUNT(DISTINCT a.customer_id) AS total_customers
FROM branch b
LEFT JOIN account a ON b.branch_id = a.branch_id
GROUP BY b.branch_name;

-- 10. Saldo total por filial
SELECT b.branch_name, SUM(a.balance) AS total_balance
FROM branch b
JOIN account a ON b.branch_id = a.branch_id
GROUP BY b.branch_name;

-- 11. Cliente com maior empréstimo
SELECT c.customer_name, l.loan_amount
FROM customer c
JOIN account a ON c.customer_id = a.customer_id
JOIN loan    l ON a.account_id  = l.account_id
WHERE l.loan_amount = (SELECT MAX(loan_amount) FROM loan);

-- 12. Clientes sem conta bancária
SELECT c.customer_name
FROM customer c
WHERE c.customer_id NOT IN (SELECT customer_id FROM account);

-- 13. Funcionários com salário acima da média
SELECT employee_name, salary
FROM employee
WHERE salary > (SELECT AVG(salary) FROM employee);

-- 14. Filial com maior número de contas
SELECT b.branch_name, COUNT(a.account_id) AS total_accounts
FROM branch b
JOIN account a ON b.branch_id = a.branch_id
GROUP BY b.branch_name
ORDER BY total_accounts DESC
LIMIT 1;

-- 15. Clientes que fizeram transações acima de 100.000
SELECT DISTINCT c.customer_name, t.amount
FROM customer c
JOIN account     a ON c.customer_id = a.customer_id
JOIN transaction t ON a.account_id  = t.account_id
WHERE t.amount > 100000;

-- 16. Total de empréstimos por filial
SELECT b.branch_name, SUM(l.loan_amount) AS total_loan
FROM branch b
JOIN account a ON b.branch_id  = a.branch_id
JOIN loan    l ON a.account_id = l.account_id
GROUP BY b.branch_name;

-- 17. Aumentar salário dos funcionários da filial 101 em 10%
UPDATE employee
SET salary = salary * 1.10
WHERE branch_id = 101;

-- 18. Deletar contas com saldo zero
DELETE FROM account WHERE balance = 0;

-- 19. Alterar transações de 'Cheque' para 'Cash'
UPDATE transaction
SET transaction_mode = 'Cash'
WHERE transaction_mode = 'Cheque';

-- 20. Saldo médio por tipo de conta
SELECT account_type, AVG(balance) AS avg_balance
FROM account
GROUP BY account_type;

-- 21. Clientes com empréstimo mas sem conta
SELECT DISTINCT c.customer_name
FROM customer c
JOIN account a ON c.customer_id = a.customer_id
JOIN loan    l ON a.account_id  = l.account_id
WHERE c.customer_id NOT IN (SELECT customer_id FROM account WHERE balance > 0);

-- 22. Filial com menor número de funcionários
SELECT b.branch_name, COUNT(e.employee_id) AS total_employees
FROM branch b
LEFT JOIN employee e ON b.branch_id = e.branch_id
GROUP BY b.branch_name
ORDER BY total_employees ASC
LIMIT 1;


-- ═══════════════════════════════════════════════════════════════════════════
-- NOTA SOBRE CRIPTOGRAFIA (T-SQL / SQL Server):
-- O bloco abaixo usa ENCRYPTBYPASSPHRASE que é específico do SQL Server.
-- Em MySQL, use AES_ENCRYPT / AES_DECRYPT:
--
--   INSERT INTO secure_data (data_name, encrypted_data)
--   VALUES ('senha_joao', AES_ENCRYPT('SenhaForte@2024', 'MinhaChaveSecreta123!'));
--
--   SELECT CAST(AES_DECRYPT(encrypted_data, 'MinhaChaveSecreta123!') AS CHAR)
--   FROM secure_data WHERE data_name = 'senha_joao';
-- ═══════════════════════════════════════════════════════════════════════════