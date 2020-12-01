package com.db.dataplatform.techtest.api.controller;

import com.db.dataplatform.techtest.TestDataHelper;
import com.db.dataplatform.techtest.server.api.controller.ServerController;
import com.db.dataplatform.techtest.server.api.model.DataBody;
import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.api.model.DataHeader;
import com.db.dataplatform.techtest.server.component.DataLakeDispatcher;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.exception.HadoopClientException;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.internal.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriTemplate;

import java.util.Collections;
import java.util.Optional;

import static com.db.dataplatform.techtest.TestDataHelper.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * I translated this into a spring test.
 * IMHO it's important to verify controller responsibilities in tandem with validators (as validation is done before the call to the controller)
 * and controller advice
 * */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ServerController.class)
public class ServerControllerComponentTest {

	public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
	public static final UriTemplate URI_GETDATA = new UriTemplate("http://localhost:8090/dataserver/data/{blockType}");
	public static final UriTemplate URI_PATCHDATA = new UriTemplate("http://localhost:8090/dataserver/update/{name}/{newBlockType}");

	@MockBean
	private Server serverMock;
	@MockBean
	private DataLakeDispatcher dataLakeDispatcher;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MockMvc mockMvc;

	private DataEnvelope testDataEnvelope;

	@Before
	public void setUp() throws HadoopClientException {
		testDataEnvelope = TestDataHelper.createTestDataEnvelopeApiObject();
	}

	@Test
	public void pushData_typical() throws Exception {
		// GIVEN
		String testDataEnvelopeJson = objectMapper.writeValueAsString(testDataEnvelope);
		given(serverMock.saveDataEnvelope(any(DataEnvelope.class))).willReturn(true);

		// WHEN, THEN
		mockMvc.perform(post(URI_PUSHDATA)
				.content(testDataEnvelopeJson)
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.checksumPass").value(true))
				.andReturn();

		verify(serverMock, times(1)).saveDataEnvelope(eq(testDataEnvelope));
		verify(dataLakeDispatcher, times(1)).dispatch(eq(testDataEnvelope));
	}

	@Test
	public void pushData_invalid_input() throws Exception {
		// GIVEN
		String jsonWithNullFields = objectMapper.writeValueAsString(new DataEnvelope(new DataHeader(null, BlockTypeEnum.BLOCKTYPEA), new DataBody(null, null)));

		// WHEN, THEN
		mockMvc.perform(post(URI_PUSHDATA)
				.content(jsonWithNullFields)
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest())
				.andReturn();

		verifyNoInteractions(serverMock);
		verifyNoInteractions(dataLakeDispatcher);
	}

	@Test
	public void pushData_invalid_input_empty_field() throws Exception {
		// GIVEN
		String jsonWithNullFields = objectMapper.writeValueAsString(new DataEnvelope(new DataHeader(TEST_NAME, BlockTypeEnum.BLOCKTYPEA), new DataBody("", "")));

		// WHEN, THEN
		mockMvc.perform(post(URI_PUSHDATA)
				.content(jsonWithNullFields)
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest())
				.andDo(print())
				.andReturn();

		verifyNoInteractions(serverMock);
		verifyNoInteractions(dataLakeDispatcher);

	}

	@Test
	public void getData_typical() throws Exception {
		// GIVEN
		given(serverMock.getAllDataByBlockType(any())).willReturn(Collections.singletonList(testDataEnvelope));

		// WHEN, THEN
		mockMvc.perform(get(URI_GETDATA.expand(BlockTypeEnum.BLOCKTYPEA.name()))
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].dataHeader.name").value(TEST_NAME))
				.andExpect(jsonPath("$[0].dataHeader.blockType").value(BlockTypeEnum.BLOCKTYPEA.name()))
				.andExpect(jsonPath("$[0].dataBody.dataBody").value(DUMMY_DATA))
				.andExpect(jsonPath("$[0].dataBody.dataMD5Checksum").value(DUMMY_DATA_CHECKSUM))
				.andReturn();

		verify(serverMock, times(1)).getAllDataByBlockType(eq(BlockTypeEnum.BLOCKTYPEA));
		verifyNoInteractions(dataLakeDispatcher);

	}

	@Test
	public void getData_invalid_input() throws Exception {
		// WHEN, THEN
		mockMvc.perform(get(URI_GETDATA.expand("foo"))
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest())
				.andReturn();

		verifyNoInteractions(serverMock);
		verifyNoInteractions(dataLakeDispatcher);
	}

	@Test
	public void patchData_typical() throws Exception {
		// GIVEN
		given(serverMock.updateBlockType(any(), any())).willReturn(Optional.of(testDataEnvelope));

		// WHEN, THEN
		mockMvc.perform(patch(URI_PATCHDATA.expand(TEST_NAME, BlockTypeEnum.BLOCKTYPEB.name()))
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.dataHeader.name").value(TEST_NAME))
				.andExpect(jsonPath("$.dataHeader.blockType").value(BlockTypeEnum.BLOCKTYPEA.name()))
				.andExpect(jsonPath("$.dataBody.dataBody").value(DUMMY_DATA))
				.andExpect(jsonPath("$.dataBody.dataMD5Checksum").value(DUMMY_DATA_CHECKSUM))
				.andReturn();

		verify(serverMock, times(1)).updateBlockType(eq(TEST_NAME), eq(BlockTypeEnum.BLOCKTYPEB));
		verifyNoInteractions(dataLakeDispatcher);
	}

	@Test
	public void patchData_item_not_found() throws Exception {
		// GIVEN
		given(serverMock.updateBlockType(any(), any())).willReturn(Optional.empty());

		// WHEN, THEN
		mockMvc.perform(patch(URI_PATCHDATA.expand(TEST_NAME, BlockTypeEnum.BLOCKTYPEB.name()))
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNotFound())
				.andReturn();

		verify(serverMock, times(1)).updateBlockType(eq(TEST_NAME), eq(BlockTypeEnum.BLOCKTYPEB));
		verifyNoInteractions(dataLakeDispatcher);
	}

	@Test
	public void patchData_invalid_name() throws Exception {
		// GIVEN
		given(serverMock.updateBlockType(any(), any())).willReturn(Optional.of(testDataEnvelope));

		final String longString = RandomString.make(40);

		// WHEN, THEN
		mockMvc.perform(patch(URI_PATCHDATA.expand(longString, BlockTypeEnum.BLOCKTYPEB.name()))
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isConflict())
				.andReturn();

		verifyNoInteractions(serverMock);
		verifyNoInteractions(dataLakeDispatcher);
	}
}
