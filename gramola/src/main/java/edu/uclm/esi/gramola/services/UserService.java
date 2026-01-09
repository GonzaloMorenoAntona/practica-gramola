package edu.uclm.esi.gramola.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.gramola.dao.UserDao;
import edu.uclm.esi.gramola.model.Token;
import edu.uclm.esi.gramola.model.User;

@Service
public class UserService {

    private final UserDao userDao;
    private final JavaMailSender mailSender;

    // Inyección por Constructor (Mejor práctica que @Autowired en atributos)
    @Autowired
    public UserService(UserDao userDao, JavaMailSender mailSender) {
        this.userDao = userDao;
        this.mailSender = mailSender;
    }

    public void register(String bar, String email, String pwd, String clientId, String clientSecret) {
        // 1. Validar si existe
        Optional<User> existing = userDao.findById(email);
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getCreationToken() != null && user.getCreationToken().isUsed()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuario ya registrado.");
            } else {
                userDao.deleteById(email); // Limpiamos registros sucios anteriores
            }
        }

        // 2. Crear y guardar usuario
        User user = new User();
        user.setBarName(bar);
        user.setEmail(email);
        user.setPwd(PasswordUtil.hash(pwd));
        user.setClientId(clientId);
        user.setClientSecret(clientSecret);
        
        Token token = new Token();
        user.setCreationToken(token);
        
        userDao.save(user);

        // 3. Enviar email de bienvenida (Lógica movida desde el Controller)
        String enlace = "http://127.0.0.1:8080/users/confirmToken/" + email + "?token=" + token.getId();
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("gonza578.gm@gmail.com"); 
        message.setTo(email);
        message.setSubject("Bienvenido a Gramola - Confirma tu cuenta");
        message.setText("Hola! Gracias por registrar tu bar.\n\n" +
                        "Haz clic aquí para confirmar y pagar:\n" + enlace);

        // Si falla el envío, saltará la excepción y el Controller devolverá error 500, que es lo correcto.
        mailSender.send(message); 
        System.out.println("Correo de bienvenida enviado a " + email);
    }

    public void confirmToken(String email, String token) {
        User user = this.userDao.findById(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Token userToken = user.getCreationToken();
        
        if (!userToken.getId().equals(token)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Token invalido.");
        }
        if (userToken.getCreationTime() < System.currentTimeMillis() - 180000) { // 3 minutos
            throw new ResponseStatusException(HttpStatus.GONE, "El token ha expirado.");
        }
        if (userToken.isUsed()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Token ya en uso.");
        }

        userToken.use();
        this.userDao.save(user);
    }

    public User login(String email, String pwd) {
        User user = this.userDao.findById(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Email o contraseña incorrecta."));

        if (!user.getCreationToken().isUsed()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Cuenta no confirmada.");
        }
        if (!PasswordUtil.verify(pwd, user.getPwd())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email o contraseña incorrecta.");
        }

        return user;
    } 
    
    public void delete(String email) {
        this.userDao.deleteById(email);
    }

    public void requestPasswordRecovery(String email) {
        User user = this.userDao.findById(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no existe"));

        Token token = new Token(); 
        user.setRecoveryToken(token); 
        this.userDao.save(user);

        String url = "http://127.0.0.1:4200/reset-password?token=" + token.getId();
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("gonza578.gm@gmail.com"); 
        message.setTo(email);
        message.setSubject("Recuperación de contraseña - La Gramola");
        message.setText("Has solicitado cambiar tu contraseña.\nHaz clic aquí para poner una nueva:\n" + url);

        this.mailSender.send(message);
    }

    public void resetPassword(String tokenId, String newPwd) {
        User user = this.userDao.findByRecoveryTokenId(tokenId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token inválido o expirado"));

        Token recoveryToken = user.getRecoveryToken();

        if (recoveryToken.isUsed()) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este enlace ya fue usado");
        }
        
        user.setPwd(PasswordUtil.hash(newPwd)); 
        recoveryToken.use();
        this.userDao.save(user);
    }
}
    

 

