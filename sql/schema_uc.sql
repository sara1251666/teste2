CREATE TABLE [uc] (
    sigla         NVARCHAR(10)  NOT NULL,
    nome          NVARCHAR(150) NOT NULL,
    anoCurricular INT           NOT NULL,
    siglaDocente  NVARCHAR(10)  REFERENCES [docente](sigla),
    siglaCurso    NVARCHAR(10)  NOT NULL REFERENCES [curso](sigla),
    numMomentos   INT           NOT NULL DEFAULT 1,
    PRIMARY KEY (sigla, siglaCurso)
);
