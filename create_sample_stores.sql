-- =====================================================
-- CREATE STORES DATABASE - SAMPLE DATA
-- =====================================================
-- This creates sample stores for each region
-- You can add more stores following the same pattern
-- =====================================================

-- NORTH 1 Region
INSERT INTO stores (
    store_code, region, level, store_name, store_address, store_city, store_state, 
    store_country, store_zip_code, store_phone_no_one, store_phone_no_two, 
    store_email_id, store_latitude, store_longitude, store_date_of_opening, 
    store_type, store_opening_time, store_closing_time, store_manager_name, 
    store_manager_no, store_manager_email, store_location_link
) VALUES 
('PRA', 'NORTH 1', 'LvL 1', 'Delhi - Pithampura', 
 'No.31, Nishant Kunj, Pitampura Main Road, Pitampura', 
 'Delhi', 'Delhi', 'India', '110034', 
 '011 49377100', '01149377100-125', 'btqpra@titan.co.in', 
 '28.69637', '77.14379', '30/3/2008', 'Stand-alone store', 
 '11:00:00', '20:00:00', 'Rohit Gautam.', '9871411503', 
 'rohit2@titan.co.in', 'https://bit.ly/3Ul9hnn')
ON DUPLICATE KEY UPDATE 
    region = VALUES(region), 
    level = VALUES(level),
    store_name = VALUES(store_name);

-- NORTH 2 Region
INSERT INTO stores (
    store_code, region, level, store_name, store_address, store_city, store_state, 
    store_country, store_zip_code, store_phone_no_one, store_phone_no_two, 
    store_email_id, store_latitude, store_longitude, store_date_of_opening, 
    store_type, store_opening_time, store_closing_time, store_manager_name, 
    store_manager_no, store_manager_email, store_location_link
) VALUES 
('LKO', 'NORTH 2', 'LvL 1', 'Lucknow - Gomti Nagar', 
 'CP 1, Anand Plaza, Viram Khand 1, Gomti Nagar, Near Patrakarpuram Crossing, Opp Andhra Bank', 
 'Lucknow', 'Uttar Pradesh', 'India', '226010', 
 '0522-4060402', '0522-4060460', 'btqlko@titan.co.in', 
 '26.85268', '81.00127', '28/5/2006', 'Stand-alone store', 
 '10:30:00', '20:00:00', 'Mehtab Ali', '9999072246', 
 'mehtabali@titan.co.in', 'https://bit.ly/3w9B7Lv')
ON DUPLICATE KEY UPDATE 
    region = VALUES(region), 
    level = VALUES(level),
    store_name = VALUES(store_name);

-- NORTH 3 Region
INSERT INTO stores (
    store_code, region, level, store_name, store_address, store_city, store_state, 
    store_country, store_zip_code, store_phone_no_one, store_phone_no_two, 
    store_email_id, store_latitude, store_longitude, store_date_of_opening, 
    store_type, store_opening_time, store_closing_time, store_manager_name, 
    store_manager_no, store_manager_email, store_location_link
) VALUES 
('YNR', 'NORTH 3', 'LvL 3', 'Yamuna Nagar - Jagadhari Road', 
 'B/v/388, Opposite Telegraph Office, Near Madhu Hotel, Jagadhari Road', 
 'Yamuna Nagar', 'Haryana', 'India', '135001', 
 '7015086993', '0', 'btqynr@titan.co.in', 
 '30.12905', '77.26739', '25/10/2012', 'Stand-alone store', 
 '10:30:00', '20:00:00', 'Rajani Nagpal', '8307755911', 
 'btqynr@titan.co.in', 'https://bit.ly/3QpcWPU')
ON DUPLICATE KEY UPDATE 
    region = VALUES(region), 
    level = VALUES(level),
    store_name = VALUES(store_name);

-- NORTH 4 Region
INSERT INTO stores (
    store_code, region, level, store_name, store_address, store_city, store_state, 
    store_country, store_zip_code, store_phone_no_one, store_phone_no_two, 
    store_email_id, store_latitude, store_longitude, store_date_of_opening, 
    store_type, store_opening_time, store_closing_time, store_manager_name, 
    store_manager_no, store_manager_email, store_location_link
) VALUES 
('FRD', 'NORTH 4', 'LvL 1', 'Faridabad - Mathura Road', 
 'GF-7 &47, FF-7, CROWN INTERIOR MALL, SEC-35, MATHURA ROAD', 
 'Faridabad', 'Haryana', 'India', '121003', 
 '0129-4916600', '0129-4161204', 'btqfrd@titan.co.in', 
 '28.46913', '77.30751', '18/7/2008', 'Mall store', 
 '10:30:00', '20:30:00', 'Kavita Banerjee', '9818629365', 
 'kavitab@titan.co.in', 'https://bit.ly/3w4zqPw')
ON DUPLICATE KEY UPDATE 
    region = VALUES(region), 
    level = VALUES(level),
    store_name = VALUES(store_name);

-- SOUTH 1 Region
INSERT INTO stores (
    store_code, region, level, store_name, store_address, store_city, store_state, 
    store_country, store_zip_code, store_phone_no_one, store_phone_no_two, 
    store_email_id, store_latitude, store_longitude, store_date_of_opening, 
    store_type, store_opening_time, store_closing_time, store_manager_name, 
    store_manager_no, store_manager_email, store_location_link
) VALUES 
('CRB', 'SOUTH 1', 'LvL 1', 'Chennai - Cathedral Road', 
 'No.100, Cathedral Road, Gopalapuram', 
 'Chennai', 'Tamil Nadu', 'India', '600086', 
 '044 2811 0368', '044 2811 0405', 'btqcrb@titan.co.in', 
 '13.04675', '80.25529', '20/7/1996', 'Stand-alone store', 
 '10:30:00', '20:00:00', 'Christy.S', '9442830109', 
 'mgrcrb@titan.co.in', 'https://bit.ly/44mFhvZ')
ON DUPLICATE KEY UPDATE 
    region = VALUES(region), 
    level = VALUES(level),
    store_name = VALUES(store_name);

-- SOUTH 2 Region
INSERT INTO stores (
    store_code, region, level, store_name, store_address, store_city, store_state, 
    store_country, store_zip_code, store_phone_no_one, store_phone_no_two, 
    store_email_id, store_latitude, store_longitude, store_date_of_opening, 
    store_type, store_opening_time, store_closing_time, store_manager_name, 
    store_manager_no, store_manager_email, store_location_link
) VALUES 
('BGR', 'SOUTH 2', 'LvL 1', 'Bangalore - Bannergatta Road', 
 '174/1, Meu Square, Next To J .D Mara Signal, Bilekhalli, Bannerghatta Main Rd (Opp Brand Factory)', 
 'Bangalore', 'Karnataka', 'India', '560076', 
 '080 6366147676', '080 6366157676', 'btqbgr@titan.co.in', 
 '12.89702', '77.60716', '21/3/2014', 'Stand-alone store', 
 '11:00:00', '20:30:00', 'Mujeeb Ur Rehman', '9986193743', 
 'mgrbgr@titan.co.in', 'https://bit.ly/3QosVxA')
ON DUPLICATE KEY UPDATE 
    region = VALUES(region), 
    level = VALUES(level),
    store_name = VALUES(store_name);

-- SOUTH 3 Region
INSERT INTO stores (
    store_code, region, level, store_name, store_address, store_city, store_state, 
    store_country, store_zip_code, store_phone_no_one, store_phone_no_two, 
    store_email_id, store_latitude, store_longitude, store_date_of_opening, 
    store_type, store_opening_time, store_closing_time, store_manager_name, 
    store_manager_no, store_manager_email, store_location_link
) VALUES 
('HNR', 'SOUTH 3', 'LvL 1', 'Hyderabad - Himayat Nagar', 
 'Plot No: 3-6-369/A/24/25/26, Near TTD Kalyana Mantapam, Himayatnagar', 
 'Hyderabad', 'Telangana', 'India', '500029', 
 '040 42595550', '040 42595551', 'btqhnr@titan.co.in', 
 '17.40067', '78.48817', '22/10/2011', 'Stand-alone store', 
 '10:00:00', '20:00:00', 'Dinesh Malani', '9948449972', 
 'mgrhnr@titan.co.in', 'https://bit.ly/3wcXKP7')
ON DUPLICATE KEY UPDATE 
    region = VALUES(region), 
    level = VALUES(level),
    store_name = VALUES(store_name);

-- EAST 1 Region
INSERT INTO stores (
    store_code, region, level, store_name, store_address, store_city, store_state, 
    store_country, store_zip_code, store_phone_no_one, store_phone_no_two, 
    store_email_id, store_latitude, store_longitude, store_date_of_opening, 
    store_type, store_opening_time, store_closing_time, store_manager_name, 
    store_manager_no, store_manager_email, store_location_link
) VALUES 
('CSB', 'EAST 1', 'LvL 1', 'Kolkata - Camac Street', 
 'TANISHQ, 1ST FLOOR, BLOCK - A,22 CAMAC STREET,BEHIND WESTSIDE', 
 'Kolkata', 'West Bengal', 'India', '700017', 
 '033-40411200', '033-40411201', 'btqcsb@titan.co.in', 
 '22.546111', '88.352697', '16/10/1997', 'Stand-alone store', 
 '11:00:00', '20:30:00', 'Shreekant Singh', '8822960340', 
 'shreekantsingh@titan.co.in', 'https://bit.ly/3WheMGo')
ON DUPLICATE KEY UPDATE 
    region = VALUES(region), 
    level = VALUES(level),
    store_name = VALUES(store_name);

-- EAST 2 Region
INSERT INTO stores (
    store_code, region, level, store_name, store_address, store_city, store_state, 
    store_country, store_zip_code, store_phone_no_one, store_phone_no_two, 
    store_email_id, store_latitude, store_longitude, store_date_of_opening, 
    store_type, store_opening_time, store_closing_time, store_manager_name, 
    store_manager_no, store_manager_email, store_location_link
) VALUES 
('TPT', 'EAST 2', 'LvL 3', 'Patna - Frazer Road', 
 'Opp.Lic Jeevan Prakash, Near Central Mall, Frazer Road', 
 'Patna', 'Bihar', 'India', '800001', 
 '9308906421', '0612 220184', 'btqtpt@titan.co.in', 
 '25.61185', '85.13833', '21/4/2000', 'Stand-alone store', 
 '10:30:00', '20:00:00', 'Mayank Jain', '8804021890', 
 'btqtpt@titan.co.in', 'https://bit.ly/3WpnGBX')
ON DUPLICATE KEY UPDATE 
    region = VALUES(region), 
    level = VALUES(level),
    store_name = VALUES(store_name);

-- WEST 1 Region
INSERT INTO stores (
    store_code, region, level, store_name, store_address, store_city, store_state, 
    store_country, store_zip_code, store_phone_no_one, store_phone_no_two, 
    store_email_id, store_latitude, store_longitude, store_date_of_opening, 
    store_type, store_opening_time, store_closing_time, store_manager_name, 
    store_manager_no, store_manager_email, store_location_link
) VALUES 
('RPT', 'WEST 1', 'LvL 2', 'Raipur - Rajendra Nagar', 
 'Near Highway Inn Opp. New Rajendra Nagar Thana, Vallabh Nagar, Civil Lines', 
 'Raipur', 'Chhattisgarh', 'India', '492001', 
 '077142 50000', '0', 'btqrpt@titan.co.in', 
 '21.23593', '81.63941', '9/1/2017', 'Stand-alone store', 
 '10:30:00', '20:30:00', 'Kunal Dutta', '6262035656', 
 'btqrpt@titan.co.in', 'https://bit.ly/44rsPLK')
ON DUPLICATE KEY UPDATE 
    region = VALUES(region), 
    level = VALUES(level),
    store_name = VALUES(store_name);

-- WEST 2 Region
INSERT INTO stores (
    store_code, region, level, store_name, store_address, store_city, store_state, 
    store_country, store_zip_code, store_phone_no_one, store_phone_no_two, 
    store_email_id, store_latitude, store_longitude, store_date_of_opening, 
    store_type, store_opening_time, store_closing_time, store_manager_name, 
    store_manager_no, store_manager_email, store_location_link
) VALUES 
('AUN', 'WEST 2', 'LvL 1', 'Pune - Aundh', 
 'I.T.I. Road, Aundh', 
 'Pune', 'Maharashtra', 'India', '411067', 
 '020 2558 5000', '020 2558 5002', 'btqaun@titan.co.in', 
 '18.55357', '73.80712', '26/2/2016', 'Stand-alone store', 
 '10:30:00', '19:30:00', 'Reena Nandha', '8446395778', 
 'reena1@titan.co.in', 'https://bit.ly/3wbB9CC')
ON DUPLICATE KEY UPDATE 
    region = VALUES(region), 
    level = VALUES(level),
    store_name = VALUES(store_name);

-- WEST 3 Region
INSERT INTO stores (
    store_code, region, level, store_name, store_address, store_city, store_state, 
    store_country, store_zip_code, store_phone_no_one, store_phone_no_two, 
    store_email_id, store_latitude, store_longitude, store_date_of_opening, 
    store_type, store_opening_time, store_closing_time, store_manager_name, 
    store_manager_no, store_manager_email, store_location_link
) VALUES 
('JAB', 'WEST 3', 'LvL 2', 'Jabalpur - Sadar', 
 '11G, Kings Way, Main Road, Sadar', 
 'Jabalpur', 'Madhya Pradesh', 'India', '482001', 
 '0761-2312866', '0761-2311667', 'btqjab@titan.co.in', 
 '23.1539', '79.94396', '20/9/2007', 'Stand-alone store', 
 '10:30:00', '21:00:00', 'Ms V M Yash Rao', '9039587220', 
 'btqjab@titan.co.in', 'https://bit.ly/3UbKxOw')
ON DUPLICATE KEY UPDATE 
    region = VALUES(region), 
    level = VALUES(level),
    store_name = VALUES(store_name);

-- Create user login accounts for each store
INSERT INTO users (username, password, role, name, email) VALUES
('PRA', 'Tanishq@123', 'Store', 'Delhi - Pithampura', 'btqpra@titan.co.in'),
('LKO', 'Tanishq@123', 'Store', 'Lucknow - Gomti Nagar', 'btqlko@titan.co.in'),
('YNR', 'Tanishq@123', 'Store', 'Yamuna Nagar - Jagadhari Road', 'btqynr@titan.co.in'),
('FRD', 'Tanishq@123', 'Store', 'Faridabad - Mathura Road', 'btqfrd@titan.co.in'),
('CRB', 'Tanishq@123', 'Store', 'Chennai - Cathedral Road', 'btqcrb@titan.co.in'),
('BGR', 'Tanishq@123', 'Store', 'Bangalore - Bannergatta Road', 'btqbgr@titan.co.in'),
('HNR', 'Tanishq@123', 'Store', 'Hyderabad - Himayat Nagar', 'btqhnr@titan.co.in'),
('CSB', 'Tanishq@123', 'Store', 'Kolkata - Camac Street', 'btqcsb@titan.co.in'),
('TPT', 'Tanishq@123', 'Store', 'Patna - Frazer Road', 'btqtpt@titan.co.in'),
('RPT', 'Tanishq@123', 'Store', 'Raipur - Rajendra Nagar', 'btqrpt@titan.co.in'),
('AUN', 'Tanishq@123', 'Store', 'Pune - Aundh', 'btqaun@titan.co.in'),
('JAB', 'Tanishq@123', 'Store', 'Jabalpur - Sadar', 'btqjab@titan.co.in')
ON DUPLICATE KEY UPDATE name = VALUES(name), email = VALUES(email);

-- Verification
SELECT 'Stores created successfully!' as status;
SELECT region, COUNT(*) as store_count FROM stores GROUP BY region ORDER BY region;
SELECT 'Total stores:' as info, COUNT(*) as total FROM stores;
