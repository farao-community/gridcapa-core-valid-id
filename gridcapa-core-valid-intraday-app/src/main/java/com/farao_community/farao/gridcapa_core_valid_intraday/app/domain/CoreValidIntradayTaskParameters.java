package com.farao_community.farao.gridcapa_core_valid_intraday.app.domain;

import com.farao_community.farao.gridcapa.task_manager.api.TaskParameterDto;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CoreValidIntradayTaskParameters {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreValidIntradayTaskParameters.class);
    private static final String KEY_VALUE_FORMAT = "%n\t\"%s\": %s";

    private static final String MAX_SELECTED_VERTICES = "MAX_SELECTED_VERTICES";
    private static final String MARGIN_FOR_PREFILTER = "MARGIN_FOR_PREFILTER";
    private static final String FRM_MARGIN_PERCENTAGE = "FRM_MARGIN_PERCENTAGE";
    private static final String MIN_RAM_MCCC = "MIN_RAM_MCCC";
    private static final String PONDERATION_CLOSEST = "PONDERATION_CLOSEST";
    private static final String PONDERATION_ANGLE = "PONDERATION_ANGLE";
    private static final String PONDERATION_CONSTRAINED = "PONDERATION_CONSTRAINED";

    private int maxSelectedVertices;
    private int marginForPrefilter;
    private int frmMarginPercentage;
    private int minRamMccc;
    private int ponderationClosest;
    private int ponderationAngle;
    private int ponderationConstrained;

    public CoreValidIntradayTaskParameters(final List<TaskParameterDto> parameters) {
        List<String> errors = new ArrayList<>();
        for (TaskParameterDto parameter : parameters) {
            switch (parameter.getId()) {
                case MAX_SELECTED_VERTICES -> maxSelectedVertices = validateIsPositiveIntegerAndGet(parameter, errors);
                case MARGIN_FOR_PREFILTER -> marginForPrefilter = validateIsPositiveIntegerAndGet(parameter, errors);
                case FRM_MARGIN_PERCENTAGE -> frmMarginPercentage = validateIsPositiveIntegerAndGet(parameter, errors);
                case MIN_RAM_MCCC -> minRamMccc = validateIsPositiveIntegerAndGet(parameter, errors);
                case PONDERATION_CLOSEST -> ponderationClosest = validateIsPositiveIntegerAndGet(parameter, errors);
                case PONDERATION_ANGLE -> ponderationAngle = validateIsPositiveIntegerAndGet(parameter, errors);
                case PONDERATION_CONSTRAINED -> ponderationConstrained = validateIsPositiveIntegerAndGet(parameter, errors);
                default -> LOGGER.warn("Unknown parameter {} (value: {}) will be ignored", parameter.getId(), parameter.getValue());
            }
        }
        if (!errors.isEmpty()) {
            String message = String.format("Validation of parameters failed. Failure reasons are: [\"%s\"].", String.join("\" ; \"", errors));
            throw new CoreValidIntradayInvalidDataException(message);
        }
    }

    private int validateIsIntegerAndGet(final TaskParameterDto parameter, final List<String> errors) {
        if ("INT".equals(parameter.getParameterType())) {
            String value = parameter.getValue() != null ? parameter.getValue() : parameter.getDefaultValue();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                errors.add(String.format("Parameter %s could not be parsed as integer (value: %s)", parameter.getId(), parameter.getValue()));
            }
        } else {
            errors.add(String.format("Parameter %s was expected to be of type INT, got %s", parameter.getId(), parameter.getParameterType()));
        }
        return 0; // default return value, won't be used as this return can be reached only in case of validation error
    }

    private int validateIsPositiveIntegerAndGet(final TaskParameterDto parameter, final List<String> errors) {
        int value = validateIsIntegerAndGet(parameter, errors);
        if (value < 0) {
            errors.add(String.format("Parameter %s should be positive (value: %s)", parameter.getId(), parameter.getValue()));
            return 0; // default return value, won't be used as this return can be reached only in case of validation error
        }
        return value;
    }

    public int getMaxSelectedVertices() {
        return maxSelectedVertices;
    }

    public int getMarginForPrefilter() {
        return marginForPrefilter;
    }

    public int getFrmMarginPercentage() {
        return frmMarginPercentage;
    }

    public int getMinRamMccc() {
        return minRamMccc;
    }

    public int getPonderationClosest() {
        return ponderationClosest;
    }

    public int getPonderationAngle() {
        return ponderationAngle;
    }

    public int getPonderationConstrained() {
        return ponderationConstrained;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String toJsonString() {
        List<String> appender = new ArrayList<>();
        appender.add(String.format(KEY_VALUE_FORMAT, MAX_SELECTED_VERTICES, maxSelectedVertices));
        appender.add(String.format(KEY_VALUE_FORMAT, MARGIN_FOR_PREFILTER, marginForPrefilter));
        appender.add(String.format(KEY_VALUE_FORMAT, FRM_MARGIN_PERCENTAGE, frmMarginPercentage));
        appender.add(String.format(KEY_VALUE_FORMAT, MIN_RAM_MCCC, minRamMccc));
        appender.add(String.format(KEY_VALUE_FORMAT, PONDERATION_CLOSEST, ponderationClosest));
        appender.add(String.format(KEY_VALUE_FORMAT, PONDERATION_ANGLE, ponderationAngle));
        appender.add(String.format(KEY_VALUE_FORMAT, PONDERATION_CONSTRAINED, ponderationConstrained));
        return String.format("{%s%n}", String.join(", ", appender));
    }
}
