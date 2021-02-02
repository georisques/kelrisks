package fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import fr.gouv.beta.fabnum.commun.facade.AbstractFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IStatsFacade;
import fr.gouv.beta.fabnum.kelrisks.metier.referentiel.interfaces.IStatsService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class StatsFacade extends AbstractFacade implements IStatsFacade {

	@Autowired
	private IStatsService statsService;
	
	@Override
    @Cacheable(value = "stats", cacheManager = "cacheManagerStats", key = "'mykey'" )
	public String getStats() {	
		log.info("stats not in cache");
		return statsService.getMatomoStats();
	}

}
