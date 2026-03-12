package com.hsgroup.audit.dto.request;
 
import jakarta.validation.constraints.*;
import lombok.Data;
 
/** Corps de POST /audits */
@Data
public class CreateAuditRequest {
 
    @NotBlank(message = "Le nom de l'organisation est obligatoire")
    @Size(min = 2, max = 255)
    private String organization;
 
    @Size(max = 255)
    private String auditor = "";
 
    @Size(max = 20)
    private String auditDate = "";
 
    @Size(max = 2000)
    private String scope = "";
}
 