
package modules.Attachment.controllers;

import modules.Attachment.models.Attachment;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AttachmentController {

    private static final String INSERT_SQL = """
        INSERT INTO public.attachments
        (url, description, animal_id, created_at)
        VALUES (?, ?, ?, now())
        """;

    private static final String SELECT_BY_ANIMAL_SQL = "SELECT * FROM public.attachments WHERE animal_id = ?";

    private static final String DELETE_SQL = "DELETE FROM public.attachments WHERE id = ?";

    public static void addAttachment(Connection conn, Attachment a) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, a.getUrl());
            ps.setString(2, a.getDescription());
            ps.setObject(3, a.getAnimalId());
            ps.executeUpdate();
        }
    }

    public static void deleteAttachment(Connection conn, UUID attachmentId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setObject(1, attachmentId);
            ps.executeUpdate();
        }
    }

    public static List<Attachment> getAttachmentsForAnimal(Connection conn, UUID animalId) throws SQLException {
        List<Attachment> attachments = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ANIMAL_SQL)) {
            ps.setObject(1, animalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID id = (UUID) rs.getObject("id");
                    String url = rs.getString("url");
                    String description = rs.getString("description");
                    Timestamp cat = rs.getTimestamp("created_at");

                    Attachment a = new Attachment(
                            id,
                            url,
                            description,
                            animalId,
                            cat != null ? cat.toLocalDateTime() : LocalDateTime.now()
                    );
                    attachments.add(a);
                }
            }
        }
        return attachments;
    }
}
