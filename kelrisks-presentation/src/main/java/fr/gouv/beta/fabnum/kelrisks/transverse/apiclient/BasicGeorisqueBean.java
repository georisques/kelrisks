package fr.gouv.beta.fabnum.kelrisks.transverse.apiclient;

import java.io.Serializable;

import lombok.Data;

@Data
public class BasicGeorisqueBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    Integer results;
    Integer page;
    Integer total_pages;
    Integer response_code;
    String  message;
    String  next;
    String  previous;
}
