
package edu.uclm.esi.gramola.model;

import com.fasterxml.jackson.annotation.JsonProperty;

//sirve para convertir automáticamente el texto JSON que te envía Spotify en un Objeto Java que tu código pueda entender y usar
//se usa en la línea final del método getAuthorizationToken de SpotiService.java
public class SpotiToken {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    // Getters y Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
    public String getRefreshToken() {
        return refreshToken;
    }  
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}