package com.db.dataplatform.techtest.server.component;

import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;

import java.util.List;
import java.util.Optional;

public interface Server {
    boolean saveDataEnvelope(DataEnvelope envelope);

    List<DataEnvelope> getAllDataByBlockType(BlockTypeEnum blockType);

    Optional<DataEnvelope> updateBlockType(String name, BlockTypeEnum newBlockType);
}
