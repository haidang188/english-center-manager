package com.englishcentermanager.entity;

public class enums
{
    public enum UserStatus{
        ACTIVE,
        INACTIVE
    }
    public enum ClassStatus{
        PLANNED,
        OPEN,
        ONGOING,
        COMPLETED,
        CANCELLED
    }

    public enum StudentClassStatus{
        STUDYING,
        WAITING_TRANSFER,
        COMPLETED,
        DROPPED,
        SUSPENDED
    }

    public enum TuitionStatus{
        UNPAID,
        PAID,
        OVERDUE
    }

    public enum PaymentMethod{
        CASH,
        BANK_TRANSFER,
        MOMO,
        OTHER
    }
}
