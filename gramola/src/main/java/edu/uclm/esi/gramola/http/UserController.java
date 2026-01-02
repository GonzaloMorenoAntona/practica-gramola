package edu.uclm.esi.gramola.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.gramola.model.User;
import edu.uclm.esi.gramola.services.UserService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("users")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
public class UserController {

    private final UserService userService;

    // Inyección limpia por constructor
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public void register(@RequestBody Map<String, String> body) {
        String bar = body.get("bar");
        String email = body.get("email");
        String pwd1 = body.get("pwd1");
        String pwd2 = body.get("pwd2");
        String clientId = body.get("clientId");
        String clientSecret = body.get("clientSecret");

        // Validaciones básicas (Capa de entrada)
        if (!pwd1.equals(pwd2)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Las contraseñas no coinciden");
        }
        if (pwd1.length() < 8) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La contraseña debe tener al menos 8 caracteres");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email no válido");
        }

        // Delegamos TODA la lógica al servicio (registro + email)
        this.userService.register(bar, email, pwd1, clientId, clientSecret);
    }

    @GetMapping("/confirmToken/{email}")
    public void confirmToken(
        @PathVariable String email,
        @RequestParam String token,
        HttpServletResponse response
    ) throws IOException {
        // Delegamos la validación al servicio
        this.userService.confirmToken(email, token);

        // Si el servicio no lanza excepción, redirigimos
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

        return ResponseEntity.ok(responseBody);
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam String email) {
        this.userService.delete(email);
    }

    @PostMapping("/request-reset-pwd")
    public void requestResetPwd(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        this.userService.requestPasswordRecovery(email);
    }

    @PostMapping("/reset-pwd")
    public void resetPwd(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPwd = body.get("newPwd");
        this.userService.resetPassword(token, newPwd);
    }
}

