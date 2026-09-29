
package pe.edu.upc.dayudita.stores.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.dayudita.stores.domain.model.Store;

public interface StoreRepository extends JpaRepository<Store,Long> {
    java.util.Optional<Store> findByName(String name);
}
