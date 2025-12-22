package edu.uclm.esi.gramola.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.gramola.dao.UserDao;
import edu.uclm.esi.gramola.model.Token;
import edu.uclm.esi.gramola.model.User;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    

    // UserService.java
    public void register(String bar, String email, String pwd, String clientId, String clientSecret) {
    // 1. Validar si ya existe un usuario con ese email y está activo → error
        Optional<User> existing = userDao.findById(email);
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getCreationToken() != null && user.getCreationToken().isUsed()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
            } else {
                // Borrar cuenta anterior no confirmada
                userDao.deleteById(email);
            }
        }

        // 2. Crear nuevo usuario
        User user = new User();
        user.setBarName(bar);
        user.setEmail(email);
        user.setPwd(PasswordUtil.hash(pwd));
        user.setClientId(clientId);
        user.setClientSecret(clientSecret);
        Token token = new Token(); // genera un UUID en el constructor
        user.setCreationToken(token);

        userDao.save(user);

        System.out.println("Enlace de confirmación:");
        System.out.println("http://127.0.0.1:8080/users/confirmToken/" + email + "?token=" + user.getCreationToken().getId());
        
}

    public void confirmToken(String email, String token) {
        // Buscar en la BD (NO en HashMap)
        User user = this.userDao.findById(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Token userToken = user.getCreationToken();
        if (!userToken.getId().equals(token)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid token");
        }

        if (userToken.getCreationTime() < System.currentTimeMillis() - 180000) {
            throw new ResponseStatusException(HttpStatus.GONE, "Token expired");
        }

        if (userToken.isUsed()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Token already used");
        }

        // Marcar token como usado y guardar
        userToken.use();
        this.userDao.save(user); // 
    }
    public User login(String email, String pwd) {
        // pwd = PasswordUtil.hash(pwd); // Si no usas cifrado, coméntalo
        User user = this.userDao.findById(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Bad email or password"));

        // Verifica si la cuenta está confirmada
        if (!user.getCreationToken().isUsed()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Account not confirmed");
        }

        if (!PasswordUtil.verify(pwd, user.getPwd())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bad email or password");
        }

        return user;
    } 
    
    public void delete(String email) {
        this.userDao.deleteById(email);
    }
    
}
 

