package com.db.dataplatform.techtest.server.component.impl;

import com.db.dataplatform.techtest.server.api.model.DataBody;
import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.api.model.DataHeader;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import com.db.dataplatform.techtest.server.component.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ServerImpl implements Server {

    private final DataBodyService dataBodyServiceImpl;
    private final ModelMapper modelMapper;

    /**
     * @param envelope
     * @return true if there is a match with the client provided checksum.
     */
    @Override
    public boolean saveDataEnvelope(DataEnvelope envelope) {
        if (isValidChecksum(envelope)) {
            // Save to persistence.
            persist(envelope);
            log.info("Data persisted successfully, data name: {}", envelope.getDataHeader().getName());
            return true;
        } else {
            log.info("Won't persist data received due to MD5 checksum mismatch, data name: {}",
                    envelope.getDataHeader().getName());
            return false;
        }
    }

    @Override
    public List<DataEnvelope> getAllDataByBlockType(BlockTypeEnum blockType) {
        return dataBodyServiceImpl
                .getDataByBlockType(blockType)
                .stream()
                .map(this::adapt)
                .collect(Collectors.toList())
                ;
    }

    @Override
    public Optional<DataEnvelope> updateBlockType(String name, BlockTypeEnum newBlockType) {
        final Optional<DataBodyEntity> dataByBlockName = dataBodyServiceImpl.getDataByBlockName(name);
        return dataByBlockName
                .map(dataBodyEntity -> {
                    final DataHeaderEntity dataHeaderEntity = dataBodyEntity.getDataHeaderEntity();
                    dataHeaderEntity.setBlockType(newBlockType);
                    return saveData(dataBodyEntity);
                })
                .map(this::adapt);
    }

    private boolean isValidChecksum(DataEnvelope envelope) {
        String md5Hex = DigestUtils.md5Hex(envelope.getDataBody().getDataBody());
        return Objects.equals(md5Hex, envelope.getDataBody().getDataMD5Checksum());
    }

    private void persist(DataEnvelope envelope) {
        log.info("Persisting data with attribute name: {}", envelope.getDataHeader().getName());
        DataHeaderEntity dataHeaderEntity = modelMapper.map(envelope.getDataHeader(), DataHeaderEntity.class);

        DataBodyEntity dataBodyEntity = modelMapper.map(envelope.getDataBody(), DataBodyEntity.class);
        dataBodyEntity.setDataHeaderEntity(dataHeaderEntity);
        dataBodyEntity.setMd5Checksum(envelope.getDataBody().getDataMD5Checksum());

        saveData(dataBodyEntity);
    }

    private DataBodyEntity saveData(DataBodyEntity dataBodyEntity) {
        return dataBodyServiceImpl.saveDataBody(dataBodyEntity);
    }

    private DataEnvelope adapt(DataBodyEntity entity) {
        final DataBody dataBody = new DataBody(entity.getDataBody(), entity.getMd5Checksum());
        final DataHeader dataHeader = new DataHeader(entity.getDataHeaderEntity().getName(), entity.getDataHeaderEntity().getBlockType());
        return new DataEnvelope(dataHeader, dataBody);
    }

}
