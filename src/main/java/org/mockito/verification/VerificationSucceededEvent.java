package org.mockito.verification;

public class VerificationSucceededEvent {
    private final Object mock;
    private final VerificationMode mode;
    private final VerificationData data;


    public VerificationSucceededEvent(Object mock, VerificationMode mode, VerificationData data) {
        this.mock = mock;
        this.mode = mode;
        this.data = data;
    }

    public Object getMock() {
        return mock;
    }

    public VerificationMode getMode() {
        return mode;
    }

    public VerificationData getData() {
        return data;
    }
}
