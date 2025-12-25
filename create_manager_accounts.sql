-- =====================================================
-- CREATE MANAGER ACCOUNTS (RBM, CEE, ABM)
-- =====================================================
-- This creates all manager login accounts
-- =====================================================

-- ============================================
-- RBM (Regional Business Manager) Accounts
-- ============================================
INSERT INTO users (username, password, role, name, email) VALUES
('EAST1', 'east1@123', 'RBM', 'Alok Ranjan', 'alokranjan@titan.co.in'),
('EAST2', 'east2@123', 'RBM', 'Moni Sengupta', 'monisengupta@titan.co.in'),
('NORTH1', 'north1@123', 'RBM', 'Munish Chawla', 'munishchawla@titan.co.in'),
('NORTH2', 'north2@123', 'RBM', 'Arun Kumar', 'arunkumar@titan.co.in'),
('NORTH3', 'north3@123', 'RBM', 'Ashish Tewari', 'ashishtewari@titan.co.in'),
('NORTH4', 'north4@123', 'RBM', 'Chandan Pareek', 'chandanpareek@titan.co.in'),
('SOUTH1', 'south1@123', 'RBM', 'Narasimhan YL', 'narasimhanyl@titan.co.in'),
('SOUTH2', 'south2@123', 'RBM', 'Arun Prasad T S', 'arunprasadts@titan.co.in'),
('SOUTH3', 'south3@123', 'RBM', 'Vasudeva Rao', 'vasudevarao@titan.co.in'),
('WEST1', 'west1@123', 'RBM', 'Abhishek Bajpai', 'abhishekbajpai@titan.co.in'),
('WEST2', 'west2@123', 'RBM', 'Bikramjit M', 'bikramjitm@titan.co.in'),
('WEST3', 'west3@123', 'RBM', 'Vishal Vyas', 'vishalvyas@titan.co.in')
ON DUPLICATE KEY UPDATE 
    password = VALUES(password),
    name = VALUES(name),
    email = VALUES(email);

-- ============================================
-- CEE (Customer Experience Executive) Accounts
-- ============================================
INSERT INTO users (username, password, role, name, email) VALUES
('EAST1-CEE-01', 'Tanishq@cee', 'CEE', 'Debjani Chatterjee', 'debjanic@titan.co.in'),
('EAST1-CEE-02', 'Tanishq@cee', 'CEE', 'Madhumita Sil', 'madhumitasil@titan.co.in'),
('EAST2-CEE-01', 'Tanishq@cee', 'CEE', 'Ravikant Verma', 'ravikv@titan.co.in'),
('NORTH1-CEE-01', 'Tanishq@cee', 'CEE', 'Nidhi Gupta', 'nidhigupta@titan.co.in'),
('NORTH2-CEE-01', 'Tanishq@cee', 'CEE', 'Dinkush Rana', 'dinkushrana@titan.co.in'),
('NORTH3-CEE-01', 'Tanishq@cee', 'CEE', 'Kamal Sharma', 'ksharma9@titan.co.in'),
('NORTH4-CEE-01', 'Tanishq@cee', 'CEE', 'Ravish Sharma', 'ravishsharma@titan.co.in'),
('SOUTH1-CEE-01', 'Tanishq@cee', 'CEE', 'Aarthy P A', 'aarthyp@titan.co.in'),
('SOUTH1-CEE-02', 'Tanishq@cee', 'CEE', 'Harshini Anbazhagan', 'harshinianbazhagan@titan.co.in'),
('SOUTH2-CEE-01', 'Tanishq@cee', 'CEE', 'Arun Dwarakanath', 'arund@titan.co.in'),
('SOUTH3-CEE-01', 'Tanishq@cee', 'CEE', 'V Spandana Reddy', 'spandanar@titan.co.in'),
('WEST1-CEE-01', 'Tanishq@cee', 'CEE', 'Hemant D Wadkar', 'hemant1@titan.co.in'),
('WEST1-CEE-02', 'Tanishq@cee', 'CEE', 'Priyanka Choudhury', 'priyankachoudhury@titan.co.in'),
('WEST2-CEE-01', 'Tanishq@cee', 'CEE', 'Tejal Pawar', 'tejalp@titan.co.in'),
('WEST3-CEE-01', 'Tanishq@cee', 'CEE', 'Abhijit Mukherjee', 'abhijitmk@titan.co.in'),
('WEST3-CEE-02', 'Tanishq@cee', 'CEE', 'Khushboo Bhojani', 'khushboobhojani@titan.co.in')
ON DUPLICATE KEY UPDATE 
    password = VALUES(password),
    name = VALUES(name),
    email = VALUES(email);

-- ============================================
-- ABM (Area Business Manager) Accounts - EAST
-- ============================================
INSERT INTO users (username, password, role, name, email) VALUES
('EAST1-ABM-01', 'Tanishq@amb', 'ABM', 'Abhishek Kar', 'abhishekkar@titan.co.in'),
('EAST1-ABM-02', 'Tanishq@amb', 'ABM', 'Aryandu Bharti Bundel', 'aryandubundel@titan.co.in'),
('EAST1-ABM-03', 'Tanishq@amb', 'ABM', 'Budhaditya Bhowmik', 'budhadityabhowmik@titan.co.in'),
('EAST1-ABM-04', 'Tanishq@amb', 'ABM', 'Koushik Banerjee', 'koushikbanerjee@titan.co.in'),
('EAST1-ABM-05', 'Tanishq@amb', 'ABM', 'Rinku Dhara', 'rinkudhara@titan.co.in'),
('EAST1-ABM-06', 'Tanishq@amb', 'ABM', 'Sagar Gera', 'sagargera@titan.co.in'),
('EAST1-ABM-07', 'Tanishq@amb', 'ABM', 'Jayant Taparia', 'jayanttaparia@titan.co.in'),
('EAST1-ABM-08', 'Tanishq@amb', 'ABM', 'Nisha Agarwal', 'nishaagarwal@titan.co.in'),
('EAST1-ABM-09', 'Tanishq@amb', 'ABM', 'Santosh Kumar Singh', 'santoshsingh@titan.co.in'),
('EAST1-ABM-10', 'Tanishq@amb', 'ABM', 'Tushar Jain', 'tusharjain@titan.co.in'),
('EAST2-ABM-01', 'Tanishq@amb', 'ABM', 'Anjani Kumar', 'anjanikumar@titan.co.in'),
('EAST2-ABM-02', 'Tanishq@amb', 'ABM', 'Asghar Ali', 'asgharali@titan.co.in'),
('EAST2-ABM-03', 'Tanishq@amb', 'ABM', 'Avisek Prasad', 'avisekprasad@titan.co.in'),
('EAST2-ABM-04', 'Tanishq@amb', 'ABM', 'Bandhubrata Banerjee', 'bandhubratabanerjee@titan.co.in'),
('EAST2-ABM-05', 'Tanishq@amb', 'ABM', 'Chitranjan Singh', 'chitranjansingh@titan.co.in'),
('EAST2-ABM-06', 'Tanishq@amb', 'ABM', 'Prityush Chandra', 'prityushchandra@titan.co.in'),
('EAST2-ABM-07', 'Tanishq@amb', 'ABM', 'Sagar Jadhav', 'sagarjadhav@titan.co.in'),
('EAST2-ABM-08', 'Tanishq@amb', 'ABM', 'Sagorika Hazra', 'sagorikahazra@titan.co.in'),
('EAST2-ABM-09', 'Tanishq@amb', 'ABM', 'Saurabh Sinha', 'saurabhsinha@titan.co.in')
ON DUPLICATE KEY UPDATE password = VALUES(password), name = VALUES(name), email = VALUES(email);

-- ============================================
-- ABM Accounts - NORTH
-- ============================================
INSERT INTO users (username, password, role, name, email) VALUES
('NORTH1-ABM-01', 'Tanishq@amb', 'ABM', 'Abhishek Babbar', 'abhishekbabbar@titan.co.in'),
('NORTH1-ABM-02', 'Tanishq@amb', 'ABM', 'Anishka Bansal', 'anishkabansal@titan.co.in'),
('NORTH1-ABM-03', 'Tanishq@amb', 'ABM', 'Bhawuk Attree', 'bhawukattree@titan.co.in'),
('NORTH1-ABM-04', 'Tanishq@amb', 'ABM', 'Kunwar Sohrab Dilbaghi', 'kunwardilbaghi@titan.co.in'),
('NORTH1-ABM-05', 'Tanishq@amb', 'ABM', 'Prashant Singh Tomar', 'prashanttomar@titan.co.in'),
('NORTH1-ABM-06', 'Tanishq@amb', 'ABM', 'Vipul Singh', 'vipulsingh@titan.co.in'),
('NORTH2-ABM-01', 'Tanishq@amb', 'ABM', 'Akshat Kapoor', 'akshatkapoor@titan.co.in'),
('NORTH2-ABM-02', 'Tanishq@amb', 'ABM', 'Arpit Dixit', 'arpitdixit@titan.co.in'),
('NORTH2-ABM-03', 'Tanishq@amb', 'ABM', 'Atul Sharma', 'atulsharma@titan.co.in'),
('NORTH2-ABM-04', 'Tanishq@amb', 'ABM', 'Hamendra Sharma', 'hamendrasharma@titan.co.in'),
('NORTH2-ABM-05', 'Tanishq@amb', 'ABM', 'Madhur Goyal', 'madhurgoyal@titan.co.in'),
('NORTH2-ABM-06', 'Tanishq@amb', 'ABM', 'Rameswar Singh', 'rameswarsingh@titan.co.in'),
('NORTH2-ABM-07', 'Tanishq@amb', 'ABM', 'Richa Chaturvedi', 'richachaturvedi@titan.co.in'),
('NORTH2-ABM-08', 'Tanishq@amb', 'ABM', 'Vartul Pandey', 'vartulpandey@titan.co.in'),
('NORTH3-ABM-01', 'Tanishq@amb', 'ABM', 'Ashwani Kumar', 'ashwanikumar@titan.co.in'),
('NORTH3-ABM-02', 'Tanishq@amb', 'ABM', 'Faizan Ahmed', 'faizanahmed@titan.co.in'),
('NORTH3-ABM-03', 'Tanishq@amb', 'ABM', 'Omraj Sharma', 'omrajsharma@titan.co.in'),
('NORTH3-ABM-04', 'Tanishq@amb', 'ABM', 'Onkar Bhasin', 'onkarbhasin@titan.co.in'),
('NORTH3-ABM-05', 'Tanishq@amb', 'ABM', 'Shaily Singh', 'shailysingh@titan.co.in'),
('NORTH4-ABM-01', 'Tanishq@amb', 'ABM', 'Ankit Rai', 'ankitrai@titan.co.in'),
('NORTH4-ABM-02', 'Tanishq@amb', 'ABM', 'Anu', 'anu@titan.co.in'),
('NORTH4-ABM-03', 'Tanishq@amb', 'ABM', 'Kapil Kashyap', 'kapilkashyap@titan.co.in'),
('NORTH4-ABM-04', 'Tanishq@amb', 'ABM', 'Ram Prasad', 'ramprasad@titan.co.in'),
('NORTH4-ABM-05', 'Tanishq@amb', 'ABM', 'Tushar Garcha', 'tushargarcha@titan.co.in')
ON DUPLICATE KEY UPDATE password = VALUES(password), name = VALUES(name), email = VALUES(email);

-- ============================================
-- ABM Accounts - SOUTH
-- ============================================
INSERT INTO users (username, password, role, name, email) VALUES
('SOUTH1-ABM-01', 'Tanishq@amb', 'ABM', 'Adithan Mohan', 'adithanmohan@titan.co.in'),
('SOUTH1-ABM-02', 'Tanishq@amb', 'ABM', 'Ignescis', 'ignescis@titan.co.in'),
('SOUTH1-ABM-03', 'Tanishq@amb', 'ABM', 'Jegan Ravi', 'jeganravi@titan.co.in'),
('SOUTH1-ABM-04', 'Tanishq@amb', 'ABM', 'Ram Gautham G', 'ramgautham@titan.co.in'),
('SOUTH1-ABM-05', 'Tanishq@amb', 'ABM', 'Rupesh M', 'rupeshm@titan.co.in'),
('SOUTH1-ABM-06', 'Tanishq@amb', 'ABM', 'Arun Joshua', 'arunjoshua@titan.co.in'),
('SOUTH1-ABM-07', 'Tanishq@amb', 'ABM', 'Govindarrajan', 'govindarrajan@titan.co.in'),
('SOUTH1-ABM-08', 'Tanishq@amb', 'ABM', 'Lokeshwaran M', 'lokeshwaranm@titan.co.in'),
('SOUTH1-ABM-09', 'Tanishq@amb', 'ABM', 'Rathish B', 'rathishb@titan.co.in'),
('SOUTH1-ABM-10', 'Tanishq@amb', 'ABM', 'Sivaranjani', 'sivaranjani@titan.co.in'),
('SOUTH2-ABM-01', 'Tanishq@amb', 'ABM', 'Antara Bhaduri', 'antarabhaduri@titan.co.in'),
('SOUTH2-ABM-02', 'Tanishq@amb', 'ABM', 'Arjun Mohandas', 'arjunmohandas@titan.co.in'),
('SOUTH2-ABM-03', 'Tanishq@amb', 'ABM', 'Bopanna Mc', 'bopannamc@titan.co.in'),
('SOUTH2-ABM-04', 'Tanishq@amb', 'ABM', 'Kailash Shrishail Angadi', 'kailashangadi@titan.co.in'),
('SOUTH2-ABM-05', 'Tanishq@amb', 'ABM', 'Lakshay Jain', 'lakshayjain@titan.co.in'),
('SOUTH2-ABM-06', 'Tanishq@amb', 'ABM', 'Sandesh Pai', 'sandeshpai@titan.co.in'),
('SOUTH2-ABM-07', 'Tanishq@amb', 'ABM', 'Snuhee Roy', 'snuheeroy@titan.co.in'),
('SOUTH3-ABM-01', 'Tanishq@amb', 'ABM', 'Ahmed Basha', 'ahmedbasha@titan.co.in'),
('SOUTH3-ABM-02', 'Tanishq@amb', 'ABM', 'Manohar M', 'manoharm@titan.co.in'),
('SOUTH3-ABM-03', 'Tanishq@amb', 'ABM', 'Nikita Warkar', 'nikitawarkar@titan.co.in'),
('SOUTH3-ABM-04', 'Tanishq@amb', 'ABM', 'Purna Chandra', 'purnachandra@titan.co.in'),
('SOUTH3-ABM-05', 'Tanishq@amb', 'ABM', 'Satyanarayana V', 'satyanarayanav@titan.co.in'),
('SOUTH3-ABM-06', 'Tanishq@amb', 'ABM', 'Srikanth V', 'srikanthv@titan.co.in'),
('SOUTH3-ABM-07', 'Tanishq@amb', 'ABM', 'Venkatesh K', 'venkateshk@titan.co.in')
ON DUPLICATE KEY UPDATE password = VALUES(password), name = VALUES(name), email = VALUES(email);

-- ============================================
-- ABM Accounts - WEST
-- ============================================
INSERT INTO users (username, password, role, name, email) VALUES
('WEST1-ABM-01', 'Tanishq@amb', 'ABM', 'Abhijit Mandal', 'abhijitmandal@titan.co.in'),
('WEST1-ABM-02', 'Tanishq@amb', 'ABM', 'Atul Rane', 'atulrane@titan.co.in'),
('WEST1-ABM-03', 'Tanishq@amb', 'ABM', 'Avinash Singh', 'avinashsingh@titan.co.in'),
('WEST1-ABM-04', 'Tanishq@amb', 'ABM', 'Mukesh Chawla', 'mukeshchawla@titan.co.in'),
('WEST1-ABM-05', 'Tanishq@amb', 'ABM', 'Navjeet Rana', 'navjeetrana@titan.co.in'),
('WEST1-ABM-06', 'Tanishq@amb', 'ABM', 'Clara Lobo', 'claralobo@titan.co.in'),
('WEST1-ABM-07', 'Tanishq@amb', 'ABM', 'Lakshay Singh', 'lakshaysingh@titan.co.in'),
('WEST1-ABM-08', 'Tanishq@amb', 'ABM', 'Mukesh Chawla 2', 'mukeshchawla2@titan.co.in'),
('WEST1-ABM-09', 'Tanishq@amb', 'ABM', 'Shakir Shaikh', 'shakirshaikh@titan.co.in'),
('WEST2-ABM-01', 'Tanishq@amb', 'ABM', 'Ashish Kumar Singh', 'ashishsingh@titan.co.in'),
('WEST2-ABM-02', 'Tanishq@amb', 'ABM', 'Chandan Gupta', 'chandangupta@titan.co.in'),
('WEST2-ABM-03', 'Tanishq@amb', 'ABM', 'Kunal Dhruv', 'kunaldhruv@titan.co.in'),
('WEST2-ABM-04', 'Tanishq@amb', 'ABM', 'Sushma P', 'sushmap@titan.co.in'),
('WEST2-ABM-05', 'Tanishq@amb', 'ABM', 'Vivekanand Prasad', 'vivekanandprasad@titan.co.in'),
('WEST3-ABM-01', 'Tanishq@amb', 'ABM', 'Charvee Agarwal', 'charveeagarwal@titan.co.in'),
('WEST3-ABM-02', 'Tanishq@amb', 'ABM', 'Gaurav Jain', 'gauravjain@titan.co.in'),
('WEST3-ABM-03', 'Tanishq@amb', 'ABM', 'Subramani Bharti', 'subramanibharti@titan.co.in'),
('WEST3-ABM-04', 'Tanishq@amb', 'ABM', 'Bhushit Hathi', 'bhushithathi@titan.co.in'),
('WEST3-ABM-05', 'Tanishq@amb', 'ABM', 'Manish Sarjare', 'manishsarjare@titan.co.in'),
('WEST3-ABM-06', 'Tanishq@amb', 'ABM', 'Sahil Vyas', 'sahilvyas@titan.co.in'),
('WEST3-ABM-07', 'Tanishq@amb', 'ABM', 'Suraj Pawar', 'surajpawar@titan.co.in')
ON DUPLICATE KEY UPDATE password = VALUES(password), name = VALUES(name), email = VALUES(email);

-- ============================================
-- Verification
-- ============================================
SELECT '=== MANAGER ACCOUNTS CREATED ===' as status;
SELECT '' as blank;
SELECT 'RBM Accounts:' as info, COUNT(*) as count FROM users WHERE role = 'RBM';
SELECT 'CEE Accounts:' as info, COUNT(*) as count FROM users WHERE role = 'CEE';
SELECT 'ABM Accounts:' as info, COUNT(*) as count FROM users WHERE role = 'ABM';
SELECT '' as blank;
SELECT 'Total Manager Accounts:' as info, COUNT(*) as count FROM users WHERE role IN ('RBM', 'CEE', 'ABM');
SELECT '' as blank;
SELECT 'Managers by Region:' as info;
SELECT 
    CASE 
        WHEN username LIKE 'EAST%' THEN 'EAST'
        WHEN username LIKE 'NORTH%' THEN 'NORTH'
        WHEN username LIKE 'SOUTH%' THEN 'SOUTH'
        WHEN username LIKE 'WEST%' THEN 'WEST'
        ELSE username
    END as region,
    role,
    COUNT(*) as count
FROM users 
WHERE role IN ('RBM', 'CEE', 'ABM')
GROUP BY region, role
ORDER BY region, role;
