#  Stock Exchange Simulation

##  Overview
The **Stock Exchange Simulation** is a multithreaded Java project that models how a financial market operates.  
It simulates **buyers** and **sellers** trading shares of various companies through a centralized **Trading Engine**.

Each participant operates concurrently using **Java threads**, interacting via a **thread-safe order queue**.  
The system supports **manual buyer selection**, meaning buyers can actively choose the best available seller offer instead of simply accepting the first one.

The simulation maintains:
- All active buy and sell offers
- A complete transaction history
- Company price updates in real time
- Persistent transaction records in **MongoDB**

---

##  Features
 **Concurrent Trading Simulation** – Buyers and sellers run in parallel threads.  
 **Automatic Order Matching** – Engine matches compatible buy/sell offers based on price and company.  
 **Manual Buyer Selection** – Buyers can inspect the list of available seller offers and choose the best price manually.  
 **Fair Pricing** – Transactions occur at an average or agreed price between buyer and seller.  
 **Data Persistence** – Each completed transaction is automatically stored in a **MongoDB** collection.  
 **Thread-Safe Execution** – Concurrency protected by **ReentrantLock** and **BlockingQueue**.  
 **Logging and Monitoring** – Logback used for runtime logging and monitoring of trades.

---

##  Technology Stack

| Component | Description |
|------------|--------------|
| **Language** | Java 21 |
| **Build Tool** | Maven |
| **Database** | MongoDB |
| **Logging** | Logback |
| **JSON Handling** | Gson |
| **Testing** | JUnit 5 |

###  Dependencies
```xml
<!-- JUnit 5 -->
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

<!-- Logback -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.6</version>
</dependency>

<!-- Gson -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.11.0</version>
</dependency>

<!-- MongoDB Java Driver -->
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
    <version>4.10.2</version>
</dependency>
