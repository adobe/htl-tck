package sightlytck.scripts.exprlang.operators;

public class UseEnumTestHelper {

    enum Constant {
        CONSTANT1, CONSTANT2
    }

    private Constant value1 = Constant.CONSTANT1;
    private Constant value2 = Constant.CONSTANT2;

    public Constant getValue1() {
        return value1;
    }

    public Constant getValue2() {
        return value2;
    }
}