/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
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
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecVertexRam;
import com.powsybl.iidm.network.Country;
import com.powsybl.openrao.commons.EICode;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparingDouble;

public class VerticesSelector {
    private final List<CoreHub> coreHubs;

    public VerticesSelector(final CoreHubsConfiguration coreHubsConfiguration) {
        this.coreHubs = Collections.unmodifiableList(coreHubsConfiguration.getCoreHubs());
    }

    /**
     *
     * @param projectedVertices all considered vertices
     * @param referenceProgram  contains the market positions
     * @param radius            the n-sphere radius
     * @param nbVertices        how many vertices do we want
     * @return selected vertices with n-sphere method
     */
    public List<Vertex> selectVerticesWithinNSphere(final List<Vertex> projectedVertices,
                                                    final ReferenceProgram referenceProgram,
                                                    final double radius,
                                                    final int nbVertices) {

        if (projectedVertices.size() <= nbVertices) {
            return projectedVertices;
        }

        final List<Vertex> verticesInSphere = projectedVertices
            .stream()
            .filter(vertex -> isInNSphere(vertex, referenceProgram, radius))
            .toList();

        if (verticesInSphere.isEmpty()) {
            return selectClosestVertices(projectedVertices, referenceProgram, nbVertices);
        } else if (verticesInSphere.size() <= nbVertices) {
            return verticesInSphere;
        } else {
            // too many vertices, we filter again
            return selectClosestVertices(verticesInSphere, referenceProgram, nbVertices);
        }

    }

    /**
     * @param projectedVertices all considered vertices
     * @param referenceProgram  contains the market positions
     * @param n                 is how many vertices we want to select
     * @return the nth vertices closest to the global market position
     */
    public List<Vertex> selectClosestVertices(final List<Vertex> projectedVertices,
                                              final ReferenceProgram referenceProgram,
                                              final int n) {

        if (projectedVertices.size() <= n) {
            return projectedVertices;
        }

        return projectedVertices.stream()
            .map(v -> vertexAndMarketDistance(referenceProgram, v))
            .sorted(comparingDouble(Pair::getRight))
            .limit(n)
            .map(Pair::getLeft)
            .toList();

    }

    /**
     *
     * @param vertices              all considered vertices
     * @param cnecRamBranchDatas    all considered CNECs
     * @param nbVertices            the maximum number of constrained vertices to return
     * @return the list of constrained vertices with the most constrained CNEC and its calculated constrained RAM
     */
    public List<CnecVertexRam> selectConstrainedVertices(final List<Vertex> vertices,
                                                         final List<CnecRamBranchData> cnecRamBranchDatas,
                                                         final int nbVertices) {

        final Map<String, String> flowBasedToVertexCodeMap = CoreHubUtils.getFlowBasedToVertexCodeMap(coreHubs);
        final List<CnecVertexRam> constrainedOrderedVertices = new ArrayList<>();
        for (final Vertex vertex : vertices) {
            final List<CnecVertexRam> vertexRamsByCnec = new ArrayList<>();
            for (final CnecRamBranchData branch : cnecRamBranchDatas) {
                final BigDecimal cnecVertexFlow = VerticesUtils.f0Core(vertex, branch, flowBasedToVertexCodeMap);
                if (cnecVertexFlow.compareTo(BigDecimal.ZERO) > 0) {
                    final BigDecimal cnecVerticeRam = BigDecimal.valueOf(branch.getRam0Core()).subtract(cnecVertexFlow);
                    vertexRamsByCnec.add(new CnecVertexRam(branch, vertex, cnecVerticeRam.setScale(0, RoundingMode.HALF_EVEN).intValue()));
                }
            }
            //for a given vertex get the lowest ram giving the most constrained CNEC
            constrainedOrderedVertices.add(vertexRamsByCnec.stream()
                                                           .sorted(Comparator.comparingInt(CnecVertexRam::ram))
                                                           .findFirst()
                                                           .orElseThrow(
                                                               () -> new CoreValidIntradayInvalidDataException(
                                                                       String.format("Impossible to find worse CNEC for vertex id %s", vertex.vertexId())
                                                               )
                                                           )
            );
        }
        return constrainedOrderedVertices.stream()
                                         .sorted(Comparator.comparingInt(CnecVertexRam::ram))
                                         .limit(nbVertices)
                                         .toList();
    }

    /**
     * @param vertex           the considered vertex
     * @param referenceProgram contains the market positions
     * @param radius           n-dimension sphere radius, input by user
     * @return if a vertex is within an n-sphere centered on market positions
     */
    private boolean isInNSphere(final Vertex vertex,
                                final ReferenceProgram referenceProgram,
                                final double radius) {
        return vertexAndMarketDistance(referenceProgram, vertex).getRight() <= radius;
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
            final Double marketPos = referenceProgram.getGlobalNetPosition(getEICodeForCoreHub(hub.forecastCode()));
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

    private EICode getEICodeForCoreHub(String hubForecastCode) {
        Country country;
        switch (hubForecastCode) {
            case "AT-CORE" : country = Country.AT;
                break;
            case "ALBE-CORE", "BE-CORE" : country =  Country.BE;
                break;
            case "CZ-CORE" : country =  Country.CZ;
                break;
            case "ALDE-CORE", "DE-CORE" : country = Country.DE;
                break;
            case "FR-CORE" : country =  Country.FR;
                break;
            case "HR-CORE" : country =  Country.HR;
                break;
            case "HU-CORE" : country =  Country.HU;
                break;
            case "NL-CORE" : country =  Country.NL;
                break;
            case "PL-CORE" : country =  Country.PL;
                break;
            case "RO-CORE" : country =  Country.RO;
                break;
            case "SI-CORE" : country =  Country.SI;
                break;
            case "SK-CORE" : country =  Country.SK;
                break;
            default: throw new IllegalArgumentException("Unknown hubForecastCode : " + hubForecastCode + ".");
        }
        return new EICode(country);
    }

}
