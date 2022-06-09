package main.model.repositories;

import main.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByCode(String code);

    boolean existsByIdAndEmailIgnoreCase(int id, String email);

    boolean existsByEmailIgnoreCase(String email);
}
