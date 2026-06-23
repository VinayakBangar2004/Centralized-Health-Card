package com.healthcard.backend.service;

import com.healthcard.backend.dto.request.CreateBillRequest;
import com.healthcard.backend.dto.request.MakePaymentRequest;
import com.healthcard.backend.dto.response.BillResponse;
import com.healthcard.backend.dto.response.PaymentResponse;
import com.healthcard.backend.entity.*;
import com.healthcard.backend.entity.enums.AuditAction;
import com.healthcard.backend.entity.enums.BillStatus;
import com.healthcard.backend.exception.ResourceNotFoundException;
import com.healthcard.backend.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final DoctorRepository doctorRepository;
    private final PathologistRepository pathologistRepository;
    private final PatientRepository patientRepository;
    private final PatientAccessService patientAccessService;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;

    @Transactional
    public BillResponse createBillByDoctor(Long doctorUserId, CreateBillRequest req, HttpServletRequest httpRequest) {
        Patient patient = patientAccessService.resolvePatientForDoctor(doctorUserId, req.healthCardNumber(), req.healthCardId(), httpRequest);
        Doctor doctor = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        Bill bill = Bill.builder()
                .patient(patient)
                .doctor(doctor)
                .description(req.description())
                .amount(req.amount())
                .status(BillStatus.PENDING)
                .build();
        bill = billRepository.save(bill);

        finalizeBillCreation(bill, doctor.getUser());
        return toResponse(bill);
    }

    @Transactional
    public BillResponse createBillByPathologist(Long pathologistUserId, Long patientId, String description, java.math.BigDecimal amount) {
        Pathologist pathologist = pathologistRepository.findByUserId(pathologistUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathologist profile not found"));

        Bill bill = Bill.builder()
                .patient(patientRepository.findById(patientId)
                        .orElseThrow(() -> new ResourceNotFoundException("Patient not found")))
                .pathologist(pathologist)
                .description(description)
                .amount(amount)
                .status(BillStatus.PENDING)
                .build();
        bill = billRepository.save(bill);

        finalizeBillCreation(bill, pathologist.getUser());
        return toResponse(bill);
    }

    private void finalizeBillCreation(Bill bill, User raisedBy) {
        auditLogRepository.save(AuditLog.builder()
                .actor(raisedBy)
                .targetPatient(bill.getPatient())
                .action(AuditAction.BILL_CREATED)
                .details("Bill raised: " + bill.getDescription() + " - ₹" + bill.getAmount())
                .build());

        notificationService.notifyUser(bill.getPatient().getUser(), "New bill",
                "A bill of ₹" + bill.getAmount() + " for \"" + bill.getDescription() + "\" has been raised.");
    }

    @Transactional
    public PaymentResponse payBill(Long patientUserId, MakePaymentRequest req) {
        Bill bill = billRepository.findById(req.billId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (!bill.getPatient().getUser().getId().equals(patientUserId)) {
            throw new ResourceNotFoundException("Bill not found");
        }
        if (bill.getStatus() == BillStatus.PAID) {
            throw new IllegalStateException("This bill has already been paid");
        }

        // Mock payment gateway settlement. Swap this block for a real Razorpay/Stripe
        // order-create + webhook-verify flow in production; the rest of the system
        // (bill/payment entities, notifications, audit log) stays unchanged.
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        Payment payment = Payment.builder()
                .bill(bill)
                .amountPaid(bill.getAmount())
                .paymentMethod(req.paymentMethod())
                .transactionId(transactionId)
                .build();
        payment = paymentRepository.save(payment);

        bill.setStatus(BillStatus.PAID);
        billRepository.save(bill);

        auditLogRepository.save(AuditLog.builder()
                .actor(bill.getPatient().getUser())
                .targetPatient(bill.getPatient())
                .action(AuditAction.BILL_PAID)
                .details("Bill #" + bill.getId() + " paid via " + req.paymentMethod())
                .build());

        return toPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getForPatient(Long patientId) {
        return billRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getForDoctor(Long doctorId) {
        return billRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getForPathologist(Long pathologistId) {
        return billRepository.findByPathologistIdOrderByCreatedAtDesc(pathologistId).stream()
                .map(this::toResponse).toList();
    }

    private BillResponse toResponse(Bill b) {
        String raisedBy = b.getDoctor() != null
                ? "Dr. " + b.getDoctor().getUser().getFullName()
                : (b.getPathologist() != null ? b.getPathologist().getLabName() : "System");

        return BillResponse.builder()
                .id(b.getId())
                .patientId(b.getPatient().getId())
                .patientName(b.getPatient().getUser().getFullName())
                .raisedBy(raisedBy)
                .description(b.getDescription())
                .amount(b.getAmount())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .build();
    }

    private PaymentResponse toPaymentResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .billId(p.getBill().getId())
                .amountPaid(p.getAmountPaid())
                .paymentMethod(p.getPaymentMethod())
                .transactionId(p.getTransactionId())
                .paidAt(p.getPaidAt())
                .build();
    }
}
