package modules.User.views;

import config.Database;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import modules.User.controllers.UserAuthController;
import modules.User.models.User;
import utils.Session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

public class UserAuthForm extends GridPane {

    private final TextField emailField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Button loginButton = new Button("Entrar");
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

        setVgap(10);
        setHgap(10);
        setPadding(new Insets(20));

        add(new Label("E-mail:"), 0, 0);
        add(emailField, 1, 0);

        add(new Label("Senha:"), 0, 1);
        add(passwordField, 1, 1);

        add(loginButton, 1, 2);

        emailField.setPromptText("email");
        passwordField.setPromptText("senha");

        loginButton.setOnAction(e -> doLogin());
        passwordField.setOnAction(e -> doLogin());

        emailField.setPrefWidth(260);
        passwordField.setPrefWidth(260);
    }

    /**
     * Realiza a autenticação do usuário com email e senha.
     * Valida os campos antes de tentar autenticar e chama o callback onSuccess em caso de sucesso.
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
