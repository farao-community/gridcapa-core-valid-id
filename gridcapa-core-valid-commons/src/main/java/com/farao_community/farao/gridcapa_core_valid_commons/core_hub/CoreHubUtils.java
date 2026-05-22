/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_commons.core_hub;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ObjectUtils.allNotNull;

public final class CoreHubUtils {
    private CoreHubUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<String, String> getFlowBasedToVertexCodeMap(final List<CoreHub> coreHubs) {
        if (coreHubs == null || coreHubs.isEmpty()) {
            return Map.of();
        }
        return coreHubs.stream()
                .filter(h -> h != null && allNotNull(h.flowbasedCode(), h.clusterVerticeCode()))
                .collect(toMap(CoreHub::flowbasedCode,
                               CoreHub::clusterVerticeCode,
                               (a, b) -> b));
    }

    public static List<CoreHub> getNonAhcCoreHubs(final List<CoreHub> coreHubs) {
        return coreHubs.stream()
                .filter(coreHub -> !coreHub.isAhcHub())
                .toList();
    }
}
