/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

class CnecRamBranchDataTest {

    private static final String TEST_ID = "testId";
    private static final int RAM_0_CORE = 100;
    private static final int AMR = 200;

    @Test
    void testCnecRamBranchData() {
        Map<String, BigDecimal> ptdfs = Map.of("AA", new BigDecimal(0.00123), "BB", new BigDecimal(0.00456), "CC", new BigDecimal(0.00789));
        CnecRamBranchData cnecRamBranchData = new CnecRamBranchData(TEST_ID, RAM_0_CORE, AMR, ptdfs);
        Assertions.assertThat(cnecRamBranchData).isNotNull();
        Assertions.assertThat(cnecRamBranchData.branchId())
                .isEqualTo(TEST_ID);
        Assertions.assertThat(cnecRamBranchData.ram0Core())
                .isEqualTo(cnecRamBranchData.getRam0Core())
                .isEqualTo(RAM_0_CORE);
        Assertions.assertThat(cnecRamBranchData.amr())
                .isEqualTo(cnecRamBranchData.getAmr())
                .isEqualTo(AMR);
        Assertions.assertThat(cnecRamBranchData.getPtdfValues())
                .isEqualTo(cnecRamBranchData.ptdfValues())
                .isEqualTo(ptdfs);
    }
}