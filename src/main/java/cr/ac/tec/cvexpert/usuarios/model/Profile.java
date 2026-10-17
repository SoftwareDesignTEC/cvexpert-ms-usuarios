package cr.ac.tec.cvexpert.usuarios.model;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Perfil profesional. El {@code id} es el {@code sub} de Google. Fechas en epoch ms UTC. */
@Document(collection = "profiles")
public class Profile {

    @Id
    private String id;
    private String fullName = "";
    @Indexed(unique = true)
    private String email;
    private String phone;
    private String address;
    private String photoUrl;
    private String bio;
    private List<String> types = new ArrayList<>();
    private List<String> modalities = new ArrayList<>();
    private List<String> sectors = new ArrayList<>();
    private Role role = Role.USER;
    private long createdAt;
    private long updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public List<String> getTypes() { return types; }
    public void setTypes(List<String> types) { this.types = types; }
    public List<String> getModalities() { return modalities; }
    public void setModalities(List<String> modalities) { this.modalities = modalities; }
    public List<String> getSectors() { return sectors; }
    public void setSectors(List<String> sectors) { this.sectors = sectors; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
