package fr.gouv.beta.fabnum.kelrisks.metier.referentiel;

import fr.gouv.beta.fabnum.kelrisks.metier.referentiel.interfaces.IGeorisquesService;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.GeorisquePaginatedAZI;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.GeorisquePaginatedCatNat;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.GeorisquePaginatedPPR;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.GeorisquePaginatedRadon;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.GeorisquePaginatedSIS;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.GeorisquePaginatedSismique;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.GeorisquePaginatedTRI;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

import javax.ws.rs.core.UriBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Log4j2
@Service("georisquesService")
public class GeorisquesService implements IGeorisquesService {
    
    private static final String RADON_URL          = "/radon?code_insee=PARAM_INSEE&page=1&page_size=20";
    private static final String TRI_URL            = "/gaspar/tri?rayon=1000&code_insee=PARAM_INSEE&page=1&page_size=20";
    private static final String AZI_URL            = "/gaspar/azi?rayon=1000&code_insee=PARAM_INSEE&page=1&page_size=20";
    private static final String SISMIQUE_URL       = "/zonage_sismique?&code_insee=PARAM_INSEE&page=1&page_size=20";
    private static final String CATNAT_URL         = "/gaspar/catnat?rayon=1000&code_insee=PARAM_INSEE&page=1&page_size=100";
    private static final String SIS_URL            = "/sis";
    private static final String PPR_URL            = "/ppr";
    
    @Autowired
    WebClient webClient;
    
    @Autowired
    private Environment env;
    
    // TODO : mettre en config
    long block = 30L;
    
    private String getAPIUrl() {
    	return env.getProperty("application.api.georisques-url");
    }
    
    public GeorisquePaginatedRadon rechercherRadonCommune(String codeINSEE) {
        
        String uri = getAPIUrl() + RADON_URL.replace("PARAM_INSEE", codeINSEE);
        log.info("RADON API: {}",uri);
        
        return webClient.get()
                       .uri(uri)
                       .accept(MediaType.APPLICATION_JSON)
                       .retrieve()
                       .bodyToMono(GeorisquePaginatedRadon.class)
                       .onErrorResume(e -> {
                           log.error("RADON_URL: {}", uri);
                           log.error("Fail API Georisques rechercherRadonCommune: {}", e.getLocalizedMessage());
                           return Mono.just(new GeorisquePaginatedRadon());
                       })
                       .block(Duration.ofSeconds(block));
    }
    
    public GeorisquePaginatedAZI rechercherAZICommune(String codeINSEE) {
    
        String uri = getAPIUrl() + AZI_URL.replace("PARAM_INSEE", codeINSEE);
        log.info("AZI API: "+uri);
    
        return webClient.get()
                       .uri(uri)
                       .accept(MediaType.APPLICATION_JSON)
                       .retrieve()
                       .bodyToMono(GeorisquePaginatedAZI.class)
                       .onErrorResume(e -> {
                           log.error("AZI_URL: {}", uri);
                           log.error("Fail API Georisques rechercherAZICommune: {}", e.getLocalizedMessage());
                           return Mono.just(new GeorisquePaginatedAZI());
                       })
                       .block(Duration.ofSeconds(block));
    }
    
    public GeorisquePaginatedTRI rechercherTRICommune(String codeINSEE) {
    
        String uri = getAPIUrl() + TRI_URL.replace("PARAM_INSEE", codeINSEE);
        log.info("TRI API: "+uri);

        return webClient.get()
                       .uri(uri)
                       .accept(MediaType.APPLICATION_JSON)
                       .retrieve()
                       .bodyToMono(GeorisquePaginatedTRI.class)
                       .onErrorResume(e -> {
                           log.error("TRI_URL: {}", uri);
                           log.error("Fail API Georisques rechercherTRICommune: {}", e.getLocalizedMessage());
                           return Mono.just(new GeorisquePaginatedTRI());
                       })
                       .block(Duration.ofSeconds(block));
    }
    
    public GeorisquePaginatedCatNat rechercherCatNatCommune(String codeINSEE) {
        
        String uri = getAPIUrl() + CATNAT_URL.replace("PARAM_INSEE", codeINSEE);
        log.info("CATNAT API: "+uri);

        return webClient.get()
                       .uri(uri)
                       .accept(MediaType.APPLICATION_JSON)
                       .retrieve()
                       .bodyToMono(GeorisquePaginatedCatNat.class)
                       .onErrorResume(e -> {
                           log.error("CATNAT_URL: {}", uri);
                           log.error("Fail API Georisques rechercherCatNatCommune: {}", e.getLocalizedMessage());
                           return Mono.just(new GeorisquePaginatedCatNat());
                       })
                       .block(Duration.ofSeconds(block));
    }
    
    public GeorisquePaginatedSismique rechercherSismiciteCommune(String codeINSEE) {
        
        String uri = getAPIUrl() + SISMIQUE_URL.replace("PARAM_INSEE", codeINSEE);
        log.info("SISMIQUE API: "+uri);
        
        return webClient.get()
                       .uri(uri)
                       .accept(MediaType.APPLICATION_JSON)
                       .retrieve()
                       .bodyToMono(GeorisquePaginatedSismique.class)
                       .onErrorResume(e -> {
                           log.error("SISMIQUE_URL: {}", uri);
                    	   log.error("Fail API Georisques rechercherSismiciteCommune: {}", e.getLocalizedMessage());
                           return Mono.just(new GeorisquePaginatedSismique());
                       })
                       .block(Duration.ofSeconds(block));
    }
    
    @Override
    public GeorisquePaginatedSIS rechercherSisCoordonnees(String lon, String lat, int rayon) {
    
        String latlon = lon + "," + lat;
    
        UriBuilder uriBuilder = UriBuilder.fromPath(getAPIUrl() + SIS_URL);
        URI generateurUri = uriBuilder
                                    .queryParam("rayon", String.valueOf(rayon))
                                    .queryParam("latlon", "{latlon}")
                                    .queryParam("page", "1")
                                    .queryParam("page_size", "10")
                                    .build(latlon);
        log.info("SIS API: "+generateurUri);
        
        return webClient.get()
                       .uri(generateurUri)
                       .accept(MediaType.APPLICATION_JSON)
                       .retrieve()
                       .bodyToMono(GeorisquePaginatedSIS.class)
                       .onErrorResume(e -> {
                           log.error("SIS_URL: {}", generateurUri);
                    	   log.error("Fail API Georisques rechercherSisCoordonnees: {}",  e.getLocalizedMessage());
                           return Mono.just(new GeorisquePaginatedSIS());
                       })
                       .block(Duration.ofSeconds(block));
    }
    
    @Override
    public GeorisquePaginatedPPR rechercherPprCoordonnees(String lon, String lat, int rayon) {
    
        String latlon = lon + "," + lat;
    
        UriBuilder uriBuilder = UriBuilder.fromPath(getAPIUrl() + PPR_URL);
        URI generateurUri = uriBuilder
                                    .queryParam("rayon", String.valueOf(rayon))
                                    .queryParam("latlon", "{latlon}")
                                    .queryParam("page", "1")
                                    .queryParam("page_size", "10")
                                    .build(latlon);
        log.info("PPR API: {}",generateurUri);
    
        return webClient.get()
                       .uri(generateurUri)
                       .accept(MediaType.APPLICATION_JSON)
                       .retrieve()
                       .bodyToMono(GeorisquePaginatedPPR.class)
                       .onErrorResume(e -> {
                           log.error("PPR_URL: {}",generateurUri);
                    	   log.error("Fail API Georisques rechercherPprCoordonnees: {}", e.getLocalizedMessage());
                           return Mono.just(new GeorisquePaginatedPPR());
                       })
                       .block();
    }
    
    @Override
    public GeorisquePaginatedPPR rechercherPprCommune(String codeInsee) {
        UriBuilder uriBuilder = UriBuilder.fromPath(getAPIUrl() + PPR_URL);
        URI generateurUri = uriBuilder
                                    .queryParam("code_insee", "{codeInsee}")
                                    .queryParam("page", "1")
                                    .queryParam("page_size", "20")
                                    .build(codeInsee);
        log.info("PPR commune API: {}",generateurUri);
    
        return webClient.get()
                       .uri(generateurUri)
                       .accept(MediaType.APPLICATION_JSON)
                       .retrieve()
                       .bodyToMono(GeorisquePaginatedPPR.class)
                       .onErrorResume(e -> {
                           log.error("PPR_URL: {}",generateurUri);
                    	   log.error("Fail API Georisques rechercherPprCommune: {}", e.getLocalizedMessage());
                           return Mono.just(new GeorisquePaginatedPPR());
                       })
                       .block();
    }
}
