CREATE TABLE [curso] (
    sigla             NVARCHAR(10)  NOT NULL PRIMARY KEY,
    nome              NVARCHAR(150) NOT NULL,
    siglaDepartamento NVARCHAR(10)  NOT NULL REFERENCES [departamento](sigla),
    propina           DECIMAL(10,2) NOT NULL DEFAULT 0,
    estado            NVARCHAR(20)  NOT NULL DEFAULT 'Ativo'
);
