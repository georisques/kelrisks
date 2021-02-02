package fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel;

import java.io.Serializable;

import lombok.Data;

@Data
public class AleaDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String         codeGaspar;
    private String         code;
    private String         libelle;
    private FamilleAleaDTO familleAlea;
}
