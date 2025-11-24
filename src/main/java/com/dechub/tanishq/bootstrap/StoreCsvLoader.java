package com.dechub.tanishq.bootstrap;

import com.dechub.tanishq.entity.Store;
import com.dechub.tanishq.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class StoreCsvLoader {

    private static final Logger log = LoggerFactory.getLogger(StoreCsvLoader.class);

    @Value("${app.csv.stores-master}")
    private Resource masterCsv;

    @Value("${app.csv.stores-hierarchy}")
    private Resource hierarchyCsv;

    private final StoreRepository storeRepository;

    public StoreCsvLoader(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void loadStoresOnStartup() {
        try {
            long existing = storeRepository.count();
            if (existing > 0) {
                log.info("Stores already present in DB ({} rows). Skipping CSV import.", existing);
                return;
            }

            log.info("Starting CSV import for stores...");
            Map<String, Store> stores = loadMasterCsv();
            mergeHierarchyCsv(stores);

            storeRepository.saveAll(stores.values());
            log.info("Finished CSV import. Inserted {} stores.", stores.size());

        } catch (Exception e) {
            log.error("Error while importing store CSVs", e);
        }
    }

    /** 1) Load base store data from stores_master.csv */
    private Map<String, Store> loadMasterCsv() throws Exception {
        Map<String, Store> result = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(masterCsv.getInputStream(), StandardCharsets.UTF_8))) {

            String header = br.readLine(); // skip header
            log.info("stores_master header: {}", header);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] cols = line.split(",", -1); // keep empty columns
                // adjust indices to your CSV (Btq Code is col 0, Region col 1, Level col 2, etc.)
                String code = cols[0].trim();
                if (code.isEmpty()) continue;

                Store s = new Store();
                s.setStoreCode(code);
                s.setRegion(cols[1].trim());           // Region
                s.setLevel(cols[2].trim());            // Level
                s.setStoreName(cols[3].trim());        // Btq Name
                s.setStoreAddress(cols[4].trim());
                s.setStoreCity(cols[5].trim());
                s.setStoreState(cols[6].trim());
                s.setStoreCountry(cols[7].trim());
                s.setStoreZipCode(cols[8].trim());
                s.setStorePhoneNoOne(cols[9].trim());
                s.setStorePhoneNoTwo(cols[10].trim());
                s.setStoreEmailId(cols[11].trim());
                s.setStoreLatitude(cols[12].trim());
                s.setStoreLongitude(cols[13].trim());
                s.setStoreDateOfOpening(cols[14].trim());
                s.setStoreType(cols[15].trim());
                s.setStoreOpeningTime(cols[16].trim());
                s.setStoreClosingTime(cols[17].trim());
                s.setStoreManagerName(cols[18].trim());
                s.setStoreManagerNo(cols[19].trim());
                s.setStoreManagerEmail(cols[20].trim());
                s.setStoreLocationLink(cols[21].trim());
                s.setLanguages(cols[22].trim());
                s.setParking(cols[23].trim());
                s.setPayment(cols[24].trim());
                s.setKakatiyaStore(cols[25].trim());
                s.setCelesteStore(cols[26].trim());
                s.setRating(cols[27].trim());
                s.setNumberOfRatings(cols[28].trim());
                s.setIsCollection(cols[29].trim());

                result.put(code, s);
            }
        }

        log.info("Loaded {} rows from stores_master.csv", result.size());
        return result;
    }

    /** 2) Merge ABM/RBM/CEE info from stores_hierarchy.csv */
    private void mergeHierarchyCsv(Map<String, Store> stores) throws Exception {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(hierarchyCsv.getInputStream(), StandardCharsets.UTF_8))) {

            String header = br.readLine();
            log.info("stores_hierarchy header: {}", header);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] cols = line.split(",", -1);
                String code = cols[0].trim(); // store_code/Btq Code
                if (code.isEmpty()) continue;

                Store s = stores.get(code);
                if (s == null) continue; // code not in master, skip

                // adjust indices to your hierarchy CSV:
                // ... region is already set from master; here we mainly care about usernames
                String rbmUsername = cols[21].trim(); // rbm_user_name
                String ceeUsername = cols[24].trim(); // cee_user_name
                String abmUsername = cols[27].trim(); // abm_user_name

                s.setRbmUsername(rbmUsername);
                s.setCeeUsername(ceeUsername);
                s.setAbmUsername(abmUsername);
            }
        }
    }
}
