package edu.uclm.esi.gramola.http;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.gramola.dao.UserDao;
import edu.uclm.esi.gramola.model.StripeTransaction;
import edu.uclm.esi.gramola.model.Token;
import edu.uclm.esi.gramola.model.User;
import edu.uclm.esi.gramola.services.PaymentService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("payments")
@CrossOrigin(origins = { "http://localhost:4200", "http://127.0.0.1:4200" }, allowCredentials = "true")
public class PaymentsController {

    @Autowired
    private PaymentService service;
    @Autowired
    private UserDao userDao;

    @GetMapping("/prepay")
    public StripeTransaction prepay(HttpSession session) {
        try {
            StripeTransaction transactionDetails = this.service.prepay();
            session.setAttribute("transactionDetails", transactionDetails);
            return transactionDetails;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
// PaymentsController.java
@PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirm(
            HttpSession session,
            @RequestBody Map<String, String> body) {

        String token = body.get("token"); // El token de confirmación de registro (el ID del token)
        String transactionId = body.get("transactionId");

        if (token == null || transactionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan parámetros");
        }

        try {
            // 1. Buscar el usuario por el ID del token de confirmación
            // Asumiendo que tienes un método en PaymentService que lo haga
            // Si no, puedes inyectar UserDao directamente aquí también
            User user = this.service.findByCreationToken(token);
            // 2. Verificar transacción (opcional pero recomendado)
            // this.service.findTransactionById(transactionId); // Asumiendo que tienes este método

            // 3. Modificar el estado del token Y el usuario directamente
            Token userToken = user.getCreationToken();
            if (userToken != null) {
                userToken.use(); // <-- ESTE ES EL PASO CLAVE: Marca el token como usado
            } else {
                // Opcional: Lanzar error si no tiene token
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Usuario no tiene token de confirmación");
            }

            // Asegúrate de que active y paid estén en true
            user.setActive(true);
            user.setPaid(true);
            user.setValidationDate(new Date()); // Fecha de confirmación/pago

            // 4. GUARDAR el usuario actualizado en la base de datos
            this.userDao.save(user); // <-- ESTE ES EL PASO CLAVE: Guarda los cambios

            // 5. Limpiar sesión (si es necesario)
            session.removeAttribute("transactionDetails");
            // 6. Devolver respuesta exitosa como JSON
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Pago confirmado y cuenta activada. Redirigiendo a login...");
            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error para depurar
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al confirmar el pago", e);
        }
    }
    @PostMapping("/prepay-song")
    public Map<String, String> prepaySong(@RequestBody Map<String, Object> body) {
        try {
            String songName = (String) body.get("songName");
            // Precio fijo de 1.00€ (100 céntimos)
            long amount = 100L; 
            
            // Llamamos al PaymentService para preparar el pago
            String clientSecret = this.service.prepareSongPayment(songName, amount);

            // Devolvemos la respuesta en formato JSON
            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", clientSecret);
            return response;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al preparar el pago: " + e.getMessage());
        }
    }
}
