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
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CnecRamMapper {

    private CnecRamMapper() {
        // utility class
    }

    public static List<CnecRamBranchData> mapCnecRamToBranches(final FlowBasedDomainDocument  flowBasedDomainDocument) {
        try {
            return flowBasedDomainDocument.getFlowBasedDomainTimeSeries().getFirst()
                    .getPeriod().getFirst()
                    .getInterval().getFirst()
                    .getFlowBasedDomain().getFirst()
                    .getConstraintResults().getConstraintResult().stream()
                    .map(CnecRamMapper::getCnecRamBranchData).toList();
        } catch (Exception e) {
            throw new CoreValidIntradayInvalidDataException("Failed to map CnecRam data to branch data", e);
        }
    }

    private static @NotNull CnecRamBranchData getCnecRamBranchData(final ConstResultType constResultType) {
        final Map<String, BigDecimal> ptdfs = new HashMap<>();
        constResultType.getPtdfs().getPtdf().forEach(p -> ptdfs.put(p.getHub().getName(), new BigDecimal(p.getValue())));
        final CriticalBranchType cb = constResultType.getCriticalBranch();
        return new CnecRamBranchData(cb.getId(), getIntValue(cb.getRAM0Core()), getIntValue(cb.getAmr()), ptdfs);
    }

    private static int getIntValue(final Float nullableFloatValue) {
        final int intValue = nullableFloatValue != null
                ? nullableFloatValue.intValue()
                : 0;
        return intValue;
    }

}
