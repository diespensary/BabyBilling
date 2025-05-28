CREATE TABLE abonent (
    abonent_id      SERIAL PRIMARY KEY,
    abonent_name    VARCHAR(255) NOT NULL,
    msisdn          VARCHAR(11)  NOT NULL,
    balance         DECIMAL(5,1) NOT NULL,
    date_registration DATE       NOT NULL,
    tariff_id        INT      NOT NULL
);

CREATE TABLE cdr_record (
    id                 SERIAL      PRIMARY KEY,
    call_type          CHAR(2)     NOT NULL,
    first_subscriber   VARCHAR(20) NOT NULL,
    second_subscriber  VARCHAR(20) NOT NULL,
    start_time         TIMESTAMP   NOT NULL,
    end_time           TIMESTAMP   NOT NULL,
    talk_time_seconds  BIGINT      NOT NULL,
    processed          BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE TABLE tariff_balance (
    id                 SERIAL      PRIMARY KEY,
    tariff_balance     FLOAT       NOT NULL,
    date_last_billing  DATE        NOT NULL,
    abonent_id         INT         NOT NULL,
    CONSTRAINT fk_tariff_balance_abonent
        FOREIGN KEY (abonent_id)
        REFERENCES abonent(abonent_id)
);
