package fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class PlanPreventionRisquesGasparDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String       idGaspar;
    private AleaDTO      alea;
    private String       codeINSEE;
    private Date         datePrescription;
    private Date         dateApplicationAnticipee;
    private Date         dateDeprescription;
    private Date         dateApprobation;
    private Date         dateAbrogation;
    private Long         id;
    // si la parcelle est sur un PPR de Georisques
    private boolean      existsInGeorisque;
    // si la parcelle est un PPR du GPU
    private boolean      existsInGpu;
    // si ce gaspar a une carte dans le GPU ou Georisques
    private boolean      existsCarte;
    private List<String> assiettes = new ArrayList<>();
    private String       idAssietteErrial;
    
}
  
