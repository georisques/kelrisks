package fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel;

import lombok.Data;

@Data
public class SiteIndustrielBasiasDTO extends AbstractLocalisationAvecPrecision {
    
    private static final long serialVersionUID = 1L;
	
    private Long   id;
    private String identifiant;
    private String adresse;
    private String raisonSociale;
    private String precision;
    private String codeINSEE;
}
