SET time_zone = '+00:00';

CREATE TABLE users (
    id              BIGINT       PRIMARY KEY,
    username        VARCHAR(100) UNIQUE NOT NULL,
    email           VARCHAR(100) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    verification_token   VARCHAR(255),
    enabled              BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT valid_username CHECK (CHAR_LENGTH(TRIM(username)) > 0),
    CONSTRAINT valid_users_id CHECK (id BETWEEN 0 AND 9999)
);

CREATE TABLE user_groups (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    admin_id    BIGINT       NOT NULL,
    name        VARCHAR(100) UNIQUE NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (admin_id) REFERENCES users(id),
    CONSTRAINT valid_group_name CHECK (CHAR_LENGTH(TRIM(name)) > 0)
);

CREATE TABLE group_members (
    id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    group_id    BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    join_time   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (group_id) REFERENCES user_groups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT uq_membership UNIQUE (group_id, user_id)
);

CREATE TABLE friendships (
    id                  BIGINT      AUTO_INCREMENT PRIMARY KEY,
    user_id_a      BIGINT      NOT NULL,
    user_id_b    BIGINT      NOT NULL,
    status      VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id_a) REFERENCES users(id),
    FOREIGN KEY (user_id_b) REFERENCES users(id),

    CONSTRAINT valid_direction UNIQUE (user_id_a, user_id_b),
    CONSTRAINT valid_friendships_status CHECK (
      status IN ('PENDING', 'ACCEPTED', 'REJECTED')
    ),
    CONSTRAINT different_friendship_users CHECK (user_id_a <> user_id_b)
);

CREATE TABLE bills (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id          BIGINT NOT NULL,
    creator_id        BIGINT NOT NULL,
    description       VARCHAR(255) NOT NULL,
    total_amount      DECIMAL(10,2) NOT NULL,
    currency          VARCHAR(3) NOT NULL DEFAULT 'EUR',
    participant_names VARCHAR(255),
    status            VARCHAR(10) NOT NULL DEFAULT 'COMPLETED',
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (group_id) REFERENCES user_groups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT positive_bills_amount CHECK (total_amount > 0),
    CONSTRAINT valid_bill_status CHECK (status IN ('PENDING', 'COMPLETED', 'DELETED')),
    CONSTRAINT valid_currency_code CHECK (CHAR_LENGTH(TRIM(currency)) = 3)
);


CREATE TABLE expense_splits (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    group_id    BIGINT        NOT NULL,
    total_owed  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_paid  DECIMAL(10,2) NOT NULL DEFAULT 0.00,

    CONSTRAINT no_duplicate_membership UNIQUE (user_id, group_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (group_id)REFERENCES user_groups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT positive_total_owed CHECK (total_owed >= 0),
    CONSTRAINT positive_total_paid CHECK (total_paid >= 0)
);

CREATE TABLE settlements (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id        BIGINT NOT NULL,
    debtor_id       BIGINT NOT NULL,
    creditor_id     BIGINT NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    status          VARCHAR(10) NOT NULL DEFAULT 'CONFIRMED',
    confirmed_by_id BIGINT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (group_id) REFERENCES user_groups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (debtor_id) REFERENCES users(id),
    FOREIGN KEY (creditor_id) REFERENCES users(id),
    FOREIGN KEY (confirmed_by_id) REFERENCES users(id),

    CONSTRAINT positive_settlement_amount CHECK (amount > 0),
    CONSTRAINT different_settlement_users CHECK (debtor_id <> creditor_id),
    CONSTRAINT valid_settlement_status CHECK (status IN ('CONFIRMED', 'REVERTED')),
    CONSTRAINT valid_settlement_confirmer CHECK (
            confirmed_by_id = debtor_id
            OR confirmed_by_id = creditor_id
	)
);