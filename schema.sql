-- schema.sql
-- Flight Reservation System Database Schema
-- Includes Monthly Promotion News subscription support

-- 1) Create database
CREATE DATABASE IF NOT EXISTS flight_booking_system
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE flight_booking_system;

-- 2) USERS TABLE  
-- Single-table inheritance (Customer, FlightAgent, SystemAdmin)
CREATE TABLE IF NOT EXISTS users (
    user_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NOT NULL,
    email             VARCHAR(100) NOT NULL UNIQUE,
    role              ENUM('CUSTOMER', 'FLIGHT_AGENT', 'SYSTEM_ADMIN') NOT NULL,

    -- Customer-only fields
    first_name        VARCHAR(50),
    last_name         VARCHAR(50),
    phone             VARCHAR(30),
    address           VARCHAR(255),
    date_of_birth     DATE,
    membership_status ENUM('REGULAR', 'SILVER', 'GOLD', 'PLATINUM'),
    
    -- Monthly Promotion News subscription (for customers)
    subscribed_to_promotions BOOLEAN DEFAULT TRUE,

    -- FlightAgent-only fields
    employee_id       VARCHAR(50),
    hire_date         DATE,
    department        VARCHAR(100),

    -- SystemAdmin-only fields
    admin_level       INT
);

-- SYSTEM ADMIN PERMISSIONS TABLE (Many-to-many style)
CREATE TABLE IF NOT EXISTS system_admin_permissions (
    user_id BIGINT NOT NULL,
    permission ENUM(
        'ALL',
        'MANAGE_USERS',
        'MANAGE_FLIGHTS',
        'MANAGE_ROUTES',
        'MANAGE_AIRCRAFT',
        'VIEW_REPORTS',
        'MODIFY_SYSTEM_SETTINGS'
    ) NOT NULL,

    PRIMARY KEY (user_id, permission),

    CONSTRAINT fk_sysadmin_permissions_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- AIRPORT
CREATE TABLE IF NOT EXISTS airports (
    airport_code  VARCHAR(10) PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    city          VARCHAR(100) NOT NULL,
    country       VARCHAR(100) NOT NULL,
    timezone      VARCHAR(50)  NOT NULL
);

-- AIRLINE
CREATE TABLE IF NOT EXISTS airlines (
    airline_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(10)  NOT NULL UNIQUE,
    country     VARCHAR(100) NOT NULL
);

-- ROUTE
CREATE TABLE IF NOT EXISTS routes (
    route_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    origin_code        VARCHAR(10) NOT NULL,
    destination_code   VARCHAR(10) NOT NULL,
    distance_km        INT,
    estimated_duration_minutes INT,
    CONSTRAINT fk_routes_origin
        FOREIGN KEY (origin_code) REFERENCES airports(airport_code),
    CONSTRAINT fk_routes_destination
        FOREIGN KEY (destination_code) REFERENCES airports(airport_code)
);

-- AIRCRAFT
CREATE TABLE IF NOT EXISTS aircraft (
    aircraft_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    model              VARCHAR(100) NOT NULL,
    manufacturer       VARCHAR(100) NOT NULL,
    total_seats        INT NOT NULL,
    seat_configuration VARCHAR(255),
    status             ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE') DEFAULT 'ACTIVE'
);

-- FLIGHT
CREATE TABLE IF NOT EXISTS flights (
    flight_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    flight_number   VARCHAR(20) NOT NULL UNIQUE,
    departure_time  DATETIME    NOT NULL,
    arrival_time    DATETIME    NOT NULL,
    status          ENUM('SCHEDULED', 'DELAYED', 'CANCELLED', 'DEPARTED', 'ARRIVED') NOT NULL,
    available_seats INT         NOT NULL,
    price           DECIMAL(10,2) NOT NULL,
    aircraft_id     BIGINT      NOT NULL,
    route_id        BIGINT      NOT NULL,
    airline_id      BIGINT      NOT NULL,
    CONSTRAINT fk_flights_aircraft
        FOREIGN KEY (aircraft_id) REFERENCES aircraft(aircraft_id),
    CONSTRAINT fk_flights_route
        FOREIGN KEY (route_id) REFERENCES routes(route_id),
    CONSTRAINT fk_flights_airline
        FOREIGN KEY (airline_id) REFERENCES airlines(airline_id)
);

-- SEAT
CREATE TABLE IF NOT EXISTS seats (
    seat_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    flight_id    BIGINT      NOT NULL,
    seat_number  VARCHAR(10) NOT NULL,
    seat_class   ENUM('ECONOMY', 'BUSINESS', 'FIRST') NOT NULL,
    is_available BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_seats_flight
        FOREIGN KEY (flight_id) REFERENCES flights(flight_id),
    CONSTRAINT uq_seats_flight_seatnumber
        UNIQUE (flight_id, seat_number)
);

-- PASSENGER
CREATE TABLE IF NOT EXISTS passengers (
    passenger_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name       VARCHAR(50) NOT NULL,
    last_name        VARCHAR(50) NOT NULL,
    date_of_birth    DATE       NOT NULL,
    passport_number  VARCHAR(50),
    nationality      VARCHAR(50)
);

-- PAYMENT
CREATE TABLE IF NOT EXISTS payments (
    payment_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount           DECIMAL(10,2) NOT NULL,
    payment_date     DATETIME      NOT NULL,
    payment_method   ENUM('CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'BANK_TRANSFER') NOT NULL,
    transaction_id   VARCHAR(100),
    status           ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') NOT NULL
);

-- RESERVATION
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_date   DATETIME NOT NULL,
    status         ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') NOT NULL,
    total_price    DECIMAL(10,2) NOT NULL,
    customer_id    BIGINT   NOT NULL,
    flight_id      BIGINT   NOT NULL,
    payment_id     BIGINT,
    CONSTRAINT fk_reservations_customer
        FOREIGN KEY (customer_id) REFERENCES users(user_id),
    CONSTRAINT fk_reservations_flight
        FOREIGN KEY (flight_id) REFERENCES flights(flight_id),
    CONSTRAINT fk_reservations_payment
        FOREIGN KEY (payment_id) REFERENCES payments(payment_id)
);

-- TICKET
CREATE TABLE IF NOT EXISTS tickets (
    ticket_number   BIGINT AUTO_INCREMENT PRIMARY KEY,
    issue_date      DATETIME NOT NULL,
    passenger_name  VARCHAR(100) NOT NULL,
    reservation_id  BIGINT   NOT NULL,
    seat_id         BIGINT   NOT NULL,
    barcode         VARCHAR(100),
    CONSTRAINT fk_tickets_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id),
    CONSTRAINT fk_tickets_seat
        FOREIGN KEY (seat_id) REFERENCES seats(seat_id)
);

-- PROMOTION
-- Stores promotional offers for monthly promotion news
CREATE TABLE IF NOT EXISTS promotions (
    promotion_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(100) NOT NULL,
    description      VARCHAR(255),
    discount_percent DECIMAL(5,2) NOT NULL,
    valid_from       DATE NOT NULL,
    valid_to         DATE NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active        BOOLEAN DEFAULT TRUE
);

-- PROMOTION_ROUTES (Optional: Link promotions to specific routes)
CREATE TABLE IF NOT EXISTS promotion_routes (
    promotion_id BIGINT NOT NULL,
    route_id     BIGINT NOT NULL,
    PRIMARY KEY (promotion_id, route_id),
    CONSTRAINT fk_promo_routes_promotion
        FOREIGN KEY (promotion_id) REFERENCES promotions(promotion_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_promo_routes_route
        FOREIGN KEY (route_id) REFERENCES routes(route_id)
        ON DELETE CASCADE
);

-- PROMOTION_NEWS_LOG (Track when monthly news was sent)
CREATE TABLE IF NOT EXISTS promotion_news_log (
    log_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    sent_date     DATE NOT NULL,
    customer_id   BIGINT NOT NULL,
    promotion_ids VARCHAR(255),
    status        ENUM('SENT', 'FAILED') NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_news_log_customer
        FOREIGN KEY (customer_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- Create indexes for faster queries
CREATE INDEX idx_promotions_valid_dates ON promotions(valid_from, valid_to);
CREATE INDEX idx_users_role_subscribed ON users(role, subscribed_to_promotions);
CREATE INDEX idx_news_log_date ON promotion_news_log(sent_date);
