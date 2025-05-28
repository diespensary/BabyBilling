CREATE TABLE subscriber (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    msisdn VARCHAR(20) NOT NULL UNIQUE,
    operator VARCHAR(50) NOT NULL
);

CREATE TABLE cdr_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    call_type CHAR(2),
    first_subscriber VARCHAR(20),
    second_subscriber VARCHAR(20),
    start_time TIMESTAMP,
    end_time TIMESTAMP
);
