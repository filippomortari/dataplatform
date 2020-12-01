package com.db.dataplatform.techtest;

import com.db.dataplatform.techtest.client.api.model.PushResponse;
import com.db.dataplatform.techtest.server.component.DataLakeDispatcher;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import static com.db.dataplatform.techtest.api.controller.ServerControllerComponentTest.URI_PUSHDATA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TechTestApplicationIT {

    @Autowired
    private RestTemplate serverRestTemplate;

    @SpyBean
    private DataLakeDispatcher dataLakeDispatcher;

    @LocalServerPort
    private int port;

    @Test
    public void should_push_data_and_dispatch_to_data_lake_and_retry_appropriately() throws InterruptedException {
        // GIVEN

        // WHEN
        PushResponse pushResponse = serverRestTemplate.postForObject(URI_PUSHDATA.replace("8090", String.valueOf(port)), TestDataHelper.createTestDataEnvelopeApiObject(), PushResponse.class);

        // THEN
        assertThat(pushResponse.getChecksumPass()).isEqualTo(true);

        Awaitility
                .await()
                .pollDelay(5000, TimeUnit.MILLISECONDS)
                .atMost(10000, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    verify(dataLakeDispatcher, atLeastOnce()).dispatch(any());
                });
    }
}