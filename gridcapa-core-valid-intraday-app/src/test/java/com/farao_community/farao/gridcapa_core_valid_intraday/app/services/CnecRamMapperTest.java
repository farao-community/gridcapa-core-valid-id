/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecRamBranchData;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.FlowBasedDomainDocument;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URL;
import java.util.List;

@SpringBootTest
class CnecRamMapperTest {

    @Autowired
    private FileImporter fileImporter;

    @Test
    void mapCnecRamToBranchesOK() {
        final CoreValidIntradayFileResource cnecRamFile = createFileResource("cnecRam", getClass().getResource("/20250921-0000-FID2-645-INIT_VIRG_REFBAL_PRES_FBPARAMS-v3.xml"));
        final FlowBasedDomainDocument flowBasedDomainDocument = fileImporter.importCnecRamFile(cnecRamFile);
        Assertions.assertThat(flowBasedDomainDocument).isNotNull();
        final List<CnecRamBranchData> cnecRamBranchData = CnecRamMapper.mapCnecRamToBranches(flowBasedDomainDocument);
        Assertions.assertThat(cnecRamBranchData)
                .isNotEmpty()
                .hasSize(1);
        final CnecRamBranchData cnecRamBranchDataFirst = cnecRamBranchData.getFirst();
        Assertions.assertThat(cnecRamBranchDataFirst.amr())
                .isEqualTo(cnecRamBranchDataFirst.getAmr())
                .isEqualTo(0);
        Assertions.assertThat(cnecRamBranchDataFirst.branchId())
                .isEqualTo("CCCCCCCCCCCC");
        Assertions.assertThat(cnecRamBranchDataFirst.getRam0Core())
                .isEqualTo(cnecRamBranchDataFirst.ram0Core())
                .isEqualTo(100);
    }

    @Test
    void mapCnecRamToBranchesException() {
        Assertions.assertThatExceptionOfType(CoreValidIntradayInvalidDataException.class)
                .isThrownBy(() -> CnecRamMapper.mapCnecRamToBranches(null))
                .withMessage("Failed to map CnecRam data to branch data")
                .withCauseInstanceOf(NullPointerException.class);
    }

    private CoreValidIntradayFileResource createFileResource(final String filename, final URL resource) {
        return new CoreValidIntradayFileResource(filename, resource.toExternalForm());
    }
}
