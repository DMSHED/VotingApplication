--liquibase formatted sql


--changeset dmshed:1
CREATE TABLE IF NOT EXISTS topic
(
    id SERIAL PRIMARY KEY ,
    name VARCHAR(64) NOT NULL UNIQUE ,
    created_by VARCHAR(32) NOT NULL
);

--changeset dmshed:2
CREATE TABLE IF NOT EXISTS vote
(
    id SERIAL PRIMARY KEY ,
    name VARCHAR(64) NOT NULL UNIQUE ,
    description TEXT ,
    topic_id INT REFERENCES topic (id) ON DELETE CASCADE

);

--changeset dmshed:3
CREATE TABLE IF NOT EXISTS vote_results
(
    vote_id INT REFERENCES vote (id) ON DELETE CASCADE ,
    name VARCHAR(32) ,
    count INT ,
    PRIMARY KEY (vote_id, name)
);
