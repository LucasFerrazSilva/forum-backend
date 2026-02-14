package com.ferraz.forumbackend.infra.exception;

public class DatabaseException extends BaseException {
    public DatabaseException(Exception e) {
        super(
            "Erro ao conectar no banco de dados",
            503,
            "DatabaseException",
            "Verifique se as configurações de conexão com o banco de dados estão corretas",
            e
        );

    }
}
