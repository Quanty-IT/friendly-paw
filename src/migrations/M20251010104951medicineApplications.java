package migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class M20251010104951medicineApplications implements Migration {

    @Override
    public String name() {
        return "20251010104951_medicineapplications";
    }



    @Override
    public void up(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS public.medicine_applications (
                    application_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    medicine_id INTEGER NOT NULL REFERENCES public.medicines(id),
                    user_uuid UUID NOT NULL REFERENCES public.users(id),
                    animal_uuid UUID NOT NULL REFERENCES public.animals(id),
                    applied_at TIMESTAMPTZ NOT NULL,
                    quantity NUMERIC,
                    next_application_at TIMESTAMPTZ,
                    frequency VARCHAR(100),
                    ends_at TIMESTAMPTZ,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                );
            """);
        }
    }

    @Override
    public void down(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
           st.execute("DROP TABLE IF EXISTS public.medicine_applications;");
        }
    }
}
