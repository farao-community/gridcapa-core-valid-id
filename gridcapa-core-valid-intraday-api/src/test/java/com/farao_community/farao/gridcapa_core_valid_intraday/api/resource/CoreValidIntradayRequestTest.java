/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.farao_community.farao.gridcapa_core_valid_intraday.api.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_externe at rte-france.com>}
 */
class CoreValidIntradayRequestTest {

    private CoreValidIntradayFileResource cnecRam;
    private CoreValidIntradayFileResource vertices;
    private CoreValidIntradayFileResource cgm;
    private CoreValidIntradayFileResource glsk;
    private CoreValidIntradayFileResource mergedCnec;
    private CoreValidIntradayFileResource marketPoint;
    private CoreValidIntradayFileResource ocappiMarketPoint;
    private CoreValidIntradayFileResource pra;
    private OffsetDateTime dateTime;

    @BeforeEach
    void setUp() {
        cnecRam = new CoreValidIntradayFileResource("cnecRam.txt", "http://path/to/cnecRam/file");
        vertices = new CoreValidIntradayFileResource("vertices.txt", "http://path/to/vertice/file");
        cgm = new CoreValidIntradayFileResource("network.txt", "http://path/to/cgm/file");
        glsk = new CoreValidIntradayFileResource("glsk.txt", "http://path/to/glsk/file");
        mergedCnec = new CoreValidIntradayFileResource("mergedCnec.txt", "http://path/to/mergedCnec/file");
        marketPoint = new CoreValidIntradayFileResource("marketPoint.txt", "http://path/to/marketPoint/file");
        ocappiMarketPoint = new CoreValidIntradayFileResource("ocappiMarketPoint.txt", "http://path/to/ocappiMarketPoint/file");
        pra = new CoreValidIntradayFileResource("pra.txt", "http://path/to/pra/file");
        dateTime = OffsetDateTime.parse("2025-10-01T00:30Z");
    }

    @Test
    void checkManualCoreValidRequest() {
        CoreValidIntradayRequest coreValidIntradayRequest = new CoreValidIntradayRequest("id", "runId", dateTime, cnecRam, vertices, cgm, glsk, mergedCnec, marketPoint, ocappiMarketPoint, pra);
        assertNotNull(coreValidIntradayRequest);
        assertEquals("id", coreValidIntradayRequest.getId());
        assertEquals("runId", coreValidIntradayRequest.getCurrentRunId());
        assertEquals("2025-10-01T00:30Z", coreValidIntradayRequest.getTimestamp().toString());
        assertEquals("cnecRam.txt", coreValidIntradayRequest.getCnecRam().getFilename());
        assertEquals("vertices.txt", coreValidIntradayRequest.getVertices().getFilename());
        assertEquals("network.txt", coreValidIntradayRequest.getCgm().getFilename());
        assertEquals("glsk.txt", coreValidIntradayRequest.getGlsk().getFilename());
        assertEquals("mergedCnec.txt", coreValidIntradayRequest.getMergedCnec().getFilename());
        assertEquals("marketPoint.txt", coreValidIntradayRequest.getMarketPoint().getFilename());
        assertEquals("ocappiMarketPoint.txt", coreValidIntradayRequest.getOcappiMarketPoint().getFilename());
        assertEquals("pra.txt", coreValidIntradayRequest.getPra().getFilename());
        assertEquals("http://path/to/mergedCnec/file", coreValidIntradayRequest.getMergedCnec().getUrl());
        assertFalse(coreValidIntradayRequest.getLaunchedAutomatically());
    }

    @Test
    void checkAutoCoreValidRequest() {
        CoreValidIntradayRequest coreValidIntradayRequest = new CoreValidIntradayRequest("id", "runId", dateTime, cnecRam, vertices, cgm, glsk, mergedCnec, marketPoint, ocappiMarketPoint, pra, true);
        assertTrue(coreValidIntradayRequest.getLaunchedAutomatically());
    }

}
