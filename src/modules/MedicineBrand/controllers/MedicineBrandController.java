package modules.MedicineBrand.controllers;

import modules.MedicineBrand.models.MedicineBrand;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ));
            }
        }
        return brands;
    }

    public MedicineBrand findById(int id) throws SQLException {
        String sql = "SELECT id, name, created_at, updated_at FROM public.medicine_brands WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MedicineBrand(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                }
            }
        }
        return null;
    }

    public void update(int id, String name) throws SQLException {
        String sql = "UPDATE public.medicine_brands SET name = ?, updated_at = now() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM public.medicine_brands WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}