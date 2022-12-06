DROP PROCEDURE IF EXISTS insert_star;
DROP PROCEDURE IF EXISTS insert_movie;
DROP PROCEDURE IF EXISTS insert_genre;
DROP PROCEDURE IF EXISTS insert_star_2;

DELIMITER $$
CREATE PROCEDURE insert_star(IN starName VARCHAR(100), IN starYear INTEGER)
BEGIN
DECLARE max_id VARCHAR(10);
DECLARE max_id_int INTEGER;
DECLARE generated_id VARCHAR(10);

-- get max id from stars
SELECT max(id) FROM stars INTO max_id;
-- convert last 8 chars of the id to int and increment
SELECT CONVERT(SUBSTRING(max_id, 3, 10), UNSIGNED INTEGER) INTO max_id_int;
SET max_id_int = max_id_int + 1;
-- concatenate the newly incremented number into a new id
SELECT CONCAT('nm', CONVERT(max_id_int, CHAR)) INTO generated_id;
-- insert the star into the table with the newly generated id
INSERT INTO stars (id, name, birthYear) VALUES (generated_id, starName, starYear);
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE insert_genre(IN inputGenre VARCHAR(32), IN movieId VARCHAR(10))
BEGIN
DECLARE existing_genre_id INTEGER;
-- if genre already exists, just add to genres_in_movies table
-- else, add a new genre to genres table. Then, add to genres_in_movies_table
IF EXISTS(SELECT name FROM genres WHERE name = inputGenre LIMIT 1) THEN
SELECT id FROM genres WHERE name = inputGenre INTO existing_genre_id;
INSERT INTO genres_in_movies VALUES(existing_genre_id, movieId);
ELSE
	INSERT INTO genres (name) VALUES (inputGenre);
SELECT id FROM genres WHERE name = inputGenre INTO existing_genre_id;
INSERT INTO genres_in_movies VALUES(existing_genre_id, movieId);
END IF;

END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE insert_star_2(IN inputStar VARCHAR(100), IN movieId VARCHAR(10))
BEGIN
DECLARE existing_star_id VARCHAR(10);
-- if star already exists, just add to stars_in_movies table
-- else, add a new star to stars table. Then, add to stars_in_movies_table
IF EXISTS(SELECT name FROM stars WHERE name = inputStar LIMIT 1) THEN
SELECT id FROM stars WHERE name = inputStar LIMIT 1 INTO existing_star_id;
INSERT INTO stars_in_movies VALUES(existing_star_id, movieId);
ELSE
	CALL insert_star(inputStar, NULL);
SELECT id FROM stars WHERE name = inputStar LIMIT 1 INTO existing_star_id;
INSERT INTO stars_in_movies VALUES(existing_star_id, movieId);
END IF;

END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE insert_movie(IN inputTitle VARCHAR(100), IN inputYear INTEGER, IN inputDirector VARCHAR(100), IN inputStar VARCHAR(100), IN inputGenre VARCHAR(32))
BEGIN
DECLARE max_movie_id VARCHAR(10);
DECLARE max_movie_id_int INTEGER;
DECLARE generated_movie_id VARCHAR(10);
-- get max id from movies
SELECT max(id) FROM movies INTO max_movie_id;
-- convert last 8 chars of the id to int and increment
SELECT CONVERT(SUBSTRING(max_movie_id, 4, 10), UNSIGNED INTEGER) INTO max_movie_id_int;
SET max_movie_id_int = max_movie_id_int + 1;
-- concatenate the newly incremented number into a new id
SELECT CONCAT('ZZZ', CONVERT(max_movie_id_int, CHAR)) INTO generated_movie_id;

-- insert the movie into movie table
INSERT INTO movies (id, title, year, director) VALUES (generated_movie_id, inputTitle, inputYear, inputDirector);
-- insert genre
CALL insert_genre(inputGenre, generated_movie_id);
-- insert star
CALL insert_star_2(inputStar, generated_movie_id);

END $$
DELIMITER ;