package fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel;

import lombok.Data;

@Data
public class SiteIndustrielBasolDTO extends AbstractLocalisationAvecPrecision {
    
    private static final long serialVersionUID = 1L;
	
    private Long   id;
    private String numerobasol;
    private String identifiantbasias;
    private String adresse;
    private String proprietaire;
    private String precision;
    private String codeINSEE;
}
  
