package apps;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import structures.Stack;
public class Expression
{
	String expr;
	ArrayList<ScalarSymbol> scalars;
	ArrayList<ArraySymbol> arrays;
	public static final String delims = " \t*+-/()[]";
	public Expression(String expr)
	{
		this.expr = expr;
	}
	public void buildSymbols()
	{
		scalars = new ArrayList<ScalarSymbol>();
		arrays = new ArrayList<ArraySymbol>();
		String curSymbol;
		for(int i = 0; i < expr.length(); i++) // Loop to look for any letters
												// in the expression
		{
			if(Character.isLetter(expr.charAt(i))) // If one is found
			{
				curSymbol = "";
				while (i < expr.length() && Character.isLetter(expr.charAt(i)))
				{
					curSymbol += expr.charAt(i); // Get the entire word
					i++;
				}
				if(i < expr.length() && expr.charAt(i) == '[') // If there is a
																// bracket after
																// it
				{
					if(!arrays.contains(curSymbol))
					{
						ArraySymbol curArray = new ArraySymbol(curSymbol);
						arrays.add(curArray); // If it is an array, add to list
												// of
												// arrays
					}
					else
						continue;
				}
				else
				{
					if(!scalars.contains(curSymbol) && i < expr.length())
					{
						ScalarSymbol curScalar = new ScalarSymbol(curSymbol);
						scalars.add(curScalar); // Otherwise it is a scalar
					}
					else
						continue;
				}
			}
		}
	}
	public void loadSymbolValues(Scanner sc) throws IOException
	{
		while (sc.hasNextLine())
		{
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String sym = st.nextToken();
			ScalarSymbol ssymbol = new ScalarSymbol(sym);
			ArraySymbol asymbol = new ArraySymbol(sym);
			int ssi = scalars.indexOf(ssymbol);
			int asi = arrays.indexOf(asymbol);
			if(ssi == -1 && asi == -1)
			{
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if(numTokens == 2)
			{ // scalar symbol
				scalars.get(ssi).value = num;
			}
			else
			{ // array symbol
				asymbol = arrays.get(asi);
				asymbol.values = new int[num];
				// following are (index,val) pairs
				while (st.hasMoreTokens())
				{
					String tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok, " (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					asymbol.values[index] = val;
				}
			}
		}
	}
	public float evaluate()
	{
		StringTokenizer exprDelims; // Symbols used for the StringTokenizer to
									// tell it when to create a new token
		exprDelims = new StringTokenizer(expr, delims, true); // Makes a new
																// StringTokenizer
		return evaluate(exprDelims); // Calls the recursive method
	}
	private Stack<String> reverseStack(Stack<String> stack) // Reverses a given
															// stack
	{
		Stack<String> stk = new Stack<String>();
		while (!stack.isEmpty())
		{
			stk.push(stack.pop());
		}
		return stk;
	}
	// Instead of using index numbers to find where to split, it is much easier
	// to simply use the delimiters we were given, along with the
	// StringTokenizer to split the expression automatically, since this is the
	// purpose of the tokenizer, why bother doing something that can be done for
	// us in a much easier way
	private float evaluate(StringTokenizer token)
	{
		// Stacks that we will be using throughout this method
		Stack<String> operators = new Stack<String>();
		Stack<String> mathSymbolsAndNums = new Stack<String>();
		Stack<String> postOrderOfOp = new Stack<String>();
		Stack<String> finalResult = new Stack<String>();
		String curToken = null;
		// This loop is to recursively get to the innermost part of a nested
		// expression, so that part can be evaluated with the rest of the
		// method, and then the recursion will allow it to evalute from the
		// inside out. The token object is passed because each recursive call it
		// will contain the next innermost part of the expression, until the
		// center is reached, and then evaluated. The beauty of the recursion
		// with the String Tokenizer is that it handles all of the expression
		// splitting for us,we don't have to worry about searching for the ()
		// and [], the delimiters we pass to the tokenizer handles it all for
		// us, so the only thing the code has to do is replace scalars and
		// arrays, and handle the order of operations and then return the
		// result, which is done with stacks since it keeps everthing in the
		// order we need
		while (token.hasMoreTokens()) // While there is still stuff in the
										// expression
		{
			curToken = token.nextToken().trim(); // Gets the next token without
													// spaces
			if(curToken != null && !curToken.equals("")) // If a valid token
			{
				if(curToken.equals("(") || curToken.equals("[")) // Recursive
																	// call
				{
					operators.push(Float.toString(evaluate(token)));
				}
				// Base case to know we can start evaluation
				else if(curToken.equals(")") || curToken.equals("]"))
				{
					break;
				}
				else // If a non ( or [ is found, add it to the main stack that
						// will later be evaluated
				{
					operators.push(curToken);
				}
			}
		}
		operators = reverseStack(operators); // Reverse the stack to make sure
												// everything gets added and
												// subtracted in the correct
												// order, otherwise, stuff will
												// get subtracted backwards and
												// give the wrong answer
		while (!operators.isEmpty()) // Gives values to the scalars and arrays
		{
			boolean found = false;
			String curr = operators.pop(); // Get the first thing in the stack
			for(int i = 0; i < scalars.size(); i++) // Replace any scalars with
													// their values
			{
				ScalarSymbol toCheck = scalars.get(i);
				if(toCheck.name.equals(curr))
				{
					mathSymbolsAndNums.push(Float.toString(scalars.get(i).value));
					found = true;
					break;
				}
				// Push numerical values to the stack
			}
			for(int i = 0; i < arrays.size(); i++) // Run through the list of
													// arrays
			{
				ArraySymbol toCheck = arrays.get(i);
				if(toCheck.name.equals(curr)) // If the current name is in the
												// list of arrays
				{
					found = true;
					String loc = operators.pop(); // Get the point in the array
					int intLoc = (int) Float.parseFloat(loc); // Convert it to
																// an
																// int
					mathSymbolsAndNums.push(Float.toString(arrays.get(i).values[intLoc]));
					// Get the value for a given position in an array
					break;
				}
			}
			if(!found)
				mathSymbolsAndNums.push(curr); // Add any numbers to the
												// stack
		}
		mathSymbolsAndNums = reverseStack(mathSymbolsAndNums); // Reverse this
																// stack
		while (!mathSymbolsAndNums.isEmpty()) // Until this stack is empty
		{
			float result = 0; // Result of the current operation
			String tmp = mathSymbolsAndNums.pop(); // Top of the stack
			switch (tmp)
			{
			case "*": // If the cur symbol is multiplication
				// Multipy the number before and after the operation together
				float a = Float.parseFloat(postOrderOfOp.pop());
				float b = Float.parseFloat(mathSymbolsAndNums.pop());
				result = a * b;
				// Push it to the after order of op stack
				postOrderOfOp.push(Float.toString(result));
				break;
			case "/": // Same thing as above except with division
				a = Float.parseFloat(postOrderOfOp.pop());
				b = Float.parseFloat(mathSymbolsAndNums.pop());
				result = a / b;
				postOrderOfOp.push(Float.toString(result));
				break;
			default:
				// If it is just a number, add to the postOrderOfOps stack
				postOrderOfOp.push(tmp);
				break;
			}
		}
		postOrderOfOp = reverseStack(postOrderOfOp); // Reverse the stack
		while (!postOrderOfOp.isEmpty())
		{
			// Same process as with * and /, but using + and -
			float result = 0;
			String tmp = postOrderOfOp.pop();
			switch (tmp)
			{
			case "+":
				float a = Float.parseFloat(finalResult.pop());
				float b = Float.parseFloat(postOrderOfOp.pop());
				result = a + b;
				finalResult.push(Float.toString(result));
				break;
			case "-":
				a = Float.parseFloat(finalResult.pop());
				b = Float.parseFloat(postOrderOfOp.pop());
				result = a - b;
				finalResult.push(Float.toString(result));
				break;
			default:
				finalResult.push(tmp);
				break;
			}
		}
		// The stack will only contain the result at this point
		return Float.parseFloat(finalResult.pop());
	}
	public void printScalars()
	{
		for(ScalarSymbol ss:scalars)
		{
			System.out.println(ss);
		}
	}
	public void printArrays()
	{
		for(ArraySymbol as:arrays)
		{
			System.out.println(as);
		}
	}
}
