package fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel;

import lombok.Data;

@Data
public class InstallationClasseeDTO extends AbstractLocalisationAvecPrecision {
    
    private static final long serialVersionUID = 1L;
	
    private Long   id;
    private String code;
    private String nom;
    private String regime;
    private String commune;
    private String adresse;
    private String codeINSEE;
    private String complementAdresse;
    private String adresseId;
    private String precision;
}
  
