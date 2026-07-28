/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.NetPositionHistory;
import com.powsybl.iidm.network.Network;
import com.powsybl.openrao.commons.EICode;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PrefilterVertices {

    private final NetPositionHistoryService netPositionHistoryService;
    private final List<CoreHub> coreHubs;

    public PrefilterVertices(final NetPositionHistoryService netPositionHistoryService,
                             final CoreHubsConfiguration coreHubsConfiguration) {
        this.netPositionHistoryService = netPositionHistoryService;
        coreHubs = coreHubsConfiguration.getCoreHubs();
    }

    public List<Vertex> prefilterVertices(
            final OffsetDateTime targetProcessDateTime,
            final ReferenceProgram marketPoints,
            final Network network,
            final List<Vertex> projectedVertices,
            final int maxSelectedVertices) {
        final List<Vertex> historicalFilteredVertices = historicPositionsFilter(targetProcessDateTime, marketPoints, projectedVertices);
        if(isListSmallerThanMax(historicalFilteredVertices, maxSelectedVertices)) {
            return projectedVertices;
        }
        final List<Vertex> hubCapacityFilteredVertices = hubCapacityFilter(network, historicalFilteredVertices);
        if(isListSmallerThanMax(hubCapacityFilteredVertices, maxSelectedVertices)) {
            return projectedVertices;
        }
        return hubCapacityFilteredVertices;
    }

    private List<Vertex> historicPositionsFilter(
            final OffsetDateTime targetProcessDateTime,
            final ReferenceProgram marketPoints,
            final List<Vertex> projectedVertices) {
        final Map<CoreHub, NetPositionHistory> nphByCoreHub = getAndUpdateNetPositionHistory(targetProcessDateTime, marketPoints);
        return projectedVertices.stream()
                .filter(vertex -> isVertexInNpBounds(vertex, nphByCoreHub))
                .toList();
    }

    private List<Vertex> hubCapacityFilter(final Network network, final List<Vertex> projectedVertices) {
        Map<CoreHub, Pair<Double, Double>> generatorAndLoadByCoreHub = new HashMap<>();
        coreHubs.stream()
                .filter(coreHub -> !coreHub.isHvdcHub())
                .f
        return null;
    }

    private boolean isVertexInNpBounds(final Vertex vertex,
                                       final Map<CoreHub, NetPositionHistory> nphByCoreHub) {
        final Map<String, Integer> coordinates = vertex.coordinates();
        return nphByCoreHub.entrySet()
                .stream()
                .map(entry -> coordinates.get(entry.getKey().clusterVerticeCode()) <= entry.getValue().getMaximumNetPosition()
                                    && coordinates.get(entry.getKey().clusterVerticeCode()) >= entry.getValue().getMinimumNetPosition())
                .reduce((a, b) -> a && b)
                .orElseThrow(() -> new CoreValidIntradayInvalidDataException(String.format("Coordinates missing for vertex id : {}", vertex.vertexId())));
    }

    private Map<CoreHub, NetPositionHistory> getAndUpdateNetPositionHistory(final OffsetDateTime targetProcessDateTime,
                                                                            final ReferenceProgram marketPoints) {
        final Set<NetPositionHistory> npHistory = netPositionHistoryService.getNpHistoryForTimestamp(targetProcessDateTime);
        final Map<CoreHub, NetPositionHistory> coreHubNphs = mapCoreHubsToNetPositionHistories(npHistory);
        final Set<NetPositionHistory> npHistoryToSave = new HashSet<>();
        coreHubNphs.forEach((ch, nph) -> {
            updateMarketPositionValues(marketPoints, ch, nph, npHistoryToSave);
        });
        netPositionHistoryService.saveAll(npHistoryToSave);
        return coreHubNphs;
    }

    private static void updateMarketPositionValues(final ReferenceProgram marketPoints,
                                                   final CoreHub coreHub,
                                                   final NetPositionHistory netPositionHistory,
                                                   final Set<NetPositionHistory> npHistoryToSave) {
        final double marketPos = marketPoints.getGlobalNetPosition(new EICode(coreHub.country()));
        if(netPositionHistory.getMaximumNetPosition() < marketPos) {
            netPositionHistory.setMaximumNetPosition(marketPos);
        }
        if(netPositionHistory.getMinimumNetPosition() > marketPos) {
            netPositionHistory.setMinimumNetPosition(marketPos);
        }
        npHistoryToSave.add(netPositionHistory);
    }

    private Map<CoreHub, NetPositionHistory> mapCoreHubsToNetPositionHistories(final Set<NetPositionHistory> npHistory) {
        Map<CoreHub, NetPositionHistory> coreHubsNph = new HashMap<>();
        coreHubs.forEach(coreHub -> coreHubsNph.put(coreHub, npHistory.stream()
                        .filter(nph -> nph.getHubRamcep2Code().equals(coreHub.ramcep2Code()))
                        .findFirst()
                        .orElseThrow(() -> new CoreValidIntradayInvalidDataException(String.format("CoreHub configuration for net position history missing for hub : {}", coreHub.name())))));
        return coreHubsNph;
    }

    private Map<CoreHub, Pair<Double, Double>>  mapCoreHubsToLoadAndGeneration(final Network network) {
        Map<CoreHub, Pair<Double, Double>> gLByCoreHub = new HashMap<>();
        coreHubs.stream()
                .filter(coreHub -> !coreHub.isHvdcHub())
                .forEach(coreHub -> gLByCoreHub.put(coreHub, Pair.of(network.)));

        return gLByCoreHub;

    }

    private Double getLoadForHub(final Network network, final CoreHub coreHub) {
        network.getSubnetwork()
    }

    private boolean isListSmallerThanMax(final List<Vertex> vertices, final int maxSelectedVertices) {
        return vertices == null || vertices.size() < maxSelectedVertices;
    }

}
