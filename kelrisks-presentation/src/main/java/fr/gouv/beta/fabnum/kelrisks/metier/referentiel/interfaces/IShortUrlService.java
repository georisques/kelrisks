package fr.gouv.beta.fabnum.kelrisks.metier.referentiel.interfaces;

import java.util.List;

import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities_write.ShortUrl;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.qo.ShortUrlQO;

public interface IShortUrlService {

	List<ShortUrl> rechercherAvecCritere(ShortUrlQO shortUrlQO);

	void save(ShortUrl entity);

}
