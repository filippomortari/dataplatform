package com.db.dataplatform.techtest.api.controller;

import com.db.dataplatform.techtest.TestDataHelper;
import com.db.dataplatform.techtest.server.api.controller.ServerController;
import com.db.dataplatform.techtest.server.api.model.DataBody;
import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.api.model.DataHeader;
import com.db.dataplatform.techtest.server.component.DataLakeDispatcher;
import com.db.dataplatform.techtest.server.exception.HadoopClientException;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Optional;

import static com.db.dataplatform.techtest.TestDataHelper.TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(MockitoJUnitRunner.class)
public class ServerControllerComponentTest {

	public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
	public static final UriTemplate URI_GETDATA = new UriTemplate("http://localhost:8090/dataserver/data/{blockType}");
	public static final UriTemplate URI_PATCHDATA = new UriTemplate("http://localhost:8090/dataserver/update/{name}/{newBlockType}");

	@Mock
	private Server serverMock;
	@Mock
	private DataLakeDispatcher dataLakeDispatcher;

	private DataEnvelope testDataEnvelope;
	private ObjectMapper objectMapper;
	private MockMvc mockMvc;
	private ServerController serverController;

	@Before
	public void setUp() throws HadoopClientException {
		serverController = new ServerController(serverMock, dataLakeDispatcher);
		mockMvc = standaloneSetup(serverController).build();
		objectMapper = Jackson2ObjectMapperBuilder
				.json()
				.build();

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
	public void getData_typical() throws Exception {
		// GIVEN
		given(serverMock.getAllDataByBlockType(any())).willReturn(Collections.singletonList(testDataEnvelope));

		// WHEN, THEN
		mockMvc.perform(get(URI_GETDATA.expand(BlockTypeEnum.BLOCKTYPEA.name()))
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].dataHeader.name").value(TEST_NAME))
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
}
