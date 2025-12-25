#!/bin/bash
# ================================================
# Quick Sample Data Creation for Testing
# ================================================
# Use this if you don't have production data to import
# This creates minimal data to test the application

echo "================================================"
echo "Creating Sample Data for Testing"
echo "================================================"
echo ""

# Database Configuration
DB_USER="root"
DB_PASS="Dechub#2025"
DB_NAME="selfie_preprod"

echo "[1/4] Creating Sample Stores..."
mysql -u $DB_USER -p$DB_PASS $DB_NAME << 'EOF'
DELETE FROM stores WHERE storeCode IN ('BLR001', 'MUM001', 'DEL001', 'CHE001', 'HYD001');

INSERT INTO stores (
    storeCode, storeName, storeAddress, storeCity, storeState, storeCountry,
    storePhoneNoOne, storeEmailId, region, storeType
) VALUES
('BLR001', 'Tanishq Bangalore MG Road', 'MG Road', 'Bangalore', 'Karnataka', 'India', '08012345601', 'blr001@tanishq.co.in', 'SOUTH', 'Showroom'),
('MUM001', 'Tanishq Mumbai Fort', 'Fort Area', 'Mumbai', 'Maharashtra', 'India', '02212345601', 'mum001@tanishq.co.in', 'WEST', 'Showroom'),
('DEL001', 'Tanishq Delhi Connaught Place', 'CP', 'New Delhi', 'Delhi', 'India', '01112345601', 'del001@tanishq.co.in', 'NORTH', 'Showroom'),
('CHE001', 'Tanishq Chennai T Nagar', 'T Nagar', 'Chennai', 'Tamil Nadu', 'India', '04412345601', 'che001@tanishq.co.in', 'SOUTH', 'Showroom'),
('HYD001', 'Tanishq Hyderabad Banjara Hills', 'Banjara Hills', 'Hyderabad', 'Telangana', 'India', '04012345601', 'hyd001@tanishq.co.in', 'SOUTH', 'Showroom');
EOF

if [ $? -eq 0 ]; then
    echo "✅ 5 stores created successfully"
else
    echo "❌ Error creating stores"
    exit 1
fi
echo ""

echo "[2/4] Creating Sample Users (Store Managers)..."
mysql -u $DB_USER -p$DB_PASS $DB_NAME << 'EOF'
DELETE FROM users WHERE code IN ('BLR001', 'MUM001', 'DEL001', 'CHE001', 'HYD001');

INSERT INTO users (code, password, role, created_at) VALUES
('BLR001', 'Password123', 'STORE_MANAGER', NOW()),
('MUM001', 'Password123', 'STORE_MANAGER', NOW()),
('DEL001', 'Password123', 'STORE_MANAGER', NOW()),
('CHE001', 'Password123', 'STORE_MANAGER', NOW()),
('HYD001', 'Password123', 'STORE_MANAGER', NOW());
EOF

if [ $? -eq 0 ]; then
    echo "✅ 5 user accounts created successfully"
    echo "   Username: BLR001, MUM001, DEL001, CHE001, HYD001"
    echo "   Password: Password123"
else
    echo "❌ Error creating users"
    exit 1
fi
echo ""

echo "[3/4] Creating Sample Events..."
mysql -u $DB_USER -p$DB_PASS $DB_NAME << 'EOF'
DELETE FROM events WHERE id IN ('EVT001', 'EVT002', 'EVT003', 'EVT004', 'EVT005');

INSERT INTO events (
    id, store_code, event_name, event_type, start_date,
    created_at, invitees, attendees, region
) VALUES
('EVT001', 'BLR001', 'Diwali Gold Festival 2025', 'Festival', '2025-11-10', NOW(), 50, 35, 'SOUTH'),
('EVT002', 'MUM001', 'Wedding Collection Launch', 'Launch', '2025-11-15', NOW(), 30, 28, 'WEST'),
('EVT003', 'CHE001', 'Diamond Exhibition', 'Exhibition', '2025-12-05', NOW(), 40, 15, 'SOUTH'),
('EVT004', 'DEL001', 'Rivaah Bridal Showcase', 'Exhibition', '2025-12-15', NOW(), 60, 0, 'NORTH'),
('EVT005', 'HYD001', 'New Year Gold Sale', 'Sale', '2025-12-31', NOW(), 100, 0, 'SOUTH');
EOF

if [ $? -eq 0 ]; then
    echo "✅ 5 events created successfully"
else
    echo "❌ Error creating events"
    exit 1
fi
echo ""

echo "[4/4] Creating Sample Attendees..."
mysql -u $DB_USER -p$DB_PASS $DB_NAME << 'EOF'
DELETE FROM attendees WHERE event_id IN ('EVT001', 'EVT002', 'EVT003');

INSERT INTO attendees (
    event_id, name, phone, like_product, first_time_tqsl, created_at
) VALUES
('EVT001', 'Rajesh Kumar', '9876543210', 'Gold Necklace', 'Y', NOW()),
('EVT001', 'Priya Sharma', '9876543211', 'Diamond Earrings', 'N', NOW()),
('EVT001', 'Amit Patel', '9876543212', 'Gold Bangles', 'Y', NOW()),
('EVT002', 'Sneha Reddy', '9876543213', 'Wedding Ring Set', 'Y', NOW()),
('EVT002', 'Vikram Singh', '9876543214', 'Platinum Chain', 'N', NOW()),
('EVT003', 'Lakshmi Iyer', '9876543215', 'Temple Jewellery', 'Y', NOW()),
('EVT003', 'Arjun Nair', '9876543216', 'Diamond Ring', 'N', NOW());
EOF

if [ $? -eq 0 ]; then
    echo "✅ 7 sample attendees created successfully"
else
    echo "❌ Error creating attendees"
    exit 1
fi
echo ""

echo "================================================"
echo "✅ Sample Data Creation Complete!"
echo "================================================"
echo ""
echo "📊 Summary:"
echo "  • 5 Stores (BLR001, MUM001, DEL001, CHE001, HYD001)"
echo "  • 5 Users (Login with store code + Password123)"
echo "  • 5 Events (EVT001 to EVT005)"
echo "  • 7 Attendees"
echo ""
echo "🧪 Test Login:"
echo "  Code: BLR001"
echo "  Password: Password123"
echo ""
echo "🔍 Verify Data:"
echo "  mysql -u root -pDechub#2025 selfie_preprod -e 'SELECT storeCode, storeName FROM stores;'"
echo ""
echo "🚀 Next Steps:"
echo "  1. Test login API"
echo "  2. Test event creation"
echo "  3. Test file upload"
echo "  4. Run full test suite"
echo ""

