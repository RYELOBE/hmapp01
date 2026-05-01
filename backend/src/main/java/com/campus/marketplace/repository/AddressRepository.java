package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;

@Repository
public class AddressRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("userId", rs.getLong("user_id"));
    row.put("receiverName", rs.getString("receiver_name"));
    row.put("receiverPhone", rs.getString("receiver_phone"));
    row.put("province", rs.getString("province"));
    row.put("city", rs.getString("city"));
    row.put("district", rs.getString("district"));
    row.put("detailAddress", rs.getString("detail_address"));
    row.put("postalCode", rs.getString("postal_code"));
    row.put("isDefault", rs.getBoolean("is_default"));
    row.put("createTime", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : null);
    row.put("updateTime", rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toString() : null);
    return row;
  };

  public AddressRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> create(Long userId, String receiverName, String receiverPhone,
      String province, String city, String district, String detailAddress,
      String postalCode, boolean isDefault) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO address (user_id, receiver_name, receiver_phone, province, city, district, detail_address, postal_code, is_default) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, userId);
      ps.setString(2, receiverName);
      ps.setString(3, receiverPhone);
      ps.setString(4, province);
      ps.setString(5, city);
      ps.setString(6, district);
      ps.setString(7, detailAddress);
      ps.setString(8, postalCode);
      ps.setBoolean(9, isDefault);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    return findById(id).orElseThrow();
  }

  public Optional<Map<String, Object>> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM address WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public List<Map<String, Object>> findByUserId(Long userId) {
    return jdbc.query(
        "SELECT * FROM address WHERE user_id = ? ORDER BY is_default DESC, created_at DESC",
        ROW_MAPPER, userId);
  }

  public Optional<Map<String, Object>> findDefaultByUserId(Long userId) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM address WHERE user_id = ? AND is_default = true",
        ROW_MAPPER, userId);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public void update(Long id, Long userId, String receiverName, String receiverPhone,
      String province, String city, String district, String detailAddress,
      String postalCode, boolean isDefault) {
    jdbc.update(
        "UPDATE address SET receiver_name = ?, receiver_phone = ?, province = ?, city = ?, district = ?, detail_address = ?, postal_code = ?, is_default = ? WHERE id = ? AND user_id = ?",
        receiverName, receiverPhone, province, city, district, detailAddress, postalCode, isDefault, id, userId);
  }

  public void delete(Long id, Long userId) {
    jdbc.update("DELETE FROM address WHERE id = ? AND user_id = ?", id, userId);
  }

  public void setDefault(Long id, Long userId) {
    jdbc.update("UPDATE address SET is_default = false WHERE user_id = ?", userId);
    jdbc.update("UPDATE address SET is_default = true WHERE id = ? AND user_id = ?", id, userId);
  }

  public int countByUserId(Long userId) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM address WHERE user_id = ?", Integer.class, userId);
    return count != null ? count : 0;
  }
}
