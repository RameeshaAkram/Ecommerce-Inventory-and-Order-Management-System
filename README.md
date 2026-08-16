# Ecommerce Inventory and Order Management System

A Java Swing application for managing inventory, orders, customers, suppliers, payments, and shipping records using MongoDB.

## Features

- Inventory management for products and stock
- Order creation and tracking
- Customer, supplier, shipping, payment, and review management
- Dashboard with summary statistics and charts
- MongoDB-backed persistence

## Tech Stack

- Java Swing
- MongoDB Java Driver
- JFreeChart
- NetBeans project structure

## Project Structure

- `InventoryManagementSystem/InventoryManagementSystem/src/inventorymanagementsystem` – Java source files
- `Libraries/` – Java library JAR dependencies
- `erd/` – ER diagram assets
- `Python code/` – supporting Python scripts

## Prerequisites

- Java JDK 17 or later
- MongoDB running locally on port 27017, or configure `MONGO_URI`

## Run

From the project folder:

```bash
javac -cp "../Libraries/*" -d build/classes $(find src -name "*.java")
java -cp "build/classes:../Libraries/*" inventorymanagementsystem.Login
```

On Windows PowerShell:

```powershell
$cp = "..\Libraries\*"
$files = Get-ChildItem -Path .\src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -cp $cp -d .\build\classes $files
java -cp ".\build\classes;$cp" inventorymanagementsystem.Login
```

## MongoDB Configuration

The app defaults to:

- URI: `mongodb://localhost:27017`
- Database: `EcommerceInventoryManagment`

You can override these with environment variables:

```bash
export MONGO_URI="mongodb://localhost:27017"
export MONGO_DATABASE="EcommerceInventoryManagment"
```

## Notes

- Hardcoded cloud credentials were removed for security.
- The project was cleaned up to be portable and safe for GitHub publishing.
- The app still requires a working MongoDB instance to run successfully.

## License

This project is provided for academic and portfolio use.
