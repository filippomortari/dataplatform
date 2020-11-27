package com.db.dataplatform.techtest.server.component.impl;

import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.component.DataLakeDispatcher;
import com.db.dataplatform.techtest.server.exception.HadoopClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class HadoopDataLakeDispatcher implements DataLakeDispatcher {

    private final RestTemplate serverRestTemplate;
    private final URI dataLakeEndpoint;

    public HadoopDataLakeDispatcher(
            RestTemplate serverRestTemplate,
            @Value("${application.datalake.endpoint}") URI dataLakeEndpoint) {
        this.serverRestTemplate = serverRestTemplate;
        this.dataLakeEndpoint = dataLakeEndpoint;
    }


    @Override
    @Async
    @Retryable(value = HadoopClientException.class, maxAttemptsExpression = "${application.datalake.retries}", backoff = @Backoff(delayExpression = "${application.datalake.delayMs}"))
    public CompletableFuture<Boolean> dispatch(DataEnvelope dataEnvelope) {
        try {
            HttpEntity<String> request = new HttpEntity<>(dataEnvelope.getDataBody().getDataBody(), null);
            final ResponseEntity<Void> responseEntity = serverRestTemplate.exchange(dataLakeEndpoint, HttpMethod.POST, request, Void.class);

            if(responseEntity.getStatusCode().is2xxSuccessful()){
                log.info("successfully dispatched to the Hadoop Data Lake");
                return CompletableFuture.completedFuture(true);
            } else if (responseEntity.getStatusCode().is4xxClientError()){
                log.info("failed to dispatch to the Hadoop Data Lake");
                return CompletableFuture.completedFuture(false);
            } else {
                log.info("failed to dispatch to the Hadoop Data Lake");
                throw new HadoopClientException("Unable to push to the Hadoop Data Lake");
            }
        } catch (HttpServerErrorException e) {
            log.info("failed to dispatch to the Hadoop Data Lake", e);
            throw new HadoopClientException("Unable to push to the Hadoop Data Lake", e);
        }
    }
}
