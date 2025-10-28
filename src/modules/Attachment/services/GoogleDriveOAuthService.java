package modules.Attachment.services;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import java.io.*;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import java.nio.charset.StandardCharsets;
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
     * Cria o arquivo credentials.json a partir das variáveis de ambiente.
     */
    private static void createCredentialsFileIfNeeded() throws IOException {
        java.io.File credentialsFile = new java.io.File("src/main/resources/credentials.json");
        
        // Criar diretório se não existir
        if (!credentialsFile.getParentFile().exists()) {
            credentialsFile.getParentFile().mkdirs();
        }
        
        // Criar arquivo se não existir
        if (!credentialsFile.exists()) {
            if (GOOGLE_CLIENT_ID == null || GOOGLE_CLIENT_SECRET == null) {
                throw new IllegalStateException(
                    "GOOGLE_DRIVE_CLIENT_ID e GOOGLE_DRIVE_CLIENT_SECRET devem estar configuradas"
                );
            }
            
            String credentialsJson = String.format(
                "{\n" +
                "  \"installed\": {\n" +
                "    \"client_id\": \"%s\",\n" +
                "    \"project_id\": \"friendly-paw\",\n" +
                "    \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                "    \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                "    \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                "    \"client_secret\": \"%s\",\n" +
                "    \"redirect_uris\": [\n" +
                "      \"http://localhost:3000/oauth2callback\"\n" +
                "    ]\n" +
                "  }\n" +
                "}",
                GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
            );
            
            try (FileWriter writer = new FileWriter(credentialsFile)) {
                writer.write(credentialsJson);
            }
        }
    }

    /**
     * Captura o código de autorização do servidor local.
     */
    private static String captureAuthorizationCode() throws IOException {
        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        
        try {
            localServer = HttpServer.create(new InetSocketAddress(3000), 0);
            
            localServer.createContext("/oauth2callback", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                
                String response;
                if (query != null && query.contains("code=")) {
                    String code = query.substring(query.indexOf("code=") + 5);
                    int endIndex = code.indexOf("&");
                    if (endIndex != -1) {
                        code = code.substring(0, endIndex);
                    }
                    codeFuture.complete(code);
                    
                    // Redirecionar para a pasta do Google Drive após 2 segundos
                    response = "<html><head><meta charset='UTF-8'>"
                            + "<meta http-equiv='refresh' content='2;url=https://drive.google.com/drive/folders/17N1UjDUDPE-aMwXbEazzMwkgljKmPy_9'>"
                            + "<script>setTimeout(function(){ window.close(); }, 2000);</script>"
                            + "<style>body { font-family: Arial; padding: 40px; text-align: center; }</style></head>"
                            + "<body><h1 style='color: green;'>✅ Autorização Bem-Sucedida!</h1>"
                            + "<p>Redirecionando para o Google Drive...</p>"
                            + "<p style='color: #666; font-size: 12px;'>Esta página será fechada automaticamente.</p>"
                            + "</body></html>";
                } else if (query != null && query.contains("error=")) {
                    String error = query.substring(query.indexOf("error=") + 6);
                    codeFuture.completeExceptionally(new IOException("Erro de autorização: " + error));
                    
                    response = "<html><body><h1>Erro de Autorização</h1>"
                            + "<p>Erro: " + error + "</p>"
                            + "</body></html>";
                } else {
                    codeFuture.completeExceptionally(new IOException("Código de autorização não encontrado"));
                    response = "<html><body><h1>Erro</h1><p>Token não encontrado</p></body></html>";
                }
                
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });
            
            localServer.setExecutor(null);
            localServer.start();
            
            System.out.println("\n=== Aguardando autorização... ===");
            System.out.println("Por favor, autorize a aplicação no navegador.");
            
            // Aguardar o código (com timeout de 60 segundos)
            try {
                String code = codeFuture.get();
                return code;
            } catch (Exception e) {
                throw new IOException("Timeout ou erro ao aguardar autorização: " + e.getMessage());
            }
            
        } finally {
            if (localServer != null) {
                localServer.stop(0);
            }
        }
    }

    /**
     * Obtém ou cria credenciais autorizadas usando OAuth 2.0.
     */
    private static Credential getCredentials() throws IOException, GeneralSecurityException {
        if (credential != null && credential.getRefreshToken() != null) {
            return credential;
        }

        if (GOOGLE_CLIENT_ID == null || GOOGLE_CLIENT_SECRET == null) {
            throw new IllegalStateException(
                "Variáveis GOOGLE_DRIVE_CLIENT_ID e GOOGLE_DRIVE_CLIENT_SECRET não configuradas."
            );
        }

        createCredentialsFileIfNeeded();
        
        HttpTransport httpTransport = new NetHttpTransport.Builder().build();
        
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
            BearerToken.authorizationHeaderAccessMethod(),
            httpTransport,
            JSON_FACTORY,
            new GenericUrl("https://oauth2.googleapis.com/token"),
            new ClientParametersAuthentication(GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET),
            GOOGLE_CLIENT_ID,
            "https://accounts.google.com/o/oauth2/auth"
        )
        .setScopes(SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
        .build();

        // Tentar carregar credenciais salvas
        Credential storedCredential = flow.loadCredential("user");
        if (storedCredential != null && storedCredential.getRefreshToken() != null) {
            System.out.println("✓ Usando credenciais salvas (sem abrir navegador)");
            credential = storedCredential;
            return storedCredential;
        }
        
        System.out.println("⚠ Credenciais não encontradas. Abrindo navegador para autorizar...");

        // Solicitar autorização
        String authorizationUrl = flow.newAuthorizationUrl()
            .setRedirectUri(REDIRECT_URI)
            .build();
        
        // Abrir navegador
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(authorizationUrl));
        } catch (Exception e) {
            System.out.println("\n=== AUTORIZAÇÃO NECESSÁRIA ===");
            System.out.println("Abra este URL no navegador:");
            System.out.println(authorizationUrl);
        }
        
        // Capturar código de autorização
        String code = captureAuthorizationCode();
        
        // Trocar código por tokens
        AuthorizationCodeTokenRequest tokenRequest = flow.newTokenRequest(code);
        tokenRequest.setRedirectUri(REDIRECT_URI);
        TokenResponse tokenResponse = tokenRequest.execute();
        
        credential = flow.createAndStoreCredential(tokenResponse, "user");
        
        System.out.println("✓ Credenciais salvas! Próximas vezes não precisará autorizar novamente.");
        
        return credential;
    }

    /**
     * Cria ou obtém uma instância do serviço Drive (singleton).
     */
    private static Drive getDriveServiceInstance() throws IOException, GeneralSecurityException {
        if (driveService == null) {
            credential = getCredentials();
            HttpTransport httpTransport = new NetHttpTransport.Builder().build();
            
            driveService = new Drive.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
        return driveService;
    }

    /**
     * Busca ou cria a pasta "friendly-paw" no Google Drive.
     */
    private static String getOrCreateFriendlyPawFolder() throws IOException, GeneralSecurityException {
        if (friendlyPawFolderId != null) {
            return friendlyPawFolderId;
        }
        
        Drive service = getDriveServiceInstance();
        
        String query = "name='" + FOLDER_NAME + "' and mimeType='application/vnd.google-apps.folder' and trashed=false";
        FileList result = service.files().list()
                .setQ(query)
                .setPageSize(1)
                .execute();
        
        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            friendlyPawFolderId = result.getFiles().get(0).getId();
        } else {
            com.google.api.services.drive.model.File folderMetadata = new com.google.api.services.drive.model.File();
            folderMetadata.setName(FOLDER_NAME);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
            
            com.google.api.services.drive.model.File folder = service.files().create(folderMetadata)
                    .setFields("id, name")
                    .execute();
            
            friendlyPawFolderId = folder.getId();
            
            Permission permission = new Permission();
            permission.setType("anyone");
            permission.setRole("reader");
            service.permissions().create(folder.getId(), permission).execute();
        }
        
        return friendlyPawFolderId;
    }

    /**
     * Faz upload de um arquivo para o Google Drive dentro da pasta "friendly-paw".
     */
    public static String uploadFile(java.io.File file) throws IOException, GeneralSecurityException {
        Drive service = getDriveServiceInstance();
        String folderId = getOrCreateFriendlyPawFolder();
        String mimeType = getMimeType(file.getName());
        
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getName();
        fileMetadata.setName(uniqueFileName);
        fileMetadata.setParents(Arrays.asList(folderId));
        
        FileContent mediaContent = new FileContent(mimeType, file);
        
        com.google.api.services.drive.model.File uploadedFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id, name, webViewLink")
                .execute();
        
        Permission permission = new Permission();
        permission.setType("anyone");
        permission.setRole("reader");
        service.permissions().create(uploadedFile.getId(), permission).execute();
        
        return uploadedFile.getWebViewLink();
    }

    /**
     * Deleta um arquivo do Google Drive.
     */
    public static boolean deleteFile(String fileId) {
        try {
            Drive service = getDriveServiceInstance();
            service.files().delete(fileId).execute();
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao deletar arquivo do Google Drive: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extrai o ID do arquivo a partir de uma URL do Google Drive.
     */
    public static String extractFileIdFromUrl(String driveUrl) {
        if (driveUrl == null || !driveUrl.contains("drive.google.com")) {
            return null;
        }
        
        int startIndex = driveUrl.indexOf("/d/");
        if (startIndex != -1) {
            startIndex += 3;
            int endIndex = driveUrl.indexOf("/", startIndex);
            if (endIndex == -1) {
                endIndex = driveUrl.length();
            }
            if (startIndex > 2 && endIndex > startIndex) {
                return driveUrl.substring(startIndex, endIndex);
            }
        }
        
        return null;
    }

    /**
     * Determina o tipo MIME do arquivo baseado na extensão.
     */
    private static String getMimeType(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "application/octet-stream";
        }
        
        String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "txt":
                return "text/plain";
            default:
                return "application/octet-stream";
        }
    }
}
