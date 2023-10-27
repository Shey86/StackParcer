import java.io.*;
import java.io.File;
import java.lang.*;

/**
 * The StackParcer4 class serves as a simple expression parser and evaluator.
 * It is designed to parse and evaluate expressions, handle variables, integers, and a set of operators
 * and operands. It includes methods for parsing expressions, performing variable-to-integer value mapping,
 * managing operators and their priorities, and evaluating expressions following standard rules.
 * It provides support for two newly created custom operators '@' (exponentiation) and '%' (modulus).
 * It also evaluates two different expressions using the custom operators. Lastly, it prints the process,
 * expressions, and results to the console and prints the expression and result to an output file.
 */

public class StackParcer4 {

    public static void main(String[] args) throws Exception {

        // Create a PrintWriter
        PrintWriter output;
        // Equate the internal name to an external file through the PrintWriter
        output = new PrintWriter(new File("JavaStackParcer.txt"));

        // Create two expressions to evaluate (converted to character array in evaluateExpression method)
        String expression1 = "A@(2*(A-C*D))+(9*B/(2*C+1)-B*3)+E%(A-F)#";
        String expression2 = "B*(3@(A-D)%(B-C@D))+4@D*2#";

        // Evaluate the expressions and print to output file
        output.println(expression1);
        evaluateExpression(expression1, output);
        output.println(expression2);
        evaluateExpression(expression2, output);
        output.close();
    }

    // Method to evaluate the given expression
    public static void evaluateExpression(String expression, PrintWriter output) {

        /* **************Create a Parser for Integer Arithmetic Expressions********************************
         The Parcer is in three parts.
            1. Variable Table.  This is two arrays.  One array contains all the character variables (including numbers)
            we might see in the expression. The second array contains the equivlant integer values of variables
            2. An Operator Table.   This is two arrays.  One array contains all the character variables we might see for
            operator symbols in the expression.  This includes () The second array contains the equivlant priority of
            evaluation of the symbol
            3. A stack for the operands.  This is a stack that will have the integer values from the expression variables
            placed on the stack
            4. A stack for the operators.  This is a stack that contains an object.  The object contains both the symbol
            of the operator and the evaluation priority for that object */

        // Create an evaluator for integer operators
        // First create one stack for the operators
        int i, num, ivalu;

        //************************************Creating the Variable Table********************************************
        // Create the variable array
        char[] vart = {'A', 'B', 'C', 'D', 'E', 'F', '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9'}; // Note that we can only use variables A,B,C,D,E,F
        // Create the corresponding integer array holding the values of the variables in table vart
        // Create a set of initializaed integer values for variables A,B,C,D,E,F
        int[] ivalue = {8, 12, 2, 3, 15, 4, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9};// note A=8,B=12,C=2,D=3,E=15,F=4
        // also note we have created the values for the integer equivalents for characters '0' thru '1'

        //*************************************Creating the Operator Table ******************************************
        // Create the symbols for the operators table
        char[] opert = {'@', '%', '*', '/', '+', '-', ')', '(', '#'};// This is the set of operators
        // Create the evaluation priority for the symbols. The higher the priority, the higher the number in the table
        int[] intvalp = {3, 2, 2, 2, 1, 1, 99, -99, -100};

        //************************************Creating the Stacks for the Operands and the Operators*****************
        // Create a stack for the integer operands
        GenericManagerStacks<Integer> opnd = new GenericManagerStacks<Integer>();
        // Create a stack for the Operators
        GenericManagerStacks<OpertorObj> oper = new GenericManagerStacks<OpertorObj>();
        // WE MUST INITIALIZE the OPERATOR STACK so the first Operator can be pushed on.
        // Must put an end of operation on the operator stack.
        System.out.println("Pushing Operator # with priority -100");
        //Create the operator object and push it on the stack.
        OpertorObj pnode1 = new OpertorObj('#', -100);
        oper.pushnode(pnode1);
        int oprior, exvalue;
        //**************************************Done with table and Stack creation*************************************

        i = 0;
        // Convert the expression from String to char array
        char[] express = expression.toCharArray();
        while (express[i] != '#') {
            System.out.println("Parsing " + express[i]);
            if (((express[i] >= '0') && (express[i] <= '9')) || ((express[i] >= 'A') && (express[i] <= 'F')))
            // Check to see if this character is a variable or an operator.
            {// we have a variable or a constant
                System.out.println("This is an operand " + express[i]);
                // Find the character in the vart table that corresponds with the value
                ivalu = findval(express[i], vart, ivalue, 15);
                if (ivalu == -99) System.out.println("No value in table for " + express[i]);
                // Now that we have the value we need to place it on the operand stack
                System.out.println("Were pushing it on the operand stack " + ivalu);
                opnd.pushnode(ivalu);
            }// End of variable stack
            else {// We are an operator
                System.out.println("This is an operator " + express[i]);
                if (express[i] == '(') { // This is a left parenthesis, push it on the stack
                    System.out.println("Pushing on operator stack " + express[i]);
                    // Create node to push on stack
                    OpertorObj pnodeo = new OpertorObj(express[i], -99);
                    oper.pushnode(pnodeo);
                } else if (express[i] == ')') {// This is a right parenthesis, we must begin to pop operands and operators
                    //until we find the a left parenthesis (
                    while ((oper.peeknode()).operator != '(') {// Must pop and evaluate the stuff on operand and operator stack
                        popevalandpush(oper, opnd);
                    }
                    // Now pop the ( node
                    oper.popnode();
                }// End of this is a right parenthesis
                else {// This is not either ( or ) is is another operator
                    oprior = findval(express[i], opert, intvalp, 6);
                    System.out.println("Peeking at top of stack " + (oper.peeknode()).priority);
                    //**********oprior MUST BE STRICTLY GREATER THAN BEFORE WE CAN PUT IT ON THE STACK********
                    while (oprior <= (oper.peeknode()).priority) popevalandpush(oper, opnd);
                    // Now push this operator on the stack.
                    System.out.println("Pushing Operator " + express[i] + " with priority " + oprior);
                    OpertorObj pnodeo = new OpertorObj(express[i], oprior);
                    oper.pushnode(pnodeo);
                }// This is the end of this is not () operator
            }// End of on operator stack
            i++;
        }// End of while express loop

        // We have found the # in the evaluation now we must evaluate the operator stack
        while ((oper.peeknode()).operator != '#') {// We are finishing up operator stack
            popevalandpush(oper, opnd);
        }// End of finishing up operator stack
        // We're done, get value of opnd stack and print
        exvalue = opnd.popnode();
        // Print the value to console and output file
        System.out.println("The value for this expression is " + exvalue);
        output.println("The value for this expression is " + exvalue);
    }// End of main

    // This method evaluates the operators in the operator table and prints the result
    public static int IntEval(int oper1, char oper, int oper2) {// This is an evaluator for binary operators operating on integers.
        int result;
        switch (oper) {
            case '+':
                result = oper1 + oper2;
                System.out.println("Evaluating  " + oper1 + " " + oper + " " + oper2 + " result: " + result);
                return result;
            case '-':
                result = oper1 - oper2;
                System.out.println("Evaluating  " + oper1 + " " + oper + " " + oper2 + " result: " + result);
                return result;
            case '*':
                result = oper1 * oper2;
                System.out.println("Evaluating  " + oper1 + " " + oper + " " + oper2 + " result: " + result);
                return result;
            case '/':
                if (oper2 != 0) {
                    result = oper1 / oper2;
                    return result;
                } else {
                    System.out.println("Attempted division by zero not allowed.");
                    return -99;
                }
                // Add two more symbols to the operator table
            case '@': // Handle the '@' operator as exponentiation
                if (oper == '@') {
                    // Ensure that exponential values are not allowed
                    if (oper1 == 0 && oper2 < 0) {
                        System.out.println("Exponential values not allowed.");
                        return -99;
                    }
                    // Handle anything raised to the power of 0 as 1
                    if (oper2 == 0) {
                        result = 1;
                    } else {
                        result = (int) Math.pow(oper1, oper2);
                    }
                    System.out.println("Evaluating  " + oper1 + " " + oper + " " + oper2 + " result: " + result);
                    return result;
                }
            case '%': // Handle '%' as the modulus operator
                result = oper1 % oper2;
                System.out.println("Evaluating  " + oper1 + " " + oper + " " + oper2 + " result: " + result);
                return result;
            default:
                System.out.println("Bad operator: " + oper);
                return -99;
        }// End of switch(oper)
    }// End of IntEval

// Everything outside of this is for the rules, priority, etc

    // This method finds the character x in the value table and returns its integer value
    public static int findval(char x, char[] vtab, int[] valtb, int last) {
        int i, vreturn = -99;
        // This finds the character x in the value table vtab and returns the
        // correspond interger value table from valtb
        for (i = 0; i <= last; i++)
            if (vtab[i] == x) vreturn = valtb[i];
        System.out.println("Found this char " + x + " priority is " + vreturn);
        return vreturn;
    }// End of findval;

    /* This method pops the operators and operands from the stack and evaluates them
    and pushes the result back on the stack
      */
    public static void popevalandpush(GenericManagerStacks<OpertorObj> x,
                                      GenericManagerStacks<Integer> y) {// This is the start of pop and push
        int a, b, c;
        char operandx;
        operandx = (x.popnode()).Getopert();
        a = y.popnode();
        b = y.popnode();
        System.out.println("Pop, Evaluate, and Push " + b +  " " + operandx + " " + a);
        c = IntEval(b, operandx, a);
        // Now push the value back on the stack for integers
        y.pushnode(c);
        return;
    }// This is the end of popevalandpush
}// This is the end of Stackparcer3




