package fr.gouv.beta.fabnum.kelrisks.facade.mapping.referentiel_write;

import java.util.List;

import org.mapstruct.Mapper;

import fr.gouv.beta.fabnum.kelrisks.facade.avis.ShortUrlDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.mapping.ICommonMapperConfig;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities_write.ShortUrl;

@Mapper(config = ICommonMapperConfig.class)
public interface IShortUrlMapper {
    
    ShortUrlDTO toDTO(ShortUrl shortUrl);
    
    ShortUrl toEntity(ShortUrlDTO shortUrlDTO);
    
    List<ShortUrlDTO> toDTOs(List<ShortUrl> rechercherAvecCritere);
}