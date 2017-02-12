package br.visualize.octavesourcevisualization.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.lightcouch.CouchDbClient;
import org.lightcouch.DesignDocument;
import org.lightcouch.DesignDocument.MapReduce;
import org.lightcouch.NoDocumentException;
import org.lightcouch.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class KDMOctaveDAO {

	private CouchDbClient dbClient;

	public void save(JsonObject kdmJson, String nameProject){
		Map<String, Object> map = new HashMap<>();
		map.put("_id", nameProject);
		map.put("kdmOctave", kdmJson);
		
		dbClient = new CouchDbClient();
		dbClient.save(map);
		dbClient.shutdown();
	}

	public String getTreemapView(){
		dbClient = new CouchDbClient();
		View view = null;
		ArrayList<JsonObject> list = null;
		try{
			view = dbClient.view("octave/treemap");
			if(view != null)
			    list = (ArrayList<JsonObject>) view.key("children").query(JsonObject.class);
		}catch(NoDocumentException e){
			return "É preciso criar a view octave/treemap";
		}finally{
			dbClient.shutdown();
		}

		JsonArray lista = null;
		if((list != null)&&(list.size() > 0))
		    lista = list.get(0).getAsJsonArray("value");
		
		JsonObject dados = null;
		if(lista != null){
		    dados = new JsonObject();
			dados.add("children", lista);
		}

		if(dados == null)
			return "É preciso importar o projeto antes de visualiza-lo.";

		return dados.toString();
	}

	public String getRelationsView(){
		dbClient = new CouchDbClient();
		View view = null;
		ArrayList<JsonObject> list = null;
		try{
			view = dbClient.view("octave/relations");
			if(view != null)
			    list = (ArrayList<JsonObject>) view.key("dados").query(JsonObject.class);
		}catch(NoDocumentException e){
			return "É preciso criar a view octave/relations";
		}finally{
			dbClient.shutdown();
		}

		JsonArray lista = null;
		if((list != null)&&(list.size() > 0))
		    lista = list.get(0).getAsJsonArray("value");
		
		JsonObject dados = null;
		if(lista != null){
		    dados = new JsonObject();
			dados.add("dados", lista);
		}

		if(dados == null)
			return "É preciso importar o projeto antes de visualiza-lo.";

		return dados.toString();
	}

	public void getAllDocuments(){
		dbClient = new CouchDbClient();
	}

	public void criaView(){
		DesignDocument designDocument = new DesignDocument();
		designDocument.setId("_design/octave");
		designDocument.setLanguage("javascript");

		MapReduce get_numberOfSeasonsMR = new MapReduce();
		get_numberOfSeasonsMR.setMap(
		  "function(doc) {"+
                 "var imports = [];"+
                 "var lista = [];"+
                 "var cont = 0, contLista = 0;"+
                 "walk(doc);"+
                 "function walk(subdoc) {"+
                  "   if(subdoc.map){"+
                   "      if(subdoc.map.type){"+
                    "         if(subdoc.map.name){"+
                     "            if(subdoc.map.type == \'kdm:Segment\'){"+
                      "               if (subdoc.map.name == \'DeepLearnToolbox\'){"+
                       "                  for (var i in subdoc.map.model.myArrayList[0].map.codeElement.myArrayList) {"+
                        "                     walk(subdoc.map.model.myArrayList[0].map.codeElement.myArrayList[i].map.codeElement.myArrayList[0]);"+
                         "lista[contLista] = {\"name\":subdoc.map.model.myArrayList[0].map.codeElement.myArrayList[i].map.name, \"size\":cont, \"imports\": imports};"+
                          "contLista++;"+
                          "                   imports = [];"+
                           "                  cont = 0;"+
                            "             }"+
                             "emit(\"dados\",lista);"+
                             "        }"+
                              "   }else{"+
                               "      if(subdoc.map){"+
                                "         if(subdoc.map.codeElement){"+
                                 "            if(subdoc.map.codeElement.myArrayList){"+
                                  "               for(var j in subdoc.map.codeElement.myArrayList){"+
                                   "                  if(subdoc.map.codeElement.myArrayList[j].map.kind){"+
                                    "                     if(subdoc.map.codeElement.myArrayList[j].map.kind == \'call\'){"+
                                     "                        if(subdoc.map.codeElement.myArrayList[j].map.codeElement.myArrayList[0].map.name!=\'indirect expression\'){"+
                                      "                           if(subdoc.map.codeElement.myArrayList[j].map.codeElement.myArrayList[0].map.name!='call'){"+
                                       "                              var compilation = searchCallable(subdoc.map.codeElement.myArrayList[j].map.codeElement.myArrayList[0].map.name);"+
                                        "                             if(compilation != \"notExist\"){"+
                                         "                                imports[cont] = compilation;"+
                                          "                                cont++;"+
                                           "                          }"+
                                         "                        }"+
                                          "                   }"+
                                           "              }"+
                                            "         }"+
                                             "        walk(subdoc.map.codeElement.myArrayList[j]);"+
                                              "   }"+
                                             "}"+
                                         "}"+
                                     "}"+
                                 "}"+
                             "}"+
                         "}"+
                     "}"+
                 "}"+
                 "function searchCallable(call) {"+
                 "        for (var i in doc.map.model.myArrayList[0].map.codeElement.myArrayList) {"+
                 "            var compilation = doc.map.model.myArrayList[0].map.codeElement.myArrayList[i];"+
                 "            var elementsBlock = compilation.map.codeElement.myArrayList[0].map.codeElement.myArrayList;"+
                 "            for(var j in elementsBlock){"+
                 "                if(elementsBlock[j].map.type == \"code:CallableUnit\"){"+
                 "                    if(elementsBlock[j].map.name == call){"+
                 "                        return compilation.map.name;"+
                 "                    }"+
                 "                }"+
                 "            }"+
                 "        }"+
                 "        return \"notExist\";"+
                 "    }"+
             "}");
		
		Map<String, MapReduce> view = new HashMap<>();
		view.put("relations", get_numberOfSeasonsMR);
		get_numberOfSeasonsMR = new MapReduce();
		get_numberOfSeasonsMR.setMap("function(doc) {"+
    "var listaCompilations = [];"+
    "var indiceCompilations = 0;"+
    "if(doc.map){"+
        "if(doc.map.type){"+
            "if(doc.map.name){"+
                "if(doc.map.type == 'kdm:Segment'){"+
                    "if (doc.map.name == 'DeepLearnToolbox'){"+
                        "for (var i in doc.map.model.myArrayList[0].map.codeElement.myArrayList) {"+
                            "var compilationUnit = doc.map.model.myArrayList[0].map.codeElement.myArrayList[i].map;"+
                            "var listaCallables = buscaCallables(compilationUnit.codeElement.myArrayList[0]);"+
                            "listaCompilations[indiceCompilations] = {\"name\":compilationUnit.name,\"children\":listaCallables};"+
                            "indiceCompilations++;"+
                        "}"+
                        "emit(\"children\",listaCompilations);"+
                    "}"+
                "}"+
            "}"+
        "}"+
    "}"+
    "function buscaCallables(blockCompilation){"+
        "var listaCallables = [];"+
        "var indiceCallables = 0;"+
        "if(blockCompilation.map){"+
            "if(blockCompilation.map.codeElement){"+
                "if(blockCompilation.map.codeElement.myArrayList){"+
                    "for(var j in blockCompilation.map.codeElement.myArrayList){"+
                        "if(blockCompilation.map.codeElement.myArrayList[j].map.type){"+
                            "if(blockCompilation.map.codeElement.myArrayList[j].map.type == 'code:CallableUnit'){"+
                                "var size = getSizeCallable(blockCompilation.map.codeElement.myArrayList[j].map.codeElement.myArrayList[1]);"+
                                "listaCallables[indiceCallables] = {\"name\":blockCompilation.map.codeElement.myArrayList[j].map.name, \"size\":size};"+
                                "indiceCallables++;"+
                            "}"+
                        "}"+
                    "}"+
                    "return listaCallables;"+
                "}"+
            "}"+
        "}"+
    "}"+
    "function getSizeCallable(blockCallable){"+
        "var qtdCodigo = 0;"+
        "for(var k in blockCallable.map.codeElement.myArrayList){"+
            "qtdCodigo++;"+
        "}"+
        "return qtdCodigo;"+
    "}"+
"}");
		view.put("treemap", get_numberOfSeasonsMR);
		designDocument.setViews(view);
		dbClient = new CouchDbClient();
		dbClient.design().synchronizeWithDb(designDocument);
		dbClient.shutdown();
	}

}