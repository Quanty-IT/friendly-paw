package modules.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
    private BorderPane mainLayout;

    public MedicineView(BorderPane mainLayout) {
        this.mainLayout = mainLayout;

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

        TableColumn<Medicine, String> nameColumn = new TableColumn<>("Nome");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 0 0 0 20;");

        TableColumn<Medicine, String> brandColumn = new TableColumn<>("Marca");
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        brandColumn.setPrefWidth(150);
        brandColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<Medicine, Integer> quantityColumn = new TableColumn<>("Quantidade");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setPrefWidth(100);
        quantityColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<Medicine, String> descriptionColumn = new TableColumn<>("Descrição");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setPrefWidth(200);
        descriptionColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<Medicine, Boolean> activeColumn = new TableColumn<>("Ativo");
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("isActive"));
        activeColumn.setPrefWidth(80);
        activeColumn.setStyle("-fx-alignment: CENTER;");
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
        createdAtColumn.setStyle("-fx-alignment: CENTER;");
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

        TableColumn<Medicine, Void> editColumn = new TableColumn<>("Editar");
        editColumn.setPrefWidth(100);
        editColumn.setMinWidth(100);
        editColumn.setMaxWidth(100);
        editColumn.setStyle("-fx-alignment: CENTER;");
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = createIconButton("E", "edit");

            {
                editBtn.setOnAction(e -> {
                    Medicine medicine = getTableView().getItems().get(getIndex());
                    if (medicine != null) editSelected(medicine);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(editBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        TableColumn<Medicine, Void> deleteColumn = new TableColumn<>("Excluir");
        deleteColumn.setPrefWidth(100);
        deleteColumn.setMinWidth(100);
        deleteColumn.setMaxWidth(100);
        deleteColumn.setStyle("-fx-alignment: CENTER;");
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button delBtn = createIconButton("x", "delete");

            {
                delBtn.setOnAction(e -> {
                    Medicine medicine = getTableView().getItems().get(getIndex());
                    if (medicine != null) deleteSelected(medicine);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(delBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(nameColumn, brandColumn, quantityColumn, descriptionColumn, activeColumn, createdAtColumn, editColumn, deleteColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("custom-table");
        table.setPlaceholder(new Label("Nenhum medicamento cadastrado"));
    }

    private Button createIconButton(String icon, String type) {
        Button btn = new Button(icon);
        btn.getStyleClass().addAll("icon-btn", "icon-btn-" + type);
        btn.setPrefSize(35, 35);
        btn.setMinSize(35, 35);
        btn.setMaxSize(35, 35);
        return btn;
    }

    private void setupLayout() {
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/assets/logo.png")));
        logo.setFitWidth(140);
        logo.setPreserveRatio(true);

        Label title = new Label("Medicamentos");
        title.getStyleClass().add("title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ImageView paw1 = new ImageView(new Image(getClass().getResourceAsStream("/assets/patas.png")));
        ImageView paw2 = new ImageView(new Image(getClass().getResourceAsStream("/assets/patas.png")));
        ImageView paw3 = new ImageView(new Image(getClass().getResourceAsStream("/assets/patas.png")));

        paw1.setPreserveRatio(true);
        paw2.setPreserveRatio(true);
        paw3.setPreserveRatio(true);

        Label addLabel = new Label("Cadastrar");
        HBox addContent = new HBox(6, addLabel, paw1);
        addContent.setAlignment(Pos.CENTER);
        Button addButton = new Button();
        addButton.setGraphic(addContent);
        addButton.getStyleClass().add("top-btn");

        Label brandsLabel = new Label("Marcas");
        HBox brandsContent = new HBox(6, brandsLabel, paw2);
        brandsContent.setAlignment(Pos.CENTER);
        Button brandsButton = new Button();
        brandsButton.setGraphic(brandsContent);
        brandsButton.getStyleClass().add("top-btn");

        Label menuLabel = new Label("Menu");
        HBox menuContent = new HBox(6, menuLabel, paw3);
        menuContent.setAlignment(Pos.CENTER);
        Button menuButton = new Button();
        menuButton.setGraphic(menuContent);
        menuButton.getStyleClass().add("top-btn");

        addButton.setOnAction(e -> openAddForm());
        brandsButton.setOnAction(e -> openBrandView());
        menuButton.setOnAction(e -> returnToMainMenu());

        HBox buttonBar = new HBox(15, addButton, brandsButton, menuButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        HBox topBar = new HBox(15, logo, spacer, buttonBar);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(30, 40, 0, 40));

        VBox content = new VBox(30, title, table);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40, 60, 40, 60));
        VBox.setVgrow(table, Priority.ALWAYS);

        this.setAlignment(Pos.TOP_CENTER);
        this.getChildren().addAll(topBar, content);
        this.getStyleClass().add("main-bg");
        this.getStylesheets().add(getClass().getResource("/styles/medicine.css").toExternalForm());
    }

    private void loadData() {
        try {
            medicinesList.clear();
            medicinesList.addAll(controller.listAll());
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao buscar medicamentos: " + e.getMessage()).showAndWait();
        }
    }

    private void openAddForm() {
        Stage formStage = new Stage();
        formStage.setTitle("Adicionar medicamento");
        formStage.initModality(Modality.APPLICATION_MODAL);

        MedicineForm form = new MedicineForm();
        Scene scene = new Scene(form, 500, 550);
        scene.getStylesheets().add(getClass().getResource("/styles/medicine.css").toExternalForm());
        formStage.setScene(scene);
        formStage.setResizable(false);
        formStage.setOnHidden(e -> loadData());
        formStage.showAndWait();
    }

    private void editSelected(Medicine selected) {
        if (selected == null) return;

        // Stage editStage = new Stage();
        // editStage.setTitle("Editar medicamento");
        // editStage.initModality(Modality.APPLICATION_MODAL);

        // MedicineEditForm form = new MedicineEditForm(selected, controller);
        // Scene scene = new Scene(form, 500, 550);
        // scene.getStylesheets().add(getClass().getResource("/styles/medicine.css").toExternalForm());
        // editStage.setScene(scene);
        // editStage.setResizable(false);
        // editStage.setOnHidden(e -> loadData());
        // editStage.showAndWait();
    }

    private void deleteSelected(Medicine selected) {
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Tem certeza que deseja excluir o medicamento '" + selected.getName() + "'?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try {
                    controller.delete(selected.getId());
                    loadData();
                    new Alert(Alert.AlertType.INFORMATION, "Medicamento excluído com sucesso!").showAndWait();
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Erro ao excluir medicamento: " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    private void openBrandView() {
        mainLayout.setCenter(new MedicineBrandView(mainLayout));
    }

    private void returnToMainMenu() {
        mainLayout.setCenter(new MenuView(mainLayout, null));
    }
}