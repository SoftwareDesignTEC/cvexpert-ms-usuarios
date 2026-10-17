package cr.ac.tec.cvexpert.usuarios.dto;

import java.util.List;

/** Respuesta paginada con la misma forma que usan los clientes desde la Fase I. */
public record PageResponse<T>(List<T> items, long totalRecords, int page, int pageSize) {
}
