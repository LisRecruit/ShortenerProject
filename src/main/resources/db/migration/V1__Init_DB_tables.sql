CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE short_urls (
    id INT PRIMARY KEY AUTO_INCREMENT,
    short_url VARCHAR(255) UNIQUE NOT NULL,
    origin_url VARCHAR(255) NOT NULL,
    date_of_creating DATE,
    date_of_expiring DATE,
    count_of_transition LONG,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);