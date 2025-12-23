package entities;

import java.io.Serializable;

/**
 * Represents a request sent between client and server.
 * A Request always contains:
 *  - A resource type (ORDER, USER, etc.)
 *  - An action to perform (GET_ALL, GET_BY_ID, CREATE, UPDATE, DELETE)
 *  - An optional ID (used for GET_BY_ID or DELETE)
 *  - An optional payload object (for CREATE or UPDATE)
 *
 * This class is serializable so it can be sent over the network.
 */
public class Request implements Serializable {

    private final ResourceType resource; // ORDER / USER / ...
    private final ActionType action;     // GET_ALL / CREATE / ...
    private final Integer id;            // Identifier (orderId, userId, etc.)
    private final Object payload;        // The body of the request (e.g., Order, User)
    //need to change the name payload
    /**
     * Creates a new Request object.
     *
     * @param resource The resource type to access (ORDER, USER, etc.)
     * @param action   The action to perform (GET, CREATE, UPDATE...)
     * @param id       Optional ID for actions that require one
     * @param payload  Extra data sent with the request (may be null)
     */
    public Request(ResourceType resource, ActionType action,
                   Integer id, Object payload) {
        this.resource = resource;
        this.action = action;
        this.id = id;
        this.payload = payload;
    }

    /** Returns the resource type. */
    public ResourceType getResource() {
        return resource;
    }

    /** Returns the action to perform. */
    public ActionType getAction() {
        return action;
    }

    /** Returns the ID (may be null). */
    public Integer getId() {
        return id;
    }

    /** Returns the payload (may be null). */
    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Request{" +
                "resource=" + resource +
                ", action=" + action +
                ", id=" + id +
                ", payload=" + payload +
                '}';
    }
}
