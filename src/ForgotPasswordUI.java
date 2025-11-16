import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX UI for the "Forgot/Reset Password" flow.
 * <p>
 * Verifies identity using {@code username} + linked customer {@code phone},
 * validates the new password, and updates it if all checks pass.
 * Displays all validation errors at once.
 * 
 *
 * author Ilinca Rusescu
 * @version 1.1
 */
public class ForgotPasswordUI {

    // =============================
    //         UI HELPER
    // =============================

    /** Centered row with label and field as a unit. */
    private static HBox row(String labelText, Control field) {
        Label label = new Label(labelText);
        label.setMinWidth(130); // aliniere perfectă
        field.setPrefWidth(220); // câmp constant

        HBox box = new HBox(8, label, field);
        box.setAlignment(Pos.CENTER); // CENTRAT
        return box;
    }

    // =============================
    //          MAIN SCENE
    // =============================

    /**
     * Builds the Reset Password scene.
     *
     * @param stage     primary {@link Stage}
     * @param customers loaded customers list (used for linking info on users)
     * @param users     loaded users list
     * @return JavaFX {@link Scene} for resetting passwords
     */
    public static Scene createForgotPasswordScene(Stage stage, List<Customer> customers, List<User> users) {

        stage.setTitle("SMT Banking - Reset Password");

        Label title = new Label("RESET PASSWORD");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        title.setPadding(new Insets(0, 0, 25, 0)); // Padding sub titlu +++ ADJUSTABIL

        // ==== FIELDS ====
        TextField usernameField = new TextField();
        TextField phoneField = new TextField();
        phoneField.textProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) phoneField.setText(oldVal);
        });

        PasswordField newPasswordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();

        Label message = new Label();
        message.setStyle("-fx-text-fill: red; -fx-font-size: 13px;");

        // ==== BUTTONS ====
        Button resetBtn = new Button("Reset Password");
        resetBtn.setPrefWidth(180);
        resetBtn.setStyle("-fx-font-weight: bold;");

        Button backBtn = new Button("Back");
        backBtn.setPrefWidth(120);

        // =============================
        //      BUTTON ACTIONS
        // =============================

        resetBtn.setOnAction(e -> {
            List<String> errors = new ArrayList<>();
            String username = usernameField.getText().trim();
            String phone = phoneField.getText().trim();
            String newPwd = newPasswordField.getText();
            String confirmPwd = confirmPasswordField.getText();

            // --- Input validations ---
            if (username.isEmpty())
                errors.add("Username cannot be blank");
            if (phone.isEmpty())
                errors.add("Phone number cannot be blank");
            else if (phone.length() != 10)
                errors.add("Phone number must have 10 digits");
            if (newPwd == null || newPwd.length() < 4)
                errors.add("New password must be at least 4 characters");
            if (!String.valueOf(newPwd).equals(String.valueOf(confirmPwd)))
                errors.add("Passwords do not match");

            // --- User + phone pairing check (only if basic fields provided) ---
            User foundUser = null;
            if (errors.isEmpty() || (!username.isEmpty() && !phone.isEmpty())) {
                for (User u : users) {
                    if (u.getUsername().equals(username) &&
                            u.getCustomer() != null &&
                            u.getCustomer().getPhoneNumber().equals(phone)) {
                        foundUser = u;
                        break;
                    }
                }
                if (foundUser == null)
                    errors.add("Invalid username or phone number");
            }

            // --- Show all errors at once ---
            if (!errors.isEmpty()) {
                message.setStyle("-fx-text-fill: red;");
                message.setText(String.join("; ", errors));
                return;
            }
            try {
                foundUser.resetPassword(newPwd);
                UserFileManager.saveUsers(users);

                message.setStyle("-fx-text-fill: green;");
                message.setText("Password successfully reset! Returning to login...");
                stage.setScene(LoginUI.createLoginScene(stage, customers, users));

            } catch (Exception ex) {
                message.setStyle("-fx-text-fill: red;");
                message.setText(ex.getMessage());
            }
        });

        backBtn.setOnAction(e -> stage.setScene(LoginUI.createLoginScene(stage, customers, users)));

        // ==== FORM (rows) ====
        VBox form = new VBox(
                15,
                row("Username:", usernameField),
                row("Phone Number:", phoneField),
                row("New Password:", newPasswordField),
                row("Confirm Password:", confirmPasswordField)
        );
        form.setAlignment(Pos.CENTER);

        // ==== BUTTONS LAYOUT ====
        VBox buttons = new VBox(10, resetBtn, backBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20, 0, 0, 0)); // <<< SPAȚIU sub Confirm Password ADJUSTABIL

        // ==== MAIN ====
        VBox main = new VBox(25, title, form, buttons, message);
        main.setAlignment(Pos.CENTER);
        main.setPadding(new Insets(30, 0, 30, 0));

        return new Scene(main, 500, 450); // <<< Înălțime redusă (ajustabilă)
    }
}
