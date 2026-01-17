package entities;

import java.io.Serializable;

/**
 * Entity class representing the availability status of a specific time slot.
 * 
 * This DTO (Data Transfer Object) is used to transport availability data
 * (time, capacity, occupancy) from the server to the client for UI rendering.
 */
public class TimeSlotStatus implements Serializable {

    private static final long serialVersionUID = 1L;
    private String time;
    private boolean isFull;
    private int currentDiners;
    private int maxCapacity;

    
    public TimeSlotStatus(String time, boolean isFull, int currentDiners, int maxCapacity) {
        this.time = time;
        this.isFull = isFull;
        this.currentDiners = currentDiners;
        this.maxCapacity = maxCapacity;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isFull() {
        return isFull;
    }

    public void setFull(boolean isFull) {
        this.isFull = isFull;
    }

    public int getCurrentDiners() {
        return currentDiners;
    }

    public void setCurrentDiners(int currentDiners) {
        this.currentDiners = currentDiners;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "TimeSlotStatus [time=" + time + ", isFull=" + isFull + ", currentDiners=" + currentDiners
                + ", maxCapacity=" + maxCapacity + "]";
    }
}