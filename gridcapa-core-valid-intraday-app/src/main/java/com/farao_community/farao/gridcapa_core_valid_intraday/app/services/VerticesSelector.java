/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubUtils;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.VerticesUtils;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecRamBranchData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecVertexRamData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CoreValidIntradayTaskParameters;
import com.powsybl.openrao.commons.EICode;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparingDouble;

@Service
public class VerticesSelector {

    private static final Comparator<CnecVertexRamData> ORDER_BY_RAM = Comparator.comparingInt(CnecVertexRamData::ram);
    private static final Comparator<Map.Entry<Vertex, Double>> ORDER_MAP_ENTRY_DOUBLE = Comparator.comparingDouble(Map.Entry::getValue);
    private static final Comparator<Map.Entry<Vertex, Double>> ORDER_BY_PONDERATION = ORDER_MAP_ENTRY_DOUBLE.thenComparingInt(e -> e.getKey().vertexId());
    private final List<CoreHub> coreHubs;

    public VerticesSelector(final CoreHubsConfiguration coreHubsConfiguration) {
        this.coreHubs = Collections.unmodifiableList(coreHubsConfiguration.getCoreHubs());
    }

    /**
     * @param projectedVertices all considered vertices
     * @param referenceProgram  contains the market positions
     * @return the vertices ordered by closest to the global market position
     */
    public List<Vertex> orderByClosestVertices(final List<Vertex> projectedVertices,
                                               final ReferenceProgram referenceProgram) {

        return projectedVertices.stream()
            .map(v -> vertexAndMarketDistance(referenceProgram, v))
            .sorted(comparingDouble(Pair::getRight))
            .map(Pair::getLeft)
            .toList();

    }

    /**
     * @param projectedVertices all considered vertices
     * @param referenceProgram  contains the market positions
     * @return vertices ordered by closest to the global market position by angle
     */
    public List<Vertex> orderByClosestVerticesByAngle(final List<Vertex> projectedVertices,
                                                      final ReferenceProgram referenceProgram) {

        return projectedVertices.stream()
                .map(v -> vertexAndMarketAngleDistance(referenceProgram, v))
                .sorted((p1, p2) -> p2.getRight().compareTo(p1.getRight()))
                .map(Pair::getLeft)
                .toList();
    }

    /**
     *
     * @param projectedVertices     all considered projectedVertices
     * @param cnecRamBranchDatas    all considered CNECs
     * @return the list of ordered constrained projectedVertices with the most constrained CNEC and its calculated constrained RAM
     */
    public List<CnecVertexRamData> orderByConstrainedVertices(final List<Vertex> projectedVertices,
                                                              final List<CnecRamBranchData> cnecRamBranchDatas) {

        final Map<String, String> flowBasedToVertexCodeMap = CoreHubUtils.getFlowBasedToVertexCodeMap(coreHubs);
        final List<CnecVertexRamData> constrainedOrderedVertices = new ArrayList<>();
        for (final Vertex vertex : projectedVertices) {
            final List<CnecVertexRamData> vertexRamsByCnec = new ArrayList<>();
            for (final CnecRamBranchData branch : cnecRamBranchDatas) {
                final BigDecimal cnecVertexFlow = VerticesUtils.f0Core(vertex, branch, flowBasedToVertexCodeMap);
                if (cnecVertexFlow.compareTo(BigDecimal.ZERO) > 0) {
                    final BigDecimal cnecVerticeRam = BigDecimal.valueOf(branch.getRam0Core()).subtract(cnecVertexFlow);
                    vertexRamsByCnec.add(new CnecVertexRamData(branch, vertex, cnecVerticeRam.setScale(0, RoundingMode.HALF_EVEN).intValue()));
                }
            }
            //for a given vertex get the lowest ram giving the most constrained CNEC
            if (!vertexRamsByCnec.isEmpty()) {
                final CnecVertexRamData minRamCnec = vertexRamsByCnec.stream()
                        .min(ORDER_BY_RAM)
                        .orElseThrow(
                                () -> new CoreValidIntradayInvalidDataException(
                                        String.format("Impossible to find worse CNEC for vertex id %s", vertex.vertexId())
                                )
                        );
                constrainedOrderedVertices.add(minRamCnec);
            }
        }
        return constrainedOrderedVertices.stream()
                                         .sorted(ORDER_BY_RAM)
                                         .toList();
    }

    /**
     *
     * @param projectedVertices                  the list of projected vertices
     * @param marketPoints                       the market points
     * @param cnecRamBranchData                  the cnec ram branches
     * @param coreValidIntradayTaskParameters    the application parameters
     * @return The ordered list of maxSelectedVertices vertices through ponderated selection
     */
    public List<Vertex> selectionSynthesis(List<Vertex> projectedVertices,
                                           ReferenceProgram marketPoints,
                                           List<CnecRamBranchData> cnecRamBranchData,
                                           CoreValidIntradayTaskParameters coreValidIntradayTaskParameters) {

        final List<Vertex> closestSelection = orderByClosestVertices(projectedVertices, marketPoints);
        final List<Vertex> angleSelection = orderByClosestVerticesByAngle(projectedVertices, marketPoints);
        final List<CnecVertexRamData> constrainedSelection = orderByConstrainedVertices(projectedVertices, cnecRamBranchData);

        final Map<Vertex, Double> vertexIdToPonderation = new HashMap<>();
        final int ponderationClosest = coreValidIntradayTaskParameters.getPonderationClosest();
        fillVertexPonderationMap(closestSelection, ponderationToQuotient(ponderationClosest), vertexIdToPonderation);
        final int ponderationAngle = coreValidIntradayTaskParameters.getPonderationAngle();
        fillVertexPonderationMap(angleSelection, ponderationToQuotient(ponderationAngle), vertexIdToPonderation);
        final List<Vertex> constrainedVertices = constrainedSelection.stream()
                .map(CnecVertexRamData::vertex)
                .toList();
        final int ponderationConstrained = coreValidIntradayTaskParameters.getPonderationConstrained();
        fillVertexPonderationMap(constrainedVertices, ponderationToQuotient(ponderationConstrained), vertexIdToPonderation);
        return vertexIdToPonderation.entrySet().stream()
                .sorted(ORDER_BY_PONDERATION)
                .limit(coreValidIntradayTaskParameters.getMaxSelectedVertices())
                .map(Map.Entry::getKey)
                .toList();
    }

    private double ponderationToQuotient(final int ponderation) {
        final double pond = ponderation;
        return pond / 100.0;
    }

    private void fillVertexPonderationMap(final List<Vertex> vertexList, final double ponderation, final Map<Vertex, Double> ponderationMap) {
        for (int index = 0; index < vertexList.size(); index++) {
            final Vertex vertex = vertexList.get(index);
            if (ponderationMap.containsKey(vertex)) {
                ponderationMap.put(vertex, index * ponderation + ponderationMap.get(vertex));
            } else {
                ponderationMap.put(vertex, index * ponderation);
            }
        }
    }

    /**
     * we return a pair because we want to be able to sort by distance but still keep the vertex data
     *
     * @param referenceProgram contains the market positions
     * @param vertex           the considered vertex
     * @return the vertex and its distance from the market
     */
    private Pair<Vertex, Double> vertexAndMarketDistance(final ReferenceProgram referenceProgram,
                                                         final Vertex vertex) {

        final Map<String, Integer> vertexPositions = vertex.coordinates();

        // global distance² = sum_over_hub(k_hub * [1D distance]²)
        double sumOfWeightedSquared = 0.0;
        for (final CoreHub hub : coreHubs) {
            final Double marketPos = referenceProgram.getGlobalNetPosition(new EICode(hub.country()));
            final Integer vertexPos = vertexPositions.get(hub.clusterVerticeCode());

            if (vertexPos == null) {
                throw new IllegalStateException(
                    String.format("Vertex %d missing required coordinate for hub %s / %s",
                                  vertex.vertexId(), hub.forecastCode(), hub.clusterVerticeCode()));
            }

            final double distanceIn1D = marketPos - vertexPos;
            final double weightedDistance = hub.coefficient() * distanceIn1D * distanceIn1D;
            sumOfWeightedSquared = sumOfWeightedSquared + weightedDistance;
        }

        return Pair.of(vertex, Math.sqrt(sumOfWeightedSquared));
    }

    /**
     * we return a pair because we want to be able to sort by distance but still keep the vertex data
     *
     * @param referenceProgram contains the market positions
     * @param vertex           the considered vertex
     * @return the vertex and its distance from the market by direction and angle (uses cosinus)
     */
    private Pair<Vertex, Double> vertexAndMarketAngleDistance(final ReferenceProgram referenceProgram,
                                                              final Vertex vertex) {

        final Map<String, Integer> vertexPositions = vertex.coordinates();

        // angle's cosinus = (scalar product of refprog . vertex) divided by (euclidian norm of refprog by vertex)
        double scalarProduct = 0.0;
        double normVertex = 0.0;
        double normMarket = 0.0;
        for (final CoreHub hub : coreHubs) {
            final Double marketPos = referenceProgram.getGlobalNetPosition(new EICode(hub.country()));
            final Integer vertexPos = vertexPositions.get(hub.clusterVerticeCode());

            if (vertexPos == null) {
                throw new IllegalStateException(
                        String.format("Vertex %d missing required coordinate for hub %s / %s",
                                      vertex.vertexId(), hub.forecastCode(), hub.clusterVerticeCode()));
            }
            scalarProduct += marketPos * vertexPos;
            normMarket += marketPos * marketPos;
            final double vertexPosition = vertexPos;
            normVertex += vertexPosition * vertexPosition;
        }
        final double cosinus = scalarProduct / (Math.sqrt(normMarket) * Math.sqrt(normVertex));

        return Pair.of(vertex, cosinus);
    }
}
