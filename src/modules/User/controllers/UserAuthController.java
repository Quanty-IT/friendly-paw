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

    public static User login(Connection conn, String email, String password) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(LOGIN_SQL)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                UUID uuid = (UUID) rs.getObject("uuid");
                String name = rs.getString("name");
                String mail = rs.getString("email");
                Timestamp cat = rs.getTimestamp("created_at");
                Timestamp uat = rs.getTimestamp("updated_at");

                return new User(
                        uuid,
                        name,
                        mail,
                        cat != null ? cat.toLocalDateTime() : LocalDateTime.now(),
                        uat != null ? uat.toLocalDateTime() : LocalDateTime.now()
                );
            }
        }
    }
}
