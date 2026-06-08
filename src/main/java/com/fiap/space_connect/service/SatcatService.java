package com.fiap.space_connect.service;

import com.fiap.space_connect.model.SatcatRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class SatcatService {

    private static final String SATCAT_URL =
            "https://celestrak.org/satcat/records.php?CATNR={id}&FORMAT=JSON";

    private final RestTemplate restTemplate;

    public SatcatService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public SatcatRecord fetchByNoradId(String noradId) {
        try {
            SatcatRecord[] records = restTemplate.getForObject(
                    SATCAT_URL, SatcatRecord[].class, noradId
            );
            if (records != null && records.length > 0) return records[0];
        } catch (Exception e) {
            log.warn("O consumo ao SATCAT falhou para noradId {}: {}", noradId, e.getMessage());
        }
        return null;
    }

    public String getRcsSize(Double rcs) {
        if (rcs == null) return "DESCONHECIDO";
        if (rcs < 0.1)   return "PEQUENO";
        if (rcs < 1.0)   return "MÉDIO";
        return "GRANDE";
    }
}