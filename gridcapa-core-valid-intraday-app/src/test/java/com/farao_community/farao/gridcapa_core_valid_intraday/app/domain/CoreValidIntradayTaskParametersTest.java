package com.farao_community.farao.gridcapa_core_valid_intraday.app.domain;

import com.farao_community.farao.gridcapa.task_manager.api.TaskParameterDto;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoreValidIntradayTaskParametersTest {

    @Test
    void nominalTest() {
        final CoreValidIntradayTaskParameters params = TestUtils.getTestCoreValidIntradayTaskParameters();

        assertThat(params.getMaxSelectedVertices()).isEqualTo(16);
        assertThat(params.getMarginForPrefilter()).isEqualTo(100);
        assertThat(params.getFrmMarginPercentage()).isEqualTo(5);
        assertThat(params.getMinRamMccc()).isEqualTo(20);
        assertThat(params.getPonderationClosest()).isEqualTo(31);
        assertThat(params.getPonderationAngle()).isEqualTo(33);
        assertThat(params.getPonderationConstrained()).isEqualTo(34);
    }

    @Test
    void unknownParameterTest() {
        List<TaskParameterDto> parameters = List.of(
                new TaskParameterDto("UNKNOWN_PARAMETER", "BOOLEAN", "true", "true")
        );

        // expected: object is created and no exception is thrown
        assertThat(new CoreValidIntradayTaskParameters(parameters)).isInstanceOf(CoreValidIntradayTaskParameters.class);
    }

    @Test
    void absentParametersTest() {
        final CoreValidIntradayTaskParameters params = new CoreValidIntradayTaskParameters(List.of());
        assertThat(params.getMaxSelectedVertices()).isZero();
        assertThat(params.getMarginForPrefilter()).isZero();
        assertThat(params.getFrmMarginPercentage()).isZero();
        assertThat(params.getMinRamMccc()).isZero();
        assertThat(params.getPonderationClosest()).isZero();
        assertThat(params.getPonderationAngle()).isZero();
        assertThat(params.getPonderationConstrained()).isZero();
    }

    @Test
    void badTypeParameterIntTest() {
        List<TaskParameterDto> parameters = List.of(
                new TaskParameterDto("MAX_SELECTED_VERTICES", "STRING", "test", "default")
        );

        Assertions.assertThatExceptionOfType(CoreValidIntradayInvalidDataException.class)
                .isThrownBy(() -> new CoreValidIntradayTaskParameters(parameters))
                .withMessage("Validation of parameters failed. Failure reasons are: [\"Parameter MAX_SELECTED_VERTICES was expected to be of type INT, got STRING\"].");
    }

    @Test
    void validationFailureIntParameterNotParseableTest() {
        List<TaskParameterDto> parameters = List.of(
                new TaskParameterDto("MAX_SELECTED_VERTICES", "INT", "3.14", "25")
        );

        Assertions.assertThatExceptionOfType(CoreValidIntradayInvalidDataException.class)
                .isThrownBy(() -> new CoreValidIntradayTaskParameters(parameters))
                .withMessage("Validation of parameters failed. Failure reasons are: [\"Parameter MAX_SELECTED_VERTICES could not be parsed as integer (value: 3.14)\"].");
    }

    @Test
    void validationFailurePositiveIntParameterTest() {
        List<TaskParameterDto> parameters = List.of(
                new TaskParameterDto("MAX_SELECTED_VERTICES", "INT", "-2", "10"),
                new TaskParameterDto("PONDERATION_CONSTRAINED", "INT", "-5", "15")
        );

        Assertions.assertThatExceptionOfType(CoreValidIntradayInvalidDataException.class)
                .isThrownBy(() -> new CoreValidIntradayTaskParameters(parameters))
                .withMessage("Validation of parameters failed. Failure reasons are: [" +
                             "\"Parameter MAX_SELECTED_VERTICES should be positive (value: -2)\" ; " +
                             "\"Parameter PONDERATION_CONSTRAINED should be positive (value: -5)\"" +
                             "].");
    }
}
