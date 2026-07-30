package com.farao_community.farao.gridcapa_core_valid_intraday.app.utils;

import com.farao_community.farao.gridcapa.task_manager.api.TaskParameterDto;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CoreValidIntradayTaskParameters;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.NetPositionHistory;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.Season;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public static CoreValidIntradayFileResource createFileResource(final String filename,
                                                             final URL resource) {
        return new CoreValidIntradayFileResource(filename, resource.toExternalForm());
    }

    public static Set<NetPositionHistory> createNphsFromSeason(final Season season,
                                                 final List<CoreHub> coreHubs) {
        return coreHubs.stream()
                .map(ch -> createNphFromSeasonAndCode(ch.ramcep2Code(), season))
                .collect(Collectors.toSet());
    }

    public static NetPositionHistory createNphFromSeasonAndCode(final String ramcep2Code,
                                                  final Season season) {
        final NetPositionHistory nph = new NetPositionHistory();
        nph.setId(UUID.randomUUID());
        nph.setSeason(season);
        nph.setHubRamcep2Code(ramcep2Code);
        nph.setMinimumNetPosition(-401.0);
        nph.setMaximumNetPosition(999.0);
        return nph;
    }
}
