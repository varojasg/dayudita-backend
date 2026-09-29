package pe.edu.upc.dayudita.products.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.dayudita.products.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStore_Id(Long storeId);

    List<Product> findByStore_IdAndActiveTrue(Long storeId);

    Optional<Product> findByIdAndStore_Id(Long productId, Long storeId);

    Optional<Product> findByStore_IdAndName(Long storeId, String name);
}
