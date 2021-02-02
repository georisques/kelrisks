package fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel;

import java.io.Serializable;

import lombok.Data;

@Data
public class PlanPreventionRisquesDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    PlanPreventionRisquesGasparDTO gaspar;
}
