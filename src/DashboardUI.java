import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * High-level JavaFX UI builder for the main dashboard of SMT Banking.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Composes the main dashboard layout (header, sidebar, content, utilities panel).</li>
 *   <li>Handles tab switching between Debit/Savings/Credit/Transactions and the Profile view.</li>
 *   <li>Renders account info cards and the current-month cashflow chart.</li>
 *   <li>Provides popups for: account creation, deposit/withdraw, credit operations, external transfer.</li>
 * </ul>
 * All methods are pure UI builders/controllers; application logic remains in the domain classes.
 * 
 *
 * <h2>Navigation Sections (Ctrl+F shortcuts):</h2>
 *
 * <pre>
 * HEADER SECTION
 * LEFT SIDEBAR MENU
 * CENTER LAYOUT CONTAINER
 * TAB HANDLERS
 * UPDATE CENTER: TRANSACTIONS TAB
 * UPDATE CENTER: ACCOUNT MISSING
 * UPDATE CENTER: ACCOUNT PRESENT
 * RIGHT COLUMN: DEBIT ACTIONS
 * RIGHT COLUMN: SAVINGS ACTIONS
 * RIGHT COLUMN: CREDIT ACTIONS
 * CHART: CURRENT MONTH CASHFLOW
 * POPUP: CREATE ACCOUNT
 * POPUP: OPERATION (DEPOSIT/WITHDRAW)
 * POPUP: CREDIT OPERATION (TAKE/REPAY)
 * POPUP: EXTERNAL TRANSFER (IBAN)
 * HELPERS: ACCOUNT LOOKUP
 * PROFILE SECTION
 * </pre>
 *
 * author Ilinca Rusescu
 * @version 2.3
 */
public class DashboardUI {

    // =============================
    //        THEME CONSTANTS
    // =============================

    private static final String BG_LIGHT = "#f7f7f7";
    private static final String CARD_LIGHT = "#ffffff";
    private static final String BORDER_LIGHT = "#dcdcdc";
    private static final String TEXT_MAIN = "#222222";
    private static final String TEXT_MUTED = "#555555";
    private static final String ACCENT = "#4FB7B3";
    private static final Insets PADDING_PAGE = new Insets(20, 25, 20, 25);

    // =============================
    //      UI PRIMITIVES (PILL)
    // =============================
    /**
     * Builds a "pill" style button with active/inactive styling.
     *
     * @param text   label to display
     * @param active whether the pill is highlighted as active
     * @return configured {@link Button}
     */
    private static Button pill(String text, boolean active) {
        String bgColor = active ? ACCENT : "#f0f0f0";
        String textColor = active ? "#000000" : TEXT_MAIN;
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + bgColor + ";" +
                "-fx-text-fill:" + textColor + ";" +
                "-fx-font-size:13px;" +
                "-fx-font-weight:" + (active ? "700" : "500") + ";" +
                "-fx-background-radius:10;" +
                "-fx-border-color:" + BORDER_LIGHT + ";" +
                "-fx-border-radius:10;" +
                "-fx-padding:8 16;");
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    // =============================
    //      UI PRIMITIVES (CARD)
    // =============================
    /**
     * Wraps a region into a card-like container with padding and border.
     *
     * @param content inner region (any layout/control)
     * @return {@link VBox} styled as a card
     */
    private static VBox card(Region content) {
        VBox box = new VBox(content);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:" + CARD_LIGHT + ";" +
                "-fx-border-color:" + BORDER_LIGHT + ";" +
                "-fx-border-width: 1;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;");
        return box;
    }

    // =============================
    //         TEXT HELPERS
    // =============================
    /**
     * Builds a bold heading label with a specific font size.
     *
     * @param text heading text
     * @param size font size in px
     * @return configured {@link Label}
     */
    private static Label heading(String text, int size) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:" + TEXT_MAIN + ";-fx-font-size:" + size + "px;-fx-font-weight:700;");
        return l;
    }

    /**
     * Builds a muted/secondary text label.
     *
     * @param text content text
     * @param size font size in px
     * @return configured {@link Label}
     */
    private static Label labelMuted(String text, int size) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:" + TEXT_MUTED + ";-fx-font-size:" + size + "px;");
        return l;
    }

    // =============================
    //         MONEY FORMAT
    // =============================
    /**
     * Formats a double value as money with 2 decimals and currency suffix.
     *
     * @param value    amount value
     * @param currency ISO currency code (e.g., "RON", "EUR")
     * @return formatted string like "1,234.00 RON"
     */
    private static String money(double value, String currency) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(value) + " " + currency;
    }

    // =============================
    //        TAB STYLE TOGGLER
    // =============================

    /**
     * Marks one button as the active tab and resets styling for the others.
     *
     * @param active active tab button (nullable for "no tab" cases like Profile)
     * @param others other buttons to reset
     */
    private static void setActiveTab(Button active, Button... others) {
        if (active != null) {
            active.setStyle("-fx-background-color:" + ACCENT + ";" +
                    "-fx-text-fill:#000000;" +
                    "-fx-font-weight:700;" +
                    "-fx-border-color:#dcdcdc;" +
                    "-fx-border-width:1;" +
                    "-fx-border-radius:8;" +
                    "-fx-background-radius:8;" +
                    "-fx-padding:8 22;");
        }

        for (Button b : others) {
            b.setStyle("-fx-background-color:#f0f0f0;" +
                    "-fx-text-fill:" + TEXT_MAIN + ";" +
                    "-fx-font-weight:500;" +
                    "-fx-border-color:#dcdcdc;" +
                    "-fx-border-radius:8;" +
                    "-fx-background-radius:8;" +
                    "-fx-padding:8 22;");
        }
    }

    // =============================
    //          MAIN SCENE
    // =============================

    /**
     * Builds the main dashboard scene (header, sidebar, center content, right panel).
     *
     * @param stage        primary {@link Stage}
     * @param user         logged-in {@link User}
     * @param customers    loaded list of {@link Customer}
     * @param allAccounts  loaded list of {@link Account} across all customers
     * @return root {@link Scene} for the dashboard
     */
    public static Scene createDashboardScene(Stage stage,
                                             User user,
                                             List<Customer> customers,
                                             List<Account> allAccounts) {

        Customer currentCustomer = user.getCustomer();
        String cid = user.getCustomerId();

        List<Account> userAccounts = allAccounts.stream()
                .filter(a -> Objects.equals(a.getCustomerId(), cid))
                .collect(Collectors.toList());

        // =============================
        //         HEADER SECTION
        // =============================

        Label appTitle = heading("SMT Banking", 24);
        Label hello = labelMuted("Hello, " + currentCustomer.getFirstName(), 14);
        Hyperlink profile = new Hyperlink("Profile");
        Hyperlink logout = new Hyperlink("Log Out");
        profile.setStyle("-fx-text-fill:" + ACCENT + "; -fx-underline: false;");
        logout.setStyle("-fx-text-fill:" + ACCENT + "; -fx-underline: false;");
        logout.setOnAction(e -> {
            List<User> users = UserFileManager.loadUsers();
            stage.setScene(LoginUI.createLoginScene(stage, customers, users));
        });

        HBox headerRight = new HBox(10, hello, new Label("|"), profile, new Label("|"), logout);
        headerRight.setAlignment(Pos.CENTER_RIGHT);

        BorderPane header = new BorderPane();
        header.setLeft(appTitle);
        header.setRight(headerRight);
        header.setPadding(new Insets(10, 0, 10, 0));

        Separator sep = new Separator();

        // =============================
        //       LEFT SIDEBAR MENU
        // =============================
        ComboBox<Account> debitSelector = new ComboBox<>();
        debitSelector.setPrefWidth(80);
        debitSelector.setPrefHeight(36);
        debitSelector.setStyle(
                "-fx-background-color:white;" +
                        "-fx-border-color:#dcdcdc;" +
                        "-fx-border-width:1 1 1 0;" +
                        "-fx-border-radius:0 8 8 0;" +
                        "-fx-background-radius:0 8 8 0;" +
                        "-fx-font-size:13px;"
        );

        List<Account> debitAccounts = userAccounts.stream()
                .filter(a -> a.getType() == 'D')
                .collect(Collectors.toList());

        // if there is no debit account registered for the user, a debit account is created by default
        if (debitAccounts.isEmpty()) {
            String newId = IdGenerator.nextAccountId();
            Account ronDebit = new DebitAccount(currentCustomer, null, 0.0, "RON");
            try {
                var field = Account.class.getDeclaredField("accountId");
                field.setAccessible(true);
                field.set(ronDebit, newId);
            } catch (Exception ignored) {}
            debitAccounts.add(ronDebit);
            userAccounts.add(ronDebit);
            allAccounts.add(ronDebit);
            AccountFileManager.saveAccounts(allAccounts);
        }

        debitSelector.getItems().setAll(debitAccounts);
        debitSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCurrency());
            }
        });
        debitSelector.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCurrency());
            }
        });
        debitSelector.getSelectionModel().selectFirst();

        Button btnDebit = new Button("DEBIT");
        btnDebit.setStyle(
                "-fx-background-color:" + ACCENT + ";" +
                        "-fx-text-fill:#000000;" +
                        "-fx-font-weight:700;" +
                        "-fx-border-color:#dcdcdc;" +
                        "-fx-border-width:1;" +
                        "-fx-border-radius:8 0 0 8;" +
                        "-fx-background-radius:8 0 0 8;" +
                        "-fx-padding:8 22;"
        );
        btnDebit.setPrefWidth(160);
        btnDebit.setPrefHeight(36);

        HBox debitRow = new HBox(btnDebit, debitSelector);
        debitRow.setSpacing(0);
        debitRow.setAlignment(Pos.CENTER_LEFT);

        Button btnSavings = pill("SAVINGS", false);
        Button btnCredit = pill("CREDIT", false);
        Button btnTx = pill("TRANSACTIONS", false);
        VBox leftMenu = new VBox(12, debitRow, btnSavings, btnCredit, btnTx);
        leftMenu.setPrefWidth(240);

        // =============================
        //     CENTER LAYOUT CONTAINER
        // =============================

        VBox centerCol = new VBox(18);

        // =============================
        //       RIGHT UTILS PANEL
        // =============================

        VBox rightCol = new VBox(12);
        rightCol.setPrefWidth(180);
        profile.setOnAction(e -> {
            setActiveTab(null, btnDebit, btnSavings, btnCredit, btnTx);
            rightCol.setVisible(false);
            showProfile(centerCol, currentCustomer, userAccounts, allAccounts, stage, debitSelector, rightCol);

        });

        // =============================
        //       DEFAULT: DEBIT TAB
        // =============================

        updateCenterContent(centerCol, 'D', currentCustomer, userAccounts, allAccounts, stage, debitSelector, rightCol);

        HBox content = new HBox(25, leftMenu, centerCol, rightCol);
        VBox root = new VBox(30, header, sep, content);
        root.setPadding(PADDING_PAGE);
        root.setStyle("-fx-background-color:" + BG_LIGHT + ";");

        // =============================
        //          TAB HANDLERS
        // =============================

        btnDebit.setOnAction(e -> {
            setActiveTab(btnDebit, btnSavings, btnCredit, btnTx);
            updateCenterContent(centerCol, 'D', currentCustomer, userAccounts, allAccounts, stage, debitSelector, rightCol);
        });
        btnSavings.setOnAction(e -> {
            setActiveTab(btnSavings, btnDebit, btnCredit, btnTx);
            updateCenterContent(centerCol, 'S', currentCustomer, userAccounts, allAccounts, stage, debitSelector, rightCol);
        });
        btnCredit.setOnAction(e -> {
            setActiveTab(btnCredit, btnDebit, btnSavings, btnTx);
            updateCenterContent(centerCol, 'C', currentCustomer, userAccounts, allAccounts, stage, debitSelector, rightCol);
        });
        btnTx.setOnAction(e -> {
            setActiveTab(btnTx, btnDebit, btnSavings, btnCredit);
            updateCenterContent(centerCol, 'T', currentCustomer, userAccounts, allAccounts, stage, debitSelector, rightCol);
        });


        return new Scene(root, 1100, 650);
    }

    // =============================
    //        UPDATE CENTER
    // =============================

    /**
     * Re-renders the center column (and right utility panel) based on the selected tab.
     * <p>Cases:
     * <ul>
     *   <li><b>T (Transactions)</b> — shows merged, sorted transaction history across user's accounts.</li>
     *   <li><b>D/S/C</b> — shows account info, actions (on right) and the monthly cashflow chart.</li>
     *   <li>Missing account for the tab — shows a "create account" call-to-action.</li>
     * </ul>
     *
     * @param centerCol      main center container
     * @param type           tab type: 'D', 'S', 'C', 'T'
     * @param customer       current customer
     * @param userAccounts   current user's accounts
     * @param allAccounts    all accounts in the system
     * @param stage          owner stage
     * @param debitSelector  combo used for selecting debit currency (nullable on non-D)
     * @param rightCol       right utilities column
     */
    private static void updateCenterContent(VBox centerCol, char type,
                                            Customer customer,
                                            List<Account> userAccounts,
                                            List<Account> allAccounts,
                                            Stage stage,
                                            ComboBox<Account> debitSelector,
                                            VBox rightCol) {
        centerCol.getChildren().clear();
        rightCol.getChildren().clear();
        rightCol.setVisible(false);

        // =============================
        //  UPDATE CENTER: TRANSACTIONS TAB
        // =============================

        if (type == 'T') {
            Label title = heading("Recent Transactions", 18);

            List<Object[]> txPairs = allAccounts.stream()
                    .filter(a -> a.getCustomerId().equals(customer.getCustomerId()))
                    .flatMap(a -> a.getTransactions().stream().map(t -> new Object[]{a.getCurrency(), t}))
                    .sorted((o1, o2) -> {
                        Transaction t1 = (Transaction) o1[1];
                        Transaction t2 = (Transaction) o2[1];
                        return t2.getTimestamp().compareTo(t1.getTimestamp());
                    })
                    .collect(Collectors.toList());

            VBox txContainer = new VBox(10);
            txContainer.setPadding(new Insets(10));
            txContainer.setAlignment(Pos.TOP_LEFT);

            if (txPairs.isEmpty()) {
                Label empty = labelMuted("No transactions yet.", 14);
                txContainer.getChildren().add(empty);
            } else {
                int i = 0;
                for (Object[] pair : txPairs) {
                    String currency = (String) pair[0];
                    Transaction tx = (Transaction) pair[1];

                    HBox row = new HBox(15);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(10, 15, 10, 15));
                    row.setMinWidth(780);
                    row.setMaxWidth(Double.MAX_VALUE);
                    row.setStyle(String.format(
                            "-fx-background-color:%s; -fx-border-color:#e0e0e0; -fx-background-radius:8; -fx-border-radius:8;",
                            (i++ % 2 == 0 ? "white" : "#fafafa")
                    ));

                    Label typeLbl = new Label(tx.getType());
                    typeLbl.setStyle("-fx-font-weight:600; -fx-text-fill:#666; -fx-min-width:120;");

                    Label descLbl = new Label(tx.getDescription());
                    descLbl.setWrapText(true);
                    descLbl.setMaxWidth(420);
                    descLbl.setStyle("-fx-text-fill:#333;");

                    Label amountLbl = new Label(String.format("%+.2f %s", tx.getAmount(), currency));
                    String color = tx.getAmount() < 0 ? "#d9534f" : "#28a745";
                    amountLbl.setStyle("-fx-font-weight:700; -fx-text-fill:" + color + "; -fx-min-width:120;");

                    Label dateLbl = new Label(tx.getTimestamp().toLocalDate().toString());
                    dateLbl.setStyle("-fx-text-fill:#777; -fx-font-size:12px;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    row.getChildren().addAll(typeLbl, descLbl, spacer, amountLbl, dateLbl);
                    txContainer.getChildren().add(row);
                }
            }

            ScrollPane scroll = new ScrollPane(txContainer);
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setStyle("-fx-background-color:transparent; -fx-border-color:transparent;");
            scroll.setPrefViewportHeight(520);

            // main container (same as balance + chart from bank accounts display
            VBox contentBox = new VBox(15, title, scroll);
            contentBox.setAlignment(Pos.TOP_LEFT);
            contentBox.setPadding(new Insets(0, 0, 0, 10));
            contentBox.setPrefWidth(780);

            // wrapper
            HBox alignedBox = new HBox();
            alignedBox.setAlignment(Pos.TOP_LEFT);
            alignedBox.setPadding(new Insets(0, 0, 0, 0));
            alignedBox.getChildren().add(contentBox);

            centerCol.getChildren().clear();
            centerCol.setAlignment(Pos.TOP_LEFT);
            centerCol.getChildren().add(alignedBox);
            return;
        }

        // =============================
        //   UPDATE CENTER: ACCOUNT PICK
        // =============================

        Account acc = (type == 'D') ? debitSelector.getValue()
                : userAccounts.stream().filter(a -> a.getType() == type).findFirst().orElse(null);

        // =============================
        //   UPDATE CENTER: ACCOUNT MISSING
        // =============================

        if (acc == null) {
            Label msg = labelMuted("Account not found. Please create an account.", 15);
            msg.setAlignment(Pos.CENTER);

            Button createBtn = new Button("Create " +
                    (type == 'D' ? "Debit" : type == 'S' ? "Savings" : "Credit") + " Account");
            createBtn.setStyle("-fx-background-color:" + ACCENT + ";-fx-font-weight:700;" +
                    "-fx-text-fill:#000;-fx-background-radius:8;-fx-padding:10 24;");
            createBtn.setOnAction(e -> showCreatePopup(type, customer, userAccounts, allAccounts, stage, centerCol, debitSelector));

            VBox contentBox = new VBox(15, msg, createBtn);
            contentBox.setAlignment(Pos.CENTER);
            contentBox.setPadding(new Insets(40));
            contentBox.setPrefWidth(700);
            centerCol.getChildren().add(card(contentBox));
        } else {

            // =============================
            //   UPDATE CENTER: ACCOUNT PRESENT
            // =============================

            rightCol.setVisible(true);
            rightCol.getChildren().clear();

            // =============================
            //    RIGHT COLUMN: DEBIT ACTIONS
            // =============================

            if (type == 'D') {
                Button depositBtn = pill("DEPOSIT", false);
                Button withdrawBtn = pill("WITHDRAW", false);
                Button transferBtn = pill("TRANSFER", false);
                Button createDebitBtn = pill("+ NEW DEBIT ACCOUNT", false);
                depositBtn.setOnAction(e ->
                        showOperationPopup("deposit", acc, customer, allAccounts, stage, centerCol, debitSelector, rightCol));
                withdrawBtn.setOnAction(e ->
                        showOperationPopup("withdraw", acc, customer, allAccounts, stage, centerCol, debitSelector, rightCol));
                transferBtn.setOnAction(e ->
                        showTransferPopup(acc, allAccounts, stage, centerCol, customer, debitSelector, rightCol)
                );

                createDebitBtn.setOnAction(e ->
                        showCreatePopup('D', customer, userAccounts, allAccounts, stage, centerCol, debitSelector));

                rightCol.getChildren().addAll(depositBtn, withdrawBtn, transferBtn, createDebitBtn);

                // =============================
                //   RIGHT COLUMN: CREDIT ACTIONS
                // =============================
            } else if (type == 'C') {
                Button repayBtn = pill("REPAY CREDIT", false);
                Button takeBtn = pill("TAKE CREDIT", false);
                Button nextMonthBtn = pill("NEXT MONTH", false);
                repayBtn.setOnAction(e ->
                        showCreditOperationPopup("repay", (CreditAccount) acc, allAccounts, stage, centerCol, customer, rightCol)
                );
                takeBtn.setOnAction(e ->
                        showCreditOperationPopup("take", (CreditAccount) acc, allAccounts, stage, centerCol, customer, rightCol)
                );

                nextMonthBtn.setOnAction(e -> {
                    double before = acc.getBalance();
                    ((CreditAccount) acc).applyMonthlyUpdate();
                    double delta = acc.getBalance() - before;
                    acc.addTransaction("INTEREST_APPLIED", delta, "Monthly credit interest applied.");

                    updateCenterContent(centerCol, 'C', customer,
                            allAccounts.stream().filter(a -> a.getCustomerId().equals(customer.getCustomerId())).toList(),
                            allAccounts, stage, debitSelector, rightCol);
                });
                rightCol.getChildren().addAll(repayBtn, takeBtn,nextMonthBtn);

                // =============================
                //  RIGHT COLUMN: SAVINGS ACTIONS
                // =============================
            } else {
                Button depositBtn = pill("DEPOSIT", false);
                Button withdrawBtn = pill("WITHDRAW", false);
                Button nextMonthBtn = pill("NEXT MONTH", false);
                depositBtn.setOnAction(e ->
                        showOperationPopup("deposit", acc, customer, allAccounts, stage, centerCol, debitSelector, rightCol));
                withdrawBtn.setOnAction(e ->
                        showOperationPopup("withdraw", acc, customer, allAccounts, stage, centerCol, debitSelector, rightCol));
                nextMonthBtn.setOnAction(e -> {
                    double before = acc.getBalance();
                    ((SavingsAccount) acc).applyMonthlyUpdate();
                    double delta = acc.getBalance() - before;
                    acc.addTransaction("INTEREST_APPLIED", delta, "Monthly savings interest applied.");

                    updateCenterContent(centerCol, 'S', customer,
                            allAccounts.stream().filter(a -> a.getCustomerId().equals(customer.getCustomerId())).toList(),
                            allAccounts, stage, debitSelector, rightCol);
                });
                rightCol.getChildren().addAll(depositBtn, withdrawBtn,nextMonthBtn);
            }

            // =============================
            //   ACCOUNT CARD (BALANCE/IBAN)
            // =============================

            Label balanceTitle = labelMuted("Balance", 13);
            Label balanceValue = heading(money(acc.getBalance(), acc.getCurrency()), 22);
            Label ibanTitle = labelMuted("IBAN", 13);
            Label ibanValue = labelMuted(acc.getIban(), 14);
            VBox info = new VBox(6, balanceTitle, balanceValue, ibanTitle, ibanValue);

            // =============================
            //  CHART: CURRENT MONTH CASHFLOW
            // =============================
            YearMonth currentMonth = YearMonth.now();

            CategoryAxis x = new CategoryAxis();
            NumberAxis y = new NumberAxis();
            x.setLabel(currentMonth.getMonth().name().substring(0, 1).toUpperCase()
                    + currentMonth.getMonth().name().substring(1).toLowerCase()
                    + " " + currentMonth.getYear());
            y.setLabel("Amount (" + acc.getCurrency() + ")");

            BarChart<String, Number> chart = new BarChart<>(x, y);
            chart.setTitle(acc.getCurrency() + " Cashflow");
            chart.setLegendVisible(false);
            chart.setLegendSide(javafx.geometry.Side.BOTTOM);
            chart.setAnimated(false);
            chart.setStyle("-fx-background-color: transparent;");

            // styling - wider bars
            chart.setCategoryGap(70);
            chart.setBarGap(-40);
            chart.setVerticalGridLinesVisible(false); // opțional, doar linii orizontale

            // transaction filter - only from the current month
            List<Transaction> accTx = acc.getTransactions().stream()
                    .filter(t -> t != null && YearMonth.from(t.getTimestamp()).equals(currentMonth))
                    .collect(Collectors.toList());

            // Money In / Money Out totals
            double inTotal = 0, outTotal = 0;
            for (Transaction t : accTx) {
                if (t.getAmount() > 0) inTotal += t.getAmount();
                else outTotal += Math.abs(t.getAmount());
            }

            // Series
            XYChart.Series<String, Number> inSeries = new XYChart.Series<>();
            inSeries.setName("Money In");
            inSeries.getData().add(new XYChart.Data<>("Money In", inTotal));

            XYChart.Series<String, Number> outSeries = new XYChart.Series<>();
            outSeries.setName("Money Out");
            outSeries.getData().add(new XYChart.Data<>("Money Out", outTotal));

            chart.getData().addAll(inSeries, outSeries);

            // Styling
            Platform.runLater(() -> {
                for (Node n : chart.lookupAll(".series0.chart-bar")) {
                    n.setStyle("-fx-bar-fill: #28a745;"); // verde (Money In)
                }
                for (Node n : chart.lookupAll(".series1.chart-bar")) {
                    n.setStyle("-fx-bar-fill: #d9534f;"); // roșu (Money Out)
                }
            });

            // --- Final Layout---
            VBox chartBox = new VBox(10, chart);
            chartBox.setAlignment(Pos.CENTER);
            centerCol.getChildren().addAll(card(info), card(chartBox));


            if (type == 'D' && debitSelector != null) {
                debitSelector.valueProperty().addListener((obs, oldA, newA) ->
                        updateCenterContent(centerCol, 'D', customer, userAccounts, allAccounts, stage, debitSelector, rightCol));
            }
        }
    }

    // =============================
    //        POPUP: CREATE ACCOUNT
    // =============================

    /**
     * Shows a popup to create a new account of a given type for the current customer.
     * <p>
     * Debit: lets user pick currency. Savings/Credit: default preconfigured currency/params.
     * Automatically saves and updates selectors/center content after creation.
     * 
     *
     * @param type          account type: 'D', 'S', or 'C'
     * @param customer      current customer
     * @param userAccounts  mutable list of customer's accounts
     * @param allAccounts   global mutable list of all accounts
     * @param parentStage   owner stage
     * @param centerCol     center column container (for refresh)
     * @param debitSelector debit accounts combo (nullable for non-debit)
     */
    private static void showCreatePopup(char type,
                                        Customer customer,
                                        List<Account> userAccounts,
                                        List<Account> allAccounts,
                                        Stage parentStage,
                                        VBox centerCol,
                                        ComboBox<Account> debitSelector) {
        Stage popup = new Stage(StageStyle.UNDECORATED);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(parentStage);

        Label title = heading("Create " +
                (type == 'D' ? "Debit" : type == 'S' ? "Savings" : "Credit") + " Account", 18);
        Label msg = labelMuted(type == 'D'
                ? "Choose a currency for your new Debit account."
                : "Confirm creation of new account.", 14);

        ComboBox<String> currencyBox = new ComboBox<>();
        if (type == 'D') {
            currencyBox.getItems().addAll("RON", "EUR", "USD");
            currencyBox.getSelectionModel().selectFirst();
        }

        Button confirm = new Button("Confirm");
        confirm.setStyle("-fx-background-color:" + ACCENT + ";-fx-font-weight:700;");
        confirm.setOnAction(e -> {
            try {
                Account newAcc = switch (type) {
                    case 'D' -> new DebitAccount(customer, null, 0.0, currencyBox.getValue());
                    case 'S' -> new SavingsAccount(customer, null, 0.0, "RON", 2.0);
                    case 'C' -> new CreditAccount(customer, null, 0.0, "RON", 5.0, -5000.0);
                    default -> null;
                };
                if (newAcc != null) {
                    allAccounts.add(newAcc);
                    userAccounts.add(newAcc);
                    AccountFileManager.saveAccounts(allAccounts);

                    if (type == 'D' && debitSelector != null) {
                        debitSelector.getItems().add(newAcc);
                        debitSelector.getSelectionModel().select(newAcc);
                    }

                    popup.close();
                    VBox rightCol = (VBox) ((HBox) ((VBox) parentStage.getScene().getRoot()).getChildren().get(2)).getChildren().get(2);
                    updateCenterContent(centerCol, type, customer, userAccounts, allAccounts, parentStage, debitSelector, rightCol);
                }
            } catch (Exception ex) {
                System.out.println("Error creating account: " + ex.getMessage());
                popup.close();
            }
        });

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> popup.close());

        HBox buttons = new HBox(10, confirm, cancel);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(15, title, msg);
        if (type == 'D') layout.getChildren().add(currencyBox);
        layout.getChildren().add(buttons);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color:" + CARD_LIGHT + ";-fx-border-color:" + BORDER_LIGHT + ";-fx-border-radius:8;");
        popup.setScene(new Scene(layout));
        popup.showAndWait();
    }

    // =============================
    //   POPUP: OPERATION (DEPOSIT/WITHDRAW)
    // =============================
    /**
     * Shows the Deposit/Withdraw popup for Debit or Savings accounts.
     * <ul>
     *   <li>Withdraw on Debit: supports quick transfer to other Debit accounts (with conversion).</li>
     *   <li>Deposit to Savings: pulls funds from matching-currency Debit.</li>
     *   <li>Withdraw from Savings: pushes funds to matching-currency Debit.</li>
     * </ul>
     *
     * @param op             "deposit" or "withdraw"
     * @param account        selected account (Debit or Savings)
     * @param customer       current customer
     * @param allAccounts    all accounts list
     * @param parentStage    owner stage
     * @param centerCol      center column container to refresh
     * @param debitSelector  debit currency selector (nullable for Savings)
     * @param rightCol       right utilities column
     */
    private static void showOperationPopup(
            String op,                  // "deposit" or "withdraw"
            Account account,            // selected account (Debit sau Savings)
            Customer customer,
            List<Account> allAccounts,
            Stage parentStage,
            VBox centerCol,
            ComboBox<Account> debitSelector, // can be null if account = Savings
            VBox rightCol
    ) {
        boolean isDeposit = "deposit".equalsIgnoreCase(op);
        boolean isWithdraw = "withdraw".equalsIgnoreCase(op);

        Stage popup = new Stage(StageStyle.UTILITY);
        popup.initOwner(parentStage);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle((isDeposit ? "Deposit" : "Withdraw") + " - " + account.getCurrency());

        Label title = new Label((isDeposit ? "Deposit" : "Withdraw") + " money");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700;");

        Label amountLbl = new Label("Amount (" + account.getCurrency() + "):");

        TextField amountField = new TextField();
        amountField.setPromptText("e.g. 100");
        amountField.setPrefWidth(150);

        HBox amountRow;
        if (isWithdraw) {
            Button maxBtn = new Button("MAX");
            maxBtn.setStyle("""
                -fx-background-color: #f0f0f0;
                -fx-border-color: #ccc;
                -fx-font-weight: 600;
                -fx-padding: 3 10;
                """);
            maxBtn.setOnAction(ev -> {
                double balance = account.getBalance();
                amountField.setText(String.format(Locale.US, "%.2f", balance));
            });
            amountRow = new HBox(8, amountField, maxBtn);
        } else {
            amountRow = new HBox(amountField);
        }
        amountRow.setAlignment(Pos.CENTER_LEFT);

        Label feedback = new Label();
        feedback.setStyle("-fx-text-fill: #b33; -fx-font-size: 12px;");

        VBox targetButtonsBox = new VBox(8); // pentru butoanele valută

        Button confirm = new Button("Confirm");
        Button cancel  = new Button("Cancel");

        confirm.setStyle("-fx-background-color:" + ACCENT + "; -fx-font-weight:700;");
        cancel.setStyle("-fx-background-color:#ddd;");

        // =============================
        //       OPERATION LOGIC
        // =============================
        confirm.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) {
                    feedback.setText("Amount must be positive.");
                    return;
                }

                // =============================
                //         DEPOSIT
                // =============================
                if (isDeposit) {
                    if (account.getType() == 'D') {
                        account.deposit(amount);
                        account.addTransaction("DEPOSIT", amount, "Money deposited successfully. | Debit Account");
                    } else if (account.getType() == 'S') {
                        // Deposit in Savings → withdraw from Debit (RON only)
                        Account source = allAccounts.stream()
                                .filter(a -> a.getType() == 'D'
                                        && a.getCustomerId().equals(customer.getCustomerId())
                                        && a.getCurrency().equalsIgnoreCase(account.getCurrency()))
                                .findFirst().orElse(null);
                        if (source == null) {
                            feedback.setText("No matching Debit account found.");
                            return;
                        }
                        if (source.getBalance() < amount) {
                            feedback.setText("Insufficient funds in Debit.");
                            return;
                        }
                        source.withdraw(amount);
                        account.deposit(amount);
                        source.addTransaction("WITHDRAW", -amount, "Money transferred to Savings Account | Debit Account");
                        account.addTransaction("DEPOSIT", amount, "Money deposited from Debit Account | Savings Account");
                    }

                    feedback.setStyle("-fx-text-fill: green;");
                    feedback.setText("Deposit successful!");
                }

                // =============================
                //         WITHDRAW
                // =============================
                else if (isWithdraw) {

                    // --- Savings → Debit
                    if (account.getType() == 'S') {
                        Account target = allAccounts.stream()
                                .filter(a -> a.getType() == 'D'
                                        && a.getCustomerId().equals(customer.getCustomerId())
                                        && a.getCurrency().equalsIgnoreCase(account.getCurrency()))
                                .findFirst().orElse(null);
                        if (target == null) {
                            feedback.setText("No matching Debit account found.");
                            return;
                        }
                        if (account.getBalance() < amount) {
                            feedback.setText("Insufficient balance in Savings.");
                            return;
                        }
                        account.withdraw(amount);
                        target.deposit(amount);
                        target.addTransaction("DEPOSIT", amount, "Deposit from Savings account | Debit Account");
                        account.addTransaction("WITHDRAW", -amount, "Withdrawn from Savings to Debit | Savings Account");

                        feedback.setStyle("-fx-text-fill: green;");
                        feedback.setText("Withdraw successful!");
                    }

                    // --- Debit → another Debit
                    else if (account.getType() == 'D') {
                        if (account.getBalance() < amount) {
                            feedback.setText("Insufficient funds.");
                            return;
                        }

                        // Find the other debit accounts (other currencies)
                        List<Account> otherDebits = allAccounts.stream()
                                .filter(a -> a.getType() == 'D'
                                        && a.getCustomerId().equals(customer.getCustomerId())
                                        && !a.getCurrency().equalsIgnoreCase(account.getCurrency()))
                                .collect(java.util.stream.Collectors.toList());

                        if (otherDebits.isEmpty()) {
                            account.withdraw(amount);
                            feedback.setText("Withdrew " + money(amount, account.getCurrency()));
                        } else {
                            // currency buttons
                            targetButtonsBox.getChildren().clear();
                            for (Account targetDebit : otherDebits) {
                                String currency = targetDebit.getCurrency();
                                Button transferBtn = new Button("Withdraw to " + currency + " account");
                                transferBtn.setStyle(
                                        "-fx-background-color:#f0f0f0;-fx-font-weight:600;-fx-padding:6 10;"
                                );
                                transferBtn.setOnAction(ev -> {
                                    double converted = ExchangeRate.convert(amount, account.getCurrency(), currency);
                                    account.withdraw(amount);
                                    targetDebit.deposit(converted);
                                    account.addTransaction("WITHDRAW", -amount,
                                            "Transferred to " + currency + " debit account.");
                                    targetDebit.addTransaction("DEPOSIT", converted,
                                            "Received from " + account.getCurrency() + " debit account.");

                                    TransactionFileManager.saveTransactions(allAccounts);
                                    AccountFileManager.saveAccounts(allAccounts);
                                    feedback.setStyle("-fx-text-fill: green;");
                                    feedback.setText(String.format(
                                            "Transferred %.2f %s → %.2f %s",
                                            amount, account.getCurrency(), converted, currency
                                    ));
                                    AccountFileManager.saveAccounts(allAccounts);
                                    popup.close();

                                    // UI update
                                    List<Account> userAccounts = allAccounts.stream()
                                            .filter(a -> a.getCustomerId().equals(customer.getCustomerId()))
                                            .collect(java.util.stream.Collectors.toList());
                                    updateCenterContent(centerCol, account.getType(), customer, userAccounts, allAccounts,
                                            parentStage, debitSelector, rightCol);
                                });
                                targetButtonsBox.getChildren().add(transferBtn);
                            }

                            // if other accounts have been saved
                            if (!targetButtonsBox.getChildren().isEmpty()) {
                                feedback.setText("Choose target account below:");
                                return;
                            }
                        }
                    }
                }

                // Save & Refresh
                AccountFileManager.saveAccounts(allAccounts);
                TransactionFileManager.saveTransactions(allAccounts);

                popup.close();

                List<Account> userAccounts = allAccounts.stream()
                        .filter(a -> a.getCustomerId().equals(customer.getCustomerId()))
                        .toList();

                updateCenterContent(centerCol, account.getType(), customer, userAccounts, allAccounts,
                        parentStage, debitSelector, rightCol);

            } catch (NumberFormatException ex) {
                feedback.setText("Invalid amount format.");
            } catch (Exception ex) {
                feedback.setText(ex.getMessage());
            }
        });

        cancel.setOnAction(e -> popup.close());

        // --- Layout ---
        VBox layout = new VBox(12);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white; -fx-border-color: #dcdcdc; -fx-border-radius: 8;");

        layout.getChildren().addAll(title, amountLbl, amountRow);
        if (isWithdraw && account.getType() == 'D') layout.getChildren().add(targetButtonsBox);
        layout.getChildren().addAll(feedback, new HBox(10, confirm, cancel));

        popup.setScene(new Scene(layout, 360, 280));
        popup.showAndWait();
    }

    // =============================
    // POPUP: CREDIT OPERATION (TAKE/REPAY)
    // =============================
    /**
     * Shows the credit popup for "take" or "repay" operations.
     * <ul>
     *   <li><b>Take</b> — increases negative balance in Credit and deposits into matching-currency Debit.</li>
     *   <li><b>Repay</b> — withdraws from matching-currency Debit and reduces Credit debt.</li>
     * </ul>
     *
     * @param op          "take" or "repay"
     * @param credit      selected {@link CreditAccount}
     * @param allAccounts all accounts list
     * @param parentStage owner stage
     * @param centerCol   center column to refresh
     * @param customer    current customer
     * @param rightCol    right utilities column
     */
    private static void showCreditOperationPopup(
            String op,
            CreditAccount credit,
            List<Account> allAccounts,
            Stage parentStage,
            VBox centerCol,
            Customer customer,
            VBox rightCol
    ) {
        boolean isTake = "take".equalsIgnoreCase(op);
        boolean isRepay = "repay".equalsIgnoreCase(op);

        Stage popup = new Stage(StageStyle.UTILITY);
        popup.initOwner(parentStage);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle((isTake ? "Take Credit" : "Repay Credit") + " - " + credit.getCurrency());

        // --- UI ---
        Label title = new Label(isTake ? "Take Credit" : "Repay Credit");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700;");

        Label limitLbl = new Label("Credit Limit: " + money(credit.getCreditLimit(), credit.getCurrency()));
        Label balanceLbl = new Label("Current Credit Balance: " + money(credit.getBalance(), credit.getCurrency()));

        Label amountLbl = new Label("Amount (" + credit.getCurrency() + "):");
        TextField amountField = new TextField();
        amountField.setPromptText("e.g. 1000");
        amountField.setPrefWidth(150);

        // Max Button
        Button maxBtn = new Button("MAX");
        maxBtn.setStyle("""
            -fx-background-color: #f0f0f0;
            -fx-border-color: #ccc;
            -fx-font-weight: 600;
            -fx-padding: 3 10;
            """);

        maxBtn.setOnAction(ev -> {
            double val;
            if (isTake) {
                // cât mai poate lua: abs(limit) + balance (balance e negativ)
                val = Math.abs(credit.getCreditLimit()) + credit.getBalance();
            } else {
                // cât are de rambursat: abs(balance)
                val = Math.abs(credit.getBalance());
            }
            if (val < 0) val = 0;
            amountField.setText(String.format(Locale.US, "%.2f", val));
        });

        HBox amountRow = new HBox(8, amountField, maxBtn);
        amountRow.setAlignment(Pos.CENTER_LEFT);

        Label feedback = new Label();
        feedback.setStyle("-fx-text-fill: #b33; -fx-font-size: 12px;");

        Button confirm = new Button("Confirm");
        Button cancel = new Button("Cancel");
        confirm.setStyle("-fx-background-color:" + ACCENT + "; -fx-font-weight:700;");
        cancel.setStyle("-fx-background-color:#ddd;");

        // --- LOGIC ---
        confirm.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) {
                    feedback.setText("Amount must be positive.");
                    return;
                }

                // finding the debit account with the same currency
                Account debit = allAccounts.stream()
                        .filter(a -> a.getType() == 'D'
                                && a.getCustomerId().equals(customer.getCustomerId())
                                && a.getCurrency().equalsIgnoreCase(credit.getCurrency()))
                        .findFirst()
                        .orElse(null);

                if (debit == null) {
                    feedback.setText("No Debit account found in " + credit.getCurrency());
                    return;
                }

                // ----------------------------
                // TAKE CREDIT
                // ----------------------------
                if (isTake) {
                    double maxAvailable = Math.abs(credit.getCreditLimit()) + credit.getBalance(); // ex: limit -5000, balance -2000 -> 3000 disponibil
                    if (amount > maxAvailable) {
                        feedback.setText("Amount exceeds available credit limit.");
                        return;
                    }

                    credit.withdraw(amount);
                    debit.deposit(amount);

                    debit.addTransaction("CREDIT_INCOMING", amount, "Funds received from Credit | Debit Account");
                    credit.addTransaction("CREDIT_TAKEN", -amount, "New Credit taken | Credit Account");


                    AccountFileManager.saveAccounts(allAccounts);
                    TransactionFileManager.saveTransactions(allAccounts);

                    feedback.setStyle("-fx-text-fill: green;");
                    feedback.setText("Credit successfully taken! Funds added to your Debit account.");
                }

                // ----------------------------
                // REPAY CREDIT
                // ----------------------------
                else if (isRepay) {
                    if (credit.getBalance() >= 0) {
                        feedback.setText("No active credit to repay.");
                        return;
                    }

                    double repayAmount = Math.min(amount, Math.abs(credit.getBalance()));

                    if (debit.getBalance() < repayAmount) {
                        feedback.setText("Insufficient funds in your Debit account.");
                        return;
                    }

                    if (amount > Math.abs(credit.getBalance())) {
                        feedback.setText("Amount exceeds owed credit");
                        return;
                    }

                    debit.withdraw(repayAmount);
                    credit.deposit(repayAmount);

                    debit.addTransaction("CREDIT_REPAY", -repayAmount, "Credit Repayment | Debit Account");
                    credit.addTransaction("DEPOSIT", repayAmount, "Money deposited | Credit Account");
                    AccountFileManager.saveAccounts(allAccounts);
                    TransactionFileManager.saveTransactions(allAccounts);

                    feedback.setStyle("-fx-text-fill: green;");
                    feedback.setText("Credit repayment successful!");
                }

                // Save & Refresh - UI
                popup.close();

                List<Account> userAccounts = allAccounts.stream()
                        .filter(a -> a.getCustomerId().equals(customer.getCustomerId()))
                        .collect(java.util.stream.Collectors.toList());

                updateCenterContent(centerCol, 'C', customer, userAccounts, allAccounts, parentStage, null, rightCol);

            } catch (NumberFormatException ex) {
                feedback.setText("Invalid amount format.");
            } catch (Exception ex) {
                feedback.setText(ex.getMessage());
            }
        });

        cancel.setOnAction(e -> popup.close());

        HBox buttons = new HBox(10, confirm, cancel);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(12, title, limitLbl, balanceLbl, amountLbl, amountRow, feedback, buttons);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white; -fx-border-color: #dcdcdc; -fx-border-radius: 8;");

        popup.setScene(new Scene(layout, 370, 290));
        popup.showAndWait();
    }

    // =============================
    //   POPUP: EXTERNAL TRANSFER (IBAN)
    // =============================

    /**
     * Shows the external transfer popup, validates IBAN, performs conversion if needed,
     * updates both sender and receiver accounts, and persists transactions.
     *
     * @param sender        the sender {@link Account} (Debit)
     * @param allAccounts   all accounts list
     * @param parentStage   owner stage
     * @param centerCol     center column to refresh
     * @param senderCustomer current customer (sender)
     * @param debitSelector  debit selector (to keep the selected account)
     * @param rightCol       right column (actions panel)
     */
    private static void showTransferPopup(
            Account sender,
            List<Account> allAccounts,
            Stage parentStage,
            VBox centerCol,
            Customer senderCustomer,
            ComboBox<Account> debitSelector,
            VBox rightCol
    ) {
        Stage popup = new Stage(StageStyle.UTILITY);
        popup.initOwner(parentStage);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("External Transfer");

        Label title = new Label("Send Money");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700;");

        TextField toField = new TextField();
        toField.setPromptText("Recipient name");

        TextField ibanField = new TextField();
        ibanField.setPromptText("Recipient IBAN");

        Button checkBtn = new Button("Check");
        checkBtn.setStyle("-fx-background-color:#e6e6e6; -fx-font-weight:600;");

        TextField amountField = new TextField();
        amountField.setPromptText("e.g. 100.00");

        Label currencyLbl = new Label(sender.getCurrency());
        currencyLbl.setStyle("-fx-font-weight:700;");

        // New Description field
        Label descLbl = new Label("Description:");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Optional description (e.g. Rent, groceries, etc.)");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);

        TextField fromField = new TextField(senderCustomer.getFullName());
        fromField.setPromptText("Sender");
        fromField.setPrefWidth(200);

        TextField dateField = new TextField(java.time.LocalDate.now().toString());
        dateField.setEditable(false);

        Label feedback = new Label();
        feedback.setStyle("-fx-text-fill:#b33; -fx-font-size:12px;");

        Button sendBtn = new Button("Send");
        Button cancelBtn = new Button("Cancel");
        sendBtn.setStyle("-fx-background-color:" + ACCENT + "; -fx-font-weight:700;");
        cancelBtn.setStyle("-fx-background-color:#ddd;");

        // Layout
        HBox ibanRow = new HBox(8, ibanField, checkBtn);
        ibanRow.setAlignment(Pos.CENTER_LEFT);

        HBox amountRow = new HBox(8, amountField, currencyLbl);
        amountRow.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("To:"), 0, 0);
        grid.add(toField, 1, 0);
        grid.add(new Label("IBAN:"), 0, 1);
        grid.add(ibanRow, 1, 1);
        grid.add(new Label("Amount:"), 0, 2);
        grid.add(amountRow, 1, 2);
        grid.add(descLbl, 0, 3);
        grid.add(descArea, 1, 3);
        grid.add(new Separator(), 0, 4, 2, 1);
        grid.add(new Label("From:"), 0, 5);
        grid.add(fromField, 1, 5);
        grid.add(new Label("Date:"), 0, 6);
        grid.add(dateField, 1, 6);

        // styling
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(90);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(350);
        grid.getColumnConstraints().addAll(col1, col2);

        HBox buttons = new HBox(10, sendBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(15, title, grid, feedback, buttons);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color:white; -fx-border-color:#dcdcdc; -fx-border-radius:8;");

        Scene scene = new Scene(layout, 500, 440); // ✅ puțin mai lat
        popup.setScene(scene);
        popup.show();

        final Account[] receiverRef = {null};

        // CHECK IBAN
        checkBtn.setOnAction(e -> {
            String iban = ibanField.getText().trim();
            if (iban.isEmpty()) {
                feedback.setText("Please enter an IBAN.");
                feedback.setStyle("-fx-text-fill:#b33;");
                return;
            }

            Account receiver = allAccounts.stream()
                    .filter(a -> a.getIban().equalsIgnoreCase(iban))
                    .findFirst()
                    .orElse(null);

            if (receiver == null) {
                feedback.setText("IBAN not found.");
                feedback.setStyle("-fx-text-fill:#b33;");
                toField.setText("");
                receiverRef[0] = null;
            } else {
                toField.setText(receiver.getAccountHolder());
                feedback.setText("✅ IBAN valid. Recipient found: " + receiver.getAccountHolder());
                feedback.setStyle("-fx-text-fill:green; -fx-font-weight:600;");
                receiverRef[0] = receiver;
            }
        });

        // SEND TRANSFER
        sendBtn.setOnAction(e -> {
            try {
                if (toField.getText().isBlank() ||
                        ibanField.getText().isBlank() ||
                        amountField.getText().isBlank()) {
                    feedback.setText("Please fill in all required fields.");
                    feedback.setStyle("-fx-text-fill:#b33;");
                    return;
                }

                Account receiver = receiverRef[0];
                if (receiver == null) {
                    feedback.setText("Please verify the IBAN first.");
                    feedback.setStyle("-fx-text-fill:#b33;");
                    return;
                }

                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) {
                    feedback.setText("Amount must be positive.");
                    feedback.setStyle("-fx-text-fill:#b33;");
                    return;
                }

                if (sender.getBalance() < amount) {
                    feedback.setText("Insufficient funds.");
                    feedback.setStyle("-fx-text-fill:#b33;");
                    return;
                }

                // automatic exchange rate
                double converted = ExchangeRate.convert(amount, sender.getCurrency(), receiver.getCurrency());
                sender.withdraw(amount);
                receiver.deposit(converted);

                String description = descArea.getText().trim().isEmpty() ? "No description" : descArea.getText().trim();

                sender.addTransaction("TRANSFER_SENT", -amount, "To: " + receiver.getAccountHolder() + " / " + description);
                receiver.addTransaction("TRANSFER_RECEIVED", converted, "From: " + senderCustomer.getFullName() + " / " + description);

                AccountFileManager.saveAccounts(allAccounts);
                TransactionFileManager.saveTransactions(allAccounts);

                feedback.setStyle("-fx-text-fill:green;");
                feedback.setText("Transfer successful!");

                popup.close();

                // UI upadte
                List<Account> userAccounts = allAccounts.stream()
                        .filter(a -> a.getCustomerId().equals(senderCustomer.getCustomerId()))
                        .collect(java.util.stream.Collectors.toList());

                updateCenterContent(centerCol, 'D', senderCustomer, userAccounts, allAccounts, parentStage, debitSelector, rightCol);

            } catch (Exception ex) {
                feedback.setText(ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> popup.close());
    }



    private static Account findMatchingDebit(List<Account> allAccounts, String customerId, String currency) {
        return allAccounts.stream()
                .filter(a -> a.getType() == 'D'
                        && a.getCustomerId().equals(customerId)
                        && a.getCurrency().equalsIgnoreCase(currency))
                .findFirst()
                .orElse(null);
    }

    // =============================
    //           PROFILE SECTION
    // =============================
    /**
     * Renders the Profile details card for the current customer (center column).
     *
     * @param centerCol     main center column
     * @param customer      current customer
     * @param userAccounts  customer's accounts list
     * @param allAccounts   all accounts
     * @param stage         owner stage
     * @param debitSelector debit selector (used to restore tab styles on return)
     * @param rightCol      right utilities panel (hidden for profile)
     */
    private static void showProfile(
            VBox centerCol,
            Customer customer,
            List<Account> userAccounts,
            List<Account> allAccounts,
            Stage stage,
            ComboBox<Account> debitSelector,
            VBox rightCol
    ) {
        centerCol.getChildren().clear();

        // country full name
        String countryFull = SignUpUI.getCountryFullName(customer.getCountry());

        // title + fields
        Label title = heading("Profile Details", 18);
        title.setStyle("-fx-text-fill:#111; -fx-font-weight:700;");

        Label name     = labelMuted("Name: "      + customer.getFullName(), 15);
        Label email    = labelMuted("Email: "     + customer.getEmail(), 15);
        Label phone    = labelMuted("Phone: "     + customer.getPhoneNumber(), 15);
        Label address1 = labelMuted("Address 1: " + customer.getAddressLine1(), 15);
        Label address2 = labelMuted("Address 2: " + customer.getAddressLine2(), 15);
        Label city     = labelMuted("City: "      + customer.getCity(), 15);
        Label country  = labelMuted("Country: "   + countryFull, 15);
        Label age      = labelMuted("Age: "       + customer.getAge(), 15);
        Label id       = labelMuted("Customer ID: " + customer.getCustomerId(), 15);

        VBox infoBox = new VBox(12, title, name, email, phone, address1, address2, city, country, age, id);
        infoBox.setPadding(new Insets(25));
        infoBox.setStyle("""
            -fx-background-color:white;
            -fx-border-color:#dcdcdc;
            -fx-border-radius:12;
            -fx-background-radius:12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8,0,0,2);
            """);
        infoBox.setPrefWidth(450);
        infoBox.setPrefHeight(360);

        VBox wrapper = new VBox(15, infoBox);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setPadding(new Insets(10, 0, 0, 0)); // cardul sus, aproape lipit de header

        centerCol.getChildren().add(wrapper);
    }

}
