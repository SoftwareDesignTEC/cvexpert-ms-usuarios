package cr.ac.tec.cvexpert.usuarios.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Dispositivo desde el que se usó la cuenta. El {@code id} se deriva del usuario,
 * el cliente y el User-Agent, así que el mismo dispositivo actualiza su registro.
 */
// Cosmos DB (API de MongoDB) exige un índice para poder ordenar.
@CompoundIndex(name = "user_recent", def = "{'userId': 1, 'lastActiveAt': -1}")
@Document(collection = "sessions")
public class Session {

    @Id
    private String id;
    private String userId;
    private String deviceName;
    private String platform;
    private String client;
    private long lastActiveAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }
    public long getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(long lastActiveAt) { this.lastActiveAt = lastActiveAt; }
}
