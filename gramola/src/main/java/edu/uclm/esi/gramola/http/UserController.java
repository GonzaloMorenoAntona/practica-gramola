package edu.uclm.esi.gramola.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.uclm.esi.gramola.dao.UserDao;
import edu.uclm.esi.gramola.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage; 
import org.springframework.mail.javamail.JavaMailSender; 
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.gramola.services.UserService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("users")
@CrossOrigin(origins = "http://127.0.0.1:4200", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserDao userDao;

    @Autowired
    private JavaMailSender mailSender; 

    @PostMapping("/register")
    public void register(@RequestBody Map<String, String> body) {
        String bar = body.get("bar");
        String email = body.get("email");
        String pwd1 = body.get("pwd1");
        String pwd2 = body.get("pwd2");
        String clientId = body.get("clientId");
        String clientSecret = body.get("clientSecret");

        // --- 1. Validaciones ---
        if (!pwd1.equals(pwd2)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Las contraseñas no coinciden");
        }
        if (pwd1.length() < 8) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La contraseña debe tener al menos 8 caracteres");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email no válido");
        }

        // --- 2. Registrar bar en la Base de Datos ---
        // Esto guarda al usuario y genera el token internamente
        this.userService.register(bar, email, pwd1, clientId, clientSecret);

        try {
            User user = this.userDao.findById(email).orElse(null);

            if (user != null && user.getCreationToken() != null) {
                String token = user.getCreationToken().getId();
                String enlace = "http://127.0.0.1:8080/users/confirmToken/" + email + "?token=" + token;

                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("gonza578.gm.com"); 
                message.setTo(email);
                message.setSubject("Bienvenido a Gramola - Confirma tu cuenta");
                message.setText("Hola! Gracias por registrar tu bar.\n\n" +
                                "Haz clic aquí para confirmar y pagar los 10€:\n" + enlace);

                mailSender.send(message);
                System.out.println("Correo enviado a " + email);
            }

        } catch (Exception e) {
            System.out.println("Error enviando email: " + e.getMessage());
        }
    }

    @GetMapping("/confirmToken/{email}")
    public void confirmToken(
        @PathVariable String email,
        @RequestParam String token,
        HttpServletResponse response
    ) throws IOException {
        User user = userDao.findById(email)
            .filter(u -> u.getCreationToken() != null && token.equals(u.getCreationToken().getId()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token inválido"));

        // Redirige al Angular para pagar
        response.sendRedirect("http://127.0.0.1:4200/payment?token=" + token);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String pwd = body.get("pwd");

        User user = this.userService.login(email, pwd);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("clientId", user.getClientId());
        String elBar = (user.getBarName() != null) ? user.getBarName() : ""; 
        responseBody.put("bar", elBar);

        System.err.println("user.getClientId(): " + user.getClientId());
        return ResponseEntity.ok(responseBody);
        
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam String email) {
        this.userService.delete(email);
    }
}

