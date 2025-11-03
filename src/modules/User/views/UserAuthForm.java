package modules.User.views;

import config.Database;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import modules.User.controllers.UserAuthController;
import modules.User.models.User;
import utils.Session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

public class UserAuthForm extends VBox {

    private final TextField emailField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Button loginButton = new Button("Login");
    private Connection conn;

    private final Consumer<User> onSuccess;

     /**
     * Construtor do formulário de autenticação de usuário.
     * 
     * @param onSuccess Callback executado após login bem-sucedido
     */
    public UserAuthForm(Consumer<User> onSuccess) {
        this.onSuccess = onSuccess;

        try {
            this.conn = Database.getConnection();
        } catch (SQLException e) {
            showError("Falha na conexão com o banco:\n" + e.getMessage());
            loginButton.setDisable(true);
        }

        setupLayout();
        setupStyles();
    }

    /**
     * Configura o layout do formulário de autenticação, incluindo campos e botões.
     * Não retorna valor e não lança exceções.
     */
    private void setupLayout() {
        setAlignment(Pos.CENTER);

        // espaço entre logo e formulário
        setSpacing(12);

        setPadding(new Insets(60, 40, 60, 40));
        getStyleClass().add("login-bg");

        final double INPUT_W = 220;
        final double TRANSLATE_X = -65;

        // Logo
        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/assets/logo.png")));
        logoView.setFitWidth(220);
        logoView.setPreserveRatio(true);

        VBox logoContainer = new VBox(6, logoView);
        logoContainer.setAlignment(Pos.CENTER);

        // Form
        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(0));

        Label emailLabel = new Label("Email:");
        emailLabel.getStyleClass().add("login-label");
        emailLabel.setTranslateX(TRANSLATE_X);

        Label passwordLabel = new Label("Senha:");
        passwordLabel.getStyleClass().add("login-label");
        passwordLabel.setTranslateX(TRANSLATE_X);

        emailField.setPromptText("exemplo@email.com");
        emailField.setTranslateX(TRANSLATE_X);
        passwordField.setPromptText("*************");
        passwordField.setTranslateX(TRANSLATE_X);
        emailField.getStyleClass().add("login-input");
        passwordField.getStyleClass().add("login-input");

        emailField.setPrefWidth(INPUT_W);
        passwordField.setPrefWidth(INPUT_W);
        emailField.setMinWidth(Region.USE_PREF_SIZE);
        emailField.setMaxWidth(Region.USE_PREF_SIZE);
        passwordField.setMinWidth(Region.USE_PREF_SIZE);
        passwordField.setMaxWidth(Region.USE_PREF_SIZE);

        // Grid alinhando label (direita) e campo (esquerda)
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(18);
        grid.setAlignment(Pos.CENTER);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(120);
        c1.setHalignment(HPos.RIGHT);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.NEVER);
        c2.setFillWidth(false);
        c2.setMaxWidth(INPUT_W);

        grid.getColumnConstraints().setAll(c1, c2);

        GridPane.setHgrow(emailField, Priority.NEVER);
        GridPane.setHgrow(passwordField, Priority.NEVER);

        grid.add(emailLabel, 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);

        // Botão
        loginButton.getStyleClass().add("login-btn");
        loginButton.setPrefWidth(160);
        loginButton.setPrefHeight(42);
        loginButton.setOnAction(e -> doLogin());
        passwordField.setOnAction(e -> doLogin());

        grid.add(loginButton, 0, 2, 2, 1);
        GridPane.setHalignment(loginButton, HPos.CENTER);

        formContainer.getChildren().add(grid);

        getChildren().addAll(logoContainer, formContainer);

        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                    if (newWin != null) {
                        emailField.requestFocus();
                    }
                });
            }
        });

        emailField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> loginButton.fire());
        loginButton.setDefaultButton(true);
    }

    /**
     * Configura os estilos CSS do formulário de autenticação.
     * Não retorna valor e não lança exceções.
     */
    private void setupStyles() {
        getStylesheets().add(getClass().getResource("/modules/User/styles/UserAuthForm.css").toExternalForm());
    }

    /**
     * Realiza a autenticação do usuário com email e senha.
     * Valida os campos antes de tentar autenticar e chama o callback onSuccess em caso de sucesso.
     * 
     * @throws SQLException Se ocorrer erro na operação do banco de dados (tratado internamente com Alert)
     */
    private void doLogin() {
        if (conn == null) {
            showError("Sem conexão com o banco.");
            return;
        }
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (email.isEmpty() || password.isEmpty()) {
            showError("Preencha e-mail e senha.");
            return;
        }

        try {
            User user = UserAuthController.login(conn, email, password);
            if (user == null) {
                showError("Credenciais inválidas.");
                return;
            }
            Session.set(user);
            if (onSuccess != null) onSuccess.accept(user);
        } catch (SQLException ex) {
            showError("Erro ao autenticar:\n" + ex.getMessage());
        }
    }

    /**
     * Exibe uma mensagem de erro em um Alert.
     * 
     * @param msg Mensagem de erro a ser exibida
     */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText("Erro");
        alert.showAndWait();
    }
}
