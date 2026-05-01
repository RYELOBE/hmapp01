package com.campus.marketplace.controller;

import com.campus.marketplace.service.FavoriteService;
import com.campus.marketplace.service.CurrentUserService;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@Validated
public class FavoriteController {

  private final FavoriteService favoriteService;
  private final CurrentUserService currentUserService;

  public FavoriteController(FavoriteService favoriteService, CurrentUserService currentUserService) {
    this.favoriteService = favoriteService;
    this.currentUserService = currentUserService;
  }

  @GetMapping
  public Map<String, Object> list() {
    Long userId = currentUserService.userId();
    return favoriteService.getFavoriteList(userId);
  }

  @GetMapping("/count")
  public Map<String, Object> count() {
    Long userId = currentUserService.userId();
    return favoriteService.getFavoriteCount(userId);
  }

  @GetMapping("/check/{itemId}")
  public Map<String, Object> check(@PathVariable Long itemId) {
    Long userId = currentUserService.userId();
    return favoriteService.checkFavorite(userId, itemId);
  }

  @PostMapping("/{itemId}")
  public Map<String, Object> add(@PathVariable Long itemId) {
    Long userId = currentUserService.userId();
    return favoriteService.addFavorite(userId, itemId);
  }

  @DeleteMapping("/{itemId}")
  public Map<String, Object> remove(@PathVariable Long itemId) {
    Long userId = currentUserService.userId();
    return favoriteService.removeFavorite(userId, itemId);
  }
}
