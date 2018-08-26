import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")

public class Library implements Serializable {

    private static final String libraryFile = "library.obj";
    
    private static final int loanLimit = 2;
    private static final int loanPeriod = 2;
    
    private static final double finePerDay = 1.0;
    private static final double maxFinesOwed = 5.0;
    private static final double damageFee = 2.0;

    private static Library self;
    
    private int bookId;
    private int memberId;
    private int loanId;
    
    private Date loadDate;

    private Map<Integer, book> catalog;
    private Map<Integer, Member> members;
    private Map<Integer, loan> loans;
    private Map<Integer, loan> currentLoans;
    private Map<Integer, book> damagedBooks;

    
    private Library() {
        catalog = new HashMap<>();
        members = new HashMap<>();
        loans = new HashMap<>();
        currentLoans = new HashMap<>();
        damagedBooks = new HashMap<>();
        bookId = 1;
        memberId = 1;
        loanId = 1;
    }

    
    public static synchronized Library INSTANCE() {
        if (self == null) {
            Path path = Paths.get(libraryFile);
            if (Files.exists(path)) {
                try (ObjectInputStream lof = new ObjectInputStream(new FileInputStream(libraryFile));) {
                    self = (Library) lof.readObject();
                    Calendar.getInstance().setDate(self.loadDate);
                    lof.close();
                } 
                
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } 
            
            else {
                self = new Library();
            }
        }
        
        return self;
        
    }

    
    public static synchronized void SAVE() {
        if (self != null) {
            self.loadDate = Calendar.getInstance().Date();
            try (ObjectOutputStream lof = new ObjectOutputStream(new FileOutputStream(libraryFile));) {
                lof.writeObject(self);
                lof.flush();
                lof.close();
            } 
            
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    
    public int BookId() {
        return bookId;
    }

    
    public int MemberId() {
        return memberId;
    }

    
    private int nextBookId() {
        return bookId++;
    }

    
    private int nextMemberId() {
        return memberId++;
    }

    
    private int nextLoanId() {
        return loanId++;
    }

    
    public List<Member> Members() {
        return new ArrayList<Member>(members.values());
    }

    
    public List<book> Books() {
        return new ArrayList<book>(catalog.values());
    }

    
    public List<loan> CurrentLoans() {
        return new ArrayList<loan>(currentLoans.values());
    }

    
    public Member Add_mem(String lastName, String firstName, String email, int phoneNo) {
        Member member = new Member(lastName, firstName, email, phoneNo, nextMemberId());
        members.put(member.getId(), member);
        return member;
    }

    
    public book Add_book(String a, String t, String c) {
        book b = new book(a, t, c, nextBookId());
        catalog.put(b.ID(), b);
        return b;
    }

    
    public Member getMember(int memberId) {
        if (members.containsKey(memberId)) {
            return members.get(memberId);
        }
        
        return null;
    }

    
    public book Book(int bookId) {
        if (catalog.containsKey(bookId)) {
            return catalog.get(bookId);
        }
        
        return null;
    }

    
    public int loanLimit() {
        return loanLimit;
    }

    
    public boolean memberCanBorrow(Member member) {
        if (member.getNumberOfCurrentLoans() == loanLimit) {
            return false;
        }

        
        if (member.getFinesOwed() >= maxFinesOwed) {
            return false;
        }

        
        for (loan loan : member.getLoans()) {
            if (loan.isOverDue()) {
                return false;
            }
        }

        return true;
        
    }

    
    public int loansRemainingForMember(Member member) {
        return loanLimit - member.getNumberOfCurrentLoans();
    }

    
    public loan issueLoan(book book, Member member) {
        Date dueDate = Calendar.getInstance().getDueDate(loanPeriod);
        
        loan loan = new loan(nextLoanId(), book, member, dueDate);
        
        member.takeOutLoan(loan);
        book.Borrow();
        
        loans.put(loan.getId(), loan);
        currentLoans.put(book.ID(), loan);
        
        return loan;
    }

    
    public loan getLoanByBookId(int bookId) {
        if (currentLoans.containsKey(bookId)) {
            return currentLoans.get(bookId);
        }
        
        return null;
    }

    
    public double calculateOverDueFine(loan loan) {
        if (loan.isOverDue()) {
            long daysOverDue = Calendar.getInstance().getDaysDifference(loan.getDueDate());
            double fine = daysOverDue * finePerDay;
            return fine;
        }
        
        return 0.0;
    }

    
    public void dischargeLoan(loan currentLoan, boolean isDamaged) {
        Member member = currentLoan.member();
        
        book book = currentLoan.Book();
        
        double overDueFine = calculateOverDueFine(currentLoan);
        
        member.addFine(overDueFine);
        member.dischargeLoan(currentLoan);
        
        book.Return(isDamaged);
        
        if (isDamaged) {
            member.addFine(damageFee);
            damagedBooks.put(book.ID(), book);
        }
        
        currentLoan.Loan();
        currentLoans.remove(book.ID());
        
    }

    
    public void checkCurrentLoans() {
        for (loan loan : currentLoans.values()) {
            loan.checkOverDue();
        }
    }

    
    public void repairBook(book currentBook) {
        if (damagedBooks.containsKey(currentBook.ID())) {
            currentBook.Repair();
            damagedBooks.remove(currentBook.ID());
        } else {
            throw new RuntimeException("Library: repairBook: book is not damaged");
        }
    }
}