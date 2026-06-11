package fa.training.librarymanagementsystem.service.impl;

import fa.training.librarymanagementsystem.dto.response.FineResponse;
import fa.training.librarymanagementsystem.dto.response.PageResponse;
import fa.training.librarymanagementsystem.entity.BorrowRecord;
import fa.training.librarymanagementsystem.entity.Fine;
import fa.training.librarymanagementsystem.exception.ResourceNotFoundException;
import fa.training.librarymanagementsystem.repository.FineRepository;
import fa.training.librarymanagementsystem.repository.specification.FineSpecification;
import fa.training.librarymanagementsystem.service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineService {

    private final FineRepository fineRepository;

    @Override
    @Transactional
    public void createFine(BorrowRecord record) {
        Fine fine = Fine.builder()
                .borrowRecord(record)
                .amount(record.getFineAmount())
                .status(Fine.FineStatus.UNPAID)
                .createdAt(LocalDate.now())
                .build();
        fineRepository.save(fine);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUnpaidFines(Long userId) {
        return fineRepository.existsByBorrowRecordUserIdAndStatus(userId, Fine.FineStatus.UNPAID);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FineResponse> getMyUnpaidFines(String username) {
        return fineRepository.findByUsernameAndStatus(username, Fine.FineStatus.UNPAID)
                .stream().map(FineResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FineResponse> getMyFineHistory(String username, Pageable pageable) {
        Page<Fine> page = fineRepository.findByUsername(username, pageable);
        return PageResponse.from(page, page.getContent().stream().map(FineResponse::from).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FineResponse> getAllFines(Long userId, Fine.FineStatus status, Pageable pageable) {
        Specification<Fine> spec = Specification
                .where(FineSpecification.hasUserId(userId))
                .and(FineSpecification.hasStatus(status));
        Page<Fine> page = fineRepository.findAll(spec, pageable);
        return PageResponse.from(page, page.getContent().stream().map(FineResponse::from).toList());
    }

    @Override
    @Transactional
    public FineResponse markAsPaid(Long borrowRecordId) {
        Fine fine = findByBorrowRecordIdOrThrow(borrowRecordId);
        if (fine.getStatus() != Fine.FineStatus.UNPAID) {
            throw new IllegalStateException("Fine is already " + fine.getStatus());
        }
        fine.setStatus(Fine.FineStatus.PAID);
        fine.setPaidAt(LocalDate.now());
        return FineResponse.from(fineRepository.save(fine));
    }

    @Override
    @Transactional
    public FineResponse waive(Long borrowRecordId) {
        Fine fine = findByBorrowRecordIdOrThrow(borrowRecordId);
        if (fine.getStatus() != Fine.FineStatus.UNPAID) {
            throw new IllegalStateException("Fine is already " + fine.getStatus());
        }
        fine.setStatus(Fine.FineStatus.WAIVED);
        return FineResponse.from(fineRepository.save(fine));
    }

    private Fine findByBorrowRecordIdOrThrow(Long borrowRecordId) {
        return fineRepository.findByBorrowRecordId(borrowRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found for borrow record: " + borrowRecordId));
    }
}
