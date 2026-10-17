package cr.ac.tec.cvexpert.usuarios.repository;

import cr.ac.tec.cvexpert.usuarios.model.LegalDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LegalDocumentRepository extends MongoRepository<LegalDocument, String> {

    /** Versión vigente: la de fecha efectiva más reciente para ese tipo e idioma. */
    Optional<LegalDocument> findFirstByKindAndLangOrderByEffectiveDateDesc(String kind, String lang);
}
