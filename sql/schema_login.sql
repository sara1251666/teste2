-- Schema da tabela de autenticação (SQL Server)
-- Executado automaticamente no arranque em modo SQL se a tabela não existir.

CREATE TABLE [login] (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    email         NVARCHAR(255) NOT NULL UNIQUE,
    passwordHash  NVARCHAR(500) NOT NULL,
    passwordSalt  NVARCHAR(255) NOT NULL,
    tipoUtilizador NVARCHAR(20) NOT NULL,   -- 'GESTOR' | 'DOCENTE' | 'ESTUDANTE'
    ativo         BIT NOT NULL DEFAULT 1,
    createdAt     DATETIME2 NOT NULL DEFAULT GETDATE(),
    updatedAt     DATETIME2 NOT NULL DEFAULT GETDATE()
);
