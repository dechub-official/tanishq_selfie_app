@echo off
REM Script to view MySQL tables in proper format
REM This fixes the messy display issue

echo ============================================
echo MySQL Table Viewer - Proper Format
echo ============================================
echo.

REM Set MySQL to use table format
mysql -u jewdev -p -t --column-names -h 10-160-128-94 applications_preprod -e "SELECT event_id, SUBSTRING(event_name, 1, 25) as event_name, event_type, event_date, location, SUBSTRING(event_url, 1, 40) as event_url, max_attendees FROM events ORDER BY event_date DESC LIMIT 20;"

echo.
echo ============================================
echo Done! Data displayed in table format
echo ============================================
pause

