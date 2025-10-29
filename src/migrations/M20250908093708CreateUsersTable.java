package migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class M20250908093708CreateUsersTable implements Migration {

    @Override
    public String name() {
        return "20250908093708_create_users_table";
    }

    @Override
    public void up (Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE EXTENSION IF NOT EXISTS "pgcrypto";
                CREATE EXTENSION IF NOT EXISTS "citext";
                CREATE EXTENSION IF NOT EXISTS "pgcrypto"; -- ensure bcrypt via crypt()
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS public.users (
                    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    name VARCHAR(255) NOT NULL,
                    email CITEXT NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL, -- password hash (bcrypt)
                    reset_code VARCHAR(255),
                    reset_code_expires_at TIMESTAMP,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                );
            """);

            st.execute("""
                INSERT INTO public.users (name, email, password)
                VALUES (
                    'QuantIT',
                    'quantit.focinhoamigo@gmail.com',
                    crypt('QuantIT@007', gen_salt('bf'))
                )
                ON CONFLICT (email) DO NOTHING;
            """);
        }
    }

    @Override
    public void down (Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS public.users;");
        }
    }
}