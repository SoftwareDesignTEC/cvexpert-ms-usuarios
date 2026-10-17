package cr.ac.tec.cvexpert.usuarios.dto;

import cr.ac.tec.cvexpert.usuarios.model.LegalDocument;

public record LegalDocumentResponse(String id, String title, String version, long effectiveDate, String content) {

    public static LegalDocumentResponse from(LegalDocument d) {
        return new LegalDocumentResponse(d.getId(), d.getTitle(), d.getVersion(), d.getEffectiveDate(), d.getContent());
    }
}
