package com.db.dataplatform.techtest.server.persistence.repository;

import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.db.dataplatform.techtest.TestDataHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DataStoreRepositoryTest {

    @Autowired
    private DataStoreRepository dataStoreRepository;

    @After
    public void tearDown() {
        dataStoreRepository.deleteAll();
    }

    @Test
    public void findAllByBlockType() {
        // GIVEN
        DataHeaderEntity testDataHeaderEntity = createTestDataHeaderEntity(Instant.now());
        DataBodyEntity testDataBodyEntity = createTestDataBodyEntity(testDataHeaderEntity);
        dataStoreRepository.save(testDataBodyEntity);

        // WHEN
        final List<DataBodyEntity> allByBlockType = dataStoreRepository.findAllByBlockType(BlockTypeEnum.BLOCKTYPEA);

        // THEN
        assertThat(allByBlockType).containsExactlyInAnyOrder(testDataBodyEntity);
    }

    @Test
    public void findByDataHeaderBlockName() {
        // GIVEN
        DataHeaderEntity testDataHeaderEntity = createTestDataHeaderEntity(Instant.now());
        DataBodyEntity testDataBodyEntity = createTestDataBodyEntity(testDataHeaderEntity);
        dataStoreRepository.save(testDataBodyEntity);

        // WHEN
        final Optional<DataBodyEntity> result = dataStoreRepository.findByDataHeaderBlockName(TEST_NAME);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testDataBodyEntity);
    }
}