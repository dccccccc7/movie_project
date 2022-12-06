CREATE TABLE movies (
	id VARCHAR(10) NOT NULL,
	title VARCHAR(100) NOT NULL,
	year INTEGER NOT NULL,
	director VARCHAR(100) NOT NULL,
	PRIMARY KEY (id),
	FULLTEXT (title)
);

CREATE TABLE stars (
	id VARCHAR(10) NOT NULL,
	name VARCHAR(100) NOT NULL,
	birthYear INTEGER,
	PRIMARY KEY (id)
);

CREATE TABLE stars_in_movies (
	starId VARCHAR(10) NOT NULL,
	movieId VARCHAR(10) NOT NULL,
	FOREIGN KEY (starId) REFERENCES stars(id),
	FOREIGN KEY (movieId) REFERENCES movies(id),
    PRIMARY KEY (starId, movieId)
);

CREATE TABLE genres (
	id INTEGER NOT NULL AUTO_INCREMENT,
	name VARCHAR(32) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE genres_in_movies (
	genreId INTEGER NOT NULL,
	movieId VARCHAR(10) NOT NULL,
	FOREIGN KEY (genreId) REFERENCES genres(id),
	FOREIGN KEY (movieId) REFERENCES movies(id),
    PRIMARY KEY (genreId, movieId)
);

CREATE TABLE creditcards (
	id VARCHAR(20) NOT NULL,
	firstName VARCHAR(50) NOT NULL,
	lastName VARCHAR(50) NOT NULL,
	expiration DATE NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE customers (
	id INTEGER NOT NULL AUTO_INCREMENT,
	firstName VARCHAR(50) NOT NULL,
	lastName VARCHAR(50) NOT NULL,
	ccId VARCHAR(20) NOT NULL,
	address VARCHAR(200) NOT NULL,
	email VARCHAR(50) NOT NULL,
	PASSWORD varchar(20) NOT NULL,
	FOREIGN KEY (ccId) REFERENCES creditcards(id),
	PRIMARY KEY (id)
);

CREATE TABLE employees (
    email varchar(50) primary key,
    password varchar(20) not null,
    fullname varchar(100)
);

CREATE TABLE sales (
	id INTEGER NOT NULL AUTO_INCREMENT,
	customerId INTEGER NOT NULL,
	movieId VARCHAR(10) NOT NULL,
	saleDate DATE NOT NULL,
	FOREIGN KEY (customerId) REFERENCES customers(id),
	FOREIGN KEY (movieId) REFERENCES movies(id),
	PRIMARY KEY (id)
);

CREATE TABLE ratings (
	movieId VARCHAR(10) NOT NULL,
	rating FLOAT NOT NULL,
	numVotes INTEGER NOT NULL,
	FOREIGN KEY (movieId) REFERENCES movies(id)
);