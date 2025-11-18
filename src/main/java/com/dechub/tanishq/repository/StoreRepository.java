package com.dechub.tanishq.repository;

import com.dechub.tanishq.dto.ExcelStoreDTO;
import com.dechub.tanishq.dto.eventsDto.*;
import java.util.List;
import java.util.Map;

public interface StoreRepository {
    // load all store_code -> password pairs
    Map<String, String> loadAllStorePasswords() throws Exception;

    // return password for a single store (or null)
    String getPasswordForStore(String storeCode) throws Exception;

    // return store metadata (same shape as current getDataFromSheet)
    Map<String, Object> getStoreDetails(String storeCode) throws Exception;

    // return all store DTOs (used by scheduled fetchData)
    List<ExcelStoreDTO> getAllStores() throws Exception;

    // convenience: stores by region
    List<storeCodeDataDTO> getStoresByRegion(String region) throws Exception;

    // RBM/ABM/CEE lookup
    List<String> getStoresByRbm(String rbmUsername) throws Exception;
    List<String> getStoresByAbm(String abmUsername) throws Exception;
    List<String> getStoresByCee(String ceeUsername) throws Exception;

    // change password (delegates to sheets for now)
    boolean changePassword(String storeCode, String oldPassword, String newPassword) throws Exception;
}
