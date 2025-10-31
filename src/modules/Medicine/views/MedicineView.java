package modules.Medicine.views;

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
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import modules.Medicine.controllers.MedicineController;
import modules.Medicine.models.Medicine;
import modules.Shared.views.MenuView;
import modules.MedicineBrand.views.MedicineBrandView;
import config.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class MedicineView extends VBox {

    private TableView<Medicine> table;
    private ObservableList<Medicine> medicinesList;
    private Connection conn;
    private MedicineController controller;
    private BorderPane mainLayout;

    /**
     * Construtor da view de lista de medicamentos.
     * 
     * @param mainLayout Layout principal da aplicação para navegação entre telas
     */
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

    /**
     * Inicializa os componentes da interface, incluindo a tabela e suas colunas.
     */
    private void initializeComponents() {
        table = new TableView<>();
        medicinesList = FXCollections.observableArrayList();
        table.setItems(medicinesList);

        // Coluna Status (primeira coluna conforme Figma)
        TableColumn<Medicine, Boolean> statusColumn = new TableColumn<>("status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("isActive"));
        statusColumn.setPrefWidth(100);
        statusColumn.setCellFactory(column -> new TableCell<Medicine, Boolean>() {
            {
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 8, 0, 12));
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setAlignment(Pos.CENTER_LEFT);
                    if (item) {
                        setText("Ativo");
                        getStyleClass().clear();
                        getStyleClass().add("status-active");
                    } else {
                        setText("Inativo");
                        getStyleClass().clear();
                        getStyleClass().add("status-inactive");
                    }
                }
            }
        });

        // Coluna Nome
        TableColumn<Medicine, String> nameColumn = new TableColumn<>("nome");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setCellFactory(column -> new TableCell<Medicine, String>() {
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

        // Coluna Marca
        TableColumn<Medicine, String> brandColumn = new TableColumn<>("marca");
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        brandColumn.setPrefWidth(150);
        brandColumn.setCellFactory(column -> new TableCell<Medicine, String>() {
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

        // Coluna Quantidade
        TableColumn<Medicine, Integer> quantityColumn = new TableColumn<>("quantidade");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setPrefWidth(120);
        quantityColumn.setCellFactory(column -> new TableCell<Medicine, Integer>() {
            {
                setAlignment(Pos.CENTER);
                setPadding(new Insets(0, 8, 0, 8));
            }
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                Medicine medicine = empty ? null : getTableView().getItems().get(getIndex());
                if (empty || medicine == null) {
                    setText(null);
                } else {
                    setAlignment(Pos.CENTER);
                    // Mostra "∞" para inativos, quantidade para ativos
                    if (!medicine.getIsActive() || item == null || item == -1) {
                        setText("∞");
                    } else {
                        setText(item.toString());
                    }
                }
            }
        });

        // Coluna Descrição
        TableColumn<Medicine, String> descriptionColumn = new TableColumn<>("descrição");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setPrefWidth(200);
        descriptionColumn.setCellFactory(column -> new TableCell<Medicine, String>() {
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

        // Coluna Ações (agrupa toggle, editar e deletar)
        TableColumn<Medicine, Void> actionsColumn = new TableColumn<>("ações");
        actionsColumn.setPrefWidth(180);
        actionsColumn.setMinWidth(180);
        actionsColumn.setMaxWidth(180);
        actionsColumn.setStyle("-fx-alignment: CENTER;");
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button toggleBtn = new Button();
            private final Button editBtn = createIconButton("/assets/icons/edit.svg", "edit");
            private final Button delBtn = createIconButton("/assets/icons/trash.svg", "delete");
            private final HBox actionsBox = new HBox(8, toggleBtn, editBtn, delBtn);

            {
                actionsBox.setAlignment(Pos.CENTER);
                
                // Botão toggle com ícone SVG
                try {
                    StackPane toggleIcon = createSvgIcon("/assets/icons/turn-on-off.svg", "#FFFFFF");
                    if (toggleIcon != null && !toggleIcon.getChildren().isEmpty()) {
                        toggleIcon.setAlignment(Pos.CENTER);
                        toggleBtn.setGraphic(toggleIcon);
                    } else {
                        toggleBtn.setText("⏻");
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao carregar ícone toggle SVG: " + e.getMessage());
                    toggleBtn.setText("⏻");
                }
                
                toggleBtn.getStyleClass().addAll("icon-btn", "icon-btn-toggle");
                toggleBtn.setPrefSize(35, 35);
                toggleBtn.setMinSize(35, 35);
                toggleBtn.setMaxSize(35, 35);
                toggleBtn.setGraphicTextGap(0);
                toggleBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                toggleBtn.setAlignment(Pos.CENTER);
                toggleBtn.setTooltip(new Tooltip("Ativar/Inativar"));
                toggleBtn.setOnAction(e -> {
                    Medicine medicine = getTableView().getItems().get(getIndex());
                    if (medicine != null) toggleActiveStatus(medicine);
                });

                editBtn.setTooltip(new Tooltip("Editar"));
                editBtn.setOnAction(e -> {
                    Medicine medicine = getTableView().getItems().get(getIndex());
                    if (medicine != null) editSelected(medicine);
                });

                delBtn.setTooltip(new Tooltip("Excluir"));
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
                    setGraphic(actionsBox);
                }
            }
        });

        table.getColumns().addAll(statusColumn, nameColumn, brandColumn, quantityColumn, descriptionColumn, actionsColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("custom-table");
        table.setPlaceholder(new Label("Nenhum medicamento cadastrado"));
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
            wrapper.getChildren().add(svgGroup);
            
            // Centraliza
            svgGroup.setTranslateX(-viewBoxWidth / 2);
            svgGroup.setTranslateY(-viewBoxHeight / 2);
            
            // Aplica a escala no wrapper
            wrapper.setScaleX(scale);
            wrapper.setScaleY(scale);
            
            // Adiciona ao pane
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
     * @param type Tipo do botão (edit, delete, toggle) para aplicar estilo específico
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
        // Logo no topo esquerdo (clicável -> Menu)
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/assets/logo.png")));
        logo.setFitWidth(140);
        logo.setPreserveRatio(true);

        Button logoBtn = new Button();
        logoBtn.setGraphic(logo);
        logoBtn.setOnAction(e -> returnToMainMenu());
        logoBtn.setCursor(javafx.scene.Cursor.HAND);
        logoBtn.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-color: transparent;");
        logoBtn.setFocusTraversable(false);

        // Título centralizado no topo
        Label title = new Label("Medicamentos");
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

        Label brandsLabel = new Label("Marcas");
        HBox brandsContent = new HBox(6, brandsLabel, paw2);
        brandsContent.setAlignment(Pos.CENTER);
        Button brandsButton = new Button();
        brandsButton.setGraphic(brandsContent);
        brandsButton.getStyleClass().add("top-btn");

        addButton.setOnAction(e -> openAddForm());
        brandsButton.setOnAction(e -> openBrandView());

        HBox buttonBar = new HBox(15, addButton, brandsButton);
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
        this.getStylesheets().add(getClass().getResource("/modules/Medicine/styles/MedicineView.css").toExternalForm());
    }

    /**
     * Carrega a lista de medicamentos do banco de dados e atualiza a tabela.
     */
    public void loadData() {
        try {
            medicinesList.clear();
            medicinesList.addAll(controller.listAll());
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao buscar medicamentos: " + e.getMessage()).showAndWait();
        }
    }

    /**
     * Abre o formulário para cadastrar um novo medicamento na mesma tela.
     */
    private void openAddForm() {
        MedicineForm form = new MedicineForm(mainLayout, this);
        mainLayout.setCenter(form);
    }

    /**
     * Abre o formulário para editar o medicamento selecionado na mesma tela.
     * 
     * @param selected Medicamento selecionado para edição
     */
    private void editSelected(Medicine selected) {
        if (selected == null) return;

        MedicineEditForm form = new MedicineEditForm(selected, controller, mainLayout, this);
        mainLayout.setCenter(form);
    }

    /**
     * Alterna o status ativo/inativo do medicamento.
     * 
     * @param selected Medicamento selecionado para alterar status
     */
    private void toggleActiveStatus(Medicine selected) {
        if (selected == null) return;

        String action = selected.getIsActive() ? "inativar" : "ativar";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Tem certeza que deseja " + action + " o medicamento '" + selected.getName() + "'?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar " + (selected.getIsActive() ? "inativação" : "ativação"));
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try {
                    Boolean newStatus = !selected.getIsActive();
                    controller.update(selected.getUuid(), selected.getName(), selected.getBrandUuid(), 
                                    selected.getQuantity(), selected.getDescription(), newStatus);
                    loadData();
                    String message = newStatus ? "Medicamento ativado com sucesso!" : "Medicamento inativado com sucesso!";
                    new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Erro ao alterar status do medicamento: " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    /**
     * Exclui o medicamento selecionado após confirmação do usuário.
     * 
     * @param selected Medicamento selecionado para exclusão
     */
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
                    controller.delete(selected.getUuid());
                    loadData();
                    new Alert(Alert.AlertType.INFORMATION, "Medicamento excluído com sucesso!").showAndWait();
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Erro ao excluir medicamento: " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    /**
     * Navega para a view de marcas de medicamentos.
     */
    private void openBrandView() {
        mainLayout.setCenter(new MedicineBrandView(mainLayout));
    }

    /**
     * Retorna para o menu principal da aplicação.
     */
    private void returnToMainMenu() {
        mainLayout.setCenter(new MenuView(mainLayout, null));
    }
}
