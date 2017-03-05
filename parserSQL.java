import java.util.ArrayList;


public class parserSQL {
	
	static int index = 0; // index of the ArrayList
	static int xmlIndex = 0;
	static String[] tagnameArray = new String[53];
	static int tagIndex = 0;
	static boolean noError = true; 
	
	public static void main(){
	/*
	 * Grammar:
	 * start -> SELECT modifier FROM table whereClause ; 
	 * modifier -> DISTINCT attributes || attributes 
	 * attributes -> compression AttributeName rename addAttr || 
	 * 					< tagBegin , > addAttr 
	 * rename -> AS AttributeName addAttr || @ 
	 * addAttr -> , attributes ||  @ 
	 * compression -> + || @ 
	 * table -> TableName addT 
	 * addTable -> , table || @ 
	 * tagBegin -> TagName, attributes  
	 * whereClause -> WHERE options || @ 
	 * options -> ColumnName operator value addOptions  
	 * operator -> = || <> || > || < || >= || <= || BETWEEN || LIKE || IN 
	 * value -> 'StringValue' || NumberValue 
	 * addOptions -> AND options || OR options || @
	 */
		
		ArrayList<tok> pHolder = new ArrayList<tok>();

		 pHolder.add(new tok("@", "@"));
		
		return;
	}
	
	public static ArrayList<tok> start(ArrayList<tok> pHolder){
		/* start -> SELECT modifier FROM table whereClause ;
		 * 
		 * This is where Command_Parser starts.
		 * 
		 * It also creates xmlList, which is ultimately passed through
		 * Command_Parser. xmlList contains all of the values the XML
		 * converter needs.
		*/
		
		ArrayList<tok> xmlList = new ArrayList<tok>(); // xmlList created
		
		if(pHolder.get(index).getType() == Token_Type.TOK_SELECT){
			index++;
			modifier(pHolder, xmlList);
			
			if(pHolder.get(index).getType() == Token_Type.TOK_FROM){
				index++;
				table(pHolder, xmlList);
				whereClause(pHolder, xmlList);
				
				if(pHolder.get(index).getType() == Token_Type.TOK_SEMICOLON){
					return xmlList;
				}
				else{
					 noError = false;
					 return xmlList = null;}
			}
			else{
				noError = false;}
		}
		else{
			noError = false;}
		
		if(noError = false){
			xmlList = null;
		}
		
		return xmlList;
	}
	
	public static void modifier(ArrayList<tok> pHolder, ArrayList<tok> xmlList){
		// modifier -> DISTINCT attributes || attributes
		// AttributeName is represented as ID
		
		if(pHolder.get(index).getType() == Token_Type.TOK_DISTINCT){
			index++;
			attributes(pHolder, xmlList);
		}
		else if(pHolder.get(index).getType() == Token_Type.TOK_ID
				|| pHolder.get(index).getType() == Token_Type.TOK_PLUS
				|| pHolder.get(index).getType() == Token_Type.TOK_LTHAN){
			attributes(pHolder, xmlList);	
		}
		else{noError = false;}
	}
	
	public static void attributes(ArrayList<tok> pHolder, ArrayList<tok> xmlList){
		/* attributes -> compression AttributeName rename addAttr || < tagBegin ,>
		 * AttributeName is represented as ID
		 * 
		 * This is the main place where attributes are added to xmlList.
		 * It also handles the attribute's compression variable in xmlList
		*/
		
		boolean localCompression = false; // this variable is used in 
											// changing setCompress to t/f
		
		if(pHolder.get(index).getType() == Token_Type.TOK_PLUS
				|| pHolder.get(index).getType() == Token_Type.TOK_ID){
			
			// the compression grammar method has been changed to boolean to
			// make this work
			if(compression(pHolder, xmlList)){
				localCompression = true;
			}
			
			if(pHolder.get(index).getType() == Token_Type.TOK_ID){
				
				// (re)initialize the tableName so table can be set in tok
				String tableName = pHolder.get(index).getTerm();
				index++;
				if(pHolder.get(index).getType() == Token_Type.TOK_PERIOD){
					index++;
					if(pHolder.get(index).getType() == Token_Type.TOK_ID){
						index++;
						
						// Adding attribute to the xmlList (table.attr format)
						xmlList.add(new tok(pHolder.get(index).getTerm(), null));
						
						// Setting the tableName of the attribute
						xmlList.get(xmlIndex).setTableName(tableName);
						
						/* Setting the tagname as whatever the "current" tagname
						 * is. tagnameArray is incremented when the parser sees 
						 * a "<" and decremented when it sees a ">"
						 */
						xmlList.get(xmlIndex).setTagname(tagnameArray[tagIndex]);
						
						// Checks to see if this attribute it compressed,
						// if it is, sets the compression to true.
						if(localCompression){
							xmlList.get(xmlIndex).setCompress(true);
						}
					}
					else{noError = false;}
				}
				
				// Adding attribute to the xmlList
				xmlList.add(new tok(pHolder.get(index).getTerm(), null));
				
				/* Setting the tagname as whatever the "current" tagname
				 * is. tagnameArray is incremented when the parser sees 
				 * a "<" and decremented when it sees a ">"
				 */
				xmlList.get(xmlIndex).setTagname(tagnameArray[tagIndex]);
				
				// Checks to see if this attribute it compressed,
				// if it is, sets the compression to true.
				if(localCompression){  
					xmlList.get(xmlIndex).setCompress(true);
				}
				
				rename(pHolder, xmlList);
				// increase xmlIndex after it has checked to see if the attr
				// is going to have an alias.
				xmlIndex++;
				addAttr(pHolder, xmlList);
			}
			else{noError = false;}
		}
		else if(pHolder.get(index).getType() == Token_Type.TOK_LTHAN){
			index++;
			// increment tagnameArray with "<"
			tagIndex++;
			tagBegin(pHolder, xmlList);
		}
		else{noError = false;}
	}
	
	public static void rename(ArrayList<tok> pHolder, ArrayList<tok> xmlList){
		/* rename -> AS AttributeName addAttr || @
		 * AttributeName is represented as ID
		 * Also sets the alias of an attribute in xmlList
		*/
		
		if(pHolder.get(index).getType() == Token_Type.TOK_AS){
			index++;
			if(pHolder.get(index).getType() == Token_Type.TOK_ID){
				
				// sets the alias of the current attribute
				xmlList.get(xmlIndex).setAlias(pHolder.get(index).getTerm());
				index++;
			}
		}
		else if(pHolder.get(index).getType() == Token_Type.TOK_COMMA
				|| pHolder.get(index).getType() == Token_Type.TOK_FROM){
			return;
		}
		else{noError = false;}
	}
	
	public static void addAttr(ArrayList<tok> pHolder, ArrayList<tok> xmlList){
		// addAttr -> , attributes || @
		
		if(pHolder.get(index).getType() == Token_Type.TOK_COMMA){
			index++;
			if(pHolder.get(index).getType() == Token_Type.TOK_GTHAN){
				return;
			}
			attributes(pHolder, xmlList);
		}
		else if(pHolder.get(index).getType() == Token_Type.TOK_FROM){
			return;
		}
		else{noError = false;}
	}
	
	public static boolean compression(ArrayList<tok> pHolder, ArrayList<tok> xmlList){
		/* compression -> + || @
		 * AttributeName is represented as ID
		 * 
		 * This one grammar method is boolean because of how it interacts with
		 * attributes to set the compression variable in xmlList.
		*/
		
		boolean compressed = false;

		if(pHolder.get(index).getType() == Token_Type.TOK_PLUS){
			index++;
			compressed = true;
		}
		else if(pHolder.get(index).getType() == Token_Type.TOK_ID){
			return compressed;
		}
		else{noError = false;}
		
		return compressed;
	}
	
	public static void table(ArrayList<tok> pHolder, ArrayList<tok> xmlList){
		// table -> TableName addT
		// TableName is represented as ID
		
		if(pHolder.get(index).getType() == Token_Type.TOK_ID){
			index++;
			addTable(pHolder, xmlList);
		}
		else{noError = false;}
	}
	
	public static void addTable(ArrayList<tok> pHolder, ArrayList<tok> xmlList){
		// addTable -> , table || @
		
		if(pHolder.get(index).getType() == Token_Type.TOK_COMMA){
			index++;
			table(pHolder, xmlList);
		}
		else if(pHolder.get(index).getType() == Token_Type.TOK_WHERE
				|| pHolder.get(index).getType() == Token_Type.TOK_SEMICOLON){
			return;
		}
		else{noError = false;}
	}
	
	public static void tagBegin(ArrayList<tok> pHolder, ArrayList<tok> xmlList){
		// tagBegin -> TagName, attributes
		// TagName is represented as ID
		
		if(pHolder.get(index).getType() == Token_Type.TOK_ID){
			
			// <-------------------------------------------------------
			// xmlList.add(new tok(pHolder.get(index).getTerm(),""));
			
			tagnameArray[tagIndex] = pHolder.get(index).getTerm();
			
			// <-------------------------------------------------------
			index++;
			if(pHolder.get(index).getType() == Token_Type.TOK_COMMA){
				index++;
				attributes(pHolder, xmlList);

					if(pHolder.get(index).getType() == Token_Type.TOK_GTHAN){
						index++;
						
						// decrement tagnameArray
						tagIndex--;
						return;
					}
					else{noError = false;}
			}
			else{noError = false;}
		}
		else{noError = false;}
	}
	
	public static void whereClause(ArrayList<tok> pHolder, ArrayList<tok> xmlList){
		// whereClause -> WHERE options || @
		
		if(pHolder.get(index).getType() == Token_Type.TOK_WHERE){
			options(pHolder, xmlList);
		}
		else if(pHolder.get(index).getType() == Token_Type.TOK_SEMICOLON){
			return;
		}
		else{
			noError = false;
		}
	}
	
	public static void options(ArrayList<tok> pHolder, ArrayList<tok> xmlList){
		// options -> ColumnName operator value addOptions
		// ColumnName is represented as ID
		
		if(pHolder.get(index).getType() == Token_Type.TOK_WHERE){
			index++;
			if(pHolder.get(index).getType() == Token_Type.TOK_ID){
				index++;
				if(pHolder.get(index).getType() == Token_Type.TOK_PERIOD){
					index++;
					if(pHolder.get(index).getType() == Token_Type.TOK_ID){
						index++;
					}
				}
				
				operator(pHolder, xmlList);
				value(pHolder, xmlList);
				addOptions(pHolder, xmlList);
			}
			else{noError = false;}
		}
		else{noError = false;}
	}

	public static void operator(ArrayList<tok> pHolder, ArrayList<tok> xmlList){
		// operator -> = || <> || > || < || >= || <= 
		//				 || BETWEEN || LIKE || IN
		
		if(pHolder.get(index).getType() == Token_Type.TOK_EQUAL
				|| pHolder.get(index).getType() == Token_Type.TOK_NEQUAL
				|| pHolder.get(index).getType() == Token_Type.TOK_GTHAN
				|| pHolder.get(index).getType() == Token_Type.TOK_LTHAN
				|| pHolder.get(index).getType() == Token_Type.TOK_GTEQUAL
				|| pHolder.get(index).getType() == Token_Type.TOK_LTEQUAL
				|| pHolder.get(index).getType() == Token_Type.TOK_BETWEEN
				|| pHolder.get(index).getType() == Token_Type.TOK_LIKE
				|| pHolder.get(index).getType() == Token_Type.TOK_IN){
			index++;
			return;
		}
		else{noError = false;}
	}

	private static void value(ArrayList<tok> pHolder, ArrayList<tok> xmlList) {
		// value -> 'StringValue' || NumberValue
		// StringValue is represented as ID
		// NumberValue is represented as NUM
		
		if(pHolder.get(index).getType() == Token_Type.TOK_APOSTROPHE){
			index++;
			if(pHolder.get(index).getType() == Token_Type.TOK_ID){
				index++;
				if(pHolder.get(index).getType() == Token_Type.TOK_APOSTROPHE){
					index++;
					return;
				}
				else{noError = false;}
			}
			else{noError = false;}
		}
		else if(pHolder.get(index).getType() == Token_Type.TOK_NUM){
			index++;
			return;
		}
		else{noError = false;}
	}
	
	private static void addOptions(ArrayList<tok> pHolder, ArrayList<tok> xmlList) {
		// addOptions -> AND options || OR options || @
		
		if(pHolder.get(index).getType() == Token_Type.TOK_AND
				|| pHolder.get(index).getType() == Token_Type.TOK_OR){
			index++;
			options(pHolder, xmlList);
		}
		else if(pHolder.get(index).getType() == Token_Type.TOK_SEMICOLON){
			return;
		}
		else{noError = false;}
	}
}
class tok {
	/*
	 * This is my tok object from Compilers. The interesting thing about this
	 * object's style is that is allows for a dual name system which could be 
	 * useful for rename statements. Effectively we could store whatever is 
	 * after the 'AS' in the second slot. If there is no 'AS', then the original
	 * name could be copied to the other. Obviously I have not made any changes, 
	 * but I was considering nameSQL and nameDisplay (or some such thing). 
	 * Open to suggestions
	 * 
	 */
	
	Token_Type type;
	String terminal;
	String alias;
	String tagname;
	String table;
	boolean compression;
	
	public tok(String terminal, String alias) {
		
		this.type = Token_Type.TOK_NONE;
		this.terminal = terminal.trim();
		this.alias = alias.trim();
		
	}
	
	public void setTableName(String table){
		this.table = table;
	}
	
	public String getTableName(){
		return this.table;
	}
	
	public void setTagname(String tagname){
		this.tagname = tagname;
	}
	
	public String getTagname(){
		return this.tagname;
	}
	
	public void setCompress(boolean compression){
		this.compression = compression;
	}
	
	public boolean getCompress(){
		return this.compression;
	}
	
	public void setType(Token_Type type){
		this.type = type;
	}
	
	public Token_Type getType(){
		return this.type;
	}
	
	public void setTerm(String terminal){
		this.terminal = terminal;
	}
	
	public String getTerm(){
		return this.terminal;
	}
	
	public void setAlias(String alias){
		this.alias = alias;
	}
	
	public String getAlias(){
		return this.alias;
	}
}

enum Token_Type
{
	TOK_NONE,  // Default, unassigned token value.
	TOK_SELECT,
	TOK_FROM,
	TOK_WHERE,
	TOK_DISTINCT,
	TOK_AS,
	TOK_BETWEEN,
	TOK_AND,
	TOK_OR,
	TOK_LIKE,
	TOK_IN,
	TOK_ID,
	TOK_NUM,
	TOK_SEMICOLON,
	TOK_LPAREN,
	TOK_RPAREN,
	TOK_APOSTROPHE,
	TOK_PERIOD,
	TOK_COMMA,
	TOK_PLUS,
	TOK_LTHAN,
	TOK_GTHAN,
	TOK_EQUAL,
	TOK_LTEQUAL, // Less than or equal to
	TOK_GTEQUAL, // Greater than or equal to
	TOK_NEQUAL   // Not equal to
}

//*~*~*~*~*~*~*~*~*~*~*~*~*~*~*
//*~*~*~*~*~*~ Fin ~*~*~*~*~*~*
//*~*~*~*~*~*~*~*~*~*~*~*~*~*~*
