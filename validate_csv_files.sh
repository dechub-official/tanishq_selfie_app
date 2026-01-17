#!/bin/bash

# =====================================================
# CSV VALIDATION SCRIPT
# =====================================================
# Validates CSV files before import to catch common issues
# =====================================================

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

CSV_DIR="/opt/tanishq/csv_import"

echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}CSV FILE VALIDATION${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Check if directory exists
if [ ! -d "$CSV_DIR" ]; then
    echo -e "${RED}✗ Directory not found: $CSV_DIR${NC}"
    exit 1
fi

# Function to validate CSV
validate_csv() {
    local file=$1
    local name=$(basename "$file")

    echo -e "\n${BLUE}Validating: ${name}${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    # Check if file exists
    if [ ! -f "$file" ]; then
        echo -e "${RED}✗ File not found${NC}"
        return 1
    fi

    # Check file size
    size=$(du -h "$file" | cut -f1)
    echo -e "${GREEN}✓ File exists (${size})${NC}"

    # Check if file is empty
    if [ ! -s "$file" ]; then
        echo -e "${RED}✗ File is empty${NC}"
        return 1
    fi

    # Count lines
    total_lines=$(wc -l < "$file")
    data_lines=$((total_lines - 1))
    echo -e "${GREEN}✓ Total lines: ${total_lines} (${data_lines} data rows)${NC}"

    # Check for header row
    header=$(head -1 "$file")
    echo -e "${BLUE}  Header: ${header:0:80}...${NC}"

    # Check encoding (look for BOM)
    if file "$file" | grep -q "with BOM"; then
        echo -e "${YELLOW}⚠ WARNING: File has BOM (Byte Order Mark)${NC}"
        echo -e "${YELLOW}  Recommendation: Save as UTF-8 without BOM${NC}"
    else
        echo -e "${GREEN}✓ Encoding looks good${NC}"
    fi

    # Check for empty lines
    empty_lines=$(grep -c '^[[:space:]]*$' "$file")
    if [ $empty_lines -gt 0 ]; then
        echo -e "${YELLOW}⚠ WARNING: Found ${empty_lines} empty lines${NC}"
    else
        echo -e "${GREEN}✓ No empty lines${NC}"
    fi

    # Check line endings
    if file "$file" | grep -q "CRLF"; then
        echo -e "${YELLOW}⚠ Windows line endings (CRLF) detected${NC}"
        echo -e "${YELLOW}  This is OK, but LF is preferred${NC}"
    else
        echo -e "${GREEN}✓ Unix line endings (LF)${NC}"
    fi

    # Count columns in header
    header_cols=$(echo "$header" | tr ',' '\n' | wc -l)
    echo -e "${BLUE}  Columns in header: ${header_cols}${NC}"

    # Check first data row
    if [ $total_lines -gt 1 ]; then
        first_data=$(sed -n '2p' "$file")
        data_cols=$(echo "$first_data" | tr ',' '\n' | wc -l)

        if [ $header_cols -eq $data_cols ]; then
            echo -e "${GREEN}✓ Column count matches (${data_cols})${NC}"
        else
            echo -e "${RED}✗ Column count mismatch!${NC}"
            echo -e "${RED}  Header: ${header_cols} columns${NC}"
            echo -e "${RED}  Data:   ${data_cols} columns${NC}"
        fi

        echo -e "${BLUE}  First data row: ${first_data:0:80}...${NC}"
    fi

    # Show preview
    echo -e "\n${BLUE}Preview (first 3 rows):${NC}"
    head -3 "$file" | while read line; do
        echo "  ${line:0:100}..."
    done

    echo ""
}

# Validate each CSV file
echo -e "${BLUE}Checking CSV files in: ${CSV_DIR}${NC}\n"

validate_csv "${CSV_DIR}/events.csv"
validate_csv "${CSV_DIR}/attendees.csv"
validate_csv "${CSV_DIR}/invitees.csv"

# Summary
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}VALIDATION SUMMARY${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Check MySQL table structures
echo -e "${BLUE}Comparing with database table structures...${NC}\n"

echo -e "${YELLOW}Events table columns:${NC}"
mysql -u root -pNagaraj@07 -e "DESCRIBE selfie_prod.events;" 2>/dev/null | grep "Field" -A 100 | tail -n +2 | awk '{print $1}' | head -20

echo -e "\n${YELLOW}Attendees table columns:${NC}"
mysql -u root -pNagaraj@07 -e "DESCRIBE selfie_prod.attendees;" 2>/dev/null | grep "Field" -A 100 | tail -n +2 | awk '{print $1}' | head -20

echo -e "\n${YELLOW}Invitees table columns:${NC}"
mysql -u root -pNagaraj@07 -e "DESCRIBE selfie_prod.invitees;" 2>/dev/null | grep "Field" -A 100 | tail -n +2 | awk '{print $1}' | head -20

echo -e "\n${GREEN}Validation complete!${NC}"
echo -e "\n${YELLOW}Next step: Run ./import_csv_to_mysql.sh${NC}\n"

