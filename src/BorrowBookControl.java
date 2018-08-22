import java.util.ArrayList;
import java.util.List;

public class BorrowBookControl {
	
	private BorrowBookUi ui;
	
	private library library;
	private member member;
	private enum ControlState { INITIALISED, READY, RESTRICTED, SCANNING, IDENTIFIED, FINALISING, COMPLETED, CANCELLED };
	private ControlState currentState;
	
	private List<book> pendingBooks;
	private List<loan> completedList;
	private book book;
	
	public BorrowBookControl() {
		this.library = library.INSTANCE();
		currentState = ControlState.INITIALISED;
	}

    
	public void setUi(BorrowBookUi ui) {
		if (!currentState.equals(ControlState.INITIALISED)) {
			throw new RuntimeException("BorrowBookControl: cannot call setUi except in INITIALISED state");
        }
        
		this.ui = ui;
		ui.setState(BorrowBookUi.UiState.READY);
		currentState = ControlState.READY;		
	}
	
    
	public void cardSwiped(int memberId) {
		if (!currentState.equals(ControlState.READY)) {
			throw new RuntimeException("BorrowBookControl: cannot call cardSwiped except in READY state");
        }
        
		member = library.getMember(memberId);
        
		if (member == null) {
			ui.DisplayMessage("Invalid memberId");
			return;
		}
        
		if (library.memberCanBorrow(member)) {
			pendingBooks = new ArrayList<>();
			ui.setState(BorrowBookUi.UiState.SCANNING);
			currentState = ControlState.SCANNING; 
        }
        
		else 
		{
			ui.DisplayMessage("Member cannot borrow at this time");
			ui.setState(BorrowBookUi.UiState.RESTRICTED); 
        }
    }	
	
    
	public void Scanned(int bookId) {
		book = null;
        
		if (!currentState.equals(ControlState.SCANNING)) {
			throw new RuntimeException("BorrowBookControl: cannot call bookScanned except in SCANNING state");
		}	
        
		book = library.Book(bookId);
        
		if (book == null) {
			ui.DisplayMessage("Invalid bookId");
			return;
		}
        
		if (!book.Available()) {
			ui.DisplayMessage("Book cannot be borrowed");
			return;
		}
        
		pendingBooks.add(book);
        
		for (book book : pendingBooks) {
			ui.DisplayMessage(book.toString());
		}
        
		if (library.loansRemainingForMember(member) - pendingBooks.size() == 0) {
			ui.DisplayMessage("Loan limit reached");
			Complete();
		}
	}
	
    
	public void Complete() {
		if (pendingBooks.size() == 0) {
			cancel();
		}
        
		else {
			ui.DisplayMessage("\nFinal Borrowing List");
            
			for (book book : pendingBooks) {
				ui.DisplayMessage(book.toString());
			}
            
			completedList = new ArrayList<loan>();
			ui.setState(BorrowBookUi.UiState.FINALISING);
			currentState = ControlState.FINALISING;
		}
	}

    
	public void commitLoans() {
		if (!currentState.equals(ControlState.FINALISING)) {
			throw new RuntimeException("BorrowBookControl: cannot call commitLoans except in FINALISING state");
		}	
        
		for (book book : pendingBooks) {
			loan loan = library.issueLoan(book, member);
			completedList.add(loan);			
		}
        
		ui.DisplayMessage("Completed Loan Slip");
        
		for (loan loan : completedList) {
			ui.DisplayMessage(loan.toString());
		}
        
		ui.setState(BorrowBookUi.UiState.COMPLETED);
		currentState = ControlState.COMPLETED;
	}
	
    
	public void cancel() {
		ui.setState(BorrowBookUi.UiState.CANCELLED);
		currentState = ControlState.CANCELLED;
	}
}
