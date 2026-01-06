package entities;

import java.io.Serializable;

/**
 * Actions for client-server communication requests
 */
public enum ActionType implements Serializable {
    GET_ALL,
    GET_BY_ID,
    GET,
    GET_ALL_BY_SUBSCRIBER_ID,
    CREATE,
    UPDATE,
    DELETE,
    LOGIN,
    LOGOUT,

    // employee
    REGISTER_EMPLOYEE,
    REGISTER_SUBSCRIBER,
    GET_MONTHLY_REPORT,

    // Subscriber specific
    GET_USER_ORDERS,

    REGISTER_CUSTOMER,

    // Reservation Logic
    CHECK_AVAILABILITY, 
    GET_AVAILABLE_TIME,// Verify table availability before booking

    // Waiting List specific
    ENTER_WAITING_LIST,
    EXIT_WAITING_LIST,
    PROMOTE_TO_ORDER,
    IDENTIFY_AT_TERMINAL,
    // Table & Billing management
    UPDATE_ORDER_STATUS, // Change status to SEATED, etc.
    PAY_BILL, // Complete payment and close order
    SEND_EMAIL,
    RESEND_CONFIRMATION
}