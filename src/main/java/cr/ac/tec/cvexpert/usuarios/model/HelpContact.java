package cr.ac.tec.cvexpert.usuarios.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/** Contacto del centro de ayuda en un idioma. {@code position} define el orden. */
// Cosmos DB (API de MongoDB) exige un índice para poder ordenar.
@CompoundIndex(name = "lang_position", def = "{'lang': 1, 'position': 1}")
@Document(collection = "help_contacts")
public class HelpContact {

    @Id
    private String id;
    private String lang;
    private int position;
    private String type;
    private String label;
    private String email;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
