package apps;
public class ScalarSymbol
{
	public String name;
	public int value;
	public ScalarSymbol(String name)
	{
		this.name = name;
		value = 0;
	}
	public String toString()
	{
		return name + "=" + value;
	}
	public boolean equals(Object o)
	{
		if(o == null || !(o instanceof ScalarSymbol))
		{
			return false;
		}
		ScalarSymbol ss = (ScalarSymbol) o;
		return name.equals(ss.name);
	}
}
