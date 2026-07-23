/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.farao_community.farao.gridcapa_core_valid_intraday.api.resource;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.OffsetDateTimeDeserializer;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.OffsetDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_externe at rte-france.com>}
 */
@Type("core-valid-intraday-request")
public class CoreValidIntradayRequest {
    @Id
    private final String id;
    private final String currentRunId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private final OffsetDateTime timestamp;
    private final CoreValidIntradayFileResource cnecRam;
    private final CoreValidIntradayFileResource vertices;
    private final CoreValidIntradayFileResource cgm;
    private final CoreValidIntradayFileResource glsk;
    private final CoreValidIntradayFileResource mergedCnec;
    private final CoreValidIntradayFileResource marketPoint;
    private final CoreValidIntradayFileResource ocappiMarketPoint;
    private final CoreValidIntradayFileResource pra;
    private final boolean launchedAutomatically;

    @JsonCreator
    public CoreValidIntradayRequest(final @JsonProperty("id") String id,
                                    final @JsonProperty("currentRunId") String currentRunId,
                                    final @JsonProperty("timestamp") OffsetDateTime timestamp,
                                    final @JsonProperty("cnecRam") CoreValidIntradayFileResource cnecRam,
                                    final @JsonProperty("vertices") CoreValidIntradayFileResource vertices,
                                    final @JsonProperty("cgm") CoreValidIntradayFileResource cgm,
                                    final @JsonProperty("glsk") CoreValidIntradayFileResource glsk,
                                    final @JsonProperty("mergedCnec") CoreValidIntradayFileResource mergedCnec,
                                    final @JsonProperty("marketPoint") CoreValidIntradayFileResource marketPoint,
                                    final @JsonProperty("ocappiMarketPoint") CoreValidIntradayFileResource ocappiMarketPoint,
                                    final @JsonProperty("pra") CoreValidIntradayFileResource pra,
                                    final @JsonProperty("launchedAutomatically") boolean launchedAutomatically) {
        this.id = id;
        this.currentRunId = currentRunId;
        this.timestamp = timestamp;
        this.cnecRam = cnecRam;
        this.vertices = vertices;
        this.cgm = cgm;
        this.glsk = glsk;
        this.mergedCnec = mergedCnec;
        this.marketPoint = marketPoint;
        this.ocappiMarketPoint = ocappiMarketPoint;
        this.pra = pra;
        this.launchedAutomatically = launchedAutomatically;
    }

    public CoreValidIntradayRequest(final String id,
                                    final String currentRunId,
                                    final OffsetDateTime timestamp,
                                    final CoreValidIntradayFileResource cnecRam,
                                    final CoreValidIntradayFileResource vertices,
                                    final CoreValidIntradayFileResource cgm,
                                    final CoreValidIntradayFileResource glsk,
                                    final CoreValidIntradayFileResource mergedCnec,
                                    final CoreValidIntradayFileResource marketPoint,
                                    final CoreValidIntradayFileResource ocappiMarketPoint,
                                    final CoreValidIntradayFileResource pra) {
        this(id, currentRunId, timestamp, cnecRam, vertices, cgm, glsk, mergedCnec, marketPoint, ocappiMarketPoint, pra, false);
    }

    public String getId() {
        return id;
    }

    public String getCurrentRunId() {
        return currentRunId;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public CoreValidIntradayFileResource getCnecRam() {
        return cnecRam;
    }

    public CoreValidIntradayFileResource getVertices() {
        return vertices;
    }

    public CoreValidIntradayFileResource getCgm() {
        return cgm;
    }

    public CoreValidIntradayFileResource getGlsk() {
        return glsk;
    }

    public CoreValidIntradayFileResource getMergedCnec() {
        return mergedCnec;
    }

    public CoreValidIntradayFileResource getMarketPoint() {
        return marketPoint;
    }

    public CoreValidIntradayFileResource getOcappiMarketPoint() {
        return ocappiMarketPoint;
    }

    public CoreValidIntradayFileResource getPra() {
        return pra;
    }

    public boolean getLaunchedAutomatically() {
        return launchedAutomatically;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
