package com.db.dataplatform.techtest.api.model;

import com.db.dataplatform.techtest.server.api.model.DataBody;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.db.dataplatform.techtest.TestDataHelper.DUMMY_DATA;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DataBodyTests {

    public static final String DUMMY_DATA_CHECKSUM = DigestUtils.md5Hex(DUMMY_DATA);

    @Test
    public void assignDataBodyFieldsShouldWorkAsExpected() {
        DataBody dataBody = new DataBody(DUMMY_DATA, DUMMY_DATA_CHECKSUM);

        assertThat(dataBody).isNotNull();
        assertThat(dataBody.getDataBody()).isEqualTo(DUMMY_DATA);
        assertThat(dataBody.getDataMD5Checksum()).isEqualTo(DUMMY_DATA_CHECKSUM);
    }
}
