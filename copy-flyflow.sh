#!/bin/bash
# FlyFlow 资源复制脚本

cd /Users/yanchao/IdeaProjects/diom-workFlow

SOURCE_DIR="flyflow-vue3-master/src/views/flyflow"
TARGET_DIR="diom-frontend/src/flyflow"

echo "========================================"
echo "  开始复制 FlyFlow 资源到 diom-frontend"
echo "========================================"
echo ""

# 检查源目录是否存在
if [ ! -d "$SOURCE_DIR" ]; then
  echo "❌ 错误：源目录不存在: $SOURCE_DIR"
  exit 1
fi

# 创建目标目录
echo "📁 创建目标目录..."
mkdir -p "$TARGET_DIR"

# 复制组件（核心）
echo "📦 复制组件 (components)..."
cp -r "$SOURCE_DIR/components" "$TARGET_DIR/"
echo "   ✅ 已复制 $(find "$TARGET_DIR/components" -type f | wc -l | tr -d ' ') 个组件文件"

# 复制 API
echo "📦 复制 API (api)..."
cp -r "$SOURCE_DIR/api" "$TARGET_DIR/"
echo "   ✅ 已复制 $(find "$TARGET_DIR/api" -type f | wc -l | tr -d ' ') 个 API 文件"

# 复制状态管理
echo "📦 复制状态管理 (stores)..."
cp -r "$SOURCE_DIR/stores" "$TARGET_DIR/"
echo "   ✅ 已复制 $(find "$TARGET_DIR/stores" -type f | wc -l | tr -d ' ') 个 store 文件"

# 复制工具函数
echo "📦 复制工具函数 (utils)..."
cp -r "$SOURCE_DIR/utils" "$TARGET_DIR/"
echo "   ✅ 已复制 $(find "$TARGET_DIR/utils" -type f | wc -l | tr -d ' ') 个工具文件"

# 复制资源文件
echo "📦 复制资源文件 (assets)..."
cp -r "$SOURCE_DIR/assets" "$TARGET_DIR/"
echo "   ✅ 已复制 $(find "$TARGET_DIR/assets" -type f | wc -l | tr -d ' ') 个资源文件"

# 复制样式文件
echo "📦 复制样式文件 (css)..."
cp -r "$SOURCE_DIR/css" "$TARGET_DIR/"
echo "   ✅ 已复制 $(find "$TARGET_DIR/css" -type f | wc -l | tr -d ' ') 个样式文件"

# 复制页面文件
echo "📦 复制页面文件 (views)..."
cp -r "$SOURCE_DIR/views" "$TARGET_DIR/"
echo "   ✅ 已复制 $(find "$TARGET_DIR/views" -type f | wc -l | tr -d ' ') 个页面文件"

echo ""
echo "========================================"
echo "  ✅ 复制完成！"
echo "========================================"
echo ""
echo "📊 统计信息："
echo "   - 总文件数: $(find "$TARGET_DIR" -type f | wc -l | tr -d ' ') 个"
echo "   - 总目录数: $(find "$TARGET_DIR" -type d | wc -l | tr -d ' ') 个"
echo "   - 总大小: $(du -sh "$TARGET_DIR" | cut -f1)"
echo ""
echo "📝 下一步："
echo "   1. cd diom-frontend"
echo "   2. npm install @logicflow/core @logicflow/extension lodash-es moment nprogress"
echo "   3. 查看迁移文档: cat FLYFLOW_MIGRATION_PLAN.md"
echo ""

