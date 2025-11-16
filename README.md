SMT Banking
A Java & JavaFX Banking Management System


SMT Banking is a complete banking management application developed in Java, using Object-Oriented Programming principles, persistent data using text files, a graphical interface created with JavaFX, and in-memory data processing during runtime.


**Features:**
- Manage customers
- Manage bank accounts (Debit, Savings, Credit)
- Perform transactions (internal & external transfers)
- Load all data from text files on startup
- Store all updated data automatically on exit
- View a dashboard with charts of monthly cashflow
- Navigate through a modern JavaFX UI with dynamic components


**Technologies Used:**
- Java 17
- JavaFXOOP
- (Object-Oriented Programming)
- Collections (ArrayList, HashMap)
- Exception Handling
- File I/O – text-based persistence
- JavaFX Charts & Layouts


**JavaFX Interface:**
- Modern dashboard layout (HBox + VBox)
- Dynamic UI panels loaded using custom show...() methods
- Live chart of current month cashflow
- Left-side navigation menu + right-side content swapping


**Data Persistence:**
All data is stored in:
- customers.txt
- accounts.txt
- transactions.txt
- users.txt
On application startup → files are read into memory
On exit → data is automatically saved back to files


**JavaDoc:**
The project includes a full JavaDoc documentation covering:
- All classes
- Public methods
- Constructors
- Business logic
- Data flow


**HOW TO RUN**
1. Clone the repository
2. Open in IntelliJ IDEA
3. Make sure JavaFX SDK is configured
4. Run Main.java

**Author: **
Ilinca Rusescu
