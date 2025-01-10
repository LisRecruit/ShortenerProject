CREATE TABLE users (
    id BIGINT  PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE short_urls (
    id BIGINT  PRIMARY KEY AUTO_INCREMENT,
    short_url VARCHAR(255) UNIQUE NOT NULL,
    origin_url VARCHAR(255) NOT NULL,
    date_of_creating DATE NOT NULL,
    date_of_expiring DATE,
    count_of_transition BIGINT DEFAULT 0,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);