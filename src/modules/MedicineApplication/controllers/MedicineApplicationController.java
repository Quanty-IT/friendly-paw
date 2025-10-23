package modules.MedicineApplication.controllers;

import config.Database;
import modules.MedicineApplication.models.MedicineApplication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MedicineApplicationController {

    /**
     * Cria uma nova aplicação de medicamento no banco de dados.
     * 
     * @param application Objeto MedicineApplication com os dados da aplicação
     * @throws SQLException Se ocorrer erro na operação do banco de dados
     */
    public void create(MedicineApplication application) throws SQLException {
        String sql = """
            INSERT INTO public.medicine_applications
            (medicine_id, user_uuid, animal_uuid, applied_at, quantity, next_application_at, frequency, ends_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, application.getMedicineId());
            pstmt.setObject(2, application.getUserUuid());
            pstmt.setObject(3, application.getAnimalUuid());
            pstmt.setTimestamp(4, Timestamp.from(application.getAppliedAt().toInstant()));
            pstmt.setBigDecimal(5, application.getQuantity());

            if (application.getNextApplicationAt() != null) {
                pstmt.setTimestamp(6, Timestamp.from(application.getNextApplicationAt().toInstant()));
            } else {
                pstmt.setNull(6, java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
            }

            pstmt.setString(7, application.getFrequency().name());

            if (application.getEndsAt() != null) {
                pstmt.setTimestamp(8, Timestamp.from(application.getEndsAt().toInstant()));
            } else {
                pstmt.setNull(8, java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
            }

            pstmt.executeUpdate();
        }
    }
}