package modules.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import modules.controllers.MedicineController;
import modules.models.Medicine;
import config.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MedicineView extends VBox {

    private TableView<Medicine> table;
    private ObservableList<Medicine> medicinesList;
    private Connection conn;
    private MedicineController controller;
    private BorderPane mainLayout; // A referência para o layout principal

    public MedicineView(BorderPane mainLayout) {
        this.mainLayout = mainLayout; // Passa o mainLayout no construtor

        try {
            this.conn = Database.getConnection();
            this.controller = new MedicineController(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao conectar com o banco de dados.").showAndWait();
            return;
        }

        initializeComponents();
        setupLayout();
        loadData();
    }

    private void initializeComponents() {
        table = new TableView<>();
        medicinesList = FXCollections.observableArrayList();
        table.setItems(medicinesList);

        TableColumn<Medicine, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(50);

        TableColumn<Medicine, String> nameColumn = new TableColumn<>("Nome");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);

        TableColumn<Medicine, String> brandColumn = new TableColumn<>("Marca");
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        brandColumn.setPrefWidth(150);

        TableColumn<Medicine, Integer> quantityColumn = new TableColumn<>("Quantidade");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setPrefWidth(100);

        TableColumn<Medicine, String> descriptionColumn = new TableColumn<>("Descrição");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setPrefWidth(200);

        TableColumn<Medicine, Boolean> activeColumn = new TableColumn<>("Ativo");
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("isActive"));
        activeColumn.setPrefWidth(80);
        activeColumn.setCellFactory(column -> new TableCell<Medicine, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Sim" : "Não");
                }
            }
        });

        TableColumn<Medicine, LocalDateTime> createdAtColumn = new TableColumn<>("Data Criação");
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdAtColumn.setPrefWidth(130);
        createdAtColumn.setCellFactory(column -> new TableCell<Medicine, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        table.getColumns().addAll(idColumn, nameColumn, brandColumn, quantityColumn, descriptionColumn, activeColumn, createdAtColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupLayout() {
        // Botões na primeira linha
        Button addButton = new Button("Cadastrar");
        addButton.setOnAction(e -> openAddForm());

        Button brandsButton = new Button("Marcas");
        brandsButton.setOnAction(e -> openBrandView());

        // Botão "Voltar" para voltar à tela anterior
        Button backButton = new Button("Voltar");
        backButton.setOnAction(e -> {
            this.mainLayout.setCenter(new MenuView(this.mainLayout, null));
        });

        // Botões na segunda linha
        Button refreshButton = new Button("Atualizar");
        refreshButton.setOnAction(e -> loadData());

        Button editButton = new Button("Editar");
        editButton.setOnAction(e -> editSelected());

        Button deleteButton = new Button("Excluir");
        deleteButton.setOnAction(e -> deleteSelected());

        // HBox para a primeira linha de botões (Cadastrar, Medicamentos, Menu)
        HBox topButtonBox = new HBox(10);
        topButtonBox.getChildren().addAll(addButton, brandsButton, backButton);
        topButtonBox.setPadding(new Insets(10, 0, 10, 0));

        // HBox para a segunda linha de botões (Atualizar, Editar, Excluir)
        HBox bottomButtonBox = new HBox(10);
        bottomButtonBox.getChildren().addAll(refreshButton, editButton, deleteButton);
        bottomButtonBox.setPadding(new Insets(10, 0, 10, 0));

        // Organiza os botões e a tabela dentro da VBox
        this.setPadding(new Insets(20));
        this.setSpacing(10);
        this.getChildren().addAll(
                new Label("Medicamentos"),
                topButtonBox,  // Primeira linha de botões
                bottomButtonBox,  // Segunda linha de botões
                table
        );

        this.setPrefSize(500, 400);
    }

    private void loadData() {
        try {
            medicinesList.clear();
            medicinesList.addAll(controller.listAll());
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao carregar medicamentos: " + e.getMessage()).showAndWait();
        }
    }

    private void openAddForm() {
        Stage formStage = new Stage();
        formStage.setTitle("Adicionar Medicamento");
        formStage.initModality(Modality.APPLICATION_MODAL);

        MedicineForm form = new MedicineForm();
        Scene scene = new Scene(form, 450, 400);

        formStage.setScene(scene);
        formStage.setResizable(false);

        formStage.setOnHidden(e -> loadData());

        formStage.showAndWait();
    }

    private void editSelected() {
        Medicine selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione um medicamento para editar.").showAndWait();
            return;
        }

        Stage formStage = new Stage();
        formStage.setTitle("Editar Medicamento");
        formStage.initModality(Modality.APPLICATION_MODAL);

        MedicineForm form = new MedicineForm(selected);
        Scene scene = new Scene(form, 450, 400);

        formStage.setScene(scene);
        formStage.setResizable(false);

        formStage.setOnHidden(e -> loadData());

        formStage.showAndWait();
    }

    private void deleteSelected() {
        Medicine selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione um medicamento para excluir.").showAndWait();
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Exclusão");
        confirmAlert.setHeaderText("Excluir medicamento?");
        confirmAlert.setContentText("Deseja realmente excluir o medicamento '" + selected.getName() + "'?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                controller.delete(selected.getId());
                new Alert(Alert.AlertType.INFORMATION, "Medicamento excluído com sucesso!").showAndWait();
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erro ao excluir medicamento: " + e.getMessage()).showAndWait();
            }
        }
    }

    private void openBrandView() {
        MedicineBrandView brandView = new MedicineBrandView(mainLayout);  // Passando mainLayout
        mainLayout.setCenter(brandView);  // Atualizando o layout com a view de marcas de medicamentos
    }

    private void returnToMainMenu() {
        MenuView mainMenu = new MenuView(mainLayout, null);  // Passando mainLayout
        mainLayout.setCenter(mainMenu);  // Atualizando o layout com o menu principal
    }
}