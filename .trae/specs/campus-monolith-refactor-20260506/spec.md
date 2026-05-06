# 校园交易平台 - 微前端转单体架构重构 PRD

## Overview
- **Summary**: 将当前基于 qiankun + Module Federation 的微前端架构重构为单体 Vue 应用，简化架构复杂度，提升开发和维护效率。
- **Purpose**: 移除不必要的微前端层，解决微前端带来的复杂性、性能开销和调试困难问题。同时排查并修复项目中的所有 bug。
- **Target Users**: 开发团队、运维团队、最终用户

## Goals
- 将 shell、portal、ops 三个应用合并为单一单体应用
- 移除 qiankun 和 vite-plugin-federation 依赖
- 整合路由、状态管理和组件
- 排查并修复所有已知 bug
- 保持原有功能完整性

## Non-Goals (Out of Scope)
- 不改变业务功能逻辑
- 不修改后端 API 接口
- 不引入新的技术栈

## Background & Context
当前项目采用了双层微前端架构：
1. **Qiankun**: 用于应用级别的微前端集成（shell 作为主应用，portal/ops 作为子应用）
2. **Module Federation**: 用于组件和工具函数的共享（mf-shared）

这种架构带来了以下问题：
- 开发复杂度高，需要启动多个服务
- 构建和部署流程复杂
- 运行时性能开销大
- 调试困难
- 代码重复（如登录逻辑、权限验证等）

## Functional Requirements
- **FR-1**: 合并 shell、portal、ops 的路由到单一路由系统
- **FR-2**: 整合状态管理（authStore、framePinia）
- **FR-3**: 移除所有微前端相关依赖（qiankun、vite-plugin-federation）
- **FR-4**: 保留所有原有业务功能
- **FR-5**: 统一登录和权限验证逻辑

## Non-Functional Requirements
- **NFR-1**: 构建时间减少 50%
- **NFR-2**: 首屏加载时间减少 30%
- **NFR-3**: 代码重复率降低 40%
- **NFR-4**: 保持代码可维护性和可扩展性

## Constraints
- **Technical**: Vue 3 + Vite + Pinia + Vue Router
- **Business**: 不影响现有业务功能
- **Dependencies**: 保留核心依赖（Vue、Pinia、Vue Router、Arco Design）

## Assumptions
- 所有子应用的代码可以无缝合并
- 路由路径可以保持不变（/portal/*, /ops/*）
- 组件可以共享使用

## Acceptance Criteria

### AC-1: 架构合并完成
- **Given**: 当前微前端架构存在
- **When**: 执行重构
- **Then**: 单体应用包含所有原有功能
- **Verification**: `programmatic` - 构建成功，所有路由可访问

### AC-2: 微前端依赖移除
- **Given**: package.json 包含 qiankun 和 vite-plugin-federation
- **When**: 执行重构
- **Then**: 这些依赖从 package.json 中移除
- **Verification**: `programmatic` - grep 检查无相关 import

### AC-3: 登录功能统一
- **Given**: 存在多个登录页面
- **When**: 执行重构
- **Then**: 只有一个统一的登录页面
- **Verification**: `human-judgment` - 检查代码结构

### AC-4: 权限验证统一
- **Given**: 存在重复的权限验证逻辑
- **When**: 执行重构
- **Then**: 权限验证集中在单一位置
- **Verification**: `human-judgment` - 检查路由守卫

### AC-5: Bug 修复完成
- **Given**: 存在已知和未知 bug
- **When**: 执行代码审查和测试
- **Then**: 所有发现的 bug 被修复
- **Verification**: `programmatic` - 测试用例通过

## Open Questions
- [ ] 如何处理 mf-shared 中的共享组件？
- [ ] 是否需要保留子应用独立运行能力？