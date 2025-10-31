package modules.MedicineBrand.views;

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
import modules.MedicineBrand.controllers.MedicineBrandController;
import modules.MedicineBrand.models.MedicineBrand;
import modules.Shared.views.MenuView;
import modules.Medicine.views.MedicineView;
import config.Database;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.SQLException;

public class MedicineBrandView extends VBox {

    private TableView<MedicineBrand> table;
    private ObservableList<MedicineBrand> brandsList;
    private Connection conn;
    private MedicineBrandController controller;
    private BorderPane mainLayout;

    /**
     * Construtor da view de lista de marcas de medicamentos.
     * 
     * @param mainLayout Layout principal da aplicação para navegação entre telas
     */
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

    /**
     * Inicializa os componentes da interface, incluindo a tabela e suas colunas.
     */
    private void initializeComponents() {
        table = new TableView<>();
        brandsList = FXCollections.observableArrayList();
        table.setItems(brandsList);

        // Coluna Nome
        TableColumn<MedicineBrand, String> nameColumn = new TableColumn<>("nome");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setCellFactory(column -> new TableCell<MedicineBrand, String>() {
            {
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 8, 0, 12));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Coluna Ações (editar e deletar)
        TableColumn<MedicineBrand, Void> actionsColumn = new TableColumn<>("ações");
        actionsColumn.setPrefWidth(120);
        actionsColumn.setMinWidth(120);
        actionsColumn.setMaxWidth(120);
        actionsColumn.setStyle("-fx-alignment: CENTER;");
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = createIconButton("/assets/icons/edit.svg", "edit");
            private final Button delBtn = createIconButton("/assets/icons/trash.svg", "delete");
            private final HBox actionsBox = new HBox(8, editBtn, delBtn);

            {
                actionsBox.setAlignment(Pos.CENTER);
                
                editBtn.setTooltip(new Tooltip("Editar"));
                editBtn.setOnAction(e -> {
                    MedicineBrand brand = getTableView().getItems().get(getIndex());
                    if (brand != null) editSelected(brand);
                });

                delBtn.setTooltip(new Tooltip("Excluir"));
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
                    setGraphic(actionsBox);
                }
            }
        });

        table.getColumns().addAll(nameColumn, actionsColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("custom-table");
        table.setPlaceholder(new Label("Nenhuma marca cadastrada"));
    }

    /**
     * Cria um ícone SVG usando SVGPath com escala correta baseada no viewBox.
     * 
     * @param svgPath Caminho do arquivo SVG
     * @param color Cor do ícone
     * @return StackPane com o ícone renderizado e dimensionado corretamente
     */
    private StackPane createSvgIcon(String svgPath, String color) {
        StackPane iconPane = new StackPane();
        iconPane.setPrefSize(18, 18);
        iconPane.setMaxSize(18, 18);
        iconPane.setAlignment(Pos.CENTER);
        
        try {
            // Lê o conteúdo do SVG
            String svgContent = new BufferedReader(
                new InputStreamReader(
                    getClass().getResourceAsStream(svgPath),
                    StandardCharsets.UTF_8
                )
            ).lines().collect(Collectors.joining("\n"));
            
            if (svgContent == null || svgContent.isEmpty()) {
                return iconPane;
            }
            
            // Extrai o viewBox do SVG
            java.util.regex.Pattern viewBoxPattern = java.util.regex.Pattern.compile(
                "viewBox\\s*=\\s*[\"']([^\"']+)[\"']", 
                java.util.regex.Pattern.CASE_INSENSITIVE
            );
            java.util.regex.Matcher viewBoxMatcher = viewBoxPattern.matcher(svgContent);
            
            double viewBoxWidth = 24.0; // Default do SVG comum
            double viewBoxHeight = 24.0;
            
            if (viewBoxMatcher.find()) {
                String[] viewBoxValues = viewBoxMatcher.group(1).trim().split("[\\s,]+");
                if (viewBoxValues.length >= 4) {
                    viewBoxWidth = Double.parseDouble(viewBoxValues[2]);
                    viewBoxHeight = Double.parseDouble(viewBoxValues[3]);
                }
            }
            
            // Calcula o scale para caber no tamanho desejado (18x18)
            double targetSize = 18.0;
            double scale = Math.min(targetSize / viewBoxWidth, targetSize / viewBoxHeight);
            scale *= 0.75; // Margem para evitar bordas cortadas e garantir espaço
            
            // Cria um Group para conter todos os paths
            javafx.scene.Group svgGroup = new javafx.scene.Group();
            
            // Extrai todos os paths do SVG
            java.util.regex.Pattern pathPattern = java.util.regex.Pattern.compile(
                "<path[^>]*d\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>", 
                java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL
            );
            java.util.regex.Matcher pathMatcher = pathPattern.matcher(svgContent);
            
            while (pathMatcher.find()) {
                String pathData = pathMatcher.group(1);
                SVGPath svgPathShape = new SVGPath();
                svgPathShape.setContent(pathData);
                svgPathShape.setFill(javafx.scene.paint.Color.valueOf(color));
                svgPathShape.setStrokeWidth(0);
                svgGroup.getChildren().add(svgPathShape);
            }
            
            // Se não encontrou paths, retorna vazio
            if (svgGroup.getChildren().isEmpty()) {
                return iconPane;
            }
            
            // Cria um wrapper Group para aplicar transformações
            javafx.scene.Group wrapper = new javafx.scene.Group();
            
            // Primeiro, centraliza o conteúdo SVG em relação ao viewBox
            svgGroup.setTranslateX(-viewBoxWidth / 2);
            svgGroup.setTranslateY(-viewBoxHeight / 2);
            
            // Aplica a escala no wrapper (escalando do centro)
            wrapper.setScaleX(scale);
            wrapper.setScaleY(scale);
            wrapper.getChildren().add(svgGroup);
            
            // O wrapper já está centralizado, apenas adiciona ao pane
            iconPane.getChildren().add(wrapper);
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar SVG: " + svgPath + " - " + e.getMessage());
            e.printStackTrace();
        }
        
        return iconPane;
    }

    /**
     * Cria um botão com ícone SVG personalizado.
     * 
     * @param svgPath Caminho do arquivo SVG a ser exibido
     * @param type Tipo do botão (edit, delete) para aplicar estilo específico
     * @return Button configurado com o ícone e estilo apropriados
     */
    private Button createIconButton(String svgPath, String type) {
        Button btn = new Button();

        String iconColor = "#FFFFFF";
        try {
            StackPane iconView = createSvgIcon(svgPath, iconColor);
            if (iconView != null && !iconView.getChildren().isEmpty()) {
                btn.setGraphic(iconView);
            } else {
                btn.setText(null); // sem fallback de texto
            }
        } catch (Exception e) {
            btn.setText(null); // sem texto mesmo no fallback
        }

        btn.getStyleClass().addAll("icon-btn", "icon-btn-" + type);

        // tamanho fixo
        btn.setPrefSize(35, 35);
        btn.setMinSize(35, 35);
        btn.setMaxSize(35, 35);

        // **centralização real do gráfico**
        btn.setGraphicTextGap(0);
        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btn.setAlignment(Pos.CENTER);
        
        // Garante que o StackPane do ícone esteja centralizado
        if (btn.getGraphic() != null && btn.getGraphic() instanceof StackPane) {
            StackPane iconStack = (StackPane) btn.getGraphic();
            iconStack.setAlignment(Pos.CENTER);
        }

        return btn;
    }

    /**
     * Configura o layout principal da view, incluindo logo, título e botões de ação.
     */
    private void setupLayout() {
        // Logo no topo esquerdo
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/assets/logo.png")));
        logo.setFitWidth(140);
        logo.setPreserveRatio(true);

        Button logoBtn = new Button();
        logoBtn.setGraphic(logo);
        logoBtn.setOnAction(e -> mainLayout.setCenter(new MenuView(mainLayout, null)));
        logoBtn.setCursor(javafx.scene.Cursor.HAND);
        logoBtn.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-color: transparent;");
        logoBtn.setFocusTraversable(false);

        // Título centralizado no topo
        Label title = new Label("Marcas");
        title.getStyleClass().add("title");

        // Botões de ação no topo direito
        ImageView paw1 = new ImageView(new Image(getClass().getResourceAsStream("/assets/patas.png")));
        ImageView paw2 = new ImageView(new Image(getClass().getResourceAsStream("/assets/patas.png")));

        paw1.setFitWidth(16);
        paw1.setPreserveRatio(true);
        paw2.setFitWidth(16);
        paw2.setPreserveRatio(true);

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

        addButton.setOnAction(e -> openAddForm());
        productsButton.setOnAction(e -> openMedicineView());

        HBox buttonBar = new HBox(15, addButton, productsButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        // Layout usando StackPane para centralizar o título absolutamente sobre a tabela
        // O título fica centralizado ignorando logo e botões
        HBox leftBar = new HBox(logoBtn);
        leftBar.setAlignment(Pos.CENTER_LEFT);

        HBox rightBar = new HBox(buttonBar);
        rightBar.setAlignment(Pos.CENTER_RIGHT);

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

        VBox content = new VBox(30, table);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40, 60, 40, 60));
        VBox.setVgrow(table, Priority.ALWAYS);

        this.setAlignment(Pos.TOP_CENTER);
        this.getChildren().addAll(topBar, content);
        this.getStyleClass().add("main-bg");
        this.getStylesheets().add(getClass().getResource("/modules/MedicineBrand/styles/MedicineBrandView.css").toExternalForm());
    }

    /**
     * Carrega a lista de marcas de medicamentos do banco de dados e atualiza a tabela.
     */
    public void loadData() {
        try {
            brandsList.clear();
            brandsList.addAll(controller.listAll());
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao buscar marcas: " + e.getMessage()).showAndWait();
        }
    }

    /**
     * Abre o formulário para cadastrar uma nova marca de medicamento na mesma tela.
     */
    private void openAddForm() {
        MedicineBrandForm form = new MedicineBrandForm(mainLayout, this);
        mainLayout.setCenter(form);
    }

    /**
     * Abre o formulário para editar a marca de medicamento selecionada na mesma tela.
     * 
     * @param selected Marca de medicamento selecionada para edição
     */
    private void editSelected(MedicineBrand selected) {
        if (selected == null) return;

        MedicineBrandEditForm form = new MedicineBrandEditForm(selected, controller, mainLayout, this);
        mainLayout.setCenter(form);
    }

    /**
     * Exclui a marca de medicamento selecionada após confirmação do usuário.
     * 
     * @param selected Marca de medicamento selecionada para exclusão
     */
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
                    controller.delete(selected.getUuid());
                    loadData();
                    new Alert(Alert.AlertType.INFORMATION, "Marca excluída com sucesso!").showAndWait();
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Erro ao excluir marca: " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    /**
     * Retorna para o menu principal da aplicação.
     */
    private void returnToMainMenu() {
        mainLayout.setCenter(new MenuView(mainLayout, null));
    }

    /**
     * Navega para a view de medicamentos.
     */
    private void openMedicineView() {
        mainLayout.setCenter(new MedicineView(mainLayout));
    }
}