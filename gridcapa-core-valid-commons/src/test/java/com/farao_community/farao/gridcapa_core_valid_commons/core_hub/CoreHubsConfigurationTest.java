/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_commons.core_hub;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_externe at rte-france.com>}
 */
@EnableConfigurationProperties
@SpringBootTest(classes = CoreHubsConfiguration.class)
class CoreHubsConfigurationTest {

    @Autowired
    private CoreHubsConfiguration coreHubsConfiguration;

    @Test
    void getCoreHubs() {
        Assertions.assertThat(coreHubsConfiguration).isNotNull();
        Assertions.assertThat(coreHubsConfiguration.getCoreHubs())
                .isNotNull()
                .hasSize(14);
        final CoreHub coreHubFrance = new CoreHub("France", "FR", "PTDF_FR", "FR-CORE", "FR", false, false, 1);
        final CoreHub coreHubHvdc = new CoreHub("Hub Allemagne AleGro", "DE_ALEGrO", "PTDF_DE_AL", "ALDE-CORE", "DE_AL", true, false, 1);
        Assertions.assertThat(coreHubsConfiguration.getCoreHubs()).contains(coreHubFrance, coreHubHvdc);
    }
}
