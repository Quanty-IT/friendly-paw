package modules.User.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private final UUID uuid;
    private final String name;
    private final String email;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * Construtor da classe User.
     * 
     * @param uuid UUID único do usuário
     * @param name Nome do usuário
     * @param email Email do usuário
     * @param createdAt Data de criação do registro
     * @param updatedAt Data da última atualização do registro
     */
    public User(UUID uuid, String name, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.uuid = uuid;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Retorna o UUID do usuário.
     * 
     * @return UUID do usuário
     */
    public UUID getUuid() { return uuid; }

    /**
     * Retorna o nome do usuário.
     * 
     * @return Nome do usuário
     */
    public String getName() { return name; }

    /**
     * Retorna o email do usuário.
     * 
     * @return Email do usuário
     */
    public String getEmail() { return email; }

    /**
     * Retorna a data de criação do registro.
     * 
     * @return Data de criação do registro
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    /**
     * Retorna a data da última atualização do registro.
     * 
     * @return Data da última atualização do registro
     */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
