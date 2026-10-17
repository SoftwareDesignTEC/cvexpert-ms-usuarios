package cr.ac.tec.cvexpert.usuarios.service;

/** Datos del dispositivo que hace la llamada: encabezado X-Client y User-Agent. */
public record ClientInfo(String client, String userAgent) {

    public static ClientInfo of(String client, String userAgent) {
        String c = client == null ? "unknown" : client.trim().toLowerCase();
        if (!c.equals("web") && !c.equals("android")) {
            c = "unknown";
        }
        return new ClientInfo(c, userAgent == null ? "" : userAgent);
    }
}
