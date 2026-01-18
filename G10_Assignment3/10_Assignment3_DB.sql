-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: bistro
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `customer_id` int NOT NULL AUTO_INCREMENT,
  `subscriber_code` int DEFAULT NULL,
  `customer_name` varchar(100) NOT NULL,
  `phone_number` varchar(255) NOT NULL,
  `email` varchar(40) DEFAULT NULL,
  `customer_type` enum('REGULAR','SUBSCRIBER') NOT NULL,
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `subscriber_code` (`subscriber_code`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (14,60020,'avigdor','0508342703','yohad50050@gmail.com','SUBSCRIBER'),(20,86149,'ido','1312312','peretz398@gmail.com','SUBSCRIBER'),(27,NULL,'bidi','0509342703','ii@gmail.com','REGULAR'),(28,NULL,'shay','0506596603','shayG@gmail.com','REGULAR'),(29,NULL,'bidiop','0509342703','ido.rotner2@gmail.com','REGULAR'),(30,NULL,'fdsfs','0549419635','@@','REGULAR');
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employees`
--

DROP TABLE IF EXISTS `employees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employees` (
  `employee_id` int NOT NULL AUTO_INCREMENT,
  `user_name` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `role` enum('MANAGER','REPRESENTATIVE') NOT NULL,
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `user_name` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employees`
--

LOCK TABLES `employees` WRITE;
/*!40000 ALTER TABLE `employees` DISABLE KEYS */;
INSERT INTO `employees` VALUES (1,'liel','123456','0556667605','lieletinger@gmail.com','MANAGER'),(2,'eti','1234','0556668511','eti@gmail.com','REPRESENTATIVE');
/*!40000 ALTER TABLE `employees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opening_hours`
--

DROP TABLE IF EXISTS `opening_hours`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `opening_hours` (
  `id` int NOT NULL AUTO_INCREMENT,
  `day_of_week` int DEFAULT NULL,
  `special_date` date DEFAULT NULL,
  `open_time` time DEFAULT NULL,
  `close_time` time DEFAULT NULL,
  `is_closed` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opening_hours`
--

LOCK TABLES `opening_hours` WRITE;
/*!40000 ALTER TABLE `opening_hours` DISABLE KEYS */;
INSERT INTO `opening_hours` VALUES (1,1,'2026-01-12','17:00:00','23:59:59',0),(2,1,'2026-01-13','12:00:00','17:00:00',0),(3,1,NULL,'08:00:00','13:00:00',0),(4,2,NULL,'17:00:00','00:00:00',0),(5,3,NULL,'17:00:00','00:00:00',0),(6,4,NULL,'17:00:00','00:00:00',0),(7,5,NULL,'17:00:00','00:00:00',0),(8,6,NULL,'17:00:00','00:00:00',0),(9,7,NULL,NULL,NULL,1);
/*!40000 ALTER TABLE `opening_hours` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order` (
  `order_number` int NOT NULL AUTO_INCREMENT,
  `order_date` datetime NOT NULL,
  `number_of_guests` int NOT NULL,
  `confirmation_code` int NOT NULL,
  `customer_id` int DEFAULT NULL,
  `table_number` int DEFAULT NULL,
  `date_of_placing_order` datetime NOT NULL,
  `arrival_time` datetime DEFAULT NULL,
  `leaving_time` datetime DEFAULT NULL,
  `total_price` decimal(10,2) DEFAULT NULL,
  `order_status` enum('APPROVED','SEATED','PAID','CANCELLED','PENDING') DEFAULT NULL,
  `reminder_sent` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`order_number`),
  KEY `fk_order_customer` (`customer_id`),
  KEY `fk_order_table` (`table_number`),
  CONSTRAINT `fk_order_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_order_table` FOREIGN KEY (`table_number`) REFERENCES `tables` (`table_number`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` VALUES (16,'2025-12-09 17:00:00',3,9578,28,NULL,'2025-12-01 17:16:00','2025-12-02 17:16:00','2025-12-02 17:16:00',0.00,'CANCELLED',0),(17,'2025-12-02 17:00:00',3,9579,20,2,'2025-12-01 17:16:00','2025-12-02 17:01:00','2025-12-02 17:30:00',49.50,'PAID',0),(18,'2025-12-02 17:00:00',3,9571,27,2,'2025-12-02 16:00:00','2025-12-02 17:30:00','2025-12-02 18:35:00',4000.50,'PAID',0),(19,'2025-12-20 17:00:00',2,9572,14,NULL,'2025-12-01 17:50:00',NULL,NULL,0.00,'CANCELLED',0),(20,'2025-12-28 17:00:00',4,9573,14,2,'2025-12-27 17:10:00','2025-12-28 17:01:00','2025-12-28 17:30:00',47.25,'PAID',0),(21,'2025-12-02 17:00:00',2,9574,14,1,'2025-12-01 17:10:00','2025-12-02 17:10:00','2025-12-02 18:45:00',300.00,'PAID',0),(22,'2025-12-28 17:00:00',3,9575,20,2,'2025-12-28 16:00:00','2025-12-28 17:30:00','2025-12-02 18:55:00',590.25,'PAID',0),(23,'2026-01-18 17:00:00',2,1300,29,NULL,'2026-01-18 01:33:34',NULL,NULL,0.00,'APPROVED',0),(24,'2026-01-18 17:00:00',2,4297,30,NULL,'2026-01-18 01:33:33',NULL,NULL,0.00,'APPROVED',0);
/*!40000 ALTER TABLE `order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tables`
--

DROP TABLE IF EXISTS `tables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tables` (
  `table_number` int NOT NULL,
  `number_of_seats` int NOT NULL,
  `is_occupied` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`table_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tables`
--

LOCK TABLES `tables` WRITE;
/*!40000 ALTER TABLE `tables` DISABLE KEYS */;
INSERT INTO `tables` VALUES (1,2,0),(2,4,0);
/*!40000 ALTER TABLE `tables` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `waiting_list`
--

DROP TABLE IF EXISTS `waiting_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `waiting_list` (
  `waiting_id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int DEFAULT NULL,
  `number_of_guests` int NOT NULL,
  `enter_time` datetime NOT NULL,
  `confirmation_code` int NOT NULL,
  `in_waiting_list` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`waiting_id`),
  KEY `fk_waiting_customer` (`customer_id`),
  CONSTRAINT `fk_waiting_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waiting_list`
--

LOCK TABLES `waiting_list` WRITE;
/*!40000 ALTER TABLE `waiting_list` DISABLE KEYS */;
INSERT INTO `waiting_list` VALUES (1,27,3,'2025-12-02 16:00:00',9571,0),(2,20,3,'2025-12-28 16:00:00',9575,0);
/*!40000 ALTER TABLE `waiting_list` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-18  1:43:47
