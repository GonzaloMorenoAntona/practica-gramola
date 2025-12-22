package edu.uclm.esi.gramola.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.esi.gramola.model.SpotiToken;
import edu.uclm.esi.gramola.services.SpotiService; // Asegúrate de la ruta
import java.util.Map;

@RestController
@RequestMapping("spoti") // Debe coincidir con backendUrl en el frontend
@CrossOrigin(origins = { "http://127.0.0.1:4200" }, allowCredentials = "true")
public class SpotiController {

    @Autowired
    private SpotiService spotiService;

    @GetMapping("/getAuthorizationToken") 
    public SpotiToken getAuthorizationToken(@RequestParam String code, @RequestParam String clientId) throws Exception { 
        SpotiToken token = this.spotiService.getAuthorizationToken(code, clientId); 
        return token;
    }
}
