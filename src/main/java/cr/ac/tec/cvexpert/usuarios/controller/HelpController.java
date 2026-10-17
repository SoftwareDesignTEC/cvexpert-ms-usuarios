package cr.ac.tec.cvexpert.usuarios.controller;

import cr.ac.tec.cvexpert.usuarios.dto.HelpContactsResponse;
import cr.ac.tec.cvexpert.usuarios.service.HelpService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelpController {

    private final HelpService service;

    public HelpController(HelpService service) {
        this.service = service;
    }

    @GetMapping("/help")
    public HelpContactsResponse help(@RequestParam(required = false) String lang) {
        return service.contacts(lang);
    }
}
