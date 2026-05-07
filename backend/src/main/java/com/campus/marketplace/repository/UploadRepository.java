package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;

@Repository
public class UploadRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
        Map<String, Object> row = new HashMap<>();
        row.put("id", rs.getLong("id"));
        row.put("originalName", rs.getString("original_name"));
        row.put("storedName", rs.getString("stored_name"));
        row.put("filePath", rs.getString("file_path"));
        row.put("fileSize", rs.getLong("file_size"));
        row.put("contentType", rs.getString("content_type"));
        row.put("url", rs.getString("url"));
        row.put("base64Data", rs.getString("base64_data"));
        row.put("uploaderId", rs.getObject("uploader_id") != null ? rs.getLong("uploader_id") : null);
        row.put("createdAt", rs.getTimestamp("created_at"));
        return row;
    };

    public UploadRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Object> save(String originalName, String storedName,
                                    String filePath, long fileSize,
                                    String contentType, String url,
                                    String base64Data, Long uploaderId) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO uploaded_file (original_name, stored_name, file_path, " +
                    "file_size, content_type, url, base64_data, uploader_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, originalName);
            ps.setString(2, storedName);
            ps.setString(3, filePath);
            ps.setLong(4, fileSize);
            ps.setString(5, contentType);
            ps.setString(6, url);
            ps.setString(7, base64Data);
            if (uploaderId != null) {
                ps.setLong(8, uploaderId);
            } else {
                ps.setNull(8, java.sql.Types.BIGINT);
            }
            return ps;
        }, kh);
        Long id = kh.getKey().longValue();
        return findById(id);
    }

    public Map<String, Object> findById(Long id) {
        List<Map<String, Object>> results = jdbc.query(
                "SELECT * FROM uploaded_file WHERE id = ?", ROW_MAPPER, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM uploaded_file WHERE id = ?", id);
    }
}
