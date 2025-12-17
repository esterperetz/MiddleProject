package Entities;

import java.io.Serializable;

/**
 * Actions for client-server communication requests
 */
public enum ActionType implements Serializable {
    GET_ALL,
    GET_BY_ID,
    CREATE,
    UPDATE,
    DELETE,

    // Subscriber specific
    GET_USER_ORDERS,
    REGISTER_SUBSCRIBER,

    // Reservation Logic
    CHECK_AVAILABILITY,    // Verify table availability before booking
    
    // Waiting List specific
    ENTER_WAITING_LIST,
    EXIT_WAITING_LIST,

    // Table & Billing management
    UPDATE_ORDER_STATUS,   // Change status to SEATED, etc.
    PAY_BILL               // Complete payment and close order
}