package Entities;

import java.io.Serializable;

public class Request implements Serializable {

    private final ResourceType resource; // ORDER / USER / ...
    private final ActionType action;     // GET_ALL / CREATE / ...
    private final Integer id;            // מזהה (orderId / userId ...)
    private final Object payload;        // הגוף – למשל Order, User וכו'

    public Request(ResourceType resource, ActionType action,
                   Integer id, Object payload) {
        this.resource = resource;
        this.action = action;
        this.id = id;
        this.payload = payload;
    }

    public ResourceType getResource() {
        return resource;
    }

    public ActionType getAction() {
        return action;
    }

    public Integer getId() {
        return id;
    }

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
