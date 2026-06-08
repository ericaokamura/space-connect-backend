package com.fiap.space_connect.controller;

import com.fiap.space_connect.model.dto.SpaceObjectDTO;
import com.fiap.space_connect.service.SpaceConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("space-connect")
@CrossOrigin(origins = "http://localhost:5173")
public class SpaceConnectController {

    @Autowired
    private SpaceConnectService spaceConnectService;

    @PostMapping("/process")
    public ResponseEntity<List<SpaceObjectDTO>> salvarDadosEspaciais() {
        return ResponseEntity.ok(spaceConnectService.calculateCurrentPosition());
    }

    @GetMapping("/fetch")
    public ResponseEntity<List<SpaceObjectDTO>> retornarDadosEspaciais() {
        return ResponseEntity.ok(spaceConnectService.retornarDadosEspaciais());
    }

    @PostMapping("/save")
    public ResponseEntity<Void> salvarDadosEspaciais(@RequestBody List<SpaceObjectDTO> spaceObjects) {
        spaceConnectService.salvarDadosEspaciais(spaceObjects);
        return ResponseEntity.ok().build();
    }
}
