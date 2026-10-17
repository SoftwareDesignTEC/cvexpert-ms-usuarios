package cr.ac.tec.cvexpert.usuarios.service;

import cr.ac.tec.cvexpert.usuarios.dto.LegalDocumentResponse;
import cr.ac.tec.cvexpert.usuarios.exception.NotFoundException;
import cr.ac.tec.cvexpert.usuarios.repository.LegalDocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class LegalService {

    private final LegalDocumentRepository repository;

    public LegalService(LegalDocumentRepository repository) {
        this.repository = repository;
    }

    /** Versión vigente en el idioma pedido; si no existe, en español. */
    public LegalDocumentResponse current(String kind, String lang) {
        String l = Languages.normalize(lang);
        return repository.findFirstByKindAndLangOrderByEffectiveDateDesc(kind, l)
                .or(() -> repository.findFirstByKindAndLangOrderByEffectiveDateDesc(kind, Languages.DEFAULT))
                .map(LegalDocumentResponse::from)
                .orElseThrow(() -> new NotFoundException("No hay documento legal '" + kind + "' cargado."));
    }

}
