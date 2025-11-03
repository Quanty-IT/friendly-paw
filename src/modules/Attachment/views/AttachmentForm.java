package modules.Attachment.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import modules.Attachment.controllers.AttachmentController;
import modules.Attachment.models.Attachment;
import modules.Attachment.services.GoogleDriveOAuthService;
import modules.Animal.views.AnimalView;
import config.Database;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
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

        // === estilo base (mesmo padrão do AnimalForm/MedicineForm) ===
        getStyleClass().add("form-bg");
        setPadding(new Insets(30, 40, 30, 40));
        setHgap(20);
        setVgap(14);
        setAlignment(Pos.TOP_CENTER);
        getStylesheets().add(getClass().getResource("/modules/Attachment/styles/AttachmentForm.css").toExternalForm());

        // ===== título =====
        Label title = new Label("Adicionar anexo");
        title.getStyleClass().add("form-title");
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);
        add(titleBox, 0, 0, 4, 1);

        // ===== campos =====
        double HALF = 280, FULL = 580;

        // Botão Selecionar + campo nome/URL (lado a lado)
        selectFileButton = new Button("Selecionar arquivo");
        selectFileButton.getStyleClass().add("form-btn-upload");
        selectFileButton.setPrefWidth(HALF);
        selectFileButton.setOnAction(e -> selectFile());

        urlField.setPromptText("Arquivo selecionado / URL (Drive)");
        urlField.setEditable(false);
        urlField.setPrefWidth(HALF);
        urlField.getStyleClass().add("form-input-readonly");

        descriptionField.setPromptText("Descrição (opcional)");
        descriptionField.setPrefRowCount(5);
        descriptionField.setPrefWidth(FULL);
        descriptionField.getStyleClass().add("form-textarea");

        // Linhas no mesmo layout helper do AnimalForm
        int r = 1;
        addRowHalf("Arquivo:", selectFileButton, "Nome/URL:", urlField, r++, HALF);
        addRowFull("Descrição:", descriptionField, r++, FULL);

        // ===== botões =====
        Button backButton = new Button("Voltar");
        backButton.getStyleClass().add("form-btn-cancel");
        backButton.setPrefWidth(150);
        backButton.setOnAction(e ->
            mainLayout.setCenter(new modules.Attachment.views.AttachmentView(mainLayout, animalUuid))
        );

        Button saveButton = new Button("Salvar");
        saveButton.getStyleClass().add("form-btn-save");
        saveButton.setPrefWidth(150);
        saveButton.setOnAction(e -> saveAttachment());

        HBox buttonBox = new HBox(15, backButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER);
        add(buttonBox, 0, r, 4, 1);

        setPrefSize(520, 600);
    }

    /**
     * Adiciona uma linha completa no formulário com label e campo de largura total.
     * 
     * @param labelText Texto do label
     * @param field Campo de controle a ser adicionado
     * @param row Número da linha no GridPane
     * @param width Largura total do campo
     */
    private void addRowFull(String labelText, Control field, int row, double width) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        HBox wrapper = new HBox(field);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefWidth(width);
        wrapper.setMaxWidth(width);

        VBox box = new VBox(8, label, wrapper);
        box.setPrefWidth(width);
        box.setMaxWidth(width);
        box.setAlignment(Pos.CENTER_LEFT);

        add(box, 0, row, 4, 1);
    }

    /**
     * Adiciona uma linha no formulário com dois campos lado a lado.
     * 
     * @param l1 Texto do label do primeiro campo
     * @param f1 Primeiro campo de controle
     * @param l2 Texto do label do segundo campo
     * @param f2 Segundo campo de controle
     * @param row Número da linha no GridPane
     * @param width Largura de cada campo (metade da largura total)
     */
    private void addRowHalf(String l1, Control f1, String l2, Control f2, int row, double width) {
        VBox left = buildLabeledBox(l1, f1, width);
        VBox right = buildLabeledBox(l2, f2, width);

        HBox rowBox = new HBox(20, left, right);
        rowBox.setAlignment(Pos.CENTER);

        add(rowBox, 0, row, 4, 1);
    }

    /**
     * Cria um VBox com label e campo de controle para uso em layouts de formulário.
     * 
     * @param labelText Texto do label
     * @param field Campo de controle
     * @param width Largura do campo
     * @return VBox configurado com label e campo
     */
    private VBox buildLabeledBox(String labelText, Control field, double width) {
        if (labelText == null || labelText.isBlank()) labelText = " ";

        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        HBox wrapper = new HBox(field);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefWidth(width);
        wrapper.setMaxWidth(width);

        VBox box = new VBox(8, label, wrapper);
        box.setPrefWidth(width);
        box.setMaxWidth(width);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /**
     * Abre um diálogo de seleção de arquivo e armazena o arquivo selecionado.
     * O nome do arquivo é exibido no campo urlField após a seleção.
     */
    private void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar arquivo");
        FileChooser.ExtensionFilter images = new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg");
        FileChooser.ExtensionFilter pdf = new FileChooser.ExtensionFilter("PDF", "*.pdf");
        FileChooser.ExtensionFilter all = new FileChooser.ExtensionFilter("Todos suportados", "*.png", "*.jpg", "*.jpeg", "*.pdf");
        fileChooser.getExtensionFilters().addAll(all, images, pdf);

        selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Mostra o nome do arquivo no campo (continua read-only)
            urlField.setText(selectedFile.getName());
        }
    }

    /**
     * Salva um novo anexo no banco de dados e faz upload para o Google Drive.
     * Valida se um arquivo foi selecionado antes de prosseguir.
     * 
     * @throws IOException Se ocorrer erro ao fazer upload do arquivo para o Google Drive (tratado internamente com Alert)
     * @throws GeneralSecurityException Se ocorrer erro na autenticação com Google Drive (tratado internamente com Alert)
     * @throws SQLException Se ocorrer erro na operação do banco de dados (tratado internamente com Alert)
     */
    private void saveAttachment() {
        if (selectedFile == null) {
            new Alert(Alert.AlertType.WARNING, "Por favor, selecione um arquivo para fazer upload.").showAndWait();
            return;
        }

        String description = descriptionField.getText();
        if (description != null && description.trim().isEmpty()) description = null;

        try {
            String fileUrl = GoogleDriveOAuthService.uploadFile(selectedFile, this.animalUuid);

            Attachment attachment = new Attachment(
                null,
                fileUrl,
                description,
                this.animalUuid,
                LocalDateTime.now()
            );

            AttachmentController.addAttachment(conn, attachment);
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
