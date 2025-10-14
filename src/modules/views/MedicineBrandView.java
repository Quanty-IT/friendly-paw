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
import modules.controllers.MedicineBrandController;
import modules.models.MedicineBrand;
import config.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class MedicineBrandView extends VBox {

    private TableView<MedicineBrand> table;
    private ObservableList<MedicineBrand> brandsList;
    private Connection conn;
    private MedicineBrandController controller;
    private BorderPane mainLayout;

    public MedicineBrandView(BorderPane mainLayout) {
        this.mainLayout = mainLayout;

        try {
            this.conn = Database.getConnection();
            this.controller = new MedicineBrandController(conn);
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
        brandsList = FXCollections.observableArrayList();
        table.setItems(brandsList);

        TableColumn<MedicineBrand, String> nameColumn = new TableColumn<>("Nome da Marca");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 0 0 0 20;");

        TableColumn<MedicineBrand, Void> editColumn = new TableColumn<>("Editar");
        editColumn.setPrefWidth(100);
        editColumn.setMinWidth(100);
        editColumn.setMaxWidth(100);
        editColumn.setStyle("-fx-alignment: CENTER;");
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = createIconButton("E", "edit");

            {
                editBtn.setOnAction(e -> {
                    MedicineBrand brand = getTableView().getItems().get(getIndex());
                    if (brand != null) editSelected(brand);
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

        TableColumn<MedicineBrand, Void> deleteColumn = new TableColumn<>("Excluir");
        deleteColumn.setPrefWidth(100);
        deleteColumn.setMinWidth(100);
        deleteColumn.setMaxWidth(100);
        deleteColumn.setStyle("-fx-alignment: CENTER;");
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button delBtn = createIconButton("x", "delete");

            {
                delBtn.setOnAction(e -> {
                    MedicineBrand brand = getTableView().getItems().get(getIndex());
                    if (brand != null) deleteSelected(brand);
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

        table.getColumns().addAll(nameColumn, editColumn, deleteColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("custom-table");
        table.setPlaceholder(new Label("Nenhuma marca cadastrada"));
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

        Label title = new Label("Marcas de medicamentos");
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

        Label productsLabel = new Label("Medicamentos");
        HBox productsContent = new HBox(6, productsLabel, paw2);
        productsContent.setAlignment(Pos.CENTER);
        Button productsButton = new Button();
        productsButton.setGraphic(productsContent);
        productsButton.getStyleClass().add("top-btn");

        Label menuLabel = new Label("Menu");
        HBox menuContent = new HBox(6, menuLabel, paw3);
        menuContent.setAlignment(Pos.CENTER);
        Button menuButton = new Button();
        menuButton.setGraphic(menuContent);
        menuButton.getStyleClass().add("top-btn");

        addButton.getStyleClass().add("top-btn");
        productsButton.getStyleClass().add("top-btn");
        menuButton.getStyleClass().add("top-btn");

        addButton.setOnAction(e -> openAddForm());
        productsButton.setOnAction(e -> openMedicineView());
        menuButton.setOnAction(e -> returnToMainMenu());

        HBox buttonBar = new HBox(15, addButton, productsButton, menuButton);
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
        this.getStylesheets().add(getClass().getResource("/styles/medicineBrand.css").toExternalForm());
    }

    private void loadData() {
        try {
            brandsList.clear();
            brandsList.addAll(controller.listAll());
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao buscar marcas: " + e.getMessage()).showAndWait();
        }
    }

    private void openAddForm() {
        Stage formStage = new Stage();
        formStage.setTitle("Adicionar marca");
        formStage.initModality(Modality.APPLICATION_MODAL);

        MedicineBrandForm form = new MedicineBrandForm();
        Scene scene = new Scene(form, 450, 250);
        scene.getStylesheets().add(getClass().getResource("/styles/medicineBrand.css").toExternalForm());
        formStage.setScene(scene);
        formStage.setResizable(false);
        formStage.setOnHidden(e -> loadData());
        formStage.showAndWait();
    }

    private void editSelected(MedicineBrand selected) {
        if (selected == null) return;

        Stage editStage = new Stage();
        editStage.setTitle("Editar marca");
        editStage.initModality(Modality.APPLICATION_MODAL);

        MedicineBrandEditForm form = new MedicineBrandEditForm(selected, controller);
        Scene scene = new Scene(form, 450, 250);
        scene.getStylesheets().add(getClass().getResource("/styles/medicineBrand.css").toExternalForm());
        editStage.setScene(scene);
        editStage.setResizable(false);
        editStage.setOnHidden(e -> loadData());
        editStage.showAndWait();
    }

    private void deleteSelected(MedicineBrand selected) {
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Tem certeza que deseja excluir a marca '" + selected.getName() + "'?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try {
                    controller.delete(selected.getId());
                    loadData();
                    new Alert(Alert.AlertType.INFORMATION, "Marca excluída com sucesso!").showAndWait();
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Erro ao excluir marca: " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    private void returnToMainMenu() {
        mainLayout.setCenter(new MenuView(mainLayout, null));
    }

    private void openMedicineView() {
        mainLayout.setCenter(new MedicineView(mainLayout));
    }
}