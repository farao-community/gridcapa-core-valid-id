package com.farao_community.farao.gridcapa_core_valid_intraday.app.domain;

import com.farao_community.farao.gridcapa_core_valid_commons.vertex.FlowBasedDomainBranchData;

import java.math.BigDecimal;
import java.util.Map;

public record CnecRamBranchData(String branchId, int ram0Core, int amr, Map<String, BigDecimal> ptdfValues) implements FlowBasedDomainBranchData {
    @Override
    public int getRam0Core() {
        return ram0Core;
    }

    @Override
    public int getAmr() {
        return amr;
    }

    @Override
    public Map<String, BigDecimal> getPtdfValues() {
        return ptdfValues;
    }
}
