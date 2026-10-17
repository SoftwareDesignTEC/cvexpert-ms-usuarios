package cr.ac.tec.cvexpert.usuarios.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/** Documento legal en un idioma. {@code kind} es {@code privacy} o {@code terms}. */
// Cosmos DB (API de MongoDB) exige un índice para poder ordenar.
@CompoundIndex(name = "kind_lang_date", def = "{'kind': 1, 'lang': 1, 'effectiveDate': -1}")
@Document(collection = "legal_documents")
public class LegalDocument {

    @Id
    private String id;
    private String kind;
    private String lang;
    private String title;
    private String version;
    private long effectiveDate;
    private String content;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public long getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(long effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
