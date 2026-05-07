# 批量修复 Sa-Token → Spring Security 迁移指南

## 需要手动替换的规则

### 1. Import 替换
```java
// ❌ 删除这些 import
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;

// ✅ 添加这个 import
import org.springframework.security.access.prepost.PreAuthorize;
```

### 2. 注解替换规则

| Sa-Token 注解 | Spring Security 替换 |
|--------------|---------------------|
| `@SaCheckLogin` | `@PreAuthorize("isAuthenticated()")` |
| `@SaCheckRole("ROLE")` | `@PreAuthorize("hasRole('ROLE')")` |
| `@SaCheckRole(value = {"A", "B"}, mode = SaMode.OR)` | `@PreAuthorize("hasAnyRole('A', 'B')")` |

### 3. StpUtil 替换

```java
// ❌ 删除
StpUtil.isLogin()
StpUtil.getLoginId()
StpUtil.getTokenValue()

// ✅ 使用 CurrentUserService (已注入)
currentUserService.userId()
```

## 已完成的文件（✅）

- [x] AIController.java
- [x] UploadController.java
- [x] GlobalExceptionHandler.java
- [x] AuthService.java
- [x] OpsAuthService.java
- [x] CurrentUserService.java
- [x] SaTokenRoleProvider.java

## 待修复的文件（⏳ 请手动或使用 IDE 批量替换）

以下文件需要将上述规则应用到每个文件：

1. **OpsController.java**
   - 第3-4行: 删除 SaToken imports，添加 PreAuthorize
   - 第20行: `@SaCheckLogin` → `@PreAuthorize("isAuthenticated()")`
   - 第21行: `@SaCheckRole("OPS")` → `@PreAuthorize("hasRole('OPS')")`

2. **ItemController.java**
   - 第3-4行: 删除 SaToken imports，添加 PreAuthorize
   - 第23行: `@SaCheckLogin` → `@PreAuthorize("isAuthenticated()")`
   - 第34/84/91/100行: `@SaCheckRole("SELLER")` → `@PreAuthorize("hasRole('SELLER')")`

3. **ResourceMenuController.java** (最多！)
   - 所有 `@SaCheckLogin` → `@PreAuthorize("isAuthenticated()")`
   - 所有 `@SaCheckRole("OPS")` → `@PreAuthorize("hasRole('OPS')")`

4. **OrderController.java**
   - 第3-5行: 删除 SaToken imports，添加 PreAuthorize
   - 第20行: `@SaCheckLogin` → `@PreAuthorize("isAuthenticated()")`
   - 第32行: `@SaCheckRole(value = {"BUYER", "OPS"}, mode = SaMode.OR)` → `@PreAuthorize("hasAnyRole('BUYER', 'OPS')")`

5. **OpsReviewController.java**
   - 第18-19行: 替换为 Spring Security 注解

6. **PortalConfigController.java**
   - 第42-43, 53-54行: 替换为 Spring Security 注解

7. **FrameController.java**
   - 第39, 46-47, 57-58行: 替换为 Spring Security 注解

## IDE 批量替换方法（推荐）

### IntelliJ IDEA:
1. Edit → Find → Replace in Path (Ctrl+Shift+R)
2. Search: `@SaCheckLogin\n`
3. Replace: `@PreAuthorize("isAuthenticated()")\n`
4. 勾选 "Regex"
5. Scope: File mask `*.java`, Directory: `backend/src`

### VS Code:
1. Ctrl+H (打开搜索替换)
2. 点击 ".*" 按钮（启用正则）
3. 搜索并替换上述模式
