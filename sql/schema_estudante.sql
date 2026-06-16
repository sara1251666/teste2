CREATE TABLE [estudante] (
    numMec         INT           NOT NULL PRIMARY KEY,
    email          NVARCHAR(255) NOT NULL UNIQUE,
    nome           NVARCHAR(100) NOT NULL,
    nif            NVARCHAR(9)   NOT NULL UNIQUE,
    morada         NVARCHAR(255),
    dataNascimento NVARCHAR(20),
    anoInscricao   INT           NOT NULL,
    siglaCurso     NVARCHAR(10)  NOT NULL REFERENCES [curso](sigla),
    saldoDevedor   DECIMAL(10,2) NOT NULL DEFAULT 0,
    anoCurricular  INT           NOT NULL DEFAULT 1
);
