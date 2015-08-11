package gate.tools;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.Utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class SimpleManualAnnotator extends JPanel implements ActionListener {
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    /**
     * Create the GUI and show it.  For thread safety, 
     * this method should be invoked from the 
     * event-dispatching thread.
     */
	
	static int OPTIONSFROMTYPEANDFEATURE = 0;
	static int OPTIONSFROMFEATURE = 1;
	static int OPTIONSFROMCOMMANDLINE = 2;
	
	static int NONEOFABOVE = 10001;
	static int SPURIOUS = 10002;
	static int UNDONE = 10003;
	
	static String noneofabove = "<NONE OF ABOVE>";
	static String spurious = "<SPURIOUS>";
	static String undone = "<NOT DONE>";
	static String back = "Back";
	static String next = "Next";
	static String saveandexit = "Save and Exit";
	
	static File corpusdir = null;
	Document currentDoc;
	List<Annotation> mentionList;
	
	int currentDocIndex = -1;
	static int docsInDir;
	int currentAnnIndex = -1;
	int mentionsInDoc = -1;
	
	//Tmp--to be got from command line
	int mode = OPTIONSFROMTYPEANDFEATURE;
	String inputASName = null;
	String contextType = "Sentence";
	String mentionType = "LookupList";
	String optionsType = "MetaMap";
	String optionsFeat = "PreferredName";
	String outputASName = "Key";
	
	JButton backButton, nextButton, exitButton;
	JLabel progress;
	JEditorPane display = new JEditorPane();
    ButtonGroup optionGroup = new ButtonGroup();
	JPanel optionsFrame = new JPanel();
	
	AnnotationTask currentAnnotationTask;
	
	public class AnnotationTask{
		Annotation mention;
		String context;
		long offset; //Relative to which start and end of mention are given
		long startOfMention;
		long endOfMention;
		String[] options;
		Annotation[] optionsAnns;
		boolean includeNoneOfAbove = true;
		boolean includeSpurious = true;
		boolean includeUndone = true;
		Annotation previouslySelected = null;
		int indexOfSelected = UNDONE;
	}
	
    public SimpleManualAnnotator() {
    	next();
    	
    	JPanel dispFrame = new JPanel();
    	dispFrame.setLayout(new BoxLayout(dispFrame, BoxLayout.Y_AXIS));
    	dispFrame.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    	dispFrame.setBackground(Color.WHITE);
    	
    	progress = new JLabel();
    	progress.setPreferredSize(new Dimension(250, 40));
    	progress.setMinimumSize(new Dimension(10, 10));
    	dispFrame.add(progress);

    	display.setEditable(false);
    	display.setContentType("text/html");
    	display.setPreferredSize(new Dimension(250, 145));
    	display.setMinimumSize(new Dimension(10, 10));
    	display.setBackground(new Color(0.94F, 0.94F, 0.94F));
    	display.setBorder(BorderFactory.createCompoundBorder(
    			BorderFactory.createMatteBorder(10,10,10,10, Color.WHITE), 
    			BorderFactory.createLoweredBevelBorder()));
        dispFrame.add(display);

    	optionsFrame.setLayout(new BoxLayout(optionsFrame, BoxLayout.Y_AXIS));
    	optionsFrame.setBackground(Color.WHITE);
        dispFrame.add(optionsFrame);
        
        redisplay();
    	
        JPanel buttonFrame = new JPanel();
        backButton = new JButton(back);
        backButton.setVerticalTextPosition(AbstractButton.CENTER);
        backButton.setHorizontalTextPosition(AbstractButton.CENTER); //aka LEFT, for left-to-right locales
        backButton.setMnemonic(KeyEvent.VK_A);
        backButton.setActionCommand(back);
 
        nextButton = new JButton(next);
        nextButton.setVerticalTextPosition(AbstractButton.CENTER);
        nextButton.setHorizontalTextPosition(AbstractButton.CENTER);
        nextButton.setMnemonic(KeyEvent.VK_Z);
        nextButton.setActionCommand(next);

        exitButton = new JButton(saveandexit);
        exitButton.setVerticalTextPosition(AbstractButton.CENTER);
        exitButton.setHorizontalTextPosition(AbstractButton.CENTER);
        exitButton.setMnemonic(KeyEvent.VK_Z);
        exitButton.setActionCommand(saveandexit);
  
        //Listen for actions on buttons 1 and 2.
        backButton.addActionListener(this);
        nextButton.addActionListener(this);
        exitButton.addActionListener(this);
 
        backButton.setToolTipText("Click this button to return to the previous item.");
        nextButton.setToolTipText("Click this button to skip to the next item.");
        exitButton.setToolTipText("Click this button to save the current document and exit.");
 
        //Add Components to this container, using the default FlowLayout.
        buttonFrame.add(backButton);
        buttonFrame.add(nextButton);
        buttonFrame.add(exitButton);
        
        add(dispFrame);
        add(buttonFrame);
    }
	
    private static void createAndShowGUI(final SimpleManualAnnotator sma) {
        //Create and set up the window.
        JFrame frame = new JFrame("GATE Simple Manual Annotator");
        frame.setPreferredSize(new Dimension(700, 450));
        frame.setMinimumSize(new Dimension(10, 10));
 
        //Create and set up the content pane.
        sma.setOpaque(true); //content panes must be opaque
        sma.setLayout(new BoxLayout(sma, BoxLayout.Y_AXIS));
        frame.setContentPane(sma);
 
        //Display the window.
        frame.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
        frame.setVisible(true);
        
        class MyWindowListener extends WindowAdapter {
    		public void windowClosing(WindowEvent ev) {
    			saveDoc(sma);
    			System.exit(0);
			}
    	}
        frame.addWindowListener(new MyWindowListener());
    }
    
    
    
    public static void main(String[] args) throws Exception {
    	Gate.init();

		gate.Utils.loadPlugin("Format_FastInfoset");
		
    	if(args.length!=1){
    		System.out.println("Usage: simpleManualAnnotator <corpusDir>");
    	} else {
			corpusdir = new File(args[0]);
			if(!corpusdir.isDirectory()){
				System.err.println(corpusdir.getAbsolutePath() + " is not a directory!");
				System.exit(0);
			} else {
				docsInDir = corpusdir.listFiles().length;
			}
    	}
    	
    	if(docsInDir<1){
			System.err.println("No documents to annotate!");
			System.exit(0);
    	}
    	
    	SimpleManualAnnotator sma = new SimpleManualAnnotator();
    	createAndShowGUI(sma);
    	
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        //javax.swing.SwingUtilities.invokeLater(new Runnable() {
        //    public void run() {
        //        createAndShowGUI(sma); 
        //    }
        //});
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		AnnotationSet outputAS = currentDoc.getAnnotations(outputASName);
		
		if (back.equals(e.getActionCommand())) {
			prev();
	    } else if (next.equals(e.getActionCommand())) {
			next();
	    } else if (noneofabove.equals(e.getActionCommand())) {
	    	if(currentAnnotationTask.previouslySelected!=null){
	    		outputAS.remove(currentAnnotationTask.previouslySelected);
	    	}
	    	FeatureMap fm = Factory.newFeatureMap();
	    	fm.put(optionsFeat, noneofabove);
	    	Utils.addAnn(outputAS, currentAnnotationTask.mention, optionsType, fm);
			next();
	    } else if (spurious.equals(e.getActionCommand())) {
	    	if(currentAnnotationTask.previouslySelected!=null){
	    		outputAS.remove(currentAnnotationTask.previouslySelected);
	    	}
	    	FeatureMap fm = Factory.newFeatureMap();
	    	fm.put(optionsFeat, spurious);
	    	Utils.addAnn(outputAS, currentAnnotationTask.mention, optionsType, fm);
			next();
	    } else if (undone.equals(e.getActionCommand())) {
	    	if(currentAnnotationTask.previouslySelected!=null){
	    		outputAS.remove(currentAnnotationTask.previouslySelected);
	    	}
			next();
	    } else if (saveandexit.equals(e.getActionCommand())) {
			saveDoc(this);
			System.exit(0);
	    } else {
	    	if(currentAnnotationTask.previouslySelected!=null){
	    		outputAS.remove(currentAnnotationTask.previouslySelected);
	    	}
	    	String option = e.getActionCommand();
	    	int opt = new Integer(option.substring(6)).intValue();
	    	Annotation toAdd = currentAnnotationTask.optionsAnns[opt];
	    	FeatureMap fm = Factory.newFeatureMap();
	    	fm.putAll(toAdd.getFeatures());
	    	Utils.addAnn(outputAS, currentAnnotationTask.mention, optionsType, fm);
			next();
	    }
		
		redisplay();
        revalidate();
        repaint();
	}
	
	public AnnotationTask createAnnotationTask(Annotation thisAnn){  
		AnnotationTask at = new AnnotationTask();
		AnnotationSet inputAS = currentDoc.getAnnotations(inputASName);
		
		AnnotationSet outputAS = currentDoc.getAnnotations(outputASName);
		AnnotationSet previous = Utils.getCoextensiveAnnotations(outputAS, thisAnn);
		String prev = null;
		if(previous.size()==1){
			at.previouslySelected = Utils.getOnlyAnn(previous);
			prev = at.previouslySelected.getFeatures().get(optionsFeat).toString();
			if(prev.equals(noneofabove)){
				at.indexOfSelected = NONEOFABOVE;
			} else if(prev.equals(spurious)){
				at.indexOfSelected = SPURIOUS;
			} else if(prev.equals(undone)){
				at.indexOfSelected = UNDONE;
			}
		}
		
		at.mention = thisAnn;
		
		//Context
		Annotation contextAnn = Utils.getOverlappingAnnotations(
				inputAS, thisAnn, contextType).iterator().next();
		at.context = Utils.cleanStringFor(currentDoc, contextAnn);
		at.offset = contextAnn.getStartNode().getOffset();
		at.startOfMention = thisAnn.getStartNode().getOffset();
		at.endOfMention = thisAnn.getEndNode().getOffset();
		
		//Options
		AnnotationSet options = Utils.getCoextensiveAnnotations(inputAS, thisAnn, optionsType);
		Iterator<Annotation> it = options.iterator();
		at.options = new String[options.size()];
		at.optionsAnns = new Annotation[options.size()];
		int index = 0;
		while(it.hasNext()){
			Annotation optan = it.next();
			String feat = optan.getFeatures().get(optionsFeat).toString();
			at.options[index] = feat;
			at.optionsAnns[index] = optan;
			if(feat.equals(prev)){
				at.indexOfSelected = index;
			}
			index++;
		}
		
		return at;
	}
	
	public void next(){
		//If we are initializing or on the last annotation we need a new doc
        if(currentDocIndex==-1 || currentAnnIndex==mentionsInDoc-1){
			saveDoc(this);
        	int foundAnns = 0;
        	while(foundAnns==0){//Hunt for next doc with mentions
	    		if(currentDocIndex<corpusdir.listFiles().length-1){
	    			if(currentDoc!=null) Factory.deleteResource(currentDoc);
	    			this.currentDocIndex++;
	    			File thisdoc = corpusdir.listFiles()[currentDocIndex];
	    	   		try {
	    	   			currentDoc = Factory.newDocument(thisdoc.toURI().toURL());
	    	   		} catch (Exception e) {
	    	   			// TODO Auto-generated catch block
	    	   			e.printStackTrace();
	    	   		}
	    	   		
	    	   		//Having got the doc we set up the anns
	    	   		mentionList = currentDoc.getAnnotations(inputASName).get(mentionType).inDocumentOrder();
	    	   		mentionsInDoc = mentionList.size();
	    	   		currentAnnIndex = -1;
	    	   		if(mentionsInDoc>0){
	    	   			foundAnns = 1; //Exit while loop, as we are happy with doc
	    	   		}
	    		} else {
	    			return; //We got stuck on the last one so exit
	    		}
        	}
        }
        
        //So now we should be in a position to simply take the next mention ann
        //since we have made sure we are on a doc with at least one remaining mention.
        currentAnnIndex++;
        Annotation toDisplay = mentionList.get(currentAnnIndex);
        currentAnnotationTask = createAnnotationTask(toDisplay);
	}
	
	public void prev(){
		if(currentAnnIndex<1){ //We need to move back a doc
			saveDoc(this);
        	int foundAnns = 0;
        	while(foundAnns==0){//Hunt for preceding doc with mentions
	    		if(currentDocIndex>0){
	    			if(currentDoc!=null) Factory.deleteResource(currentDoc);
	    			this.currentDocIndex--;
	    			File thisdoc = corpusdir.listFiles()[currentDocIndex];
	    	   		try {
	    	   			currentDoc = Factory.newDocument(thisdoc.toURI().toURL());
	    	   		} catch (Exception e) {
	    	   			// TODO Auto-generated catch block
	    	   			e.printStackTrace();
	    	   		}
	    	   		
	    	   		//Having got the doc we set up the anns
	    	   		mentionList = currentDoc.getAnnotations(inputASName).get(mentionType).inDocumentOrder();
	    	   		mentionsInDoc = mentionList.size();
	    	   		currentAnnIndex = mentionsInDoc;
	    	   		if(mentionsInDoc>0){
	    	   			foundAnns = 1; //Exit while loop, as we are happy with doc
	    	   		}
	    		} else {
	    			return; //We got stuck on the first one so exit
	    		}
        	}
        }
        
        //So now we should be in a position to simply take the next mention ann
        //since we have made sure we are on a doc with at least one remaining mention.
        currentAnnIndex--;
        Annotation toDisplay = mentionList.get(currentAnnIndex);
        currentAnnotationTask = createAnnotationTask(toDisplay);
	}
	
	private void redisplay(){
		if(currentAnnotationTask==null){
			return;
		}
    	progress.setText(progressReport());
    	
    	int start = new Long(currentAnnotationTask.startOfMention-currentAnnotationTask.offset).intValue();
    	int end = new Long(currentAnnotationTask.endOfMention-currentAnnotationTask.offset).intValue();
    	String htmlStr = currentAnnotationTask.context.substring(0, start);
    	htmlStr = htmlStr + "<b><span style=\"background-color:#66CDAA\">";
    	htmlStr = htmlStr + currentAnnotationTask.context.substring(start, end);
    	htmlStr = htmlStr + "</span></b>";
    	htmlStr = htmlStr + currentAnnotationTask.context.substring(end, currentAnnotationTask.context.length());
    	display.setText(htmlStr);

    	//Remove any existing radio buttons from optionsFrame
    	while(optionsFrame.getComponentCount()>0){
    		optionsFrame.remove(optionsFrame.getComponentCount()-1);
    	}
    	optionGroup = new ButtonGroup();
    	
        for(int i=0;i<currentAnnotationTask.options.length;i++){
	        JRadioButton button = new JRadioButton(currentAnnotationTask.options[i]);
	        button.setMnemonic(i);
	        button.setActionCommand("option" + i);
	        button.setSelected(true);
	        optionGroup.add(button);
	        button.addActionListener(this);
	        if(currentAnnotationTask.indexOfSelected==i){
	        	button.setSelected(true);
	        }
	        button.setBackground(new Color(0.99F, 0.95F, 0.99F));
	        optionsFrame.add(button);
	    }
        if(currentAnnotationTask.includeNoneOfAbove){
	        JRadioButton button = new JRadioButton(noneofabove);
	        button.setMnemonic(KeyEvent.VK_9);
	        button.setActionCommand(noneofabove);
	        optionGroup.add(button);
	        button.addActionListener(this);
	        if(currentAnnotationTask.indexOfSelected==NONEOFABOVE){
	        	button.setSelected(true);
	        }
	        button.setBackground(new Color(0.99F, 0.95F, 0.99F));
	        optionsFrame.add(button);
        }
        if(currentAnnotationTask.includeSpurious){
	        JRadioButton button = new JRadioButton(spurious);
	        button.setMnemonic(KeyEvent.VK_0);
	        button.setActionCommand(spurious);
	        optionGroup.add(button);
	        button.addActionListener(this);
	        if(currentAnnotationTask.indexOfSelected==SPURIOUS){
	        	button.setSelected(true);
	        }
	        button.setBackground(new Color(0.99F, 0.95F, 0.99F));
	        optionsFrame.add(button);
        }
        if(currentAnnotationTask.includeUndone){
	        JRadioButton button = new JRadioButton(undone);
	        button.setMnemonic(KeyEvent.VK_0);
	        button.setActionCommand(undone);
	        optionGroup.add(button);
	        button.addActionListener(this);
	        if(currentAnnotationTask.indexOfSelected==UNDONE){
	        	button.setSelected(true);
	        }
	        button.setBackground(Color.WHITE);
	        optionsFrame.add(button);
        }
	}
	
	private String progressReport(){
		return (currentDocIndex+1) + " of " + docsInDir + " docs, "
    			+ (currentAnnIndex+1) + " of " + mentionsInDoc + " annotations.";
	}
	
	private static void saveDoc(SimpleManualAnnotator sma){
		if(sma.currentDoc!=null){
			try {
				FileWriter thisdocfile = new FileWriter(corpusdir.listFiles()[sma.currentDocIndex]);
				thisdocfile.write(sma.currentDoc.toXml());
				thisdocfile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
