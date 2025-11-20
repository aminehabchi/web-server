#!/bin/zsh

VERSION="3.9.6"
MAVEN_DIR="$HOME/apache-maven-$VERSION"

echo "Downloading Maven $VERSION..."
wget https://dlcdn.apache.org/maven/maven-3/$VERSION/binaries/apache-maven-$VERSION-bin.tar.gz

echo "Extracting..."
tar -xvzf apache-maven-$VERSION-bin.tar.gz -C $HOME

echo "Updating .zshrc ..."
if ! grep -q "apache-maven-$VERSION" ~/.zshrc; then
  echo 'export MAVEN_HOME="$HOME/apache-maven-'"$VERSION"'"' >> ~/.zshrc
  echo 'export PATH="$MAVEN_HOME/bin:$PATH"' >> ~/.zshrc
fi

echo "Reloading shell..."
source ~/.zshrc

echo "Done. Run 'mvn -v' to verify."
