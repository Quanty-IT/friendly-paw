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

    /**
     * Construtor da classe Animal.
     * 
     * @param uuid UUID único do animal
     * @param name Nome do animal
     * @param sex Sexo do animal
     * @param species Espécie do animal
     * @param breed Raça do animal
     * @param size Porte do animal
     * @param color Cor do animal
     * @param birthdate Data de nascimento do animal
     * @param microchip Número do microchip do animal
     * @param rga Registro Geral Animal
     * @param castrated Indica se o animal é castrado
     * @param fiv Status do teste FIV
     * @param felv Status do teste FeLV
     * @param status Status atual do animal
     * @param notes Observações sobre o animal
     * @param createdAt Data de criação do registro
     * @param updatedAt Data da última atualização do registro
     */
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

    /**
     * Retorna o UUID do animal.
     * 
     * @return UUID do animal
     */
    public UUID getUuid() { return uuid; }
    
    /**
     * Define o UUID do animal.
     * 
     * @param uuid UUID do animal
     */
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    /**
     * Retorna o nome do animal.
     * 
     * @return Nome do animal
     */
    public String getName() { return name; }
    
    /**
     * Define o nome do animal.
     * 
     * @param name Nome do animal
     */
    public void setName(String name) { this.name = name; }

    /**
     * Retorna o sexo do animal.
     * 
     * @return Sexo do animal
     */
    public String getSex() { return sex; }
    
    /**
     * Define o sexo do animal.
     * 
     * @param sex Sexo do animal
     */
    public void setSex(String sex) { this.sex = sex; }

    /**
     * Retorna a espécie do animal.
     * 
     * @return Espécie do animal
     */
    public String getSpecies() { return species; }
    
    /**
     * Define a espécie do animal.
     * 
     * @param species Espécie do animal
     */
    public void setSpecies(String species) { this.species = species; }

    /**
     * Retorna a raça do animal.
     * 
     * @return Raça do animal
     */
    public String getBreed() { return breed; }
    
    /**
     * Define a raça do animal.
     * 
     * @param breed Raça do animal
     */
    public void setBreed(String breed) { this.breed = breed; }

    /**
     * Retorna o porte do animal.
     * 
     * @return Porte do animal
     */
    public String getSize() { return size; }
    
    /**
     * Define o porte do animal.
     * 
     * @param size Porte do animal
     */
    public void setSize(String size) { this.size = size; }

    /**
     * Retorna a cor do animal.
     * 
     * @return Cor do animal
     */
    public String getColor() { return color; }
    
    /**
     * Define a cor do animal.
     * 
     * @param color Cor do animal
     */
    public void setColor(String color) { this.color = color; }

    /**
     * Retorna a data de nascimento do animal.
     * 
     * @return Data de nascimento do animal
     */
    public LocalDate getBirthdate() { return birthdate; }
    
    /**
     * Define a data de nascimento do animal.
     * 
     * @param birthdate Data de nascimento do animal
     */
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }

    /**
     * Retorna o número do microchip do animal.
     * 
     * @return Número do microchip do animal
     */
    public String getMicrochip() { return microchip; }
    
    /**
     * Define o número do microchip do animal.
     * 
     * @param microchip Número do microchip do animal
     */
    public void setMicrochip(String microchip) { this.microchip = microchip; }

    /**
     * Retorna o RGA do animal.
     * 
     * @return RGA do animal
     */
    public String getRga() { return rga; }
    
    /**
     * Define o RGA do animal.
     * 
     * @param rga RGA do animal
     */
    public void setRga(String rga) { this.rga = rga; }

    /**
     * Retorna se o animal é castrado.
     * 
     * @return true se o animal é castrado, false caso contrário
     */
    public boolean getCastrated() { return castrated; }
    
    /**
     * Define se o animal é castrado.
     * 
     * @param castrated true se o animal é castrado, false caso contrário
     */
    public void setCastrated(boolean castrated) { this.castrated = castrated; }

    /**
     * Retorna o status do teste FIV.
     * 
     * @return Status do teste FIV
     */
    public String getFiv() { return fiv; }
    
    /**
     * Define o status do teste FIV.
     * 
     * @param fiv Status do teste FIV
     */
    public void setFiv(String fiv) { this.fiv = fiv; }

    /**
     * Retorna o status do teste FeLV.
     * 
     * @return Status do teste FeLV
     */
    public String getFelv() { return felv; }
    
    /**
     * Define o status do teste FeLV.
     * 
     * @param felv Status do teste FeLV
     */
    public void setFelv(String felv) { this.felv = felv; }

    /**
     * Retorna o status atual do animal.
     * 
     * @return Status atual do animal
     */
    public String getStatus() { return status; }
    
    /**
     * Define o status atual do animal.
     * 
     * @param status Status atual do animal
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Retorna as observações sobre o animal.
     * 
     * @return Observações sobre o animal
     */
    public String getNotes() { return notes; }
    
    /**
     * Define as observações sobre o animal.
     * 
     * @param notes Observações sobre o animal
     */
    public void setNotes(String notes) { this.notes = notes; }

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
