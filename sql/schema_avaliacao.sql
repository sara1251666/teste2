CREATE TABLE [avaliacao] (
    numMec    INT          NOT NULL REFERENCES [estudante](numMec),
    siglaUC   NVARCHAR(10) NOT NULL,
    anoLetivo INT          NOT NULL,
    nota1     DECIMAL(4,2),
    nota2     DECIMAL(4,2),
    nota3     DECIMAL(4,2),
    PRIMARY KEY (numMec, siglaUC, anoLetivo)
);
