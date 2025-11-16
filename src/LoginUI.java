import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

/**
 * JavaFX UI class for the login screen of the SMT Banking application.
 * <p>
 * Provides a graphical interface for user authentication. It validates login credentials
 * against registered {@link User} objects and, upon successful login, redirects the user
 * to the {@link DashboardUI} scene. The interface also allows navigation to account creation
 * and password recovery screens.
 * 
 *
 * author Ilinca Rusescu
 * @version 1.0
 */

public class LoginUI {

    // =============================
    //        UI HELPER METHOD
    // =============================

    /**
     * Creates a labeled input field for consistent UI formatting.
     *
     * @param labelText text to display above the input field
     * @param field     the {@link Control} component (e.g., {@link TextField}, {@link PasswordField})
     * @param width     desired width for the input field
     * @return a {@link VBox} containing the label and field, centered horizontally
     */
    private static VBox labeledField(String labelText, Control field, double width) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px;");
        label.setMinWidth(width);
        label.setMaxWidth(width);
        label.setAlignment(Pos.CENTER_LEFT); // eticheta aliniată la stânga pe lățimea field-ului

        field.setMaxWidth(width);

        VBox box = new VBox(3, label, field);
        box.setAlignment(Pos.CENTER); // încadrăm totul în centru pe pagină
        return box;
    }

    // =============================
    //         MAIN SCENE
    // =============================

    /**
     * Builds and returns the login scene for the JavaFX stage.
     * <p>
     * Includes username/password fields, login validation logic, and navigation
     * to "Create Account" and "Forgot Password" screens.
     * 
     *
     * @param stage     the main application {@link Stage}
     * @param customers list of registered {@link Customer} objects
     * @param users     list of registered {@link User} objects
     * @return the constructed {@link Scene} for the login interface
     */
    public static Scene createLoginScene(Stage stage, List<Customer> customers, List<User> users){
        stage.setTitle("SMT Banking - Login");

        double FIELD_WIDTH = 170;

        Label title = new Label("SMT Banking");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 13px;");

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(FIELD_WIDTH-20);

        Button signUpBtn = new Button("Create Account");
        signUpBtn.setMaxWidth(FIELD_WIDTH-20);

        Button forgotPasswordBtn = new Button("Forgot Password?");
        forgotPasswordBtn.setMaxWidth(FIELD_WIDTH-50);
        forgotPasswordBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: blue; -fx-underline: true;");

        // =============================
        //         LOGIN ACTION
        // =============================

        /**
         * Validates user credentials and loads the dashboard upon success.
         * Displays an error message if authentication fails or the customer link is missing.
         */
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            User found = users.stream()
                    .filter(u -> u.getUsername().equals(username) && u.checkPassword(password))
                    .findFirst()
                    .orElse(null);

            if (found == null) {
                messageLabel.setText("Invalid username or password!");
                return;
            }

            // Associate corresponding customer
            Customer linked = customers.stream()
                    .filter(c -> c.getCustomerId().equals(found.getCustomerId()))
                    .findFirst()
                    .orElse(null);

            if (linked != null){
                found.associateCustomer(linked);
            }else {
                messageLabel.setText("Customer profile not found for this user!");
                return;
            }

            stage.setScene(DashboardUI.createDashboardScene(stage, found, Main.customers, Main.accounts));
        });

        // =============================
        //         NAVIGATION
        // =============================

        /**
         * Redirects the user to the registration scene.
         */
        signUpBtn.setOnAction(e -> stage.setScene(SignUpUI.createSignUpScene(stage, customers, users)));

        /**
         * Redirects the user to the "Forgot Password" scene.
         */
        forgotPasswordBtn.setOnAction(e -> stage.setScene(ForgotPasswordUI.createForgotPasswordScene(stage, customers, users)));

        // =============================
        //         SCENE LAYOUT
        // =============================

        VBox layout = new VBox(
                10,
                title,
                labeledField("Username", usernameField, FIELD_WIDTH),
                labeledField("Password", passwordField, FIELD_WIDTH),
                messageLabel,
                loginButton,
                signUpBtn,
                forgotPasswordBtn
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        return new Scene(layout, 420, 420);
    }
}
