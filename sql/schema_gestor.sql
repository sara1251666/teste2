CREATE TABLE [gestor] (
    email          NVARCHAR(255) NOT NULL PRIMARY KEY,
    nome           NVARCHAR(100) NOT NULL,
    nif            NVARCHAR(9)   NOT NULL UNIQUE,
    morada         NVARCHAR(255),
    dataNascimento NVARCHAR(20)
);
