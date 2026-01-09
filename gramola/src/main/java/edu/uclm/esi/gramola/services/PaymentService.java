package edu.uclm.esi.gramola.services;

import java.util.Date;
import java.util.List;


import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import edu.uclm.esi.gramola.dao.PriceDao;
import edu.uclm.esi.gramola.dao.StripeTransactionDao;
import edu.uclm.esi.gramola.dao.UserDao;
import edu.uclm.esi.gramola.model.Price;
import edu.uclm.esi.gramola.model.StripeTransaction;
import edu.uclm.esi.gramola.model.Token;
import edu.uclm.esi.gramola.model.User;

@Service
public class PaymentService {

    static {
        // Tu clave de Test de Stripe
        Stripe.apiKey = "sk_test_51SIV0yRm0ClsCnoVDwqAd9ebRIBhNWfjVR5YjfChUYmQqm0BtOSqIAMcpanTNGh4f9R5RKJmtUHYPzXazTcwkWhA00fUleXm34";
    }

    // --- INYECCIÓN DE REPOSITORIOS (DAOs) ---
    @Autowired
    private StripeTransactionDao transactionDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PriceDao priceDao; 


    // --- MÉTODOS PÚBLICOS (Usados por el Controller) ---

    public List<Price> getSubscriptionPlans() {
        return this.priceDao.findByType("SUBSCRIPTION");
    }

    public Price getSongPrice() {
        List<Price> prices = this.priceDao.findByType("SONG");
        if (prices.isEmpty()) {
            // Es mejor lanzar una excepción controlada si no hay precio
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No hay precio configurado para canciones (SONG)");
        }
        return prices.get(0);
    }

    // Lógica para /prepay (Suscripciones)
    public StripeTransaction iniciarTransaccionSuscripcion(Long priceId, String email) throws Exception {
        Price p = this.priceDao.findById(priceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan de suscripción no encontrado"));
        
        return this.prepay(p.getValue(), email);
    }

    // Lógica para /prepay-song (Canciones)
    public String prepararPagoCancion(String songName, String email) throws Exception {
        Price songPrice = this.getSongPrice();
        long amountInCents = (long) (songPrice.getValue() * 100);
        
        return this.prepareSongPaymentInternal(songName, amountInCents, email);
    }

    // Lógica para /confirm (Confirmar registro)
    public void confirmarPagoRegistro(String tokenValue, String transactionId) {
        // 1. Buscar usuario
        User user = this.findByCreationToken(tokenValue);
        
        // 2. Validar token
        Token userToken = user.getCreationToken();
        if (userToken == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no tiene un token de confirmación pendiente.");
        }
        
        // 3. Consumir el token y activar usuario
        userToken.use();
        user.setActive(true);
        user.setPaid(true);
        user.setValidationDate(new Date());

        // 4. Guardar cambios en BD
        this.userDao.save(user);
    }
    
    // Método auxiliar para buscar usuario por token
    public User findByCreationToken(String token) {
        return userDao.findByCreationToken_Id(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token inválido o usuario no encontrado"));
    }

    
    // --- MÉTODOS INTERNOS (Lógica de Stripe) ---

    public StripeTransaction prepay(double amount, String email) throws StripeException {
        long amountInCents = (long) (amount * 100); 

        PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
                .setCurrency("eur")
                .setAmount(amountInCents)
                .build();
        
        PaymentIntent intent = PaymentIntent.create(createParams);
        JSONObject transactionDetails = new JSONObject(intent.toJson());
        
        StripeTransaction st = new StripeTransaction();
        st.setData(transactionDetails);
        st.setEmail(email);
        st.setPaymentType("SUBSCRIPTION");
        this.transactionDao.save(st);
        
        return st;
    }

    private String prepareSongPaymentInternal(String songName, long amountInCents, String email) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("eur")
                .setDescription("Canción: " + songName)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        JSONObject transactionDetails = new JSONObject(intent.toJson());
        
        StripeTransaction st = new StripeTransaction();
        st.setData(transactionDetails);
        st.setEmail(email);
        st.setPaymentType("SONG");
        this.transactionDao.save(st);
        return intent.getClientSecret();
    }
}