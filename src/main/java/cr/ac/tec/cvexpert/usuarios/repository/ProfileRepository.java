package cr.ac.tec.cvexpert.usuarios.repository;

import cr.ac.tec.cvexpert.usuarios.model.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProfileRepository extends MongoRepository<Profile, String> {
}
