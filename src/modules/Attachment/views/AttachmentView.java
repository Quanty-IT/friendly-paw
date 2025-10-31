package modules.Attachment.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import modules.Attachment.controllers.AttachmentController;
import modules.Attachment.models.Attachment;
import modules.Animal.views.AnimalView;
import config.Database;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AttachmentView extends VBox {

    private TableView<Attachment> tableView;
    private ObservableList<Attachment> attachmentList;
    private Connection conn;
    private final BorderPane mainLayout;
    private final UUID animalUuid;

    public AttachmentView(BorderPane mainLayout, UUID animalUuid) {
        this.mainLayout = mainLayout;
        this.animalUuid = animalUuid;

        try {
            this.conn = Database.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao conectar com o banco de dados.").showAndWait();
            return;
        }

        initializeComponents();
        setupLayout();
        loadAttachments();
    }

    /* =========================
       UI helpers (SVG + Buttons)
       ========================= */

    private StackPane createSvgIcon(String svgPath, String color) {
        // container fixo 18x18, centraliza automaticamente
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

            javafx.scene.Group g = new javafx.scene.Group();
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
                Label fallback = new Label("📎");
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
        tableView = new TableView<>();
        attachmentList = FXCollections.observableArrayList();
        tableView.setItems(attachmentList);

        // Coluna: Arquivo (link)
        TableColumn<Attachment, String> fileColumn = new TableColumn<>("arquivo");
        fileColumn.setCellValueFactory(new PropertyValueFactory<>("file"));
        fileColumn.setCellFactory(column -> new TableCell<>() {
            {
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 8, 0, 12));
            }
            @Override
            protected void updateItem(String file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null || file.isBlank()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Hyperlink link = new Hyperlink(file);
                    link.setOnAction(event -> {
                        try {
                            java.awt.Desktop.getDesktop().browse(new java.net.URI(file));
                        } catch (Exception e) {
                            new Alert(Alert.AlertType.ERROR, "Não foi possível abrir o arquivo.").showAndWait();
                        }
                    });
                    setGraphic(link);
                    setText(null);
                }
            }
        });

        // Coluna: Descrição
        TableColumn<Attachment, String> descriptionColumn = new TableColumn<>("descrição");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(col -> new TableCell<>() {
            {
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0,8,0,12));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });

        // Coluna: Criado em (formatado)
        TableColumn<Attachment, LocalDateTime> createdAtColumn = new TableColumn<>("criado em");
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        createdAtColumn.setCellFactory(col -> new TableCell<>() {
            { setAlignment(Pos.CENTER); }
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt.format(item));
            }
        });
        createdAtColumn.setPrefWidth(160);

        // Coluna: Ações (trash)
        TableColumn<Attachment, Void> actionColumn = new TableColumn<>("ações");
        actionColumn.setPrefWidth(80);
        actionColumn.setMinWidth(80);
        actionColumn.setMaxWidth(100);
        actionColumn.setStyle("-fx-alignment: CENTER;");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = createIconButton("/assets/icons/trash.svg", "icon-btn-delete");
            private final HBox pane = new HBox(8, deleteButton);

            {
                pane.setAlignment(Pos.CENTER);
                pane.setPickOnBounds(false);

                deleteButton.setTooltip(new Tooltip("Excluir anexo"));
                deleteButton.setOnAction(event -> {
                    Attachment attachment = getTableView().getItems().get(getIndex());
                    if (attachment == null) return;

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Tem certeza que deseja deletar este anexo?",
                            ButtonType.YES, ButtonType.NO);
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            try {
                                AttachmentController.deleteAttachment(conn, attachment.getUuid());
                                getTableView().getItems().remove(attachment);
                            } catch (SQLException e) {
                                e.printStackTrace();
                                new Alert(Alert.AlertType.ERROR, "Erro ao deletar anexo: " + e.getMessage()).showAndWait();
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tableView.getColumns().addAll(fileColumn, descriptionColumn, createdAtColumn, actionColumn);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getStyleClass().add("custom-table");
        tableView.setPlaceholder(new Label("Nenhum anexo cadastrado"));
    }

    private void setupLayout() {
        // Logo topo esquerdo
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/assets/logo.png")));
        logo.setFitWidth(140);
        logo.setPreserveRatio(true);

        // Título centralizado
        Label title = new Label("Anexos");
        title.getStyleClass().add("title");

        // Botões topo direito (Adicionar / Animais)
        Button addButton = new Button("Adicionar");
        addButton.getStyleClass().add("top-btn");
        addButton.setOnAction(e -> mainLayout.setCenter(new AttachmentForm(mainLayout, animalUuid)));

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
                getClass().getResource("/modules/Attachment/styles/AttachmentView.css").toExternalForm()
        );
    }

    /* =========================
       Load data
       ========================= */
    private void loadAttachments() {
        try {
            List<Attachment> attachments = AttachmentController.getAttachmentsForAnimal(conn, animalUuid);
            attachmentList.clear();
            attachmentList.addAll(attachments);
            tableView.setItems(attachmentList);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao buscar anexos: " + e.getMessage()).showAndWait();
        }
    }
}
