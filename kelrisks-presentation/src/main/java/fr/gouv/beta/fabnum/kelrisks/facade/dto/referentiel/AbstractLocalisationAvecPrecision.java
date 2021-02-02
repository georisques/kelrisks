package fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel;

import java.io.Serializable;

import lombok.Data;

@Data
public class AbstractLocalisationAvecPrecision implements Serializable {

    private static final long serialVersionUID = 1L;
	
    private String ewkt;
    private String precision;
}
