package cr.ac.tec.cvexpert.usuarios.service;

import cr.ac.tec.cvexpert.usuarios.exception.BadRequestException;

/** Idiomas admitidos por el contenido del servicio. */
final class Languages {

    static final String DEFAULT = "es";

    private Languages() {
    }

    static String normalize(String lang) {
        if (lang == null || lang.isBlank()) {
            return DEFAULT;
        }
        String l = lang.trim().toLowerCase();
        if (!l.equals("es") && !l.equals("en")) {
            throw new BadRequestException("Idioma no admitido: " + lang + ". Use es o en.");
        }
        return l;
    }
}
