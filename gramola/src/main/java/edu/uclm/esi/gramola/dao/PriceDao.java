package edu.uclm.esi.gramola.dao;
import edu.uclm.esi.gramola.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceDao extends JpaRepository<Price, Long> {
    // Buscará automáticamente por el campo 'type'
    List<Price> findByType(String type);
}
