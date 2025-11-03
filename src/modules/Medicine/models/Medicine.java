package modules.Medicine.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Medicine {

    private UUID uuid;
    private String name;
    private UUID brandUuid;
    private String brandName;
    private Integer quantity;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Construtor da classe Medicine.
     * 
     * @param uuid UUID único do medicamento
     * @param name Nome do medicamento
     * @param brandUuid UUID da marca do medicamento
     * @param brandName Nome da marca do medicamento
     * @param quantity Quantidade disponível do medicamento
     * @param description Descrição do medicamento
     * @param isActive Indica se o medicamento está ativo
     * @param createdAt Data de criação do registro
     * @param updatedAt Data da última atualização do registro
     */
    public Medicine(UUID uuid, String name, UUID brandUuid, String brandName, Integer quantity, String description, Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.uuid = uuid;
        this.name = name;
        this.brandUuid = brandUuid;
        this.brandName = brandName;
        this.quantity = quantity;
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Retorna o UUID do medicamento.
     * 
     * @return UUID do medicamento
     */
    public UUID getUuid() { return uuid; }
    
    /**
     * Define o UUID do medicamento.
     * 
     * @param uuid UUID do medicamento
     */
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    /**
     * Retorna o nome do medicamento.
     * 
     * @return Nome do medicamento
     */
    public String getName() { return name; }
    
    /**
     * Define o nome do medicamento.
     * 
     * @param name Nome do medicamento
     */
    public void setName(String name) { this.name = name; }

    /**
     * Retorna o UUID da marca do medicamento.
     * 
     * @return UUID da marca do medicamento
     */
    public UUID getBrandUuid() { return brandUuid; }
    
    /**
     * Define o UUID da marca do medicamento.
     * 
     * @param brandUuid UUID da marca do medicamento
     */
    public void setBrandUuid(UUID brandUuid) { this.brandUuid = brandUuid; }

    /**
     * Retorna o nome da marca do medicamento.
     * 
     * @return Nome da marca do medicamento
     */
    public String getBrandName() { return brandName; }
    
    /**
     * Define o nome da marca do medicamento.
     * 
     * @param brandName Nome da marca do medicamento
     */
    public void setBrandName(String brandName) { this.brandName = brandName; }

    /**
     * Retorna a quantidade disponível do medicamento.
     * 
     * @return Quantidade disponível do medicamento
     */
    public Integer getQuantity() { return quantity; }
    
    /**
     * Define a quantidade disponível do medicamento.
     * 
     * @param quantity Quantidade disponível do medicamento
     */
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    /**
     * Retorna a descrição do medicamento.
     * 
     * @return Descrição do medicamento
     */
    public String getDescription() { return description; }
    
    /**
     * Define a descrição do medicamento.
     * 
     * @param description Descrição do medicamento
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Retorna se o medicamento está ativo.
     * 
     * @return true se o medicamento está ativo, false caso contrário
     */
    public Boolean getIsActive() { return isActive; }
    
    /**
     * Define se o medicamento está ativo.
     * 
     * @param isActive true se o medicamento está ativo, false caso contrário
     */
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

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