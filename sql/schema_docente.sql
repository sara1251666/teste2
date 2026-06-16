CREATE TABLE [docente] (
    sigla          NVARCHAR(10)  NOT NULL PRIMARY KEY,
    email          NVARCHAR(255) NOT NULL UNIQUE,
    nome           NVARCHAR(100) NOT NULL,
    nif            NVARCHAR(9)   NOT NULL UNIQUE,
    morada         NVARCHAR(255),
    dataNascimento NVARCHAR(20)
);
