package com.fiap.space_connect.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class SatcatRecord {
    @JsonProperty("OBJECT_NAME")  private String objectName;
    @JsonProperty("NORAD_CAT_ID") private String noradCatId;
    @JsonProperty("OBJECT_TYPE")  private String objectType;   // PAYLOAD, R/B, DEB, UNKNOWN
    @JsonProperty("OWNER")        private String owner;
    @JsonProperty("RCS")          private Double rcs;          // m², pode ser null
}