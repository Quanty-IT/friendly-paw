
package modules.Attachment.views;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import modules.Attachment.controllers.AttachmentController;
import modules.Attachment.models.Attachment;
import modules.Attachment.services.GoogleDriveOAuthService;
import modules.Animal.views.AnimalView;
import config.Database;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class AttachmentForm extends GridPane {

    private TextField urlField;
    private TextArea descriptionField;
    private File selectedFile;
    private Button selectFileButton;

    private Connection conn;
    private BorderPane mainLayout;
    private UUID animalUuid;

    /**
     * Construtor do formulário de anexos.
     * 
     * @param mainLayout Layout principal da aplicação para navegação entre telas
     * @param animalUuid UUID do animal associado ao anexo
     */
    public AttachmentForm(BorderPane mainLayout, UUID animalUuid) {
        this.mainLayout = mainLayout;
        this.animalUuid = animalUuid;

        urlField = new TextField();
        descriptionField = new TextArea();

        try {
            this.conn = Database.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Button saveButton = new Button("Salvar");
        saveButton.setOnAction(e -> saveAttachment());

        Button backButton = new Button("Voltar");
        backButton.setOnAction(e -> mainLayout.setCenter(new AnimalView(mainLayout)));

        HBox buttonBox = new HBox(10, saveButton, backButton);

        selectFileButton = new Button("Selecionar Arquivo");
        selectFileButton.setOnAction(e -> selectFile());

        this.setVgap(10);
        this.setHgap(10);
        this.setPadding(new Insets(20, 20, 20, 20));

        int row = 0;
        this.add(new Label("Arquivo:"), 0, row); 
        HBox fileBox = new HBox(10, selectFileButton);
        this.add(fileBox, 1, row++);
        
        this.add(new Label("URL do arquivo:"), 0, row); this.add(urlField, 1, row++);
        this.add(new Label("Descrição:"), 0, row); this.add(descriptionField, 1, row++);
        this.add(buttonBox, 1, row);

        urlField.setPrefWidth(240);
        urlField.setEditable(false);
        urlField.setStyle("-fx-background-color: #f0f0f0;");
        descriptionField.setPrefWidth(240);
        descriptionField.setPrefHeight(100);

        this.setPrefSize(520, 600);
    }

    /**
     * Abre um diálogo para seleção de arquivo (PNG, JPEG, JPG ou PDF).
     * O arquivo selecionado será usado para upload no Google Drive.
     */
    private void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Arquivo");
        
        // Configura os filtros de extensão de arquivos aceitos (PNG, JPEG, JPG ou PDF)
        FileChooser.ExtensionFilter images = new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg");
        FileChooser.ExtensionFilter pdf = new FileChooser.ExtensionFilter("PDF", "*.pdf");
        FileChooser.ExtensionFilter allSupported = new FileChooser.ExtensionFilter("Todos Suportados", "*.png", "*.jpg", "*.jpeg", "*.pdf");
        
        fileChooser.getExtensionFilters().addAll(allSupported, images, pdf);
        
        selectedFile = fileChooser.showOpenDialog(null);
        
        if (selectedFile != null) {
            urlField.setText(selectedFile.getName());
            System.out.println("Arquivo selecionado: " + selectedFile.getAbsolutePath());
        }
    }

    /**
     * Salva um anexo no banco de dados após fazer upload do arquivo no Google Drive.
     * Valida se um arquivo foi selecionado antes de prosseguir.
     */
    private void saveAttachment() {
        if (selectedFile == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText("Nenhum arquivo selecionado");
            alert.setContentText("Por favor, selecione um arquivo para fazer upload.");
            alert.showAndWait();
            return;
        }

        String description = descriptionField.getText();
        if (description != null && description.trim().isEmpty()) {
            description = null;
        }

        try {
            // Faz upload do arquivo para o Google Drive usando OAuth 2.0
            // O arquivo é organizado na pasta do animal específico
            String fileUrl = GoogleDriveOAuthService.uploadFile(selectedFile, this.animalUuid);
            System.out.println("Arquivo enviado para Google Drive: " + fileUrl);

            // Cria o objeto Attachment (o UUID é gerado automaticamente pelo banco de dados)
            Attachment attachment = new Attachment(
                    null, // UUID gerado automaticamente pelo banco de dados
                    fileUrl,
                    description,
                    this.animalUuid,
                    LocalDateTime.now()
            );

            AttachmentController.addAttachment(conn, attachment);
            System.out.println("Anexo adicionado com sucesso!");
            mainLayout.setCenter(new AttachmentView(mainLayout, this.animalUuid));
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao fazer upload do arquivo");
            alert.setContentText("Ocorreu um erro ao fazer upload do arquivo: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
