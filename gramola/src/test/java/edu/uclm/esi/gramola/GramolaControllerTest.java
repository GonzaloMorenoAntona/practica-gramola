package edu.uclm.esi.gramola;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import edu.uclm.esi.gramola.dao.UserDao;
import edu.uclm.esi.gramola.model.User;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class GramolaControllerTest {

    @Autowired
    private MockMvc server;

    @Autowired
    private UserDao userDao;

    // CASO 1: Intentar registrarse con contraseñas distintas 
    @Test
    @Order(1)
    void testRegistroFallidoPassword() throws Exception {
        JSONObject usuario = new JSONObject();
        usuario.put("email", "pepe@prueba.com");
        usuario.put("pwd1", "patata123");
        usuario.put("pwd2", "limon123"); // Son distintas
        usuario.put("bar", "Bar Pepe");
        usuario.put("clientId", "123");
        usuario.put("clientSecret", "123");

        this.server.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuario.toString()))
                .andExpect(status().isConflict());
    }

    // CASO 2: Registro correcto 
    @Test
    @Order(2)
    void testRegistroCorrecto() throws Exception {
        JSONObject usuario = new JSONObject();
        usuario.put("email", "pepe@prueba.com");
        usuario.put("pwd1", "patata123"); // Coinciden
        usuario.put("pwd2", "patata123");
        usuario.put("bar", "Bar Pepe");
        usuario.put("clientId", "123");
        usuario.put("clientSecret", "123");

        this.server.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuario.toString()))
                .andExpect(status().isOk());
    }
    
    // CASO 3: Login Fallido por Usuario no confirmado
    @Test
    @Order(3)
    void testLoginFallido() throws Exception {
        JSONObject credenciales = new JSONObject();
        credenciales.put("email", "pepe@prueba.com");
        credenciales.put("pwd", "patata123");

        this.server.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credenciales.toString()))
                .andExpect(status().isNotAcceptable());
    }

    // CASO 4: Confirmar Cuenta y Login Correcto 
    @Test
    @Order(4)
    void testLoginCorrecto() throws Exception {
        // buscamos el usuario en la BD para coger su token real
        User pepe = userDao.findById("pepe@prueba.com").orElseThrow();
        String token = pepe.getCreationToken().getId();

        // simulamos el clic en el enlace de confirmación
        this.server.perform(get("/users/confirmToken/pepe@prueba.com")
                .param("token", token))
                .andExpect(status().is3xxRedirection()); // Esperamos redirección al pago

        // login correcto ahora que está confirmado
        JSONObject credenciales = new JSONObject();
        credenciales.put("email", "pepe@prueba.com");
        credenciales.put("pwd", "patata123");

        this.server.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credenciales.toString()))
                .andExpect(status().isOk());
    }

    // CASO 5: Registro Duplicado el cual debe fallar porque ya existe el usuario
    @Test
    @Order(5)
    void testRegistroDuplicado() throws Exception {
        JSONObject usuario = new JSONObject();
        usuario.put("email", "pepe@prueba.com");
        usuario.put("pwd1", "patata123");
        usuario.put("pwd2", "patata123");
        usuario.put("bar", "Bar Pepe 2");
        usuario.put("clientId", "123");
        usuario.put("clientSecret", "123");

        this.server.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuario.toString()))
                .andExpect(status().isConflict()); // Error 409
    }

    // CASO 6: Login con contraseña mal 
    @Test
    @Order(6)
    void testLoginPasswordIncorrecta() throws Exception {
        JSONObject credenciales = new JSONObject();
        credenciales.put("email", "pepe@prueba.com");
        credenciales.put("pwd", "contraseñaFalsa"); 

        this.server.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credenciales.toString()))
                .andExpect(status().isForbidden()); // Error 403
    }
}