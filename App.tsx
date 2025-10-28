
import React, { useState, useEffect } from 'react';
// FIX: Added AttendanceStatus to the import list and extensions to all imports.
import { Student, AttendanceRecord, Teacher, Notice, Invoice, FeeType, MonthlyFeeStatus, InvoiceStatus, FeeItem, Payment, AttendanceStatus } from './types.ts';
import { CLASS_CONFIGS, MONTHS } from './constants.ts';

// Data
import { initialStudents } from './data/initialStudents.ts';
import { initialTeachers } from './data/initialTeachers.ts';
import { initialNotices } from './data/initialNotices.ts';
import { initialInvoices } from './data/initialFees.ts';
import { initialFeeTypes } from './data/initialFeeTypes.ts';

// Components
import Header from './components/Header.tsx';
import Dashboard from './components/Dashboard.tsx';
import StudentForm from './components/StudentForm.tsx';
import ResultsTable from './components/ResultsTable.tsx';
import ResultCard from './components/ResultCard.tsx';
import SearchPage from './components/SearchPage.tsx';
import AttendancePage from './components/AttendancePage.tsx';
import StudentsPage from './components/StudentsPage.tsx';
import StudentPortalPage from './components/StudentPortalPage.tsx';
import TeachersPage from './components/TeachersPage.tsx';
import NoticeBoard from './components/NoticeBoard.tsx';
import FeesPage from './components/FeesPage.tsx';
import FeeLedgerPage from './components/FeeLedgerPage.tsx';
import PaymentReceiptPage from './components/PaymentReceiptPage.tsx';

type Page = 'DASHBOARD' | 'STUDENTS' | 'RESULTS' | 'SEARCH' | 'ATTENDANCE' | 'FEES' | 'FEE_LEDGER' | 'PAYMENT_RECEIPT' | 'TEACHERS' | 'NOTICES' | 'ADD_STUDENT';

// Helper to create initial fee status from invoices
const createInitialFeeStatus = (invoices: Invoice[]): MonthlyFeeStatus => {
  const status: MonthlyFeeStatus = {};
  (invoices || []).forEach(invoice => {
    if (invoice.status === 'Paid') {
        if (!status[invoice.year]) status[invoice.year] = {};
        if (!status[invoice.year][invoice.studentId]) status[invoice.year][invoice.studentId] = {};
        
        const monthsToProcess = invoice.months || invoice.month.split(', ');
        monthsToProcess.forEach(month => {
             if(month) status[invoice.year][invoice.studentId][month.trim()] = true;
        })
    }
  });
  return status;
};

const useStickyState = (defaultValue: any, key: string) => {
    const [value, setValue] = useState(() => {
        const initialDefault = typeof defaultValue === 'function' ? defaultValue() : defaultValue;
        try {
            const stickyValue = window.localStorage.getItem(key);
            if (stickyValue !== null) {
                const parsed = JSON.parse(stickyValue);
                // If localStorage has `null` but the default is not `null` (e.g., an array),
                // it's likely corrupt data. Revert to the default to prevent crashes.
                if (parsed === null && initialDefault !== null) {
                    return initialDefault;
                }
                return parsed;
            }
        } catch (error) {
            console.warn(`Error reading localStorage key “${key}”:`, error);
        }
        return initialDefault;
    });

    useEffect(() => {
        window.localStorage.setItem(key, JSON.stringify(value));
    }, [key, value]);

    return [value, setValue];
};

const App: React.FC = () => {
    const [page, setPage] = useState<Page>('DASHBOARD');
    const [students, setStudents] = useStickyState(initialStudents, 'students');
    const [teachers, setTeachers] = useStickyState(initialTeachers, 'teachers');
    const [notices, setNotices] = useStickyState(initialNotices, 'notices');
    const [invoices, setInvoices] = useStickyState(initialInvoices, 'invoices');
    const [feeTypes, setFeeTypes] = useStickyState(initialFeeTypes, 'feeTypes');
    const [attendanceRecords, setAttendanceRecords] = useStickyState({}, 'attendanceRecords');
    const [feeStatus, setFeeStatus] = useStickyState(() => createInitialFeeStatus(invoices), 'feeStatus');
    
    const [studentToEdit, setStudentToEdit] = useState<Student | null>(null);
    const [studentToView, setStudentToView] = useState<Student | null>(null);
    const [studentForPortal, setStudentForPortal] = useState<Student | null>(null);

    // This effect ensures that the fee status is correctly synced when invoices change.
    useEffect(() => {
        setFeeStatus(createInitialFeeStatus(invoices));
    }, [invoices, setFeeStatus]);

    // Handlers
    const handleSaveStudent = (studentData: Student) => {
        const existingIndex = (students || []).findIndex((s: Student) => s.id === studentData.id);
        if (existingIndex > -1) {
            const updatedStudents = [...(students || [])];
            updatedStudents[existingIndex] = studentData;
            setStudents(updatedStudents);
        } else {
            setStudents([...(students || []), studentData]);
        }
        setStudentToEdit(null);
        setPage('STUDENTS');
    };

    const handleEditStudent = (student: Student) => {
        setStudentToEdit(student);
        setPage('ADD_STUDENT');
    };

    const handleViewResult = (student: Student) => {
        setStudentToView(student);
    };

    const handleViewPortal = (student: Student) => {
        setStudentForPortal(student);
    };

    // FIX: Changed records type from InvoiceStatus to AttendanceStatus to match component props.
    const handleSaveAttendance = (date: string, records: { [studentId: number]: AttendanceStatus }) => {
        setAttendanceRecords({ ...attendanceRecords, [date]: records });
    };

    const handleAddTeacher = (teacher: Omit<Teacher, 'id' | 'imageUrl'>) => {
        const newTeacher: Teacher = {
            ...teacher,
            id: Date.now(),
            imageUrl: `https://via.placeholder.com/150/808080/FFFFFF?text=${teacher.name.split(' ').map(n => n[0]).join('')}`
        };
        setTeachers([...(teachers || []), newTeacher]);
    };

    const handleAddNotice = (notice: Omit<Notice, 'id'>) => {
        const newNotice: Notice = { ...notice, id: Date.now() };
        setNotices([...(notices || []), newNotice]);
    };
    
    const handleUpdateInvoiceStatus = (invoiceId: number, newStatus: InvoiceStatus) => {
        const newInvoices = (invoices || []).map((inv: Invoice) => inv.id === invoiceId ? { ...inv, status: newStatus } : inv);
        setInvoices(newInvoices);
    };

    const handleDeleteInvoice = (invoiceId: number) => {
        setInvoices((invoices || []).filter((inv: Invoice) => inv.id !== invoiceId));
    };

    const handleAddInvoice = (invoice: Omit<Invoice, 'id'>) => {
        const newInvoice: Invoice = { ...invoice, id: Date.now() };
        setInvoices([...(invoices || []), newInvoice]);
    };
    
    const handleAddFeeType = (feeType: Omit<FeeType, 'id'>) => {
        setFeeTypes([...(feeTypes || []), { ...feeType, id: Date.now() }]);
    };

    const handleUpdateFeeType = (feeType: FeeType) => {
        setFeeTypes((feeTypes || []).map((ft: FeeType) => ft.id === feeType.id ? feeType : ft));
    };

    const handleDeleteFeeType = (feeTypeId: number) => {
        const feeTypeToDelete = (feeTypes || []).find((ft: FeeType) => ft.id === feeTypeId);
        if (!feeTypeToDelete) return;

        // Protect the essential 'Tuition Fee' from being deleted.
        if (feeTypeToDelete.name === 'Tuition Fee') {
            alert("'টিউশন ফি' মোছা যাবে না কারণ এটি একটি অপরিহার্য ফি।");
            return;
        }
        
        // Confirm with the user, explaining the consequence.
        if (window.confirm(`আপনি কি "${feeTypeToDelete.name}" ফি-টি নিশ্চিতভাবে মুছে ফেলতে চান? এটি পুরনো ইনভয়েস থেকে মুছে যাবে না, তবে নতুন করে আর যোগ করা যাবে না।`)) {
             setFeeTypes((feeTypes || []).filter((ft: FeeType) => ft.id !== feeTypeId));
        }
    };

    const handleSaveFeeLedger = (updatedStatus: MonthlyFeeStatus) => {
        setFeeStatus(updatedStatus);
    };

    const handleProcessPayment = (payment: Payment) => {
        const tuitionFeeAmount = CLASS_CONFIGS[payment.student.className]?.tuitionFee || 0;
        const items: FeeItem[] = [
            ...payment.paidMonths.map(month => ({
                description: `Tuition Fee (${month})`,
                amount: tuitionFeeAmount
            })),
            ...payment.otherFees
        ];

        const newInvoice: Invoice = {
            id: Date.now(),
            studentId: payment.student.studentId,
            studentName: payment.student.name,
            className: payment.student.className,
            roll: payment.student.roll,
            month: payment.paidMonths.join(', '),
            year: payment.year,
            items: items,
            totalAmount: payment.totalAmount,
            amountPaid: payment.totalAmount,
            dueDate: new Date().toISOString().split('T')[0],
            status: 'Paid',
            months: payment.paidMonths
        };

        setInvoices([...(invoices || []), newInvoice]);
    };

    const renderPage = () => {
        if (studentToView) {
            return <ResultCard student={studentToView} onBack={() => setStudentToView(null)} />;
        }
        if (studentForPortal) {
            return <StudentPortalPage 
                student={studentForPortal} 
                attendanceRecords={attendanceRecords}
                feeStatus={feeStatus}
                invoices={invoices}
                onBack={() => setStudentForPortal(null)} 
                onUpdateInvoiceStatus={handleUpdateInvoiceStatus}
                onDeleteInvoice={handleDeleteInvoice}
            />;
        }

        switch (page) {
            case 'DASHBOARD':
                return <Dashboard students={students || []} notices={notices || []} teachers={teachers || []} />;
            case 'STUDENTS':
                return <StudentsPage students={students || []} onEdit={handleEditStudent} onPortal={handleViewPortal} />;
            case 'ADD_STUDENT':
                return <div className="bg-white dark:bg-gray-800 p-8 rounded-lg shadow-lg"><StudentForm onSave={handleSaveStudent} studentToEdit={studentToEdit} /></div>;
            case 'RESULTS':
                return <ResultsTable students={students || []} onView={handleViewResult} onEdit={handleEditStudent} onPortal={handleViewPortal} />;
            case 'SEARCH':
                return <SearchPage students={students || []} onView={handleViewPortal} />;
            case 'ATTENDANCE':
                return <AttendancePage students={students || []} attendanceRecords={attendanceRecords} onSave={handleSaveAttendance} />;
            case 'FEES':
                return <FeesPage 
                            students={students || []} 
                            invoices={invoices || []} 
                            feeTypes={feeTypes || []} 
                            onUpdateInvoiceStatus={handleUpdateInvoiceStatus}
                            onDeleteInvoice={handleDeleteInvoice}
                            onAddFeeType={handleAddFeeType}
                            onUpdateFeeType={handleUpdateFeeType}
                            onDeleteFeeType={handleDeleteFeeType}
                            onAddInvoice={handleAddInvoice}
                        />;
            case 'FEE_LEDGER':
                return <FeeLedgerPage students={students || []} feeStatus={feeStatus} onSaveAll={handleSaveFeeLedger} />;
            case 'PAYMENT_RECEIPT':
                return <PaymentReceiptPage students={students || []} feeTypes={feeTypes || []} onProcessPayment={handleProcessPayment} monthlyFeeStatus={feeStatus} />;
            case 'TEACHERS':
                return <TeachersPage teachers={teachers || []} onAddTeacher={handleAddTeacher} />;
            case 'NOTICES':
                return <NoticeBoard notices={notices || []} onAddNotice={handleAddNotice} />;
            default:
                return <Dashboard students={students || []} notices={notices || []} teachers={teachers || []} />;
        }
    };

    return (
        <div className="bg-gray-100 dark:bg-gray-900 text-gray-900 dark:text-gray-100 min-h-screen font-sans">
            <Header setPage={(p: any) => {
                setStudentToView(null);
                setStudentForPortal(null);
                setStudentToEdit(null);
                setPage(p as Page);
            }} />
            <main className="p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto">
                {renderPage()}
            </main>
        </div>
    );
};

export default App;
