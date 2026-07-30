package com.farao_community.farao.gridcapa_core_valid_intraday.app.utils;

import com.farao_community.farao.gridcapa.task_manager.api.TaskParameterDto;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CoreValidIntradayTaskParameters;

import java.util.List;

public final class TestUtils {

    private TestUtils() {

    }

    public static CoreValidIntradayTaskParameters getTestCoreValidIntradayTaskParameters() {
        List<TaskParameterDto> parameters = List.of(
                new TaskParameterDto("MAX_SELECTED_VERTICES", "INT", "16", "6"),
                new TaskParameterDto("MARGIN_FOR_PREFILTER", "INT", "100", "1515"),
                new TaskParameterDto("FRM_MARGIN_PERCENTAGE", "INT", "5", "45"),
                new TaskParameterDto("MIN_RAM_MCCC", "INT", "20", "25"),
                new TaskParameterDto("PONDERATION_CLOSEST", "INT", "31", "1515"),
                new TaskParameterDto("PONDERATION_ANGLE", "INT", "33", "1515"),
                new TaskParameterDto("PONDERATION_CONSTRAINED", "INT", "34", "1515")
        );

        return new CoreValidIntradayTaskParameters(parameters);
    }

    public static CoreValidIntradayTaskParameters getTestCoreValidIntradayTaskParametersMaxSelect2() {
        List<TaskParameterDto> parameters = List.of(
                new TaskParameterDto("MAX_SELECTED_VERTICES", "INT", "2", "6"),
                new TaskParameterDto("MARGIN_FOR_PREFILTER", "INT", "100", "1515"),
                new TaskParameterDto("FRM_MARGIN_PERCENTAGE", "INT", "5", "45"),
                new TaskParameterDto("MIN_RAM_MCCC", "INT", "20", "25"),
                new TaskParameterDto("PONDERATION_CLOSEST", "INT", "31", "1515"),
                new TaskParameterDto("PONDERATION_ANGLE", "INT", "33", "1515"),
                new TaskParameterDto("PONDERATION_CONSTRAINED", "INT", "34", "1515")
        );

        return new CoreValidIntradayTaskParameters(parameters);
    }
}
