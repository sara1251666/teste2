CREATE TABLE [anoLetivo] (
    ano    INT          NOT NULL PRIMARY KEY,
    estado NVARCHAR(20) NOT NULL DEFAULT 'PLANEAMENTO'
);

CREATE TABLE [anoLetivoHistorico] (
    ano          INT          NOT NULL PRIMARY KEY,
    estado       NVARCHAR(20) NOT NULL,
    dataArquivo  NVARCHAR(30)
);
