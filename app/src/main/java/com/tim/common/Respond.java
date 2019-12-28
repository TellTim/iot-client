package com.tim.common;

/**
 * Respond
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:48
 */
public class Respond {
    private State state;
    private String data;

    public Respond(State state, String data) {
        this.state = state;
        this.data = data;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public enum State{
        /**
         *
         */
        NO_LOCAL_AUTH,
        NO_REMOTE_AUTH,
        AUTH_CONFIRM,
        UNKNOWN_EXCEPTION;
    }
}
