package fr.gouv.beta.fabnum.kelrisks.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AccueilController {
    
    @RequestMapping("/")
    public String accueil() {
        
        return "index.html";
    }
    
    @RequestMapping("/sanity_test")
    @ResponseBody
    public String greeting() {
        
        return "Hello World";
    }
}
