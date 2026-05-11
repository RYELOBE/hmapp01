package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;

@Repository
public class CircleCommentRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("postId", rs.getLong("post_id"));
    row.put("parentId", rs.getObject("parent_id") != null ? rs.getLong("parent_id") : null);
    row.put("userId", rs.getLong("user_id"));
    row.put("userName", rs.getString("user_name"));
    row.put("replyToName", rs.getString("reply_to_user_name"));
    row.put("content", rs.getString("content"));
    row.put("status", rs.getString("status"));
    row.put("likeCount", rs.getInt("like_count"));
    row.put("createTime", rs.getTimestamp("create_time") != null ? rs.getTimestamp("create_time").toString() : null);
    return row;
  };

  public CircleCommentRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> save(Long postId, Long userId, String userName, String content) {
    return save(postId, null, null, userId, userName, content);
  }

  public Map<String, Object> save(Long postId, Long parentId, String replyToName, Long userId, String userName, String content) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO circle_comment (post_id, parent_id, reply_to_user_name, user_id, user_name, content) VALUES (?, ?, ?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, postId);
      if (parentId != null) {
        ps.setLong(2, parentId);
      } else {
        ps.setNull(2, java.sql.Types.BIGINT);
      }
      ps.setString(3, replyToName);
      ps.setLong(4, userId);
      ps.setString(5, userName);
      ps.setString(6, content);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    return findById(id);
  }

  public Map<String, Object> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query("SELECT * FROM circle_comment WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? null : results.get(0);
  }

  public List<Map<String, Object>> findByPostIdOrderByCreateTimeAsc(Long postId, int pageNo, int pageSize) {
    String sql = "SELECT * FROM circle_comment WHERE post_id = ? ORDER BY create_time ASC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    return jdbc.query(sql, ROW_MAPPER, postId, pageSize, offset);
  }

  public int countByPostId(Long postId) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM circle_comment WHERE post_id = ?", Integer.class, postId);
    return count != null ? count : 0;
  }

  public void deleteByPostIdAndUserId(Long postId, Long userId) {
    jdbc.update("DELETE FROM circle_comment WHERE post_id = ? AND user_id = ?", postId, userId);
  }

  /**
   * 查询所有评论列表（按时间倒序）
   */
  public List<Map<String, Object>> findAllOrderByTime(int pageNo, int pageSize) {
    String sql = "SELECT * FROM circle_comment ORDER BY create_time DESC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    return jdbc.query(sql, ROW_MAPPER, pageSize, offset);
  }

  /**
   * 统计评论总数
   */
  public int countAll() {
    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM circle_comment", Integer.class);
    return count != null ? count : 0;
  }

  /**
   * 删除评论
   */
  public void deleteById(Long id) {
    jdbc.update("DELETE FROM circle_comment WHERE id = ?", id);
  }

  public List<Map<String, Object>> findByStatusPaged(String status, int pageNo, int pageSize) {
    String sql = "SELECT * FROM circle_comment WHERE status = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    return jdbc.query(sql, ROW_MAPPER, status, pageSize, offset);
  }

  public List<Map<String, Object>> findByStatusNotPaged(String excludedStatus, int pageNo, int pageSize) {
    String sql = "SELECT * FROM circle_comment WHERE status != ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    return jdbc.query(sql, ROW_MAPPER, excludedStatus, pageSize, offset);
  }

  public int countByStatus(String status) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM circle_comment WHERE status = ?", Integer.class, status);
    return count != null ? count : 0;
  }

  public int countByStatusNot(String excludedStatus) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM circle_comment WHERE status != ?", Integer.class, excludedStatus);
    return count != null ? count : 0;
  }

  public void updateStatus(Long id, String status) {
    jdbc.update("UPDATE circle_comment SET status = ? WHERE id = ?", status, id);
  }

  /**
   * 查询父评论（parentId IS NULL）及其子评论
   */
  public List<Map<String, Object>> findParentCommentsWithReplies(Long postId, String status, int pageNo, int pageSize) {
    String sql = "SELECT * FROM circle_comment WHERE post_id = ? AND parent_id IS NULL AND status = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    List<Map<String, Object>> parents = jdbc.query(sql, ROW_MAPPER, postId, status, pageSize, offset);

    for (Map<String, Object> parent : parents) {
      Long parentId = ((Number) parent.get("id")).longValue();
      List<Map<String, Object>> replies = findByParentId(parentId, status);
      parent.put("replies", replies);
    }

    return parents;
  }

  /**
   * 查询某条评论的所有子评论
   */
  public List<Map<String, Object>> findByParentId(Long parentId, String status) {
    String sql = "SELECT * FROM circle_comment WHERE parent_id = ? AND status = ? ORDER BY create_time ASC";
    return jdbc.query(sql, ROW_MAPPER, parentId, status);
  }

  /**
   * 统计指定状态的评论总数（包括父评论和子评论）
   */
  public int countByPostIdAndStatus(Long postId, String status) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM circle_comment WHERE post_id = ? AND status = ?", Integer.class, postId, status);
    return count != null ? count : 0;
  }

  public void incrementLikeCount(Long commentId) {
    jdbc.update("UPDATE circle_comment SET like_count = like_count + 1 WHERE id = ?", commentId);
  }

  public void decrementLikeCount(Long commentId) {
    jdbc.update("UPDATE circle_comment SET like_count = GREATEST(like_count - 1, 0) WHERE id = ?", commentId);
  }

  /**
   * 查询某帖子的所有评论（用于构建树形结构）
   * 一次性获取所有已审核评论，避免 N+1 查询问题
   */
  public List<Map<String, Object>> findAllByPostIdAndStatus(Long postId, String status) {
    String sql = "SELECT * FROM circle_comment WHERE post_id = ? AND status = ? ORDER BY create_time ASC";
    return jdbc.query(sql, ROW_MAPPER, postId, status);
  }

  /**
   * 查询顶级评论（parent_id IS NULL）的分页列表
   */
  public List<Map<String, Object>> findTopLevelCommentsPaged(Long postId, String status, int pageNo, int pageSize) {
    String sql = "SELECT * FROM circle_comment WHERE post_id = ? AND parent_id IS NULL AND status = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    return jdbc.query(sql, ROW_MAPPER, postId, status, pageSize, offset);
  }

  /**
   * 统计顶级评论数量（用于分页）
   */
  public int countTopLevelComments(Long postId, String status) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM circle_comment WHERE post_id = ? AND parent_id IS NULL AND status = ?",
        Integer.class, postId, status);
    return count != null ? count : 0;
  }
}
