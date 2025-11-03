package modules.Shared.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import modules.Animal.controllers.AnimalController;
import modules.Animal.models.Animal;
import modules.Animal.views.AnimalView;
import modules.Medicine.views.MedicineView;
import config.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MenuView extends VBox {

    private BorderPane mainLayout;
    private Connection conn;

    /**
     * Construtor da view do menu principal.
     * 
     * @param mainLayout Layout principal da aplicação para navegação entre telas
     * @param stage Stage da aplicação (não utilizado atualmente)
     */
    public MenuView(BorderPane mainLayout, Stage stage) {
        this.mainLayout = mainLayout;

        try {
            this.conn = Database.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao conectar com o banco de dados.").showAndWait();
            return;
        }

        setupLayout();
        loadStatistics();
    }

    /**
     * Configura o layout principal da view do menu.
     * Não retorna valor e não lança exceções.
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
        Label title = new Label("Menu");
        title.getStyleClass().add("menu-title");

        // Botões de ação no topo direito
        ImageView paw1 = new ImageView(new Image(getClass().getResourceAsStream("/assets/patas.png")));
        ImageView paw2 = new ImageView(new Image(getClass().getResourceAsStream("/assets/patas.png")));

        paw1.setFitWidth(16);
        paw1.setPreserveRatio(true);
        paw2.setFitWidth(16);
        paw2.setPreserveRatio(true);

        Label animaisLabel = new Label("Animais");
        HBox animaisContent = new HBox(6, animaisLabel, paw1);
        animaisContent.setAlignment(Pos.CENTER);
        Button animaisButton = new Button();
        animaisButton.setGraphic(animaisContent);
        animaisButton.getStyleClass().add("top-btn");
        animaisButton.setOnAction(e -> {
            AnimalView animalView = new AnimalView(this.mainLayout);
            this.mainLayout.setCenter(animalView);
        });

        Label medicamentosLabel = new Label("Medicamentos");
        HBox medicamentosContent = new HBox(6, medicamentosLabel, paw2);
        medicamentosContent.setAlignment(Pos.CENTER);
        Button medicamentosButton = new Button();
        medicamentosButton.setGraphic(medicamentosContent);
        medicamentosButton.getStyleClass().add("top-btn");
        medicamentosButton.setOnAction(e -> {
            MedicineView medicineView = new MedicineView(this.mainLayout);
            this.mainLayout.setCenter(medicineView);
        });

        HBox buttonBar = new HBox(15, animaisButton, medicamentosButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        // Layout usando StackPane para centralizar o título absolutamente
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

        // Área de estatísticas - três colunas
        VBox statisticsContainer = createStatisticsContainer();
        
        VBox content = new VBox(40, statisticsContainer);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40, 60, 40, 60));

        this.setAlignment(Pos.TOP_CENTER);
        this.getChildren().addAll(topBar, content);
        this.getStyleClass().add("main-bg");
        
        // Carrega o CSS se existir, senão usa o CSS padrão
        try {
            String cssPath = "/modules/Shared/styles/MenuView.css";
            if (getClass().getResource(cssPath) != null) {
                this.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }
        } catch (Exception e) {
            // CSS opcional, não quebra se não existir
        }
    }

    private Label quarentenaDogsLabel, quarentenaCatsLabel, quarentenaTotalLabel;
    private Label abrigadosDogsLabel, abrigadosCatsLabel, abrigadosTotalLabel;
    private Label adotadosDogsLabel, adotadosCatsLabel, adotadosTotalLabel;

    /**
     * Cria o container com as três colunas de estatísticas.
     * 
     * @return VBox com as três colunas de dados
     */
    private VBox createStatisticsContainer() {
        // Coluna Quarentena
        VBox quarentenaColumn = createStatisticsColumn("Quarentena", 
            createStatsBox("0", "Total de cães", "stats-box-dark"),
            createStatsBox("0", "Total de gatos", "stats-box-dark"),
            createStatsBox("0", "Total de animais", "stats-box-light"));

        // Armazena referências dos labels
        VBox quarentenaBoxes = (VBox) quarentenaColumn.getChildren().get(1);
        this.quarentenaDogsLabel = (Label) ((VBox) quarentenaBoxes.getChildren().get(0)).getChildren().get(0);
        this.quarentenaCatsLabel = (Label) ((VBox) quarentenaBoxes.getChildren().get(1)).getChildren().get(0);
        this.quarentenaTotalLabel = (Label) ((VBox) quarentenaBoxes.getChildren().get(2)).getChildren().get(0);

        // Coluna Abrigados
        VBox abrigadosColumn = createStatisticsColumn("Abrigados",
            createStatsBox("0", "Total de cães", "stats-box-dark"),
            createStatsBox("0", "Total de gatos", "stats-box-dark"),
            createStatsBox("0", "Total de animais", "stats-box-light"));

        VBox abrigadosBoxes = (VBox) abrigadosColumn.getChildren().get(1);
        this.abrigadosDogsLabel = (Label) ((VBox) abrigadosBoxes.getChildren().get(0)).getChildren().get(0);
        this.abrigadosCatsLabel = (Label) ((VBox) abrigadosBoxes.getChildren().get(1)).getChildren().get(0);
        this.abrigadosTotalLabel = (Label) ((VBox) abrigadosBoxes.getChildren().get(2)).getChildren().get(0);

        // Coluna Adotados
        VBox adotadosColumn = createStatisticsColumn("Adotados",
            createStatsBox("0", "Total de cães", "stats-box-dark"),
            createStatsBox("0", "Total de gatos", "stats-box-dark"),
            createStatsBox("0", "Total de animais", "stats-box-light"));

        VBox adotadosBoxes = (VBox) adotadosColumn.getChildren().get(1);
        this.adotadosDogsLabel = (Label) ((VBox) adotadosBoxes.getChildren().get(0)).getChildren().get(0);
        this.adotadosCatsLabel = (Label) ((VBox) adotadosBoxes.getChildren().get(1)).getChildren().get(0);
        this.adotadosTotalLabel = (Label) ((VBox) adotadosBoxes.getChildren().get(2)).getChildren().get(0);

        // Container para as três colunas
        HBox columnsContainer = new HBox(30, quarentenaColumn, abrigadosColumn, adotadosColumn);
        columnsContainer.setAlignment(Pos.CENTER);

        VBox container = new VBox(columnsContainer);
        container.setAlignment(Pos.CENTER);

        return container;
    }


    /**
     * Cria uma coluna de estatísticas com título e boxes de dados.
     * 
     * @param title Título da coluna
     * @param boxes Boxes de dados para adicionar na coluna
     * @return VBox com a coluna completa
     */
    private VBox createStatisticsColumn(String title, VBox... boxes) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("column-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        VBox boxesContainer = new VBox(15, boxes);
        boxesContainer.setAlignment(Pos.CENTER);
        boxesContainer.setPrefWidth(280);

        VBox column = new VBox(20, titleLabel, boxesContainer);
        column.setAlignment(Pos.CENTER);
        column.setPrefWidth(280);
        
        return column;
    }

    /**
     * Cria um box de estatística com número e label.
     * 
     * @param number Número a ser exibido
     * @param label Texto descritivo
     * @param styleClass Classe CSS do box
     * @return VBox configurado como box de estatística
     */
    private VBox createStatsBox(String number, String label, String styleClass) {
        Label numberLabel = new Label(number);
        numberLabel.getStyleClass().add("stats-number");

        Label labelText = new Label(label);
        labelText.getStyleClass().add("stats-label");

        VBox box = new VBox(8, numberLabel, labelText);
        box.getStyleClass().addAll("stats-box", styleClass);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 15, 20, 15));
        box.setPrefWidth(250);
        box.setMinWidth(250);
        box.setMaxWidth(250);

        return box;
    }

    /**
     * Carrega as estatísticas dos animais e atualiza os boxes.
     * 
     * @throws SQLException Se ocorrer erro na operação do banco de dados (tratado internamente com Alert)
     */
    private void loadStatistics() {
        try {
            List<Animal> animals = AnimalController.getAllAnimals(conn);
            
            // Inicializa contadores
            int quarentenaDogs = 0, quarentenaCats = 0;
            int abrigadosDogs = 0, abrigadosCats = 0;
            int adotadosDogs = 0, adotadosCats = 0;

            // Conta animais por status e espécie
            for (Animal animal : animals) {
                String status = animal.getStatus();
                String species = animal.getSpecies();
                
                if (status != null && species != null) {
                    if ("quarantine".equals(status)) {
                        if ("dog".equals(species)) quarentenaDogs++;
                        else if ("cat".equals(species)) quarentenaCats++;
                    } else if ("sheltered".equals(status)) {
                        if ("dog".equals(species)) abrigadosDogs++;
                        else if ("cat".equals(species)) abrigadosCats++;
                    } else if ("adopted".equals(status)) {
                        if ("dog".equals(species)) adotadosDogs++;
                        else if ("cat".equals(species)) adotadosCats++;
                    }
                }
            }

            // Atualiza os labels nos boxes
            int quarentenaTotal = quarentenaDogs + quarentenaCats;
            int abrigadosTotal = abrigadosDogs + abrigadosCats;
            int adotadosTotal = adotadosDogs + adotadosCats;

            quarentenaDogsLabel.setText(String.valueOf(quarentenaDogs));
            quarentenaCatsLabel.setText(String.valueOf(quarentenaCats));
            quarentenaTotalLabel.setText(String.valueOf(quarentenaTotal));

            abrigadosDogsLabel.setText(String.valueOf(abrigadosDogs));
            abrigadosCatsLabel.setText(String.valueOf(abrigadosCats));
            abrigadosTotalLabel.setText(String.valueOf(abrigadosTotal));

            adotadosDogsLabel.setText(String.valueOf(adotadosDogs));
            adotadosCatsLabel.setText(String.valueOf(adotadosCats));
            adotadosTotalLabel.setText(String.valueOf(adotadosTotal));

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao carregar estatísticas: " + e.getMessage()).showAndWait();
        }
    }
}
