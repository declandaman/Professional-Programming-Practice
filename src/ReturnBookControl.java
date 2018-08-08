public class ReturnBookControl {

    private ReturnBookUi ui;
    
    private enum ControlState { INITIALISED, READY, INSPECTING };
    
    private ControlState state;
    
    private Library library;
    
    private Loan currentLoan;

    
    public ReturnBookControl() {
        this.library = Library.INSTANCE();
        
        state = ControlState.INITIALISED;
    }

    
    public void setUi(ReturnBookUi ui) {
        if (!state.equals(ControlState.INITIALISED)) {
            throw new RuntimeException("ReturnBookControl: cannot call setUI except in INITIALISED state");
        }
        this.ui = ui;
        
        ui.setState(ReturnBookUi.UiState.READY);
        
        state = ControlState.READY;
    }

    
    public void bookScanned(int bookId) {
        if (!state.equals(ControlState.READY)) {
            throw new RuntimeException("ReturnBookControl: cannot call bookScanned except in READY state");
        }
        
        Book currentBook = library.book(bookId);
        
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
        if (!state.equals(ControlState.READY)) {
            throw new RuntimeException("ReturnBookControl: cannot call scanningComplete except in READY state");
        }
        
        ui.setState(ReturnBookUi.UiState.COMPLETED);
    }

    
    public void dischargeLoan(boolean isDamaged) {
        if (!state.equals(ControlState.INSPECTING)) {
            throw new RuntimeException("ReturnBookControl: cannot call dischargeLoan except in INSPECTING state");
        }
        
        library.dischargeLoan(currentLoan, isDamaged);
        
        currentLoan = null;
        
        ui.setState(ReturnBookUi.UiState.READY);
        
        state = ControlState.READY;
    }
}