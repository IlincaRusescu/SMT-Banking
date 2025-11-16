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
 * JavaFX UI class for creating new user and customer accounts.
 * <p>
 * Provides a registration form that validates input data before creating
 * corresponding {@link Customer} and {@link User} objects.
 * 
 * <p>
 * Duplicate phone numbers, CNPs, or usernames are not allowed.
 * 
 *
 * author Ilinca Rusescu
 * @version 1.2
 */
public class SignUpUI {

    /** Available gender options. */
    private static final String[] GENDERS = {"Female", "Male", "Other"};

    /** List of supported EU countries with ISO codes. */
    private static final String[] EU_COUNTRIES = {
            "Austria (AT)", "Belgium (BE)", "Bulgaria (BG)", "Croatia (HR)", "Cyprus (CY)",
            "Czech Republic (CZ)", "Denmark (DK)", "Estonia (EE)", "Finland (FI)", "France (FR)",
            "Germany (DE)", "Greece (GR)", "Hungary (HU)", "Ireland (IE)", "Italy (IT)",
            "Latvia (LV)", "Lithuania (LT)", "Luxembourg (LU)", "Malta (MT)", "Netherlands (NL)",
            "Poland (PL)", "Portugal (PT)", "Romania (RO)", "Slovakia (SK)", "Slovenia (SI)",
            "Spain (ES)", "Sweden (SE)", "United Kingdom (UK)"
    };

    // =============================
    //         HELPER METHODS
    // =============================

    /** Extracts the country code from a string like "Romania (RO)". */
    private static String extractCountryCode(String value) {
        return value.substring(value.indexOf("(") + 1, value.indexOf(")"));
    }

    /** Returns the full country name from a 2-letter code (e.g., "RO" → "Romania"). */
    public static String getCountryFullName(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) return "-";
        String code = countryCode.trim().toUpperCase();

        for (String entry : EU_COUNTRIES) {
            // fiecare element e gen "Romania (RO)"
            if (entry.endsWith("(" + code + ")")) {
                return entry.substring(0, entry.indexOf(" (")).trim();
            }
        }
        return code;
    }

    /** Helper for building labeled input rows. */
    private static HBox row(String labelText, Control field) {
        Label label = new Label(labelText);
        label.setMinWidth(110);
        HBox box = new HBox(10, label, field);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // =============================
    //         MAIN SCENE
    // =============================
    /**
     * Builds and returns the sign-up scene.
     * <p>
     * The form collects customer data, performs validation, prevents duplicate
     * phone/CNP/username entries, and creates linked {@link Customer} and {@link User} objects.
     * 
     *
     * @param stage     the main application {@link Stage}
     * @param customers list of existing customers
     * @param users     list of existing users
     * @return the constructed {@link Scene} for user registration
     */
    public static Scene createSignUpScene(Stage stage, List<Customer> customers, List<User> users) {

        stage.setTitle("SMT Banking - Sign Up");

        Label title = new Label("CREATE NEW ACCOUNT");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        // ===== INPUT FIELDS =====
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();

        TextField ageField = new TextField();
        ageField.textProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) ageField.setText(oldVal);
        });

        ComboBox<String> genderField = new ComboBox<>();
        genderField.getItems().addAll(GENDERS);

        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        phoneField.textProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) phoneField.setText(oldVal);
        });

        TextField cnpField = new TextField();
        cnpField.textProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) cnpField.setText(oldVal);
        });

        TextField address1Field = new TextField();
        TextField address2Field = new TextField();
        TextField cityField = new TextField();
        TextField postalField = new TextField();

        ComboBox<String> countryField = new ComboBox<>();
        countryField.getItems().addAll(EU_COUNTRIES);

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmField = new PasswordField();

        Label message = new Label();
        message.setStyle("-fx-text-fill: red;");


        // ===== LAYOUT: LEFT COLUMN =====
        VBox left = new VBox(
                row("First Name:", firstNameField),
                row("Last Name:", lastNameField),
                row("Age:", ageField),
                row("Gender:", genderField),
                row("CNP:", cnpField),
                row("Address 1:", address1Field),
                row("Address 2:", address2Field),
                row("City:", cityField),
                row("Postal Code:", postalField),
                row("Country:", countryField)
        );
        left.setSpacing(12);

        // ===== LAYOUT: RIGHT COLUMN =====
        VBox right = new VBox(
                row("Email:", emailField),
                row("Phone Number:", phoneField),
                row("Username:", usernameField),
                row("Password:", passwordField),
                row("Confirm Password:", confirmField)
        );
        right.setSpacing(14);

        // ===== CREATE BUTTON in RIGHT COLUMN =====
        Button createBtn = new Button("CREATE");
        createBtn.setPrefWidth(160);
        createBtn.setStyle("-fx-font-weight: bold;");
        HBox createButtonBox = new HBox(createBtn);
        createButtonBox.setAlignment(Pos.CENTER);
        createButtonBox.setPadding(new Insets(30, 0, 25, 0));
        right.getChildren().add(createButtonBox);

        // ===== BACK BUTTON (in right column) =====
        Button backBtn = new Button("BACK");
        backBtn.setPrefWidth(90);
        HBox backButtonBox = new HBox(backBtn);
        backButtonBox.setAlignment(Pos.CENTER_RIGHT);
        backButtonBox.setPadding(new Insets(45, 0, 0, 0));

        right.getChildren().addAll(backButtonBox);

        // ===== TWO-COLUMN LAYOUT =====
        HBox formLayout = new HBox(90, left, right);
        formLayout.setAlignment(Pos.CENTER);

        // ===== MAIN CONTAINER =====
        VBox main = new VBox(25, title, formLayout, message);
        main.setPadding(new Insets(35, 50, 35, 50));
        main.setAlignment(Pos.TOP_CENTER);

        // =============================
        //          BUTTON ACTIONS
        // =============================

        /**
         * Validates all inputs, checks for duplicates, and creates the user and customer.
         */
        createBtn.setOnAction(e -> {
            List<String> errors = new ArrayList<>();

            // --- Field validation ---
            if (!emailField.getText().contains("@"))
                errors.add("Invalid email format");
            if (!passwordField.getText().equals(confirmField.getText()))
                errors.add("Passwords do not match");
            if (genderField.getValue() == null || countryField.getValue() == null)
                errors.add("Select gender and country");
            if (ageField.getText().isBlank())
                errors.add("Age cannot be blank");
            if (usernameField.getText().isBlank())
                errors.add("Username cannot be blank");

            // --- Duplicate checks ---
            if (CustomerFileManager.phoneExists(customers, phoneField.getText()))
                errors.add("Phone number already exists");
            if (CustomerFileManager.cnpExists(customers, cnpField.getText()))
                errors.add("CNP already exists");
            if (UserFileManager.usernameExists(users, usernameField.getText()))
                errors.add("Username already exists");

            // --- Display all errors if any ---
            if (!errors.isEmpty()) {
                message.setStyle("-fx-text-fill: red;");
                message.setText(String.join("; ", errors));
                return;
            }

            // --- Create new entities ---
            try {
                String countryCode = extractCountryCode(countryField.getValue());

                Customer newCustomer = new Customer(
                        firstNameField.getText(),
                        lastNameField.getText(),
                        Integer.parseInt(ageField.getText()),
                        genderField.getValue(),
                        emailField.getText(),
                        phoneField.getText(),
                        cnpField.getText(),
                        address1Field.getText(),
                        address2Field.getText().isBlank() ? null : address2Field.getText(),
                        cityField.getText(),
                        postalField.getText(),
                        countryCode
                );

                // --- Create linked user ---
                User newUser = new User(usernameField.getText(), passwordField.getText(), newCustomer);

                customers.add(newCustomer);
                users.add(newUser);

                // --- Save data ---
                CustomerFileManager.saveCustomers(customers);
                UserFileManager.saveUsers(users);

                message.setStyle("-fx-text-fill: green;");
                message.setText("Account created successfully!");
                stage.setScene(LoginUI.createLoginScene(stage, customers, users));

            } catch (Exception ex) {
                message.setStyle("-fx-text-fill: red;");
                message.setText(ex.getMessage());
            }
        });

        /** Navigates back to the login screen. */
        backBtn.setOnAction(e -> stage.setScene(LoginUI.createLoginScene(stage, customers, users)));

        return new Scene(main, 800, 550);
    }
}
