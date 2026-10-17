package cr.ac.tec.cvexpert.usuarios.dto;

public record ApiError(int status, String message, String path, long timestamp) {
}
