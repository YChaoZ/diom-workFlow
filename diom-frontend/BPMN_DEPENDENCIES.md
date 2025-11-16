# BPMN设计器依赖安装说明

## 📦 需要安装的NPM包

```bash
cd diom-frontend

# 安装bpmn-js核心库
npm install bpmn-js@^14.0.0

# 安装属性面板
npm install bpmn-js-properties-panel@^3.0.0

# 安装Camunda扩展
npm install camunda-bpmn-moddle@^7.0.0

# 安装依赖后验证
npm list bpmn-js
npm list bpmn-js-properties-panel
npm list camunda-bpmn-moddle
```

## 📝 package.json 依赖

在 `package.json` 中添加以下依赖：

```json
{
  "dependencies": {
    "bpmn-js": "^14.0.0",
    "bpmn-js-properties-panel": "^3.0.0",
    "camunda-bpmn-moddle": "^7.0.0"
  }
}
```

## ✅ 安装验证

运行以下命令验证安装成功：

```bash
npm run dev
```

如果启动成功且无报错，说明依赖安装完成。

