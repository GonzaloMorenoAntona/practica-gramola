package edu.uclm.esi.gramola.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Token {
    @Id @Column(length=36)
    private String id;
    private long creationTime;
    private long useTime=0;

    public Token() {
        this.id = java.util.UUID.randomUUID().toString();
        this.creationTime = System.currentTimeMillis();
        
    }
    
    public long getUseTime() {
        return useTime;
    }
    public void setUseTime(long useTime) {
        this.useTime = useTime;
    }   
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public long getCreationTime() {
        return this.creationTime;
    }
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
    public boolean isUsed() {
        return this.useTime>0;
    }

    public void use() {
        this.useTime = System.currentTimeMillis();
    }

}
