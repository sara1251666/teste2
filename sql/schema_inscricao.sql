CREATE TABLE [inscricao] (
    numMec    INT          NOT NULL REFERENCES [estudante](numMec),
    siglaUC   NVARCHAR(10) NOT NULL,
    anoLetivo INT          NOT NULL REFERENCES [anoLetivo](ano),
    PRIMARY KEY (numMec, siglaUC, anoLetivo)
);
