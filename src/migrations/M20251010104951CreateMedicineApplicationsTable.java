package migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class M20251010104951CreateMedicineApplicationsTable implements Migration {

    @Override
    public String name() {
        return "20251010104951_create_medicine_applications_table";
    }

    @Override
    public void up(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS public.medicine_applications (
                    application_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    medicine_uuid UUID NOT NULL REFERENCES public.medicines(uuid),
                    user_uuid UUID NOT NULL REFERENCES public.users(uuid),
                    animal_uuid UUID NOT NULL REFERENCES public.animals(uuid),
                    applied_at TIMESTAMP NOT NULL,
                    quantity INTEGER,
                    next_application_at TIMESTAMP,
                    frequency VARCHAR(10) CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ANNUALLY', 'EVERY_WEEKDAY')),
                    ends_at TIMESTAMP,
                    google_calendar_id TEXT,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
