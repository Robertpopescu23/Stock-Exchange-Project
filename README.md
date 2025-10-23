# Stock Exchange Simulation

## Overview
The **Stock Exchange** project simulates the trading of shares belonging to various companies. Buyers and sellers interact through offers and requests, and transactions occur when prices and quantities match. The system provides full visibility of offers, requests, and transaction history, and allows participants to modify their offers or requests if no ongoing transactions exist for the relevant shares.

This project is designed in Java and can be extended into a distributed system with clients acting as buyers and sellers. Currently, buyers and sellers are simulated using Java threads and algorithms that operate on shared exchange information.

---

## Features
- Sellers can offer a number of shares at a specified price.
- Buyers can request a number of shares at a specified price.
- Transactions occur automatically when a matching offer and request exist:
    - Number of shares traded: `n = min(offer, request)`
- Full visibility of market information:
    - Active offers
    - Active requests
    - Transaction history
- Offers and requests can be modified if shares are not currently involved in a transaction.

---

## Technology Stack
The project uses **Java 21** and leverages the following dependencies from Maven:

- **JUnit 5** – for unit testing
  ```xml
  <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-api</artifactId>
      <version>5.10.2</version>
      <scope>test</scope>
  </dependency>
  <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-engine</artifactId>
      <version>5.10.2</version>
      <scope>test</scope>
  </dependency>
  ```

- **JavaFX** – for graphical user interface
```xml
  <dependency>
  <groupId>org.openjfx</groupId>
  <artifactId>javafx-controls</artifactId>
  <version>23</version>
  </dependency>
  ```

- **Logback** – for logging
```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.6</version>
</dependency>
  ```
- **Gson** – for JSON serialization and deserialization
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.11.0</version>
</dependency>
```
- **MongoDB Java Driver** – for database integration
```xml
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
    <version>4.10.2</version>
</dependency>
```
