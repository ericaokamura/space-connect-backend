package com.fiap.space_connect.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CaptureEstimateDTO {
    private String  noradId;
    private String  objectName;
    private String  objectType;
    private String  rcsSize;

    // Dados orbitais
    private double  currentAltitudeKm;
    private double  inclinationDeg;

    // Estimativas da missão
    private double  deltaVms;
    private double  fuelKg;
    private double  transferTimeHours;

    // Custos
    private double  fuelCostUSD;
    private double  operationsCostUSD;
    private double  totalCostUSD;

    // Janela de tempo para captura
    private String  estimatedArrival;   // data/hora ISO
}
