package com.fiap.space_connect.service;

import com.fiap.space_connect.mapper.SpaceConnectMapper;
import com.fiap.space_connect.model.SpaceObject;
import com.fiap.space_connect.model.dto.SpaceObjectDTO;
import com.fiap.space_connect.repository.SpaceConnectRepository;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.PVCoordinates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class SpaceConnectService {

    @Autowired
    private SpaceConnectRepository spaceConnectRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final static String DEBRIS_URL = "https://celestrak.org/NORAD/elements/gp.php?GROUP=cosmos-2251-debris&FORMAT=tle";

    private final static String RB_URL =   "https://celestrak.org/NORAD/elements/gp.php?GROUP=visual&FORMAT=tle";

    private final static String NORAD_URL = "https://celestrak.org/NORAD/elements/gp.php?CATNR={id}&FORMAT=tle";

    public List<SpaceObjectDTO> calculateCurrentPosition() {

        this.spaceConnectRepository.deleteAll();

        List<SpaceObjectDTO> spaceObjects = new ArrayList<>();
        String dadosRB = extrairDadosRocketBodyTLE();
        String dadosDebris = extrairDadosDebrisTLE();

        Map<String, TLE> map = new HashMap<>();
        map.putAll(parseTlesForDEBAndRB(dadosRB));
        map.putAll(parseTlesForDEBAndRB(dadosDebris));

        for(Map.Entry<String, TLE> entry : map.entrySet()) {
            System.out.println(entry.getKey());
            TLEPropagator propagator =
                    TLEPropagator.selectExtrapolator(entry.getValue());

            AbsoluteDate now =
                    new AbsoluteDate(
                            new Date(),
                            TimeScalesFactory.getUTC()
                    );

            SpacecraftState state =
                    propagator.propagate(now);

            PVCoordinates pv =
                    state.getPVCoordinates();

            Vector3D position = pv.getPosition();

            String noradId = entry.getKey().split("ˆ")[1];
            String objectName = entry.getKey().split("ˆ")[0];

            SpaceObject spaceObject = new SpaceObject();
            spaceObject.setNoradId(noradId);
            spaceObject.setObjectName(objectName);
            spaceObject.setPositionX(position.getX());
            spaceObject.setPositionY(position.getY());
            spaceObject.setPositionZ(position.getZ());
            spaceConnectRepository.save(spaceObject);

            spaceObjects.add(SpaceConnectMapper.convertModelToDTO(spaceObject));

        }

        return spaceObjects;
    }

    private String extrairDadosDebrisTLE() {
        return restTemplate.getForObject(DEBRIS_URL, String.class);
    }

    private String extrairDadosRocketBodyTLE() {
        return restTemplate.getForObject(RB_URL, String.class);
    }

    private String extrairDadosNoradTLE(String noradId) {
        return restTemplate.getForObject(NORAD_URL, String.class, noradId);
    }

    private Map<String, TLE> parseTlesForDEBAndRB(String tleText) {
        String[] lines = tleText.split("\\R");
        Map<String, TLE> map = new HashMap<>();
        for (int i = 0; i < lines.length; i += 3) {

            String line1 = lines[i + 1];
            String line2 = lines[i + 2];

            String objectName = lines[i].trim();
            String noradId = line2.substring(2, 7);

            if(objectName.contains("DEB") || objectName.contains("R/B")) {
                map.put(objectName + "ˆ" + noradId, new TLE(line1, line2));
            }
        }
        return map;
    }

    public List<SpaceObjectDTO> retornarDadosEspaciais() {
        List<SpaceObjectDTO> spaceObjects = new ArrayList<>();
        spaceConnectRepository.findAll().forEach(obj -> spaceObjects.add(SpaceConnectMapper.convertModelToDTO(obj)));
        return spaceObjects;
    }

    public void salvarDadosEspaciais(List<SpaceObjectDTO> spaceObjects) {
        spaceObjects.forEach(obj -> spaceConnectRepository.save(SpaceConnectMapper.convertDTOToModel(obj)));
    }
}
