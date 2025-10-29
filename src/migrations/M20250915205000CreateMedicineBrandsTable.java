package migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class M20250915205000CreateMedicineBrandsTable implements Migration {

    @Override
    public String name() {
        return "20250915205000_create_medicine_brands";
    }

    @Override
    public void up(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS public.medicine_brands (
                    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    name VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                );
            """);
        }
    }

    @Override
    public void down(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS public.medicine_brands;");
        }
    }
}
