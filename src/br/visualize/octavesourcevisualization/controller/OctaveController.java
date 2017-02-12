package br.visualize.octavesourcevisualization.controller;

import java.io.File;

import br.octave.astOctaveToKDM.octaveKDM.OctaveElementsKDM;
import br.octave.astOctaveToKDM.octaveKDM.ParseOctaveASTToKDM;
import br.octave.structureKDM.KDMSegment;

public class OctaveController {

	public static KDMSegment loadSegment(File gitRepo, String projectName) throws Exception{
		ParseOctaveASTToKDM oct = new ParseOctaveASTToKDM();
		OctaveElementsKDM kdm = oct.loadProject(gitRepo,projectName);
		return kdm.getSegment();
	}
}