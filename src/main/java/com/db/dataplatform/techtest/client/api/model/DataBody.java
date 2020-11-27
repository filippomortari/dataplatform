package com.db.dataplatform.techtest.client.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@JsonSerialize(as = DataBody.class)
@JsonDeserialize(as = DataBody.class)
@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class DataBody {

    @NotNull
    private final String dataBody;

    @NotNull
    private final String dataMD5Checksum;
}
