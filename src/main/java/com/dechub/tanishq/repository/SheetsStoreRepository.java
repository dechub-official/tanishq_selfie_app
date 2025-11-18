package com.dechub.tanishq.repository;

import com.dechub.tanishq.gsheet.GSheetUserDetailsUtil;
import com.dechub.tanishq.dto.ExcelStoreDTO;
import com.dechub.tanishq.dto.eventsDto.*;
import com.dechub.tanishq.repository.StoreRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.xmlbeans.XmlOptions.safeGet;

@Repository
public class SheetsStoreRepository implements StoreRepository {

    private final GSheetUserDetailsUtil gSheet;

    public SheetsStoreRepository(GSheetUserDetailsUtil gSheet) {
        this.gSheet = gSheet;
    }

    @Override
    public Map<String, String> loadAllStorePasswords() throws Exception {
        return gSheet.loadAllStorePasswords();
    }

    @Override
    public String getPasswordForStore(String storeCode) throws Exception {
        return gSheet.getNewPassword(storeCode);
    }

    @Override
    public Map<String, Object> getStoreDetails(String storeCode) throws Exception {
        return gSheet.getDataFromSheet(storeCode);
    }

    @Override
    public List<ExcelStoreDTO> getAllStores() throws Exception {
        return gSheet.getData();
    }

    @Override
    public List<storeCodeDataDTO> getStoresByRegion(String region) throws Exception {
        return gSheet.getStoresByRegion(region);
    }

    @Override
    public List<String> getStoresByRbm(String rbmUsername) throws Exception {
        return gSheet.getStoresByRbmUsername(rbmUsername);
    }

    @Override
    public List<String> getStoresByAbm(String abmUsername) throws Exception {
        return gSheet.getStoresByAbmUsername(abmUsername);
    }

    @Override
    public List<String> getStoresByCee(String ceeUsername) throws Exception {
        return gSheet.getStoresByCeeUsername(ceeUsername);
    }

    @Override
    public boolean changePassword(String storeCode, String oldPassword, String newPassword) throws Exception {
        return gSheet.changePassword(storeCode, oldPassword, newPassword).isStatus();
    }

}
