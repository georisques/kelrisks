package fr.gouv.beta.fabnum.kelrisks.transverse.apiclient;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GeorisquePaginatedTRI extends BasicGeorisqueBean {
    
    private static final long serialVersionUID = 1L;
	
    List<TRI> data = new ArrayList<>();
    
    @Data
    public static class TRI implements Serializable {
        
    	private static final long serialVersionUID = 1L;
        
        String              code_national_tri;
        String              libelle_tri;
        List<LibelleRisque> liste_libelle_risque = new ArrayList<>();
        
        @Data
        public static class LibelleRisque implements Serializable {
            
            private static final long serialVersionUID = 1L;
            
            String num_risque;
            String libelle_risque_long;
        }
    }
}
