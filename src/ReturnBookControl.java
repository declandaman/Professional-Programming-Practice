public class ReturnBookControl {
  
    private ReturnBookUi ui;
    
    private enum ControlState { INITIALISED, READY, INSPECTING };
    
    private ControlState state;
    
    private library library;
    
    private loan currentLoan;

    
    public ReturnBookControl() {
        this.library = library.INSTANCE();
        
        state = ControlState.INITIALISED;
    }
    
    public void bookScanned(int bookId) {
        if (!state.equals(ControlState.READY)) {
            throw new RuntimeException("ReturnBookControl: cannot call bookScanned except in READY state");
        }
        
        book currentBook = library.Book(bookId);
        
        if (currentBook == null) {
            ui.display("Invalid Book Id");
            return;
        }
        
        if (!currentBook.On_loan()) {
            ui.display("Book has not been borrowed");
            return;
        }
        
        currentLoan = library.getLoanByBookId(bookId);
        
        double overDueFine = 0.0;
        
        if (currentLoan.isOverDue()) {
            overDueFine = library.calculateOverDueFine(currentLoan);
        }
        
        ui.display("Inspecting");
        
        ui.display(currentBook.toString());
        
        ui.display(currentLoan.toString());
        
        if (currentLoan.isOverDue()) {
            String overDueFineMessage = String.format("\nOverdue fine : $%.2f", overDueFine);
            ui.display(overDueFineMessage);
        }
        
        ui.setState(ReturnBookUi.UiState.INSPECTING);
        
        state = ControlState.INSPECTING;
    }

	public void scanningComplete() {
		if (!state.equals(CONTROL_STATE.READY)) {
			throw new RuntimeException("ReturnBookControl: cannot call scanningComplete except in READY state");
		}	
		ui.setState(ReturnBookUI.UI_STATE.COMPLETED);		
	}


	public void dischargeLoan(boolean isDamaged) {
		if (!state.equals(CONTROL_STATE.INSPECTING)) {
			throw new RuntimeException("ReturnBookControl: cannot call dischargeLoan except in INSPECTING state");
		}	
		library.dischargeLoan(currentLoan, isDamaged);
		currentLoan = null;
		ui.setState(ReturnBookUI.UI_STATE.READY);
		state = CONTROL_STATE.READY;				
	}


}
