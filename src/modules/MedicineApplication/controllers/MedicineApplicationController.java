package modules.MedicineApplication.controllers;

import config.Database;
import modules.MedicineApplication.models.MedicineApplication;
import modules.MedicineApplication.models.MedicineApplication.Frequency;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Controller para CRUD de aplicações de medicamento.
 *
 * Convenções assumidas na tabela public.medicine_applications:
 *  - application_uuid (PK, UUID, default gen_random_uuid())  [opcional]
 *  - medicine_uuid (UUID)    NOT NULL
 *  - user_uuid (UUID)        NOT NULL
 *  - animal_uuid (UUID)      NOT NULL
 *  - applied_at (timestamptz) NOT NULL
 *  - quantity (numeric)      NOT NULL
 *  - next_application_at (timestamptz) NULL
 *  - frequency (text)        NOT NULL (valores do enum Frequency.name())
 *  - ends_at (timestamptz)   NULL
 *  - created_at (timestamptz) DEFAULT now()
 */
public class MedicineApplicationController {

    /** Cria uma nova aplicação */
    public void create(MedicineApplication application) throws SQLException {
        String sql = """
            INSERT INTO public.medicine_applications
            (medicine_uuid, user_uuid, animal_uuid, applied_at, quantity, next_application_at, frequency, ends_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, application.getMedicineUuid());
            pstmt.setObject(2, application.getUserUuid());
            pstmt.setObject(3, application.getAnimalUuid());
            pstmt.setTimestamp(4, Timestamp.from(application.getAppliedAt().toInstant()));
            pstmt.setBigDecimal(5, application.getQuantity());

            if (application.getNextApplicationAt() != null) {
                pstmt.setTimestamp(6, Timestamp.from(application.getNextApplicationAt().toInstant()));
            } else {
                pstmt.setNull(6, Types.TIMESTAMP_WITH_TIMEZONE);
            }

            pstmt.setString(7,
                    application.getFrequency() != null ? application.getFrequency().name() : Frequency.DOES_NOT_REPEAT.name()
            );

            if (application.getEndsAt() != null) {
                pstmt.setTimestamp(8, Timestamp.from(application.getEndsAt().toInstant()));
            } else {
                pstmt.setNull(8, Types.TIMESTAMP_WITH_TIMEZONE);
            }

            pstmt.executeUpdate();
        }
    }

    /** Lista todas as aplicações de um animal (para a listagem) */
    public static List<MedicineApplication> getApplicationsForAnimal(Connection conn, UUID animalUuid) throws SQLException {
        String sql = """
            SELECT application_uuid, medicine_uuid, user_uuid, animal_uuid,
                   applied_at, quantity, next_application_at, frequency, ends_at, created_at
              FROM public.medicine_applications
             WHERE animal_uuid = ?
             ORDER BY applied_at DESC, created_at DESC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, animalUuid);
            try (ResultSet rs = ps.executeQuery()) {
                List<MedicineApplication> out = new ArrayList<>();
                while (rs.next()) out.add(mapRow(rs));
                return out;
            }
        }
    }

    /** (Opcional) Buscar uma aplicação específica por UUID */
    public static MedicineApplication getByUuid(Connection conn, UUID applicationUuid) throws SQLException {
        String sql = """
            SELECT application_uuid, medicine_uuid, user_uuid, animal_uuid,
                   applied_at, quantity, next_application_at, frequency, ends_at, created_at
              FROM public.medicine_applications
             WHERE application_uuid = ?
             LIMIT 1
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, applicationUuid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Deleta uma aplicação pelo UUID */
    public static void delete(Connection conn, UUID applicationUuid) throws SQLException {
        String sql = "DELETE FROM public.medicine_applications WHERE application_uuid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, applicationUuid);
            ps.executeUpdate();
        }
    }

    /** Mapper centralizado ResultSet -> Model */
    private static MedicineApplication mapRow(ResultSet rs) throws SQLException {
        MedicineApplication m = new MedicineApplication();

        Object appId = rs.getObject("application_uuid");
        if (appId instanceof UUID u) m.setApplicationUuid(u);

        Object medicineId = rs.getObject("medicine_uuid");
        if (medicineId instanceof UUID u) m.setMedicineUuid(u);

        Object userId = rs.getObject("user_uuid");
        if (userId instanceof UUID u) m.setUserUuid(u);

        Object animalId = rs.getObject("animal_uuid");
        if (animalId instanceof UUID u) m.setAnimalUuid(u);

        Timestamp appliedTs = rs.getTimestamp("applied_at");
        if (appliedTs != null) {
            m.setAppliedAt(ZonedDateTime.ofInstant(appliedTs.toInstant(), ZoneId.systemDefault()));
        }

        m.setQuantity(rs.getBigDecimal("quantity"));

        Timestamp nextTs = rs.getTimestamp("next_application_at");
        if (nextTs != null) {
            m.setNextApplicationAt(ZonedDateTime.ofInstant(nextTs.toInstant(), ZoneId.systemDefault()));
        }

        String freq = rs.getString("frequency");
        if (freq != null && !freq.isBlank()) {
            try { m.setFrequency(Frequency.valueOf(freq)); }
            catch (IllegalArgumentException ex) { m.setFrequency(Frequency.DOES_NOT_REPEAT); }
        } else {
            m.setFrequency(Frequency.DOES_NOT_REPEAT);
        }

        Timestamp endsTs = rs.getTimestamp("ends_at");
        if (endsTs != null) {
            m.setEndsAt(ZonedDateTime.ofInstant(endsTs.toInstant(), ZoneId.systemDefault()));
        }

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            m.setCreatedAt(ZonedDateTime.ofInstant(createdTs.toInstant(), ZoneId.systemDefault()));
        }

        return m;
    }
}
