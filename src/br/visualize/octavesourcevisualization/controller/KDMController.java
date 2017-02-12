package br.visualize.octavesourcevisualization.controller;

import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import br.octave.structureKDM.CompilationUnit;
import br.octave.structureKDM.KDMSegment;
import br.octave.structureKDM.codeElement.AbstractCodeElement;
import br.octave.structureKDM.codeElement.ActionElement;
import br.octave.structureKDM.codeElement.CallableUnit;
import br.octave.structureKDM.model.CodeModel;

public class KDMController {

	private int qtdDependencies = 0;
	private KDMSegment segment;
	private JsonArray arrayDependenciesJson = null;

	public String getRelationsCompilationsUnit(KDMSegment segment){
		this.segment = segment;
		List<CompilationUnit> compilations = ((CodeModel)segment.getModels().get(0)).getModules();
		Iterator<CompilationUnit> itr = compilations.iterator();
		
		JsonArray arrayJson = new JsonArray();
		
		while(itr.hasNext()){
			CompilationUnit compilation = itr.next();
			JsonObject jsonCompilation = new JsonObject();
			jsonCompilation.addProperty("name", compilation.getName());
			getDependenciesFromCompilationUnit(compilation);
			jsonCompilation.addProperty("size", this.qtdDependencies);
			jsonCompilation.add("imports", this.arrayDependenciesJson);
			arrayJson.add(jsonCompilation);
		}
		JsonObject jsonParent = new JsonObject();
		jsonParent.add("dados", arrayJson);
		
		return jsonParent.getAsString();
	}

	private void getDependenciesFromCompilationUnit(CompilationUnit compilation) {
		this.qtdDependencies = 0;
		this.arrayDependenciesJson = null;
		List<AbstractCodeElement> list = compilation.getCodeElements();
		Iterator<AbstractCodeElement> itr = list.get(0).getCodeElements().iterator();
		while(itr.hasNext()){
			searchCallElement(itr.next());
		}
	}
	
	private void searchCallElement(AbstractCodeElement element){
		if(element instanceof ActionElement){
			ActionElement actElement = (ActionElement)element;
			if(actElement.getKind().equals("call")){
				ActionElement callNameElement = (ActionElement)actElement.getCodeElements().get(0);
				String name = callNameElement.getName();
				String[] names = searchCallableUnit(name);
				for(int x=0; x< names.length; x++){
					if(this.arrayDependenciesJson == null){
						this.arrayDependenciesJson = new JsonArray();
					}
					if(names[x] != null){
					    this.arrayDependenciesJson.add(names[x]);
					    this.qtdDependencies++;
					}
				}
			}
		}
		if(element.getCodeElements() != null){
			if(element.getCodeElements().size() > 0){
				List<AbstractCodeElement> list = element.getCodeElements();
				Iterator<AbstractCodeElement> itr = list.iterator();
				while(itr.hasNext()){
					searchCallElement(itr.next());
				}
			}
		}
	}

	private String[] searchCallableUnit(String name){
		String[] names = new String[10]; 
		int indice = 0;
		List<CompilationUnit> compilations = ((CodeModel)segment.getModels().get(0)).getModules();
		Iterator<CompilationUnit> itr = compilations.iterator();
		
		while(itr.hasNext()){
			CompilationUnit compilation = itr.next();
			List<AbstractCodeElement> listBlock = compilation.getCodeElements();
			AbstractCodeElement block = listBlock.get(0);
			
			List<AbstractCodeElement> elements = block.getCodeElements();
			Iterator<AbstractCodeElement> itrElementsBlock = elements.iterator();
			
			while(itrElementsBlock.hasNext()){
				AbstractCodeElement element = itrElementsBlock.next();
				if(element instanceof CallableUnit){
				    CallableUnit callable = (CallableUnit)element;
				    if((callable.getKind().equals(name))||(name.equals("["+callable.getKind()+"]"))){
				    	names[indice] = compilation.getName();
				    	indice++;
				    }
				}
			}
		}
		return names;
	}

	public String getCompilationUnitWithCallables(KDMSegment segment){
		List<CompilationUnit> compilations = ((CodeModel)segment.getModels().get(0)).getModules();
		Iterator<CompilationUnit> itr = compilations.iterator();
		
		JsonObject jsonParent = new JsonObject();
		
		JsonArray arrayJson = new JsonArray();
		
		while(itr.hasNext()){
			CompilationUnit compilation = itr.next();
			JsonArray arrayCallables = getJsonCallables(compilation);
			if(arrayCallables.size() > 0){
				JsonObject jsonCompilation = new JsonObject();
				jsonCompilation.add("children", arrayCallables);
				jsonCompilation.addProperty("name", compilation.getName());
				arrayJson.add(jsonCompilation);
			}
		}
		jsonParent.add("children",arrayJson);
		jsonParent.addProperty("name", segment.getName());
		return jsonParent.getAsString();
	}
	
	private JsonArray getJsonCallables(CompilationUnit compilation){
		JsonArray arrayJson = new JsonArray();
		
		List<AbstractCodeElement> listBlock = compilation.getCodeElements();
		AbstractCodeElement block = listBlock.get(0);
		
		List<AbstractCodeElement> elements = block.getCodeElements();
		Iterator<AbstractCodeElement> itr = elements.iterator();
		
		while(itr.hasNext()){
			AbstractCodeElement element = itr.next();
			if(element instanceof CallableUnit){
			    JsonObject jsonCallable = new JsonObject();
			    CallableUnit callable = (CallableUnit)element;
			    jsonCallable.addProperty("name", callable.getKind());
			    jsonCallable.addProperty("size", getSizeCallable(callable));
			    arrayJson.add(jsonCallable);
			}
		}
		return arrayJson;
	}

	private int getSizeCallable(CallableUnit callable) {
		List<AbstractCodeElement> list = callable.getCodeElements();
		AbstractCodeElement block = list.get(0);
		return block.getCodeElements().size();
	}
	
}
