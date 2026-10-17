package cr.ac.tec.cvexpert.usuarios.dto;

import java.util.List;

public record HelpContactsResponse(List<Contact> contacts) {

    public record Contact(String type, String label, String email) {
    }
}
