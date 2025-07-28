package com.mythicemporium.logging;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditContext {
    private String operationType;
    private String ipAddress;
}
