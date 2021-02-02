package fr.gouv.beta.fabnum.kelrisks.persistance.referentiel_write;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities_write.ShortUrl;


@Repository("shortUrlRepository")
public interface IShortUrlRepository extends JpaRepository<ShortUrl, Long>, JpaSpecificationExecutor<ShortUrl>{
	
	List<ShortUrl> findByUrl(String url);
	
	List<ShortUrl> findByCode(String code);

}