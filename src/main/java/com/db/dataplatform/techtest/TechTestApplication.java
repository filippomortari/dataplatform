package com.db.dataplatform.techtest;

import com.db.dataplatform.techtest.client.api.model.DataBody;
import com.db.dataplatform.techtest.client.api.model.DataEnvelope;
import com.db.dataplatform.techtest.client.api.model.DataHeader;
import com.db.dataplatform.techtest.client.component.Client;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.EnableRetry;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static com.db.dataplatform.techtest.Constant.DUMMY_DATA;

@EnableRetry
@Slf4j
@SpringBootApplication
public class TechTestApplication {

	public static final String HEADER_NAME = "TSLA-USDGBP-10Y";
	public static final String MD5_CHECKSUM = "cecfd3953783df706878aaec2c22aa70";

	@Autowired
	private Client client;

	public static void main(String[] args) {
		SpringApplication.run(TechTestApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initiatePushDataFlow() {
		pushData();
		queryData();
		updateData();
	}

	private void updateData() {
		boolean success = client.updateData(HEADER_NAME, BlockTypeEnum.BLOCKTYPEB.name());
		log.info("updateData {}", success ? "successfully" : "unsuccessfully");
	}

	private void queryData() {
		List<DataEnvelope> data = client.getData(BlockTypeEnum.BLOCKTYPEA.name());
		log.info("queryData returned {} entries", data.size());
	}

	private void pushData()  {

		final DataBody dataBody = new DataBody(DUMMY_DATA, MD5_CHECKSUM);
		final DataHeader dataHeader = new DataHeader(HEADER_NAME, BlockTypeEnum.BLOCKTYPEA);
		final DataEnvelope dataEnvelope = new DataEnvelope(dataHeader, dataBody);

		client.pushData(dataEnvelope);
	}

}
