export type SubjectMarks = Record<string, number>;

export interface Student {
  id: string;
  studentId: number;
  name: string;
  roll: number;
  className: string;
  section: string;
  fatherName: string;
  motherName: string;
  dateOfBirth: string;
  address: string;
  marks: SubjectMarks;
}

export enum Grade {
  APlus = 'A+',
  A = 'A',
  B = 'B',
  C = 'C',
  D = 'D',
  F = 'F',
}

export type AttendanceStatus = 'P' | 'A' | 'L' | 'F'; // Present, Absent, Late, Half-day

export type AttendanceRecord = {
  [date: string]: {
    [studentId: number]: AttendanceStatus;
  };
};

export interface Teacher {
    id: number;
    name: string;
    designation: string;
    subject: string;
    joiningDate: string;
    contact: string;
    email: string;
    imageUrl: string;
}

export interface Notice {
    id: number;
    title: string;
    date: string;
    content: string;
}

export type FeeFrequency = 'Monthly' | 'Yearly' | 'One-time';

export interface FeeType {
    id: number;
    name: string;
    amount: number;
    frequency: FeeFrequency;
}

export interface FeeItem {
    description: string;
    amount: number;
}

export type InvoiceStatus = 'Paid' | 'Unpaid' | 'Overdue';

export interface Invoice {
    id: number;
    studentId: number;
    studentName: string;
    className: string;
    roll: number;
    month: string;
    year: number;
    items: FeeItem[];
    totalAmount: number;
    amountPaid: number;
    dueDate: string;
    status: InvoiceStatus;
    months?: string[];
}

export interface MonthlyFeeStatus {
  [year: number]: {
    [studentId: number]: {
      [month: string]: boolean;
    };
  };
}

export interface Payment {
    student: Student;
    year: number;
    paidMonths: string[];
    otherFees: FeeItem[];
    totalAmount: number;
}
