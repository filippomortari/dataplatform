package com.db.dataplatform.techtest.client.component.impl;

import com.db.dataplatform.techtest.client.api.model.DataEnvelope;
import com.db.dataplatform.techtest.client.api.model.PushResponse;
import com.db.dataplatform.techtest.client.component.Client;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Client code does not require any test coverage
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientImpl implements Client {

    public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
    public static final UriTemplate URI_GETDATA = new UriTemplate("http://localhost:8090/dataserver/data/{blockType}");
    public static final UriTemplate URI_PATCHDATA = new UriTemplate("http://localhost:8090/dataserver/update/{name}/{newBlockType}");

    private final RestTemplate clientRestTemplate;

    @Override
    public boolean pushData(DataEnvelope dataEnvelope) {
        log.info("Pushing data {} to {}", dataEnvelope.getDataHeader().getName(), URI_PUSHDATA);
        try {
            PushResponse pushResponse = clientRestTemplate.postForObject(URI_PUSHDATA, dataEnvelope, PushResponse.class);
            return Optional
                    .ofNullable(pushResponse)
                    .map(PushResponse::getChecksumPass)
                    .orElse(false);
        } catch (RestClientException e) {
            log.warn("Failed to push data to {}", URI_PUSHDATA, e);
            return false;
        }
    }

    @Override
    public List<DataEnvelope> getData(String blockType) {
        log.info("Query for data with header block type {}", blockType);
        final URI targetUrl = URI_GETDATA.expand(blockType);

        try {
            ResponseEntity<List<DataEnvelope>> responseEntity = clientRestTemplate.exchange(
                    targetUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<DataEnvelope>>() {});

            log.info("Received data with success");
            return responseEntity.getBody();
        } catch (RestClientException e) {
            log.warn("Failed to query for data with header block type {}", blockType, e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean updateData(String blockName, String newBlockType) {
        log.info("Updating blocktype to {} for block with name {}", newBlockType, blockName);
        try {
            final DataEnvelope dataEnvelope = clientRestTemplate.patchForObject(URI_PATCHDATA.expand(blockName, newBlockType), null, DataEnvelope.class);
            log.info("Successfully updated blocktype to {} for block with name {}", newBlockType, blockName);
            return Objects.nonNull(dataEnvelope);
        } catch (RestClientException e) {
            log.warn("Failed to update blocktype to {} for block with name {}", newBlockType, blockName, e);
            return false;
        }
    }


}
