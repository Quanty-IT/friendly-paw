package modules.MedicineBrand.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class MedicineBrand {

    private UUID uuid;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Construtor da classe MedicineBrand.
     * 
     * @param uuid UUID único da marca de medicamento
     * @param name Nome da marca de medicamento
     * @param createdAt Data de criação do registro
     * @param updatedAt Data da última atualização do registro
     */
    public MedicineBrand(UUID uuid, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.uuid = uuid;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Retorna o UUID da marca de medicamento.
     * 
     * @return UUID da marca de medicamento
     */
    public UUID getUuid() { return uuid; }
    
    /**
     * Define o UUID da marca de medicamento.
     * 
     * @param uuid UUID da marca de medicamento
     */
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    /**
     * Retorna o nome da marca de medicamento.
     * 
     * @return Nome da marca de medicamento
     */
    public String getName() { return name; }
    
    /**
     * Define o nome da marca de medicamento.
     * 
     * @param name Nome da marca de medicamento
     */
    public void setName(String name) { this.name = name; }

    /**
     * Retorna a data de criação do registro.
     * 
     * @return Data de criação do registro
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    /**
     * Define a data de criação do registro.
     * 
     * @param createdAt Data de criação do registro
     */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Retorna a data da última atualização do registro.
     * 
     * @return Data da última atualização do registro
     */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    /**
     * Define a data da última atualização do registro.
     * 
     * @param updatedAt Data da última atualização do registro
     */
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}