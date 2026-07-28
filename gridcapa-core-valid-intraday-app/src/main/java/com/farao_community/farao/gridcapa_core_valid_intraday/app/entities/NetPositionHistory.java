/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(indexes = { @Index(columnList = "hubRamcep2Code", name = "season") })
public class NetPositionHistory {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "hubRamcep2Code", nullable = false)
    private String hubRamcep2Code;

    @Column(name = "season", nullable = false)
    private Season season;

    @Column(name = "minimumNetPosition", nullable = false)
    private double minimumNetPosition;

    @Column(name = "maximumNetPosition", nullable = false)
    private double maximumNetPosition;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getHubRamcep2Code() {
        return hubRamcep2Code;
    }

    public void setHubRamcep2Code(final String hubRamcep2Code) {
        this.hubRamcep2Code = hubRamcep2Code;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(final Season season) {
        this.season = season;
    }

    public double getMinimumNetPosition() {
        return minimumNetPosition;
    }

    public void setMinimumNetPosition(final double minimumNetPosition) {
        this.minimumNetPosition = minimumNetPosition;
    }

    public double getMaximumNetPosition() {
        return maximumNetPosition;
    }

    public void setMaximumNetPosition(final double maximumNetPosition) {
        this.maximumNetPosition = maximumNetPosition;
    }
}
