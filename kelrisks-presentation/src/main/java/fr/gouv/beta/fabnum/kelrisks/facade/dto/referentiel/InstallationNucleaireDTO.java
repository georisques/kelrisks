package fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel;

import java.io.Serializable;

import lombok.Data;

@Data
public class InstallationNucleaireDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String  nomInstallation;
    private String  libCommune;
    private boolean centrale = false;
}
