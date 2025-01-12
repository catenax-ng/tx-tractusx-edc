/*
 * Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.tractusx.edc.token;

import org.eclipse.edc.spi.iam.ClaimToken;
import org.eclipse.edc.spi.iam.IdentityService;
import org.eclipse.edc.spi.iam.TokenParameters;
import org.eclipse.edc.spi.iam.TokenRepresentation;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.TypeManager;

import java.util.Map;

import static java.lang.String.format;

/**
 * An {@link IdentityService} that will mimic the behaviour of DAPS by inserting the "referringConnector" claim into any token.
 * Please only use in testing scenarios!
 */
public class MockDapsService implements IdentityService {

    private static final String BUSINESS_PARTNER_NUMBER_CLAIM = "BusinessPartnerNumber";
    private static final String REFERRING_CONNECTOR_CLAIM = "referringConnector";
    private final String businessPartnerNumber;
    private TypeManager typeManager = new TypeManager();

    public MockDapsService(String businessPartnerNumber) {
        this.businessPartnerNumber = businessPartnerNumber;
    }

    @Override
    public Result<TokenRepresentation> obtainClientCredentials(TokenParameters parameters) {
        var token = Map.of(BUSINESS_PARTNER_NUMBER_CLAIM, businessPartnerNumber);

        TokenRepresentation tokenRepresentation = TokenRepresentation.Builder.newInstance()
                .token(typeManager.writeValueAsString(token))
                .build();
        return Result.success(tokenRepresentation);
    }

    @Override
    public Result<ClaimToken> verifyJwtToken(TokenRepresentation tokenRepresentation, String audience) {

        var token = typeManager.readValue(tokenRepresentation.getToken(), Map.class);
        if (token.containsKey(BUSINESS_PARTNER_NUMBER_CLAIM)) {
            return Result.success(ClaimToken.Builder.newInstance()
                    .claim(BUSINESS_PARTNER_NUMBER_CLAIM, token.get(BUSINESS_PARTNER_NUMBER_CLAIM))
                    .claim(REFERRING_CONNECTOR_CLAIM, token.get(BUSINESS_PARTNER_NUMBER_CLAIM)).build());
        }
        return Result.failure(format("Expected %s and %s claims, but token did not contain them", BUSINESS_PARTNER_NUMBER_CLAIM, REFERRING_CONNECTOR_CLAIM));
    }

}
