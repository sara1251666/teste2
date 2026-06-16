CREATE TABLE [historicoAcademico] (
    anoLetivo INT          NOT NULL,
    numMec    INT          NOT NULL REFERENCES [estudante](numMec),
    siglaUC   NVARCHAR(10) NOT NULL,
    notas     NVARCHAR(100),
    estado    NVARCHAR(20),
    PRIMARY KEY (anoLetivo, numMec, siglaUC)
);
