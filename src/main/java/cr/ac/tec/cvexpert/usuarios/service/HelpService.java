package cr.ac.tec.cvexpert.usuarios.service;

import cr.ac.tec.cvexpert.usuarios.dto.HelpContactsResponse;
import cr.ac.tec.cvexpert.usuarios.model.HelpContact;
import cr.ac.tec.cvexpert.usuarios.repository.HelpContactRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HelpService {

    private final HelpContactRepository repository;

    public HelpService(HelpContactRepository repository) {
        this.repository = repository;
    }

    /** Contactos en el idioma pedido; si no hay, en español. */
    public HelpContactsResponse contacts(String lang) {
        List<HelpContact> found = repository.findByLangOrderByPositionAsc(Languages.normalize(lang));
        if (found.isEmpty()) {
            found = repository.findByLangOrderByPositionAsc(Languages.DEFAULT);
        }
        return new HelpContactsResponse(found.stream()
                .map(c -> new HelpContactsResponse.Contact(c.getType(), c.getLabel(), c.getEmail()))
                .toList());
    }
}
