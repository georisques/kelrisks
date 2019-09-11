package fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel;

import lombok.Data;

@Data
public class InstallationClasseeDTO {
    
    private Long   id;
    private String code;
    private String nom;
    private String regime;
    private String commune;
    private String codeInsee;
    private String codePostal;
    private String adresse;
    private String complementAdresse;
    private String ewkt;
    private String precision;
    private String adresseId;
}
  