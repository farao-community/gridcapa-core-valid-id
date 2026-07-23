/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.domain;

import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class CnecVertexRamDataTest {

    private static final int RAM = 133;

    @Test
    void testCnecVertexRamData() {
        final CnecRamBranchData cnec = new CnecRamBranchData("id", 145, 257, Map.of("AB", BigDecimal.valueOf(1, 2345), "CD", BigDecimal.valueOf(456, 78)));
        final Vertex vertex = new Vertex(57, Map.of("EF", 127, "GH", 556));
        final CnecVertexRamData tested = new CnecVertexRamData(cnec, vertex, RAM);
        assertThat(tested.cnec()).isEqualTo(cnec);
        assertThat(tested.vertex()).isEqualTo(vertex);
        assertThat(tested.ram()).isEqualTo(RAM);
    }
}
