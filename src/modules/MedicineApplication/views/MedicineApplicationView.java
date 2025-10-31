package modules.MedicineApplication.views;

import config.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import modules.Animal.models.Animal;
import modules.Animal.views.AnimalView;
import modules.Medicine.controllers.MedicineController;
import modules.Medicine.models.Medicine;
import modules.MedicineApplication.controllers.MedicineApplicationController;
import modules.MedicineApplication.models.MedicineApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Listagem de Aplicações de Medicamento de um animal.
 * Estilo e UX seguem o mesmo padrão de AttachmentView.
 */
public class MedicineApplicationView extends VBox {

    private final BorderPane mainLayout;
    private final Animal animal;          // recebemos o objeto Animal direto
    private final UUID animalUuid;

    private Connection conn;

    private final TableView<MedicineApplication> tableView = new TableView<>();
    private final ObservableList<MedicineApplication> appList = FXCollections.observableArrayList();

    // lookup para nome do medicamento por UUID
    private final Map<UUID, String> medicineNames = new HashMap<>();

    public MedicineApplicationView(BorderPane mainLayout, Animal animal) {
        this.mainLayout = mainLayout;
        this.animal = animal;
        this.animalUuid = animal.getUuid();

        try {
            this.conn = Database.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao conectar com o banco de dados.").showAndWait();
            return;
        }

        initializeComponents();
        setupLayout();
        loadMedicinesLookup();
        loadApplications();
    }

    /* =========================
       UI helpers (SVG + Buttons)
       ========================= */

    private StackPane createSvgIcon(String svgPath, String color) {
        StackPane box = new StackPane();
        box.setMinSize(18, 18);
        box.setPrefSize(18, 18);
        box.setMaxSize(18, 18);
        box.setAlignment(Pos.CENTER);
        box.setPickOnBounds(false);

        try (var in = getClass().getResourceAsStream(svgPath)) {
            if (in == null) return box;

            String svg = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            if (svg.isEmpty()) return box;

            var pRe = java.util.regex.Pattern.compile(
                    "<path[^>]*d\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>",
                    java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL
            );
            var m = pRe.matcher(svg);

            Group g = new Group();
            while (m.find()) {
                String d = m.group(1);
                var sp = new SVGPath();
                sp.setContent(d);
                var c = javafx.scene.paint.Color.valueOf(color);
                sp.setFill(c);
                sp.setStroke(c);
                sp.setStrokeWidth(1.8);
                sp.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
                sp.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
                g.getChildren().add(sp);
            }

            if (g.getChildren().isEmpty()) {
                Label fallback = new Label("💊");
                fallback.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
                box.getChildren().add(fallback);
                return box;
            }

            var b = g.getLayoutBounds();
            double w = b.getWidth(), h = b.getHeight();
            if (w == 0 || h == 0) { box.getChildren().add(g); return box; }

            double target = 18.0, margin = 0.78;
            double scale = Math.min((target * margin) / w, (target * margin) / h);
            g.setScaleX(scale); g.setScaleY(scale);
            g.setPickOnBounds(false);

            box.getChildren().add(g);
        } catch (Exception e) {
            System.err.println("Erro ao carregar SVG: " + svgPath + " - " + e.getMessage());
        }
        return box;
    }

    private Button createIconButton(String svgPath, String... extraStyleClasses) {
        Button btn = new Button();
        StackPane icon = createSvgIcon(svgPath, "#FFFFFF");
        btn.setGraphic(icon);

        btn.getStyleClass().addAll("icon-btn");
        if (extraStyleClasses != null) btn.getStyleClass().addAll(extraStyleClasses);

        btn.setMinSize(35, 35);
        btn.setPrefSize(35, 35);
        btn.setMaxSize(35, 35);
        btn.setPickOnBounds(false);
        btn.setGraphicTextGap(0);
        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btn.setAlignment(Pos.CENTER);
        return btn;
    }

    /* ==============
       Build the view
       ============== */

    private void initializeComponents() {
        tableView.setItems(appList);

        // Coluna: Medicamento
        TableColumn<MedicineApplication, UUID> medCol = new TableColumn<>("medicamento");
        medCol.setCellValueFactory(new PropertyValueFactory<>("medicineUuid"));
        medCol.setCellFactory(col -> new TableCell<>() {
            { setAlignment(Pos.CENTER_LEFT); setPadding(new Insets(0,8,0,12)); }
            @Override protected void updateItem(UUID item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : medicineNames.getOrDefault(item, "—"));
            }
        });

        // Quantidade (mg)
        TableColumn<MedicineApplication, BigDecimal> qtyCol = new TableColumn<>("quantidade (mg)");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setCellFactory(col -> new TableCell<>() {
            { setAlignment(Pos.CENTER); }
            @Override protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.stripTrailingZeros().toPlainString());
            }
        });
        qtyCol.setPrefWidth(140);

        // Aplicado em
        TableColumn<MedicineApplication, ZonedDateTime> appliedCol = new TableColumn<>("aplicado em");
        appliedCol.setCellValueFactory(new PropertyValueFactory<>("appliedAt"));
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        appliedCol.setCellFactory(col -> new TableCell<>() {
            { setAlignment(Pos.CENTER); }
            @Override protected void updateItem(ZonedDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : dt.format(item));
            }
        });
        appliedCol.setPrefWidth(160);

        // Frequência
        TableColumn<MedicineApplication, MedicineApplication.Frequency> freqCol = new TableColumn<>("frequência");
        freqCol.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        freqCol.setCellFactory(col -> new TableCell<>() {
            { setAlignment(Pos.CENTER); }
            @Override protected void updateItem(MedicineApplication.Frequency item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });
        freqCol.setPrefWidth(150);

        // Próxima aplicação
        TableColumn<MedicineApplication, ZonedDateTime> nextCol = new TableColumn<>("próxima aplicação");
        nextCol.setCellValueFactory(new PropertyValueFactory<>("nextApplicationAt"));
        nextCol.setCellFactory(col -> new TableCell<>() {
            { setAlignment(Pos.CENTER); }
            @Override protected void updateItem(ZonedDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "—" : dt.format(item));
            }
        });
        nextCol.setPrefWidth(170);

        // Fim do tratamento
        TableColumn<MedicineApplication, ZonedDateTime> endsCol = new TableColumn<>("fim do tratamento");
        endsCol.setCellValueFactory(new PropertyValueFactory<>("endsAt"));
        endsCol.setCellFactory(col -> new TableCell<>() {
            { setAlignment(Pos.CENTER); }
            @Override protected void updateItem(ZonedDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "—" : dt.format(item));
            }
        });
        endsCol.setPrefWidth(170);

        // Ações (Excluir)
        TableColumn<MedicineApplication, Void> actionCol = new TableColumn<>("ações");
        actionCol.setPrefWidth(80);
        actionCol.setMinWidth(80);
        actionCol.setMaxWidth(100);
        actionCol.setStyle("-fx-alignment: CENTER;");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = createIconButton("/assets/icons/trash.svg", "icon-btn-delete");
            private final HBox pane = new HBox(8, deleteButton);
            {
                pane.setAlignment(Pos.CENTER);
                pane.setPickOnBounds(false);

                deleteButton.setTooltip(new Tooltip("Excluir aplicação"));
                deleteButton.setOnAction(e -> {
                    MedicineApplication app = getTableView().getItems().get(getIndex());
                    if (app == null) return;

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Tem certeza que deseja deletar esta aplicação?",
                            ButtonType.YES, ButtonType.NO);
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            try {
                                MedicineApplicationController.delete(conn, app.getApplicationUuid());
                                getTableView().getItems().remove(app);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                new Alert(Alert.AlertType.ERROR, "Erro ao deletar aplicação: " + ex.getMessage()).showAndWait();
                            }
                        }
                    });
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tableView.getColumns().addAll(
                medCol, qtyCol, appliedCol, freqCol, nextCol, endsCol, actionCol
        );

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getStyleClass().add("custom-table");
        tableView.setPlaceholder(new Label("Nenhuma aplicação cadastrada"));
    }

    private void setupLayout() {
        // Logo topo esquerdo
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/assets/logo.png")));
        logo.setFitWidth(140);
        logo.setPreserveRatio(true);

        // Título centralizado
        Label title = new Label("Aplicações de medicamento");
        title.getStyleClass().add("title");

        // Botões topo direito
        Button addButton = new Button("Aplicar");
        addButton.getStyleClass().add("top-btn");
        addButton.setOnAction(e ->
                mainLayout.setCenter(new MedicineApplicationForm(mainLayout, animal))
        );

        Button backButton = new Button("Animais");
        backButton.getStyleClass().add("top-btn");
        backButton.setOnAction(e -> mainLayout.setCenter(new AnimalView(mainLayout)));

        HBox rightBar = new HBox(15, addButton, backButton);
        rightBar.setAlignment(Pos.CENTER_RIGHT);

        HBox leftBar = new HBox(logo);
        leftBar.setAlignment(Pos.CENTER_LEFT);

        StackPane topBar = new StackPane(leftBar, title, rightBar);
        StackPane.setAlignment(leftBar, Pos.CENTER_LEFT);
        StackPane.setAlignment(title, Pos.CENTER);
        StackPane.setAlignment(rightBar, Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(30, 60, 0, 60));

        VBox content = new VBox(30, tableView);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40, 60, 40, 60));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        this.setAlignment(Pos.TOP_CENTER);
        this.getChildren().addAll(topBar, content);
        this.getStyleClass().add("main-bg");
        this.getStylesheets().add(
                getClass().getResource("/modules/MedicineApplication/styles/MedicineApplicationView.css").toExternalForm()
        );
    }

    /* =========================
       Load data
       ========================= */

    private void loadMedicinesLookup() {
        try {
            MedicineController medCtrl = new MedicineController(conn);
            for (Medicine m : medCtrl.listAll()) {
                medicineNames.put(m.getUuid(), m.getName());
            }
        } catch (SQLException e) {
            System.err.println("Falha ao carregar medicamentos: " + e.getMessage());
        }
    }

    private void loadApplications() {
        try {
            List<MedicineApplication> apps =
                    MedicineApplicationController.getApplicationsForAnimal(conn, animalUuid);

            appList.clear();
            appList.addAll(apps);
            tableView.setItems(appList);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao buscar aplicações: " + e.getMessage()).showAndWait();
        }
    }
}
