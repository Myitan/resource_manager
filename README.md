# Resource Manager 📚

A little desktop app I built to manage a fictional library.

This was a fun weekend project to get some hands-on experience with Kotlin, Jetpack Compose for Desktop, and connecting directly to a MySQL database. The idea was to build a complete, self-contained management tool for a small resource library—think books, clients, and rentals.

It's a classic CRUD (Create, Read, Update, Delete) application with all the essential features for running a small library:

-   **Book Management:** You can add new books, edit their details, search the catalog by title or author, and see which ones are currently available or checked out.
-   **Client Registry:** A simple system to add and manage library clients, including their contact info.
-   **Rental System:** A core feature to handle checking books out to clients and processing their returns. The app automatically updates a book's availability.
-   **Rental History:** You can select any client and see a full history of all the books they've ever rented.
-   **Role-Based Access:** I implemented a basic login system with two roles: `admin` (can do everything, including deleting books) and `pracownik` (standard user).
-   **Data Export:** I added a feature to export the entire book catalog to a **CSV** or **PDF** file for record-keeping.

## Tech

-   **Language:** **Kotlin**
-   **UI:** **Jetpack Compose for Desktop** for a clean, modern interface.
-   **Database:** Direct connection to a **MySQL** database using **JDBC**.
-   **PDF/CSV Libraries:** Used **iTextPDF** for PDF generation and **OpenCSV** for the CSV export.

## Wanna Run It Yourself?

This isn't meant for a real library, but if you want to see how it works or poke at the code, here's the setup:

#### What you'll need:
-   JDK 11+
-   IntelliJ IDEA
-   A local MySQL server running.

#### The Steps:

1.  **Clone the code:**
    ```sh
    git clone [https://github.com/your-username/resource_manager.git](https://github.com/your-username/resource_manager.git)
    ```

2.  **Set up the Database:**
    * Make sure your MySQL server is running.
    * Create a new database named `library_db`.
    * You'll need to create the `books`, `clients`, `rentals`, and `users` tables. You can find the required schema in a `schema.sql` file if I ever add one (or just deduce it from the repository classes 😉).
    * The app connects with the user `root` and no password by default. If your setup is different, change the credentials in `service/DatabaseManager.kt`.

3.  **Run It:**
    Open the project in IntelliJ, let Gradle do its thing, and then run the `./gradlew run` command in the terminal.
