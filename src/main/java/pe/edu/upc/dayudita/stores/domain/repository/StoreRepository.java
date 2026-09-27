//Interface to access the database that extends JPARepository

package pe.edu.upc.dayudita.stores.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.dayudita.stores.domain.model.Store;

public interface StoreRepository extends JpaRepository<Store,Long> {
}
