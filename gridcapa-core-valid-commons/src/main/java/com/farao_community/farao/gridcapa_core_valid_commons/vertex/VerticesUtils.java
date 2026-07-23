/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.farao_community.farao.gridcapa_core_valid_commons.vertex;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubUtils;
import com.farao_community.farao.gridcapa_core_valid_commons.exception.CoreValidCommonsInvalidDataException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;

public final class VerticesUtils {

    public static final String VERTEX_ID_HEADER = "Vertex ID";
    private static final int DELTA_SCALE = 15;

    private VerticesUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Vertex> importVertices(final InputStream verticesStream,
                                              final List<CoreHub> coreHubs) {
        return importVertices(new InputStreamReader(verticesStream, UTF_8), coreHubs);
    }

    public static List<Vertex> getVerticesProjectedOnDomain(final List<Vertex> baseVertices,
                                                            final List<? extends FlowBasedDomainBranchData> branchesData,
                                                            final List<CoreHub> coreHubs) {

        final Map<String, String> flowBasedToVertexCodeMap = CoreHubUtils.getFlowBasedToVertexCodeMap(coreHubs);
        final List<Vertex> newVertices = new ArrayList<>();

        for (final Vertex vertex : baseVertices) {
            BigDecimal deltaMin = ONE;
            boolean shouldProject = false;
            for (final FlowBasedDomainBranchData branch : branchesData) {
                final BigDecimal f0Core = f0Core(vertex, branch, flowBasedToVertexCodeMap);
                if (shouldComputeDelta(branch, f0Core)) {
                    final BigDecimal delta = delta(branch, f0Core);
                    if (delta != null && delta.compareTo(deltaMin) < 0) {
                        shouldProject = true;
                        deltaMin = delta;
                    }
                }
            }

            final Vertex vertexOnDomain = shouldProject ? projectedVertex(vertex, deltaMin) : vertex;

            newVertices.add(vertexOnDomain);

        }

        return newVertices;
    }

    private static boolean shouldComputeDelta(final FlowBasedDomainBranchData branch,
                                              final BigDecimal f0Core) {
        return BigDecimal.valueOf(branch.getRam0Core()).add(BigDecimal.valueOf(branch.getAmr())).subtract(f0Core).compareTo(ZERO) <= 0;
    }

    private static Vertex projectedVertex(final Vertex vertex,
                                          final BigDecimal delta) {
        final Map<String, Integer> coordinates = new HashMap<>(vertex.coordinates());
        coordinates.replaceAll((k, v) -> toProjectedPosition(v, delta));
        return new Vertex(vertex.vertexId(), coordinates);
    }

    private static List<Vertex> importVertices(final Reader reader,
                                               final List<CoreHub> coreHubs) {

        try {
            final List<Vertex> vertices = new ArrayList<>();
            final Iterable<CSVRecord> csvRecords = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            csvRecords.forEach(csvRecord -> {
                final Map<String, Integer> positions = coreHubs.stream()
                        .collect(toMap(CoreHub::clusterVerticeCode,
                                       coreHub -> getCoordinate(csvRecord, coreHub)));
                final int vertexId = Integer.parseInt(csvRecord.get(VERTEX_ID_HEADER));
                vertices.add(new Vertex(vertexId, positions));
            });

            return vertices;
        } catch (final Exception e) {
            throw new CoreValidCommonsInvalidDataException("Exception occurred while parsing vertices file", e);
        }
    }

    /**
     * Set a coordinate to zero only if it is an empty HVDC coordinate.
     * This is the only case where a coordinate should be empty or blank.
     * Otherwise, should launch an exception.
     */
    private static Integer getCoordinate(final CSVRecord csvRecord,
                                         final CoreHub corehub) {
        final String coordinateString = csvRecord.get(corehub.clusterVerticeCode());
        if (corehub.isHvdcHub() && StringUtils.isBlank(coordinateString)) {
            return 0;
        }
        try {
            return Integer.parseInt(coordinateString);
        } catch (final NumberFormatException nfe) {
            throw new CoreValidCommonsInvalidDataException("Could not parse %s as an integer".formatted(coordinateString), nfe);
        }
    }

    private static int toProjectedPosition(final Integer netPosition,
                                           final BigDecimal delta) {
        return BigDecimal.valueOf(netPosition).multiply(delta).setScale(0, HALF_EVEN).intValue();
    }

    private static BigDecimal delta(final FlowBasedDomainBranchData branchData,
                                    final BigDecimal f0Core) {

        if (ZERO.compareTo(f0Core) == 0) {
            return null;
        }

        return BigDecimal.valueOf(branchData.getAmr())
                .add(BigDecimal.valueOf(branchData.getRam0Core()))
                .divide(f0Core, DELTA_SCALE, HALF_EVEN);
    }

    public static BigDecimal f0Core(final Vertex vertex,
                                     final FlowBasedDomainBranchData branchData,
                                     final Map<String, String> fbToVertexCode) {
        //f0Core = ∑_over_hubs(PTDF*NP)
        return branchData
                .getPtdfValues()
                .entrySet()
                .stream()
                .map(ptdf -> getFlowOnHub(ptdf, vertex, fbToVertexCode))
                .reduce(BigDecimal::add)
                .orElseThrow(() -> new CoreValidCommonsInvalidDataException(
                        String.format("Cannot compute f0Core: no PTDF values provided for vertex %d",
                                      vertex.vertexId())));
    }

    private static BigDecimal getFlowOnHub(final Map.Entry<String, BigDecimal> ptdf,
                                           final Vertex vertex,
                                           final Map<String, String> fbToVertexCode) {
        final String countryCode = fbToVertexCode.get(ptdf.getKey());
        return ptdf.getValue().multiply(getNetPosition(countryCode, vertex));
    }

    private static BigDecimal getNetPosition(final String countryCode,
                                             final Vertex vertex) {
        return BigDecimal.valueOf(vertex.coordinates().getOrDefault(countryCode, 0));
    }
}
