package modules.Attachment.services;

import com.google.api.client.http.FileContent;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GoogleDriveSharedFolderService {
    
    private static final String APPLICATION_NAME = "Friendly Paw";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(
        "https://www.googleapis.com/auth/drive.file"
    );
    
    // ID da pasta compartilhada (configure aqui após criar e compartilhar)
    private static final String SHARED_FOLDER_ID = System.getenv("GOOGLE_DRIVE_SHARED_FOLDER_ID") != null ? 
        System.getenv("GOOGLE_DRIVE_SHARED_FOLDER_ID") : "";
    
    private static final String SERVICE_ACCOUNT_FILE = System.getenv("GOOGLE_SERVICE_ACCOUNT_FILE") != null ? 
        System.getenv("GOOGLE_SERVICE_ACCOUNT_FILE") : "friendly-paw-calendar.json";
    
    private static Drive driveService;

    /**
     * Obtém instância do serviço Drive usando Service Account.
     */
    private static Drive getDriveService() throws IOException, GeneralSecurityException {
        if (driveService == null) {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            
            // Carregar credenciais da Service Account
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                new FileInputStream(SERVICE_ACCOUNT_FILE)
            ).createScoped(SCOPES);
            
            driveService = new Drive.Builder(
                httpTransport,
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
            ).setApplicationName(APPLICATION_NAME).build();
        }
        return driveService;
    }

    /**
     * Faz upload de um arquivo para a pasta compartilhada.
     */
    public static String uploadFile(java.io.File file) throws IOException, GeneralSecurityException {
        if (SHARED_FOLDER_ID.isEmpty()) {
            throw new IllegalStateException(
                "GOOGLE_DRIVE_SHARED_FOLDER_ID não configurado. " +
                "Crie uma pasta no seu Google Drive, compartilhe com a service account " +
                "e configure a variável de ambiente com o ID da pasta."
            );
        }
        
        Drive service = getDriveService();
        String mimeType = getMimeType(file.getName());
        
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getName();
        fileMetadata.setName(uniqueFileName);
        fileMetadata.setParents(Arrays.asList(SHARED_FOLDER_ID));
        
        FileContent mediaContent = new FileContent(mimeType, file);
        
        File uploadedFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id, name, webViewLink")
                .execute();
        
        // Tornar arquivo público
        Permission permission = new Permission();
        permission.setType("anyone");
        permission.setRole("reader");
        service.permissions().create(uploadedFile.getId(), permission).execute();
        
        System.out.println("✓ Arquivo enviado para Google Drive (Service Account)");
        System.out.println("  ID: " + uploadedFile.getId());
        
        return uploadedFile.getWebViewLink();
    }

    /**
     * Deleta um arquivo do Google Drive.
     */
    public static boolean deleteFile(String fileId) {
        try {
            Drive service = getDriveService();
            service.files().delete(fileId).execute();
            System.out.println("✓ Arquivo deletado do Google Drive");
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao deletar arquivo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extrai o ID do arquivo de uma URL.
     */
    public static String extractFileIdFromUrl(String driveUrl) {
        if (driveUrl == null || !driveUrl.contains("drive.google.com")) {
            return null;
        }
        
        int startIndex = driveUrl.indexOf("/d/");
        if (startIndex != -1) {
            startIndex += 3;
            int endIndex = driveUrl.indexOf("/", startIndex);
            if (endIndex == -1) endIndex = driveUrl.length();
            if (startIndex > 2 && endIndex > startIndex) {
                return driveUrl.substring(startIndex, endIndex);
            }
        }
        return null;
    }

    /**
     * Determina o tipo MIME do arquivo.
     */
    private static String getMimeType(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) return "application/octet-stream";
        
        String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        
        switch (extension) {
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "mp4": return "video/mp4";
            case "avi": return "video/x-msvideo";
            case "mov": return "video/quicktime";
            case "txt": return "text/plain";
            default: return "application/octet-stream";
        }
    }
}
