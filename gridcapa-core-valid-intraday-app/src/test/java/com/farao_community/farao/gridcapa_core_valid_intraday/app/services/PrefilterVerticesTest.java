/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.NetPositionHistory;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.Season;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.repositories.NetPositionHistoryRepository;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@SpringBootTest
class PrefilterVerticesTest {

    @Autowired
    private PrefilterVertices prefilterVertices;

    @Autowired
    private FileImporter fileImporter;

    @Autowired
    private CoreHubsConfiguration coreHubsConfiguration;

    @Autowired
    private NetPositionHistoryRepository netPositionHistoryRepository;

    private static final OffsetDateTime TEST_DATE_TIME = OffsetDateTime.parse("2021-07-22T22:30Z");

    @BeforeEach
    void initDatabase() {
        netPositionHistoryRepository.deleteAll();
        final List<CoreHub> coreHubs = coreHubsConfiguration.getCoreHubs();
        netPositionHistoryRepository.saveAll(createNphsFromSeason(Season.SUMMER, coreHubs));
    }

    @Test
    void prefilterVerticesTest() {

        //less vertices than max selected vertices: should return entry
        final List<Vertex> testVertices = getTestVertices();
        final List<Vertex> verticesResult1 = prefilterVertices.prefilterVertices(TEST_DATE_TIME, getTestRefProg(), getTestEmptyNetwork(), testVertices, 0.0, 5);
        Assertions.assertThat(verticesResult1)
                .isEqualTo(testVertices);

        //first prefilter ko: no season data
        Assertions.assertThatExceptionOfType(CoreValidIntradayInvalidDataException.class)
                .isThrownBy(() -> prefilterVertices.prefilterVertices(OffsetDateTime.parse("2021-12-31T22:30Z"), getTestRefProg(), getTestEmptyNetwork(), testVertices, 0.0, 3))
                .withMessage("CoreHub configuration for net position history missing for hub : Belgique");

        //first prefilter ok but second prefilter no generators
        Assertions.assertThatExceptionOfType(CoreValidIntradayInvalidDataException.class)
                .isThrownBy(() -> prefilterVertices.prefilterVertices(TEST_DATE_TIME, getTestRefProg(), getTestEmptyNetwork(), testVertices, 0.0, 3))
                .withMessage("No generation on network for hub : Belgique");

        //first prefilter ok but second prefilter no loads
        Assertions.assertThatExceptionOfType(CoreValidIntradayInvalidDataException.class)
                .isThrownBy(() -> prefilterVertices.prefilterVertices(TEST_DATE_TIME, getTestRefProg(), getTestEmptyNetworkWithCountryGeneraor(Country.BE), testVertices, 0.0, 3))
                .withMessage("No load on network for hub : Belgique");

        //both filters ok but less than max selected vertices param
        final List<Vertex> verticesResult2 = prefilterVertices.prefilterVertices(TEST_DATE_TIME, getTestRefProg(), getTestNetwork(), testVertices, 0.0, 5);
        Assertions.assertThat(verticesResult2)
                .isEqualTo(testVertices);

        //both filters ok
        final List<Vertex> verticesResult3 = prefilterVertices.prefilterVertices(TEST_DATE_TIME, getTestRefProg(), getTestNetwork(), testVertices, 0.0, 3);
        Assertions.assertThat(verticesResult3)
                .hasSize(4);
        Assertions.assertThat(verticesResult3.stream().map(Vertex::vertexId).toList())
                .containsExactly(1, 2, 3, 7);
    }

    private List<Vertex> getTestVertices() {
        final CoreValidIntradayFileResource verticesFile = createFileResource("vertex", getClass().getResource("/fake-vertice-PrefilterTest.csv"));
        return fileImporter.importVertices(verticesFile);
    }

    private ReferenceProgram getTestRefProg() {
        final CoreValidIntradayFileResource refProgFile = createFileResource("refprog", getClass().getResource("/20210723-FID2-632-v2-10V1001C--00264T-to-10V1001C--00085T.xml"));
        return fileImporter.importReferenceProgram(refProgFile, TEST_DATE_TIME);
    }

    private Network getTestNetwork() {
        final Network network = mock(Network.class);
        final List<Generator> gens = coreHubsConfiguration.getCoreHubs()
                .stream()
                .map(coreHub -> getGenerator(coreHub.country()))
                .toList();

        final List<Load> loads = coreHubsConfiguration.getCoreHubs()
                .stream()
                .map(coreHub -> getLoad(coreHub.country()))
                .toList();

        when(network.getGeneratorStream())
                .thenAnswer((Answer<Stream<Generator>>) invocation -> Stream.of(gens.toArray(new Generator[0])));
        when(network.getLoadStream())
                .thenAnswer((Answer<Stream<Load>>) invocation -> Stream.of(loads.toArray(new Load[0])));
        return network;
    }

    private Network getTestEmptyNetworkWithCountryGeneraor(Country country) {
        final Network network = mock(Network.class);
        final Generator generator = getGenerator(country);
        when(network.getGeneratorStream()).thenReturn(Stream.of(generator));
        return network;
    }

    private static Generator getGenerator(final Country country) {
        final Generator generator = mock(Generator.class);
        when(generator.getMaxP()).thenReturn(2000.0);
        final Substation substation = mock(Substation.class);
        when(substation.getNullableCountry()).thenReturn(country);
        final VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        final Terminal terminal = mock(Terminal.class);
        when(terminal.isConnected()).thenReturn(true);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(generator.getTerminal()).thenReturn(terminal);
        return generator;
    }

    private static Load getLoad(final Country country) {
        final Load load = mock(Load.class);
        when(load.getP0()).thenReturn(500.0);
        final Substation substation = mock(Substation.class);
        when(substation.getNullableCountry()).thenReturn(country);
        final VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        final Terminal terminal = mock(Terminal.class);
        when(terminal.isConnected()).thenReturn(true);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(load.getTerminal()).thenReturn(terminal);
        return load;
    }

    private Network getTestEmptyNetwork() {
        return mock(Network.class);
    }

    //TODO refactor into utils test class
    private CoreValidIntradayFileResource createFileResource(final String filename,
                                                             final URL resource) {
        return new CoreValidIntradayFileResource(filename, resource.toExternalForm());
    }

    Set<NetPositionHistory> createNphsFromSeason(final Season season,
                                                 final List<CoreHub> coreHubs) {
        return coreHubs.stream()
                .map(ch -> createNphFromSeasonAndCode(ch.ramcep2Code(), season))
                .collect(Collectors.toSet());
    }

    NetPositionHistory createNphFromSeasonAndCode(final String ramcep2Code,
                                                  final Season season) {
        final NetPositionHistory nph = new NetPositionHistory();
        nph.setId(UUID.randomUUID());
        nph.setSeason(season);
        nph.setHubRamcep2Code(ramcep2Code);
        nph.setMinimumNetPosition(-401.0);
        nph.setMaximumNetPosition(999.0);
        return nph;
    }
}
