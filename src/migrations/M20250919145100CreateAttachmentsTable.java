package migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class M20250919145100CreateAttachmentsTable implements Migration {

    @Override
    public String name() {
        return "20250919145100_create_attachments_table";
    }

    @Override
    public void up(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS public.attachments (
                    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    file VARCHAR(255) NOT NULL,
                    description TEXT,
                    animal_uuid UUID NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_animal
                        FOREIGN KEY(animal_uuid) 
	                    REFERENCES public.animals(uuid)
                );
            """);
        }
    }

    @Override
    public void down(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS public.attachments;");
        }
    }
}