import java.util.Scanner;

public class ReturnBookUi {

    public static enum UiState { INITIALISED, READY, INSPECTING, COMPLETED };
    
    private ReturnBookControl control;
    
    private Scanner input;
    
    private UiState state;

    
    public ReturnBookUi(ReturnBookControl control) {
        this.control = control;
        input = new Scanner(System.in);
        
        state = UiState.INITIALISED;
        
        control.setUi(this);
    }
    
    
    private String getInput(String prompt) {
        System.out.print(prompt);
        return input.nextLine();
    }

    
    private void output(Object object) {
        System.out.println(object);
    }

    
    public void display(Object object) {
        output(object);
    }

    
    public void setState(UiState state) {
        this.state = state;
    }
    
    
    public void run() {
        output("Return Book Use Case UI\n");
        while (true) {
            switch (state) {
                case INITIALISED: {
                    break;
                }
                case READY: {
                    String bookIdString = getInput("Scan Book (<enter> completes): ");
                    if (bookIdString.length() == 0) {
                        control.scanningComplete();
                    } 
                    else {
                        try {
                            int bookId = Integer.valueOf(bookIdString).intValue();
                            control.bookScanned(bookId);
                        } 
                        catch (NumberFormatException error) {
                            output("Invalid Book Id");
                        }
                    }
                    break;
                }
                
                case INSPECTING: {
                    String answer = getInput("Is book damaged? (Y/N): ");
                    boolean isDamaged = false;
                    if (answer.toUpperCase().equals("Y")) {
                        isDamaged = true;
                    }
                    control.dischargeLoan(isDamaged);
                }

                case COMPLETED: {
                    output("Return processing complete");
                    return;
                }

                default: {
                    output("Unhandled state");
                    throw new RuntimeException("ReturnBookUI : unhandled state :" + state);
                }
            }
        }
    }
}