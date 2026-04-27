/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecRamBranchData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecVertexRamData;
import com.powsybl.iidm.network.Country;
import com.powsybl.openrao.commons.EICode;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceExchangeData;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VerticesSelectorTest {
    private final VerticesSelector selector = new VerticesSelector(new TestCoreHubConf());

    @Test
    void shouldSelectWithNSphere() {

        final List<Vertex> twoVertices = selector.selectVerticesWithinNSphere(getTestVertices(),
                                                                              getTestRefProg(),
                                                                              10.0,
                                                                              3);

        final List<Vertex> fourVerticesReducedToThree = selector.selectVerticesWithinNSphere(getTestVertices(),
                                                                                             getTestRefProg(),
                                                                                             1000.0,
                                                                                             3);

        assertThat(getIds(twoVertices)).containsExactlyInAnyOrder(1, 5);

        assertThat(getIds(fourVerticesReducedToThree)).containsExactlyInAnyOrder(1, 4, 5);
    }

    @Test
    void shouldSelectClosest() {
        final List<Vertex> selectedVertices = selector.selectClosestVertices(getTestVertices(), getTestRefProg(), 2);

        assertThat(getIds(selectedVertices)).containsExactlyInAnyOrder(1, 5);
    }

    @Test
    void shouldSelectConstrained() {
        final CnecRamBranchData cnec1 = new CnecRamBranchData("id1", 1450, 157, Map.of("fb1", BigDecimal.valueOf(.3345), "fb2", BigDecimal.valueOf(0.156), "fb3", BigDecimal.valueOf(.78)));
        final CnecRamBranchData cnec2 = new CnecRamBranchData("id2", 3450, 257, Map.of("fb1", BigDecimal.valueOf(.345), "fb2", BigDecimal.valueOf(0.256), "fb3", BigDecimal.valueOf(.78)));

        final int nbVertices = 3;
        final List<CnecVertexRamData> selectedVertices = selector.selectConstrainedVertices(getTestVertices2(), List.of(cnec1, cnec2), nbVertices);
        assertThat(selectedVertices).hasSize(nbVertices);
        assertThat(selectedVertices.getFirst().ram()).isLessThanOrEqualTo(selectedVertices.getLast().ram());
        assertThat(selectedVertices.getFirst().ram()).isEqualTo(251);

    }

    private ReferenceProgram getTestRefProg() {
        // net positions : AA = -300 , BB = 600, CC = -300
        return new TestRefProg();
    }

    private static List<Integer> getIds(final List<Vertex> vertices) {
        return vertices.stream().map(Vertex::vertexId).toList();
    }

    private static class TestRefProg extends ReferenceProgram {
        public TestRefProg(final List<ReferenceExchangeData> referenceExchangeDataList) {
            super(referenceExchangeDataList);
        }

        public TestRefProg() {
            super(new ArrayList<>());
        }

        @Override
        public double getGlobalNetPosition(final EICode area) {
            return (area.equals(new EICode(Country.FR)) || area.equals(new EICode(Country.BE))) ? -300.0 : 600.0;
        }
    }

    private static class TestCoreHubConf extends CoreHubsConfiguration {
        public TestCoreHubConf() {
            // test class
        }

        @Override
        public List<CoreHub> getCoreHubs() {
            return List.of(
                new CoreHub("Test1", "ram1", "fb1", "FR-CORE", "AA", false, 1),
                new CoreHub("Test2", "ram2", "fb2", "DE-CORE", "BB", false, 1),
                new CoreHub("Test3", "ram3", "fb3", "BE-CORE", "CC", false, 1)
            );
        }
    }

    private List<Vertex> getTestVertices() {
        return List.of(new Vertex(1, Map.of("AA", -301, "BB", 600, "CC", -300)),
                       new Vertex(2, Map.of("AA", 300, "BB", -600, "CC", -300)),
                       new Vertex(3, Map.of("AA", 300, "BB", 600, "CC", -300)),
                       new Vertex(4, Map.of("AA", -350, "BB", 600, "CC", -300)),
                       new Vertex(5, Map.of("AA", -299, "BB", 600, "CC", -300)));
    }


    private List<Vertex> getTestVertices2() {
        return List.of(new Vertex(1, Map.of("AA", -500, "BB", 400, "CC", 300)),
                       new Vertex(2, Map.of("AA", 1300, "BB", -1600, "CC", 1300)),
                       new Vertex(3, Map.of("AA", 200, "BB", 100, "CC", 50)),
                       new Vertex(4, Map.of("AA", -350, "BB", 600, "CC", 300)),
                       new Vertex(5, Map.of("AA", -299, "BB", 600, "CC", 300)));
    }

}
