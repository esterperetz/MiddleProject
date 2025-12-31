package entities;

import java.io.Serializable;

/**
 * Enum for the resources of the object that we are sending
 */
public enum ResourceType implements Serializable {
    ORDER,
    CUSTOMER,
    MANAGERTEAM,
    WAITING_LIST,
    TABLE,
    EMPLOYEE,
    BUSINESS_HOUR
}