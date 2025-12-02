package Entities;

import java.util.ArrayList;
import java.util.List;


public class RequestPath {

    private String path;      // /orders/
    private String method;    // GET, POST, PUT, DELETE
    private List<String> items;    // רשימה גנרית

    public RequestPath() {
        this.items = new ArrayList<>();
    }

    public RequestPath(String path, String method) {
        this.path = path;
        this.method = method;
        this.items = new ArrayList<>();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

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
    public static RequestPath parse(String raw) {

        RequestPath req = new RequestPath();

        raw = raw.trim();

        // path='Order'
        int pathStart = raw.indexOf("path='") + 6;
        int pathEnd = raw.indexOf("'", pathStart);
        req.setPath(raw.substring(pathStart, pathEnd));

        // method='get'
        int methodStart = raw.indexOf("method='") + 8;
        int methodEnd = raw.indexOf("'", methodStart);
        req.setMethod(raw.substring(methodStart, methodEnd));

        // items=[GET_ALL_ORDERS]
        int itemsStart = raw.indexOf("items=[") + 7;
        int itemsEnd = raw.indexOf("]", itemsStart);

        String itemsStr = raw.substring(itemsStart, itemsEnd).trim();

        if (!itemsStr.isEmpty()) {
            // במקרה של כמה פריטים: [A, B, C]
            String[] parts = itemsStr.split(",");
            for (String p : parts) {
                req.addItem(p.trim());
            }
        }

        return req;
    }

}
