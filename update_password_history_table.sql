-- Drop the existing password_history table
DROP TABLE IF EXISTS password_history;

-- Create new password_history table with btq_code as primary key (no id column)
CREATE TABLE password_history (
    btq_code VARCHAR(255) NOT NULL PRIMARY KEY,
    old_password VARCHAR(255),
    new_password VARCHAR(255) NOT NULL,
    changed_at DATETIME(6) NOT NULL
);

-- Verify the table structure
DESCRIBE password_history;
