
package modules.Attachment.controllers;

import modules.Attachment.models.Attachment;
import modules.Attachment.services.GoogleDriveOAuthService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AttachmentController {

    private static final String INSERT_SQL = """
        INSERT INTO public.attachments
        (file, description, animal_uuid, created_at)
        VALUES (?, ?, ?, now())
        """;

    private static final String SELECT_BY_ANIMAL_SQL = "SELECT * FROM public.attachments WHERE animal_uuid = ?";

    private static final String DELETE_SQL = "DELETE FROM public.attachments WHERE uuid = ?";

    public static void addAttachment(Connection conn, Attachment attachment) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, attachment.getFile());
            ps.setString(2, attachment.getDescription());
            ps.setObject(3, attachment.getAnimalUuid());
            ps.executeUpdate();
        }
    }

    public static void deleteAttachment(Connection conn, UUID attachmentId) throws SQLException {
        // First, get the attachment to delete the file from Google Drive
        String selectSql = "SELECT file FROM public.attachments WHERE uuid = ?";
        
        try (PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
            selectPs.setObject(1, attachmentId);
            try (ResultSet rs = selectPs.executeQuery()) {
                if (rs.next()) {
                    String url = rs.getString("file");
                    // Delete file from Google Drive
                    if (url != null && url.contains("drive.google.com")) {
                        String fileUuid = GoogleDriveOAuthService.extractFileIdFromUrl(url);
                        if (fileUuid != null) {
                            GoogleDriveOAuthService.deleteFile(fileUuid);
                        }
                    }
                }
            }
        }
        
        // Now delete from database
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
                    UUID uuid = (UUID) rs.getObject("uuid");
                    String file = rs.getString("file");
                    String description = rs.getString("description");
                    Timestamp createdAt = rs.getTimestamp("created_at");

                    Attachment attachment = new Attachment(
                            uuid,
                            file,
                            description,
                            animalId,
                            createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now()
                    );
                    attachments.add(attachment);
                }
            }
        }
        return attachments;
    }
}
