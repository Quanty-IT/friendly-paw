package modules.MedicineBrand.controllers;

import modules.MedicineBrand.models.MedicineBrand;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MedicineBrandController {

    private final Connection conn;

    public MedicineBrandController(Connection conn) {
        this.conn = conn;
    }

    public void insert(String name) throws SQLException {
        String sql = "INSERT INTO public.medicine_brands (name, created_at, updated_at) VALUES (?, now(), now())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    public List<MedicineBrand> listAll() throws SQLException {
        List<MedicineBrand> brands = new ArrayList<>();
        String sql = "SELECT * FROM public.medicine_brands ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                brands.add(new MedicineBrand(
                        (UUID) rs.getObject("uuid"),
                        rs.getString("name"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ));
            }
        }
        return brands;
    }

    public MedicineBrand findByUuid(UUID uuid) throws SQLException {
        String sql = "SELECT uuid, name, created_at, updated_at FROM public.medicine_brands WHERE uuid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MedicineBrand(
                            (UUID) rs.getObject("uuid"),
                            rs.getString("name"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                }
            }
        }
        return null;
    }

    public void update(UUID uuid, String name) throws SQLException {
        String sql = "UPDATE public.medicine_brands SET name = ?, updated_at = now() WHERE uuid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setObject(2, uuid);
            ps.executeUpdate();
        }
    }

    public void delete(UUID uuid) throws SQLException {
        String sql = "DELETE FROM public.medicine_brands WHERE uuid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, uuid);
            ps.executeUpdate();
        }
    }
}