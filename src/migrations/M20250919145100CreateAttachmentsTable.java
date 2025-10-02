package migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class M20250919145100CreateAttachmentsTable implements Migration {

    @Override
    public String name() {
        return "20250919145100_createattachmentstable";
    }

    @Override
    public void up(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS public.attachments (
                    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                    url text NOT NULL,
                    description text,
                    animal_id uuid NOT NULL,
                    created_at timestamptz NOT NULL DEFAULT now(),
                    CONSTRAINT fk_animal
                        FOREIGN KEY(animal_id) 
	                    REFERENCES animals(id)
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