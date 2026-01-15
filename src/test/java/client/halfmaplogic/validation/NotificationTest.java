package client.halfmaplogic.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.halfmaplogic.validation.exception.HalfMapGenerationException;

public class NotificationTest {
    private Notification notification;

    @BeforeEach
    public void setUp() {
        notification = new Notification();
    }

    @Test
    public void NewNotification_HasErrorsCalled_ReturnsFalse() {
        assertThat(notification.hasErrors(), is(false));
        assertThat(notification.getErrors(), hasSize(0));
    }

    @Test
    public void ValidErrorsList_AddErrorsCalled_ErrorsAreStored() {
        var error1 = new HalfMapGenerationException("Error 1");
        var error2 = new HalfMapGenerationException("Error 2");
        var errorList = List.of(error1, error2);

        notification.addErrors(errorList);

        assertThat(notification.hasErrors(), is(true));
        assertThat(notification.getErrors(), hasSize(2));
        assertThat(notification.getErrors().getFirst(), is(error1));
    }

    @Test
    public void NullList_AddErrorsCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> notification.addErrors(null));
    }

    @Test
    public void ErrorsStored_GetErrorsCalled_ReturnsImmutableCopy() {
        var error = new HalfMapGenerationException("Error");
        notification.addErrors(List.of(error));

        var errors = notification.getErrors();

        assertThrows(UnsupportedOperationException.class, () -> errors.add(new HalfMapGenerationException("New")));
    }
}