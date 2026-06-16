CREATE TABLE [pagamento] (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    numMec        INT           NOT NULL REFERENCES [estudante](numMec),
    valorPago     DECIMAL(10,2) NOT NULL,
    dataPagamento NVARCHAR(20)  NOT NULL
);
