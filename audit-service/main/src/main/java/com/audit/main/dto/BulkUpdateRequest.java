package main.java.com.audit.main.dto;

import com.hsgroup.audit.model.ComplianceResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/** Corps de POST /audits/{id}/subcategories/bulk */
@Data
public class BulkUpdateRequest {

    @NotEmpty(message = "La liste d'items ne peut pas être vide")
    @Valid
    private List<BulkItem> items;

    @Data
    public static class BulkItem {
        @NotBlank @Size(max = 20)
        private String subcategoryId;

        @NotNull
        private ComplianceResult result;

        @Size(max = 5000) private String evidence = "";
        @Size(max = 5000) private String auditorNotes = "";
        @Size(max = 5000) private String recommendation = "";
        private String status = "done";
    }
}