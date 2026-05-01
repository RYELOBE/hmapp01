package com.campus.marketplace.service;

import com.campus.marketplace.repository.AddressRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

  private final AddressRepository addressRepository;

  public AddressService(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  public Map<String, Object> createAddress(Long userId, String receiverName, String receiverPhone,
      String province, String city, String district, String detailAddress,
      String postalCode, Boolean isDefault) {
    if (receiverName == null || receiverName.isBlank()) {
      throw new IllegalArgumentException("收货人姓名不能为空");
    }
    if (receiverPhone == null || receiverPhone.isBlank()) {
      throw new IllegalArgumentException("联系电话不能为空");
    }
    if (province == null || province.isBlank()) {
      throw new IllegalArgumentException("省份不能为空");
    }
    if (city == null || city.isBlank()) {
      throw new IllegalArgumentException("城市不能为空");
    }
    if (detailAddress == null || detailAddress.isBlank()) {
      throw new IllegalArgumentException("详细地址不能为空");
    }

    int addressCount = addressRepository.countByUserId(userId);
    boolean setAsDefault = isDefault != null && isDefault;
    if (setAsDefault) {
      addressRepository.setDefault(0L, userId);
    } else if (addressCount == 0) {
      setAsDefault = true;
    }

    Map<String, Object> address = addressRepository.create(
        userId, receiverName, receiverPhone, province, city, district, detailAddress,
        postalCode, setAsDefault);

    return Map.of("code", 200, "data", address);
  }

  public List<Map<String, Object>> getAddressList(Long userId) {
    List<Map<String, Object>> addresses = addressRepository.findByUserId(userId);
    return addresses;
  }

  public Map<String, Object> getAddress(Long userId, Long addressId) {
    var addressOpt = addressRepository.findById(addressId);
    if (addressOpt.isEmpty()) {
      throw new IllegalArgumentException("地址不存在");
    }
    Map<String, Object> address = addressOpt.get();
    if (!userId.equals(address.get("userId"))) {
      throw new IllegalArgumentException("无权访问此地址");
    }
    return Map.of("code", 200, "data", address);
  }

  public Map<String, Object> updateAddress(Long userId, Long addressId, String receiverName,
      String receiverPhone, String province, String city, String district,
      String detailAddress, String postalCode, Boolean isDefault) {
    var addressOpt = addressRepository.findById(addressId);
    if (addressOpt.isEmpty()) {
      throw new IllegalArgumentException("地址不存在");
    }
    Map<String, Object> existing = addressOpt.get();
    if (!userId.equals(existing.get("userId"))) {
      throw new IllegalArgumentException("无权修改此地址");
    }

    if (receiverName == null || receiverName.isBlank()) {
      throw new IllegalArgumentException("收货人姓名不能为空");
    }
    if (receiverPhone == null || receiverPhone.isBlank()) {
      throw new IllegalArgumentException("联系电话不能为空");
    }
    if (province == null || province.isBlank()) {
      throw new IllegalArgumentException("省份不能为空");
    }
    if (city == null || city.isBlank()) {
      throw new IllegalArgumentException("城市不能为空");
    }
    if (detailAddress == null || detailAddress.isBlank()) {
      throw new IllegalArgumentException("详细地址不能为空");
    }

    if (isDefault != null && isDefault) {
      addressRepository.setDefault(addressId, userId);
    }

    addressRepository.update(addressId, userId, receiverName, receiverPhone,
        province, city, district, detailAddress, postalCode, isDefault != null && isDefault);

    var updated = addressRepository.findById(addressId);
    return Map.of("code", 200, "data", updated.orElseThrow());
  }

  public Map<String, Object> deleteAddress(Long userId, Long addressId) {
    var addressOpt = addressRepository.findById(addressId);
    if (addressOpt.isEmpty()) {
      throw new IllegalArgumentException("地址不存在");
    }
    Map<String, Object> address = addressOpt.get();
    if (!userId.equals(address.get("userId"))) {
      throw new IllegalArgumentException("无权删除此地址");
    }

    addressRepository.delete(addressId, userId);
    return Map.of("code", 200, "message", "删除成功");
  }

  public Map<String, Object> setDefaultAddress(Long userId, Long addressId) {
    var addressOpt = addressRepository.findById(addressId);
    if (addressOpt.isEmpty()) {
      throw new IllegalArgumentException("地址不存在");
    }
    Map<String, Object> address = addressOpt.get();
    if (!userId.equals(address.get("userId"))) {
      throw new IllegalArgumentException("无权操作此地址");
    }

    addressRepository.setDefault(addressId, userId);
    return Map.of("code", 200, "message", "设置默认地址成功");
  }

  public Map<String, Object> getDefaultAddress(Long userId) {
    var addressOpt = addressRepository.findDefaultByUserId(userId);
    if (addressOpt.isEmpty()) {
      var addresses = addressRepository.findByUserId(userId);
      if (addresses.isEmpty()) {
        return Map.of("code", 404, "message", "暂无收货地址");
      }
      return Map.of("code", 200, "data", addresses.get(0));
    }
    return Map.of("code", 200, "data", addressOpt.get());
  }
}
