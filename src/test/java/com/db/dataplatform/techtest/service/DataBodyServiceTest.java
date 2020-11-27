package com.db.dataplatform.techtest.service;

import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.persistence.repository.DataStoreRepository;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import com.db.dataplatform.techtest.server.service.impl.DataBodyServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.db.dataplatform.techtest.TestDataHelper.createTestDataBodyEntity;
import static com.db.dataplatform.techtest.TestDataHelper.createTestDataHeaderEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DataBodyServiceTest {

    public static final String TEST_NAME_NO_RESULT = "TestNoResult";

    @Mock
    private DataStoreRepository dataStoreRepositoryMock;

    private DataBodyService dataBodyService;
    private DataBodyEntity expectedDataBodyEntity;

    @Before
    public void setup() {
        DataHeaderEntity testDataHeaderEntity = createTestDataHeaderEntity(Instant.now());
        expectedDataBodyEntity = createTestDataBodyEntity(testDataHeaderEntity);

        dataBodyService = new DataBodyServiceImpl(dataStoreRepositoryMock);
    }

    @Test
    public void shouldSaveDataBodyEntityAsExpected(){
        // WHEN
        dataBodyService.saveDataBody(expectedDataBodyEntity);

        // THEN
        verify(dataStoreRepositoryMock, times(1))
                .save(eq(expectedDataBodyEntity));
    }

    @Test
    public void shouldGetDataByBlockType(){
        // GIVEN
        final BlockTypeEnum blockType = BlockTypeEnum.BLOCKTYPEA;
        given(dataStoreRepositoryMock.findAllByBlockType(any())).willReturn(Collections.singletonList(expectedDataBodyEntity));

        // WHEN
        final List<DataBodyEntity> result = dataBodyService.getDataByBlockType(blockType);

        // THEN
        verify(dataStoreRepositoryMock, times(1))
                .findAllByBlockType(eq(blockType));
        assertThat(result).containsExactlyInAnyOrder(expectedDataBodyEntity);
    }

    @Test
    public void shouldGetDataByBlockName(){
        // GIVEN
        final String blockName = "blockName";
        given(dataStoreRepositoryMock.findByDataHeaderBlockName(any())).willReturn(Optional.of(expectedDataBodyEntity));

        // WHEN
        final Optional<DataBodyEntity> dataByBlockName = dataBodyService.getDataByBlockName(blockName);

        // THEN
        verify(dataStoreRepositoryMock, times(1))
                .findByDataHeaderBlockName(eq(blockName));
        assertThat(dataByBlockName).isNotEmpty();
        assertThat(dataByBlockName.get()).isEqualTo(expectedDataBodyEntity);
    }
}
