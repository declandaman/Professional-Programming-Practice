import java.util.Scanner;


public class BorrowBookUi {
	
	public static enum UiState { INITIALISED, READY, RESTRICTED, SCANNING, IDENTIFIED, FINALISING, COMPLETED, CANCELLED };

	private BorrowBookControl control;
	private Scanner userInput;
	private UiState currentState;
	
	public BorrowBookUi(BorrowBookControl control) {
		this.control = control;
		userInput = new Scanner(System.in);
		currentState = UiState.INITIALISED;
		control.setUi(this);
	}
	
    
	private String UserInputPrompt(String prompt) {
		System.out.print(prompt);
		return userInput.nextLine();
	}	
		
    
	private void output(Object object) {
		System.out.println(object);
	}
		
    
	public void setState(UiState currentState) {
		this.currentState = currentState;
	}
	
    
	public void run() {
		output("Borrow Book Use Case UI\n");
		
		while (true) {
			
			switch (currentState) {			
			
			case CANCELLED:
				output("Borrowing Cancelled");
				return;
				
			case READY:
				String memStr = UserInputPrompt("Swipe member card (press <enter> to cancel): ");
                
				if (memStr.length() == 0) {
					control.cancel();
					break;
				}
                
				try {
					int memberId = Integer.valueOf(memStr).intValue();
					control.cardSwiped(memberId);
				}
                
				catch (NumberFormatException e) {
					output("Invalid Member Id");
				}
                
				break;
				
			case RESTRICTED:
				UserInputPrompt("Press <any key> to cancel");
				control.cancel();
				break;
				
			case SCANNING:
				String bookStr = UserInputPrompt("Scan Book (<enter> completes): ");
                
				if (bookStr.length() == 0) {
					control.Complete();
					break;
				}
                
				try {
					int bookId = Integer.valueOf(bookStr).intValue();
					control.Scanned(bookId);
				} 
                
                catch (NumberFormatException e) {
					output("Invalid Book Id");
				} 
                
				break;
				
			case FINALISING:
				String answer = UserInputPrompt("Commit loans? (Y/N): ");
                
				if (answer.toUpperCase().equals("N")) {
					control.cancel();
				} 
                
                else {
					control.commitLoans();
					UserInputPrompt("Press <any key> to complete ");
				}
                
				break;				
				
			case COMPLETED:
				output("Borrowing Completed");
				return;
				
			default:
				output("Unhandled state");
				throw new RuntimeException("BorrowBookUI : unhandled state :" + currentState);			
			}
		}		
	}

    
	public void DisplayMessage(Object message) {
		output(message);		
	}
}
