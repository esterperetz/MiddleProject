package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity class representing an entry in the Waiting List.
 * 
 * This object holds information about a customer waiting for a table,
 * including the requested time, party size, and their status in the queue.
 * It implements Serializable to allow transfer between client and server.
 */
public class WaitingList implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer waitingId;
    private Integer customerId;

    private int numberOfGuests;
    private Date enterTime;
    private int confirmationCode;
    private Customer customer;
    private Date reservationDate;

    // Acts as a status flag (1 = active in list, 0 = removed/promoted)
    private int inWaitingList;

    /**
     * Default constructor.
     */
    public WaitingList() {

    }

 
    public WaitingList(Integer waitingId, Integer customerId,
            int numberOfGuests, Date enterTime, int confirmationCode, Customer customer) {
        this.waitingId = waitingId;
        this.customerId = customerId;
        this.numberOfGuests = numberOfGuests;
        this.enterTime = enterTime;
        this.confirmationCode = confirmationCode;
        this.customer = customer;
    }

    // --- Getters and Setters ---

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setReservationDate(Date date) {
        this.reservationDate = date;
    }

    public Date getReservationDate() {
        return this.reservationDate;
    }

    public Integer getWaitingId() {
        return waitingId;
    }

    public void setWaitingId(Integer waitingId) {
        this.waitingId = waitingId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public Date getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(Date enterTime) {
        this.enterTime = enterTime;
    }

    public int getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(int confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    /**
     * Sets the status of the entry in the waiting list.
     * Usually 1 for active, 0 for removed/promoted.
     *
     * @param val The status value.
     */
    public void setInWaitingList(int val) {
        this.inWaitingList = val;
    }

    public int getInWaitingList() {
        return inWaitingList;
    }

    @Override
    public String toString() {
        return "WaitingList [guests=" + numberOfGuests + ", code=" + confirmationCode + "]" + " date: " + reservationDate;
    }
}