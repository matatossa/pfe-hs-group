package main.java.com.audit.main.repository;
 
import com.hsgroup.audit.entity.Audit;
import com.hsgroup.audit.entity.SubcategoryResult;
import com.hsgroup.audit.model.ComplianceResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface SubcategoryResultRepository extends JpaRepository<SubcategoryResult, UUID> {
 
    List<SubcategoryResult> findByAuditIdOrderBySubcategoryIdAsc(UUID auditId);
 
    Optional<SubcategoryResult> findByAuditIdAndSubcategoryId(UUID auditId, String subcategoryId);
 
    List<SubcategoryResult> findByAuditIdAndFunctionId(UUID auditId, String functionId);
 
    @Query("SELECT sc FROM SubcategoryResult sc WHERE sc.audit.id = :auditId " +
           "AND sc.result IN ('NON_COMPLIANT', 'PARTIALLY_COMPLIANT') " +
           "ORDER BY sc.functionId, sc.subcategoryId")
    List<SubcategoryResult> findActionItems(@Param("auditId") UUID auditId);
 
    @Query("SELECT COUNT(sc) FROM SubcategoryResult sc WHERE sc.audit.id = :auditId " +
           "AND sc.result != 'NOT_ASSESSED'")
    long countAssessed(@Param("auditId") UUID auditId);
}