package com.db.dataplatform.techtest.server.api.controller;

import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.api.model.PushResponse;
import com.db.dataplatform.techtest.server.component.DataLakeDispatcher;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/dataserver")
@RequiredArgsConstructor
@Validated
public class ServerController {

    private final Server server;
    private final DataLakeDispatcher dataLakeDispatcher;

    @PostMapping(value = "/pushdata", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PushResponse> pushData(@Valid @RequestBody DataEnvelope dataEnvelope) {

        log.info("Data envelope received: {}", dataEnvelope.getDataHeader().getName());
        boolean checksumPass = server.saveDataEnvelope(dataEnvelope);
        if (checksumPass) {
            log.info("Dispatching data envelope to the data lake: {}", dataEnvelope.getDataHeader().getName());
            dataLakeDispatcher.dispatch(dataEnvelope);
        }

        log.info("Data envelope persisted. Attribute name: {}", dataEnvelope.getDataHeader().getName());
        return ResponseEntity.ok(new PushResponse(checksumPass));
    }

    @GetMapping(value = "/data/{blockType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DataEnvelope>> getData(@PathVariable BlockTypeEnum blockType) {
        log.info("getData request received for blockType: {}", blockType);
        return ResponseEntity
                .ok(server.getAllDataByBlockType(blockType));
    }

    @PatchMapping(value = "/update/{name}/{newBlockType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataEnvelope> patchData(@Valid @NotEmpty @PathVariable("name") String name, @PathVariable("newBlockType") BlockTypeEnum newBlockType) {
        log.info("patch request received for: {}", name);
        return ResponseEntity
                .of(server.updateBlockType(name, newBlockType));
    }

}
