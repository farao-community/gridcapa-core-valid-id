/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
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
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CoreValidIntradayTaskParameters;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.CnecRamMapper;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileImporter;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.PrefilterVertices;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.VerticesSelector;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.FlowBasedDomainDocument;
import com.powsybl.glsk.api.GlskDocument;
import com.powsybl.iidm.network.Network;
import com.powsybl.openrao.commons.EICode;
import com.powsybl.openrao.data.crac.io.fbconstraint.FbConstraintCreationContext;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.slf4j.Logger;
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

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm");
    private static final String RTE_EI_CODE = "10YFR-RTE------C";

    private final FileImporter fileImporter;
    private final PrefilterVertices prefilterVertices;
    private final VerticesSelector verticesSelector;
    private final CoreHubsConfiguration coreHubsConfiguration;
    private final Logger businessLogger;

    public CoreValidIntradayHandler(final FileImporter fileImporter,
                                    final Logger businessLogger,
                                    final PrefilterVertices prefilterVertices,
                                    final VerticesSelector verticesSelector,
                                    final CoreHubsConfiguration coreHubsConfiguration) {
        this.fileImporter = fileImporter;
        this.prefilterVertices = prefilterVertices;
        this.verticesSelector = verticesSelector;
        this.coreHubsConfiguration = coreHubsConfiguration;
        this.businessLogger = businessLogger;
    }

    public String handleCoreValidIntradayRequest(final CoreValidIntradayRequest coreValidIntradayRequest) {
        setUpEventLogging(coreValidIntradayRequest);
        final OffsetDateTime targetProcessDateTime = coreValidIntradayRequest.getTimestamp();
        final String formattedTimestamp = TIMESTAMP_FORMATTER.format(targetProcessDateTime);
        final CoreValidIntradayTaskParameters coreValidIntradayTaskParameters = new CoreValidIntradayTaskParameters(coreValidIntradayRequest.getTaskParameterList());
        businessLogger.info("Starting computation of request id: {}, for timestamp: {}, task parameters are:{}", coreValidIntradayRequest.getId(), formattedTimestamp, coreValidIntradayTaskParameters.toJsonString());
        //TODO import stuff
        final FlowBasedDomainDocument flowBasedDomainCnecRam = fileImporter.importCnecRamFile(coreValidIntradayRequest.getCnecRam());
        final List<Vertex> importedVertices = fileImporter.importVertices(coreValidIntradayRequest.getVertices());
        final Network network = fileImporter.importNetwork(coreValidIntradayRequest.getCgm());
        final GlskDocument glskDocument = fileImporter.importGlskFile(coreValidIntradayRequest.getGlsk());
        final FbConstraintCreationContext fbConstraintCreationContext = fileImporter.importMergedCnec(coreValidIntradayRequest.getMergedCnec(), network, targetProcessDateTime);
        final ReferenceProgram marketPoints = fileImporter.importReferenceProgram(coreValidIntradayRequest.getMarketPoint(), targetProcessDateTime);
        if (coreValidIntradayRequest.getOcappiMarketPoint() != null) {
            marketPoints.getAllGlobalNetPositions()
                    .put(new EICode(RTE_EI_CODE),
                         fileImporter.importAggregatedScheduleFile(coreValidIntradayRequest.getOcappiMarketPoint(), targetProcessDateTime).doubleValue());
        }
        //select vertices
        final List<CnecRamBranchData> cnecRamBranchData = CnecRamMapper.mapCnecRamToBranches(flowBasedDomainCnecRam);
        final List<Vertex> projectedVertices = VerticesUtils.getVerticesProjectedOnDomain(importedVertices, cnecRamBranchData, coreHubsConfiguration.getCoreHubs());
        final List<Vertex> prefilteredVertices = prefilterVertices.prefilterVertices(targetProcessDateTime, marketPoints, network, projectedVertices, coreValidIntradayTaskParameters);
        businessLogger.info(String.format("Prefiltered Vertices are : %s", logVerticeIds(prefilteredVertices)));
        final List<Vertex> ponderatedSelection = verticesSelector.selectionSynthesis(projectedVertices, marketPoints, cnecRamBranchData, coreValidIntradayTaskParameters);
        businessLogger.info(String.format("Selected Vertices are : %s", logVerticeIds(ponderatedSelection)));
        //TODO calculate IVA stuff

        //TODO output IVAs
        return coreValidIntradayRequest.getId();
    }

    private static void setUpEventLogging(final CoreValidIntradayRequest coreValidIntradayRequest) {
        MDC.put("gridcapa-task-id", coreValidIntradayRequest.getId());
    }

    private String logVerticeIds(List<Vertex> vertices) {
        return vertices.stream().mapToInt(Vertex::vertexId).mapToObj(Integer::toString).reduce((s, s2) -> s + ", " + s2).orElse("");
    }
}
