Money Transfer Application

Overview

The Money Transfer Application is a Spring Boot-based API designed to facilitate secure and efficient money transfers between accounts. It supports transaction processing, account validation, fee calculations, and transaction summaries with robust error handling and logging.

Features

Transfer Funds: Users can transfer money between accounts with automatic fee calculation.

Transaction History: Retrieve a paginated list of transactions based on filters such as account number, status, and date range.

Transaction Summary: Get a summary of transactions for a specified date.

Commission Calculation: Automatic commission calculation for eligible transactions.

Error Handling: Custom exceptions for insufficient funds, account validation failures, and invalid transactions.

Logging & Auditing: Logs all transaction activities for traceability and debugging.

Technologies Used

Java 17

Spring Boot 3 (Spring Web, Spring Data JPA, Spring Validation)

Hibernate (ORM for database interactions)

Jakarta Validation (Input validation)

Lombok (Simplifies boilerplate code)

H2 database support

SLF4J & Logback (Logging framework)

Getting Started

Prerequisites

Ensure you have the following installed:

Java 17 or later

Maven 3.6+

PostgreSQL (or use the default H2 database)

Installation

Clone the repository:

git clone (https://github.com/gisungkefas/IntraBankTransferApp/tree/master)
cd money-transfer-app

Configure the database in application.properties:

spring.datasource.url=jdbc:postgresql://localhost:5432/money_transfer_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

Build and run the application:

mvn clean install
mvn spring-boot:run

The application will start on http://localhost:8080

API Endpoints

1. Transfer Funds

Endpoint: POST /api/transactions/transfer

Request Body:

{
  "sourceAccountNumber": "123456789",
  "destinationAccountNumber": "987654321",
  "amount": 100.50,
  "description": "Payment for services"
}

Response:

{
  "transactionId": "abc123",
  "status": "SUCCESSFUL",
  "billedAmount": 102.00,
  "transactionFee": 1.50
}

2. Get Transactions

Endpoint: GET /api/transactions

Query Parameters:

status (optional)

accountNumber (optional)

startDate, endDate (optional, format: YYYY-MM-DD)

page, size (pagination)

3. Get Transaction Summary

Endpoint: GET /api/transactions/summary

Query Parameter: date (format: YYYY-MM-DD)

Response:

{
  "totalTransactions": 50,
  "successfulTransactions": 45,
  "failedTransactions": 5,
  "totalAmount": 50000.00
}

Error Handling

The API returns appropriate HTTP status codes and messages:

400 Bad Request: Validation errors

404 Not Found: Account or transaction not found

500 Internal Server Error: Unexpected errors

License

This project is licensed under the MIT License - see the LICENSE file for details.

Author

Developed by Kefas Gisung.
