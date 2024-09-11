package com.cycos.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class RESTController {

    @Autowired
    private LotRepository repository;

    @GetMapping("/state")
    @ResponseBody
    public ResponseEntity<Lot> getState(@RequestParam(name = "lot") int lotNumber) {
        log.debug("EP called ::= [GET /state]");
        Lot lot = repository.selectLot(lotNumber);
        if (lot != null) {
            return ResponseEntity.ok(lot);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/state/all")
    @ResponseBody
    public ResponseEntity<List<Lot>> getAllStates() {
        log.debug("EP called ::= [GET /state/all]");
        List<Lot> lots = repository.selectAllLots();
        return ResponseEntity.ok(lots);
    }



    @PostMapping("/state")
    public ResponseEntity<Void> setState(@Valid @RequestParam("lot") int a, @Valid @RequestParam("state") int b) {
        log.info("Endpoint calculate was called with parameters: {} and {}", a, b);
        repository.upsertLots("dummyMac", a, b, 0, 0);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/deleteLot")
    public ResponseEntity<String> deleteLot(@RequestParam("mac") String macAddress) {
        log.info("Endpoint deleteLot was called with parameter: {}", macAddress);
        String result = repository.deleteLot(macAddress);
        if (result.equals("Success")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteSensor")
    public ResponseEntity<String> deleteSensor(@RequestParam("mac") String macAddress) {
        log.info("Endpoint deleteSensors was called with parameter: {}", macAddress);
        String result = repository.deleteSensors(macAddress);
        if (result.equals("Success")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
 
    @PostMapping("/editSensor")
    public ResponseEntity<String> setSensor(@Valid @RequestParam("mac") String mac, @Valid @RequestParam("lot") int lot) {
        log.info("Endpoint editSensor was called with parameters: {} and {}", mac, lot);
        try {
            String result = repository.upsertSensors(mac, lot);
            if (result.equals("Success")) {
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                log.error("Failed to upsert sensor: {}, lot: {}", mac, lot);
                return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            log.error("Exception during upsertSensor operation", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
