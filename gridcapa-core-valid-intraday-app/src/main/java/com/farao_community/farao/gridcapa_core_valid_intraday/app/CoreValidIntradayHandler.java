/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app;

import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileImporter;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.FlowBasedDomainDocument;
import com.powsybl.glsk.api.GlskDocument;
import com.powsybl.iidm.network.Network;
import com.powsybl.openrao.data.crac.io.fbconstraint.FbConstraintCreationContext;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_externe at rte-france.com>}
 */
@Component
public class CoreValidIntradayHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreValidIntradayHandler.class);
    private static final DateTimeFormatter ARTIFACTS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm");

    private final Logger eventsLogger;
    private final FileImporter fileImporter;
    private final MinioAdapter minioAdapter;

    public CoreValidIntradayHandler(final FileImporter fileImporter, final MinioAdapter minioAdapter, final Logger eventsLogger) {
        this.fileImporter = fileImporter;
        this.minioAdapter = minioAdapter;
        this.eventsLogger = eventsLogger;
    }

    public String handleCoreValidIntradayRequest(final CoreValidIntradayRequest coreValidIntradayRequest) {
        setUpEventLogging(coreValidIntradayRequest);
        final OffsetDateTime targetProcessDateTime = coreValidIntradayRequest.getTimestamp();
        final String formattedTimestamp = TIMESTAMP_FORMATTER.format(targetProcessDateTime);
        //TODO import stuff
        final FlowBasedDomainDocument flowBasedDomainDocument = fileImporter.importCnecRamFile(coreValidIntradayRequest.getCnecRam());
        final List<Vertex> importedVertices = fileImporter.importVertices(coreValidIntradayRequest.getVertices());
        final Network network = fileImporter.importNetwork(coreValidIntradayRequest.getCgm());
        final GlskDocument glskDocument = fileImporter.importGlskFile(coreValidIntradayRequest.getGlsk());
        final FbConstraintCreationContext fbConstraintCreationContext = fileImporter.importMergedCnec(coreValidIntradayRequest.getMergedCnec(), network, targetProcessDateTime);
        final ReferenceProgram referenceProgram = fileImporter.importReferenceProgram(coreValidIntradayRequest.getMarketPoint(), targetProcessDateTime);
        final Map<String, BigDecimal> ocappiMaketPoints = coreValidIntradayRequest.getOcappiMarketPoint() != null
                ? fileImporter.importAggregatedScheduleFile(coreValidIntradayRequest.getOcappiMarketPoint(), targetProcessDateTime)
                : Map.of();

        //TODO calculate IVA stuff

        //TODO output IVAs
        return coreValidIntradayRequest.getId();
    }

    private static void setUpEventLogging(final CoreValidIntradayRequest coreValidIntradayRequest) {
        MDC.put("gridcapa-task-id", coreValidIntradayRequest.getId());
    }

}
