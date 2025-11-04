# CareSync Healthcare Management System (CHMS)

CareSync Healthcare Management System (CHMS) is a lightweight desktop application designed for patient record management, appointment scheduling, and report analysis. It allows healthcare professionals to efficiently create, read, update, and delete patient information, including personal details, contact info, and medical history. CHMS focuses on simplicity, reliability, and extensibility, applying JavaFX, modular programming, and database integration. It follows the MVC (Model-View-Controller) pattern for clean architecture.

## Features

- **Patient Management**: Full CRUD operations (Create, Read, Update, Delete) for patient records with role-based access control.
- **Doctor Management**: Admin-only user registration system for doctors with role assignment and profile management.
- **Appointment Scheduling**: Comprehensive booking system with patient/doctor dropdowns, status tracking (Scheduled, Completed, Cancelled), and real-time data refresh across modules.
- **User Authentication**: Secure login/logout system with session management and role-based UI restrictions (Admin vs. Doctor views).
- **Reporting & Analytics**: Multi-tab dashboard with KPI cards, interactive charts, demographics analysis, appointment trends, doctor performance metrics, and custom report generation.
- **Intuitive UI**: JavaFX-based interface with FXML-driven layouts, TabPane navigation, TableView for data display, modal dialogs for confirmations/errors, and animated dashboard components.
- **Data Validation**: Built-in checks for required fields (name, date of birth, gender, email), input sanitization, and duplicate prevention.
- **Search & Filtering**: Dynamic table filtering by status, date range, and name search with real-time updates.
- **Persistent Storage**: MySQL database integration via JDBC with connection pooling, SQL injection prevention, and automatic data refresh.
- **Modular Design**: Java 21+ modules with proper access controls for FXML, reflection-based components, and MVC architecture for separation of concerns.
- **Error Handling**: Comprehensive exception management with user-friendly alerts, FXML loading error resolution, and controller initialization safeguards.

Future enhancements will include medical record attachments, Dashboard, UI design improvements, and beyond.

## Screenshots
| Patient Management                                                                                                                           |  Appointment Scheduling                                                                                                                             |   Reporting & Analytics                                                                                                                            | Doctor's Dashboard                                                                                                                            |
| -------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| <img width="450" height="350" alt="Patient Management" src="https://github.com/user-attachments/assets/732b0249-01b6-432a-9d5e-28817a24dc32" /> | <img width="450" height="350" alt="Appointment Scheduling" src="https://github.com/user-attachments/assets/05581768-e29e-4db0-bd41-913f4f00bd3c" /> | <img width="450" height="350" alt="Reporting & Analytics" src="https://github.com/user-attachments/assets/7fcbb41a-2d74-43a7-8906-bc87a7a2bbfc" /> | <img width="450" height="350" alt="Doctor's Dashboard" src="https://github.com/user-attachments/assets/da641406-a7f1-4442-b158-319a750e3e7c" /> |

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

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

