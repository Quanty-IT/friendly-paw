package migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class M20250915210000CreateMedicinesTable implements Migration {

    @Override
    public String name() {
        return "20250915210000_create_medicines";
    }

    @Override
    public void up(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS public.medicines (
                    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    name VARCHAR(255) NOT NULL,
                    brand_uuid UUID REFERENCES public.medicine_brands(uuid) ON DELETE CASCADE,
                    quantity INTEGER DEFAULT -1,
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
            st.execute("DROP TABLE IF EXISTS public.medicines;");
        }
    }
}