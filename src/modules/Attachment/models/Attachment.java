
package modules.Attachment.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Attachment {
    private UUID uuid;
    private String file;
    private String description;
    private UUID animalUuid;
    private LocalDateTime createdAt;

    public Attachment(UUID uuid, String file, String description, UUID animalUuid, LocalDateTime createdAt) {
        this.uuid = uuid;
        this.file = file;
        this.description = description;
        this.animalUuid = animalUuid;
        this.createdAt = createdAt;
    }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public String getFile() { return file; }
    public void setFile(String file) { this.file = file; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getAnimalUuid() { return animalUuid; }
    public void setAnimalUuid(UUID animalUuid) { this.animalUuid = animalUuid; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
