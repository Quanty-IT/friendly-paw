package modules.MedicineApplication.models;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public class MedicineApplication {

    public enum Frequency {
        DOES_NOT_REPEAT("Não se repete"),
        DAILY("Diário"),
        WEEKLY("Semanal"),
        MONTHLY("Mensal"),
        ANNUALLY("Anual"),
        EVERY_WEEKDAY("Todos os dias úteis");

        private final String displayName;

        /**
         * Construtor do enum Frequency.
         * 
         * @param displayName Nome para exibição em português
         */
        Frequency(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Retorna o nome para exibição da frequência.
         * 
         * @return Nome em português da frequência
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Retorna a representação em string da frequência.
         * 
         * @return Nome para exibição da frequência
         */
        @Override
        public String toString() {
            return displayName;
        }

        /**
         * Converte a frequência para formato RRULE do Google Calendar.
         * 
         * @param endDate Data de fim da recorrência (opcional)
         * @return String RRULE válida ou null se não se repete
         */
        public String toRRULE(java.time.ZonedDateTime endDate) {
            if (this == DOES_NOT_REPEAT) {
                return null;
            }

            StringBuilder rule = new StringBuilder("RRULE:");
            
            switch (this) {
                case DAILY:
                    rule.append("FREQ=DAILY");
                    break;
                case WEEKLY:
                    rule.append("FREQ=WEEKLY");
                    break;
                case MONTHLY:
                    rule.append("FREQ=MONTHLY");
                    break;
                case ANNUALLY:
                    rule.append("FREQ=YEARLY");
                    break;
                case EVERY_WEEKDAY:
                    rule.append("FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR");
                    break;
            }

            // Adicionar data de fim se especificada (incluindo o dia final)
            if (endDate != null) {
                // Adicionar 1 dia para incluir o dia final na recorrência
                java.time.ZonedDateTime endDateInclusive = endDate.plusDays(1);
                String endDateStr = endDateInclusive.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
                rule.append(";UNTIL=").append(endDateStr);
            }

            return rule.toString();
        }

        /**
         * Verifica se a frequência é recorrente.
         * 
         * @return true se é recorrente, false caso contrário
         */
        public boolean isRecurring() {
            return this != DOES_NOT_REPEAT;
        }
    }

    private UUID applicationUuid;
    private UUID medicineUuid;
    private UUID userUuid;
    private UUID animalUuid;
    private ZonedDateTime appliedAt;
    private BigDecimal quantity;
    private ZonedDateTime nextApplicationAt;
    private Frequency frequency;
    private ZonedDateTime endsAt;
    private ZonedDateTime createdAt;

    public MedicineApplication() {
    }

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

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public ZonedDateTime getNextApplicationAt() { return nextApplicationAt; }
    public void setNextApplicationAt(ZonedDateTime nextApplicationAt) { this.nextApplicationAt = nextApplicationAt; }

    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }

    public ZonedDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(ZonedDateTime endsAt) { this.endsAt = endsAt; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}