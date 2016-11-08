package org.mockitousage.debugging;

import org.assertj.core.api.Condition;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.listeners.VerificationListener;
import org.mockito.verification.VerificationEvent;
import org.mockito.verification.VerificationMode;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class VerificationListenerCallBackTest {

    @Test
    public void should_call_single_listener_on_verify() throws Exception {
        //given
        RememberingListener listener = new RememberingListener();
        MockingProgress mockingProgress = ThreadSafeMockingProgress.mockingProgress();
        mockingProgress.addListener(listener);

        Foo foo = mock(Foo.class);
        Method invocationWantedMethod = Foo.class.getDeclaredMethod("doSomething", String.class);

        //when
        verify(foo, never()).doSomething("");

        //then
        assertThat(listener).is(notifiedFor(foo, never(), invocationWantedMethod));
    }

    @Test
    public void should_call_all_listeners_on_verify() throws Exception {
        //given
        RememberingListener listener = new RememberingListener();
        RememberingListener listener2 = new RememberingListener();

        MockingProgress mockingProgress = ThreadSafeMockingProgress.mockingProgress();
        mockingProgress.addListener(listener);
        mockingProgress.addListener(listener2);

        Foo foo = mock(Foo.class);
        Method invocationWantedMethod = Foo.class.getDeclaredMethod("doSomething", String.class);

        //when
        verify(foo, never()).doSomething("");

        //then
        assertThat(listener).is(notifiedFor(foo, never(), invocationWantedMethod));
        assertThat(listener2).is(notifiedFor(foo, never(), invocationWantedMethod));
    }

    @Test
    public void should_not_call_listener_when_verify_was_called_incorrectly() {
        //given
        RememberingListener listener = new RememberingListener();
        MockingProgress mockingProgress = ThreadSafeMockingProgress.mockingProgress();
        mockingProgress.addListener(listener);

        Foo foo = null;

        //when
        try {
            verify(foo).doSomething("");
            fail("Exception expected.");
        } catch (Exception e) {
            //then
            assertNull(listener.cause);
        }
    }

    @Test
    public void should_notify_when_verification_throws_type_error() {
        //given
        RememberingListener listener = new RememberingListener();
        MockingProgress mockingProgress = ThreadSafeMockingProgress.mockingProgress();
        mockingProgress.addListener(listener);

        Foo foo = mock(Foo.class);

        //when
        try {
            verify(foo).doSomething("");
            fail("Exception expected.");
        } catch (Throwable e) {
            //then
            assertThat(listener.cause).isInstanceOf(MockitoAssertionError.class);
        }
    }

    @Test
    public void should_notify_when_verification_throws_runtime_exception() {
        RememberingListener listener = new RememberingListener();
        MockingProgress mockingProgress = ThreadSafeMockingProgress.mockingProgress();
        mockingProgress.addListener(listener);

        Foo foo = mock(Foo.class);
        //when
        try {
            verify(foo, new RuntimeExceptionVerificationMode()).doSomething("");
            fail("Exception expected.");
        } catch (Throwable e) {
            //then
            assertThat(listener.cause).isInstanceOf(RuntimeException.class);
        }
    }

    static class RememberingListener implements VerificationListener {
        Object mock;
        VerificationMode mode;
        VerificationData data;
        Throwable cause;

        @Override
        public void onVerification(VerificationEvent verificationEvent) {
            this.mock = verificationEvent.getMock();
            this.mode = verificationEvent.getMode();
            this.data = verificationEvent.getData();
            this.cause = verificationEvent.getCause();
        }

        public Method getWantedMethod() {
            return data.getWanted().getInvocation().getMethod();
        }
    }

    static Condition<RememberingListener> notifiedFor(final Object mock, final VerificationMode mode, final Method wantedMethod) {
        return new Condition<RememberingListener>() {
            public boolean matches(RememberingListener toBeAsserted) {
                assertThat(toBeAsserted.mock).isEqualTo(mock);
                assertThat(toBeAsserted.mode).isEqualToComparingFieldByField(mode);
                assertThat(toBeAsserted.getWantedMethod()).isEqualTo(wantedMethod);
                return true;
            }
        };
    }

    static class RuntimeExceptionVerificationMode implements VerificationMode {
        @Override
        public void verify(VerificationData data) {
            throw new RuntimeException();
        }

        @Override
        public VerificationMode description(String description) {
            return null;
        }
    }
}
