package cr.ac.tec.cvexpert.usuarios.repository;

import cr.ac.tec.cvexpert.usuarios.model.HelpContact;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HelpContactRepository extends MongoRepository<HelpContact, String> {

    List<HelpContact> findByLangOrderByPositionAsc(String lang);
}
