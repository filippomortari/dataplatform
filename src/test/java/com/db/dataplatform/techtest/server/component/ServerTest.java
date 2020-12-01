package com.db.dataplatform.techtest.server.component;

import com.db.dataplatform.techtest.server.api.model.DataBody;
import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.api.model.DataHeader;
import com.db.dataplatform.techtest.server.component.impl.ServerImpl;
import com.db.dataplatform.techtest.server.configuration.ServerMapperConfiguration;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.db.dataplatform.techtest.TestDataHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServerTest {

    @Mock
    private DataBodyService dataBodyServiceImplMock;

    private ModelMapper modelMapper;

    private DataBodyEntity expectedDataBodyEntity;
    private DataEnvelope testDataEnvelope;

    private Server server;

    @Before
    public void setup() {
        ServerMapperConfiguration serverMapperConfiguration = new ServerMapperConfiguration();
        modelMapper = serverMapperConfiguration.createModelMapperBean();

        testDataEnvelope = createTestDataEnvelopeApiObject();
        expectedDataBodyEntity = modelMapper.map(testDataEnvelope.getDataBody(), DataBodyEntity.class);
        expectedDataBodyEntity.setDataHeaderEntity(modelMapper.map(testDataEnvelope.getDataHeader(), DataHeaderEntity.class));

        server = new ServerImpl(dataBodyServiceImplMock, modelMapper);
    }

    @Test
    public void shouldSaveDataEnvelopeAsExpected() {
        boolean success = server.saveDataEnvelope(testDataEnvelope);

        assertThat(success).isTrue();
        verify(dataBodyServiceImplMock, times(1)).saveDataBody(eq(expectedDataBodyEntity));
    }

    @Test
    public void shouldNotSaveWhenChecksumMismatch() {
        // GIVEN
        DataEnvelope dataEnvelope = new DataEnvelope(new DataHeader(null, null), new DataBody(DUMMY_DATA, "foo"));

        // WHEN
        boolean success = server.saveDataEnvelope(dataEnvelope);

        // THEN
        assertThat(success).isFalse();
        verifyNoInteractions(dataBodyServiceImplMock);
    }

    @Test
    public void shouldGetAllDataByBlockType() {
        // GIVEN
        given(dataBodyServiceImplMock.getDataByBlockType(any()))
                .willReturn(Collections.singletonList(expectedDataBodyEntity));

        // WHEN
        final List<DataEnvelope> allDataByBlockType = server.getAllDataByBlockType(BlockTypeEnum.BLOCKTYPEA);

        // THEN
        assertThat(allDataByBlockType).containsExactlyInAnyOrder(testDataEnvelope);
        verify(dataBodyServiceImplMock, times(1)).getDataByBlockType(eq(BlockTypeEnum.BLOCKTYPEA));
    }

    @Test
    public void shouldUpdateBlockType() {
        // GIVEN
        given(dataBodyServiceImplMock.getDataByBlockName(any()))
                .willReturn(Optional.of(expectedDataBodyEntity));

        given(dataBodyServiceImplMock.saveDataBody(any()))
                .willReturn(expectedDataBodyEntity);

        // WHEN
        final Optional<DataEnvelope> dataEnvelope = server.updateBlockType(TEST_NAME, BlockTypeEnum.BLOCKTYPEB);

        // THEN
        assertThat(dataEnvelope).isPresent();
        assertThat(dataEnvelope.get().getDataHeader().getBlockType()).isEqualTo(BlockTypeEnum.BLOCKTYPEB);

        verify(dataBodyServiceImplMock, times(1)).getDataByBlockName(eq(TEST_NAME));
        ArgumentCaptor<DataBodyEntity> argumentCaptor = ArgumentCaptor.forClass(DataBodyEntity.class);
        verify(dataBodyServiceImplMock, times(1)).saveDataBody(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getDataHeaderEntity().getBlockType()).isEqualTo(BlockTypeEnum.BLOCKTYPEB);
    }
}
