package br.visualize.octavesourcevisualization.controller;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

public class XMIParse {

	private DocumentBuilderFactory dbf;
	private DocumentBuilder docBuilder;
	// private Document doc = docBuilder.parse(new File("arquivo.xml"));
	private Document doc;
	private Element rootElement;
	private String nameProject;
	private KDMSegment segment;

	public XMIParse(String nameProject, KDMSegment segment)
			throws ParserConfigurationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException,
			SecurityException, ClassNotFoundException, JDOMException, IOException {
		this.nameProject = nameProject;
		this.segment = segment;
		this.dbf = DocumentBuilderFactory.newInstance();
		this.docBuilder = this.dbf.newDocumentBuilder();
		this.doc = docBuilder.newDocument();
	}

//	private void generateJson(String content, String name){
//		FileWriter writeFile = null;
//		
//		try{
//			writeFile = new FileWriter(name+".json");
//			//Escreve no arquivo conteudo do Objeto JSON
//			writeFile.write(content);
//			writeFile.close();
//		}catch(IOException e){
//			e.printStackTrace();
//		}
//	}

	public DOMSource createXMI() throws Exception {
		createRootNode(this.nameProject);
		createCodeModel();
		createInventoryModel();
		generateXMIFile();
		System.out.println("Fim");
		return new DOMSource(doc);
	}

	private void createRootNode(String nameProject) {
		this.rootElement = this.doc.createElement("kdm:Segment");
		this.rootElement.setAttribute("xmi:version", "2.4.1");
		this.rootElement.setAttribute("xmlns:xmi", "http://www.omg.org/spec/XMI/2.4.1");
		this.rootElement.setAttribute("xmlns:action", "http://schema.omg.org/spec/KDM/1.2/action");
		this.rootElement.setAttribute("xmlns:code", "http://schema.omg.org/spec/KDM/1.2/code");
		this.rootElement.setAttribute("xmlns:kdm", "http://schema.omg.org/spec/KDM/1.2/kdm");
		this.rootElement.setAttribute("xmlns:source", "http://schema.omg.org/spec/KDM/1.2/source");
		this.rootElement.setAttribute("name", nameProject);
		this.doc.appendChild(this.rootElement);
	}
	
	private void createCodeModel(){
		Element codeModel = this.doc.createElement("model");
		codeModel.setAttribute("xmi:type", "code:CodeModel");
		codeModel.setAttribute("name", this.nameProject);
		this.rootElement.appendChild(codeModel);
		createCompilationUnit(codeModel);
	}

    private void createInventoryModel(){
    	Element inventoryModel = this.doc.createElement("model");
    	inventoryModel.setAttribute("xmi:type", "source:InventoryModel");
		this.rootElement.appendChild(inventoryModel);
		createInventoryElements(inventoryModel);
	}

    private void createCompilationUnit(Element parent){
    	CodeModel code = (CodeModel)this.segment.getModels().get(0);
    	List<CompilationUnit> listCompilationUnit = (List<CompilationUnit>)code.getModules();
    	Iterator<CompilationUnit> itr = listCompilationUnit.iterator();
    	while(itr.hasNext()){
    		CompilationUnit compilationUnit = (CompilationUnit)itr.next();
    		Element compilation = this.doc.createElement("codeElement");
        	compilation.setAttribute("xmi:type", "code:CompilationUnit");
        	compilation.setAttribute("name", compilationUnit.getName());
        	parent.appendChild(compilation);
        	createCompilationUnitElements(compilationUnit, compilation);
    	}
    }

    private void createInventoryElements(Element parent){
    	InventoryModel inventory = (InventoryModel)this.segment.getModels().get(1);
    	Iterator<AbstractInventoryElement> itr = inventory.getInventoryElements().iterator();
    	while(itr.hasNext()){
    		AbstractInventoryElement element = itr.next();
    		Element inventoryElementChild = this.doc.createElement("inventoryElement");
    		if(element instanceof Directory){
    			Directory directory = (Directory)element;
    			inventoryElementChild.setAttribute("xmi:type", "source:Directory");
    			inventoryElementChild.setAttribute("path",directory.getPath());
    			inventoryElementChild.setAttribute("language","octave");
    		}else{
    			SourceFile file = (SourceFile)element;
    			inventoryElementChild.setAttribute("xmi:type", "source:SourceFile");
    			inventoryElementChild.setAttribute("name",file.getName());
    			inventoryElementChild.setAttribute("path",file.getPath());
    			inventoryElementChild.setAttribute("language","octave");
    		}
    		parent.appendChild(inventoryElementChild);
    	}
    }

    private void createCompilationUnitElements(CompilationUnit compilation, Element parent){
    	List<AbstractCodeElement> codeElements = compilation.getCodeElements();
    	Iterator<AbstractCodeElement> itr = codeElements.iterator();
    	while(itr.hasNext()){
    		AbstractCodeElement codeElement = (AbstractCodeElement)itr.next();
    		createElements(codeElement, parent);
    	}
    }

    public void createElements(AbstractCodeElement codeElement, Element parent){
    	Element element = getXMLElementFromCodeElement(codeElement);
		parent.appendChild(element);
		List<AbstractCodeElement> codeElements = codeElement.getCodeElements();
		if(codeElements != null){
	    	Iterator<AbstractCodeElement> itr = codeElements.iterator();
	    	while(itr.hasNext()){
	    		AbstractCodeElement codeElementInto = (AbstractCodeElement)itr.next();
	    		createElements(codeElementInto, element);
	    	}
		}
    }

    private Element getXMLElementFromCodeElement(AbstractCodeElement codeElement){
    	Element element = this.doc.createElement("codeElement");
    	if(codeElement instanceof ActionElement){
    		ActionElement action = (ActionElement)codeElement;
    		element.setAttribute("xmi:type","action:ActionElement");
    		element.setAttribute("kind", action.getKind());
    		element.setAttribute("name", action.getName());
		}
        if(codeElement instanceof CallableUnit){
        	CallableUnit callable = (CallableUnit)codeElement;
        	element.setAttribute("xmi:type", "code:CallableUnit");
        	element.setAttribute("name", callable.getKind());
        	Signature signature = callable.getSignature();
        	Element elementSignature = this.doc.createElement("codeElement");
   			elementSignature.setAttribute("xmi:type", "code:Signature");
   			elementSignature.setAttribute("name",callable.getKind());
   			element.appendChild(elementSignature);
   			if(signature.getParameters() != null){
	   			Iterator<ParameterUnit> parameters = signature.getParameters().iterator();
	   			if(parameters != null){
		   			while(parameters.hasNext()){
		   				ParameterUnit parameterUnit = parameters.next();
		   				Element elementParameter = this.doc.createElement("parameterUnit");
		   				elementParameter.setAttribute("kind", parameterUnit.getKind());
		   				elementParameter.setAttribute("name", parameterUnit.getName());
		   				elementParameter.setAttribute("pos", String.valueOf(parameterUnit.getPos()));
		   				elementSignature.appendChild(elementParameter);
		   			}
	   			}
   			}
		}
        if(codeElement instanceof StorableUnit){
        	StorableUnit storable = (StorableUnit)codeElement;
        	element.setAttribute("xmi:type","code:StorableUnit");
        	element.setAttribute("kind",storable.getKind());
        }
        if(codeElement instanceof BlockUnit){
        	BlockUnit block = (BlockUnit)codeElement;
        	element.setAttribute("xmi:type","action:BlockUnit");
        	element.setAttribute("kind",block.getKind());
        	element.setAttribute("name",block.getName());
        }
    	return element;
    }

	private void generateXMIFile() throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult console = new StreamResult(System.out);
		StreamResult file = new StreamResult(new File("/tmp/"+this.nameProject+".xml"));
		// faz a transformação dos dados para arquivo e para a console!
		transformer.transform(source, console);
		transformer.transform(source, file);
	}
}