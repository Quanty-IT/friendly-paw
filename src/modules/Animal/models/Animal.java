package modules.Animal.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Animal {
    private UUID uuid;
    private String name;
    private String sex;
    private String species;
    private String breed;
    private String size;
    private String color;
    private LocalDate birthdate;
    private String microchip;
    private String rga;
    private boolean castrated;
    private String fiv;
    private String felv;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Animal(UUID uuid, String name, String sex, String species, String breed, String size, String color,
                  LocalDate birthdate, String microchip, String rga, boolean castrated, String fiv, String felv, String status,
                  String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.uuid = uuid;
        this.name = name;
        this.sex = sex;
        this.species = species;
        this.breed = breed;
        this.size = size;
        this.color = color;
        this.birthdate = birthdate;
        this.microchip = microchip;
        this.rga = rga;
        this.castrated = castrated;
        this.fiv = fiv;
        this.felv = felv;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }

    public String getMicrochip() { return microchip; }
    public void setMicrochip(String microchip) { this.microchip = microchip; }

    public String getRga() { return rga; }
    public void setRga(String rga) { this.rga = rga; }

    public boolean getCastrated() { return castrated; }
    public void setCastrated(boolean castrated) { this.castrated = castrated; }

    public String getFiv() { return fiv; }
    public void setFiv(String fiv) { this.fiv = fiv; }

    public String getFelv() { return felv; }
    public void setFelv(String felv) { this.felv = felv; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
