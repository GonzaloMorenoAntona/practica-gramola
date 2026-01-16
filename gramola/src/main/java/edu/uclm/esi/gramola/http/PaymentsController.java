package edu.uclm.esi.gramola.http;

import java.util.HashMap;
import java.util.List;
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

import edu.uclm.esi.gramola.model.Price;
import edu.uclm.esi.gramola.model.StripeTransaction;
import edu.uclm.esi.gramola.model.User;
import edu.uclm.esi.gramola.services.PaymentService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("payments")
@CrossOrigin(origins = { "http://127.0.0.1:4200" }, allowCredentials = "true")
public class PaymentsController {

    @Autowired
    private PaymentService service;

    @GetMapping("/plans")
    public List<Price> getSubscriptionPlans() {
        return this.service.getSubscriptionPlans();
    }
    //prepara todo antes de que se vea el formulario de tarjeta de Stripe
    @PostMapping("/prepay")
    public StripeTransaction prepay(HttpSession session, @RequestBody Map<String, Object> body) {
        try {
            Long priceId = Long.valueOf(body.get("priceId").toString());
            String token = (String) body.get("token");

            User user = this.service.findByCreationToken(token);
            String email = user.getEmail();
            
            StripeTransaction transactionDetails = this.service.iniciarTransaccionSuscripcion(priceId, email);
            
            session.setAttribute("transactionDetails", transactionDetails);
            return transactionDetails;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    // valida el pago una vez introducidos los datos de la tarjeta
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirm(HttpSession session, @RequestBody Map<String, Object> body) {
        Object tokenObj =  body.get("token");
        Object transactionIdObj =  body.get("transactionId");
        Object priceIdObj =  body.get("priceId");

        if (tokenObj == null || transactionIdObj == null || priceIdObj == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan parámetros en la solicitud.");
        }

        try {
            String token = tokenObj.toString();
            String transactionId = transactionIdObj.toString();
            
            Long priceId = Long.parseLong(priceIdObj.toString());
            this.service.confirmarPagoRegistro(token, transactionId, priceId);

            session.removeAttribute("transactionDetails");
            
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Pago confirmado y cuenta activada. Redirigiendo a login...");
            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al confirmar el pago: " + e.getMessage());
        }
    }

    @PostMapping("/prepay-song")
    public Map<String, String> prepaySong(@RequestBody Map<String, Object> body) {
        try {
            String songName = (String) body.get("songName");
            String email = (String) body.get("email"); // desde que bar se hace la cmpra
            
            String clientSecret = this.service.prepararPagoCancion(songName, email);

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", clientSecret);
            return response;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al preparar el pago: " + e.getMessage());
        }
    }

    @GetMapping("/song-price")
    public Price getSongPrice() {
        return this.service.getSongPrice();
    }
}
