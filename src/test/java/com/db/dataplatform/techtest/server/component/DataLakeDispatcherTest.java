package com.db.dataplatform.techtest.server.component;

import com.db.dataplatform.techtest.TestDataHelper;
import com.db.dataplatform.techtest.server.component.impl.HadoopDataLakeDispatcher;
import com.db.dataplatform.techtest.server.exception.HadoopClientException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpMethod.POST;

@RunWith(MockitoJUnitRunner.class)
public class DataLakeDispatcherTest {

    private DataLakeDispatcher dataLakeDispatcher;
    @Mock
    private RestTemplate restTemplate;

    private URI uri;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ResponseEntity<Void> responseEntity;

    @Before
    public void setUp() throws Exception {
        uri = new URI("http://google.com");
        dataLakeDispatcher = new HadoopDataLakeDispatcher(restTemplate, uri);
    }

    @Test
    public void shouldDispatch_and_return_true_when_HTTP_OK() throws ExecutionException, InterruptedException {

        given(restTemplate.exchange(eq(uri), eq(POST), any(), eq(Void.class))).willReturn(responseEntity);
        given(responseEntity.getStatusCode()).willReturn(HttpStatus.OK);

        // WHEN
        final CompletableFuture<Boolean> dispatch = dataLakeDispatcher.dispatch(TestDataHelper.createTestDataEnvelopeApiObject());
        assertThat(dispatch.get()).isTrue();
    }

    @Test
    public void shouldDispatch_and_return_false_when_HTTP_4xx() throws ExecutionException, InterruptedException {

        given(restTemplate.exchange(eq(uri), eq(POST), any(), eq(Void.class))).willReturn(responseEntity);
        given(responseEntity.getStatusCode()).willReturn(HttpStatus.BAD_REQUEST);

        // WHEN
        final CompletableFuture<Boolean> dispatch = dataLakeDispatcher.dispatch(TestDataHelper.createTestDataEnvelopeApiObject());
        assertThat(dispatch.get()).isFalse();
    }

    @Test
    public void shouldDispatch_and_throw_to_allow_retry_to_kick_in() throws ExecutionException, InterruptedException {

        final HttpServerErrorException httpServerErrorException = new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT);
        given(restTemplate.exchange(eq(uri), eq(POST), any(), eq(Void.class))).willThrow(httpServerErrorException);

        // WHEN, THEN
        assertThatThrownBy(() -> {
            dataLakeDispatcher.dispatch(TestDataHelper.createTestDataEnvelopeApiObject());
        }).isInstanceOf(HadoopClientException.class)
                .hasMessageContaining("Unable to push to the Hadoop Data Lake")
                .hasCause(httpServerErrorException)
        ;
    }
}