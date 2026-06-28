CREATE DATABASE IF NOT EXISTS tabled;
USE tables;
SET time_zone = '+00:00';

CREATE TABLE users (
    id              BIGINT       PRIMARY KEY,
    username        VARCHAR(100) UNIQUE NOT NULL,
    email           VARCHAR(100) UNIQUE NOT NULL,
    password_hash 	VARCHAR(255) NOT NULL,

    CONSTRAINT valid_username CHECK (CHAR_LENGTH(TRIM(username)) > 0), -- reject empty usernames and/or containing only whitespaces
    CONSTRAINT valid_users_id CHECK (id BETWEEN 0 AND 9999) -- user ID must be in range 0-9999
);

CREATE TABLE user_groups (
    id       		BIGINT       PRIMARY KEY AUTO_INCREMENT,
    admin_id		BIGINT		 NOT NULL,
    name      		VARCHAR(100) UNIQUE NOT NULL,
    created_at      TIMESTAMP	 NOT NULL DEFAULT CURRENT_TIMESTAMP, -- set current time as default group's creation time
            
	FOREIGN KEY (admin_id) REFERENCES users(id),
    CONSTRAINT valid_group_name CHECK (CHAR_LENGTH(TRIM(name)) > 0) -- reject empty group names and/or containing only whitespaces
    
);

CREATE TABLE group_members (
	id			  BIGINT		 PRIMARY KEY AUTO_INCREMENT,
    group_id      BIGINT       	 NOT NULL,
    user_id       BIGINT       	 NOT NULL,
    join_time     TIMESTAMP	 	 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (group_id) REFERENCES user_groups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT no_duplicate_membership UNIQUE (group_id, user_id)
);

CREATE TABLE friendships (
    id              	BIGINT       PRIMARY KEY AUTO_INCREMENT,
    request_sender  	BIGINT       NOT NULL,
    request_receiver  	BIGINT       NOT NULL,
    friends_status  	VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    created_at      	TIMESTAMP 	 NOT NULL DEFAULT CURRENT_TIMESTAMP,
	
    FOREIGN KEY (request_sender) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (request_receiver) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT valid_direction UNIQUE (request_sender, request_receiver),
    CONSTRAINT valid_friendships_status CHECK (
        friends_status IN ('PENDING', 'ACCEPTED', 'REJECTED') -- define 3 statuses of a friendship
    )
);

CREATE TABLE bills (
    id              	BIGINT        PRIMARY KEY AUTO_INCREMENT,
    group_id        	BIGINT		  NOT NULL,
    creator_id      	BIGINT    	  NOT NULL,
    bills_description	VARCHAR(200)  NOT NULL,
    total_amount    	DECIMAL(10,2) NOT NULL,
    currency        	VARCHAR(3)    NOT NULL DEFAULT 'EUR',
    participants		VARCHAR(100),
    bills_status 		VARCHAR(10)   NOT NULL DEFAULT 'PAID',
    created_at      	TIMESTAMP	  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (group_id) REFERENCES user_groups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT positive_bills_amount CHECK (total_amount > 0), -- ensure that the amount to be splitted is always larger than 0
    CONSTRAINT valid_bills_status CHECK (
        bills_status IN ('PENDING', 'PAID', 'DELETED') -- statuses of a bill
    )
);

CREATE TABLE expense_splits (
    id              BIGINT     	  PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT        NOT NULL,
    group_id        BIGINT        NOT NULL,
    total_owed      DECIMAL(10,2) NOT NULL DEFAULT 0.00, -- total amount that a user owes others
    total_paid      DECIMAL(10,2) NOT NULL DEFAULT 0.00, -- total amount that a user is being owed

    CONSTRAINT no_duplicate_membership UNIQUE (user_id, group_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES user_groups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT positive_total_owed CHECK (total_owed >= 0), -- the total amount that a user owes others must be positive
    CONSTRAINT positive_total_paid CHECK (total_paid >= 0) -- the total amount that a user is owed others must be positive
);

CREATE TABLE messages (
    id              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    sender_id       BIGINT       NOT NULL,
    receiver_id    	BIGINT       NOT NULL,
    group_id        BIGINT       NOT NULL,
    bill_id         BIGINT       NOT NULL,
    message_type    VARCHAR(20)  NOT NULL DEFAULT 'TEXT',
    content         TEXT         NULL,
    created_at      TIMESTAMP	 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (sender_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES user_groups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (bill_id) REFERENCES bills(id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT valid_messages_type CHECK (
        message_type IN ('TEXT', 'BILL')
    )
);
