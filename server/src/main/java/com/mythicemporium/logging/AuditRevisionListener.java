package com.mythicemporium.logging;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        AuditRevisionEntity audit = (AuditRevisionEntity) revisionEntity;

        String username = "system";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated()) {
            username = auth.getName();
        }

        audit.setOperationType(AuditContextHolder.getContext().getOperationType());
        audit.setIpAddress(AuditContextHolder.getContext().getIpAddress());

        audit.setUsername(username);
    }
}
