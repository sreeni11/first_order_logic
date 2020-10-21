import java.io.*;
import java.util.LinkedList;

public class homework
{
	public static void main(String [] args)
	{
		AISystem s = new AISystem();
		try
		{
			s.initialise();
		}
		catch(FileNotFoundException e)
		{
			System.out.println("File not found, closing program");
			System.out.println(e);
			System.exit(0);
		}
		
		//s.display();

		try
		{
			s.run();
		}
		catch(IOException e)
		{
			System.out.println("IO Exception has occured, closing program");
			System.exit(0);
		}
	}
}

class AISystem
{
	private int numQueries;
	private LinkedList<Literal> queries;
	private int numKB;
	private LinkedList<KnowledgeBase> KB;
	public static char base = 'a';

	public AISystem()
	{
		numQueries = 0;
		queries = new LinkedList<Literal>();
		numKB = 0;
		KB = new LinkedList<KnowledgeBase>();
	}

	//Getters and Setters
	public int getNumQueries()
	{
		return numQueries;
	}

	public LinkedList<Literal> getQueries()
	{
		return queries;
	}

	public int getNumKB()
	{
		return numKB;
	}

	public LinkedList<KnowledgeBase> getKB()
	{
		return KB;
	}

	public void setNumQueries(int n)
	{
		numQueries = n;
	}

	public void setQueries(LinkedList<Literal> q)
	{
		queries = q;
	}

	public void setNumKB(int n)
	{
		numKB = n;
	}

	public void setKB(LinkedList<KnowledgeBase> k)
	{
		KB = k;
	}

	//Functions
	public void initialise() throws FileNotFoundException
	{
		FileInputStream fis = new FileInputStream("./input.txt");

		BufferedReader in = new BufferedReader(new InputStreamReader(fis));
		String str = "";

		try
		{
			str = in.readLine();
		}
		catch(IOException e)
		{
			System.out.println("Error reading input file, closing program");
			System.exit(0);
		}
		numQueries = Integer.parseInt(str);
		for(int i = 0; i < numQueries; ++i)
		{
			try
			{
				str = in.readLine();
				str = str.replace(" ", "");
				queries.add(new Literal(str));
			}
			catch(IOException e)
			{
				System.out.println("Error reading input file, closing program");
				System.exit(0);
			}
		}

		try
		{
			str = in.readLine();
		}
		catch(IOException e)
		{
			System.out.println("Error reading input file, closing program");
			System.exit(0);
		}
		numKB = Integer.parseInt(str);
		for(int i = 0; i < numKB; ++i)
		{
			try
			{
				str = in.readLine();
				str = str.replace(" ", "");
				KB.add(new KnowledgeBase(str));
			}
			catch(IOException e)
			{
				System.out.println("Error reading input file, closing program");
				System.exit(0);
			}
		}
		try
		{
			in.close();
		}
		catch(IOException e)
		{
			System.out.println("Error reading input file, closing program");
			System.exit(0);
		}
		catch(Exception e)
		{
			System.out.println("Unexpected error " + e + ", closing program");
			System.exit(0);
		}
	}

	public void run() throws IOException
	{
		LinkedList<KnowledgeBase> temp;
		LinkedList<KnowledgeBase> imp;
		LinkedList<KnowledgeBase> fact;
		Literal temp2;
		boolean test = false;
		PrintWriter writer = new PrintWriter(new FileWriter("./output.txt"));
		for(int i = 0; i < numQueries; ++i)
		{
			temp = copyKB();
			temp2 = new Literal(queries.get(i));
			temp2.notOp();
			temp.addFirst(new KnowledgeBase(temp2));
			test = false;

			while(!test)
			{
				LinkedList<KnowledgeBase> res = new LinkedList<KnowledgeBase>();
				LinkedList<KnowledgeBase> temp3;
				for(int j = 0; j < temp.size()-1; ++j)
				{
					for(int k = j+1; k < temp.size(); ++k)
					{
						temp3 = resolve(temp.get(j), temp.get(k));
						if(containsEmpty(temp3))
						{
							writer.println("TRUE");
							test = true;
							break;
						}
						
						for(int n = 0; n < temp3.size(); ++n)
						{
							if(!res.contains(temp3.get(n)))
							{
								res.add(temp3.get(n));
							}
						}
					}
					if(test)
					{
						break;
					}
				}

				if(!test && isSubset(res, temp))
				{
					writer.println("FALSE");
					test = true;
					break;
				}
				if(!test)
				{
					for(int j = 0; j < res.size(); ++j)
					{
						temp.add(res.get(j));
					}
				}
			}
		}
		writer.close();
	}

	public boolean containsEmpty(LinkedList<KnowledgeBase> l)
	{
		for(int i = 0; i < l.size(); ++i)
		{
			if(l.get(i).getImplication().size() == 0 && l.get(i).getRes().getConstArgs().size() == 0)
			{
				return true;
			}
		}
		return false;
	}

	public LinkedList<KnowledgeBase> resolve(KnowledgeBase a, KnowledgeBase b)
	{
		LinkedList<KnowledgeBase> res = new LinkedList<KnowledgeBase>();
		if(a.getIsImplication() && b.getIsImplication())
		{
			if(a.getRes().notEquals(b.getRes()))
			{
				KnowledgeBase t = new KnowledgeBase();
				t.setImplication(a.getImplication());
				t.joinImplications(b.getImplication());
				t.setIsImplication(true);
				res.add(t);
			}
			else
			{
				LinkedList<KnowledgeBase> te;
				
				te = unification(a,b);
				for(int i = 0; i < te.size(); ++i)
				{
					if(te.get(i).getAnd() && !res.contains(te.get(i)))
					{
						res.add(te.get(i));
					}
				}
			}
		}
		else if(!a.getIsImplication() && b.getIsImplication())
		{
			if(a.getRes().notEquals(b.getRes()))
			{
				KnowledgeBase t = new KnowledgeBase();
				t.setImplication(b.getImplication());
				t.setIsImplication(true);
				res.add(t);
			}
			else
			{
				KnowledgeBase t = unification(b, a.getRes());
				if(t.getAnd())
				{	
					res.add(t);
				}
			}
		}
		else if(a.getIsImplication() && !b.getIsImplication())
		{
			if(a.getRes().notEquals(b.getRes()))
			{
				KnowledgeBase t = new KnowledgeBase();
				t.setImplication(a.getImplication());
				t.setIsImplication(true);
				res.add(t);
			}
			else
			{
				KnowledgeBase t = unification(a, b.getRes());
				if(t.getAnd())
				{	
					res.add(t);
				}
			}
		}
		else
		{
			if(a.getRes().notEquals(b.getRes()))
			{
				return res;
			}
		}
		return res;
	}

	public LinkedList<KnowledgeBase> unification(KnowledgeBase a, KnowledgeBase b)
	{
		LinkedList<KnowledgeBase> ret = new LinkedList<KnowledgeBase>();
		KnowledgeBase ka = new KnowledgeBase(a);
		KnowledgeBase kb = new KnowledgeBase(b);
		LinkedList<Literal> la = ka.getImplication();
		if(ka.getRes().getConstArgs().size() > 0)
		{
			la.add(ka.getRes());
		}
		LinkedList<Literal> lb = kb.getImplication();
		if(kb.getRes().getConstArgs().size() > 0)
		{
			lb.add(kb.getRes());
		}
		boolean check;
		LinkedList<String> st1;
		LinkedList<String> st2;
		String s1;
		String s2;
		LinkedList<String> str;
		Literal tmp;
		for(int i = 0; i < la.size(); ++i)
		{
			st1 = la.get(i).getConstArgs();
			s1 = la.get(i).getPredicate();
			for(int j = 0; j < lb.size(); ++j)
			{
				check = true;
				tmp = new Literal();
				str = new LinkedList<String>();
				st2 = lb.get(j).getConstArgs();
				s2 = lb.get(j).getPredicate();
				if(s1.equals(s2) && la.get(i).getNot() != lb.get(j).getNot())
				{
					tmp.setPredicate(s1);
					tmp.setNot(la.get(i).getNot());
					for(int k = 0; k < st1.size(); ++k)
					{
						if(st1.get(k).equals(st2.get(k)))
						{
							str.add(new String(st1.get(k)));
						}
						else
						{
							if(Character.isLowerCase(st1.get(k).charAt(0)) && st2.get(k).length() > 1)
							{
								String st = new String(st1.get(k));
								String rp = new String(st2.get(k));
								str.add(rp);
								ka.replaceArgRes(st, rp);
								for(int z = 0; z < la.size(); ++z)
								{
									la.get(z).replaceArg(st, rp);
								}
							}
							else if(Character.isLowerCase(st2.get(k).charAt(0)) && st1.get(k).length() > 1)
							{
								String st = new String(st2.get(k));
								String rp = new String(st1.get(k));
								str.add(rp);
								kb.replaceArgRes(st, rp);
								for(int z = 0; z < lb.size(); ++z) 
								{
									lb.get(z).replaceArg(st, rp);
								}
							}
							else if(Character.isLowerCase(st1.get(j).charAt(0)) && Character.isLowerCase(st2.get(j).charAt(0)))
							{
								String st = new String(st2.get(j));
								String rp = new String(st1.get(j));
								str.add(rp);
								kb.replaceArgRes(st,rp);
								for(int z = 0; z < lb.size(); ++z)
								{
									lb.get(z).replaceArg(st, rp);
								}	
							}
							else
							{
								check = false;
								break;
							}
						}
					}
				}
				else
				{
					check = false;
				}
				if(check)
				{
					la.remove(tmp);
					tmp.notOp();
					lb.remove(tmp);
					
					KnowledgeBase sing = new KnowledgeBase();
					sing.setImplication(la);
					sing.joinImplications(lb);
					sing.setAnd(true);
					ret.add(sing);
				}
			}
		}
		return ret;
	}

	public KnowledgeBase unification(KnowledgeBase kb, Literal l)
	{
		KnowledgeBase ret = new KnowledgeBase();
		KnowledgeBase ka = new KnowledgeBase(kb);
		Literal test = ka.getRes();
		boolean check = true;
		LinkedList<String> st1 = test.getConstArgs();
		LinkedList<String> st2 = l.getConstArgs();
		String s1 = test.getPredicate();
		String s2 = l.getPredicate();
		LinkedList<Literal> a = ka.getImplication();
		LinkedList<String> str = new LinkedList<String>();
		if(s1.equals(s2) && test.getNot() != l.getNot())
		{
			for(int i = 0; i < st1.size(); ++i)
			{
				if(st1.get(i).equals(st2.get(i)))
				{
					str.add(new String(st1.get(i)));
				}
				else
				{
					if(Character.isLowerCase(st1.get(i).charAt(0)) && st2.get(i).length() > 1) 
					{
						String st = new String(st1.get(i));
						String rp = new String(st2.get(i));
						str.add(rp);
						for(int j = 0; j < a.size(); ++j)
						{
							a.get(j).replaceArg(st, rp);
						}
					}
					else if(Character.isLowerCase(st2.get(i).charAt(0)) && st1.get(i).length() > 1)
					{
						String st = new String(st2.get(i));
						String rp = new String(st1.get(i));
						str.add(rp);
						for(int j = 0; j < a.size(); ++j)
						{
							a.get(j).replaceArg(st, rp);
						}
					}
					else if(Character.isLowerCase(st1.get(i).charAt(0)) && Character.isLowerCase(st2.get(i).charAt(0)))
					{
						String st = new String(st1.get(i));
						str.add(st);	
					}
					else
					{
						check = false;
						break;
					}
				}
			}
		}
		else
		{
			check = false;
		}
		if(check)
		{
			ret.setImplication(a);
			ret.setIsImplication(true);
			ret.setAnd(true);
			return ret;
		}
		
		ka = new KnowledgeBase(kb);
		a = ka.getImplication();

		for(int i = 0; i < a.size(); ++i)
		{
			s1 = a.get(i).getPredicate();
			check = true;
			str = new LinkedList<String>();
			Literal tmp = new Literal();
			if(s1.equals(s2) && a.get(i).getNot() != l.getNot())
			{
				tmp.setPredicate(s1);
				tmp.setNot(a.get(i).getNot());
				st1 = a.get(i).getConstArgs();
				for(int j = 0; j < st1.size(); ++j)
				{
					if(st1.get(j).equals(st2.get(j)))
					{
						str.add(new String(st1.get(j)));
					}
					else
					{
						if(Character.isLowerCase(st1.get(j).charAt(0)) && st2.get(j).length() > 1)
						{
							String st = new String(st1.get(j));
							String rp = new String(st2.get(j));
							str.add(rp);
							ka.replaceArgRes(st, rp);
							for(int k = 0; k < a.size(); ++k)
							{
								a.get(k).replaceArg(st, rp);

							}
						}
						else if(Character.isLowerCase(st2.get(j).charAt(0)) && st1.get(j).length() > 1)
						{
							String st = new String(st2.get(j));
							String rp = new String(st1.get(j));
							str.add(rp);
							ka.replaceArgRes(st, rp);
							for(int k = 0; k < a.size(); ++k)
							{
								a.get(k).replaceArg(st, rp);
							}
						}
						else if(Character.isLowerCase(st1.get(j).charAt(0)) && Character.isLowerCase(st2.get(j).charAt(0)))
						{
							String st = new String(st1.get(j));
							str.add(st);	
						}
						else
						{
							check = false;
							break;
						}
					}
				}
			}
			else
			{
				check = false;
			}
			if(check)
			{
				tmp.setConstArgs(str);
				a.remove(tmp);
				if(a.size() > 0)
				{	
					ret.setImplication(a);
					ret.setIsImplication(true);
				}
				else
				{
					ret.setIsImplication(false);
				}
				ret.setRes(ka.getRes());
				ret.setAnd(true);
				return ret;
			}
		}
		return ret;
	}

	public boolean isSubset(LinkedList<KnowledgeBase> a, LinkedList<KnowledgeBase> b)
	{
		boolean res = true;
		if(a.size() > b.size())
		{
			res = false;
		}
		else
		{
			for(int i = 0; i < a.size(); ++i)
			{
				if(!(b.contains(a.get(i))))
				{
					res = false;
					break;
				}
			}
		}
		return res;
	}

	public LinkedList<KnowledgeBase> copyKB()
	{
		LinkedList<KnowledgeBase> temp = new LinkedList<KnowledgeBase>();
		for(int i = 0; i < KB.size(); ++i)
		{
			temp.add(new KnowledgeBase(KB.get(i)));
		}
		return temp;
	}

	public void display()
	{
		System.out.println("Queries:");
		for(int i = 0; i < queries.size(); ++i)
		{
			queries.get(i).display();
			System.out.println();
		}
		System.out.println();
		System.out.println("Knowledge Base:");
		for(int i = 0; i < KB.size(); ++i)
		{
			KB.get(i).display();
		}
	}
}

class KnowledgeBase
{
	LinkedList<Literal> implication;
	Literal res;
	boolean isImplication;
	boolean and;

	public KnowledgeBase()
	{
		implication = new LinkedList<Literal>();
		res = new Literal();
		isImplication = false;
		and = false;
	}

	public KnowledgeBase(String s)
	{
		if(s.indexOf("=>") > -1)
		{
			isImplication = true;
			implication = new LinkedList<Literal>();
			String [] temp = s.split("=>");
			res = new Literal(temp[1]);
			String [] temp2 = temp[0].split("\\&");
			temp2 = standardize(temp2);
			for(int i = 0; i < temp2.length; ++i)
			{
				implication.add(new Literal(temp2[i]));
				implication.get(i).notOp();
			}
			and = false;
		}
		else
		{
			isImplication = false;
			implication = new LinkedList<Literal>();
			res = new Literal(s);
			and = false;
		}
	}

	public KnowledgeBase(KnowledgeBase k)
	{
		and = k.getAnd();
		isImplication = k.getIsImplication();
		implication = new LinkedList<Literal>();
		if(isImplication)
		{
			LinkedList<Literal> temp = k.getImplication();
			for(int i = 0; i < temp.size(); ++i)
			{
				implication.add(new Literal(temp.get(i)));
			}
		}
		res = new Literal(k.getRes());
	}

	public KnowledgeBase(Literal l)
	{
		and = false;
		isImplication = false;
		implication = new LinkedList<Literal>();
		res = new Literal(l);
	}

	//Getters and Setters
	public LinkedList<Literal> getImplication()
	{
		return implication;
	}

	public Literal getRes()
	{
		return res;
	}

	public boolean getIsImplication()
	{
		return isImplication;
	}

	public boolean getAnd()
	{
		return and;
	}

	public void setImplication(LinkedList<Literal> l)
	{
		implication = new LinkedList<Literal>();
		for(int i = 0; i < l.size(); ++i)
		{
			implication.add(new Literal(l.get(i)));
		}
	}

	public void joinImplications(LinkedList<Literal> l)
	{
		if(implication == null)
		{
			implication = new LinkedList<Literal>();
		}
		for(int i = 0; i < l.size(); ++i)
		{
			implication.add(new Literal(l.get(i)));
		}
	}

	public void addImplication(Literal l)
	{
		if(implication == null)
		{
			implication = new LinkedList<Literal>();
		}
		implication.add(new Literal(l));
	}

	public void setRes(Literal r)
	{
		res = new Literal(r);
	}

	public void setIsImplication(boolean b)
	{
		isImplication = b;
	}

	public void setAnd(boolean b)
	{
		and = b;
	}

	//Functions
	public String [] standardize(String [] s)
	{
		String [] temp;
		String [] temp2;
		String [] res = new String[s.length];
		for(int i = 0; i < s.length; ++i)
		{
			res[i] = "";
			if(s[i].charAt(0) == '~')
			{
				res[i] = "~";
				s[i] = s[i].replace("~", "");
			}
			temp = s[i].split("\\(");
			res[i] += temp[0] + "(";
			temp[1] = temp[1].replace(")","");
			temp2 = temp[1].split(",");
			for(int j = 0; j < temp2.length; ++j)
			{
				if(Character.isLowerCase(temp2[j].charAt(0)))
				{
					String c = getVar(s);
					String t = temp2[j];
					if(this.res.containsArg(t))
					{
						LinkedList<Integer> ind = this.res.getIndices(t);
						for(int k = 0; k < ind.size(); ++k)
						{
							this.res.setConstArg(c,ind.get(k));
						}
					}
					for(int k = i+1; k < s.length; ++k)
					{
						s[k] = s[k].replace(t + ",","-"+c + ",");
						s[k] = s[k].replace(t + ")","-"+c + ")");
					}
					temp2[j] = c;
				}
				
				if(j != temp2.length-1)
				{
					res[i] += temp2[j] + ",";
				}
				else
				{
					res[i] += temp2[j] + ")";
				}
				res[i] = res[i].replace("-","");
			}
		}
		return res;
	}

	public String getVar(String[] s)
	{
		String res = "";
		Boolean test = true;
		for(int i = 0; i < 26; ++i)
		{
			res = "" + (char)(AISystem.base + i);
			for(int j = 0; j < s.length; ++j)
			{
				String [] temp;
				String [] temp2;
				temp = s[j].split("\\(");
				temp[1] = temp[1].replace(")","");
				temp2 = temp[1].split(",");
				for(int k = 0; k < temp2.length; ++k)
				{
					if(temp2[k].equals(res))
					{
						test = false;
						break;
					}
				}
				if(!test)
				{
					break;
				}
			}
			if(test)
			{
				AISystem.base += i + 1;
				if(AISystem.base > 'z')
				{
					AISystem.base = 'a';
				}
				return res;
			}
			test = true;
		}
		return res;
	}

	@Override
	public boolean equals(Object o)
	{
		if(o == this)
		{
			return true;
		}

		if(!(o instanceof KnowledgeBase))
		{
			return false;
		}
		KnowledgeBase a = (KnowledgeBase)o;
		boolean res = false;
		if((isImplication && implication.size() == 1) && (!a.getIsImplication())) //check if only one implication in this and res in a or other way around check if they are equal
		{
			if(implication.get(0).equals(a.getRes()))
			{
				return true;
			}
		}
		if((a.getIsImplication() && a.getImplication().size() == 1) && (!isImplication)) //check if only one implication in this and res in a or other way around check if they are equal
		{
			if(a.getImplication().get(0).equals(res))
			{
				return true;
			}
		}
		if(isImplication == a.getIsImplication())
		{
			res = this.res.equals(a.getRes());
			if(isImplication)
			{
				if(res)
				{
					LinkedList<Literal> temp = a.getImplication();
					if(temp.size() == implication.size())
					{	
						for(int i = 0; i < temp.size(); ++i)
						{
							if(!implication.contains(temp.get(i)))
							{
								res = false;
								break;
							}
						}
					}
					else
					{
						res = false;
					}
				}
			}
		}
		return res;
	}

	public void replaceArgRes(String st, String rp)
	{
		res.replaceArg(st,rp);
	}

	public void display()
	{
		for(int i = 0; i < implication.size(); ++i)
		{
			implication.get(i).display();
			if(!and)
			{
				System.out.print(" | ");
			}
			else
			{
				System.out.print(" & ");	
			}
		}
		res.display();
		System.out.println();
	}

	public void notOp()
	{
		if(isImplication)
		{
			and = !and;
			for(int i = 0; i < implication.size(); ++i)
			{
				implication.get(i).notOp();
			}
		}
		else
		{
			res.notOp();
		}
	}
}

class Literal
{
	String predicate;
	LinkedList<String> constArgs;
	boolean not;

	public Literal()
	{
		not = false;
		predicate = new String();
		constArgs = new LinkedList<String>();
	}


	public Literal(String s)
	{
		Literal temp = convertToLiteral(s);
		not = temp.getNot();
		predicate = new String(temp.getPredicate());
		setConstArgs(temp.getConstArgs());	
	}

	public Literal(Literal l)
	{
		not = l.getNot();
		predicate = new String(l.getPredicate());
		setConstArgs(l.getConstArgs());
	}

	//Getters and Setters
	public boolean getNot()
	{
		return not;
	}

	public String getPredicate()
	{
		return predicate;
	}

	public LinkedList<String> getConstArgs()
	{
		return constArgs;
	}

	public void setNot(boolean b)
	{
		not = b;
	}

	public void setPredicate(String s)
	{
		predicate = s;
	}

	public void setConstArgs(LinkedList<String> s)
	{
		constArgs = new LinkedList<String>();
		for(int i = 0; i < s.size(); ++i)
		{
			constArgs.add(new String(s.get(i)));
		}
	}

	public void addConstArgs(String s)
	{
		constArgs.add(s);
	}

	public void setConstArg(String s, int i)
	{
		constArgs.set(i,new String(s));
	}

	//Functions
	public boolean containsArg(String s)
	{
		for(int i = 0; i < constArgs.size(); ++i)
		{
			if(constArgs.get(i).equals(s))
			{
				return true;
			}
		}
		return false;
	}

	public void replaceArg(String st, String rp)
	{
		for(int i = 0; i < constArgs.size(); ++i)
		{
			if(constArgs.get(i).equals(new String(st)))
			{
				constArgs.set(i,new String(rp));
			}
		}
	}

	public LinkedList<Integer> getIndices(String s)
	{
		LinkedList<Integer> res = new LinkedList<Integer>();
		for(int i = 0; i < constArgs.size(); ++i)
		{
			if(constArgs.get(i).equals(s))
			{
				res.add(i);
			}
		}
		return res;
	}

	@Override
	public boolean equals(Object o)
	{
		if(o == this)
		{
			return true;
		}

		if(!(o instanceof Literal))
		{
			return false;
		}
		Literal l = (Literal)o;
		boolean res = false;
		LinkedList<String> temp = l.getConstArgs();
		if(temp.size() != constArgs.size())
		{
			return res;
		}
		if(predicate.equals(l.getPredicate()))
		{
			if(not == l.getNot())
			{
				res = true;
				for(int i = 0; i < constArgs.size(); ++i)
				{
					if(!(constArgs.get(i).equals(temp.get(i))))
					{
						res = false;
						break;
					}
				}
			}
		}
		return res;	
	}

	public boolean notEquals(Literal l)
	{
		boolean res = false;
		LinkedList<String> temp = l.getConstArgs();
		if(temp.size() != constArgs.size())
		{
			return res;
		}
		if(predicate.equals(l.getPredicate()))
		{
			if(not == !l.getNot())
			{
				res = true;
				for(int i = 0; i < constArgs.size(); ++i)
				{
					if(!(constArgs.get(i).equals(temp.get(i))))
					{
						res = false;
						break;
					}
				}
			}
		}
		return res;
	}

	public void display()
	{
		if(not)
		{
			System.out.print("~");
		}
		System.out.print(predicate + "(");
		for(int i = 0; i < constArgs.size(); ++i)
		{
			if(i != constArgs.size() - 1)
			{
				System.out.print(constArgs.get(i) + ",");
			}
			else
			{
				System.out.print(constArgs.get(i));	
			}
		}
		System.out.print(")");
	}

	public Literal convertToLiteral(String s)
	{
		Literal res = new Literal();
		if(s.charAt(0) == '~')
		{
			res.setNot(true);
			s = s.replace("~","");
		}
		String [] temp = s.split("\\(");
		res.setPredicate(new String(temp[0]));
		temp[1] = temp[1].replace(")","");
		String [] temp2 = temp[1].split(",");
		LinkedList<String> temp3 = new LinkedList<String>();
		for(int i = 0; i < temp2.length; ++i)
		{
			temp3.add(new String(temp2[i]));
		}
		res.setConstArgs(temp3);
		return res;
	}

	public void notOp()
	{
		not = !not;
	}
}