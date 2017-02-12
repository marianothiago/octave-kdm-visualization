package br.visualize.octavesourcevisualization.controller;

import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import br.octave.structureKDM.CompilationUnit;
import br.octave.structureKDM.KDMSegment;
import br.octave.structureKDM.ParameterUnit;
import br.octave.structureKDM.Signature;
import br.octave.structureKDM.codeElement.AbstractCodeElement;
import br.octave.structureKDM.codeElement.ActionElement;
import br.octave.structureKDM.codeElement.BlockUnit;
import br.octave.structureKDM.codeElement.CallableUnit;
import br.octave.structureKDM.codeElement.StorableUnit;
import br.octave.structureKDM.inventoryElement.AbstractInventoryElement;
import br.octave.structureKDM.inventoryElement.Directory;
import br.octave.structureKDM.inventoryElement.SourceFile;
import br.octave.structureKDM.model.CodeModel;
import br.octave.structureKDM.model.InventoryModel;
import br.octave.structureKDM.model.KDMModel;

public class KDMParseJson {

	private String nameProject;
	private KDMSegment segment;
	private JsonObject jsonSignatureElement;

	public KDMParseJson(String nameProject, KDMSegment segment){
		this.nameProject = nameProject;
		this.segment = segment;
	}

	public JsonObject createJSON() throws Exception {
		JsonObject jsonSegment = new JsonObject();
		jsonSegment.addProperty("type", "kdm:Segment");
		jsonSegment.addProperty("name",this.nameProject);
		if(this.segment != null){
			JsonArray models = new JsonArray();
			createCodeModel(models);
			createInventoryModel(models);
			jsonSegment.add("model",models);
		}
		return jsonSegment;
	}

	private void createCodeModel(JsonArray jsonModels){
		JsonObject jsonCodeModel = new JsonObject();
		jsonCodeModel.addProperty("type", "code:CodeModel");
		jsonCodeModel.addProperty("name", this.nameProject);
		createCompilationUnit(jsonCodeModel);
		jsonModels.add(jsonCodeModel);
	}

    private void createInventoryModel(JsonArray jsonModels){
    	JsonObject jsonInventoryModel = new JsonObject();
		jsonInventoryModel.addProperty("type", "source:InventoryModel");
		createInventoryElements(jsonInventoryModel);
		jsonModels.add(jsonInventoryModel);
	}

    private void createCompilationUnit(JsonObject jsonCodeModel){
    	JsonArray compilationsUnit = new JsonArray();
    	List<KDMModel> listModels = this.segment.getModels();
    	if((listModels != null)&&(listModels.size() > 0)){
	    	CodeModel code = (CodeModel)listModels.get(0);
	    	List<CompilationUnit> listCompilationUnit = (List<CompilationUnit>)code.getModules();
	    	if((listCompilationUnit != null)&&(listCompilationUnit.size() > 0)){
		    	Iterator<CompilationUnit> itr = listCompilationUnit.iterator();
		    	while(itr.hasNext()){
		    		CompilationUnit compilationUnit = (CompilationUnit)itr.next();
		    		JsonObject jsonCompilation = new JsonObject();
		        	jsonCompilation.addProperty("type", "code:CompilationUnit");
		        	jsonCompilation.addProperty("name", compilationUnit.getName());
		        	createCompilationUnitElements(compilationUnit, jsonCompilation);
		        	compilationsUnit.add(jsonCompilation);
		    	}
	    		jsonCodeModel.add("codeElement", compilationsUnit);
	    	}
    	}
    }

    private void createInventoryElements(JsonObject jsonInventoryModel){
    	JsonArray jsonInventoryElements = new JsonArray();
    	List<KDMModel> models = this.segment.getModels();
    	if((models != null)&&(models.size() > 1)){
	    	InventoryModel inventory = (InventoryModel)models.get(1);
	    	List<AbstractInventoryElement> inventoryElements = inventory.getInventoryElements();
	    	if((inventoryElements != null)&&(inventoryElements.size() > 0)){
		    	Iterator<AbstractInventoryElement> itr = inventoryElements.iterator();
		    	while(itr.hasNext()){
		    		AbstractInventoryElement element = itr.next();
		    		JsonObject inventoryElement = new JsonObject();
		    		if(element instanceof Directory){
		    			Directory directory = (Directory)element;
		    			inventoryElement.addProperty("type", "source:Directory");
		    			inventoryElement.addProperty("path",directory.getPath());
		    			inventoryElement.addProperty("language","octave");
		    		}else{
		    			SourceFile file = (SourceFile)element;
		    			inventoryElement.addProperty("type", "source:SourceFile");
		    			inventoryElement.addProperty("name",file.getName());
		    			inventoryElement.addProperty("path",file.getPath());
		    			inventoryElement.addProperty("language","octave");
		    		}
		    		jsonInventoryElements.add(inventoryElement);
		    	}
		    	jsonInventoryModel.add("inventoryElement", jsonInventoryElements);
	    	}
    	}
    }

    private void createCompilationUnitElements(CompilationUnit compilation, JsonObject jsonCompilation){
    	JsonArray jsonElements = new JsonArray();
    	List<AbstractCodeElement> codeElements = compilation.getCodeElements();
    	Iterator<AbstractCodeElement> itr = codeElements.iterator();
    	while(itr.hasNext()){
    		AbstractCodeElement codeElement = (AbstractCodeElement)itr.next();
    		createElements(codeElement, jsonElements);
    	}
    	jsonCompilation.add("codeElement", jsonElements);
    }

    public void createElements(AbstractCodeElement codeElement, JsonArray jsonElements){
    	JsonObject jsonElement = getJsonElementFromCodeElement(codeElement);
    	jsonElements.add(jsonElement);
		List<AbstractCodeElement> codeElements = codeElement.getCodeElements();
		if((codeElements != null)&&(codeElements.size()>0)){
			JsonArray jsonElements2 = new JsonArray();
			if(jsonSignatureElement != null){
				jsonElements2.add(jsonSignatureElement);
				jsonSignatureElement = null;
			}
	    	Iterator<AbstractCodeElement> itr = codeElements.iterator();
	    	while(itr.hasNext()){
	    		AbstractCodeElement codeElementInto = (AbstractCodeElement)itr.next();
	    		createElements(codeElementInto, jsonElements2);
	    	}
	    	jsonElement.add("codeElement", jsonElements2);
		}
    }

    private JsonObject getJsonElementFromCodeElement(AbstractCodeElement codeElement){
    	JsonObject jsonElement = new JsonObject();
    	if(codeElement instanceof ActionElement){
    		ActionElement action = (ActionElement)codeElement;
    		jsonElement.addProperty("type","action:ActionElement");
    		jsonElement.addProperty("kind", action.getKind());
    		jsonElement.addProperty("name", action.getName());
		}
        if(codeElement instanceof CallableUnit){
        	CallableUnit callable = (CallableUnit)codeElement;
        	jsonElement.addProperty("type", "code:CallableUnit");
        	jsonElement.addProperty("name", callable.getKind());
        	Signature signature = callable.getSignature();
        	jsonSignatureElement = new JsonObject();
        	jsonSignatureElement.addProperty("type", "code:Signature");
        	jsonSignatureElement.addProperty("name",callable.getKind());
   			if(signature.getParameters() != null){
   				List<ParameterUnit> listParameters = signature.getParameters();
	   			Iterator<ParameterUnit> parameters = listParameters.iterator();
	   			if(parameters != null){
	   				JsonArray jsonParameters = new JsonArray();
		   			while(parameters.hasNext()){
		   				ParameterUnit parameterUnit = parameters.next();
		   				JsonObject jsonParameterUnit = new JsonObject();
		   				jsonParameterUnit.addProperty("kind", parameterUnit.getKind());
		   				jsonParameterUnit.addProperty("name", parameterUnit.getName());
		   				jsonParameterUnit.addProperty("pos", String.valueOf(parameterUnit.getPos()));
		   				jsonParameters.add(jsonParameterUnit);
		   			}
		   			if(listParameters.size()>0)
		   			    jsonSignatureElement.add("parameterUnit", jsonParameters);
	   			}
   			}
		}
        if(codeElement instanceof StorableUnit){
        	StorableUnit storable = (StorableUnit)codeElement;
        	jsonElement.addProperty("type","code:StorableUnit");
        	jsonElement.addProperty("kind",storable.getKind());
        }
        if(codeElement instanceof BlockUnit){
        	BlockUnit block = (BlockUnit)codeElement;
        	jsonElement.addProperty("type","action:BlockUnit");
        	jsonElement.addProperty("kind",block.getKind());
        	jsonElement.addProperty("name",block.getName());
        }
    	return jsonElement;
    }
	
}
