package com.dechub.tanishq.repository.mysql;

import com.dechub.tanishq.dto.ExcelStoreDTO;
import com.dechub.tanishq.dto.eventsDto.storeCodeDataDTO;
import com.dechub.tanishq.repository.StoreRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Profile("mysql")
@Primary
public class JdbcStoreRepository implements StoreRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcStoreRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Map<String, String> loadAllStorePasswords() throws Exception {
        String sql = "SELECT store_code, new_password FROM store_passwords";
        List<Map<String,Object>> rows = jdbc.queryForList(sql, Collections.emptyMap());
        Map<String,String> map = new HashMap<>();
        for (Map<String,Object> r : rows) {
            String code = String.valueOf(r.get("store_code")).trim().toUpperCase();
            String pw = r.get("new_password") == null ? "" : String.valueOf(r.get("new_password"));
            map.put(code, pw);
        }
        return map;
    }

    @Override
    public String getPasswordForStore(String storeCode) throws Exception {
        String sql = "SELECT new_password FROM store_passwords WHERE store_code = :sc LIMIT 1";
        Map<String,Object> p = Collections.singletonMap("sc", storeCode);
        List<String> list = jdbc.query(sql, p, (rs, i) -> rs.getString("new_password"));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Map<String, Object> getStoreDetails(String storeCode) throws Exception {
        String sql = "SELECT * FROM stores WHERE store_code = :sc LIMIT 1";
        Map<String,Object> p = Collections.singletonMap("sc", storeCode);
        List<Map<String,Object>> rows = jdbc.queryForList(sql, p);
        return rows.isEmpty() ? Collections.emptyMap() : rows.get(0);
    }

    @Override
    public List<ExcelStoreDTO> getAllStores() throws Exception {
        String sql = "SELECT store_code, store_name, address, city, state, country, zipcode FROM stores";
        return jdbc.query(sql, Collections.emptyMap(), (rs, i) -> {
            ExcelStoreDTO e = new ExcelStoreDTO();
            e.setStoreCode(rs.getString("store_code"));
            e.setStoreName(rs.getString("store_name"));
            e.setStoreAddress(rs.getString("address"));
            e.setStoreCity(rs.getString("city"));
            e.setStoreState(rs.getString("state"));
            e.setStoreCountry(rs.getString("country"));
            e.setStoreZipCode(rs.getString("zipcode"));
            return e;
        });
    }

    @Override
    public List<storeCodeDataDTO> getStoresByRegion(String region) throws Exception {
        String sql = "SELECT store_code FROM stores WHERE region = :region";
        Map<String,Object> p = Collections.singletonMap("region", region);
        return jdbc.query(sql, p, (rs, i) -> {
            storeCodeDataDTO dto = new storeCodeDataDTO();
            dto.setStoreCode(rs.getString("store_code"));
            return dto;
        });
    }

    @Override
    public List<String> getStoresByRbm(String rbmUsername) throws Exception {
        // If you have RBM stored in stores table add column rbm_username to stores; otherwise maintain a mapping table.
        String sql = "SELECT store_code FROM stores WHERE rbm_username = :r";
        Map<String,Object> p = Collections.singletonMap("r", rbmUsername);
        return jdbc.query(sql, p, (rs, i) -> rs.getString("store_code"));
    }

    @Override
    public List<String> getStoresByAbm(String abmUsername) throws Exception {
        String sql = "SELECT store_code FROM stores WHERE abm_username = :r";
        Map<String,Object> p = Collections.singletonMap("r", abmUsername);
        return jdbc.query(sql, p, (rs, i) -> rs.getString("store_code"));
    }

    @Override
    public List<String> getStoresByCee(String ceeUsername) throws Exception {
        String sql = "SELECT store_code FROM stores WHERE cee_username = :r";
        Map<String,Object> p = Collections.singletonMap("r", ceeUsername);
        return jdbc.query(sql, p, (rs, i) -> rs.getString("store_code"));
    }

    @Override
    public boolean changePassword(String storeCode, String oldPassword, String newPassword) throws Exception {
        String current = getPasswordForStore(storeCode);
        if (current == null || !current.equals(oldPassword)) return false;
        String sql = "UPDATE store_passwords SET old_password = :oldPw, new_password = :newPw, updated_at = :ts WHERE store_code = :sc";
        Map<String,Object> p = new HashMap<>();
        p.put("oldPw", oldPassword);
        p.put("newPw", newPassword);
        p.put("ts", new java.sql.Timestamp(System.currentTimeMillis()));
        p.put("sc", storeCode);
        int u = jdbc.update(sql, p);
        return u > 0;
    }
}
