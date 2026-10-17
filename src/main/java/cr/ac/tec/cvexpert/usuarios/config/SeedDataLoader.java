package cr.ac.tec.cvexpert.usuarios.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cr.ac.tec.cvexpert.usuarios.model.HelpContact;
import cr.ac.tec.cvexpert.usuarios.model.LegalDocument;
import cr.ac.tec.cvexpert.usuarios.repository.HelpContactRepository;
import cr.ac.tec.cvexpert.usuarios.repository.LegalDocumentRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Carga los textos legales y los contactos de ayuda de src/main/resources/seed
 * la primera vez (cuando la colección está vacía). Así no quedan escritos en los clientes.
 */
@Component
public class SeedDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataLoader.class);

    private final AppProperties properties;
    private final LegalDocumentRepository legal;
    private final HelpContactRepository help;
    private final ObjectMapper mapper;

    public SeedDataLoader(AppProperties properties, LegalDocumentRepository legal,
                          HelpContactRepository help, ObjectMapper mapper) {
        this.properties = properties;
        this.legal = legal;
        this.help = help;
        this.mapper = mapper;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (!properties.seedEnabled()) {
            return;
        }
        if (legal.count() == 0) {
            List<LegalDocument> docs = read("seed/legal.json", new TypeReference<>() { });
            legal.saveAll(docs);
            log.info("Cargados {} documentos legales iniciales", docs.size());
        }
        if (help.count() == 0) {
            List<HelpContact> contacts = read("seed/help.json", new TypeReference<>() { });
            help.saveAll(contacts);
            log.info("Cargados {} contactos de ayuda iniciales", contacts.size());
        }
    }

    private <T> T read(String path, TypeReference<T> type) throws IOException {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return mapper.readValue(in, type);
        }
    }
}
