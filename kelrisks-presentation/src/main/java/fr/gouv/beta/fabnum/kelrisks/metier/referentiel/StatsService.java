package fr.gouv.beta.fabnum.kelrisks.metier.referentiel;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import fr.gouv.beta.fabnum.kelrisks.metier.referentiel.interfaces.IStatsService;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@Service("statsService")
public class StatsService implements IStatsService {
	// TODO : revoir la periode ?
	// FIXME : bizarre si date 2019/02 en prod reponse vide, en recette pas de problème, semble y avoir une limite de durée quelque part
	// si date=2020-10-09,2050-12-31 reponse vide aussi...
    private static final String MATOMO_PARAMS = "&module=API&method=Events.getAction&secondaryDimension=eventName&flat=1&period=range&date=2021-01-01,2050-12-31&format=json";
	
    // TODO : utiliser une class de config
    @Autowired
	private Environment env;
    
	@Autowired
    WebClient webClient;
    
	@Override
	public String getMatomoStats() {
		String token = env.getProperty("application.matomo-token-auth");
		String baseUrl = env.getProperty("application.matomo-url");
				
		String uri = baseUrl + MATOMO_PARAMS + "&token_auth=" + token;
		return webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error(" V : " + e.getLocalizedMessage());
                    return Mono.just("");
                })
                .block(Duration.ofSeconds(30L));

	}

}
