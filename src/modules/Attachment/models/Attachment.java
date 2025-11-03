
package modules.Attachment.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Attachment {
    private UUID uuid;
    private String file;
    private String description;
    private UUID animalUuid;
    private LocalDateTime createdAt;

    /**
     * Construtor da classe Attachment.
     * 
     * @param uuid UUID único do anexo
     * @param file URL ou caminho do arquivo
     * @param description Descrição do anexo
     * @param animalUuid UUID do animal ao qual o anexo pertence
     * @param createdAt Data de criação do registro
     */
    public Attachment(UUID uuid, String file, String description, UUID animalUuid, LocalDateTime createdAt) {
        this.uuid = uuid;
        this.file = file;
        this.description = description;
        this.animalUuid = animalUuid;
        this.createdAt = createdAt;
    }

    /**
     * Retorna o UUID do anexo.
     * 
     * @return UUID do anexo
     */
    public UUID getUuid() { return uuid; }
    
    /**
     * Define o UUID do anexo.
     * 
     * @param uuid UUID do anexo
     */
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    /**
     * Retorna a URL ou caminho do arquivo.
     * 
     * @return URL ou caminho do arquivo
     */
    public String getFile() { return file; }
    
    /**
     * Define a URL ou caminho do arquivo.
     * 
     * @param file URL ou caminho do arquivo
     */
    public void setFile(String file) { this.file = file; }

    /**
     * Retorna a descrição do anexo.
     * 
     * @return Descrição do anexo
     */
    public String getDescription() { return description; }
    
    /**
     * Define a descrição do anexo.
     * 
     * @param description Descrição do anexo
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Retorna o UUID do animal ao qual o anexo pertence.
     * 
     * @return UUID do animal
     */
    public UUID getAnimalUuid() { return animalUuid; }
    
    /**
     * Define o UUID do animal ao qual o anexo pertence.
     * 
     * @param animalUuid UUID do animal
     */
    public void setAnimalUuid(UUID animalUuid) { this.animalUuid = animalUuid; }

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
}
