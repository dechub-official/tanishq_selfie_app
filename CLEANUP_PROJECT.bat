@echo off
REM =====================================================
REM Project Cleanup Script
REM Removes unnecessary documentation and script files
REM Keeps essential deployment files and project structure
REM =====================================================

echo =====================================================
echo Starting Project Cleanup
echo =====================================================
echo.

REM Create a backup list before deletion
echo Creating backup list of files to be deleted...
echo Files to be deleted: > DELETED_FILES_LIST.txt
echo. >> DELETED_FILES_LIST.txt

REM =====================================================
REM DELETE UNNECESSARY MD FILES (Documentation)
REM =====================================================
echo.
echo [1/3] Removing unnecessary .md documentation files...

for %%f in (
    "ABSOLUTE_FINAL_FIX.md"
    "ACTUAL_ROOT_CAUSE_FIX.md"
    "ALL_CSV_FILES_SUMMARY.md"
    "BUILD_ERROR_FIX.md"
    "CLEANUP_COMPLETE.md"
    "CLEANUP_SUMMARY.md"
    "CLIENT_HANDOVER_CREDENTIALS.md"
    "CLIENT_SIEM_SSO_SUMMARY.md"
    "COMPLETE_DEPLOYMENT_GUIDE.md"
    "COMPLETE_ERROR_ANALYSIS_AND_SOLUTION.md"
    "COMPLETE_SOLUTION.md"
    "CSV_COLUMN_MAPPING_ISSUE.md"
    "CSV_COLUMN_ORDER_VISUAL_GUIDE.md"
    "CSV_IMPORT_ANALYSIS_AND_FIX.md"
    "CSV_IMPORT_CHECKLIST.md"
    "CSV_IMPORT_COMPLETE_SOLUTION.md"
    "CSV_IMPORT_FIX_ISSUES.md"
    "CSV_IMPORT_INDEX.md"
    "CSV_IMPORT_MANUAL_STEPS.md"
    "CSV_IMPORT_QUICK_FIX.md"
    "CSV_IMPORT_QUICK_SOLUTION.md"
    "CSV_IMPORT_QUICK_START.md"
    "CSV_IMPORT_VISUAL_GUIDE.md"
    "CSV_UPLOAD_CHECKLIST.md"
    "CSV_UPLOAD_DOCUMENTATION_INDEX.md"
    "CSV_UPLOAD_QUICK_COMMANDS.md"
    "CSV_UPLOAD_SOLUTION_READY.md"
    "CSV_UPLOAD_VISUAL_GUIDE.md"
    "DATABASE_SECURITY_SETUP.md"
    "DATA_DOCUMENTATION_INDEX.md"
    "DATA_FLOW_DIAGRAM.md"
    "DATA_MANAGEMENT_OVERVIEW.md"
    "DATA_MIGRATION_GUIDE.md"
    "DATA_MIGRATION_QUICK_REFERENCE.md"
    "DATA_QUICK_REFERENCE.md"
    "DEPLOYMENT_METHODS_COMPARISON.md"
    "DEPLOYMENT_QUICK_REFERENCE.md"
    "DEPLOYMENT_SUCCESS_VERIFY.md"
    "DEPLOY_FIX_NOW.md"
    "DEPLOY_PRODUCTION_NOHUP.md"
    "DEPLOY_PRODUCTION_NOW.md"
    "FIXED_IMPORT_COMMAND.md"
    "FIX_502_BAD_GATEWAY.md"
    "FIX_COLLATION_IMPORT.md"
    "FIX_LOCAL_INFILE_ERROR.md"
    "FIX_NOT_DEPLOYED_YET.md"
    "FIX_PASSWORD_CHANGE_ERROR.md"
    "FIX_TABLE_NOT_EXIST.md"
    "FRONTEND_DATA_SOURCE_ISSUE.md"
    "GREETING_INDEX.md"
    "GREETING_PROJECT_ANALYSIS.md"
    "GREETING_QUICK_REFERENCE.md"
    "GREETING_VERIFICATION_GUIDE.md"
    "GREETING_VISUAL_GUIDE.md"
    "IMPORT_CSV_TO_MYSQL.md"
    "INDEX_CSV_IMPORT.md"
    "LEGACY_DATA_MIGRATION_GUIDE.md"
    "NEXT_STEPS.md"
    "PASSWORD_FIX_COMPLETE_GUIDE.md"
    "PASSWORD_FIX_FINAL_V3.md"
    "PRODUCTION_DEPLOYMENT_CHECKLIST.md"
    "PRODUCTION_MIGRATION_GUIDE.md"
    "PRODUCTION_MYSQL_CONFIG.md"
    "PRODUCTION_STATUS_SUCCESS.md"
    "PROJECT_CLEANUP_REPORT.md"
    "QUICK_FIX_OCTOBER.md"
    "QUICK_START_NOHUP.md"
    "QUICK_START_PASSWORD_FIX.txt"
    "README_CSV_IMPORT.md"
    "README_CSV_IMPORT_SOLUTION.md"
    "README_DATA_MIGRATION.md"
    "SIEM_SSO_COMPATIBILITY_ANALYSIS.md"
    "SIMPLE_DATA_MIGRATION_STEPS.md"
    "TRANSFER_INSTRUCTIONS.txt"
    "TROUBLESHOOT_CSV_NOT_IMPORTING.md"
    "UPLOAD_CSV_DATA_GUIDE.md"
    "VENDOR_ACCESS_GUIDE.md"
    "VERIFICATION_GUIDE.md"
    "WHATS_NEXT_SUCCESS.md"
    "WHY_EVENTS_NOT_SHOWING.md"
    "WHY_STILL_NOT_SHOWING.md"
    "YOUR_DEPLOYMENT_PROCESS.md"
    "YOUR_QUESTIONS_ANSWERED.md"
    "DEPLOY_NOW.txt"
) do (
    if exist %%f (
        echo Deleting: %%f
        echo %%f >> DELETED_FILES_LIST.txt
        del /f /q %%f
    )
)

REM =====================================================
REM DELETE UNNECESSARY BAT FILES (Temporary/Diagnostic)
REM =====================================================
echo.
echo [2/3] Removing unnecessary .bat script files...

for %%f in (
    "BUILD_PASSWORD_FIX.bat"
    "BUILD_PREPROD.bat"
    "DEPLOY_FIX.bat"
    "export_database_for_preprod.bat"
    "migrate_data_windows.bat"
    "SETUP_PRODUCTION_DATABASE.bat"
    "setup_scripts_on_server.bat"
    "UPLOAD_AND_DEPLOY.bat"
    "upload_csv_files.bat"
    "upload_to_production.bat"
) do (
    if exist %%f (
        echo Deleting: %%f
        echo %%f >> DELETED_FILES_LIST.txt
        del /f /q %%f
    )
)

REM =====================================================
REM DELETE UNNECESSARY SH FILES (Diagnostic/Temporary)
REM =====================================================
echo.
echo [3/3] Removing unnecessary .sh script files...

for %%f in (
    "check_production_ready.sh"
    "complete_csv_import_fixed.sh"
    "deploy_password_fix.sh"
    "diagnose_csv_ids.sh"
    "diagnose_csv_import.sh"
    "diagnose_import.sh"
    "diagnostic_production.sh"
    "export_november_to_sheets.sh"
    "export_preprod_data.sh"
    "final_verification.sh"
    "fix_october_import.sh"
    "import_csv_now.sh"
    "import_csv_to_mysql.sh"
    "import_csv_to_mysql_fixed.sh"
    "import_production_data.sh"
    "migrate_data.sh"
    "migrate_preprod_to_prod.sh"
    "setup_production_server.sh"
    "start_application_secure.sh"
    "validate_csv_files.sh"
    "verify_and_fix_final.sh"
    "verify_deployment.sh"
    "verify_production_ready.sh"
) do (
    if exist %%f (
        echo Deleting: %%f
        echo %%f >> DELETED_FILES_LIST.txt
        del /f /q %%f
    )
)

REM =====================================================
REM DELETE POWERSHELL SCRIPT (Temporary)
REM =====================================================
for %%f in (
    "upload_csv_windows.ps1"
) do (
    if exist %%f (
        echo Deleting: %%f
        echo %%f >> DELETED_FILES_LIST.txt
        del /f /q %%f
    )
)

REM =====================================================
REM DELETE SQL SCRIPTS (Temporary/Diagnostic)
REM =====================================================
for %%f in (
    "setup_database_security.sql"
    "setup_production_database.sql"
    "verification_queries.sql"
) do (
    if exist %%f (
        echo Deleting: %%f
        echo %%f >> DELETED_FILES_LIST.txt
        del /f /q %%f
    )
)

REM =====================================================
REM SUMMARY
REM =====================================================
echo.
echo =====================================================
echo Cleanup Complete!
echo =====================================================
echo.
echo Files kept (Essential for deployment):
echo   - README.md (Main project documentation)
echo   - pom.xml (Maven build file)
echo   - build-preprod.bat (Build script)
echo   - deploy_production.sh (Production deployment)
echo   - src/ (Source code)
echo   - target/ (Build output)
echo   - storage/ (Storage directory)
echo   - database_backup/ (Database backups)
echo   - .git/ (Version control)
echo   - .gitignore
echo.
echo Deleted files list saved to: DELETED_FILES_LIST.txt
echo.
echo Your project is now clean and ready for deployment!
echo No database, server, or application functionality affected.
echo.
pause

