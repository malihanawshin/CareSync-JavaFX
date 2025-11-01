# CareSync: Healthcare Management System (HMS)

The Healthcare Management System (HMS) is a lightweight desktop application designed for basic patient record management. It allows healthcare professionals to efficiently create, read, update, and delete patient information, including personal details, contact info, and medical history. This project serves as an educational example of building a modern Java desktop app using JavaFX for the UI and JDBC for database connectivity with MySQL.
Built for small-scale use, HMS focuses on simplicity, reliability, and extensibility, making it ideal for learning JavaFX, modular programming, and database integration. It follows the MVC (Model-View-Controller) pattern for clean architecture.

## Features

- **Patient Management**: Full CRUD operations (Create, Read, Update, Delete) for patient records.
- **Intuitive UI**: JavaFX-based interface with form inputs, TableView for data display, and modal dialogs for confirmations/errors.
- **Data Validation**: Built-in checks for required fields (e.g., name, date of birth, gender).
- **Search & Filtering**: Easily view and manage patient lists with dynamic table updates.
- **Persistent Storage**: MySQL database integration via JDBC for secure, relational data handling.
- **Modular Design**: Java 21+ modules with proper access controls for FXML and reflection-based components.
- **Responsive Layout**: Clean, tab-free dashboard with HBox/VBox layouts for optimal desktop viewing.

Future enhancements could include appointment scheduling, user authentication, reporting, and medical record attachments.

## Technologies Used

- **Java SE 25**: Core language and module system.
- **JavaFX 21**: UI framework for desktop applications (Scene Builder for FXML design).
- **JDBC**: Database connectivity with MySQL Connector/J.
- **MySQL**: Relational database for patient data storage.
- **IntelliJ IDEA 2025**: IDE for development (Community or Ultimate Edition).
- **Maven**: Build tool for dependency management.

## Prerequisites

- Java Development Kit (JDK) 25 (Oracle JDK or OpenJDK).
- IntelliJ IDEA (with JavaFX plugin enabled).
- MySQL Server 8.0+ (with MySQL Workbench for management).
- Maven 3.6+ (integrated in IntelliJ).

## Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/HealthcareApp.git
cd HealthcareApp
```

### 2. Set Up the Database
1. Install and start MySQL Server.
2. Open MySQL Workbench or use the command line to create the database:
3. Update the database credentials in `src/main/java/com/example/healthcareapp/dao/DatabaseConnection.java`.

### 3. Import into IntelliJ
1. Open IntelliJ IDEA and select "Open" or "Import Project".
2. Choose the project root folder and select Maven as the build system.
3. Add dependencies:
   - In `pom.xml`, ensure MySQL Connector/J is included:
     ```xml
     <dependency>
         <groupId>mysql</groupId>
         <artifactId>mysql-connector-java</artifactId>
         <version>8.0.33</version>
     </dependency>
     ```
   - Download JavaFX SDK from [OpenJFX](https://openjfx.io/) and add it to Project Structure > Libraries.

### 4. Configure Module Path
- In Run Configurations (Run > Edit Configurations), add VM options:
  ```
  --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics,javafx.media,javafx.web
  ```
- Update `module-info.java` to open packages as needed (controller and model to javafx.fxml/base).

### 5. Build and Run
- Run `mvn clean compile` in terminal, or use Build > Rebuild Project in IntelliJ.
- Set the main class to `com.example.healthcareapp.HealthcareApplication`.
- Click Run to launch the application.

## Usage
1. **Launch the App**: The main window opens with a form for entering patient details and a table for viewing records.
2. **Add a Patient**: Fill in required fields (First Name, Last Name, Date of Birth, Gender) and click "Add Patient".
3. **View/Edit**: Select a row in the table to load details into the form, then modify and click "Update Patient".
4. **Delete**: Select a patient and click "Delete Patient" (with confirmation dialog).
5. **Clear Form**: Click "Clear Form" to reset inputs.

Sample data entry:
- First Name: John
- Last Name: Doe
- Date of Birth: 1980-05-15
- Gender: Male
- Phone: +1-555-0123
- Email: john.doe@example.com
- Address: 123 Main St, City, State 12345
- Medical History: Allergies: Peanuts; Previous conditions: Hypertension

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgements

- Inspired by common hospital management systems and JavaFX tutorials.
- Thanks to the OpenJFX team for the excellent UI framework.
- Built with guidance from JetBrains IntelliJ IDEA documentation.

