package modules.MedicineBrand.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class MedicineBrand {

    private UUID uuid;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MedicineBrand(UUID uuid, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.uuid = uuid;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}