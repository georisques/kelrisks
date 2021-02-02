package fr.gouv.beta.fabnum.kelrisks.metier.referentiel;

import fr.gouv.beta.fabnum.kelrisks.metier.referentiel.interfaces.IAdresseDataGouvService;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.AdresseDataGouvPaginatedFeatures;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Log4j2
@Service("adresseDataGouvService")
public class AdresseDataGouvService implements IAdresseDataGouvService {
    
    private static final String ADRESSE_BASE_URL = "https://api-adresse.data.gouv.fr/search";
    private static final String COMMUNE_URL      = ADRESSE_BASE_URL + "/?q=PARAM_INSEE&limit=1";
    
    @Autowired
    private WebClient webClient;
    
    public List<AdresseDataGouvPaginatedFeatures.Adresse> rechercherCommune(String codeINSEE) {
        
        String uri = COMMUNE_URL.replace("PARAM_INSEE", codeINSEE);
        
        AdresseDataGouvPaginatedFeatures block = webClient.get()
                                                         .uri(uri)
                                                         .accept(MediaType.APPLICATION_JSON)
                                                         .retrieve()
                                                         .bodyToMono(AdresseDataGouvPaginatedFeatures.class)
                                                         .onErrorResume(e -> {
                                                             log.error(" V : " + e.getLocalizedMessage());
                                                             return Mono.just(new AdresseDataGouvPaginatedFeatures());
                                                         })
                                                         .block(Duration.ofSeconds(30L));
        
        if (block != null) { return block.getFeatures(); }
        
        return new ArrayList<>();
    }
}
