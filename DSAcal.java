import java.util.*;

public class DSAcal{

    static class InvalidExpressionException extends Exception {
        public InvalidExpressionException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to the Unified Calculator!");

        // Main continuous loop for the entire program.
        while (true) {
            List<Object> tokens = null;
            List<Double> evenNumbers = new ArrayList<>();
            List<Double> oddNumbers = new ArrayList<>();

            // Loop until a valid expression is entered.
            while (tokens == null) {
                try {
                    System.out.print("\nEnter a mathematical expression (or type 'exit' to quit): ");
                    String expr = sc.nextLine();
                    
                    if (expr.equalsIgnoreCase("exit") || expr.equalsIgnoreCase("quit")) {
                        System.out.println("Thank you for using the calculator. Goodbye!");
                        sc.close();
                        return; // Terminate the main method.
                    }
                    
                    tokens = tokenizeExpression(expr, evenNumbers, oddNumbers);
                } catch (InvalidExpressionException e) {
                    System.out.println("Error: " + e.getMessage() + ". Please try again.");
                }
            }
            
            boolean representationChosen = false;
            while(!representationChosen) {
                System.out.println("\nExpression is valid. What would you like to do?");
                System.out.println("1. Represent as a LinkedList (with link format)");
                System.out.println("2. Represent as a Queue (with separated queues)");
                System.out.println("3. Represent as a simple ArrayList");
                System.out.println("4. Enter a new expression");
                System.out.println("5. Quit");
                System.out.print("Enter your choice (1-5): ");
                
                try {
                    int mode = sc.nextInt();
                    sc.nextLine(); // Consume newline

                    switch (mode) {
                        case 1:
                            handleLinkedListMode(tokens, evenNumbers, oddNumbers);
                            break;
                        case 2:
                            System.out.print("Enter capacity for input queues: ");
                            int inCap = sc.nextInt();
                            System.out.print("Enter capacity for even/odd queues: ");
                            int eoCap = sc.nextInt();
                            sc.nextLine(); // Consume newline
                            handleQueueMode(tokens, evenNumbers, oddNumbers, inCap, eoCap);
                            break;
                        case 3:
                            handleArrayListMode(tokens, evenNumbers, oddNumbers);
                            break;
                        case 4:
                            representationChosen = true; // Breaks the inner loop to get a new expression.
                            break;
                        case 5:
                            System.out.println("Exiting the calculator.");
                            sc.close();
                            return; // Exit the program entirely.
                        default:
                            System.out.println("Invalid choice. Please select 1-5.");
                    }
                    if (mode >= 1 && mode <= 3) {
                           System.out.println("----------------------------------------");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    sc.nextLine(); // Clear the invalid input
                } catch (InvalidExpressionException | ArithmeticException e) {
                    System.out.println("Calculation Error: " + e.getMessage());
                    representationChosen = true; // Force re-entry of expression on calculation error.
                }
            }
        }
    }

    private static void handleArrayListMode(List<Object> tokens, List<Double> even, List<Double> odd) throws InvalidExpressionException, ArithmeticException {
        ArrayList<Object> expressionList = new ArrayList<>(tokens);
        System.out.println("\nRepresentation: " + expressionList);
        double result = evaluate(expressionList);
        System.out.println("Result: " + result);
        System.out.println("Even Numbers: " + even);
        System.out.println("Odd Numbers: " + odd);
    }

    private static void handleLinkedListMode(List<Object> tokens, List<Double> even, List<Double> odd) throws InvalidExpressionException, ArithmeticException {
        LinkedList<Object> expressionList = new LinkedList<>(tokens);
        double result = evaluate(expressionList);
        
        System.out.println(); // For spacing
        printAsLinks(expressionList, "Representation");
        System.out.println("Result: " + result);
        printAsLinks(new ArrayList<>(even), "Even Numbers");
        printAsLinks(new ArrayList<>(odd), "Odd Numbers");
    }
    
    private static void handleQueueMode(List<Object> tokens, List<Double> evenNumbers, List<Double> oddNumbers, int inCap, int eoCap) throws InvalidExpressionException, ArithmeticException {
        Queue<Object> expressionQueue = new LinkedList<>(tokens);
        LinkedList<Queue<Double>> inputQueues = new LinkedList<>();
        LinkedList<Queue<Double>> evenQueues = new LinkedList<>();
        LinkedList<Queue<Double>> oddQueues = new LinkedList<>();
        
        for (Object token : tokens) {
            if (token instanceof Double) {
                addToQueueList(inputQueues, (Double) token, inCap);
            }
        }
        evenNumbers.forEach(n -> addToQueueList(evenQueues, n, eoCap));
        oddNumbers.forEach(n -> addToQueueList(oddQueues, n, eoCap));

        System.out.println("\nRepresentation: " + expressionQueue);
        double result = evaluate(expressionQueue);
        System.out.println("Result: " + result);
        System.out.println("Input Queues:");
        printQueueList(inputQueues);
        System.out.println("Even Queues:");
        printQueueList(evenQueues);
        System.out.println("Odd Queues:");
        printQueueList(oddQueues);
    }

    private static List<Object> tokenizeExpression(String expr, Collection<Double> even, Collection<Double> odd) throws InvalidExpressionException {
        expr = expr.replaceAll("\\s+", ""); // Remove all whitespace
        // Insert '*' for implicit multiplication: number( or )(
        expr = expr.replaceAll("(?<=\\d|\\))\\(", "*(");
        
        List<Object> tokens = new ArrayList<>();
        int balance = 0;
        even.clear();
        odd.clear();

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    sb.append(expr.charAt(i++));
                }
                i--; // Adjust index after reading the full number

                try {
                    double num = Double.parseDouble(sb.toString());
                    tokens.add(num);
                    if (num % 1 == 0) { // Check if it's an integer
                        if ((int) num % 2 == 0) even.add(num);
                        else odd.add(num);
                    }
                } catch (NumberFormatException e) {
                    throw new InvalidExpressionException("Invalid number format: " + sb.toString());
                }
            } else if ("+-*/()".indexOf(c) != -1) {
                tokens.add(c);
                if (c == '(') balance++;
                else if (c == ')') balance--;
                if (balance < 0) throw new InvalidExpressionException("Unbalanced parentheses: Extra ')' detected");
            } else {
                throw new InvalidExpressionException("Invalid character in expression: " + c);
            }
        }

        if (balance != 0) {
            throw new InvalidExpressionException("Unbalanced parentheses: Mismatch in '(' and ')' count");
        }
        return tokens;
    }

    private static double evaluate(Iterable<Object> tokens) throws InvalidExpressionException, ArithmeticException {
        LinkedList<Double> values = new LinkedList<>();
        LinkedList<Character> ops = new LinkedList<>();
        Object lastToken = null;

        for (Object token : tokens) {
            if (token instanceof Double) {
                values.push((Double) token);
            } else if (token instanceof Character) {
                char op = (Character) token;
                if (op == '(') ops.push(op);
                else if (op == ')') {
                    while (!ops.isEmpty() && ops.peek() != '(') evaluateTop(values, ops);
                    if (ops.isEmpty()) throw new InvalidExpressionException("Mismatched parentheses");
                    ops.pop();
                } else {
                    if (op == '-' && (lastToken == null || (lastToken instanceof Character && "*/+(".indexOf((char)lastToken) != -1))) {
                        values.push(0.0);
                    }
                    while (!ops.isEmpty() && ops.peek() != '(' && precedence(ops.peek()) >= precedence(op)) {
                        evaluateTop(values, ops);
                    }
                    ops.push(op);
                }
            }
            lastToken = token;
        }

        while (!ops.isEmpty()) {
            if (ops.peek() == '(') throw new InvalidExpressionException("Mismatched parentheses");
            evaluateTop(values, ops);
        }
        if (values.size() != 1) {
            throw new InvalidExpressionException("Malformed expression. Check operators and operands");
        }
        return values.pop();
    }

    private static void evaluateTop(LinkedList<Double> values, LinkedList<Character> ops) throws InvalidExpressionException, ArithmeticException {
        if (values.size() < 2) throw new InvalidExpressionException("Missing operand for operator: " + ops.peek());
        double b = values.pop();
        double a = values.pop();
        char op = ops.pop();
        values.push(applyOperation(a, b, op));
    }

    private static int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        return 0;
    }

    private static double applyOperation(double a, double b, char op) throws ArithmeticException {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) throw new ArithmeticException("Division by zero");
                return a / b;
            default: throw new IllegalArgumentException("Invalid operator: " + op);
        }
    }

    private static void addToQueueList(LinkedList<Queue<Double>> list, double num, int capacity) {
        if (list.isEmpty() || list.getLast().size() >= capacity) {
            list.add(new LinkedList<>());
        }
        list.getLast().add(num);
    }

    private static void printQueueList(LinkedList<Queue<Double>> queues) {
        if (queues.isEmpty()) {
               System.out.println("   [None]");
               return;
        }
        int i = 1;
        for (Queue<Double> q : queues) {
            System.out.println("   Queue " + (i++) + ": " + q);
        }
    }
    
    private static void printAsLinks(Collection<?> collection, String label) {
        System.out.print(label + ": ");
        for (Object item : collection) {
            System.out.print(item + " -> ");
        }
        System.out.println("null");
    }
}