package modules.MedicineApplication.models;

import java.time.ZonedDateTime;
import java.util.UUID;

public class MedicineApplication {

    public enum Frequency {
        DOES_NOT_REPEAT("Não se repete"),
        DAILY("Diário"),
        WEEKLY("Semanal"),
        MONTHLY("Mensal"),
        ANNUALLY("Anual"),
        EVERY_WEEKDAY("Dias úteis");

        private final String displayName;
        Frequency(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override public String toString() { return displayName; }

        /**
         * Converte para RRULE do Google Calendar.
         * 
         * @param endDate Data final da recorrência
         * @return String representando a RRULE
         */
        public String toRRULE(ZonedDateTime endDate) {
            if (this == DOES_NOT_REPEAT) return null;

            StringBuilder rule = new StringBuilder("RRULE:");
            switch (this) {
                case DAILY -> rule.append("FREQ=DAILY");
                case WEEKLY -> rule.append("FREQ=WEEKLY");
                case MONTHLY -> rule.append("FREQ=MONTHLY");
                case ANNUALLY -> rule.append("FREQ=YEARLY");
                case EVERY_WEEKDAY -> rule.append("FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR");
            }

            // Inclui o dia final (convenção amigável)
            if (endDate != null) {
                String until = endDate.plusDays(1)
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
                rule.append(";UNTIL=").append(until);
            }
            return rule.toString();
        }

        public boolean isRecurring() { return this != DOES_NOT_REPEAT; }
    }

    private UUID applicationUuid;
    private UUID medicineUuid;
    private UUID userUuid;
    private UUID animalUuid;

    private ZonedDateTime appliedAt;
    private Integer   quantity;
    private ZonedDateTime nextApplicationAt;
    private Frequency     frequency;
    private ZonedDateTime endsAt;
    private ZonedDateTime createdAt;

    private String googleCalendarGoogleCalendarId;

    public MedicineApplication() {}

    // Alias compatível com getUuid()/setUuid()
    public UUID getUuid() { return applicationUuid; }
    public void setUuid(UUID uuid) { this.applicationUuid = uuid; }

    public UUID getApplicationUuid() { return applicationUuid; }
    public void setApplicationUuid(UUID applicationUuid) { this.applicationUuid = applicationUuid; }

    public UUID getMedicineUuid() { return medicineUuid; }
    public void setMedicineUuid(UUID medicineUuid) { this.medicineUuid = medicineUuid; }

    public UUID getUserUuid() { return userUuid; }
    public void setUserUuid(UUID userUuid) { this.userUuid = userUuid; }

    public UUID getAnimalUuid() { return animalUuid; }
    public void setAnimalUuid(UUID animalUuid) { this.animalUuid = animalUuid; }

    public ZonedDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(ZonedDateTime appliedAt) { this.appliedAt = appliedAt; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public ZonedDateTime getNextApplicationAt() { return nextApplicationAt; }
    public void setNextApplicationAt(ZonedDateTime nextApplicationAt) { this.nextApplicationAt = nextApplicationAt; }

    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }

    public ZonedDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(ZonedDateTime endsAt) { this.endsAt = endsAt; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public String getGoogleCalendarGoogleCalendarId() { return googleCalendarGoogleCalendarId; }
    public void setGoogleCalendarGoogleCalendarId(String googleCalendarGoogleCalendarId) { this.googleCalendarGoogleCalendarId = googleCalendarGoogleCalendarId; }

    @Override public String toString() {
        return "MedicineApplication{uuid=%s, animal=%s, med=%s, qty=%s, googleCalendarId=%s}"
                .formatted(applicationUuid, animalUuid, medicineUuid, quantity, googleCalendarGoogleCalendarId);
    }
}
