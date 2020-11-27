package com.db.dataplatform.techtest.server.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

@JsonSerialize(as = PushResponse.class)
@JsonDeserialize(as = PushResponse.class)
@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class PushResponse {
    private final Boolean checksumPass;
}
