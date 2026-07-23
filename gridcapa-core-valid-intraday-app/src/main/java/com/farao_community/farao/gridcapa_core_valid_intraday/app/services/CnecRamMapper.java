/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecRamBranchData;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.ConstResultType;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.CriticalBranchType;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.FlowBasedDomainDocument;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CnecRamMapper {

    private CnecRamMapper() {
        // utility class
    }

    public static List<CnecRamBranchData> mapCnecRamToBranches(final FlowBasedDomainDocument flowBasedDomainDocument) {
        try {
            return flowBasedDomainDocument.getFlowBasedDomainTimeSeries().getFirst()
                    .getPeriod().getFirst()
                    .getInterval().getFirst()
                    .getFlowBasedDomain().getFirst()
                    .getConstraintResults().getConstraintResult().stream()
                    .map(CnecRamMapper::getCnecRamBranchData).toList();
        } catch (final Exception e) {
            throw new CoreValidIntradayInvalidDataException("Failed to map CnecRam data to branch data", e);
        }
    }

    private static CnecRamBranchData getCnecRamBranchData(final ConstResultType constraintResult) {
        final Map<String, BigDecimal> ptdfs = constraintResult.getPtdfs().getPtdf().stream()
                .collect(Collectors.toMap(
                        p -> p.getHub().getName(),
                        p -> BigDecimal.valueOf(p.getValue())
                ));
        final CriticalBranchType cb = constraintResult.getCriticalBranch();
        return new CnecRamBranchData(cb.getId(), getIntValue(cb.getRAM0Core()), getIntValue(cb.getAmr()), ptdfs);
    }

    private static int getIntValue(final Float nullableFloatValue) {
        return nullableFloatValue != null
                ? nullableFloatValue.intValue()
                : 0;
    }

}
