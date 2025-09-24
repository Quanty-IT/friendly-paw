package modules.views;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;

public class MenuView extends VBox {

    private BorderPane mainLayout;

    public MenuView(BorderPane mainLayout, Stage stage) {
        this.mainLayout = mainLayout;

        // Botões "Animais" e "Produtos"
        Button btnAnimais = new Button("Animais");
        Button btnMedicamentos = new Button("Medicamentos");

        // Ao clicar em "Animais", carrega a view de animais
        btnAnimais.setOnAction(e -> {
            AnimalView animalView = new AnimalView(this.mainLayout);
            this.mainLayout.setCenter(animalView);
        });

        // Ao clicar em "Medicamentos", carrega a view de medicamentos
        btnMedicamentos.setOnAction(e -> {
            MedicineView medicineView = new MedicineView(this.mainLayout);
            this.mainLayout.setCenter(medicineView);
        });

        // Organiza os botões na tela
        this.getChildren().addAll(btnAnimais, btnMedicamentos);
    }
}
