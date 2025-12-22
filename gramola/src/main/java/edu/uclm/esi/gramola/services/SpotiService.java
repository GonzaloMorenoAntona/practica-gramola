// src/main/java/edu/uclm/esi/gramola/services/SpotiService.java
package edu.uclm.esi.gramola.services;


import edu.uclm.esi.gramola.dao.UserDao;
import edu.uclm.esi.gramola.model.SpotiToken;
import edu.uclm.esi.gramola.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient; // Usamos RestClient, que es la forma moderna

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Service
public class SpotiService {

    @Autowired
    private UserDao userDao;
    

    // URL base de la API de tokens de Spotify
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    // URL de redirección (debe coincidir con la registrada en la app de Spotify y con la usada en el frontend)
    private static final String REDIRECT_URI = "http://127.0.0.1:4200/callback";


    public SpotiToken getAuthorizationToken(String code, String clientId) {
        // 1. Buscar el User (y por tanto el clientSecret) usando el clientId
        Optional<User> userOpt = userDao.findByClientId(clientId);
        if (userOpt.isEmpty()) {
             throw new RuntimeException("User not found for clientId: " + clientId);
        }
        User user = userOpt.get();
        String clientSecret = user.getClientSecret();

        // 2. Preparar el cuerpo de la petición POST a Spotify
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("grant_type", "authorization_code");
        form.add("redirect_uri", REDIRECT_URI); // Hardcoded como en el documento

        // 3. Preparar la cabecera de autenticación Basic
        String header = this.basicAuth(clientId, clientSecret);
        RestClient restClient = RestClient.create(); // Crear una instancia de RestClient

        SpotiToken token = restClient.post() // <-- Usar el RestClient inyectado
                    .uri(TOKEN_URL) // <-- Establecer la URL
                    .header(HttpHeaders.AUTHORIZATION, header) // <-- Establecer la cabecera de autenticacion
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED) // <-- Establecer el tipo de contenido
                    .body(form) // <-- Establecer el cuerpo de la peticion
                    .retrieve() // <-- Indicar que se quiere recuperar la respuesta
                    .body(SpotiToken.class); // <-- Mapear directamente el cuerpo de la respuesta JSON a un objeto SpotiToken


        return token;

    }
    private String basicAuth(String clientId, String clientSecret) {
        
        String pair = clientId + ":" + clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(pair.getBytes(StandardCharsets.UTF_8));
    }

}
