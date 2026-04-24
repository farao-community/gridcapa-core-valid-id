/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.VerticesUtils;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecRamBranchData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.CnecRamMapper;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileImporter;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.VerticesSelector;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.FlowBasedDomainDocument;
import com.powsybl.glsk.api.GlskDocument;
import com.powsybl.iidm.network.Network;
import com.powsybl.openrao.commons.EICode;
import com.powsybl.openrao.data.crac.io.fbconstraint.FbConstraintCreationContext;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_externe at rte-france.com>}
 */
@Component
public class CoreValidIntradayHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreValidIntradayHandler.class);
    private static final DateTimeFormatter ARTIFACTS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm");
    //TODO replace with parameters
    private static final int MAX_SELECTED_VERTICES = 5;
    private static final int SELECTED_CONTROL_ZONE_SIZE = 500;

    private final Logger eventsLogger;
    private final FileImporter fileImporter;
    private final MinioAdapter minioAdapter;
    private final CoreHubsConfiguration coreHubsConfiguration;

    public CoreValidIntradayHandler(final FileImporter fileImporter, final MinioAdapter minioAdapter, final Logger eventsLogger, final CoreHubsConfiguration coreHubsConfiguration) {
        this.fileImporter = fileImporter;
        this.minioAdapter = minioAdapter;
        this.eventsLogger = eventsLogger;
        this.coreHubsConfiguration = coreHubsConfiguration;
    }

    public String handleCoreValidIntradayRequest(final CoreValidIntradayRequest coreValidIntradayRequest) {
        setUpEventLogging(coreValidIntradayRequest);
        final OffsetDateTime targetProcessDateTime = coreValidIntradayRequest.getTimestamp();
        final String formattedTimestamp = TIMESTAMP_FORMATTER.format(targetProcessDateTime);
        //TODO import stuff
        final FlowBasedDomainDocument flowBasedDomainCnecRam = fileImporter.importCnecRamFile(coreValidIntradayRequest.getCnecRam());
        final List<Vertex> importedVertices = fileImporter.importVertices(coreValidIntradayRequest.getVertices());
        final Network network = fileImporter.importNetwork(coreValidIntradayRequest.getCgm());
        final GlskDocument glskDocument = fileImporter.importGlskFile(coreValidIntradayRequest.getGlsk());
        final FbConstraintCreationContext fbConstraintCreationContext = fileImporter.importMergedCnec(coreValidIntradayRequest.getMergedCnec(), network, targetProcessDateTime);
        final ReferenceProgram marketPoints = fileImporter.importReferenceProgram(coreValidIntradayRequest.getMarketPoint(), targetProcessDateTime);
        if (coreValidIntradayRequest.getOcappiMarketPoint() != null) {
            marketPoints.getAllGlobalNetPositions()
                    .put(new EICode("10YFR-RTE------C"),
                         fileImporter.importAggregatedScheduleFile(coreValidIntradayRequest.getOcappiMarketPoint(),
                                                                   targetProcessDateTime)
                                 .doubleValue());
        }
        //TODO calculate IVA stuff
        final List<CnecRamBranchData> branchesData = CnecRamMapper.mapCnecRamToBranches(flowBasedDomainCnecRam);
        List<Vertex> projectedVertices = VerticesUtils.getVerticesProjectedOnDomain(importedVertices, branchesData, coreHubsConfiguration.getCoreHubs());
        VerticesSelector verticesSelector = new VerticesSelector(coreHubsConfiguration);
        final List<Vertex> vertices = verticesSelector.selectVerticesWithinNSphere(projectedVertices, marketPoints, SELECTED_CONTROL_ZONE_SIZE, MAX_SELECTED_VERTICES);

        //TODO output IVAs
        return coreValidIntradayRequest.getId();
    }

    private static void setUpEventLogging(final CoreValidIntradayRequest coreValidIntradayRequest) {
        MDC.put("gridcapa-task-id", coreValidIntradayRequest.getId());
    }

}
