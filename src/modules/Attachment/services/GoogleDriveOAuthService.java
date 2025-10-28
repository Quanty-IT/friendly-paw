package modules.Attachment.services;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import java.io.*;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GoogleDriveOAuthService {
    
    private static final String APPLICATION_NAME = "Friendly Paw";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String FOLDER_NAME = "friendly-paw";
    
    // OAuth 2.0 credentials
    private static final String GOOGLE_CLIENT_ID = System.getenv("GOOGLE_DRIVE_CLIENT_ID");
    private static final String GOOGLE_CLIENT_SECRET = System.getenv("GOOGLE_DRIVE_CLIENT_SECRET");
    private static final String REDIRECT_URI = "http://localhost:3000/oauth2callback";
    
    private static final List<String> SCOPES = Arrays.asList(
        DriveScopes.DRIVE, 
        DriveScopes.DRIVE_FILE
    );
    
    private static Drive driveService;
    private static String friendlyPawFolderId = null;
    private static Credential credential;
    private static HttpServer localServer;
    
    /**
     * Captura o código de autorização do servidor local.
     */
    private static String captureAuthorizationCode() throws IOException {
        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        
        try {
            localServer = HttpServer.create(new InetSocketAddress(3000), 0);
            
            localServer.createContext("/oauth2callback", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                
                if (query != null && query.contains("code=")) {
                    String code = query.substring(query.indexOf("code=") + 5, 
                        query.contains("&") ? query.indexOf("&") : query.length());
                    codeFuture.complete(code);
                    exchange.close();
                } else {
                    String error = query != null && query.contains("error=") 
                        ? query.substring(query.indexOf("error=") + 6)
                        : "Código não encontrado";
                    codeFuture.completeExceptionally(new IOException(error));
                    
                    String response = "Erro: " + error;
                    exchange.sendResponseHeaders(400, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            });
            
            localServer.setExecutor(null);
            localServer.start();
            System.out.println("Aguardando autorização no navegador...");
            
            String code = codeFuture.get();
            return code;
        } catch (Exception e) {
            throw new IOException("Erro ao autorizar: " + e.getMessage());
        } finally {
            if (localServer != null) localServer.stop(0);
        }
    }

    /**
     * Obtém ou cria credenciais autorizadas usando OAuth 2.0.
     */
    private static Credential getCredentials() throws IOException, GeneralSecurityException {
        if (credential != null && credential.getRefreshToken() != null) return credential;

        if (GOOGLE_CLIENT_ID == null || GOOGLE_CLIENT_SECRET == null) {
            throw new IllegalStateException("GOOGLE_DRIVE_CLIENT_ID e GOOGLE_DRIVE_CLIENT_SECRET devem estar configuradas");
        }
        
        HttpTransport httpTransport = new NetHttpTransport.Builder().build();
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
            BearerToken.authorizationHeaderAccessMethod(), httpTransport, JSON_FACTORY,
            new GenericUrl("https://oauth2.googleapis.com/token"),
            new ClientParametersAuthentication(GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET),
            GOOGLE_CLIENT_ID, "https://accounts.google.com/o/oauth2/auth")
            .setScopes(SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .build();

        Credential storedCredential = flow.loadCredential("user");
        if (storedCredential != null && storedCredential.getRefreshToken() != null) {
            credential = storedCredential;
            return credential;
        }

        String authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
        
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(authorizationUrl));
        } catch (Exception e) {
            System.out.println("Abra este URL no navegador:\n" + authorizationUrl);
        }
        
        String code = captureAuthorizationCode();
        TokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
        credential = flow.createAndStoreCredential(tokenResponse, "user");
        
        return credential;
    }

    /**
     * Cria ou obtém uma instância do serviço Drive (singleton).
     */
    private static Drive getDriveServiceInstance() throws IOException, GeneralSecurityException {
        if (driveService == null) {
            credential = getCredentials();
            driveService = new Drive.Builder(new NetHttpTransport.Builder().build(), JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();
        }
        return driveService;
    }

    /**
     * Busca ou cria a pasta "friendly-paw" no Google Drive.
     */
    private static String getOrCreateFriendlyPawFolder() throws IOException, GeneralSecurityException {
        if (friendlyPawFolderId != null) return friendlyPawFolderId;
        
        Drive service = getDriveServiceInstance();
        String query = "name='" + FOLDER_NAME + "' and mimeType='application/vnd.google-apps.folder' and trashed=false";
        FileList result = service.files().list().setQ(query).setPageSize(1).execute();
        
        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            friendlyPawFolderId = result.getFiles().get(0).getId();
        } else {
            com.google.api.services.drive.model.File folder = service.files().create(
                new com.google.api.services.drive.model.File().setName(FOLDER_NAME)
                    .setMimeType("application/vnd.google-apps.folder"))
                .setFields("id").execute();
            friendlyPawFolderId = folder.getId();
            service.permissions().create(folder.getId(), 
                new Permission().setType("anyone").setRole("reader")).execute();
        }
        return friendlyPawFolderId;
    }

    /**
     * Busca ou cria a pasta do animal dentro de friendly-paw.
     */
    private static String getOrCreateAnimalFolder(UUID animalId) throws IOException, GeneralSecurityException {
        Drive service = getDriveServiceInstance();
        String animalFolderName = animalId.toString();
        String query = "name='" + animalFolderName + "' and mimeType='application/vnd.google-apps.folder' " +
                       "and parents in '" + getOrCreateFriendlyPawFolder() + "' and trashed=false";
        
        FileList result = service.files().list().setQ(query).setPageSize(1).execute();
        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            return result.getFiles().get(0).getId();
        }
        
        com.google.api.services.drive.model.File folder = service.files().create(
            new com.google.api.services.drive.model.File().setName(animalFolderName)
                .setMimeType("application/vnd.google-apps.folder")
                .setParents(Arrays.asList(getOrCreateFriendlyPawFolder())))
            .setFields("id").execute();
        
        service.permissions().create(folder.getId(), 
            new Permission().setType("anyone").setRole("reader")).execute();
        return folder.getId();
    }

    /**
     * Upload de arquivo para Google Drive organizado por animal.
     * Estrutura: friendly-paw/{animalId}/arquivo.png
     */
    public static String uploadFile(java.io.File file, UUID animalId) throws IOException, GeneralSecurityException {
        if (file == null || !file.exists()) {
            throw new IOException("Arquivo não existe ou é inválido");
        }
        
        String mimeType;
        try {
            mimeType = getMimeType(file.getName());
        } catch (IllegalArgumentException e) {
            throw new IOException("Formato de arquivo não suportado: " + e.getMessage());
        }
        
        Drive service = getDriveServiceInstance();
        String animalFolderId = getOrCreateAnimalFolder(animalId);
        
        String fileName = UUID.randomUUID().toString() + "_" + file.getName();
        
        com.google.api.services.drive.model.File uploadedFile = service.files().create(
            new com.google.api.services.drive.model.File()
                .setName(fileName)
                .setParents(Arrays.asList(animalFolderId)),
            new FileContent(mimeType, file))
            .setFields("id,webViewLink").execute();
        
        service.permissions().create(uploadedFile.getId(), 
            new Permission().setType("anyone").setRole("reader")).execute();
        
        return uploadedFile.getWebViewLink();
    }

    /**
     * Deleta um arquivo do Google Drive.
     */
    public static boolean deleteFile(String fileId) {
        try {
            getDriveServiceInstance().files().delete(fileId).execute();
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao deletar: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extrai o ID do arquivo a partir de uma URL do Google Drive.
     */
    public static String extractFileIdFromUrl(String driveUrl) {
        if (driveUrl == null || !driveUrl.contains("/d/")) return null;
        
        int start = driveUrl.indexOf("/d/") + 3;
        int end = driveUrl.indexOf("/", start);
        return end == -1 ? driveUrl.substring(start) : driveUrl.substring(start, end);
    }

    /**
     * Determina o tipo MIME do arquivo baseado na extensão.
     * Aceita apenas: PNG, JPEG, JPG e PDF.
     */
    private static String getMimeType(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "pdf" -> "application/pdf";
            default -> throw new IllegalArgumentException("Formato não suportado: " + ext + ". Aceito apenas: PNG, JPEG, JPG, PDF");
        };
    }
}
