package com.tim.common;

/**
 * Respond
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:48
 */
public class Respond<T> {
    private State state;
    private T t;

    public Respond(State state, T t) {
        this.state = state;
        this.t = t;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }



    public enum State {
        /**
         *
         */
        SUCCESS("200", "success"),
        PARAM_EMPTY("3000", "param empty"),
        BIND_EXIST("4000", "bind exist"),
        BIND_NOT_EXIST("4001", "bind not exist"),
        LOCAL_BIND_NOT_EXIST("4002", "local bind not exist"),
        DEVICE_NOT_EXIST("5000", "device not exist"),
        REGISTER_FAILURE("5001", "device not exist"),
        ERROR("9000", "error"),
        NET_ERROR("9001", "net error");

        private String code;
        private String value;

        State(String code, String value) {
            this.code = code;
            this.value = value;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
