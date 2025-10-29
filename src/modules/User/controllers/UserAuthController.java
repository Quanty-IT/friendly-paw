package modules.User.controllers;

import modules.User.models.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserAuthController {

    private static final String LOGIN_SQL = """
        SELECT uuid, name, email, created_at, updated_at
        FROM public.users
        WHERE email = ? AND password = crypt(?, password)
        LIMIT 1
    """;

    /**
     * Autentica um usuário através de email e senha.
     * 
     * @param conn Conexão com o banco de dados
     * @param email Email do usuário para autenticação
     * @param password Senha do usuário para autenticação
     * @return Objeto User se as credenciais forem válidas, null caso contrário
     * @throws SQLException Se ocorrer erro na operação do banco de dados
     */
    public static User login(Connection conn, String email, String password) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(LOGIN_SQL)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                UUID uuid = (UUID) rs.getObject("uuid");
                String name = rs.getString("name");
                String email = rs.getString("email");
                Timestamp createdAt = rs.getTimestamp("created_at");
                Timestamp updatedAt = rs.getTimestamp("updated_at");

                return new User(
                        uuid,
                        name,
                        email,
                        createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now(),
                        updatedAt != null ? updatedAt.toLocalDateTime() : LocalDateTime.now()
                );
            }
        }
    }
}
