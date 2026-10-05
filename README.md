📚 Library Management System

Desktop application for managing a library, built with Java 21, JavaFX, Maven, Hibernate and MySQL.

Application type: JavaFX Desktop Application
Architecture: Layered Architecture
UI: Custom JavaFX CSS — Atelier style
Main class: com.library.management.Main

📑 Table of Contents

1. Overview

2. Features

3. Technologies

4. Architecture

5. Project Structure

6. Requirements

7. Database Configuration

8. Run the Application

9. Build the Project

10. Package as Windows Application

11. Authentication and Authorization

12. Borrowing and Returning

13. Fine Calculation

14. UI Design

15. Error Handling

16. Development Workflow

17. Troubleshooting

18. Future Improvements

19. License

1. Overview

Library Management System is a desktop application developed to support daily library management operations.

The system provides functionality for:

User authentication

Role-based authorization

User management

Book management

Author management

Category management

Library card management

Borrowing management

Book returns

Fine calculation

Receipt management

Revenue statistics

Search and validation

Windows desktop packaging

The application is a JavaFX desktop application and does not require a web browser.

2. Features

🔐 Authentication

Login with username and password

BCrypt password verification

Required-field validation

Invalid-login validation

Logout

Navigation between Login and Dashboard

👥 User Management

Administrators can:

Add users

Edit users

Delete users

Search users

Activate/deactivate users

Assign roles

Validate usernames

Validate email addresses

Prevent duplicate usernames

Prevent duplicate email addresses

The Reader role is not exposed as an option in the administrator user form.

📚 Book Management

Book management includes:

Add books

Edit books

Delete books

Search books

Manage ISBN

Manage price

Manage authors

Manage categories

Manage book copies

Track book availability/status

Validate duplicate ISBN

✍️ Author Management

Add author

Edit author

Delete/deactivate author

Search author

Manage book-author relationships

🗂️ Category Management

Add category

Edit category

Delete category

Search category

Confirmation before deletion

Validation before deleting data

💳 Library Card Management

Create library cards

Manage card information

Track card status

Search library cards

Manage library-card related revenue

📖 Borrowing Management

Create borrowing records

Select books/copies

Track borrowing dates

Track due dates

Manage deposits

View borrowing details

Search borrowing records

Print borrowing information when permitted

Track borrowing status

Supported statuses:

BORROWING
PARTIALLY_RETURNED
OVERDUE
RETURNED

🔄 Returning Books

When books are returned, staff can select the condition of each returned copy.

Available conditions:

Condition

Meaning

GOOD

Returned normally

DAMAGED

Book is damaged

LOST

Book is lost

The system calculates applicable fines from book condition and overdue days.

💰 Fine Management

The system supports:

Damage fines

Lost-book fines

Late-return fines

Total fine calculation

Deposit comparison

Refund calculation

Additional payment calculation where applicable

Fine/revenue statistics

🧾 Receipt Management

Create receipt information

Manage receipt codes

Validate duplicate receipt codes

Display business errors using custom Atelier dialogs

📊 Dashboard & Revenue

Dashboard statistics include:

Today's revenue

Monthly revenue

Fine revenue

Renewal revenue

Library-card revenue

Total revenue

Library statistics

3. Technologies

Technology

Purpose

Java 21

Main programming language

JavaFX

Desktop GUI

Maven

Build and dependency management

Hibernate ORM

Persistence / ORM

MySQL

Relational database

jBCrypt

Password hashing

Jakarta Validation

Data validation

Lombok

Reduce boilerplate

Ikonli

JavaFX icons

FontAwesome 5

Icon set

Git / GitHub

Version control

jpackage

Windows application packaging

Development environment:

Windows 11

IntelliJ IDEA

PowerShell

Maven Wrapper

4. Architecture

The application follows a layered architecture:

┌──────────────────────────────┐
│            VIEW              │
│          JavaFX UI           │
└──────────────┬───────────────┘
│
▼
┌──────────────────────────────┐
│          SERVICE             │
│        Business Logic        │
└──────────────┬───────────────┘
│
▼
┌──────────────────────────────┐
│         REPOSITORY           │
│       Database Access        │
└──────────────┬───────────────┘
│
▼
┌──────────────────────────────┐
│          HIBERNATE           │
│             ORM              │
└──────────────┬───────────────┘
│
▼
┌──────────────────────────────┐
│            MYSQL             │
└──────────────────────────────┘

View Layer

Responsible for:

JavaFX screens

Forms

Tables

Dialogs

Navigation

User interaction

Service Layer

Responsible for:

Business rules

Validation

Authentication

Borrowing

Returning

Fine calculation

Revenue calculation

Repository Layer

Responsible for:

CRUD operations

Database queries

Hibernate sessions

Persistence

Entity Layer

Represents database entities such as:

User

Role

Book

Author

Category

Borrowing

Library Card

Receipt

Configuration Layer

Contains:

Hibernate configuration

Database configuration

Persistence initialization

5. Project Structure

library-management/
│
├── .idea/
│
├── library-management-backend/
│   │
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   │       ├── css/
│   │   │       │   ├── login.css
│   │   │       │   └── dashboard.css
│   │   │       ├── images/
│   │   │       └── hibernate.properties
│   │   │
│   │   └── test/
│   │
│   ├── pom.xml
│   └── mvnw.cmd
│
├── LibraryManagement.ico
├── start-app.bat
├── start-app.vbs
├── README.md
└── dist/

Main Java package:

com.library.management

Main entry point:

com.library.management.Main

6. Requirements

Install the following:

Windows 10/11

JDK 21

MySQL Server

Git

IntelliJ IDEA or another Java IDE

Maven does not need to be installed globally because the project uses Maven Wrapper.

Check Java:

java -version

Check compiler:

javac -version

Check Maven Wrapper:

.\library-management-backend\mvnw.cmd -version

The application is developed with Java 21.

7. Database Configuration

The application uses MySQL through Hibernate.

Database configuration is located at:

library-management-backend/src/main/resources/hibernate.properties

Example:

hibernate.connection.driver_class=com.mysql.cj.jdbc.Driver
hibernate.connection.url=jdbc:mysql://localhost:3306/library_management
hibernate.connection.username=YOUR_USERNAME
hibernate.connection.password=YOUR_PASSWORD

Replace these values with your local MySQL configuration.

Security: Never commit real database passwords, API keys or other secrets to GitHub.

8. Run the Application

Option 1 — Project launcher

From the project root:

.\start-app.bat

The launcher starts:

com.library.management.Main

Option 2 — Maven

.\library-management-backend\mvnw.cmd `
    -f .\library-management-backend\pom.xml `
org.codehaus.mojo:exec-maven-plugin:3.5.0:java `
    -Dexec.mainClass=com.library.management.Main `
-Dexec.classpathScope=runtime

Option 3 — IntelliJ IDEA

Open the project as a Maven project and run:

com.library.management.Main

9. Build the Project

Compile

.\library-management-backend\mvnw.cmd `
    -f .\library-management-backend\pom.xml `
clean compile

Package

.\library-management-backend\mvnw.cmd `
    -f .\library-management-backend\pom.xml `
clean package

Generated JAR:

library-management-backend/target/library-management-1.0.0.jar

10. Package as Windows Application

The application can be packaged with Java jpackage.

Step 1 — Build

.\library-management-backend\mvnw.cmd `
    -f .\library-management-backend\pom.xml `
clean package

Step 2 — Remove previous package

Remove-Item .\dist -Recurse -Force

Step 3 — Create Windows application image

& "C:\Program Files\OpenLogic\jdk-21.0.11.10-hotspot\bin\jpackage.exe" `
    --type app-image `
--name "LibraryManagement" `
    --input ".\library-management-backend\target" `
--main-jar "library-management-1.0.0.jar" `
    --main-class "com.library.management.Main" `
--icon ".\LibraryManagement.ico" `
--dest ".\dist"

Output:

dist/
└── LibraryManagement/
├── LibraryManagement.exe
├── app/
└── runtime/

Run:

.\dist\LibraryManagement\LibraryManagement.exe

Development vs Release

For development:

start-app.bat

For the packaged application:

dist/LibraryManagement/LibraryManagement.exe

The packaged application remains a JavaFX desktop application.

11. Authentication and Authorization

Authentication flow:

┌─────────────┐
│    Login    │
└──────┬──────┘
│
▼
Validate username/password
│
▼
Authenticate user
│
▼
Check role/permissions
│
▼
Open Dashboard

Passwords are verified using BCrypt.

Password flow:

Password
│
▼
BCrypt
│
▼
Password Hash
│
▼
Database

The application does not need to store passwords as plain text.

12. Borrowing and Returning

Borrowing statuses

BORROWING
PARTIALLY_RETURNED
OVERDUE
RETURNED

Borrowing

The system tracks:

Borrowing record

Selected book/copy

Borrowing date

Due date

Deposit

Current status

Returning

For each returned copy, staff can select:

GOOD
DAMAGED
LOST

Printing

Borrowing information can be printed while the borrowing record is active.

For:

RETURNED

the print function is unavailable.

13. Fine Calculation

Condition fine

Condition

Fine

GOOD

0 VND

DAMAGED

50,000 VND / copy

LOST

Book price

Late-return fine

5,000 VND / overdue day

Total fine

Total Fine
=
Condition Fine
+
Late Fine

Refund

The deposit is compared with the calculated fine:

Refund
=
max(0, Deposit - Total Fine)

If the total fine exceeds the deposit, the remaining amount can be treated as an additional amount due.

14. UI Design

The application uses a custom Atelier design language.

Design direction

Classic Library
+
Vintage
+
Premium Desktop UI

Color palette

The interface is based on:

Mahogany / dark brown

Parchment cream

Antique gold

Warm beige

Deep brown

Typography

Primary fonts include:

Georgia

Palatino

Segoe UI

UI characteristics

Rounded panels

Subtle shadows

Consistent spacing

Custom JavaFX CSS

Styled tables

Custom dialogs

Consistent buttons

Desktop-oriented layouts

The UI is written directly using JavaFX and CSS.

Scene Builder is not required.

15. Error Handling

Business errors use custom Atelier-themed dialogs instead of the default JavaFX appearance.

Examples:

Invalid username/password

Empty required fields

Duplicate username

Duplicate email

Duplicate ISBN

Duplicate receipt code

Invalid business operation

Other validation errors

This keeps error messages consistent with the application's visual design.

16. Development Workflow

Recommended workflow:

1. Modify code

Java source:

library-management-backend/src/main/java/

Resources:

library-management-backend/src/main/resources/

2. Compile

.\library-management-backend\mvnw.cmd `
    -f .\library-management-backend\pom.xml `
clean compile

3. Run

.\start-app.bat

4. Test

Check:

UI

Validation

Database changes

Business logic

Error handling

Navigation

5. Package

When the application is ready:

.\library-management-backend\mvnw.cmd `
    -f .\library-management-backend\pom.xml `
clean package

Then regenerate the packaged Windows application using jpackage.

17. Troubleshooting

Java is not recognized

Run:

java -version

Then:

where java

Verify that JAVA_HOME points to JDK 21.

Example:

C:\Program Files\OpenLogic\jdk-21.0.11.10-hotspot

Maven is not recognized

Use Maven Wrapper:

.\library-management-backend\mvnw.cmd -version

Application does not start

Compile first:

.\library-management-backend\mvnw.cmd `
    -f .\library-management-backend\pom.xml `
clean compile

Then:

.\start-app.bat

Check the terminal output for the actual exception.

JAR not found when using jpackage

Check:

Get-ChildItem .\library-management-backend\target\*.jar

Current JAR:

library-management-1.0.0.jar

The --main-jar argument must exactly match the generated JAR name.

Icon does not change

There are two important icon locations.

JavaFX window icon

The JavaFX application loads its icon from:

src/main/resources/images/

Example:

LibraryManagement.png

Windows packaged application icon

jpackage uses:

LibraryManagement.ico

with:

--icon ".\LibraryManagement.ico"

After changing the icon, rebuild the package:

Remove-Item .\dist -Recurse -Force

Then run jpackage again.

Windows may cache icons. Restarting Windows Explorer or Windows may be necessary if an old icon remains visible.

18. Future Improvements

Possible future improvements:

PDF reports

Excel export

More advanced statistics

Automatic database backup

Fine-payment history

Notification system

Advanced book filtering

Barcode / QR-code support

Book cover management

Import/export data

More granular permissions

Audit logs

Automated unit/integration testing

Windows installer (.msi)

Application update mechanism

19. License

This project is developed for educational and project purposes.

Third-party libraries and assets remain subject to their respective licenses.

👨‍💻 Project Information

Library Management System

Built with:

Java 21
JavaFX
Maven
Hibernate
MySQL

Main class:

com.library.management.Main

Packaged application:

LibraryManagement.exe

Project type:

JavaFX Desktop Application