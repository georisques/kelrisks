package fr.gouv.beta.fabnum.kelrisks.metier.referentiel;

import fr.gouv.beta.fabnum.kelrisks.metier.referentiel.interfaces.IBRGMService;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.BRGMPaginatedCanalisation;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.BRGMPaginatedInstallationNuclaire;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Log4j2
@Service("brgmService")
public class BRGMService implements IBRGMService {
    
    private static final String CANALISATIONS_COORDONNEES_URL            = "version=1.0.0&service=wfs&request=getfeature&typename=CANALISATIONS&propertyname=gid,num_com,nom_com," +
                                                                           "transporteur,desc_ouv,cat_fluide,longueur&outputformat=geojson&rayon=RAYON&X=LONGITUDE&Y=LATITUDE";
    private static final String CANALISATIONS_COMMUNE_URL                = "version=1.0.0&service=wfs&request=getfeature&typename=CANALISATIONS_COMMUNE&propertyname=gid,num_com," +
                                                                           "nom_com,transporteur,desc_ouv,cat_fluide,longueur&numinsee=PARAM_INSEE&outputformat=geojson";
    private static final String INSTALLATIONS_NUCLEAIRES_COORDONNEES_URL = "&service=wfs&version=2.0.0&request=getfeature&typename=INSTALLATIONS_NUCLEAIRES&outputformat=geojson&rayon=RAYON&X=LONGITUDE&Y=LATITUDE";
    private static final String INSTALLATIONS_NUCLEAIRES_COMMUNE_URL     = "&service=wfs&version=2.0.0&request=getfeature&typename=INSTALLATIONS_NUCLEAIRES_COMMUNE&numinsee" +
                                                                           "=PARAM_INSEE&rayon=10000&outputformat=geojson";
    
    @Autowired
    WebClient webClient;
    @Autowired
    private Environment env;
    
    private String getServiceUrl() {
    	return env.getProperty("application.wfs.brgm-url");
    }
    
    public BRGMPaginatedCanalisation rechercherCanalisationsCommune(String codeINSEE) {
        
        String uri = getServiceUrl() + CANALISATIONS_COMMUNE_URL.replace("PARAM_INSEE", codeINSEE);
        log.info("rechercherCanalisationsCommune: {}", uri);
        
        return webClient.get()
                       .uri(uri)
                       .accept(MediaType.APPLICATION_JSON)
                       .retrieve()
                       .bodyToMono(BRGMPaginatedCanalisation.class)
                       .onErrorResume(e -> {
                           log.error("CANALISATIONS_COMMUNE_URL: {}", uri);
                           log.error("Fail MapServer rechercherCanalisationsCommune: {}", e.getLocalizedMessage());
                           return Mono.just(new BRGMPaginatedCanalisation());
                       })
                       .block(Duration.ofSeconds(30L));
    }
    
    @Override
    public BRGMPaginatedCanalisation rechercherCanalisationsCoordonnees(String lon, String lat, int rayon) {
    
    	String uri = getServiceUrl()  + CANALISATIONS_COORDONNEES_URL.replace("RAYON", ""+rayon).replace("LONGITUDE", lon).replace("LATITUDE", lat);
    
        log.info("rechercherCanalisationsCoordonnees: {}",uri);

        return webClient.get()
                       .uri(uri)
                       .accept(MediaType.APPLICATION_JSON)
                       .retrieve()
                       .bodyToMono(BRGMPaginatedCanalisation.class)
                       .onErrorResume(e -> {
                           log.error("CANALISATIONS_COORDONNEES_URL: {}", uri);
                           log.error("Fail MapServer rechercherCanalisationsCoordonnees: {}", e.getLocalizedMessage());
                           return Mono.just(new BRGMPaginatedCanalisation());
                       })
                       .block(Duration.ofSeconds(30L));
    }
    
    public BRGMPaginatedInstallationNuclaire rechercherInstallationsNucleairesCommune(String codeINSEE) {
    
        String uri = getServiceUrl() + INSTALLATIONS_NUCLEAIRES_COMMUNE_URL.replace("PARAM_INSEE", codeINSEE);
        log.info("rechercherInstallationsNucleairesCommune: {}", uri);

        return webClient.get()
                       .uri(uri)
                       .accept(MediaType.APPLICATION_JSON)
                       .retrieve()
                       .bodyToMono(BRGMPaginatedInstallationNuclaire.class)
                       .onErrorResume(e -> {
                           log.error("INSTALLATIONS_NUCLEAIRES_COMMUNE_URL: {}",uri);
                           log.error("Fail MapServer rechercherInstallationsNucleairesCommune: {}",e.getLocalizedMessage());
                           return Mono.just(new BRGMPaginatedInstallationNuclaire());
                       })
                       .block(Duration.ofSeconds(30L));
    }
    
    @Override
    public BRGMPaginatedInstallationNuclaire rechercherInstallationsNucleairesCoordonnees(String lon, String lat, int rayon) {

    	String uri = getServiceUrl()  + INSTALLATIONS_NUCLEAIRES_COORDONNEES_URL.replace("RAYON", ""+rayon).replace("LONGITUDE", lon).replace("LATITUDE", lat);

        log.info("rechercherInstallationsNucleairesCoordonnees: {}", uri);

        return webClient.get()
                       .uri(uri)
                       .accept(MediaType.APPLICATION_JSON)
                       .retrieve()
                       .bodyToMono(BRGMPaginatedInstallationNuclaire.class)
                       .onErrorResume(e -> {
                           log.error("INSTALLATIONS_NUCLEAIRES_COORDONNEES_URL: {}",uri);
                           log.error("Fail MapServer rechercherInstallationsNucleairesCoordonnees: {}", e.getLocalizedMessage());
                          return Mono.just(new BRGMPaginatedInstallationNuclaire());
                       })
                       .block(Duration.ofSeconds(30L));
    }
}
