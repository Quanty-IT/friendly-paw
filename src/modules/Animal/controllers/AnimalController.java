package modules.Animal.controllers;

import modules.Animal.models.Animal;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnimalController {

    private static final String INSERT_SQL = """
        INSERT INTO public.animals
        (name, sex, species, breed, size, color, birthdate, microchip, rga, castrated, fiv, felv, status, notes, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now())
        """;

    private static final String SELECT_SQL = """
        SELECT *
        FROM public.animals
        ORDER BY name ASC
    """;

    private static final String UPDATE_SQL = """
        UPDATE public.animals
        SET name = ?, sex = ?, species = ?, breed = ?, size = ?, color = ?, birthdate = ?, microchip = ?, rga = ?, castrated = ?, fiv = ?, felv = ?, status = ?, notes = ?, updated_at = now()
        WHERE uuid = ?
        """;

    private static final String DELETE_SQL = "DELETE FROM public.animals WHERE uuid = ?";

    /**
     * Adiciona um novo animal ao banco de dados.
     * 
     * @param conn Conexão com o banco de dados
     * @param animal Objeto Animal com os dados do animal a ser cadastrado
     * @throws SQLException Se ocorrer erro na operação do banco de dados
     */
    public static void addAnimal(Connection conn, Animal animal) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, animal.getName());
            ps.setString(2, animal.getSex());
            ps.setString(3, animal.getSpecies());
            ps.setString(4, animal.getBreed());
            ps.setString(5, animal.getSize());
            ps.setString(6, animal.getColor());
            if (animal.getBirthdate() != null) {
                ps.setDate(7, Date.valueOf(animal.getBirthdate()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setString(8, animal.getMicrochip());
            ps.setString(9, animal.getRga());
            ps.setBoolean(10, animal.getCastrated());
            ps.setString(11, animal.getFiv());
            ps.setString(12, animal.getFelv());
            ps.setString(13, animal.getStatus());
            ps.setString(14, animal.getNotes());
            ps.executeUpdate();
        }
    }

    /**
     * Atualiza os dados de um animal existente no banco de dados.
     * 
     * @param conn Conexão com o banco de dados
     * @param animal Objeto Animal com os dados atualizados (deve conter o UUID do animal)
     * @throws SQLException Se ocorrer erro na operação do banco de dados
     */
    public static void updateAnimal(Connection conn, Animal animal) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, animal.getName());
            ps.setString(2, animal.getSex());
            ps.setString(3, animal.getSpecies());
            ps.setString(4, animal.getBreed());
            ps.setString(5, animal.getSize());
            ps.setString(6, animal.getColor());
            if (animal.getBirthdate() != null) {
                ps.setDate(7, Date.valueOf(animal.getBirthdate()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setString(8, animal.getMicrochip());
            ps.setString(9, animal.getRga());
            ps.setBoolean(10, animal.getCastrated());
            ps.setString(11, animal.getFiv());
            ps.setString(12, animal.getFelv());
            ps.setString(13, animal.getStatus());
            ps.setString(14, animal.getNotes());
            ps.setObject(15, animal.getUuid());
            ps.executeUpdate();
        }
    }

    /**
     * Remove um animal do banco de dados.
     * 
     * @param conn Conexão com o banco de dados
     * @param animalId UUID do animal a ser removido
     * @throws SQLException Se ocorrer erro na operação do banco de dados
     */
    public static void deleteAnimal(Connection conn, UUID animalId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setObject(1, animalId);
            ps.executeUpdate();
        }
    }

    /**
     * Retorna uma lista com todos os animais cadastrados no banco de dados.
     * 
     * @param conn Conexão com o banco de dados
     * @return Lista de objetos Animal contendo todos os animais cadastrados
     * @throws SQLException Se ocorrer erro na operação do banco de dados
     */
    public static List<Animal> getAllAnimals(Connection conn) throws SQLException {
        List<Animal> animals = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(SELECT_SQL)) {
            while (rs.next()) {
                UUID uuid = (UUID) rs.getObject("uuid");
                String name = rs.getString("name");
                String sex = rs.getString("sex");
                String species = rs.getString("species");
                String breed = rs.getString("breed");
                String size = rs.getString("size");
                String color = rs.getString("color");
                Date birthdate = rs.getDate("birthdate");
                String microchip = rs.getString("microchip");
                String rga = rs.getString("rga");
                boolean castrated = rs.getBoolean("castrated");
                String fiv = rs.getString("fiv");
                String felv = rs.getString("felv");
                String status = rs.getString("status");
                String notes = rs.getString("notes");
                Timestamp createdAt = rs.getTimestamp("created_at");
                Timestamp updatedAt = rs.getTimestamp("updated_at");

                Animal animal = new Animal(
                        uuid,
                        name,
                        sex,
                        species,
                        breed,
                        size,
                        color,
                        birthdate != null ? birthdate.toLocalDate() : null,
                        microchip,
                        rga,
                        castrated,
                        fiv,
                        felv,
                        status,
                        notes,
                        createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now(),
                        updatedAt != null ? updatedAt.toLocalDateTime() : LocalDateTime.now()
                );
                animals.add(animal);
            }
        }
        return animals;
    }
}
