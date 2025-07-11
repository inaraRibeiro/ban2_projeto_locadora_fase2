CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(200),
    UNIQUE (cpf)
);


CREATE TABLE genres (
    genre_id SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    rental_fee DECIMAL(6, 2) NOT NULL
);

CREATE TABLE movies (
    movie_id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    release_year INT NOT NULL,
    genre_id INT NOT NULL,
    director VARCHAR(100),
    duration_minutes INT,
    rating VARCHAR(10),
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (genre_id) REFERENCES genres(genre_id)
);


CREATE TABLE rentals (
    rental_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    rental_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP,
    rental_fee DECIMAL(6, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id)
);

INSERT INTO genres(title, rental_fee)
VALUES
('Drama', 7.00),
('Crime', 7.00),
('Terror', 8.00),
('Acao', 7.50),
('Comedia', 7.00),
('Sci-fi', 7.00),
('Infantil', 6.50);

INSERT INTO movies (title, release_year, genre_id, director, duration_minutes, rating)
VALUES
('Um Sonho de Liberdade', 1994, 1, 'Frank Darabont', 142, '18'),
('O Poderoso Chefao', 1972, 2, 'Francis Ford Coppola', 175, '18'),
('Pulp Fiction', 1994, 2, 'Quentin Tarantino', 154, '18'),
('Batman: O Cavaleiro das Trevas', 2008, 4, 'Christopher Nolan', 152, '13'),
('A Origem', 2010, 6, 'Christopher Nolan', 148, '13'),
('Procurando Nemo', 2003, 7, 'Andrew Stanton', 100, 'L');


INSERT INTO users (first_name, last_name, email, phone, cpf)
VALUES
('João', 'Silva', 'joão.silva@gmail.com', '47 91234-4321', '12332145602'),
('Maria', 'Joaquina', 'maria.joaquina@hotmail.com', '47 94321-1234', '12332145603'),
('Inara', 'Ribeiro', 'inara.bussiness@hotmail.com', '47 94321-4321', '12332145604');

insert into rentals (user_id, movie_id, rental_date, due_date, rental_fee, status)
values
(1, 2, '2025-04-10 19:54:06.308', '2025-04-15 19:54:06.308', 6.50, 'ACTIVE'),
(1, 1, '2025-04-10 19:54:06.308', '2025-04-15 19:54:06.308', 6.50, 'ACTIVE');

update movies set available_copies = 0 WHERE movie_id = 1;
update movies set available_copies = 0 WHERE movie_id = 2;