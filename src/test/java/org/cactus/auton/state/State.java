package org.cactus.auton.state;

public enum State {
    START("start"), APPLY("apply"), RUNNING("running"), FINISH("finish"), ERROR("error");
    private final String value;
    State(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }

    /**
     * 判断是否等于给定的 value 字符串。
     */
    public boolean equalsValue(String value) {
        return this.value.equals(value);
    }
}
