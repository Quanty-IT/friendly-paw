
package modules.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Attachment {
    private UUID id;
    private String url;
    private String description;
    private UUID animalId;
    private LocalDateTime createdAt;

    public Attachment(UUID id, String url, String description, UUID animalId, LocalDateTime createdAt) {
        this.id = id;
        this.url = url;
        this.description = description;
        this.animalId = animalId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getAnimalId() { return animalId; }
    public void setAnimalId(UUID animalId) { this.animalId = animalId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
