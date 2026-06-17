/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.FlowBasedDomainDocument;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
class IvaVolumesManagerTest {

    @Autowired
    private FileImporter fileImporter;
    private static final OffsetDateTime TEST_DATE_TIME = OffsetDateTime.parse("2021-07-22T22:30Z");

    private ReferenceProgram referenceProgram;
    private FlowBasedDomainDocument flowBasedDomainDocument;

    private CoreValidIntradayFileResource createFileResource(final String filename,
                                                             final String filePath) {
        return new CoreValidIntradayFileResource(filename, getClass().getResource(filePath).toExternalForm());
    }

    @BeforeEach
    void setup() {
        final CoreValidIntradayFileResource refProgFile = createFileResource("refprog", "/20210723-FID2-632-v2-10V1001C--00264T-to-10V1001C--00085T.xml");
        referenceProgram = fileImporter.importReferenceProgram(refProgFile, TEST_DATE_TIME);
        final CoreValidIntradayFileResource cnecRamFile = createFileResource("cnecRam", "/20260119-0000-FID2-645-INIT_VIRG_REFBAL_PRES_FBPARAMS-v3.xml");
        flowBasedDomainDocument = fileImporter.importCnecRamFile(cnecRamFile);
    }

    @Test
    void shouldHaveZeroIfRaoNotCalled() {
        final IvaVolumesManager mgr = new IvaVolumesManager(List.of(mockVertex(2000)),
                                                            referenceProgram,
                                                            Map.of("CCCCCCCCCCCC", BigDecimal.valueOf(0.1)),
                                                            flowBasedDomainDocument);
        final RaoService rao = mock();
        assertThat(mgr.computeIvaVolumes(100, rao))
            .isNotEmpty()
            .containsValue(ZERO);

        verify(rao, never()).computeIvaVolume(any(), any());
    }

    @Test
    void shouldNotHaveZeroIfRaoCalled() {
        final IvaVolumesManager mgr = new IvaVolumesManager(List.of(mockVertex(-300000)),
                                                            referenceProgram,
                                                            Map.of("CCCCCCCCCCCC", BigDecimal.valueOf(1000)),
                                                            flowBasedDomainDocument);
        final RaoService rao = mock();
        Mockito.when(rao.computeIvaVolume(any(), any()))
            .thenReturn(TEN);

        assertThat(mgr.computeIvaVolumes(100, rao))
            .isNotEmpty()
            .doesNotContainValue(ZERO);

        verify(rao).computeIvaVolume(any(), any());
    }

    private Vertex mockVertex(final int frValue) {
        return new Vertex(1, Map.of("FR", frValue, "BE", -1000, "AT", 500));
    }

}
