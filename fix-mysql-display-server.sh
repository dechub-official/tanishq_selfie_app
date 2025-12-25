#!/bin/bash
# Fix MySQL Display on Server - Make tables show properly
# Run this on your server: bash fix-mysql-display-server.sh

echo "========================================"
echo "🔧 MySQL Display Fix for Server"
echo "========================================"
echo ""

# Create MySQL config file
echo "📝 Creating MySQL configuration file..."

cat > ~/.my.cnf << 'EOF'
[mysql]
# Force table output format
table

# Show column names
column-names

# Use pager for large results (allows horizontal scrolling)
pager=less -S -n -i -F -X

# Auto-complete table and column names
auto-rehash

# Show warnings after queries
show-warnings

# Better prompt showing current database
prompt='mysql [\d]> '

# Set max column width for better display
max_allowed_packet=16M

[client]
# Default character set
default-character-set=utf8mb4
EOF

echo "✅ MySQL config created at ~/.my.cnf"
echo ""

# Set proper terminal environment
echo "📝 Configuring terminal settings..."

# Add to bashrc if not already there
if ! grep -q "export COLUMNS=200" ~/.bashrc; then
    echo "export COLUMNS=200" >> ~/.bashrc
    echo "export LANG=en_US.UTF-8" >> ~/.bashrc
    echo "✅ Terminal settings added to ~/.bashrc"
else
    echo "✅ Terminal settings already configured"
fi

# Apply immediately
export COLUMNS=200
export LANG=en_US.UTF-8

echo ""
echo "========================================"
echo "✅ FIX APPLIED SUCCESSFULLY!"
echo "========================================"
echo ""
echo "🎉 Your MySQL tables will now display properly!"
echo ""
echo "📋 How to use:"
echo ""
echo "   1. Connect to MySQL:"
echo "      mysql -u root -p applications_preprod"
echo ""
echo "   2. Run queries - they'll display in nice tables:"
echo "      SELECT * FROM events LIMIT 10;"
echo ""
echo "   3. For very wide tables, use vertical display:"
echo "      SELECT * FROM events WHERE event_id=1\G"
echo ""
echo "   4. Use pager commands inside MySQL:"
echo "      \P less -S       (enable horizontal scrolling)"
echo "      \n               (disable pager)"
echo ""
echo "========================================"
echo "🔍 Quick Tests:"
echo "========================================"
echo ""
echo "Test 1: Check events table"
echo "   mysql -u root -p applications_preprod -e 'SELECT event_id, event_name, event_type FROM events LIMIT 5;'"
echo ""
echo "Test 2: Check stores table"
echo "   mysql -u root -p applications_preprod -e 'SELECT store_id, store_code, store_name FROM stores LIMIT 5;'"
echo ""
echo "========================================"
echo "✅ Setup Complete!"
echo "========================================"

