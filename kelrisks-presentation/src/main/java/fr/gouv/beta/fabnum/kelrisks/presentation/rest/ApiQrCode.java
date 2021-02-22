package fr.gouv.beta.fabnum.kelrisks.presentation.rest;

import fr.gouv.beta.fabnum.commun.metier.util.CipherSpecs;
import fr.gouv.beta.fabnum.commun.metier.util.SecurityHelper;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.QueryParam;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class ApiQrCode extends AbstractBasicApi {
    
    @Value("${kelrisks.app.security.passphrase}")
    String passphrase;
    
    private static final String ERROR_MESSAGE = "désolé une erreur est survenue";
    
    public ApiQrCode() {
        // Rien à faire
    }
    
    @GetMapping(value = "/api/qrcode/check", produces = MediaType.TEXT_HTML_VALUE)
    public String checkQrCode(@QueryParam("hash") @NotBlank String hash) {
        
        CipherSpecs cipherSpecs = new CipherSpecs();
        cipherSpecs.IV = Base64.getDecoder().decode(hash.split("###")[0]);
        cipherSpecs.salt = Base64.getDecoder().decode(hash.split("###")[1]);
        String encodedText = hash.split("###")[2];
        
        SecurityHelper securityHelper = new SecurityHelper(cipherSpecs, passphrase, false);
        String         decodedText    = securityHelper.decode(encodedText);
        
        String[] values = decodedText.split("\\|\\|");
        
        try {
            File baseAvis = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "qrcode.html");
            
            org.jsoup.nodes.Document htmlDocument = Jsoup.parse(baseAvis, StandardCharsets.UTF_8.name());
            
            String html = getQrCodeContent(values);
            
            htmlDocument.select("#qrcode_content").append(html);
            
            return htmlDocument.outerHtml();
        }
        catch (Exception e) {
           log.error("Fail to create HTML from QR Code", e);
        }
        
        return "<!DOCTYPE html>\n" +
               "<html lang=\"fr\">\n" +
               "    <head>\n" +
               "        <meta content=\"text/html; charset=utf-8\"\n" +
               "              http-equiv=\"Content-Type\"/>" +
               "    </head>\n" +
               "    <body>\n" +
               "        <h1>Désolé, il y a une erreur lors de la vérification du Code QR</h1>\n" +
               "    <body>\n" +
               "</html>";
    }
    
    private String getQrCodeContent(String[] values) {
    	int nbValues = values.length;
    	StringBuilder html = new StringBuilder("");
        
        html.append("      <p>" + "Nombre de BASIAS sur la parcelle : " + getValueSplit(0, 0, values, nbValues) + "</p>\n");
        addUl(html, 0, values, nbValues);

        html.append("      <p>" + "Nombre de BASIAS dans une parcelle contigüe : " + getValueSplit(1, 0, values, nbValues) + "</p>\n");
        addUl(html, 1, values, nbValues);

        html.append("      <p>" + "Nombre de Basol sur la parcelle : " + getValueSplit(2, 0, values, nbValues) + "</p>\n");
        addUl(html, 2, values, nbValues);

        html.append("      <p>" + "Nombre de Basol dans une parcelle contigüe : " + getValueSplit(3, 0, values, nbValues) + "</p>\n");
        addUl(html, 3, values, nbValues);

        html.append("      <p>" + "Nombre de Secteurs d'Information sur les Sols sur la parcelle : " + getValueSplit(4, 0, values, nbValues) + "</p>\n");
        addUl(html, 4, values, nbValues);

        html.append("      <p>" + "Nombre de Secteurs d'Information sur les Sols dans une parcelle contigüe : " + getValueSplit(5, 0, values, nbValues) + "</p>\n");
        addUl(html, 5, values, nbValues);

        html.append("      <p>" + "Nombre de Installations classées sur la parcelle : " + getValueSplit(6, 0, values, nbValues) + "</p>\n");
        addUl(html, 6, values, nbValues);

        html.append("      <p>" + "Nombre de Installations classées dans une parcelle contigüe : " + getValueSplit(7, 0, values, nbValues) + "</p>\n");
        addUl(html, 7, values, nbValues);


        html.append("      <p>" + "Niveau d'argile" + " : " + getValue(8, values, nbValues) + "</p>\n");
        html.append("      <p>" + "Nombre de PPRs" + " : " + getValueSplit(9, 0, values, nbValues) + "</p>\n");
        addUl(html, 9, values, nbValues);
        
        html.append("      <p>" + "Nombre de TRIs : " + getValue(10, values, nbValues) + "</p>\n");
        html.append("      <p>" + "Nombre de AZIs : " + getValue(11, values, nbValues) + "</p>\n");
        html.append("      <p>" + "Nombre de canalisations : " + getValue(12, values, nbValues) + "</p>\n");
        html.append("      <p>" + "Nombre d'installations nucléaires : " +  getValueSplit(13, 0, values, nbValues) + "</p>\n");
        addUl(html, 13, values, nbValues);
        
        html.append("      <p>" + "Zonage exposition au bruit : " + getValue(14, values, nbValues) + "</p>\n");
        
        return html.toString();
    }
    
    private void addUl(StringBuilder html, int indexVal, String[] values, int nbValues) {
    	if(indexVal < nbValues && values[indexVal].split("==").length > 1) {
           html.append("      <ul>" + Stream.of(getValueSplit(indexVal, 1, values, nbValues).split(",")).map(s -> "         <li>" + s + "</li>\n").collect(Collectors.joining()) + "</ul>\n");
    	}
    }
    
    private String getValue(int indexVal, String[] values, int nbValues) {
    	if(indexVal < nbValues) {
    		String val = values[indexVal];
    		if (val == null || val.equals("null")) {
    			val = "";
        	}
    		return val;
    	}
    	return ERROR_MESSAGE;
    }
    
    private String getValueSplit(int indexVal, int indexSplit, String[] values, int nbValues) {
    	if(indexVal < nbValues) {
        	String value = values[indexVal].split("==")[indexSplit];
        	if (value == null || value.equals("null")) {
        		value = ERROR_MESSAGE;
        	}
        	return value;
    	}
    	return ERROR_MESSAGE;
    }
}
