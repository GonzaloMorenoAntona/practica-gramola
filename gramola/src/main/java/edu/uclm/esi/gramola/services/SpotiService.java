
package edu.uclm.esi.gramola.services;


import edu.uclm.esi.gramola.dao.UserDao;
import edu.uclm.esi.gramola.model.SpotiToken;
import edu.uclm.esi.gramola.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient; 

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Service
public class SpotiService {

    @Autowired
    private UserDao userDao;
    

    // URL base de la API de tokens de Spotify
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    // URL de redirección 
    private static final String REDIRECT_URI = "http://127.0.0.1:4200/callback";


    public SpotiToken getAuthorizationToken(String code, String clientId) { //el code es un código temporal de spotify
        // buscar el User usando el clientId
        Optional<User> userOpt = userDao.findByClientId(clientId);
        if (userOpt.isEmpty()) {
             throw new RuntimeException("User not found for clientId: " + clientId);
        }
        User user = userOpt.get();
        String clientSecret = user.getClientSecret();

        // preparar el cuerpo de la petición POST a Spotify
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code); //el codigo temporal que nos da spotify
        form.add("grant_type", "authorization_code"); //grant_type es un codigo de autorizacion
        form.add("redirect_uri", REDIRECT_URI); 

        // preparar la cabecera de autenticación Basic
        String header = this.basicAuth(clientId, clientSecret); //codifica en base64 el clientId y clientSecret
        RestClient restClient = RestClient.create(); // Crear una instancia de RestClient

        SpotiToken token = restClient.post() 
                    .uri(TOKEN_URL) // establecer la URL
                    .header(HttpHeaders.AUTHORIZATION, header) // establecer la cabecera de autenticacion
                    .body(form) // establecer el cuerpo de la peticion
                    .retrieve() // indicar que se quiere recuperar la respuesta
                    .body(SpotiToken.class); // mapear directamente el cuerpo de la respuesta JSON a un objeto SpotiToken


        return token; //devuelve el token que contiene access token y refresh token

    }
    // método auxiliar para crear la cabecera de autenticación Basic, ya que hay que codificar el clientId y clientSecret en base64
    private String basicAuth(String clientId, String clientSecret) {
        String pair = clientId + ":" + clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(pair.getBytes(StandardCharsets.UTF_8));
    }

}
