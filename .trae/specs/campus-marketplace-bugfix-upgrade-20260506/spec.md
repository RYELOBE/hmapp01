# 校园二手交易平台 - Bug修复与功能升级 - Product Requirement Document

## Overview
- **Summary**: 对现有校园二手交易平台进行全面Bug修复，并实现类似CommonEngine-web的动态资源配置与页面生成功能。目标是让整个项目可以正常演示，并为后续页面和业务逻辑升级打下基础。
- **Purpose**: 解决当前项目中前端和后端存在的问题，实现通过运营端资源配置动态生成页面的核心功能，提升平台的可配置性和扩展性。
- **Target Users**: 系统管理员、运营人员、开发者、最终用户（买家和卖家）

## Goals
- 修复所有前端和后端Bug，让项目可以正常运行和演示
- 确保MySQL数据库可以正常连接和使用
- 实现动态资源配置系统（类似CommonEngine-web）
- 实现通过运营端配置生成门户页面的功能
- 确保所有核心功能（登录、商品发布、审核、订单等）正常工作

## Non-Goals (Out of Scope)
- 不实现全新的业务功能，只在现有基础上完善
- 不进行大规模UI重构
- 不涉及支付系统集成
- 不部署到生产环境

## Background & Context
- 项目是一个Vue3+Java Spring Boot的全栈校园二手交易平台
- 使用Qiankun微前端架构，包含Shell主应用、Portal门户子应用、Ops运营子应用
- 原项目依赖MySQL数据库，当前无法启动后端服务
- 前端页面已启动，显示有网络错误（后端未启动）
- 目标是参考CommonEngine-web的设计思路，实现可配置的门户系统

## Functional Requirements
- **FR-1**: 修复所有后端编译和运行时Bug
- **FR-2**: 修复所有前端Vue组件和API调用Bug
- **FR-3**: 实现H2内存数据库支持及数据初始化
- **FR-4**: 完善资源菜单配置功能（后端+前端）
- **FR-5**: 实现角色资源权限管理
- **FR-6**: 实现门户设计器（类似CommonEngine-web）
- **FR-7**: 实现动态页面路由生成和加载
- **FR-8**: 完善登录和权限验证流程

## Non-Functional Requirements
- **NFR-1**: 前端页面加载时间 < 3秒
- **NFR-2**: API响应时间 < 500ms
- **NFR-3**: 代码符合现有项目的架构风格
- **NFR-4**: 支持在本地环境一键启动所有服务

## Constraints
- **Technical**: 必须使用现有技术栈（Vue3、Java Spring Boot、Qiankun）
- **Business**: 在现有代码基础上修改，不重写核心逻辑
- **Dependencies**: 保持对现有第三方库的依赖版本

## Assumptions
- 用户本地环境有Node.js 20+、JDK 17+、Maven 3.9+
- 只需要本地演示功能，不需要持久化数据
- 参考项目CommonEngine-web在用户本地可访问

## Acceptance Criteria

### AC-1: 项目可以正常启动运行
- **Given**: 本地开发环境已配置好
- **When**: 执行启动脚本
- **Then**: 前端所有应用和后端服务都能正常启动并访问
- **Verification**: `human-judgment`
- **Notes**: 通过浏览器访问检查

### AC-2: 登录功能正常
- **Given**: 系统已启动
- **When**: 使用测试账号登录
- **Then**: 可以成功登录并跳转到对应页面
- **Verification**: `human-judgment`

### AC-3: H2数据库正常工作
- **Given**: 后端已启动
- **When**: 访问系统功能
- **Then**: 数据可以正常读写
- **Verification**: `human-judgment`

### AC-4: 资源配置功能可用
- **Given**: 已登录运营账号
- **When**: 进入资源管理页面
- **Then**: 可以配置菜单、角色、权限
- **Verification**: `human-judgment`

### AC-5: 门户设计器功能可用
- **Given**: 已登录运营账号
- **When**: 进入门户设计页面
- **Then**: 可以配置门户页面布局和内容
- **Verification**: `human-judgment`

### AC-6: 核心业务功能正常
- **Given**: 用户已登录
- **When**: 使用商品发布、购买、审核等功能
- **Then**: 所有流程正常工作
- **Verification**: `human-judgment`

## Open Questions
- [ ] CommonEngine-web项目在本地的具体路径需要确认
- [ ] 是否需要保留MySQL支持，还是完全改用H2
- [ ] 门户设计器需要实现到什么程度的功能

