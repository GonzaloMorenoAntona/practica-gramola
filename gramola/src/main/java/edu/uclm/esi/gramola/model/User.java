package edu.uclm.esi.gramola.model;

import java.util.Date;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;


@Entity
public class User {
    @Id // Indica que este campo es la clave primaria
    @Column // Indica que este campo es la clave primaria
    private String email;

    private String pwd;
    private String barName;
    private String clientId;
    private String clientSecret;

    private boolean active = false;
    private boolean paid = false;
    private Date validationDate;
   
     @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "token_id", referencedColumnName = "id")
    private Token creationToken;

    // Getters y Setters CORRECTOS
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPwd() { return pwd; }
    public void setPwd(String pwd) { this.pwd = pwd; }

    public String getBarName() { return barName; }
    public void setBarName(String barName) { this.barName = barName; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public Token getCreationToken() { return creationToken; }
    public void setCreationToken(Token creationToken) { this.creationToken = creationToken; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public Date getValidationDate() { return validationDate; }
    public void setValidationDate(Date validationDate) { this.validationDate = validationDate; }

    

}
