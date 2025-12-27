#!/usr/bin/env zsh
set -e

# Versions
SIEGE_VERSION="4.1.7"
PREFIX="$HOME/.local"

echo "Installing siege $SIEGE_VERSION locally (no sudo)..."

# Create directories
mkdir -p "$HOME/src"
mkdir -p "$PREFIX"

cd "$HOME/src"

# Download siege source
if [ ! -f "siege-$SIEGE_VERSION.tar.gz" ]; then
    echo "Downloading siege..."
    curl -LO "http://download.joedog.org/siege/siege-$SIEGE_VERSION.tar.gz"
fi

# Extract
tar -xzf "siege-$SIEGE_VERSION.tar.gz"
cd "siege-$SIEGE_VERSION"

# Configure for local install
./configure --prefix="$PREFIX"

# Build
make

# Install locally
make install

# Add to PATH if not already added
if ! echo "$PATH" | grep -q "$PREFIX/bin"; then
    echo "" >> ~/.zshrc
    echo "# Siege local install" >> ~/.zshrc
    echo "export PATH=\"$PREFIX/bin:\$PATH\"" >> ~/.zshrc
    echo "PATH updated in ~/.zshrc"
fi

echo "Siege installed successfully!"
echo "Restart your terminal or run:"
echo "  source ~/.zshrc"
echo "Test with:"
echo "  siege --version"
