package com.authora.projectservice.Util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for accessing security context and extracting authentication information
 */
@Component
public class SecurityContextHelper {

    /**
     * Get the current JWT token from the security context
     * @return JWT token with "Bearer " prefix or null if not available
     */
    public String getCurrentAuthToken() {
        // Try to get the current request from RequestContextHolder
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader;
            }
        }

        // If token not found in request attributes, try to get from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            // Depending on your implementation, the token might be stored in credentials
            // This is just a placeholder - you need to implement based on your security setup
            Object credentials = auth.getCredentials();
            if (credentials != null && credentials instanceof String) {
                return "Bearer " + credentials.toString();
            }
        }

        return null;
    }

    /**
     * Get the current authenticated username (email)
     * @return the username or null if not authenticated
     */
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
    

}