package modules.MedicineBrand.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import modules.MedicineBrand.controllers.MedicineBrandController;
import modules.MedicineBrand.models.MedicineBrand;
import modules.MedicineBrand.views.MedicineBrandView;
import config.Database;

import java.sql.Connection;
import java.sql.SQLException;

public class MedicineBrandForm extends VBox {

    private TextField nameField;
    private Connection conn;
    private BorderPane mainLayout;
    private MedicineBrandView medicineBrandView;

    /**
     * Construtor do formulário de cadastro de nova marca de medicamento.
     * 
     * @param mainLayout Layout principal para navegação
     * @param medicineBrandView View de marcas para recarregar dados após salvar
     */
    public MedicineBrandForm(BorderPane mainLayout, MedicineBrandView medicineBrandView) {
        this.mainLayout = mainLayout;
        this.medicineBrandView = medicineBrandView;
        this.setSpacing(20);
        this.setPadding(new Insets(30, 40, 30, 40));
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("form-bg");

        try {
            conn = Database.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setupComponents();
    }

    /**
     * Configura todos os componentes do formulário, incluindo campos e botões.
     */
    private void setupComponents() {
        // Título
        Label titleLabel = new Label("Cadastrar nova marca");
        titleLabel.getStyleClass().add("form-title");
        
        // Centraliza o título usando um HBox
        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 10, 0));

        // Nome
        Label nameLabel = new Label("Nome da marca:");
        nameLabel.getStyleClass().add("form-label");

        nameField = new TextField();
        nameField.setPromptText("Digite o nome da marca");
        nameField.setPrefWidth(580);
        nameField.setMaxWidth(580);
        nameField.getStyleClass().add("form-input");

        // Container fixo com largura do input - label à esquerda, input centralizado
        HBox nameInputWrapper = new HBox(nameField);
        nameInputWrapper.setAlignment(Pos.CENTER);
        nameInputWrapper.setPrefWidth(580);
        nameInputWrapper.setMaxWidth(580);
        
        VBox nameBox = new VBox(8, nameLabel, nameInputWrapper);
        nameBox.setPrefWidth(580);
        nameBox.setMaxWidth(580);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        // Botões
        Button saveButton = new Button("Salvar");
        saveButton.getStyleClass().add("form-btn-save");
        saveButton.setPrefWidth(150);
        saveButton.setOnAction(e -> saveBrand());

        Button backButton = new Button("Voltar");
        backButton.getStyleClass().add("form-btn-cancel");
        backButton.setPrefWidth(150);
        backButton.setOnAction(e -> goBack());

        HBox buttonBox = new HBox(15, backButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox formContent = new VBox(20, nameBox);
        formContent.setAlignment(Pos.CENTER);
        formContent.setPadding(new Insets(10, 0, 10, 0));

        this.getChildren().addAll(titleBox, formContent, buttonBox);
        this.getStylesheets().add(getClass().getResource("/modules/MedicineBrand/styles/MedicineBrandForm.css").toExternalForm());
    }

    /**
     * Salva uma nova marca de medicamento no banco de dados.
     * Valida se o nome foi preenchido antes de prosseguir.
     */
    private void saveBrand() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Por favor, digite o nome da marca.");
            alert.setTitle("Campo obrigatório");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        try {
            MedicineBrandController controller = new MedicineBrandController(conn);
            controller.insert(name);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Marca cadastrada com sucesso!");
            alert.setTitle("Sucesso");
            alert.setHeaderText(null);
            alert.showAndWait();
            // Recarrega os dados e volta para a view
            if (medicineBrandView != null) {
                medicineBrandView.loadData();
            }
            goBack();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao salvar a marca: " + e.getMessage());
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    /**
     * Volta para a view de lista de marcas.
     */
    private void goBack() {
        if (mainLayout != null && medicineBrandView != null) {
            mainLayout.setCenter(medicineBrandView);
        }
    }
}