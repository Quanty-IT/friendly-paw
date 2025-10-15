package modules.models;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public class MedicineApplication {

    private UUID applicationUuid;
    private Integer medicineId; // ATENÇÃO: Corrigido para Integer, como na migration
    private UUID userUuid;
    private UUID animalUuid;
    private ZonedDateTime appliedAt;
    private BigDecimal quantity;
    private ZonedDateTime nextApplicationAt;
    private String frequency;
    private ZonedDateTime endsAt;
    private ZonedDateTime createdAt;

    // Construtor padrão
    public MedicineApplication() {
    }

    // Para gerar Getters e Setters no IntelliJ:
    // 1. Clique com o botão direito em uma área vazia do código.
    // 2. Vá em "Generate..." (ou use o atalho Alt + Insert).
    // 3. Selecione "Getter and Setter", selecione todos os campos e clique em OK.

    public UUID getApplicationUuid() {
        return applicationUuid;
    }

    public void setApplicationUuid(UUID applicationUuid) {
        this.applicationUuid = applicationUuid;
    }

    public Integer getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Integer medicineId) {
        this.medicineId = medicineId;
    }

    public UUID getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(UUID userUuid) {
        this.userUuid = userUuid;
    }

    public UUID getAnimalUuid() {
        return animalUuid;
    }

    public void setAnimalUuid(UUID animalUuid) {
        this.animalUuid = animalUuid;
    }

    public ZonedDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(ZonedDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public ZonedDateTime getNextApplicationAt() {
        return nextApplicationAt;
    }

    public void setNextApplicationAt(ZonedDateTime nextApplicationAt) {
        this.nextApplicationAt = nextApplicationAt;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public ZonedDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(ZonedDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }
}