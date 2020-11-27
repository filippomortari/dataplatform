package com.db.dataplatform.techtest.server.api.model;

import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import javax.validation.constraints.NotBlank;

@JsonSerialize(as = DataHeader.class)
@JsonDeserialize(as = DataHeader.class)
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class DataHeader {

    @NotBlank
    private final String name;

    private final BlockTypeEnum blockType;

}
