package migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class M20250915210000CreateMedicines implements Migration {

    @Override
    public String name() {
        return "20250915210000_create_medicines";
    }

    @Override
    public void up(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE medicines (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    brand_id INTEGER REFERENCES medicine_brands(id) ON DELETE CASCADE,
                    quantity INTEGER DEFAULT 0,
                    description TEXT,
                    is_active BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                );
            """);
        }
    }

    @Override
    public void down(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS medicines;");
        }
    }
}