package com.mythicemporium.security;

import com.mythicemporium.dto.*;
import com.mythicemporium.model.Product;
import com.mythicemporium.model.ProductVariation;
import com.mythicemporium.service.ProductService;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class ProductPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if(auth == null || !auth.isAuthenticated() || targetDomainObject == null || !(permission instanceof String)) {
            return false;
        }

        String action = (String) permission;

        if(targetDomainObject instanceof ProductRequestDTO p) {
            return checkProductPermission(action);
        }

        if(targetDomainObject instanceof ProductVariationRequestDTO pv) {
            return checkProductVariationPermission(action);
        }

        if(targetDomainObject instanceof StockUpdateDTO s) {
            return checkProductVariationPermission(action);
        }

        if(targetDomainObject instanceof PriceUpdateDTO p) {
            return checkProductVariationPermission(action);
        }

        if(targetDomainObject instanceof BrandRequestDTO b) {
            return checkBrandPermission(action);
        }

        if(targetDomainObject instanceof CategoryRequestDTO c) {
            return checkCategoryPermission(action);
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if(auth == null || !auth.isAuthenticated() || targetId == null || permission == null) {
            return false;
        }

        String action = permission.toString();

        if("Product".equalsIgnoreCase(targetType)) {
            return checkProductPermission(action);
        }

        if("ProductVariation".equalsIgnoreCase(targetType)) {
            return checkProductVariationPermission(action);
        }

        if("Brand".equalsIgnoreCase(targetType)) {
            return checkBrandPermission(action);
        }

        if("Category".equalsIgnoreCase(targetType)) {
            return checkCategoryPermission(action);
        }

        return false;
    }

    private boolean checkProductPermission(String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return switch(action) {
            case "create", "update", "delete" -> isAdmin;
            default -> false;
        };
    }

    private boolean checkProductVariationPermission(String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return switch(action) {
            case "create", "update", "delete" -> isAdmin;
            default -> false;
        };
    }

    private boolean checkBrandPermission(String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return switch(action) {
            case "create", "update", "delete" -> isAdmin;
            default -> false;
        };
    }

    private boolean checkCategoryPermission(String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return switch(action) {
            case "create", "update", "delete" -> isAdmin;
            default -> false;
        };
    }
}
