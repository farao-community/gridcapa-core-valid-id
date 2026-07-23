/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.FlowBasedDomainDocument;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import java.io.InputStream;

/**
 * @author Amira Kahya {@literal <amira.kahya at rte-france.com>}
 */
public final class CnecRamImporter {

    private CnecRamImporter() {
        // utility class
    }

    public static FlowBasedDomainDocument importCnecRam(final InputStream inputStream) {
        try {
            final JAXBContext context = JAXBContext.newInstance(FlowBasedDomainDocument.class);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            return (FlowBasedDomainDocument) unmarshaller.unmarshal(inputStream);
        } catch (final Exception e) {
            throw new CoreValidIntradayInvalidDataException("Cannot unmarshal FlowBasedDomainDocument", e);
        }
    }
}
