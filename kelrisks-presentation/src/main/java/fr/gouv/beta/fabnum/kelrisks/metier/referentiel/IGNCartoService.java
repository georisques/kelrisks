package fr.gouv.beta.fabnum.kelrisks.metier.referentiel;

import fr.gouv.beta.fabnum.kelrisks.metier.referentiel.interfaces.IIGNCartoService;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.IGNCartoAssiettePaginatedFeatures;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.IGNCartoGenerateurPaginatedFeatures;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import javax.ws.rs.core.UriBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

/**
Documentation de l'API Carto :
https://apicarto.ign.fr/api/doc/pdf/doc_user_modUrbanisme_v1.pdf
https://apicarto.ign.fr/api/doc/gpu#/SUP/get_gpu_generateur_sup_s
http://www.geoinformations.developpement-durable.gouv.fr/fichier/pdf/20190211_nomenclature_ordre_alphabetique_cle7abf9e.pdf?arg=177835582&cle=0673a76b5f0f4aff7e397dcaf7be486b73aa6e55&file=pdf%2F20190211_nomenclature_ordre_alphabetique_cle7abf9e.pdf
 */
@Log4j2
@Service("iGNCartoService")
public class IGNCartoService implements IIGNCartoService {
    
    private static final String IGN_CARTO_BASE_URL = "https://apicarto.ign.fr/api";
    private static final String ASSIETTE_URL      = IGN_CARTO_BASE_URL + "/gpu/assiette-sup-s";
    private static final String SURFACIQUE_URL    = IGN_CARTO_BASE_URL + "/gpu/generateur-sup-s";
    
    @Autowired
    WebClient webClient;
    
    @Override
    public IGNCartoAssiettePaginatedFeatures rechercherAssiettesContenantPolygon(String geom) {
        
    	// 13300,SALON DE PROVENCE
    	// Code parcelle : BC-230
    	// https://ubiq.co/tech-blog/increase-max-url-length-apache/
    	// TODO : adapter ce chiffre 2048
    	if (geom.length() < 2048) {
            UriBuilder uriBuilder  = UriBuilder.fromPath(ASSIETTE_URL);
            URI        assietteUri = uriBuilder.queryParam("geom", "{geom}").build(geom);
            log.info("rechercherAssiettesContenantPolygon: {}", assietteUri.toString());
            
            return webClient.get()
                           .uri(assietteUri)
                           .accept(MediaType.APPLICATION_JSON)
                           .retrieve()
                           .bodyToMono(IGNCartoAssiettePaginatedFeatures.class)
                           .onErrorResume(e -> {
                               log.error("API IGN Assiettes {}",assietteUri);
                               log.error("Fail API IGN Assiettes {}" , e.getLocalizedMessage());
                               return Mono.empty();
                           })
                           .block();
    	} else {
    		try {
    			return webClient.post().uri(new URI(ASSIETTE_URL))
    					.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
    					.body(BodyInserters.fromValue(new StringBuilder("{\"geom\":").append(geom).append("}").toString()))
				.retrieve().bodyToMono(IGNCartoAssiettePaginatedFeatures.class)
				.onErrorResume(e -> {
				    log.error("API POST IGN Assiettes geom size: {} geom: {}", geom.length(), geom);
				    log.error("Fail POST API IGN Assiettes {}" , e.getLocalizedMessage());
				    return Mono.empty();
				})
				.block();
			} catch (URISyntaxException e) {
				log.error("Bad URI", e);
				return null;
			}
    	}

    }
    
    @Override
    public IGNCartoGenerateurPaginatedFeatures rechercherGenerateur(String partition) {
    
        UriBuilder uriBuilder = UriBuilder.fromPath(SURFACIQUE_URL);
        URI generateurUri = uriBuilder
                                    .queryParam("partition", "{partition}")
                                    .build(partition);
        log.info("rechercherGenerateur: {}", generateurUri.toString());

        return webClient.get()
                       .uri(generateurUri)
                       .accept(MediaType.APPLICATION_JSON)
                       .retrieve()
                       .bodyToMono(IGNCartoGenerateurPaginatedFeatures.class)
                       .onErrorResume(e -> {
                           log.error("API IGN Generateur: {}",generateurUri);
                           log.error("Fail API IGN Generateur: {}" , e.getLocalizedMessage());
                           return Mono.empty();
                       })
                       .block();
    }
}
