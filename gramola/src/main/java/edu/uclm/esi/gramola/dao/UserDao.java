package edu.uclm.esi.gramola.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.uclm.esi.gramola.model.User;

@Repository
public interface UserDao extends JpaRepository <User, String> {

    User findByEmailAndPwd(String email, String pwd);
    Optional<User> findByCreationToken_Id(String tokenId); 
    Optional<User> findByClientId(String clientId);
   
}
