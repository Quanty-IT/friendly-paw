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
        
        /**
         * Construtor do enum Frequency.
         * 
         * @param displayName Nome de exibição da frequência em português
         */
        Frequency(String displayName) { this.displayName = displayName; }
        
        /**
         * Retorna o nome de exibição da frequência.
         * 
         * @return Nome de exibição da frequência em português
         */
        public String getDisplayName() { return displayName; }
        
        /**
         * Retorna o nome de exibição da frequência.
         * 
         * @return Nome de exibição da frequência em português
         */
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

        /**
         * Verifica se a frequência é recorrente (diferente de DOES_NOT_REPEAT).
         * 
         * @return true se a frequência é recorrente, false caso contrário
         */
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

    /**
     * Construtor padrão da classe MedicineApplication.
     */
    public MedicineApplication() {}

    /**
     * Retorna o UUID da aplicação (alias para getApplicationUuid).
     * 
     * @return UUID da aplicação
     */
    public UUID getUuid() { return applicationUuid; }
    
    /**
     * Define o UUID da aplicação (alias para setApplicationUuid).
     * 
     * @param uuid UUID da aplicação
     */
    public void setUuid(UUID uuid) { this.applicationUuid = uuid; }

    /**
     * Retorna o UUID da aplicação.
     * 
     * @return UUID da aplicação
     */
    public UUID getApplicationUuid() { return applicationUuid; }
    
    /**
     * Define o UUID da aplicação.
     * 
     * @param applicationUuid UUID da aplicação
     */
    public void setApplicationUuid(UUID applicationUuid) { this.applicationUuid = applicationUuid; }

    /**
     * Retorna o UUID do medicamento aplicado.
     * 
     * @return UUID do medicamento
     */
    public UUID getMedicineUuid() { return medicineUuid; }
    
    /**
     * Define o UUID do medicamento aplicado.
     * 
     * @param medicineUuid UUID do medicamento
     */
    public void setMedicineUuid(UUID medicineUuid) { this.medicineUuid = medicineUuid; }

    /**
     * Retorna o UUID do usuário que aplicou o medicamento.
     * 
     * @return UUID do usuário
     */
    public UUID getUserUuid() { return userUuid; }
    
    /**
     * Define o UUID do usuário que aplicou o medicamento.
     * 
     * @param userUuid UUID do usuário
     */
    public void setUserUuid(UUID userUuid) { this.userUuid = userUuid; }

    /**
     * Retorna o UUID do animal que recebeu o medicamento.
     * 
     * @return UUID do animal
     */
    public UUID getAnimalUuid() { return animalUuid; }
    
    /**
     * Define o UUID do animal que recebeu o medicamento.
     * 
     * @param animalUuid UUID do animal
     */
    public void setAnimalUuid(UUID animalUuid) { this.animalUuid = animalUuid; }

    /**
     * Retorna a data e hora em que o medicamento foi aplicado.
     * 
     * @return Data e hora da aplicação
     */
    public ZonedDateTime getAppliedAt() { return appliedAt; }
    
    /**
     * Define a data e hora em que o medicamento foi aplicado.
     * 
     * @param appliedAt Data e hora da aplicação
     */
    public void setAppliedAt(ZonedDateTime appliedAt) { this.appliedAt = appliedAt; }

    /**
     * Retorna a quantidade de medicamento aplicada.
     * 
     * @return Quantidade aplicada
     */
    public Integer getQuantity() { return quantity; }
    
    /**
     * Define a quantidade de medicamento aplicada.
     * 
     * @param quantity Quantidade aplicada
     */
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    /**
     * Retorna a data e hora da próxima aplicação (se houver recorrência).
     * 
     * @return Data e hora da próxima aplicação
     */
    public ZonedDateTime getNextApplicationAt() { return nextApplicationAt; }
    
    /**
     * Define a data e hora da próxima aplicação (se houver recorrência).
     * 
     * @param nextApplicationAt Data e hora da próxima aplicação
     */
    public void setNextApplicationAt(ZonedDateTime nextApplicationAt) { this.nextApplicationAt = nextApplicationAt; }

    /**
     * Retorna a frequência de aplicação do medicamento.
     * 
     * @return Frequência de aplicação
     */
    public Frequency getFrequency() { return frequency; }
    
    /**
     * Define a frequência de aplicação do medicamento.
     * 
     * @param frequency Frequência de aplicação
     */
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }

    /**
     * Retorna a data e hora em que a recorrência termina.
     * 
     * @return Data e hora do término da recorrência
     */
    public ZonedDateTime getEndsAt() { return endsAt; }
    
    /**
     * Define a data e hora em que a recorrência termina.
     * 
     * @param endsAt Data e hora do término da recorrência
     */
    public void setEndsAt(ZonedDateTime endsAt) { this.endsAt = endsAt; }

    /**
     * Retorna a data de criação do registro.
     * 
     * @return Data de criação
     */
    public ZonedDateTime getCreatedAt() { return createdAt; }
    
    /**
     * Define a data de criação do registro.
     * 
     * @param createdAt Data de criação
     */
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Retorna o ID do evento no Google Calendar associado a esta aplicação.
     * 
     * @return ID do evento no Google Calendar
     */
    public String getGoogleCalendarGoogleCalendarId() { return googleCalendarGoogleCalendarId; }
    
    /**
     * Define o ID do evento no Google Calendar associado a esta aplicação.
     * 
     * @param googleCalendarGoogleCalendarId ID do evento no Google Calendar
     */
    public void setGoogleCalendarGoogleCalendarId(String googleCalendarGoogleCalendarId) { this.googleCalendarGoogleCalendarId = googleCalendarGoogleCalendarId; }

    /**
     * Retorna uma representação em string do objeto MedicineApplication.
     * 
     * @return String formatada com informações da aplicação de medicamento
     */
    @Override public String toString() {
        return "MedicineApplication{uuid=%s, animal=%s, med=%s, qty=%s, googleCalendarId=%s}"
                .formatted(applicationUuid, animalUuid, medicineUuid, quantity, googleCalendarGoogleCalendarId);
    }
}
