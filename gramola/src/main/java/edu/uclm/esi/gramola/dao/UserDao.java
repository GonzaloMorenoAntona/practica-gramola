package edu.uclm.esi.gramola.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.uclm.esi.gramola.model.User;

@Repository
public interface UserDao extends JpaRepository <User, String> {

    User findByEmailAndPwd(String email, String pwd);//busca usuario por email y pwd
    Optional<User> findByCreationToken_Id(String tokenId); //busca usuario por token de creacion
    Optional<User> findByClientId(String clientId); //busca usuario por clientId
    Optional<User> findByRecoveryTokenId(String tokenId);// sirve para buscar usuario por token de recuperacion
   
}
