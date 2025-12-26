package edu.uclm.esi.gramola.services;

import java.util.Date;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;


import edu.uclm.esi.gramola.dao.StripeTransactionDao;
import edu.uclm.esi.gramola.dao.UserDao;

import edu.uclm.esi.gramola.model.StripeTransaction;
import edu.uclm.esi.gramola.model.User;

@Service
public class PaymentService {

    static {
        Stripe.apiKey = "sk_test_51SIV0yRm0ClsCnoVDwqAd9ebRIBhNWfjVR5YjfChUYmQqm0BtOSqIAMcpanTNGh4f9R5RKJmtUHYPzXazTcwkWhA00fUleXm34";
    }

    @Autowired
    private StripeTransactionDao dao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private StripeTransactionDao transactionRepository;
    

    public StripeTransaction prepay() throws StripeException {
       PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
                    .setCurrency("eur")
                    .setAmount(1000L)
                    .build();
        PaymentIntent intent = PaymentIntent.create(createParams);
        JSONObject transactionDetails = new JSONObject(intent.toJson());
        StripeTransaction st = new StripeTransaction();
        st.setData(transactionDetails);
        this.dao.save(st);
        return st;
    }

        // En PaymentService.java
        public User findByCreationToken(String token) {
            return userDao.findByCreationToken_Id(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token inválido"));
        }

        public StripeTransaction findTransactionById(String id) {
            return transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transacción no encontrada"));
        }

        public void activateUser(User user) {
        user.setActive(true);
        user.setPaid(true);
        user.setValidationDate(new Date());
        userDao.save(user);
    }

    public String prepareSongPayment(String songName, long amountInCents) throws Exception {
    PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amountInCents) // El precio en céntimos
            .setCurrency("eur")
            .setDescription("Canción: " + songName)
            .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
            )
            .build();

    PaymentIntent intent = PaymentIntent.create(params);
    return intent.getClientSecret();
}
}

