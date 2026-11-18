SQLite integration
===================

This project was converted from direct MySQL connections to a local SQLite database.

Database file:
    database/abdolhashem.db

SQLite JDBC driver expected by NetBeans:
    lib/sqlite-jdbc-3.50.3.0.jar

If the JAR is not present:
    1. Run setup-sqlite.bat on Windows.
    2. Open the project in NetBeans.
    3. Clean and Build.
    4. Run the project.

Default first-run login:
    Username: admin
    Password: admin123

The database is initialized automatically by:
    src/nematullah/database/Database.java

Tables:
    student
    room
    employee
    costs
    salary
    payment
    user

The project keeps its existing Swing forms and now uses one centralized SQLite connection.

Important notes
---------------
* The database path is relative ("database/abdolhashem.db"), so start the program from the
  project folder. Starting it from another folder creates a second, empty database there.
* Always do "Clean and Build" after pulling these changes (the old build/ and dist/ folders
  contained the previous MySQL build and were removed).
