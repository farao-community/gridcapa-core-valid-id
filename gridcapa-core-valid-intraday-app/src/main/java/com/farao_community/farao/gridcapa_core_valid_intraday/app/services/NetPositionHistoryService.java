/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.configuration.SeasonDatesConfiguration;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.NetPositionHistory;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.Season;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.repositories.NetPositionHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
public class NetPositionHistoryService {

    private final NetPositionHistoryRepository netPositionHistoryRepository;
    private final SeasonDatesConfiguration seasonDatesConfiguration;

    public NetPositionHistoryService(final NetPositionHistoryRepository netPositionHistoryRepository,
                                     final SeasonDatesConfiguration seasonDatesConfiguration) {
        this.netPositionHistoryRepository = netPositionHistoryRepository;
        this.seasonDatesConfiguration = seasonDatesConfiguration;
    }

    public Set<NetPositionHistory> getNpHistoryForTimestamp(final OffsetDateTime targetProcessDateTime) {
        final Season processSeason = getSeasonForTimestamp(targetProcessDateTime);
        return netPositionHistoryRepository.findAllBySeason(processSeason);
    }

    public List<NetPositionHistory> saveAll(final Set<NetPositionHistory> npHistoryToSave) {
        return netPositionHistoryRepository.saveAllAndFlush(npHistoryToSave);
    }

    private Season getSeasonForTimestamp(final OffsetDateTime targetProcessDateTime) {
        final OffsetDateTime springStart = getSeasonStartDate(seasonDatesConfiguration.getSpringStartDate(), targetProcessDateTime);
        final OffsetDateTime summerStart = getSeasonStartDate(seasonDatesConfiguration.getSummerStartDate(), targetProcessDateTime);
        final OffsetDateTime autumnStart = getSeasonStartDate(seasonDatesConfiguration.getAutumnStartDate(), targetProcessDateTime);
        final OffsetDateTime winterStart = getSeasonStartDate(seasonDatesConfiguration.getWinterStartDate(), targetProcessDateTime);
        //not knowing the dates, this is a temporary implementation
        //WARNING the logic depends of the configuration dates : here we consider the winter to be on two different years
        if ((springStart.isBefore(targetProcessDateTime) || springStart.isEqual(targetProcessDateTime)) && summerStart.isAfter(targetProcessDateTime)) {
            return Season.SPRING;
        } else if ((summerStart.isBefore(targetProcessDateTime) || summerStart.isEqual(targetProcessDateTime)) && autumnStart.isAfter(targetProcessDateTime)) {
            return Season.SUMMER;
        } else if ((autumnStart.isBefore(targetProcessDateTime) || autumnStart.isEqual(targetProcessDateTime)) && winterStart.isAfter(targetProcessDateTime)) {
            return Season.AUTUMN;
        } else if (winterStart.isBefore(targetProcessDateTime) || winterStart.isEqual(targetProcessDateTime) || springStart.isAfter(targetProcessDateTime)) {
            return Season.WINTER;
        } else {
            throw new CoreValidIntradayInvalidDataException("Impossible to find season for target process date : configuration issue possible!");
        }
    }

    private OffsetDateTime getSeasonStartDate(final String configStartData,
                                              final OffsetDateTime targetProcessDateTime) {
        final String[] dayMonthSpring = configStartData.split("-");
        return targetProcessDateTime.withMonth(Integer.parseInt(dayMonthSpring[1])).withDayOfMonth(Integer.parseInt(dayMonthSpring[0]));
    }
}
