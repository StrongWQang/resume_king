# Resume King - 简历王！智能简历制作工具

一个基于 Vue 3 + TypeScript 的在线简历制作工具，提供所见即所得的简历编辑体验。

## 功能特性

- 🎨 多种精美简历模板
  - 现代简约风格
  - 专业商务风格
  - 创意设计风格
- ✏️ 所见即所得的编辑体验
  - 拖拽式组件布局
  - 实时预览
  - 组件大小调整
  - 文本样式自定义
- 📝 丰富的组件类型
  - 文本组件（标题、正文、段落）
  - 图片组件（支持上传和裁剪）
  - 分隔线组件
- 💾 数据管理
  - 自动保存到本地存储
  - 支持导出/导入简历数据
  - 云端数据同步
- 📄 导出功能
  - 导出为 PDF 文件
  - 支持自定义页面大小和边距
- 🖼️ 图片处理
  - 支持图片上传到阿里云 OSS
  - 图片代理服务，解决跨域问题
  - 图片压缩和优化

## 技术栈

### 前端
- Vue 3 - 渐进式 JavaScript 框架
- TypeScript - JavaScript 的超集
- Vite - 下一代前端构建工具
- Pinia - Vue 的状态管理库
- Element Plus - 基于 Vue 3 的组件库
- iText 7 - PDF 生成库

### 后端
- Spring Boot - Java 后端框架
- MyBatis - ORM 框架
- MySQL - 关系型数据库
- 阿里云 OSS - 对象存储服务

## 项目设置

### 环境要求
- Node.js >= 16.0.0
- Java >= 11
- MySQL >= 8.0

### 前端开发环境设置
```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 跳过类型检查构建（开发阶段）
npx vite build
```

### 后端开发环境设置
```bash
# 进入后端目录
cd backend

# 使用 Maven 安装依赖
mvn install

# 运行应用
mvn spring-boot:run
```

### 配置文件
1. 前端配置
   - 在 `frontend/.env` 文件中配置环境变量
   - 在 `frontend/src/config` 目录下配置应用参数

2. 后端配置
   - 在 `src/main/resources/application.yml` 中配置数据库和 OSS
   - 配置阿里云 OSS 的访问密钥和 Bucket 信息

## 项目结构
```
resume_king/
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── components/     # Vue 组件
│   │   ├── store/         # Pinia 状态管理
│   │   ├── template/      # 简历模板
│   │   └── utils/         # 工具函数
│   └── public/            # 静态资源
└── backend/               # 后端项目
    ├── src/
    │   ├── controller/    # 控制器
    │   ├── service/      # 服务层
    │   ├── mapper/       # MyBatis 映射
    │   └── entity/       # 实体类
    └── resources/        # 配置文件
```

## 使用指南

### 创建新简历
1. 点击"新建简历"按钮
2. 选择喜欢的模板
3. 开始编辑内容

### 编辑简历
1. 拖拽组件到画布
2. 调整组件位置和大小
3. 编辑文本内容和样式
4. 上传和调整图片

### 导出简历
1. 点击"导出"按钮
2. 选择导出格式（PDF）
3. 等待生成完成
4. 下载文件

## 开发指南

### 添加新模板
1. 在 `frontend/src/template` 目录下创建新模板文件
2. 实现模板组件配置
3. 在模板选择器中添加新模板

### 添加新组件
1. 在 `frontend/src/components` 目录下创建新组件
2. 实现组件逻辑和样式
3. 在组件选择器中注册新组件

## 贡献指南
1. Fork 项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证
MIT License

## 联系方式
- 项目维护者：[mp9]
- 邮箱：[1353632408@qq.com]
- CSDN：[mp9只想干开发] 


![img.png](img.png)