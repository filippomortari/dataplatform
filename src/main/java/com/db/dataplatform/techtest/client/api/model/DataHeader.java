package com.db.dataplatform.techtest.client.api.model;

import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@JsonSerialize(as = DataHeader.class)
@JsonDeserialize(as = DataHeader.class)
@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class DataHeader {

    @NotNull
    private final String name;

    @NotNull
    private final BlockTypeEnum blockType;

}
