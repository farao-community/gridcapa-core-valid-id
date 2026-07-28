/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.entities;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NetPositionHistoryTest {

    @Test
    void testGettersAndSetters() {
        final UUID uuid = UUID.randomUUID();
        final String hubRamcep2Code = "FR";
        final Season season = Season.SUMMER;
        final double minimumNetPosition = 123.1;
        final double maximumNetPosition = 4000.0;
        final NetPositionHistory npHistory = new NetPositionHistory();
        npHistory.setId(uuid);
        npHistory.setHubRamcep2Code(hubRamcep2Code);
        npHistory.setSeason(season);
        npHistory.setMinimumNetPosition(minimumNetPosition);
        npHistory.setMaximumNetPosition(maximumNetPosition);
        assertEquals(uuid, npHistory.getId());
        assertEquals(hubRamcep2Code, npHistory.getHubRamcep2Code());
        assertEquals(season, npHistory.getSeason());
        assertEquals(minimumNetPosition, npHistory.getMinimumNetPosition(), 0.2);
        assertEquals(maximumNetPosition, npHistory.getMaximumNetPosition(), 0.2);
    }
}
