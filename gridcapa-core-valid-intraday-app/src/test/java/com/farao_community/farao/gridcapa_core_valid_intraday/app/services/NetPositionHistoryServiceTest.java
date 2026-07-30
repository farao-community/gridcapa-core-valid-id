/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.NetPositionHistory;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.Season;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.repositories.NetPositionHistoryRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.createNphsFromSeason;

@SpringBootTest
class NetPositionHistoryServiceTest {

    @Autowired
    NetPositionHistoryService netPositionHistoryService;

    @Autowired
    private CoreHubsConfiguration coreHubsConfiguration;

    @Autowired
    private NetPositionHistoryRepository netPositionHistoryRepository;

    @Test
    void getNpHistoryForTimestamp() {
        netPositionHistoryRepository.deleteAll();

        final List<CoreHub> coreHubs = coreHubsConfiguration.getCoreHubs();
        final Set<NetPositionHistory> nphsSpring = createNphsFromSeason(Season.SPRING, coreHubs);
        final Set<NetPositionHistory> nphsSummer = createNphsFromSeason(Season.SUMMER, coreHubs);
        final Set<NetPositionHistory> nphsAutumn = createNphsFromSeason(Season.AUTUMN, coreHubs);
        final Set<NetPositionHistory> nphsWinter = createNphsFromSeason(Season.WINTER, coreHubs);
        netPositionHistoryService.saveAll(nphsSpring);
        netPositionHistoryService.saveAll(nphsSummer);
        netPositionHistoryService.saveAll(nphsAutumn);
        netPositionHistoryService.saveAll(nphsWinter);
        final OffsetDateTime winter1 = OffsetDateTime.of(LocalDateTime.of(1999, Month.JANUARY, 1, 1, 32, 59), ZoneOffset.of("+01:00"));

        final Set<NetPositionHistory> winter1Result = netPositionHistoryService.getNpHistoryForTimestamp(winter1);
        Assertions.assertThat(winter1Result)
                .first()
                .hasFieldOrPropertyWithValue("season", Season.WINTER);

        final OffsetDateTime winter2 = OffsetDateTime.of(LocalDateTime.of(2021, Month.MARCH, 22, 1, 32, 59), ZoneOffset.of("+01:00"));

        final Set<NetPositionHistory> winter2Result = netPositionHistoryService.getNpHistoryForTimestamp(winter2);
        Assertions.assertThat(winter2Result)
                .first()
                .hasFieldOrPropertyWithValue("season", Season.WINTER);

        final OffsetDateTime winter3 = OffsetDateTime.of(LocalDateTime.of(2025, Month.DECEMBER, 21, 1, 32, 59), ZoneOffset.of("+01:00"));

        final Set<NetPositionHistory> winter3Result = netPositionHistoryService.getNpHistoryForTimestamp(winter3);
        Assertions.assertThat(winter3Result)
                .first()
                .hasFieldOrPropertyWithValue("season", Season.WINTER);

        final OffsetDateTime spring1 = OffsetDateTime.of(LocalDateTime.of(2021, Month.MARCH, 23, 1, 32, 59), ZoneOffset.of("+01:00"));

        final Set<NetPositionHistory> spring1Result = netPositionHistoryService.getNpHistoryForTimestamp(spring1);
        Assertions.assertThat(spring1Result)
                .first()
                .hasFieldOrPropertyWithValue("season", Season.SPRING);

        final OffsetDateTime spring2 = OffsetDateTime.of(LocalDateTime.of(2025, Month.JUNE, 21, 1, 32, 59), ZoneOffset.of("+02:00"));

        final Set<NetPositionHistory> spring2Result = netPositionHistoryService.getNpHistoryForTimestamp(spring2);
        Assertions.assertThat(spring2Result)
                .first()
                .hasFieldOrPropertyWithValue("season", Season.SPRING);

        final OffsetDateTime summer1 = OffsetDateTime.of(LocalDateTime.of(2021, Month.JUNE, 22, 1, 32, 59), ZoneOffset.of("+02:00"));

        final Set<NetPositionHistory> summer1Result = netPositionHistoryService.getNpHistoryForTimestamp(summer1);
        Assertions.assertThat(summer1Result)
                .first()
                .hasFieldOrPropertyWithValue("season", Season.SUMMER);

        final OffsetDateTime summer2 = OffsetDateTime.of(LocalDateTime.of(2025, Month.SEPTEMBER, 22, 1, 32, 59), ZoneOffset.of("+02:00"));

        final Set<NetPositionHistory> summer2Result = netPositionHistoryService.getNpHistoryForTimestamp(summer2);
        Assertions.assertThat(summer2Result)
                .first()
                .hasFieldOrPropertyWithValue("season", Season.SUMMER);

        final OffsetDateTime autumn1 = OffsetDateTime.of(LocalDateTime.of(2021, Month.SEPTEMBER, 23, 1, 32, 59), ZoneOffset.of("+02:00"));

        final Set<NetPositionHistory> autumn1Result = netPositionHistoryService.getNpHistoryForTimestamp(autumn1);
        Assertions.assertThat(autumn1Result)
                .first()
                .hasFieldOrPropertyWithValue("season", Season.AUTUMN);

        final OffsetDateTime autumn2 = OffsetDateTime.of(LocalDateTime.of(2025, Month.DECEMBER, 20, 1, 32, 59), ZoneOffset.of("+01:00"));

        final Set<NetPositionHistory> autumn2Result = netPositionHistoryService.getNpHistoryForTimestamp(autumn2);
        Assertions.assertThat(autumn2Result)
                .first()
                .hasFieldOrPropertyWithValue("season", Season.AUTUMN);
    }

}
