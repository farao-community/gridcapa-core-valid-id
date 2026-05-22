/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.farao_community.farao.gridcapa_core_valid_commons.vertex;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.exception.CoreValidCommonsInvalidDataException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.math.RoundingMode.HALF_EVEN;

class VerticesUtilsTest {

    private final List<CoreHub> coreHubs = getTestCoreHubs();

    @Test
    void importVertices() throws IOException {
        try (final InputStream inputStream = getClass().getResource("vertices.csv").openStream()) {
            final List<Vertex> vertices = VerticesUtils.importVertices(inputStream, coreHubs);
            Assertions.assertThat(vertices)
                    .isNotNull()
                    .hasSize(4)
                    .first()
                    .hasFieldOrPropertyWithValue("vertexId", 1);
            final Map<String, Integer> entries = Map.of(
                    "AA", 11,
                    "BB", 111,
                    "CC", 1111,
                    "D_D", 1
            );
            final Map<String, Integer> coordinates = vertices.getFirst().coordinates();
            Assertions.assertThat(coordinates)
                    .hasSize(4)
                    .containsAllEntriesOf(entries);
            final Vertex vertex3 = vertices.get(3);
            Assertions.assertThat(vertex3.vertexId()).isEqualTo(4);
            final Map<String, Integer> entries3 = Map.of(
                    "AA", 66,
                    "BB", 666,
                    "CC", 6666,
                    "D_D", 0
            );
            final Map<String, Integer> coordinates3 = vertex3.coordinates();
            Assertions.assertThat(coordinates3)
                    .hasSize(4)
                    .containsAllEntriesOf(entries3);
        }
    }

    @Test
    void importVerticesWithException() throws IOException {
        try (final InputStream inputStream = Mockito.mock(InputStream.class)) {
            Assertions.assertThatExceptionOfType(CoreValidCommonsInvalidDataException.class)
                    .isThrownBy(() -> VerticesUtils.importVertices(inputStream, coreHubs))
                    .withMessage("Exception occurred while parsing vertices file");
        }
    }

    @Test
    void importEmptyNonHvdcVerticesWithException() throws IOException {
        try (final InputStream inputStream = getClass().getResource("bad-vertices.csv").openStream()) {
            Assertions.assertThatExceptionOfType(CoreValidCommonsInvalidDataException.class)
                    .isThrownBy(() -> VerticesUtils.importVertices(inputStream, coreHubs))
                    .withMessage("Exception occurred while parsing vertices file");
        }
    }

    @Test
    void projectVertices() {
        final List<TestBranch> branches = getTestBranches();
        final Vertex vertex1 = getTestVertex1();
        final Vertex vertex2 = getTestVertex2();

        //fcore v1, b1 = 0.1*2000+0.2*-1000+0.3*500 = 150
        //      => delta = 1
        //fcore v1, b2  = fcore v1, b1 = 150
        //      => delta = 2/3
        final List<Vertex> projected = VerticesUtils.getVerticesProjectedOnDomain(List.of(vertex1), branches, getTestCoreHubs());
        vertex1.coordinates()
                .forEach((countryCode, baseValue) ->
                                 Assertions.assertThat(projected.getFirst().coordinates())
                                         .containsEntry(countryCode, BigDecimal.valueOf(2.0 / 3.0)
                                                 .multiply(BigDecimal.valueOf(baseValue)).setScale(0, HALF_EVEN).intValue()));

        //fcore v2 = 0.1*1000+0.2*-1000+0.3*500 = 50
        //      => delta = 3 or 2 => no change
        final List<Vertex> unprojected = VerticesUtils.getVerticesProjectedOnDomain(List.of(vertex2), branches, getTestCoreHubs());
        Assertions.assertThat(unprojected.getFirst().coordinates())
                .containsExactlyInAnyOrderEntriesOf(vertex2.coordinates());
        //fcore = 0 => still no change
        final List<Vertex> unprojected2 = VerticesUtils.getVerticesProjectedOnDomain(List.of(vertex2), getTestBranchWithZeros(), getTestCoreHubs());
        Assertions.assertThat(unprojected2.getFirst().coordinates())
                .containsExactlyInAnyOrderEntriesOf(vertex2.coordinates());
    }

    private List<CoreHub> getTestCoreHubs() {
        return List.of(
                new CoreHub("Test1", "ram1", "fb1", "fc1", "AA", false, false, 1),
                new CoreHub("Test2", "ram2", "fb2", "fc2", "BB", false, false, 1),
                new CoreHub("Test3", "ram3", "fb3", "fc3", "CC", false, false, 1),
                new CoreHub("Test4", "ram4", "fb4", "fc4", "D_D", true, false, 1)
        );
    }

    private Vertex getTestVertex1() {
        return new Vertex(1, Map.of("AA", 2000, "BB", -1000, "CC", 500));
    }

    private Vertex getTestVertex2() {
        return new Vertex(1, Map.of("AA", 1000, "BB", -1000, "CC", 500));
    }

    private List<TestBranch> getTestBranches() {
        final Map<String, BigDecimal> ptdfs = Map.of(
                "fb1", BigDecimal.valueOf(0.1),
                "fb2", BigDecimal.valueOf(0.2),
                "fb3", BigDecimal.valueOf(0.3)
        );
        return List.of(
                new TestBranch(100, 50, ptdfs),
                new TestBranch(100, 0, ptdfs)
        );
    }

    private List<TestBranch> getTestBranchWithZeros() {
        final Map<String, BigDecimal> ptdfs = Map.of(
                "fb1", BigDecimal.valueOf(0),
                "fb2", BigDecimal.valueOf(0),
                "fb3", BigDecimal.valueOf(0)
        );
        return List.of(
                new TestBranch(100, 50, ptdfs),
                new TestBranch(100, 0, ptdfs)
        );
    }

    private record TestBranch(int getRam0Core, int getAmr,
                              Map<String, BigDecimal> getPtdfValues) implements FlowBasedDomainBranchData {
    }
}
