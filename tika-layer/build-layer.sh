#!/bin/bash
# Build script for Tika Lambda Layer
# This script builds the layer package structure required by AWS Lambda

set -e

echo "Building Tika Lambda Layer..."

# Build Maven project
mvn clean package

# Create layer directory structure
LAYER_DIR="target/layer"
mkdir -p "$LAYER_DIR/java/lib"

# Copy dependencies to layer
cp -r target/layer/java/lib/* "$LAYER_DIR/java/lib/" 2>/dev/null || true

# If dependencies weren't copied by the plugin, copy them manually
if [ ! -d "$LAYER_DIR/java/lib" ] || [ -z "$(ls -A $LAYER_DIR/java/lib)" ]; then
    echo "Copying dependencies manually..."
    mvn dependency:copy-dependencies -DoutputDirectory="$LAYER_DIR/java/lib" -DincludeScope=runtime
fi

echo "Layer build complete. Layer structure:"
echo "  $LAYER_DIR/java/lib/"

ls -lh "$LAYER_DIR/java/lib" | head -10
