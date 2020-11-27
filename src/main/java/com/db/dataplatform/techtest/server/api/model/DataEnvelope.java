package com.db.dataplatform.techtest.server.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@JsonSerialize(as = DataEnvelope.class)
@JsonDeserialize(as = DataEnvelope.class)
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class DataEnvelope {

    @NotNull
    @Valid
    private final DataHeader dataHeader;

    @NotNull
    private final DataBody dataBody;
}
