package entities;

import java.io.Serializable;

/**
 * Enumeration defining the specific actions (commands) for client-server communication.
 * 
 * Used in the Request object to route the operation to the correct controller/handler on the server.
 */
public enum ActionType implements Serializable {

    // --- Standard CRUD & Generic Fetching ---
    /** Retrieve all records of a specific resource. */
    GET_ALL,
    /** Retrieve a specific record by its unique ID. */
    GET_BY_ID,
    /** Generic retrieval action. */
    GET,
    /** Retrieve all records associated with a specific subscriber ID. */
    GET_ALL_BY_SUBSCRIBER_ID,
    /** Retrieve a record by a unique code (e.g., confirmation code). */
    GET_BY_CODE,
    /** Create a new record in the database. */
    CREATE,
    /** Update an existing record. */
    UPDATE,
    /** Delete a record from the database. */
    DELETE,
    /** Generic action to set specific details. */
    SET_DETAILS,

    // --- Authentication ---
    /** Employee login request. */
    LOGIN,
    /** Employee logout request. */
    LOGOUT,

    // --- Employee & Management Actions ---
    /** Register a new employee in the system. */
    REGISTER_EMPLOYEE,
    /** Register a new subscriber (usually performed by a manager). */
    REGISTER_SUBSCRIBER,
    /** Generate or fetch data for the monthly management report. */
    GET_MONTHLY_REPORT,
    /** Download the actual report file (PDF/Excel) from the server. */
    DOWNLOAD_REPORT,
    /** Update the restaurant's business opening hours. */
    UPDATE_OPENING_HOURS,

    // --- Customer & Subscriber Actions ---
    /** Retrieve order history for a specific user. */
    GET_USER_ORDERS,
    /** Register a generic/regular customer (non-subscriber). */
    REGISTER_CUSTOMER,
    /** Validate a QR code at the entrance or terminal. */
    CHECK_QR_CODE,

    // --- Reservation Logic ---
    /** Check general availability for tables. */
    CHECK_AVAILABILITY,
    /** Verify specific available time slots before booking. */
    GET_AVAILABLE_TIME,

    // --- Waiting List Management ---
    /** Retrieve the entire waiting list. */
    GET_ALL_LIST,
    /** Retrieve the waiting list filtered by a specific date. */
    GET_WAITING_LIST_BY_DATE,
    /** Add a customer to the waiting list. */
    ENTER_WAITING_LIST,
    /** Remove a customer from the waiting list (e.g., gave up). */
    EXIT_WAITING_LIST,
    /** Move a customer from the waiting list to an active order/table. */
    PROMOTE_TO_ORDER,
    /** Identify a customer at the self-service terminal. */
    IDENTIFY_AT_TERMINAL,

    // --- Order Lifecycle & Billing ---
    /** Update the checkout status (customer leaving). */
    UPDATE_CHECKOUT,
    /** Update the status of an order (e.g., from APPROVED to SEATED). */
    UPDATE_ORDER_STATUS,
    /** Process payment and close the order. */
    PAY_BILL,

    // --- Notifications ---
    /** Send a general email. */
    SEND_EMAIL,
    /** Resend a reservation confirmation email/SMS. */
    RESEND_CONFIRMATION
}