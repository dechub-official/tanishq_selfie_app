package com.dechub.tanishq.entity;

import javax.persistence.*;

@Entity
@Table(name = "store_codes")       // ⬅ new table name
public class StoreLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "store_code")   // ⬅ maps to DB column store_code
    private String storeCode;      // ⬅ field renamed

    private String password;
    private String role; // ABM, RBM, CEE
    private String email;

    // getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStoreCode() { return storeCode; }
    public void setStoreCode(String storeCode) { this.storeCode = storeCode; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
