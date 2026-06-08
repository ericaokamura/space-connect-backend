package com.fiap.space_connect.service;

import com.fiap.space_connect.model.SatcatRecord;
import com.fiap.space_connect.model.dto.CaptureEstimateDTO;
import lombok.extern.slf4j.Slf4j;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.TimeStampedPVCoordinates;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CaptureEstimatorService {

    // Considerações da missão
    private static final double CHASER_ALT_KM   = 400.0;  // altura do coletor em Km
    private static final double DRY_MASS_KG      = 500.0;  // massa do coletor em Kg
    private static final double ISP              = 450.0;  // impulso específico para um propulsor de hidrazina (em segundos)
    private static final double FUEL_COST_PER_KG = 10_000.0;  // custo por Kg em USD/kg para orbitar
    private static final double OPS_COST_PER_HR  = 50_000.0;  // custo por hora em USD/hr

    private final static String NORAD_URL = "https://celestrak.org/NORAD/elements/gp.php?CATNR={id}&FORMAT=tle";

    private final SatcatService satcatService;

    private final RestTemplate restTemplate;

    public CaptureEstimatorService(SatcatService satcatService, RestTemplate restTemplate) {
        this.satcatService = satcatService;
        this.restTemplate = restTemplate;
    }

    public CaptureEstimateDTO estimate(String noradId) {

        // 1. Extrair TLE
        TLE tle = extractTLEFromNoradId(noradId);
        AbsoluteDate now = new AbsoluteDate(
                new java.util.Date(), TimeScalesFactory.getUTC()
        );

        // 2. Propagar para atualizar estado
        TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);
        SpacecraftState state    = propagator.propagate(now);

        // 3. Extrair posição geodética
        Frame earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        OneAxisEllipsoid earth = new OneAxisEllipsoid(
                Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING,
                earthFrame
        );
        TimeStampedPVCoordinates pv = state.getPVCoordinates(earthFrame);
        GeodeticPoint gp = earth.transform(pv.getPosition(), earthFrame, now);

        double altKm = gp.getAltitude() / 1000.0;

        // 4. Extrair elementos Keplerian
        KeplerianOrbit orbit = new KeplerianOrbit(
                state.getOrbit().getPVCoordinates(), state.getFrame(),
                Constants.WGS84_EARTH_MU
        );
        double incDeg = Math.toDegrees(orbit.getI());
        double r1 = Constants.WGS84_EARTH_EQUATORIAL_RADIUS + CHASER_ALT_KM * 1000;
        double r2 = Constants.WGS84_EARTH_EQUATORIAL_RADIUS + altKm * 1000;

        // 5. Delta-V (Hohmann + inclinação)
        double mu  = Constants.WGS84_EARTH_MU;
        double dv1 = Math.sqrt(mu / r1) * (Math.sqrt(2 * r2 / (r1 + r2)) - 1);
        double dv2 = Math.sqrt(mu / r2) * (1 - Math.sqrt(2 * r1 / (r1 + r2)));
        double dvTransfer = Math.abs(dv1) + Math.abs(dv2);

        // Custo por inclinação  (assume-se que o coletor está a uma inclinação de 0° da linha de base)
        double chaserInc = 0.0;
        double vTarget   = Math.sqrt(mu / r2);
        double dvPlane   = 2 * vTarget * Math.sin(Math.toRadians(
                Math.abs(incDeg - chaserInc) / 2.0
        ));
        double dvTotal = dvTransfer + dvPlane;

        // 6. Combustível
        double fuelKg = DRY_MASS_KG *
                (Math.exp(dvTotal / (ISP * Constants.G0_STANDARD_GRAVITY)) - 1);

        // 7. Tempo de Transferência
        double sma     = (r1 + r2) / 2.0;
        double timeHrs = Math.PI * Math.sqrt(Math.pow(sma, 3) / mu) / 3600.0;

        // 8. Data/hora de chegada
        AbsoluteDate arrival = now.shiftedBy(timeHrs * 3600.0);
        String arrivalStr = arrival.toString(TimeScalesFactory.getUTC());

        // 9. Cálculo de custo
        double fuelCost = fuelKg * FUEL_COST_PER_KG;
        double opsCost  = timeHrs * OPS_COST_PER_HR;
        double total    = fuelCost + opsCost;

        // 10. Enriquecer com dados SATCAT
        SatcatRecord satcat  = satcatService.fetchByNoradId(noradId);
        String objectName    = satcat != null ? satcat.getObjectName() : "UNKNOWN";
        String objectType    = satcat != null ? satcat.getObjectType()  : "UNKNOWN";
        String rcsSize       = satcat != null
                ? satcatService.getRcsSize(satcat.getRcs()) : "UNKNOWN";

        return CaptureEstimateDTO.builder()
                .noradId(noradId)
                .objectName(objectName)
                .objectType(objectType)
                .rcsSize(rcsSize)
                .currentAltitudeKm(altKm)
                .inclinationDeg(incDeg)
                .deltaVms(dvTotal)
                .fuelKg(fuelKg)
                .transferTimeHours(timeHrs)
                .fuelCostUSD(fuelCost)
                .operationsCostUSD(opsCost)
                .totalCostUSD(total)
                .estimatedArrival(arrivalStr)
                .build();
    }

    private TLE extractTLEFromNoradId(String noradId) {
        String tleText = restTemplate.getForObject(NORAD_URL, String.class, noradId);
        Map<String, TLE> map = parseTles(tleText);
        Map.Entry<String, TLE> entry = map.entrySet().iterator().next();
        return entry.getValue();
    }

    private Map<String, TLE> parseTles(String tleText) {
        String[] lines = tleText.split("\\R");
        Map<String, TLE> map = new HashMap<>();
        for (int i = 0; i < lines.length; i += 3) {

            String line1 = lines[i + 1];
            String line2 = lines[i + 2];

            String objectName = lines[i].trim();
            String noradId = line2.substring(2, 7);

            map.put(objectName + "ˆ" + noradId, new TLE(line1, line2));
        }
        return map;
    }

}
