package Entities;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents an Order in the system.
 * An order contains:
 *  - Order number
 *  - Order date
 *  - Number of guests
 *  - Confirmation code
 *  - Subscriber ID
 *  - Date when the order was created
 *
 * This class is Serializable so it can be sent between client and server.
 */
public class Order implements Serializable {

    private int order_number;
    private Date order_date;
    private int number_of_guests;
    private int confirmation_code;
    private int subscriber_id;
    private Date date_of_placing_order;

    /**
     * Creates a new Order object.
     *
     * @param order_number           Unique order ID
     * @param order_date             The date of the reservation
     * @param number_of_guests       Number of guests
     * @param confirmation_code       Confirmation number
     * @param subscriber_id           ID of the subscriber who created the order
     * @param date_of_placing_order   Date the order was placed
     */
    public Order(int order_number, Date order_date, int number_of_guests,
                 int confirmation_code, int subscriber_id, Date date_of_placing_order) {
        this.order_number = order_number;
        this.order_date = order_date;
        this.number_of_guests = number_of_guests;
        this.confirmation_code = confirmation_code;
        this.subscriber_id = subscriber_id;
        this.date_of_placing_order = date_of_placing_order;
    }

    /** Returns the order number. */
    public int getOrder_number() {
        return order_number;
    }

    /** Sets the order number. */
    public void setOrder_number(int order_number) {
        this.order_number = order_number;
    }

    /** Returns the order date. */
    public Date getOrder_date() {
        return order_date;
    }

    /** Sets the order date. */
    public void setOrder_date(Date order_date) {
        this.order_date = order_date;
    }

    /** Returns how many guests are in the order. */
    public int getNumber_of_guests() {
        return number_of_guests;
    }

    /** Sets the number of guests. */
    public void setNumber_of_guests(int number_of_guests) {
        this.number_of_guests = number_of_guests;
    }

    /** Returns the confirmation code. */
    public int getConfirmation_code() {
        return confirmation_code;
    }

    /** Sets the confirmation code. */
    public void setConfirmation_code(int confirmation_code) {
        this.confirmation_code = confirmation_code;
    }

    /** Returns the subscriber ID. */
    public int getSubscriber_id() {
        return subscriber_id;
    }

    /** Sets the subscriber ID. */
    public void setSubscriber_id(int subscriber_id) {
        this.subscriber_id = subscriber_id;
    }

    /** Returns the placing date of the order. */
    public Date getDate_of_placing_order() {
        return date_of_placing_order;
    }

    /** Sets the placing date of the order. */
    public void setDate_of_placing_order(Date date_of_placing_order) {
        this.date_of_placing_order = date_of_placing_order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(confirmation_code, date_of_placing_order, number_of_guests,
                order_date, order_number, subscriber_id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Order other = (Order) obj;

        return order_number == other.order_number &&
               number_of_guests == other.number_of_guests &&
               confirmation_code == other.confirmation_code &&
               subscriber_id == other.subscriber_id &&
               Objects.equals(order_date, other.order_date) &&
               Objects.equals(date_of_placing_order, other.date_of_placing_order);
    }

    /**
     * Returns a readable representation of the order, used for printing and debugging.
     */
    @Override
    public String toString() {
        return "Order [order_number=" + order_number +
                ", order_date=" + order_date +
                ", number_of_guests=" + number_of_guests +
                ", confirmation_code=" + confirmation_code +
                ", subscriber_id=" + subscriber_id +
                ", date_of_placing_order=" + date_of_placing_order + "]";
    }

    /**
     * Parses a string into an Order object.
     * Assumes the format is the same as produced by toString().
     *
     * @param str The string representing the order
     * @return A new Order object
     * @throws ParseException If a date field fails to parse
     */
    public static Order parseOrder(String str) throws ParseException {

        String[] parts = str.split(", ");

        int order_number = 0;
        Date order_date = null;
        int number_of_guests = 0;
        int confirmation_code = 0;
        int subscriber_id = 0;
        Date date_of_placing_order = null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        for (String part : parts) {
            String[] keyValue = part.split("=");

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            switch (key) {
                case "order_number":
                    order_number = Integer.parseInt(value);
                    break;

                case "order_date":
                    order_date = sdf.parse(value);
                    break;

                case "number_of_guests":
                    number_of_guests = Integer.parseInt(value);
                    break;

                case "confirmation_code":
                    confirmation_code = Integer.parseInt(value);
                    break;

                case "subscriber_id":
                    subscriber_id = Integer.parseInt(value);
                    break;

                case "date_of_placing_order":
                    date_of_placing_order = sdf.parse(value);
                    break;
            }
        }

        return new Order(order_number, order_date, number_of_guests,
                confirmation_code, subscriber_id, date_of_placing_order);
    }
}
