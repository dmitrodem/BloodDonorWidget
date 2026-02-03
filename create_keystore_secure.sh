#!/bin/bash
# Create keystore for signing the APK with auto-generated passwords
# This script should be run locally, not committed to git

KEYSTORE_FILE="blood_donor_widget.jks"
KEY_ALIAS="blood_donor_widget"
PROPERTIES_FILE="keystore.properties"

# Generate random passwords
STORE_PASSWORD=$(openssl rand -base64 32 | tr -dc 'a-zA-Z0-9' | head -c 16)

echo "Creating keystore for Blood Donor Widget..."
echo ""

# Generate keystore with auto-generated passwords
keytool -genkey -v \
  -keystore "$KEYSTORE_FILE" \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -dname "CN=Blood Donor Widget, OU=Development, O=Example, L=City, ST=State, C=US" \
  -storepass "$STORE_PASSWORD" \
  -keypass "$STORE_PASSWORD"

# Create keystore.properties file
cat > "$PROPERTIES_FILE" << PROP
storeFile=$KEYSTORE_FILE
storePassword=$STORE_PASSWORD
keyAlias=$KEY_ALIAS
keyPassword=$KEY_PASSWORD
PROP

echo ""
echo "✅ Keystore created: $KEYSTORE_FILE"
echo "✅ Properties file created: $PROPERTIES_FILE"
echo ""
echo "📋 Keystore details:"
echo "   Store file: $KEYSTORE_FILE"
echo "   Key alias: $KEY_ALIAS"
echo "   Store password: $STORE_PASSWORD"
echo "   Key password: $STORE_PASSWORD"
echo ""
echo "⚠️  IMPORTANT: Save these passwords! You'll need them for future releases."
echo "   The keystore.properties file has been created with these values."
echo ""
echo "🔒 Security note:"
echo "   - $PROPERTIES_FILE is in .gitignore (won't be committed)"
echo "   - Keep $KEYSTORE_FILE and $PROPERTIES_FILE secure"
echo "   - Never commit these files to version control"
echo ""
echo "🚀 To build release APK:"
echo "   ./gradlew assembleRelease"
