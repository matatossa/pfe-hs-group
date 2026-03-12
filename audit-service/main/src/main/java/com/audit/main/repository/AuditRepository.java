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
interface AuditRepository extends JpaRepository<Audit, UUID> {
 
    @Query("SELECT a FROM Audit a WHERE " +
           "(:org IS NULL OR LOWER(a.organization) LIKE LOWER(CONCAT('%', :org, '%'))) AND " +
           "(:status IS NULL OR a.status = :status) ORDER BY a.createdAt DESC")
    List<Audit> findWithFilters(@Param("org") String org, @Param("status") String status);
 
    List<Audit> findAllByOrderByCreatedAtDesc();
 
    long countByStatus(String status);
}
 