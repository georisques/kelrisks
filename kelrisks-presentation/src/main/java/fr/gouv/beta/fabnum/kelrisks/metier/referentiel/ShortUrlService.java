package fr.gouv.beta.fabnum.kelrisks.metier.referentiel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.gouv.beta.fabnum.kelrisks.metier.referentiel.interfaces.IShortUrlService;
import fr.gouv.beta.fabnum.kelrisks.persistance.referentiel_write.IShortUrlRepository;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities_write.ShortUrl;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.qo.ShortUrlQO;

/**
 * Service métier de gestion de l'entité ShortUrl
 */
@Service("shortUrlService")
public class ShortUrlService implements IShortUrlService  {
    
    
    @Autowired
    private IShortUrlRepository repo;
    
	@Override
	public List<ShortUrl> rechercherAvecCritere(ShortUrlQO shortUrlQO) {
		List<ShortUrl> shortUrls = new ArrayList<>();
		
		String code = shortUrlQO.getCode();
		String url = shortUrlQO.getUrl();
		
		if(StringUtils.isNotEmpty(code)) {
			shortUrls = repo.findByCode(code);
		} else if(StringUtils.isNotEmpty(url)) {
			shortUrls = repo.findByUrl(url);

		}
		return shortUrls;
	}

	@Override
	public void save(ShortUrl entity) {
		repo.save(entity);
	}
}
  