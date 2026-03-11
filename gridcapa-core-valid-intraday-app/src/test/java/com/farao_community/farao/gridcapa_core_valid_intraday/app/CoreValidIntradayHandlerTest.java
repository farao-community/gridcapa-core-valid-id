/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileImporter;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@SpringBootTest
class CoreValidIntradayHandlerTest {

    @MockitoBean
    private FileImporter fileImporter;

    @Autowired
    private CoreValidIntradayHandler coreValidIntradayHandler;

    @Test
    void handleCoreValidIntradayRequestTestImportOcappiThenException() {
        CoreValidIntradayRequest request = Mockito.mock(CoreValidIntradayRequest.class);
        Mockito.when(request.getTimestamp()).thenReturn(OffsetDateTime.now());
        Mockito.when(request.getOcappiMarketPoint()).thenReturn(new CoreValidIntradayFileResource("test_file_name", "test_file_url"));
        Mockito.when(fileImporter.importReferenceProgram(Mockito.any(), Mockito.any())).thenReturn(new ReferenceProgram(List.of()));
        Mockito.when(fileImporter.importAggregatedScheduleFile(Mockito.any(), Mockito.any())).thenReturn(BigDecimal.ZERO);
        Assertions.assertThatExceptionOfType(CoreValidIntradayInvalidDataException.class).isThrownBy(() -> coreValidIntradayHandler.handleCoreValidIntradayRequest(request));
        Mockito.verify(fileImporter).importCnecRamFile(Mockito.any());
        Mockito.verify(fileImporter).importVertices(Mockito.any());
        Mockito.verify(fileImporter).importNetwork(Mockito.any());
        Mockito.verify(fileImporter).importGlskFile(Mockito.any());
        Mockito.verify(fileImporter).importMergedCnec(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(fileImporter).importAggregatedScheduleFile(Mockito.any(), Mockito.any());
    }

    @Test
    void handleCoreValidIntradayRequestTestImportRefProgThenException() {
        CoreValidIntradayRequest request = Mockito.mock(CoreValidIntradayRequest.class);
        Mockito.when(request.getTimestamp()).thenReturn(OffsetDateTime.now());
        Assertions.assertThatExceptionOfType(CoreValidIntradayInvalidDataException.class).isThrownBy(() -> coreValidIntradayHandler.handleCoreValidIntradayRequest(request));
        Mockito.verify(fileImporter).importCnecRamFile(Mockito.any());
        Mockito.verify(fileImporter).importVertices(Mockito.any());
        Mockito.verify(fileImporter).importNetwork(Mockito.any());
        Mockito.verify(fileImporter).importGlskFile(Mockito.any());
        Mockito.verify(fileImporter).importMergedCnec(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(fileImporter).importReferenceProgram(Mockito.any(), Mockito.any());
    }
}
