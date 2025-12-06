package Entities;

import java.io.Serializable;

/**
 * Enum for the actions of the object that we are sending
 */
public enum ActionType implements Serializable {
    GET_ALL,
    GET_BY_ID,
    CREATE,
    UPDATE,
    DELETE,
    GET_USER_ORDERS
}