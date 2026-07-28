/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.NetPositionHistory;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.Season;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.repositories.NetPositionHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Set;

@Service
public class NetPositionHistoryService {

    private NetPositionHistoryRepository netPositionHistoryRepository;

    public NetPositionHistoryService(final NetPositionHistoryRepository netPositionHistoryRepository) {
        this.netPositionHistoryRepository = netPositionHistoryRepository;
    }

    public Set<NetPositionHistory> getNpHistoryForTimestamp(final OffsetDateTime targetProcessDateTime) {
        final Season processSeason = getSeasonForTimestamp(targetProcessDateTime);
        return netPositionHistoryRepository.findAllBySeason(processSeason);
    }

    private Season getSeasonForTimestamp(final OffsetDateTime targetProcessDateTime) {


        return null;
    }

    public void saveAll(final Set<NetPositionHistory> npHistoryToSave) {
        netPositionHistoryRepository.saveAllAndFlush(npHistoryToSave);
    }
}
