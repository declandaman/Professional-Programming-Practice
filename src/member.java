import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")

public class Member implements Serializable {

    private String lastName;
    private String firstName;
    private String email;
    
    private int phoneNo;
    private int id;
    
    private double fines;

    private Map<Integer, loan> loans;

    
    public Member(String lastName, String firstName, String email, int phoneNo, int id) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.phoneNo = phoneNo;
        this.id = id;
        this.loans = new HashMap<>();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Member: ").append(id).append("\n")
            .append("Name: ").append(lastName).append(", ").append(fines).append("\n")
            .append("Email: ").append(email).append("\n")
            .append("Phone:").append(phoneNo)
            .append("\n")
            .append(String.format("Fines Owed : $%.2f", fines))
            .append("\n");

        for (loan loan : loans.values()) {
            sb.append(loan).append("\n");
        }
        
        return sb.toString();
        
    }

    
    public int getId() {
        return id;
    }

    
    public List<loan> getLoans() {
        return new ArrayList<loan>(loans.values());
    }

    
    public int getNumberOfCurrentLoans() {
        return loans.size();
    }

    
    public double getFinesOwed() {
        return fines;
    }

    
    public void takeOutLoan(loan loan) {
        if (!loans.containsKey(loan.getId())) {
            loans.put(loan.getId(), loan);
        } else {
            throw new RuntimeException("Duplicate loan added to member");
        }
    }

    
    public String getLastName() {
        return lastName;
    }

    
    public String getFirstName() {
        return firstName;
    }

    
    public void addFine(double fine) {
        fines += fine;
    }

    
    public double payFine(double amount) {
        if (amount < 0) {
            throw new RuntimeException("Member.payFine: amount must be positive");
        }
        
        double change = 0;
        
        if (amount > fines) {
            change = amount - fines;
            fines = 0;
        } else {
            fines -= amount;
        }
        
        return change;
        
    }

    
    public void dischargeLoan(loan loan) {
        if (loans.containsKey(loan.getId())) {
            loans.remove(loan.getId());
        } else {
            throw new RuntimeException("No such loan held by member");
        }
    }
}