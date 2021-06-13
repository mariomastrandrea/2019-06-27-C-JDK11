package it.polito.tdp.crimes;

import java.net.URL;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import it.polito.tdp.crimes.model.Couple;
import it.polito.tdp.crimes.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

public class CrimesController 
{
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ComboBox<String> boxCategoria;

    @FXML
    private ComboBox<LocalDate> boxGiorno;

    @FXML
    private Button btnAnalisi;

    @FXML
    private ComboBox<Couple<String>> boxArco;

    @FXML
    private Button btnPercorso;

    @FXML
    private TextArea txtResult;
    
	private Model model;


    @FXML
    void doCreaGrafo(ActionEvent event) 
    {
    	String selectedCategory = this.boxCategoria.getValue();
    	LocalDate selectedDate = this.boxGiorno.getValue();
    	
    	if(selectedCategory == null || selectedDate == null)
    	{
    		this.txtResult.setText("Errore: devi prima selezionare una categoria di reato e un giorno dai rispettivi menù a tendina");
    		return;
    	}
    	
    	this.model.createGraph(selectedCategory, selectedDate);
    	
    	int numVertices = this.model.getNumVertices();
    	int numEdges = this.model.getNumEdges();
    	
    	Collection<Couple<String>> edgesLowerThanAvgWeight = this.model.getEdgesLowerThanAvgWeight();
    	
    	String couplesOutput = edgesLowerThanAvgWeight.isEmpty() ? 
    									"(nessuno)" : this.print(edgesLowerThanAvgWeight);
    	
    	String output = String.format("Grafo creato:\n# Vertici: %d\n# Archi: %d\n\nArchi il cui peso è inferiore al peso mediano:\n%s",
    			numVertices, numEdges, couplesOutput);
    	
    	this.txtResult.setText(output);
    	
    	//updating view
    	this.boxArco.getItems().clear();
    	this.boxArco.getItems().addAll(edgesLowerThanAvgWeight);
    }
    
    private <T> String print(Collection<T> rows)
    {
    	StringBuilder sb = new StringBuilder();
    	
    	int count = 0;
    	for(var row : rows)
    	{
    		if(sb.length() > 0)
    			sb.append("\n");
    		
    		sb.append(++count).append(") ").append(row.toString());
    	}
    	
    	return sb.toString();
    }

    @FXML
    void doCalcolaPercorso(ActionEvent event) 
    {
    	Couple<String> selectedCouple = this.boxArco.getValue();
    	
    	if(selectedCouple == null)
    	{
    		this.txtResult.setText("Errore: devi prima selezionare un arco dal menù a tendina");
    		return;
    	}
    	
    	String vertex1 = selectedCouple.getFirst();
    	String vertex2 = selectedCouple.getSecond();
    	
    	this.model.computeMaxWeightPathBetween(vertex1, vertex2);
    	
    	List<String> path = this.model.getMaxWeightPath();
    	int maxWeight = this.model.getMaxWeight();
    	
    	String output = this.printBestPath(path, maxWeight, vertex1, vertex2);
    	this.txtResult.setText(output);
    }

    private String printBestPath(List<String> path, int maxWeight, String vertex1, String vertex2)
	{
		StringBuilder sb = new StringBuilder();
		
		for(String v : path)
		{
			if(sb.length() > 0)
				sb.append(", ");
			
			sb.append(v);
		}
		
		return String.format("Il cammino aciclico di peso massimo tra %s e %s è:\n{%s}\n\nPeso massimo: %d",
				vertex1, vertex2, sb.toString(), maxWeight);
	}

	@FXML
    void initialize() 
    {
        assert boxCategoria != null : "fx:id=\"boxCategoria\" was not injected: check your FXML file 'Crimes_2019-06-27-C.fxml'.";
        assert boxGiorno != null : "fx:id=\"boxGiorno\" was not injected: check your FXML file 'Crimes_2019-06-27-C.fxml'.";
        assert btnAnalisi != null : "fx:id=\"btnAnalisi\" was not injected: check your FXML file 'Crimes_2019-06-27-C.fxml'.";
        assert boxArco != null : "fx:id=\"boxArco\" was not injected: check your FXML file 'Crimes_2019-06-27-C.fxml'.";
        assert btnPercorso != null : "fx:id=\"btnPercorso\" was not injected: check your FXML file 'Crimes_2019-06-27-C.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Crimes_2019-06-27-C.fxml'.";
    }
    
    public void setModel(Model model) 
    {
    	this.model = model;
    	
    	List<String> allOffenseCategories = this.model.getAllOffenseCategories();
    	this.boxCategoria.getItems().clear();
    	this.boxCategoria.getItems().addAll(allOffenseCategories);
    	
    	List<LocalDate> allReportedDates = this.model.getAllReportedDates();
    	this.boxGiorno.getItems().clear();
    	this.boxGiorno.getItems().addAll(allReportedDates);
    }
}

