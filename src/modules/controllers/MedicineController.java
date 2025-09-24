package modules.controllers;

import modules.models.Medicine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineController {

    private final Connection conn;

    public MedicineController(Connection conn) {
        this.conn = conn;
    }

    public void insert(String name, Integer brandId, Integer quantity, String description, Boolean isActive) throws SQLException {
        String sql = "INSERT INTO public.medicines (name, brand_id, quantity, description, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, now(), now())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setObject(2, brandId);
            ps.setObject(3, quantity);
            ps.setString(4, description);
            ps.setBoolean(5, isActive);
            ps.executeUpdate();
        }
    }

    public List<Medicine> listAll() throws SQLException {
        List<Medicine> medicines = new ArrayList<>();
        String sql = """
                SELECT m.id, m.name, m.brand_id, mb.name as brand_name, m.quantity, 
                       m.description, m.is_active, m.created_at, m.updated_at
                FROM public.medicines m
                LEFT JOIN public.medicine_brands mb ON m.brand_id = mb.id
                ORDER BY m.created_at DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                medicines.add(new Medicine(rs.getInt("id"), rs.getString("name"), (Integer) rs.getObject("brand_id"), rs.getString("brand_name"), (Integer) rs.getObject("quantity"), rs.getString("description"), rs.getBoolean("is_active"), rs.getTimestamp("created_at").toLocalDateTime(), rs.getTimestamp("updated_at").toLocalDateTime()));
            }
        }
        return medicines;
    }

    public Medicine findById(int id) throws SQLException {
        String sql = """
                SELECT m.id, m.name, m.brand_id, mb.name as brand_name, m.quantity, 
                       m.description, m.is_active, m.created_at, m.updated_at
                FROM public.medicines m
                LEFT JOIN public.medicine_brands mb ON m.brand_id = mb.id
                WHERE m.id = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Medicine(rs.getInt("id"), rs.getString("name"), (Integer) rs.getObject("brand_id"), rs.getString("brand_name"), (Integer) rs.getObject("quantity"), rs.getString("description"), rs.getBoolean("is_active"), rs.getTimestamp("created_at").toLocalDateTime(), rs.getTimestamp("updated_at").toLocalDateTime());
                }
            }
        }
        return null;
    }

    public void update(int id, String name, Integer brandId, Integer quantity, String description, Boolean isActive) throws SQLException {
        String sql = "UPDATE public.medicines SET name = ?, brand_id = ?, quantity = ?, description = ?, is_active = ?, updated_at = now() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setObject(2, brandId);
            ps.setObject(3, quantity);
            ps.setString(4, description);
            ps.setBoolean(5, isActive);
            ps.setInt(6, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM public.medicines WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}