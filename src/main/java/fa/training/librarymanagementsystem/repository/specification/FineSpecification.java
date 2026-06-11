package fa.training.librarymanagementsystem.repository.specification;

import fa.training.librarymanagementsystem.entity.Fine;
import org.springframework.data.jpa.domain.Specification;

public class FineSpecification {

    public static Specification<Fine> hasUserId(Long userId) {
        return (root, query, cb) -> userId == null ? null
                : cb.equal(root.get("borrowRecord").get("user").get("id"), userId);
    }

    public static Specification<Fine> hasStatus(Fine.FineStatus status) {
        return (root, query, cb) -> status == null ? null
                : cb.equal(root.get("status"), status);
    }
}
