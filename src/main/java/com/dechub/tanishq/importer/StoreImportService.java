package com.dechub.tanishq.importer;

import com.dechub.tanishq.dto.ExcelStoreDTO;
import com.dechub.tanishq.gsheet.GSheetUserDetailsUtil;
import com.dechub.tanishq.repository.mysql.JdbcStoreImportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreImportService {

    private final JdbcStoreImportRepository jdbcStoreImportRepository;
    private final GSheetUserDetailsUtil gSheet;  // ✅ FIXED

    public void importStores(String storeSheetId) {
        try {
            log.info("📥 Importing store data from Google Sheets...");

            List<ExcelStoreDTO> rows = gSheet.getData();  // ✅ USE EXISTING UTIL
            if (rows == null || rows.isEmpty()) {
                log.warn("⚠ No store rows found.");
                return;
            }

            List<Map<String, Object>> batch = new ArrayList<>();

            for (ExcelStoreDTO s : rows) {
                Map<String, Object> store = new HashMap<>();
                store.put("store_code", s.getStoreCode());
                store.put("store_name", s.getStoreName());
                store.put("store_address", s.getStoreAddress());
                store.put("city", s.getStoreCity());
                store.put("state", s.getStoreState());
                store.put("country", s.getStoreCountry());
                store.put("zipcode", s.getStoreZipCode());
                store.put("phone_1", s.getStorePhoneNoOne());
                store.put("phone_2", s.getStorePhoneNoTwo());
                store.put("email", s.getStoreEmailId());
                store.put("latitude", s.getStoreLatitude());
                store.put("longitude", s.getStoreLongitude());
                store.put("opening_date", s.getStoreDateOfOpening());
                store.put("store_type", s.getStoreType());
                store.put("opening_time", s.getStoreOpeningTime());
                store.put("closing_time", s.getStoreClosingTime());
                store.put("manager_name", s.getStoreManagerName());
                store.put("manager_phone", s.getStoreManagerNo());
                store.put("manager_email", s.getStoreManagerEmail());
                store.put("maps_link", s.getStoreLocationLink());
                store.put("languages", s.getLanguages());
                store.put("parking", s.getParking());
                store.put("payment", s.getPayment());
                batch.add(store);
            }

            log.info("📦 Prepared {} store records for import", batch.size());

            jdbcStoreImportRepository.clearStores();
            jdbcStoreImportRepository.batchInsertStores(batch);

            log.info("✅ Store import completed successfully.");

        } catch (Exception e) {
            log.error("❌ Store import failed", e);
        }
    }
}
