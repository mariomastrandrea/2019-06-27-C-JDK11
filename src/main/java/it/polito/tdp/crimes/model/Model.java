package it.polito.tdp.crimes.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import it.polito.tdp.crimes.db.EventsDao;

public class Model 
{
	private final EventsDao dao;
	private Graph<String, DefaultWeightedEdge> graph;
	private List<LocalDate> allReportedDates;
	private List<String> allOffenseCategories;
	
	private List<String> maxWeightPath;
	private int maxWeight;
	
	
	public Model()
	{
		this.dao = new EventsDao();
	}
	
	public List<LocalDate> getAllReportedDates()
	{
		if(this.allReportedDates == null)
			this.allReportedDates = this.dao.getAllDates();
		
		return this.allReportedDates;
	}
	
	public List<String> getAllOffenseCategories()
	{
		if(this.allOffenseCategories == null)
			this.allOffenseCategories = this.dao.getAllOffenseCategories();
		
		return this.allOffenseCategories;
	}

	public void createGraph(String selectedCategory, LocalDate selectedDate)
	{
		this.graph = GraphTypeBuilder.<String,DefaultWeightedEdge>undirected()
									.allowingMultipleEdges(false)
									.allowingSelfLoops(false)
									.weighted(true)
									.edgeClass(DefaultWeightedEdge.class)
									.buildGraph();
		// add vertices
		List<String> offenseTypes = this.dao.getOffenseTypesOf(selectedCategory, selectedDate);
		Graphs.addAllVertices(this.graph, offenseTypes);
		
		// add edges
		Collection<Couple<String>> offenseTypesCouples =
				this.dao.getOffenseTypesCouplesOf(selectedCategory, selectedDate);
		
		for(var couple : offenseTypesCouples)
		{
			String v1 = couple.getFirst();
			String v2 = couple.getSecond();
			int weight = couple.getWeight();
			
			Graphs.addEdge(this.graph, v1, v2, weight);
		}
	}

	public int getNumVertices()
	{
		return this.graph.vertexSet().size();
	}

	public int getNumEdges()
	{
		return this.graph.edgeSet().size();
	}

	public Collection<Couple<String>> getEdgesLowerThanAvgWeight()
	{
		int minWeight = Integer.MAX_VALUE;
		int maxWeight = Integer.MIN_VALUE;
		
		for(var edge : this.graph.edgeSet())
		{
			int weight = (int)this.graph.getEdgeWeight(edge);
			
			if(weight < minWeight)
				minWeight = weight;
			
			if(weight > maxWeight)
				maxWeight = weight;
		}
		
		double avgWeight = (double)(minWeight + maxWeight) / 2.0;
				
		Collection<Couple<String>> edgesLowerThanAvgWeight = 
							this.graph.edgeSet().stream()
							.filter(e -> this.graph.getEdgeWeight(e) < avgWeight)
							.map(e -> 
							{
								String v1 = this.graph.getEdgeSource(e);
								String v2 = this.graph.getEdgeTarget(e);
								int weight = (int)this.graph.getEdgeWeight(e);
								return new Couple<String>(v1, v2, weight);
							})
							.collect(Collectors.toList());
		
		return edgesLowerThanAvgWeight;
	}

	public void computeMaxWeightPathBetween(String vertex1, String vertex2)
	{
		this.maxWeightPath = new ArrayList<>();
		this.maxWeight = Integer.MIN_VALUE;
		
		List<String> partialSolution = new ArrayList<>();
		partialSolution.add(vertex1);
		
		Set<String> partialSolutionSet = new HashSet<>();
		partialSolutionSet.add(vertex1);
		
		this.recursiveMaxWeightPathComputation(partialSolution, partialSolutionSet, vertex2, 0);
	}
	
	private void recursiveMaxWeightPathComputation(List<String> partialSolution, 
			Set<String> partialSolutionSet,	//to reduce time complexity (in .remove())
			String destination,	int currentWeight)
	{
		String lastVertex = partialSolution.get(partialSolution.size() - 1);
		
		if(lastVertex.equals(destination))
		{
			if(currentWeight > this.maxWeight)
			{
				this.maxWeight = currentWeight;
				this.maxWeightPath = new ArrayList<>(partialSolution);
			}
			return;
		}
		
		var nextEdges = this.graph.edgesOf(lastVertex);
		
		for(var edge : nextEdges)
		{
			int weight = (int)this.graph.getEdgeWeight(edge);
			String nextVertex = Graphs.getOppositeVertex(this.graph, edge, lastVertex);
			
			if(partialSolutionSet.contains(nextVertex)) continue;
			
			partialSolution.add(nextVertex);
			partialSolutionSet.add(nextVertex);
			
			this.recursiveMaxWeightPathComputation(partialSolution, partialSolutionSet, 
					destination, currentWeight + weight);
			
			//backtracking
			partialSolution.remove(partialSolution.size() - 1);
			partialSolutionSet.remove(nextVertex);
		}
	}

	public List<String> getMaxWeightPath()
	{
		return this.maxWeightPath;
	}

	public int getMaxWeight()
	{
		return this.maxWeight;
	}
}
