package com.db.dataplatform.techtest.client.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@JsonSerialize(as = DataEnvelope.class)
@JsonDeserialize(as = DataEnvelope.class)
@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class DataEnvelope {

    @NotNull
    private final DataHeader dataHeader;

    @NotNull
    private final DataBody dataBody;
}
