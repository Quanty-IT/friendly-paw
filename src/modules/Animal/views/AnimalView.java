package modules.Animal.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import modules.Animal.controllers.AnimalController;
import modules.Animal.models.Animal;
import modules.MedicineApplication.views.MedicineApplicationView;
import modules.Shared.views.MenuView;
import modules.Attachment.views.AttachmentView;
import modules.MedicineApplication.views.MedicineApplicationForm;
import config.Database;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AnimalView extends VBox {

    private TableView<Animal> tableView;
    private ObservableList<Animal> animalList;
    private Connection conn;

    // Referência ao layout principal para navegação entre telas
    private final BorderPane mainLayout;

    public AnimalView(BorderPane mainLayout) {
        this.mainLayout = mainLayout;

        try {
            this.conn = Database.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao conectar com o banco de dados.").showAndWait();
            return;
        }

        initializeComponents();
        setupLayout();
        loadAnimals();
    }

    /**
     * Cria um ícone SVG a partir de um arquivo SVG do classpath.
     * 
     * @param svgPath Caminho do arquivo SVG no classpath
     * @param color Cor a ser aplicada ao ícone (formato hexadecimal, ex: "#FFFFFF")
     * @return StackPane contendo o ícone renderizado, ou um StackPane vazio em caso de erro
     */
    private StackPane createSvgIcon(String svgPath, String color) {
        StackPane box = new StackPane();
        box.setMinSize(18, 18);
        box.setPrefSize(18, 18);
        box.setMaxSize(18, 18);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setPickOnBounds(false);

        try (var in = getClass().getResourceAsStream(svgPath)) {
            if (in == null) return box;

            String svg = new BufferedReader(new InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))
                    .lines().collect(java.util.stream.Collectors.joining("\n"));
            if (svg.isEmpty()) return box;

            // pega TODOS os <path ... d="...">
            var pRe = java.util.regex.Pattern.compile(
                    "<path[^>]*d\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>",
                    java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL
            );
            var m = pRe.matcher(svg);

            javafx.scene.Group g = new javafx.scene.Group();
            while (m.find()) {
                String d = m.group(1);
                var sp = new javafx.scene.shape.SVGPath();
                sp.setContent(d);
                // fill + stroke garantem visibilidade (ícones stroke-only)
                javafx.scene.paint.Color c = javafx.scene.paint.Color.valueOf(color);
                sp.setFill(c);
                sp.setStroke(c);
                sp.setStrokeWidth(1.8);
                sp.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
                sp.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
                g.getChildren().add(sp);
            }

            if (g.getChildren().isEmpty()) {
                // Fallback para SVGs que não usam <path>
                Label fallback = new Label("📎");
                fallback.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
                box.getChildren().add(fallback);
                return box;
            }

            // calcula escala pelo bounds reais do conteúdo
            javafx.geometry.Bounds b = g.getLayoutBounds();
            double w = b.getWidth();
            double h = b.getHeight();
            if (w == 0 || h == 0) { box.getChildren().add(g); return box; }

            double target = 18.0;
            double margin = 0.78; // “respiro” para não encostar nas bordas
            double scale = Math.min((target * margin) / w, (target * margin) / h);

            g.setScaleX(scale);
            g.setScaleY(scale);

            // StackPane centraliza automaticamente
            g.setPickOnBounds(false);

            box.getChildren().add(g);
        } catch (Exception e) {
            System.err.println("Erro ao carregar SVG: " + svgPath + " - " + e.getMessage());
        }
        return box;
    }

    /**
     * Cria um botão circular com um ícone SVG centralizado.
     * 
     * @param svgPath Caminho do SVG no classpath
     * @param extraStyleClasses Classes extras para estilização (ex: "icon-btn-edit")
     * @return Button configurado com o ícone SVG e estilos aplicados
     */
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
        btn.setAlignment(javafx.geometry.Pos.CENTER);
        return btn;
    }

    /**
     * Inicializa os componentes da interface, incluindo a tabela e suas colunas.
     */
    private void initializeComponents() {
        tableView = new TableView<>();
        animalList = FXCollections.observableArrayList();
        tableView.setItems(animalList);

        // Status
        TableColumn<Animal, String> statusColumn = new TableColumn<>("status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(120);
        statusColumn.setCellFactory(col -> new TableCell<>() {
            {
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 8, 0, 12));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("status-active", "status-inactive");
                } else {
                    String pt = convertStatusToPt(item);
                    setText(pt);
                    getStyleClass().removeAll("status-active", "status-inactive");
                    // só como exemplo visual: adotado = "ativo" (verde), perdido = "inativo" (vermelho)
                    if ("Adotado".equals(pt)) getStyleClass().add("status-active");
                    if ("Perdido".equals(pt)) getStyleClass().add("status-inactive");
                }
            }
        });

        // Outras colunas
        TableColumn<Animal, String> nameColumn = new TableColumn<>("nome");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Animal, String> speciesColumn = new TableColumn<>("espécie");
        speciesColumn.setCellValueFactory(new PropertyValueFactory<>("species"));
        speciesColumn.setCellFactory(col -> new TableCell<>() {
            { setAlignment(Pos.CENTER_LEFT); setPadding(new Insets(0,8,0,12)); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item==null ? null : convertSpeciesToPt(item));
            }
        });

        TableColumn<Animal, String> breedColumn = new TableColumn<>("raça");
        breedColumn.setCellValueFactory(new PropertyValueFactory<>("breed"));
        breedColumn.setCellFactory(col -> new TableCell<>() {
            { setAlignment(Pos.CENTER_LEFT); setPadding(new Insets(0,8,0,12)); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item==null ? null : convertBreedToPt(item));
            }
        });

        TableColumn<Animal, String> sizeColumn = new TableColumn<>("porte");
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeColumn.setCellFactory(col -> new TableCell<>() {
            { setAlignment(Pos.CENTER); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item==null ? null : convertSizeToPt(item));
            }
        });

        TableColumn<Animal, Boolean> castratedColumn = new TableColumn<>("castrado");
        castratedColumn.setCellValueFactory(new PropertyValueFactory<>("castrated"));
        castratedColumn.setCellFactory(col -> new TableCell<>() {
            { setAlignment(Pos.CENTER); }
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item==null ? null : (item ? "Sim" : "Não"));
            }
        });

        TableColumn<Animal, String> fivColumn = new TableColumn<>("FIV");
        fivColumn.setCellValueFactory(new PropertyValueFactory<>("fiv"));
        fivColumn.setCellFactory(col -> new TableCell<>() {
            { setAlignment(Pos.CENTER); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item==null ? null : convertYesNoNotTestedToPt(item));
            }
        });

        TableColumn<Animal, String> felvColumn = new TableColumn<>("FeLV");
        felvColumn.setCellValueFactory(new PropertyValueFactory<>("felv"));
        felvColumn.setCellFactory(col -> new TableCell<>() {
            { setAlignment(Pos.CENTER); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item==null ? null : convertYesNoNotTestedToPt(item));
            }
        });

        // Coluna Ações (ícones)
        TableColumn<Animal, Void> actionColumn = new TableColumn<>("ações");
        actionColumn.setPrefWidth(220);
        actionColumn.setMinWidth(220);
        actionColumn.setMaxWidth(220);
        actionColumn.setStyle("-fx-alignment: CENTER;");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button attachmentsButton= createIconButton("/assets/icons/attachment.svg", "icon-btn-attachment");
            private final Button applyMedicineBtn = createIconButton("/assets/icons/medicine.svg", "icon-btn-medicine");
            private final Button editButton       = createIconButton("/assets/icons/edit.svg", "icon-btn-edit");
            private final Button deleteButton     = createIconButton("/assets/icons/trash.svg", "icon-btn-delete");
            private final HBox pane = new HBox(8, attachmentsButton, applyMedicineBtn, editButton, deleteButton);

            {
                if (applyMedicineBtn.getGraphic() != null) {
                    applyMedicineBtn.getGraphic().setRotate(90);
                }

                pane.setAlignment(Pos.CENTER);
                pane.setPickOnBounds(false);

                attachmentsButton.setTooltip(new Tooltip("Anexos"));
                applyMedicineBtn.setTooltip(new Tooltip("Medicamentos Aplicados"));
                editButton.setTooltip(new Tooltip("Editar"));
                deleteButton.setTooltip(new Tooltip("Excluir"));

                editButton.setOnAction(event -> {
                    Animal animal = getTableView().getItems().get(getIndex());
                    if (animal != null) {
                        AnimalView.this.mainLayout.setCenter(new AnimalForm(AnimalView.this.mainLayout, animal));
                    } else {
                        new Alert(Alert.AlertType.WARNING, "Selecione um animal para editar.").showAndWait();
                    }
                });

                deleteButton.setOnAction(event -> {
                    Animal animal = getTableView().getItems().get(getIndex());
                    if (animal == null) return;
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja deletar este animal?", ButtonType.YES, ButtonType.NO);
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            try {
                                AnimalController.deleteAnimal(conn, animal.getUuid());
                                getTableView().getItems().remove(animal);
                            } catch (SQLException e) {
                                e.printStackTrace();
                                new Alert(Alert.AlertType.ERROR, "Erro ao deletar animal: " + e.getMessage()).showAndWait();
                            }
                        }
                    });
                });

                attachmentsButton.setOnAction(event -> {
                    Animal animal = getTableView().getItems().get(getIndex());
                    if (animal != null) {
                        AnimalView.this.mainLayout.setCenter(new AttachmentView(AnimalView.this.mainLayout, animal.getUuid()));
                    } else {
                        new Alert(Alert.AlertType.WARNING, "Selecione um animal para ver os anexos.").showAndWait();
                    }
                });

                applyMedicineBtn.setOnAction(event -> {
                    Animal animal = getTableView().getItems().get(getIndex());
                    if (animal != null) {
                        mainLayout.setCenter(new MedicineApplicationView(mainLayout, animal));
                    }
                });

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tableView.getColumns().addAll(
            statusColumn, nameColumn, speciesColumn, breedColumn,
            sizeColumn, castratedColumn, fivColumn, felvColumn, actionColumn
        );

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getStyleClass().add("custom-table");
        tableView.setPlaceholder(new Label("Nenhum animal cadastrado"));
    }

    /**
     * Configura o layout principal da view, incluindo logo, título e botões de ação.
     */
    private void setupLayout() {
        // Logo topo esquerdo
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/assets/logo.png")));
        logo.setFitWidth(140);
        logo.setPreserveRatio(true);

        Button logoBtn = new Button();
        logoBtn.setGraphic(logo);
        logoBtn.setOnAction(e -> mainLayout.setCenter(new MenuView(mainLayout, null)));
        logoBtn.setCursor(javafx.scene.Cursor.HAND);
        logoBtn.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-color: transparent;");
        logoBtn.setFocusTraversable(false);

        // Título centralizado
        Label title = new Label("Animais");
        title.getStyleClass().add("title");

        Button addButton = new Button("Cadastrar");
        addButton.getStyleClass().add("top-btn");
        addButton.setOnAction(e -> mainLayout.setCenter(new AnimalForm(mainLayout, null)));

        HBox rightBar = new HBox(15, addButton);
        rightBar.setAlignment(Pos.CENTER_RIGHT);

        HBox leftBar = new HBox(logoBtn);
        leftBar.setAlignment(Pos.CENTER_LEFT);

        var sideMaxWidth = javafx.beans.binding.Bindings.max(leftBar.widthProperty(), rightBar.widthProperty());
        leftBar.minWidthProperty().bind(sideMaxWidth);
        rightBar.minWidthProperty().bind(sideMaxWidth);

        BorderPane topBar = new BorderPane();
        topBar.setLeft(leftBar);
        topBar.setCenter(title);
        topBar.setRight(rightBar);
        BorderPane.setAlignment(title, Pos.CENTER);
        topBar.setPadding(new Insets(30, 60, 0, 60));

        title.setMaxWidth(Region.USE_PREF_SIZE);
        rightBar.setMaxWidth(Region.USE_PREF_SIZE);
        leftBar.setMaxWidth(Region.USE_PREF_SIZE);

        VBox content = new VBox(30, tableView);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40, 60, 40, 60));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        this.setAlignment(Pos.TOP_CENTER);
        this.getChildren().addAll(topBar, content);
        this.getStyleClass().add("main-bg");
        this.getStylesheets().add(
            getClass().getResource("/modules/Animal/styles/AnimalView.css").toExternalForm()
        );
    }

    /**
     * Carrega todos os animais do banco de dados e atualiza a tabela.
     * 
     * @throws SQLException Se ocorrer erro na operação do banco de dados (tratado internamente com Alert)
     */
    private void loadAnimals() {
        try {
            List<Animal> animals = AnimalController.getAllAnimals(conn);
            animalList.clear();
            animalList.addAll(animals);
            tableView.setItems(animalList);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao buscar animais: " + e.getMessage()).showAndWait();
        }
    }

    /**
     * Converte o valor de sexo do inglês para português.
     * 
     * @param value Valor em inglês ("male" ou "female")
     * @return Valor em português ("Macho" ou "Fêmea"), ou null se o valor não for reconhecido
     */
    private String convertSexToPt(String value) {
        return switch (value) {
            case "male" -> "Macho";
            case "female" -> "Fêmea";
            default -> null;
        };
    }
    /**
     * Converte o valor de espécie do inglês para português.
     * 
     * @param value Valor em inglês ("dog" ou "cat")
     * @return Valor em português ("Cachorro" ou "Gato"), ou null se o valor não for reconhecido
     */
    private String convertSpeciesToPt(String value) {
        return switch (value) {
            case "dog" -> "Cachorro";
            case "cat" -> "Gato";
            default -> null;
        };
    }
    /**
     * Converte o valor de raça do inglês para português.
     * 
     * @param value Valor em inglês (identificador da raça)
     * @return Valor em português (nome da raça), ou null se o valor não for reconhecido
     */
    private String convertBreedToPt(String value) {
        return switch (value) {
            case "mixed-breed" -> "S.R.D";
            case "shih-tzu" -> "Shih-tzu";
            case "yorkshire-terrier" -> "Yorkshire Terrier";
            case "german-spitz" -> "Spitz Alemão";
            case "french-bulldog" -> "Buldogue Francês";
            case "poodle" -> "Poodle";
            case "lhasa-apso" -> "Lhasa Apso";
            case "golden-retriever" -> "Golden Retriever";
            case "rottweiler" -> "Rottweiler";
            case "labrador-retriever" -> "Labrador Retriever";
            case "pug" -> "Pug";
            case "german-shepherd" -> "Pastor Alemão";
            case "border-collie" -> "Border Collie";
            case "long-haired-chihuahua" -> "Chihuahua de Pelo Longo";
            case "belgian-malinois" -> "Pastor Belga Malinois";
            case "maltese" -> "Maltês";
            default -> null;
        };
    }
    /**
     * Converte o valor de porte do inglês para português.
     * 
     * @param value Valor em inglês ("small", "medium" ou "large")
     * @return Valor em português ("Pequeno", "Médio" ou "Grande"), ou null se o valor não for reconhecido
     */
    private String convertSizeToPt(String value) {
        return switch (value) {
            case "small" -> "Pequeno";
            case "medium" -> "Médio";
            case "large" -> "Grande";
            default -> null;
        };
    }
    /**
     * Converte o valor de cor do inglês para português.
     * 
     * @param value Valor em inglês (identificador da cor)
     * @return Valor em português (nome da cor), ou null se o valor não for reconhecido
     */
    private String convertColorToPt(String value) {
        return switch (value) {
            case "black" -> "Preto";
            case "white" -> "Branco";
            case "gray" -> "Cinza";
            case "brown" -> "Marrom";
            case "golden" -> "Dourado";
            case "cream" -> "Creme";
            case "tan" -> "Canela";
            case "speckled" -> "Malhado";
            default -> null;
        };
    }
    /**
     * Converte o valor de teste (yes/no/not-tested) do inglês para português.
     * 
     * @param value Valor em inglês ("yes", "no" ou "not-tested")
     * @return Valor em português ("Sim", "Não" ou "Não testado"), ou null se o valor não for reconhecido
     */
    private String convertYesNoNotTestedToPt(String value) {
        return switch (value) {
            case "yes" -> "Sim";
            case "no" -> "Não";
            case "not-tested" -> "Não testado";
            default -> null;
        };
    }
    /**
     * Converte o valor de status do inglês para português.
     * 
     * @param value Valor em inglês ("quarantine", "sheltered", "adopted" ou "lost")
     * @return Valor em português ("Quarentena", "Abrigado", "Adotado" ou "Perdido"), ou null se o valor não for reconhecido
     */
    private String convertStatusToPt(String value) {
        return switch (value) {
            case "quarantine" -> "Quarentena";
            case "sheltered" -> "Abrigado";
            case "adopted" -> "Adotado";
            case "lost" -> "Perdido";
            default -> null;
        };
    }
}
