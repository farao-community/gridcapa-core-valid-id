/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "net-position-history",
        indexes = {@Index(columnList = "hub_ramcep2_code,season", name = "net_position_history_idx")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"hub_ramcep2_code", "season"}, name = "uk-net-position-history")})
public class NetPositionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "hub_ramcep2_code", length = 9, nullable = false)
    private String hubRamcep2Code;

    @Enumerated(EnumType.STRING)
    @Column(name = "season", length = 6, nullable = false)
    private Season season;

    @Column(name = "minimum_net_position", nullable = false)
    private double minimumNetPosition;

    @Column(name = "maximum_net_position", nullable = false)
    private double maximumNetPosition;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
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
