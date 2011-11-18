package plugins.LLcatalogueSplitter;

import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.html.JQuerySplitter;
import org.molgenis.framework.ui.html.JQuerySplitterContents;
import org.molgenis.util.Entity;
import org.molgenis.util.Tuple;


public class LLcatalogueSplitterPlugin  extends PluginModel<Entity>
{

	private static final long serialVersionUID = -6143910771849972946L;
	
	JQuerySplitter<Object> splitter;
	
	public LLcatalogueSplitterPlugin(String name, ScreenController<?> parent)
	{	
		
		super(name, parent);
		
		splitter = new JQuerySplitter<Object>("splitter");
		JQuerySplitterContents splitterContents = new JQuerySplitterContents();
		splitterContents.setLeftPane("<p>This is the left side of the splitter </p> <p>&nbsp;</p>");
		splitterContents.setRightTopPane("<p> Right Top Pane </p>");
		splitterContents.setRightBottomPane("<p>right bottom Pane</p>");
	}

	@Override
	public String getViewName()
	{
		return "plugins_LLcatalogueSplitter_LLcatalogueSplitterPlugin";
	}

	@Override
	public String getViewTemplate()
	{
		return "plugins/LLcatalogueSplitter/LLcatalogueSplitterPlugin.ftl";
	}

	
	@Override
	public void handleRequest(Database db, Tuple request)
	{
		//replace example below with yours
//		try
//		{
//			//start database transaction
//			db.beginTx();
//
//			//get the "__action" parameter from the UI
//			String action = request.getAction();
//		
//			if( action.equals("do_add") )
//			{
//				Experiment e = new Experiment();
//				e.set(request);
//				db.add(e);
//			}
//
//			//commit all database actions above
//			db.commitTx();
//
//		} catch(Exception e)
//		{
//			db.rollbackTx();
//			//e.g. show a message in your form
//		}
	}

	@Override
	public void reload(Database db)
	{
//		try
//		{
//			Database db = this.getDatabase();
//			Query q = db.query(Experiment.class);
//			q.like("name", "test");
//			List<Experiment> recentExperiments = q.find();
//			
//			//do something
//		}
//		catch(Exception e)
//		{
//			//...
//		}
	}
	
	@Override
	public boolean isVisible()
	{
		if (!this.getLogin().isAuthenticated()) {
			return false;
		}
		return true;
	}

	
	
	public String getSplitter() {
		return splitter.toHtml();
	}
}
