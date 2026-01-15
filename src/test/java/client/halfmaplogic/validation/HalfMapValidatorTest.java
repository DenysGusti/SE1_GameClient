package client.halfmaplogic.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.data.fromclient.HalfMap;
import client.halfmaplogic.validation.exception.HalfMapGenerationException;
import client.halfmaplogic.validation.rule.HalfMapValidationRule;

public class HalfMapValidatorTest {
    private HalfMapValidator validator;
    private HalfMapValidationRule ruleMock;
    private HalfMap halfMapMock;

    @BeforeEach
    public void setUp() {
        ruleMock = mock(HalfMapValidationRule.class);
        halfMapMock = mock(HalfMap.class);
        validator = new HalfMapValidator(Set.of(ruleMock));
    }

    @Test
    public void ValidHalfMap_ValidateCalledWithNoErrors_ReturnsEmptyNotification() {
        when(ruleMock.validate(halfMapMock)).thenReturn(Collections.emptyList());

        var notification = validator.validate(halfMapMock);

        assertThat(notification.hasErrors(), is(false));
    }

    @Test
    public void InvalidHalfMap_ValidateCalledWithRuleErrors_ReturnsNotificationWithErrors() {
        var error = new HalfMapGenerationException("Invalid Terrain");
        when(ruleMock.validate(halfMapMock)).thenReturn(List.of(error));

        var notification = validator.validate(halfMapMock);

        assertThat(notification.hasErrors(), is(true));
        assertThat(notification.getErrors(), hasSize(1));
        assertThat(notification.getErrors().getFirst().getMessage(), is("Invalid Terrain"));
    }

    @Test
    public void ValidRule_AddRuleCalled_RuleIsUsedDuringValidation() {
        var secondRuleMock = mock(HalfMapValidationRule.class);
        var secondError = new HalfMapGenerationException("Second Error");

        when(ruleMock.validate(halfMapMock)).thenReturn(Collections.emptyList());
        when(secondRuleMock.validate(halfMapMock)).thenReturn(List.of(secondError));

        validator.addRule(secondRuleMock);
        var notification = validator.validate(halfMapMock);

        assertThat(notification.getErrors(), hasSize(1));
        assertThat(notification.getErrors().getFirst(), is(secondError));
    }

    @Test
    public void NullRulesSet_ConstructorCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new HalfMapValidator(null));
    }

    @Test
    public void NullRule_AddRuleCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> validator.addRule(null));
    }

    @Test
    public void NullHalfMap_ValidateCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(null));
    }
}