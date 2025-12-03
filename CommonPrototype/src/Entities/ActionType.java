package Entities;

import java.io.Serializable;

public enum ActionType implements Serializable {
    GET_ALL,
    GET_BY_ID,
    CREATE,
    UPDATE,
    DELETE
}