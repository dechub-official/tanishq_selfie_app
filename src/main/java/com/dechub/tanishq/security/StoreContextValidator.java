package com.dechub.tanishq.security;

import com.dechub.tanishq.service.TanishqPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Security component to validate store access permissions
 * Prevents Account Takeover vulnerability by validating store context
 */
@Component
public class StoreContextValidator {

    private static final Logger log = LoggerFactory.getLogger(StoreContextValidator.class);

    @Autowired
    private TanishqPageService tanishqPageService;

    /**
     * Validate if the current authenticated user has access to the requested store
     * 
     * @param session HTTP session containing authenticated user info
     * @param requestedStoreCode Store code being accessed
     * @return true if access is authorized, false otherwise
     */
    public boolean validateStoreAccess(HttpSession session, String requestedStoreCode) {
        if (session == null || requestedStoreCode == null || requestedStoreCode.trim().isEmpty()) {
            log.warn("Invalid validation request - session or storeCode is null");
            return false;
        }

        // Get authenticated user info from session
        String authenticatedUser = (String) session.getAttribute("authenticatedUser");
        String userType = (String) session.getAttribute("userType");
        
        if (authenticatedUser == null || userType == null) {
            log.warn("No authenticated user found in session");
            return false;
        }

        try {
            Set<String> authorizedStores = getAuthorizedStores(authenticatedUser, userType);
            
            // Check if requested store is in authorized list
            boolean isAuthorized = authorizedStores.contains(requestedStoreCode.toUpperCase());
            
            if (!isAuthorized) {
                log.error("SECURITY ALERT: User '{}' (type: {}) attempted unauthorized access to store '{}'", 
                         authenticatedUser, userType, requestedStoreCode);
            }
            
            return isAuthorized;
            
        } catch (Exception e) {
            log.error("Error validating store access for user '{}': {}", authenticatedUser, e.getMessage());
            return false;
        }
    }

    /**
     * Validate if the user has access to any of the stores associated with an event
     */
    public boolean validateEventAccess(HttpSession session, String eventId) {
        if (session == null || eventId == null) {
            return false;
        }

        try {
            // Get the store code associated with this event
            String eventStoreCode = tanishqPageService.getStoreCodeForEvent(eventId);
            if (eventStoreCode == null) {
                log.warn("Could not find store for event: {}", eventId);
                return false;
            }
            
            return validateStoreAccess(session, eventStoreCode);
            
        } catch (Exception e) {
            log.error("Error validating event access: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get all authorized store codes for a user based on their type
     */
    private Set<String> getAuthorizedStores(String username, String userType) throws Exception {
        Set<String> stores = new HashSet<>();
        
        switch (userType.toUpperCase()) {
            case "STORE":
                // Store users can only access their own store
                stores.add(username.toUpperCase());
                break;
                
            case "ABM":
                List<String> abmStores = tanishqPageService.fetchStoresByAbm(username);
                stores.addAll(abmStores);
                break;
                
            case "RBM":
                List<String> rbmStores = tanishqPageService.fetchStoresByRbm(username);
                stores.addAll(rbmStores);
                break;
                
            case "CEE":
                List<String> ceeStores = tanishqPageService.fetchStoresByCee(username);
                stores.addAll(ceeStores);
                break;
                
            case "CORPORATE":
                List<String> corpStores = tanishqPageService.fetchStoresByCorporate(username);
                stores.addAll(corpStores);
                break;
                
            case "REGIONAL":
                // Regional managers have access to stores in their region
                // These are handled separately
                stores.add(username.toUpperCase());
                break;
                
            default:
                log.warn("Unknown user type: {}", userType);
                break;
        }
        
        return stores;
    }

    /**
     * Check if user is authenticated in current session
     */
    public boolean isAuthenticated(HttpSession session) {
        if (session == null) {
            return false;
        }
        
        String authenticatedUser = (String) session.getAttribute("authenticatedUser");
        return authenticatedUser != null && !authenticatedUser.trim().isEmpty();
    }

    /**
     * Get the authenticated username from session
     */
    public String getAuthenticatedUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute("authenticatedUser");
    }

    /**
     * Store authentication info in session
     */
    public void setAuthenticatedUser(HttpSession session, String username, String userType) {
        if (session != null) {
            session.setAttribute("authenticatedUser", username);
            session.setAttribute("userType", userType);
            session.setAttribute("loginTimestamp", System.currentTimeMillis());
            
            // Set session timeout to 30 minutes
            session.setMaxInactiveInterval(1800);

            log.info("User '{}' authenticated with type '{}'", username, userType);
        }
    }

    /**
     * Clear session authentication
     */
    public void clearAuthentication(HttpSession session) {
        if (session != null) {
            session.removeAttribute("authenticatedUser");
            session.removeAttribute("userType");
            session.removeAttribute("loginTimestamp");
            session.invalidate();
        }
    }

    /**
     * Validate multiple store codes at once (for bulk operations)
     */
    public boolean validateMultipleStoreAccess(HttpSession session, List<String> storeCodes) {
        if (session == null || storeCodes == null || storeCodes.isEmpty()) {
            return false;
        }

        // All stores must be authorized
        for (String storeCode : storeCodes) {
            if (!validateStoreAccess(session, storeCode)) {
                return false;
            }
        }
        
        return true;
    }
}

