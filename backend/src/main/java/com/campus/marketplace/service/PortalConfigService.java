package com.campus.marketplace.service;

import com.campus.marketplace.repository.PortalConfigRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PortalConfigService {
  private final PortalConfigRepository portalConfigRepository;

  public PortalConfigService(PortalConfigRepository portalConfigRepository) {
    this.portalConfigRepository = portalConfigRepository;
  }

  public List<Map<String, Object>> getAllPortals() {
    return portalConfigRepository.findAll();
  }

  public Map<String, Object> getByPortalCode(String portalCode) {
    return portalConfigRepository.findByPortalCode(portalCode)
        .orElseThrow(() -> new IllegalArgumentException("门户 [" + portalCode + "] 不存在"));
  }

  public Map<String, Object> savePortal(String portalCode, String portalName,
      String templateType, String configJson, String updatedBy) {
    return portalConfigRepository.save(portalCode, portalName, templateType, configJson, updatedBy);
  }

  public void deletePortal(String portalCode) {
    boolean removed = portalConfigRepository.delete(portalCode);
    if (!removed) {
      throw new IllegalArgumentException("门户 [" + portalCode + "] 不存在");
    }
  }
}
