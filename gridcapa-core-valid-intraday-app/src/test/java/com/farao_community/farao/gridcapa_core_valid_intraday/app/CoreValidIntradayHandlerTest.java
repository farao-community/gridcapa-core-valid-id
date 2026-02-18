package com.farao_community.farao.gridcapa_core_valid_intraday.app;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileImporter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;

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