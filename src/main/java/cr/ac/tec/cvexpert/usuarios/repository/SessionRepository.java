package cr.ac.tec.cvexpert.usuarios.repository;

import cr.ac.tec.cvexpert.usuarios.model.Session;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SessionRepository extends MongoRepository<Session, String> {

    Page<Session> findByUserIdOrderByLastActiveAtDesc(String userId, Pageable pageable);

    List<Session> findByUserIdOrderByLastActiveAtDesc(String userId);
}
