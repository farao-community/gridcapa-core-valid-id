package com.farao_community.farao.gridcapa_core_valid_commons.core_hub;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class CoreHubUtilsTest {

    @Test
    void getNonAhcCoreHubs() {
        CoreHub coreHubAhc1 = new CoreHub("AA", "AA", "AA", "AA", "AA", false, true, 0.0);
        CoreHub coreHubAhc2 = new CoreHub("BB", "BB", "BB", "BB", "BB", true, true, 0.0);
        CoreHub coreHub3 = new CoreHub("CC", "CC", "CC", "CC", "CC", true, false, 0.0);
        CoreHub coreHub4 = new CoreHub("DD", "DD", "DD", "DD", "DD", false, false, 0.0);
        List<CoreHub> coreHubs = List.of(coreHubAhc1, coreHubAhc2, coreHub3, coreHub4);
        Assertions.assertThat(CoreHubUtils.getNonAhcCoreHubs(coreHubs))
                .hasSize(2)
                .containsExactlyInAnyOrder(coreHub3, coreHub4);

    }
}
