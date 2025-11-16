import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.util.List;
import java.util.ArrayList;

public class Main extends Application {

    public static List<Customer> customers = new ArrayList<>();
    public static List<User> users = new ArrayList<>();
    public static List<Account> accounts = new ArrayList<>();


    @Override
    public void start(Stage stage) {

        customers = CustomerFileManager.loadCustomers();
        users = UserFileManager.loadUsers();
        accounts = AccountFileManager.loadAccounts(customers);
        TransactionFileManager.loadTransactions(accounts);

        for (User u : users) {
            for (Customer c : customers) {
                if (u.getCustomerId().equals(c.getCustomerId())) {
                    u.associateCustomer(c);
                    break;
                }
            }
        }


        Scene loginScene = LoginUI.createLoginScene(stage, customers, users);
        stage.setScene(loginScene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop(){

        CustomerFileManager.saveCustomers(customers);
        UserFileManager.saveUsers(users);
        AccountFileManager.saveAccounts(accounts);
        TransactionFileManager.saveTransactions(accounts);

        System.out.println("[INFO] Data saved. Goodbye!");
    }

    public static void main(String[] args) {
        launch();
    }
}
