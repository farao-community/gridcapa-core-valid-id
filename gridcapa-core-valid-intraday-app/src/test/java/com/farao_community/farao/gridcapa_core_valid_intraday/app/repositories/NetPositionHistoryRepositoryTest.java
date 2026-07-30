/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.repositories;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.NetPositionHistory;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.Season;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.createNphsFromSeason;

@SpringBootTest
class NetPositionHistoryRepositoryTest {

    @Autowired
    NetPositionHistoryRepository netPositionHistoryRepository;

    @Autowired
    private CoreHubsConfiguration coreHubsConfiguration;

    @BeforeEach
    void setup() {
        final Set<NetPositionHistory> summerNphs = createNphsFromSeason(Season.SUMMER, coreHubsConfiguration.getCoreHubs());
        netPositionHistoryRepository.saveAllAndFlush(summerNphs);
    }

    @Test
    void findAllBySeason() {
        Assertions.assertThat(netPositionHistoryRepository.findAllBySeason(Season.SPRING))
                .isEmpty();
        Assertions.assertThat(netPositionHistoryRepository.findAllBySeason(Season.AUTUMN))
                .isEmpty();
        Assertions.assertThat(netPositionHistoryRepository.findAllBySeason(Season.WINTER))
                .isEmpty();
        Assertions.assertThat(netPositionHistoryRepository.findAllBySeason(Season.SUMMER))
                .isNotEmpty()
                .hasSize(coreHubsConfiguration.getCoreHubs().size());
    }

    @AfterEach
    void tearDown() {
        netPositionHistoryRepository.deleteAll();
    }
}
