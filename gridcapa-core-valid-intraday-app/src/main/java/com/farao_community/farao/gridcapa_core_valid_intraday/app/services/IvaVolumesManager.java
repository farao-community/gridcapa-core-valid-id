/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.ConstResultType;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.CriticalBranchType;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.FlowBasedDomainDocument;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.FlowBasedDomainType;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.IntervalType;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;

public class IvaVolumesManager {
    private static final String FR = "FR";
    private static final String FRENCH_TSO_EIC = "10YFR-RTE------C";
    private static final double FRM_MARGIN_PERCENTAGE = 0.05;

    private final List<Vertex> vertices;
    private final BigDecimal frenchMarketGlobalNetPosition;
    private final Map<String, BigDecimal> ptdfsZsByBranch;
    private final List<CriticalBranchType> criticalBranches;

    public IvaVolumesManager(final List<Vertex> vertices,
                             final ReferenceProgram refProg,
                             final Map<String, BigDecimal> ptdfsZsByBranch,
                             final FlowBasedDomainDocument fbDomainDoc) {
        this.vertices = vertices;
        // global = in regard to the whole zone, France excluded
        this.frenchMarketGlobalNetPosition = BigDecimal.valueOf(refProg.getGlobalNetPosition(FRENCH_TSO_EIC));
        this.ptdfsZsByBranch = ptdfsZsByBranch;

        this.criticalBranches = fbDomainDoc
            .getFlowBasedDomainTimeSeries()
            .stream()
            .flatMap(ts -> ts.getPeriod().stream())
            .flatMap(pt -> pt.getInterval().stream())
            .flatMap(IvaVolumesManager::getCriticalBranchesFromInterval)
            .toList();
    }

    public Map<String, BigDecimal> computeIvaVolumes(final double riskMarginInMW, final RaoService raoService, final BigDecimal minRamMcccPercent) {
        final Map<String, BigDecimal> idToIva = new HashMap<>();
        final BigDecimal margin = BigDecimal.valueOf(riskMarginInMW);

        for (final CriticalBranchType branch : this.criticalBranches) {
            final BigDecimal frm = getFrm(branch);
            final BigDecimal frmWithRisk =  frm.add(margin);

            final BigDecimal iva = this.vertices.stream()
                .filter(v -> getMarginFromMarket(branch, v).compareTo(frmWithRisk) >= 0) // margin > FRM
                .map(v -> raoService.computeIvaVolume(branch, v))
                .max(BigDecimal::compareTo)
                .orElse(ZERO);

            final BigDecimal branchIvaMax = getBranchIvaMax(branch, frm, minRamMcccPercent);
            idToIva.put(branch.getId(), iva.min(branchIvaMax));
        }

        return idToIva;
    }

    private BigDecimal getBranchIvaMax(final CriticalBranchType branch, final BigDecimal frm, final BigDecimal minRamMcccPercent) {
        return BigDecimal.valueOf(branch.getFMax())
                         .multiply(BigDecimal.ONE.subtract(minRamMcccPercent))
                         .subtract(frm)
                         .subtract(BigDecimal.valueOf(branch.getF0Core()))
                         //AMR and CVA are still not available in file, when they will be, uncomment
                         //.add(BigDecimal.valueOf(branch.getAmr()))
                         //.add(BigDecimal.valueOf(branch.getCva()))
                         .setScale(0, RoundingMode.HALF_EVEN)
                         .max(ZERO);
    }

    private static Stream<CriticalBranchType> getCriticalBranchesFromInterval(final IntervalType interval) {
        return interval.getFlowBasedDomain().stream()
            .flatMap(IvaVolumesManager::getConstraintResultStream)
            .map(ConstResultType::getCriticalBranch)
            .filter(IvaVolumesManager::isFrenchOrigin);
    }

    private static Stream<ConstResultType> getConstraintResultStream(final FlowBasedDomainType domain) {

        if (domain.getConstraintResults() == null || domain.getConstraintResults().getConstraintResult() == null) {
            return Stream.empty();
        } else {
            return domain.getConstraintResults().getConstraintResult().stream();
        }
    }

    private static boolean isFrenchOrigin(final CriticalBranchType criticalBranch) {
        return FR.equals(criticalBranch.getTsoOrigin());
    }

    /**
     * ∆F∆NP = PTDFZS * (NP_Xi – NP_RefProg), the flow discrepancy caused by the shift from RefProg to the vertex Xi
     *
     * @param criticalBranch the critical branch for which we want to select the Zone-To-Slack PTDF
     * @param vertexNP       the vertex net position (NP_Xi)
     * @return ∆F∆NP
     */
    private BigDecimal getFlowGap(final CriticalBranchType criticalBranch,
                                  final BigDecimal vertexNP) {
        return ptdfsZsByBranch.getOrDefault(criticalBranch.getId(), ZERO)
            .multiply(vertexNP.subtract(frenchMarketGlobalNetPosition));
    }

    /**
     * RAM_Xi,CNEi = RAM_RefProg,CNEi – ∆F∆NP(NP_Xi)
     *
     * @param criticalBranch the branch for which we want to calculate the margin
     * @param vertex         the vertex to use for calculation
     * @return RAM_Xi,CNEi
     */
    private BigDecimal getMarginFromMarket(final CriticalBranchType criticalBranch,
                                           final Vertex vertex) {
        final BigDecimal ramRefProg = BigDecimal.valueOf(criticalBranch.getFMax()
                                                         - criticalBranch.getFRef()
                                                         - criticalBranch.getFrmMw());

        final BigDecimal frenchPosVertex = BigDecimal.valueOf(Optional.ofNullable(vertex.coordinates().get(FR))
                                                                  .orElseThrow());

        return ramRefProg.subtract(getFlowGap(criticalBranch, frenchPosVertex));
    }

    private static BigDecimal getFrm(final CriticalBranchType criticalBranch) {
        return BigDecimal.valueOf(FRM_MARGIN_PERCENTAGE)
            .multiply(BigDecimal.valueOf(criticalBranch.getFMax()));
    }

}
