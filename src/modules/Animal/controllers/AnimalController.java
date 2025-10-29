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

    private static final String SELECT_SQL = "SELECT * FROM public.animals";

    private static final String UPDATE_SQL = """
        UPDATE public.animals
        SET name = ?, sex = ?, species = ?, breed = ?, size = ?, color = ?, birthdate = ?, microchip = ?, rga = ?, castrated = ?, fiv = ?, felv = ?, status = ?, notes = ?, updated_at = now()
        WHERE uuid = ?
        """;

    private static final String DELETE_SQL = "DELETE FROM public.animals WHERE uuid = ?";

    public static void addAnimal(Connection conn, Animal a) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, a.getName());
            ps.setString(2, a.getSex());
            ps.setString(3, a.getSpecies());
            ps.setString(4, a.getBreed());
            ps.setString(5, a.getSize());
            ps.setString(6, a.getColor());
            if (a.getBirthdate() != null) {
                ps.setDate(7, Date.valueOf(a.getBirthdate()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setString(8, a.getMicrochip());
            ps.setString(9, a.getRga());
            ps.setBoolean(10, a.getCastrated());
            ps.setString(11, a.getFiv());
            ps.setString(12, a.getFelv());
            ps.setString(13, a.getStatus());
            ps.setString(14, a.getNotes());
            ps.executeUpdate();
        }
    }

    public static void updateAnimal(Connection conn, Animal a) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, a.getName());
            ps.setString(2, a.getSex());
            ps.setString(3, a.getSpecies());
            ps.setString(4, a.getBreed());
            ps.setString(5, a.getSize());
            ps.setString(6, a.getColor());
            if (a.getBirthdate() != null) {
                ps.setDate(7, Date.valueOf(a.getBirthdate()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setString(8, a.getMicrochip());
            ps.setString(9, a.getRga());
            ps.setBoolean(10, a.getCastrated());
            ps.setString(11, a.getFiv());
            ps.setString(12, a.getFelv());
            ps.setString(13, a.getStatus());
            ps.setString(14, a.getNotes());
            ps.setObject(15, a.getUuid());
            ps.executeUpdate();
        }
    }

    public static void deleteAnimal(Connection conn, UUID animalId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setObject(1, animalId);
            ps.executeUpdate();
        }
    }

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
                Timestamp cat = rs.getTimestamp("created_at");
                Timestamp uat = rs.getTimestamp("updated_at");

                Animal a = new Animal(
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
                        cat != null ? cat.toLocalDateTime() : LocalDateTime.now(),
                        uat != null ? uat.toLocalDateTime() : LocalDateTime.now()
                );
                animals.add(a);
            }
        }
        return animals;
    }
}
