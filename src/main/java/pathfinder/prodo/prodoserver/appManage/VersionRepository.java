package pathfinder.prodo.prodoserver.appManage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {

    @Query(value = "select v.appVersion from version v where v.versionId = 1")
    Optional<Long> getCurrentVersion();
}
