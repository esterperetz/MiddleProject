package Entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a simple request structure sent between client and server.
 * It contains:
 *  - A path (resource name)
 *  - An HTTP-like method (GET, POST, PUT, DELETE)
 *  - A list of string items (parameters or extra data)
 */
public class RequestPath {

    private String path;      // Example: "Order"
    private String method;    // Example: "GET"
    private List<String> items; // Generic list of items

    /**
     * Creates an empty RequestPath with an empty items list.
     */
    public RequestPath() {
        this.items = new ArrayList<>();
    }

    /**
     * Creates a RequestPath with a path and method.
     */
    public RequestPath(String path, String method) {
        this.path = path;
        this.method = method;
        this.items = new ArrayList<>();
    }

    /** Returns the request path. */
    public String getPath() {
        return path;
    }

    /** Sets the request path. */
    public void setPath(String path) {
        this.path = path;
    }

    /** Returns the request method. */
    public String getMethod() {
        return method;
    }

    /** Sets the request method. */
    public void setMethod(String method) {
        this.method = method;
    }

    /** Returns the list of items. */
    public List<String> getItems() {
        return items;
    }

    /** Replaces the items list. */
    public void setItems(List<String> items) {
        this.items = items;
    }

    /** Updates an item at a specific index. */
    public void setItem(int index, String value) {
        items.set(index, value);
    }

    /** Adds a new item to the list. */
    public void addItem(String item) {
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "RequestPath{" +
                "path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", items=" + items +
                '}';
    }

    /**
     * Parses a RequestPath from a raw string created by toString().
     * Works only with the same format.
     */
    public static RequestPath parse(String raw) {

        RequestPath req = new RequestPath();
        raw = raw.trim();

        // Extract path
        int pathStart = raw.indexOf("path='") + 6;
        int pathEnd = raw.indexOf("'", pathStart);
        req.setPath(raw.substring(pathStart, pathEnd));

        // Extract method
        int methodStart = raw.indexOf("method='") + 8;
        int methodEnd = raw.indexOf("'", methodStart);
        req.setMethod(raw.substring(methodStart, methodEnd));

        // Extract items
        int itemsStart = raw.indexOf("items=[") + 7;
        int itemsEnd = raw.indexOf("]", itemsStart);
        String itemsStr = raw.substring(itemsStart, itemsEnd).trim();

        if (!itemsStr.isEmpty()) {
            // Example: "A, B, C"
            String[] parts = itemsStr.split(",");
            for (String p : parts) {
                req.addItem(p.trim());
            }
        }

        return req;
    }
}
