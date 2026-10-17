package cr.ac.tec.cvexpert.usuarios.service;

/**
 * Identidad que APIM agrega a cada llamada (X-User-Id y X-User-Email).
 * El servicio confía en estos valores porque solo acepta llamadas con el certificado de APIM.
 */
public record CurrentUser(String userId, String email) {
}
