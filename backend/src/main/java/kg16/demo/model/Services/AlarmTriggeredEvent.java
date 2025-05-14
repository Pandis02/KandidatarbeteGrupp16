package kg16.demo.model.services;

/**
 * Represents a custom event triggered by an alarm condition in the system.
 * This event can be published to notify listeners (e.g., email or webhook services).
 *
 * The three variables typically represent alarm metadata such as type, source, and reason.
 */
public class AlarmTriggeredEvent {

    private final String variable1;
    private final String variable2;
    private final String variable3;

    /**
     * Constructs a new AlarmTriggeredEvent with the given parameters.
     *
     * @param variable1 the device
     * @param variable2 the building
     * @param variable3 the room
     */
    public AlarmTriggeredEvent(String variable1, String variable2, String variable3) {
        this.variable1 = variable1;
        this.variable2 = variable2;
        this.variable3 = variable3;
    }

    /** @return the device attacked */
    public String getVariable1() {
        return variable1;
    }

    /** @return the building it is located in */
    public String getVariable2() {
        return variable2;
    }

    /** @return the room it is located in */
    public String getVariable3() {
        return variable3;
    }
}
