package fr.gouv.beta.fabnum.kelrisks.presentation.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiKelrisks {
    
    @Value("${kelrisks.app.version}")
    String appVersion;
    @Value("${kelrisks.api.version}")
    String apiVersion;
    
    public ApiKelrisks() {
        // Rien à faire
    }
    
    @GetMapping("/api/apiversion")
    public ResponseEntity<String> getApiVersion() {
        
        return ResponseEntity.ok(apiVersion);
    }
    
    @GetMapping("/api/appversion")
    public ResponseEntity<String> getAppVersion() {
        
        return ResponseEntity.ok(appVersion);
    }
}
