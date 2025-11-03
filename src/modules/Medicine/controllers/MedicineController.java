package modules.Medicine.controllers;

import modules.Medicine.models.Medicine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MedicineController {

    private final Connection conn;

    /**
     * Construtor do MedicineController.
     * 
     * @param conn Conexão com o banco de dados
     */
    public MedicineController(Connection conn) {
        this.conn = conn;
    }

    /**
     * Insere um novo medicamento no banco de dados.
     * 
     * @param name Nome do medicamento
     * @param brandId UUID da marca do medicamento
     * @param quantity Quantidade disponível do medicamento
     * @param description Descrição opcional do medicamento
     * @param isActive Indica se o medicamento está ativo
     * @throws SQLException Se ocorrer erro na operação do banco de dados
     */
    public void insert(String name, UUID brandId, Integer quantity, String description, Boolean isActive) throws SQLException {
        String sql = "INSERT INTO public.medicines (name, brand_uuid, quantity, description, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, now(), now())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setObject(2, brandId);
            ps.setObject(3, quantity != null ? quantity : -1);
            ps.setString(4, description);
            ps.setBoolean(5, isActive);
            ps.executeUpdate();
        }
    }

    /**
     * Retorna uma lista com todos os medicamentos cadastrados no banco de dados.
     * Os medicamentos são ordenados por data de criação (mais recentes primeiro).
     * 
     * @return Lista de objetos Medicine contendo todos os medicamentos cadastrados
     * @throws SQLException Se ocorrer erro na operação do banco de dados
     */
    public List<Medicine> listAll() throws SQLException {
        List<Medicine> medicines = new ArrayList<>();
        String sql = """
                SELECT m.uuid, m.name, m.brand_uuid, mb.name as brand_name, m.quantity, 
                       m.description, m.is_active, m.created_at, m.updated_at
                FROM public.medicines m
                LEFT JOIN public.medicine_brands mb ON m.brand_uuid = mb.uuid
                ORDER BY m.created_at DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                medicines.add(new Medicine((UUID) rs.getObject("uuid"), rs.getString("name"), (UUID) rs.getObject("brand_uuid"), rs.getString("brand_name"), (Integer) rs.getObject("quantity"), rs.getString("description"), rs.getBoolean("is_active"), rs.getTimestamp("created_at").toLocalDateTime(), rs.getTimestamp("updated_at").toLocalDateTime()));
            }
        }
        return medicines;
    }

    /**
     * Busca um medicamento específico pelo seu UUID.
     * 
     * @param uuid UUID do medicamento a ser buscado
     * @return Objeto Medicine se encontrado, null caso contrário
     * @throws SQLException Se ocorrer erro na operação do banco de dados
     */
    public Medicine findByUuid(UUID uuid) throws SQLException {
        String sql = """
                SELECT m.uuid, m.name, m.brand_uuid, mb.name as brand_name, m.quantity, 
                       m.description, m.is_active, m.created_at, m.updated_at
                FROM public.medicines m
                LEFT JOIN public.medicine_brands mb ON m.brand_uuid = mb.uuid
                WHERE m.uuid = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Medicine((UUID) rs.getObject("uuid"), rs.getString("name"), (UUID) rs.getObject("brand_uuid"), rs.getString("brand_name"), (Integer) rs.getObject("quantity"), rs.getString("description"), rs.getBoolean("is_active"), rs.getTimestamp("created_at").toLocalDateTime(), rs.getTimestamp("updated_at").toLocalDateTime());
                }
            }
        }
        return null;
    }

    /**
     * Atualiza os dados de um medicamento existente no banco de dados.
     * 
     * @param uuid UUID do medicamento a ser atualizado
     * @param name Novo nome do medicamento
     * @param brandId Novo UUID da marca do medicamento
     * @param quantity Nova quantidade disponível do medicamento
     * @param description Nova descrição do medicamento
     * @param isActive Novo status de ativação do medicamento
     * @throws SQLException Se ocorrer erro na operação do banco de dados
     */
    public void update(UUID uuid, String name, UUID brandId, Integer quantity, String description, Boolean isActive) throws SQLException {
        String sql = "UPDATE public.medicines SET name = ?, brand_uuid = ?, quantity = ?, description = ?, is_active = ?, updated_at = now() WHERE uuid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setObject(2, brandId);
            ps.setObject(3, quantity != null ? quantity : -1);
            ps.setString(4, description);
            ps.setBoolean(5, isActive);
            ps.setObject(6, uuid);
            ps.executeUpdate();
        }
    }

    /**
     * Remove um medicamento do banco de dados.
     * 
     * @param uuid UUID do medicamento a ser removido
     * @throws SQLException Se ocorrer erro na operação do banco de dados
     */
    public void delete(UUID uuid) throws SQLException {
        String sql = "DELETE FROM public.medicines WHERE uuid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, uuid);
            ps.executeUpdate();
        }
    }
}