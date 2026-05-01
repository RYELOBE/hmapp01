package com.campus.marketplace.controller;

import com.campus.marketplace.service.AddressService;
import com.campus.marketplace.service.CurrentUserService;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@Validated
public class AddressController {

  private final AddressService addressService;
  private final CurrentUserService currentUserService;

  public AddressController(AddressService addressService, CurrentUserService currentUserService) {
    this.addressService = addressService;
    this.currentUserService = currentUserService;
  }

  @GetMapping
  public Map<String, Object> list() {
    Long userId = currentUserService.userId();
    var addresses = addressService.getAddressList(userId);
    return Map.of("code", 200, "data", addresses);
  }

  @GetMapping("/default")
  public Map<String, Object> getDefault() {
    Long userId = currentUserService.userId();
    return addressService.getDefaultAddress(userId);
  }

  @GetMapping("/{id}")
  public Map<String, Object> get(@PathVariable Long id) {
    Long userId = currentUserService.userId();
    return addressService.getAddress(userId, id);
  }

  @PostMapping
  public Map<String, Object> create(@RequestBody @Validated AddressRequest request) {
    Long userId = currentUserService.userId();
    return addressService.createAddress(userId, request.receiverName(), request.receiverPhone(),
        request.province(), request.city(), request.district(), request.detailAddress(),
        request.postalCode(), request.isDefault());
  }

  @PutMapping("/{id}")
  public Map<String, Object> update(@PathVariable Long id, @RequestBody @Validated AddressRequest request) {
    Long userId = currentUserService.userId();
    return addressService.updateAddress(userId, id, request.receiverName(), request.receiverPhone(),
        request.province(), request.city(), request.district(), request.detailAddress(),
        request.postalCode(), request.isDefault());
  }

  @DeleteMapping("/{id}")
  public Map<String, Object> delete(@PathVariable Long id) {
    Long userId = currentUserService.userId();
    return addressService.deleteAddress(userId, id);
  }

  @PutMapping("/{id}/default")
  public Map<String, Object> setDefault(@PathVariable Long id) {
    Long userId = currentUserService.userId();
    return addressService.setDefaultAddress(userId, id);
  }

  public record AddressRequest(
      @NotBlank(message = "收货人姓名不能为空") String receiverName,
      @NotBlank(message = "联系电话不能为空") String receiverPhone,
      @NotBlank(message = "省份不能为空") String province,
      @NotBlank(message = "城市不能为空") String city,
      String district,
      @NotBlank(message = "详细地址不能为空") String detailAddress,
      String postalCode,
      Boolean isDefault
  ) {}
}
