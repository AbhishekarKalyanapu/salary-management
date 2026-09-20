-- V1: initial schema for ACME salary management (SQL Server)

CREATE TABLE currency (
    code        CHAR(3)        NOT NULL,
    rate_to_usd DECIMAL(18, 6) NOT NULL,   -- 1 unit of this currency = rate_to_usd USD
    as_of       DATE           NOT NULL,
    CONSTRAINT pk_currency PRIMARY KEY (code),
    CONSTRAINT ck_currency_rate CHECK (rate_to_usd > 0)
);

CREATE TABLE country (
    code          CHAR(2)       NOT NULL,
    name          NVARCHAR(100) NOT NULL,
    currency_code CHAR(3)       NOT NULL,
    CONSTRAINT pk_country PRIMARY KEY (code),
    CONSTRAINT fk_country_currency FOREIGN KEY (currency_code) REFERENCES currency (code)
);

CREATE TABLE department (
    id   INT IDENTITY(1, 1) NOT NULL,
    name NVARCHAR(100)      NOT NULL,
    CONSTRAINT pk_department PRIMARY KEY (id),
    CONSTRAINT uq_department_name UNIQUE (name)
);

CREATE TABLE job_title (
    id            INT IDENTITY(1, 1) NOT NULL,
    title         NVARCHAR(150)      NOT NULL,
    department_id INT                NOT NULL,
    CONSTRAINT pk_job_title PRIMARY KEY (id),
    CONSTRAINT uq_job_title UNIQUE (title),
    CONSTRAINT fk_job_title_department FOREIGN KEY (department_id) REFERENCES department (id)
);

CREATE TABLE employee (
    id            BIGINT IDENTITY(1, 1) NOT NULL,
    first_name    NVARCHAR(100)         NOT NULL,
    last_name     NVARCHAR(100)         NOT NULL,
    email         NVARCHAR(255)         NOT NULL,
    country_code  CHAR(2)               NOT NULL,
    department_id INT                   NOT NULL,
    job_title_id  INT                   NOT NULL,
    level         TINYINT               NOT NULL,          -- 1 (junior) .. 6 (executive)
    hire_date     DATE                  NOT NULL,
    salary        DECIMAL(18, 2)        NOT NULL,          -- annual, in local currency
    currency_code CHAR(3)               NOT NULL,
    created_at    DATETIME2(0)          NOT NULL CONSTRAINT df_employee_created DEFAULT SYSUTCDATETIME(),
    updated_at    DATETIME2(0)          NOT NULL CONSTRAINT df_employee_updated DEFAULT SYSUTCDATETIME(),
    row_version   ROWVERSION            NOT NULL,          -- optimistic concurrency
    CONSTRAINT pk_employee PRIMARY KEY (id),
    CONSTRAINT uq_employee_email UNIQUE (email),
    CONSTRAINT ck_employee_salary CHECK (salary > 0),
    CONSTRAINT ck_employee_level CHECK (level BETWEEN 1 AND 6),
    CONSTRAINT fk_employee_country FOREIGN KEY (country_code) REFERENCES country (code),
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES department (id),
    CONSTRAINT fk_employee_job_title FOREIGN KEY (job_title_id) REFERENCES job_title (id),
    CONSTRAINT fk_employee_currency FOREIGN KEY (currency_code) REFERENCES currency (code)
);

CREATE TABLE salary_history (
    id             BIGINT IDENTITY(1, 1) NOT NULL,
    employee_id    BIGINT                NOT NULL,
    old_salary     DECIMAL(18, 2)        NULL,             -- NULL for the initial record
    new_salary     DECIMAL(18, 2)        NOT NULL,
    currency_code  CHAR(3)               NOT NULL,
    effective_date DATE                  NOT NULL,
    reason         NVARCHAR(255)         NULL,
    created_at     DATETIME2(0)          NOT NULL CONSTRAINT df_salary_history_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT pk_salary_history PRIMARY KEY (id),
    CONSTRAINT ck_salary_history_new CHECK (new_salary > 0),
    CONSTRAINT fk_salary_history_employee FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE CASCADE,
    CONSTRAINT fk_salary_history_currency FOREIGN KEY (currency_code) REFERENCES currency (code)
);

-- Indexes chosen for the directory filters and the insight queries.
CREATE INDEX ix_employee_country_title ON employee (country_code, job_title_id) INCLUDE (salary, currency_code);
CREATE INDEX ix_employee_department    ON employee (department_id) INCLUDE (salary, currency_code);
CREATE INDEX ix_employee_name          ON employee (last_name, first_name);
CREATE INDEX ix_employee_salary        ON employee (salary);
CREATE INDEX ix_salary_history_employee ON salary_history (employee_id, effective_date DESC);
