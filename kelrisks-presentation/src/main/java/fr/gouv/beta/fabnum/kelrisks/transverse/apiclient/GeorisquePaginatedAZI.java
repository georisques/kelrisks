package fr.gouv.beta.fabnum.kelrisks.transverse.apiclient;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GeorisquePaginatedAZI extends BasicGeorisqueBean {

    private static final long serialVersionUID = 1L;

    List<AZI> data = new ArrayList<>();
    
    @Data
    public static class AZI implements Serializable {
        
    	private static final long serialVersionUID = 1L;
        
        String              code_national_azi;
        String              libelle_azi;
        List<LibelleRisque> liste_libelle_risque = new ArrayList<>();
        
        @Data
        public static class LibelleRisque implements Serializable {
            
            private static final long serialVersionUID = 1L;
            
            String num_risque;
            String libelle_risque_long;
        }
    }
}
