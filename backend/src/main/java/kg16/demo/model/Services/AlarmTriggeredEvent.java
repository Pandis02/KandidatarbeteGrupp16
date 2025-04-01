package kg16.demo.model.services; // Keep this package since everything else is here

public class AlarmTriggeredEvent {

    private final String variable1;
    private final String variable2;
    private final String variable3;

    public AlarmTriggeredEvent(String variable1, String variable2, String variable3) {
        this.variable1 = variable1;
        this.variable2 = variable2;
        this.variable3 = variable3;
    }

    String getVariable1() {
        return variable1;
    }

    String getVariable2() {
        return variable2;
    }

    String getVariable3() {
        return variable3;
    }

}
