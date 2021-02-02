package fr.gouv.beta.fabnum.kelrisks.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IStatsFacade;

@RestController
public class StatsControlleur {

    @Autowired
    IStatsFacade statsFacade;
    
    /**
     * Get Matomo stats (custom stats)
     * @return
     */
    @GetMapping(value = "/api/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> stats() {
        
         return ResponseEntity.ok(statsFacade.getStats());
    }
	
}
