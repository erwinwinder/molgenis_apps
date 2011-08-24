/*
 * Date: December 24, 2010 Template: PluginScreenJavaTemplateGen.java.ftl
 * generator: org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.3
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package plugins.animal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.GenericPlugin;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.DateInput;
import org.molgenis.framework.ui.html.DivPanel;
import org.molgenis.framework.ui.html.IntInput;
import org.molgenis.framework.ui.html.RepeatingPanel;
import org.molgenis.framework.ui.html.SelectInput;
import org.molgenis.framework.ui.html.TextLineInput;
import org.molgenis.pheno.Code;
import org.molgenis.pheno.ObservationTarget;
import org.molgenis.pheno.ObservedValue;
import org.molgenis.protocol.ProtocolApplication;
import org.molgenis.util.Tuple;

import commonservice.CommonService;

public class AddAnimalPlugin extends GenericPlugin
{
	private static final long serialVersionUID = -4185405160313262242L;
	private CommonService ct = CommonService.getInstance();
	
	private List<String> bases = null;

	// inputs
	public SelectInput species = null;
	public SelectInput sex = null;
	public SelectInput animaltype = null;
	public SelectInput source = null;
	public SelectInput background = null;
	public SelectInput gene = null;
	public SelectInput genestate = null;
	public DateInput birthdate = null;
	public DateInput entrydate = null;
	public SelectInput namebase = null;
	public TextLineInput<String> startnumberhelper = null;
	public TextLineInput<String> newnamebase = null;
	public IntInput startnumber = null;
	public IntInput numberofanimals = null;
	public SelectInput actor = null;
	public ActionInput addbutton = null;

	// container that renders whole form as divs (left labels, right inputs)
	public DivPanel containingPanel = null;
	// subpanel to conditionally show the genetic modification questions (background, genotype)
	public DivPanel gmoPanel = null;
	// sub-subpanel to conditionally show the genetic modification questions (gene, genestate)
	public DivPanel genePanel = null;
	// subpanel to conditionally show the custom name questions (base, start number)
	public DivPanel namePanel = null;

	public AddAnimalPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}
	
	public String getCustomHtmlHeaders()
    {
        return "<link rel=\"stylesheet\" style=\"text/css\" href=\"res/css/animaldb.css\">\n" +
               "<script src=\"res/scripts/custom/addanimals.js\" type=\"text/javascript\" language=\"javascript\"></script>\n";
    }

	@Override
	public void reload(Database db)
	{
		try
		{
			ct.setDatabase(db);
			ct.makeObservationTargetNameMap(this.getLogin().getUserId(), false);
			
			bases = ct.getNameBases();
			
			//if (tablePanel == null) {
				populateTablePanel(db);
			//}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			this.getMessages().clear();
			String message = "Something went wrong while loading lists";
			if (e.getMessage() != null)
			{
				message += (": " + e.getMessage());
			}
			this.getMessages().add(new ScreenMessage(message, false));
		}
	}

	@Override
	public void handleRequest(Database db, Tuple request)
	{
		try {
			String action = request.getAction();
			if (action.equals("Add")) {
				containingPanel.setValuesFromRequest(request);
				handleAddRequest(db, request);
			}
		} catch (Exception e) {
			try {
				db.rollbackTx();
			} catch (DatabaseException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			if (e.getMessage() != null) {
				this.getMessages().add(new ScreenMessage(e.getMessage(), false));
			}
		}
	}
	
	private void handleAddRequest(Database db, Tuple request) throws Exception {
		
		int speciesId = 0;
		if (species.getObject() != null) {
			speciesId = Integer.parseInt(species.getObject().toString());
		} else {
			throw(new Exception("No species given - animal(s) not added"));
		}
		
		int sexId = 0;
		if (sex.getObject() != null) {
			sexId = Integer.parseInt(sex.getObject().toString());
		} else {
			throw(new Exception("No sex given - animal(s) not added"));
		}
		
		int sourceId = 0;
		if (source.getObject() != null) {
			sourceId = Integer.parseInt(source.getObject().toString());
		} else {
			throw(new Exception("No source given - animal(s) not added"));
		}
		
		String animalType = null;
		if (animaltype.getObject() != null) {
			animalType = animaltype.getObject().toString();
		} else {
			throw(new Exception("No animal type given - animal(s) not added"));
		}
		
		// GMO info
		int backgroundId = 0;
		List<String> geneList = new ArrayList<String>();
		List<String> genestateList = new ArrayList<String>();
		// User may have filled in fields and then switched the AnimalType from GMO again,
		// so we need to check first what the AnimalType is.
		if (animalType.equals("B. Transgeen dier")) {
			// GMO panel already made visible through JavaScript, now make permanent
			gmoPanel.setHidden(false);
			
			if (background.getObject() != null) {
				backgroundId = Integer.parseInt(background.getObject().toString());
			} else {
				throw(new Exception("No background given - animal(s) not added"));
			}
			// Check gene name/state pairs
			List<String> tmpGeneList = new ArrayList<String>();
			if (gene.getObject() instanceof String) {
				tmpGeneList.add(gene.getValue());
			} else {
				tmpGeneList = (List<String>) gene.getObject();
			}
			List<String> tmpGenestateList = new ArrayList<String>();
			if (genestate.getObject() instanceof String) {
				tmpGenestateList.add(genestate.getValue());
			} else {
				tmpGenestateList = (List<String>) genestate.getObject();
			}
			// Zero or more gene - genestate couples
			if (tmpGeneList != null) {
				int nrOfGenes = 0;
				for (String tmpGene : tmpGeneList) {
					geneList.add(tmpGene);
					genestateList.add(tmpGenestateList.get(nrOfGenes++));
				}
			}
		} else {
			// GMO panel already made invisible through JavaScript, now make permanent
			gmoPanel.setHidden(true);
		}
		
		SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
		
		// Birth date
		String birthDate = null;
		if (!birthdate.getValue().equals("")) {
			birthDate = birthdate.getValue();
		}
		
		// Entry date
		Date entryDate = null;
		if (!entrydate.getValue().equals("")) {
			String entryDateString = entrydate.getValue();
			entryDate = dateOnlyFormat.parse(entryDateString);
		} else {
			throw(new Exception("No entry date given - animal(s) not added"));
		}
						
		// Name
		String nameBase = null;
		int startNumber = -1;
		if (namebase.getObject() != null) {
			nameBase = namebase.getObject().toString();
			if (nameBase.equals("New")) {
				if (newnamebase.getObject() != null) {
					nameBase = newnamebase.getObject().toString();
				} else {
					nameBase = "";
				}	
			}
		} else {
			 nameBase = "";
		}
		if (startnumber.getObject() != null) {
			// TODO: Find out why HtmlInput<E>'s getObject() returns a String object and not an
			// Integer one, as expected!
			startNumber = Integer.parseInt(startnumber.getValue());
		} else {
			startNumber = 1; // standard start at 1
		}
		
		// Number of animals
		int nrOfAnimals = 1;
		if (numberofanimals.getObject() != null) {
			// TODO: Find out why HtmlInput<E>'s getObject() returns a String object and not an
			// Integer one, as expected!
			nrOfAnimals = Integer.parseInt(numberofanimals.getValue());
		} else {
			throw(new Exception("No number given - animal(s) not added"));
		}
		
		// Investigation
		int invid = ct.getOwnUserInvestigationIds(this.getLogin().getUserId()).get(0);
		
		db.beginTx();
		
		// Init lists that we can later add to the DB at once
		List<ObservedValue> valuesToAddList = new ArrayList<ObservedValue>();
		List<ObservationTarget> animalsToAddList = new ArrayList<ObservationTarget>();
		List<ProtocolApplication> appsToAddList = new ArrayList<ProtocolApplication>();
		
		// Make all animals
		for (int i = 0; i < nrOfAnimals; i++) {
			// Make and add animal
			ObservationTarget newAnimal = ct.createIndividual(invid, nameBase + (startNumber + i), 
					this.getLogin().getUserId());
			animalsToAddList.add(newAnimal);
		}
		db.add(animalsToAddList);
		
		// Make all protocol applications
		List<Integer> protocolIdList = new ArrayList<Integer>();
		protocolIdList.add(ct.getProtocolId("SetActive"));
		protocolIdList.add(ct.getProtocolId("SetSpecies"));
		protocolIdList.add(ct.getProtocolId("SetSex"));
		protocolIdList.add(ct.getProtocolId("SetAnimalType"));
		protocolIdList.add(ct.getProtocolId("SetSource"));
		protocolIdList.add(ct.getProtocolId("SetBackground"));
		protocolIdList.add(ct.getProtocolId("SetGenotype"));
		protocolIdList.add(ct.getProtocolId("SetDateOfBirth"));
		for (int j = 0; j < 8; j++) {
			ProtocolApplication newApp = ct.createProtocolApplication(invid, protocolIdList.get(j));
			appsToAddList.add(newApp);
		}
		db.add(appsToAddList);
		
		// Make all values
		List<Integer> featureIdList = new ArrayList<Integer>();
		featureIdList.add(ct.getMeasurementId("Active"));
		featureIdList.add(ct.getMeasurementId("Species"));
		featureIdList.add(ct.getMeasurementId("Sex"));
		featureIdList.add(ct.getMeasurementId("AnimalType"));
		featureIdList.add(ct.getMeasurementId("Source"));
		featureIdList.add(ct.getMeasurementId("Background"));
		featureIdList.add(ct.getMeasurementId("GeneName"));
		featureIdList.add(ct.getMeasurementId("GeneState"));
		featureIdList.add(ct.getMeasurementId("DateOfBirth"));
		
		for (ObservationTarget animal : animalsToAddList) {
			int animalid = animal.getId();
			
			// Set Active, with (start)time = entrydate and endtime = null
			ProtocolApplication app = appsToAddList.get(0);
	 		valuesToAddList.add(ct.createObservedValue(invid, app.getId(), entryDate, null, 
	 				featureIdList.get(0), animalid, "Alive", 0));
			// Set species
	 		app = appsToAddList.get(1);
	 		valuesToAddList.add(ct.createObservedValue(invid, app.getId(), entryDate, null, 
	 				featureIdList.get(1), animalid, null, speciesId));
			// Set sex
	 		app = appsToAddList.get(2);
	 		valuesToAddList.add(ct.createObservedValue(invid, app.getId(), entryDate, null, 
	 				featureIdList.get(2), animalid, null, sexId));
			// Set animaltype
	 		app = appsToAddList.get(3);
			valuesToAddList.add(ct.createObservedValue(invid, app.getId(), entryDate,  null, 
					featureIdList.get(3), animalid, animalType, 0));
			// Set source
			app = appsToAddList.get(4);
	 		valuesToAddList.add(ct.createObservedValue(invid, app.getId(), entryDate, null, 
	 				featureIdList.get(4), animalid, null, sourceId));
			// Set background
			if (backgroundId != 0) {
				app = appsToAddList.get(5);
				valuesToAddList.add(ct.createObservedValue(invid, app.getId(), entryDate, null, 
						featureIdList.get(5), animalid, null, backgroundId));
			}
			// Set genotype(s)
			int index = 0;
			for (String gene : geneList) {
				String geneState = genestateList.get(index);
				// Make protocol application
				app = appsToAddList.get(6);
				valuesToAddList.add(ct.createObservedValue(invid, app.getId(), entryDate, null, 
						featureIdList.get(6), animalid, gene, 0));
				valuesToAddList.add(ct.createObservedValue(invid, app.getId(), entryDate, null, 
						featureIdList.get(7), animalid, geneState, 0));
				index++;
			}
			// Set birthdate
			if (birthDate != null) {
				app = appsToAddList.get(7);
				valuesToAddList.add(ct.createObservedValue(invid, app.getId(), entryDate, null, 
						featureIdList.get(8), animalid, birthDate, 0));
			}
			
		}	
		db.add(valuesToAddList);
		
		db.commitTx();
		
		// Update custom label map now new animals have been added
		ct.makeObservationTargetNameMap(this.getLogin().getUserId(), true);
		
		// Add success message to the screen
		this.getMessages().clear();
		this.getMessages().add(new ScreenMessage(animalsToAddList.size() + " animal(s) added succesfully", true));
	}
	
	private void populateTablePanel(Database db) throws DatabaseException, ParseException {
		
		List<Integer> investigationIds = ct.getAllUserInvestigationIds(this.getLogin().getUserId());
		
		// panel for all elements
		containingPanel = new DivPanel(this.getName() + "panel", "");

		// Populate animal species list
		// DISCUSSION: why is this not ontology???
		species = new SelectInput("species", "Species:");
		species.setOptions(ct.getAllMarkedPanels("Species", investigationIds), "id", "name");
		species.setNillable(false);

		// Populate sexes list
		// DISCUSSION: why is this not ontology???
		sex = new SelectInput("sex", "Sex:");
		sex.setOptions(ct.getAllMarkedPanels("Sex", investigationIds), "id", "name");
		sex.setNillable(false);

		// Populate source list
		// All source types except "Eigen fok binnen uw organisatorische werkeenheid",
		// which is taken care of in the Breeding Module
		source = new SelectInput("source", "Source:");
		List<ObservationTarget> tmpSourceList = ct.getAllMarkedPanels("Source", investigationIds);
		for (ObservationTarget tmpSource : tmpSourceList)
		{
			int featureId = ct.getMeasurementId("SourceType");
			List<ObservedValue> sourceTypeValueList = db.query(ObservedValue.class).eq(ObservedValue.TARGET,
					tmpSource.getId()).eq(ObservedValue.FEATURE, featureId).find();
			if (sourceTypeValueList.size() > 0) {
				String sourcetype = sourceTypeValueList.get(0).getValue();
				if (!sourcetype.equals("Eigen fok binnen uw organisatorische werkeenheid")) {
					source.addOption(tmpSource.getId(), tmpSource.getName());
				}
			}
		}
		source.setNillable(false);
		
		// Populate animaltype list
		animaltype = new SelectInput("animaltype", "Animal type:");
		for (Code c : ct.getAllCodesForFeature("AnimalType")) {
			animaltype.addOption(c.getDescription(), c.getCode_String() + " ("
					+ c.getDescription() + ")");
		}
		animaltype.setOnchange("showHideGenotypeDiv(this.value);");
		animaltype.setNillable(false);

		// background, gene and genestate are REPEATING and CONDITIONAL on (animaltype = Transgeen dier)
		
		gmoPanel = new DivPanel("GMO", "Genotype(s):");
		gmoPanel.setId("GMO");
		
		background = new SelectInput("background", "Background:");
		background.setOptions(ct.getAllMarkedPanels("Background", investigationIds), "id", "name");
		gmoPanel.add(background);
		
		genePanel = new RepeatingPanel("geneinput", "GMO information:");
		
		gene = new SelectInput("gene", "Gene:");
		for (String option : ct.getAllCodesForFeatureAsStrings("GeneName")) {
			gene.addOption(option, option);
		}
		genePanel.add(gene);

		genestate = new SelectInput("genestate", "Gene state:");
		for (String option : ct.getAllCodesForFeatureAsStrings("GeneState")) {
			genestate.addOption(option, option);
		}
		genePanel.add(genestate);
		
		gmoPanel.add(genePanel);
		gmoPanel.setHidden(true);

		birthdate = new DateInput("birthdate", "Date of birth (if known):");
		birthdate.setValue(null);

		entrydate = new DateInput("entrydate", "Date of entry:");
		entrydate.setNillable(false);
		
		namePanel = new DivPanel("Name", "Name:");
		
		namebase = new SelectInput("namebase", "Name base (may be empty):");
		namebase.setId("namebase");
		namebase.addOption("New", "New (specify below)");
		for (String base : bases) {
			if (!base.equals("")) {
				namebase.addOption(base, base);
			}
		}
		namebase.setOnchange("updateStartNumber(this.value)");
		namePanel.add(namebase);
		
		startnumberhelper = new TextLineInput<String>("startnumberhelper");
		startnumberhelper.setLabel("");
		String helperContents = "";
		helperContents += "1"; // start number for new base
		for (String base : bases) {
			if (!base.equals("")) {
				helperContents += (";" + (ct.getHighestNumberForNameBase(base) + 1));
			}
		}
		helperContents += (";" + (ct.getHighestNumberForNameBase("") + 1)); // start number for empty base (comes last in jQuery select box)
		startnumberhelper.setValue(helperContents);
		startnumberhelper.setHidden(true);
		namePanel.add(startnumberhelper);
		
		newnamebase = new TextLineInput<String>("newnamebase");
		newnamebase.setLabel("New name base:");
		namePanel.add(newnamebase);
	
		startnumber = new IntInput("startnumber", "Start numbering at:");
		startnumber.setId("startnumber");
		startnumber.setValue(1); // start with highest number for new base (comes first in jQuery select box)
		namePanel.add(startnumber);
		
		numberofanimals = new IntInput("numberofanimals", "Number of animals:");
		numberofanimals.setValue(1);
		numberofanimals.setNillable(false);

		addbutton = new ActionInput("Add", "", "Add animal(s)");

		// add everything to the panel
		containingPanel.add(species);
		containingPanel.add(sex);
		containingPanel.add(source);
		containingPanel.add(animaltype);
		containingPanel.add(gmoPanel);
		containingPanel.add(birthdate);
		containingPanel.add(entrydate);
		containingPanel.add(namePanel);
		containingPanel.add(numberofanimals);
		containingPanel.add(addbutton);
	}
	
	public String render()
	{
		return this.containingPanel.toHtml();
	}

	public List<String> getBases() {
		return bases;
	}

	public void setBases(List<String> bases) {
		this.bases = bases;
	}
	
}
