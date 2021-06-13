package it.polito.tdp.crimes.model;

public class Couple<T>
{
	private final T first;
	private final T second;
	private final int weight;
	
	
	public Couple(T first, T second, int weight)
	{
		this.first = first;
		this.second = second;
		this.weight = weight;
	}

	public T getFirst()
	{
		return this.first;
	}

	public T getSecond()
	{
		return this.second;
	}

	public int getWeight()
	{
		return this.weight;
	}
	
	@Override
	public String toString()
	{
		return String.format("%s  -  %s  (%d)",	this.first.toString(), 
				this.second.toString(), this.weight);
	}
}
