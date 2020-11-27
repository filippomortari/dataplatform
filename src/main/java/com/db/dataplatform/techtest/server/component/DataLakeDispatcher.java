package com.db.dataplatform.techtest.server.component;


import com.db.dataplatform.techtest.server.api.model.DataEnvelope;

import java.util.concurrent.CompletableFuture;

public interface DataLakeDispatcher {
    CompletableFuture<Boolean> dispatch(DataEnvelope dataEnvelope);
}
