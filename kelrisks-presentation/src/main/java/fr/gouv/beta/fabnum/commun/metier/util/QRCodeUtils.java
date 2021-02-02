package fr.gouv.beta.fabnum.commun.metier.util;

import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class QRCodeUtils {
    
    public static String generateQRCodePng(String barcodeText) throws Exception {
        
        if (barcodeText.length() > 4296) { throw new Exception("Texte trop long pour la génération d'un QR."); }
        
        ByteArrayOutputStream stream = QRCode.from(barcodeText)
                                               .withSize(300, 300)
                                               .to(ImageType.PNG)
                                               .stream();
        
        //BASE64Encoder encoder = new BASE64Encoder();
        //return "data:image/png;base64," + encoder.encode(stream.toByteArray());

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(stream.toByteArray());
    }
}
