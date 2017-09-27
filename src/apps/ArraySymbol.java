package apps;
public class ArraySymbol
{
	public String name;
	public int[] values;
	public ArraySymbol(String name)
	{
		this.name = name;
		values = null;
	}
	public String toString()
	{
		if(values == null || values.length == 0)
		{
			return name + "=[ ]";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append("=[");
		sb.append(values[0]);
		for(int i = 1; i < values.length; i++)
		{
			sb.append(',');
			sb.append(values[i]);
		}
		sb.append(']');
		return sb.toString();
	}
	public boolean equals(Object o)
	{
		if(o == null || !(o instanceof ArraySymbol))
		{
			return false;
		}
		ArraySymbol as = (ArraySymbol) o;
		return name.equals(as.name);
	}
}
