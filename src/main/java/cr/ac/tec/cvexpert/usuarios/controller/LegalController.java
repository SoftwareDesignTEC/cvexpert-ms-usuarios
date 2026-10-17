package cr.ac.tec.cvexpert.usuarios.controller;

import cr.ac.tec.cvexpert.usuarios.dto.LegalDocumentResponse;
import cr.ac.tec.cvexpert.usuarios.service.LegalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/legal")
public class LegalController {

    private final LegalService service;

    public LegalController(LegalService service) {
        this.service = service;
    }

    @GetMapping("/privacy")
    public LegalDocumentResponse privacy(@RequestParam(required = false) String lang) {
        return service.current("privacy", lang);
    }

    @GetMapping("/terms")
    public LegalDocumentResponse terms(@RequestParam(required = false) String lang) {
        return service.current("terms", lang);
    }
}
