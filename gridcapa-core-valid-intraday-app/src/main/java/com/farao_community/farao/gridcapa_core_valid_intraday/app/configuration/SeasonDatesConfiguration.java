/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("core-valid-intraday-runner.seasons")
public class SeasonDatesConfiguration {

    private String springStartDate;
    private String summerStartDate;
    private String autumnStartDate;
    private String winterStartDate;

    public void setWinterStartDate(final String winterStartDate) {
        this.winterStartDate = winterStartDate;
    }

    public void setAutumnStartDate(final String autumnStartDate) {
        this.autumnStartDate = autumnStartDate;
    }

    public void setSummerStartDate(final String summerStartDate) {
        this.summerStartDate = summerStartDate;
    }

    public void setSpringStartDate(final String springStartDate) {
        this.springStartDate = springStartDate;
    }

    public String getSpringStartDate() {
        return springStartDate;
    }

    public String getSummerStartDate() {
        return summerStartDate;
    }

    public String getAutumnStartDate() {
        return autumnStartDate;
    }

    public String getWinterStartDate() {
        return winterStartDate;
    }
}
