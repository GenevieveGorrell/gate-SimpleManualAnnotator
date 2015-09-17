# gate-SimpleManualAnnotator

Swing interface allowing fast manual annotation of a local directory of GATE documents.

##Overview

This annotation tool allows you to quickly annotate a directory of GATE documents stored locally. It is suitable for tasks such as accepting or rejecting automatically generated annotations in text or selecting from several options. It presents each annotation to be reviewed in a clear and simple way with key press options allowing one of a short list of options to be selected quickly, without mouse use, facilitating an annotation rate in the region of one every couple of seconds depending on task complexity. Annotation choices are written back onto the GATE documents.

An example task might be, for every Person annotation that appears in the corpus, to accept or reject that annotation as an accurate identification of a person mention. Alternatively you might have Mention annotations to be annotated as either Person, Location or Organization. Alternatively you might have Mention annotations each of which has a different set of possibilities for what that Mention is.

##Setup

###Configuring the Task

Your corpus of GATE documents needs to be prepared with annotations that describe what annotations you want to be presented to the annotator (hereafter referred to as "mentions"), and what options they should be presented with. Three modes are available for you to choose between depending on your task and data. The task is defined in the configuration file, which is passed to the annotation tool on startup, an example of which is provided, and in here you will choose your mode and specify the required options. We begin by listing options that pertain to all three modes, before discussing each mode separately.

**Options that need to be set in all modes**

- autoadvance - This option specifies whether the tool should automatically move on to the next mention when the user selects an options (quicker) or wait for the user to manually move on to the next mention (which some users may prefer).

- inputASName - The name of the input annotation set from which the mentions and options are to be drawn.
- contextType - Each mention is presented to the annotator with some context, to enable them to form a more accurate judgement. Your corpus needs to be prepared with suitable context annotations, such as sentences.
- mentionType - The annotation type of the mentions that will be presented to the annotator to form a judgement about.
- outputASName - Once the annotator has made their choice, an output annotation will be generated recording this. The output annotation set name indicates the annotation set you want these judgements to go into.
- includeNoneOfAbove - In addition to the choices you provide, do you want the annotator to also see an automatically generated "none of the above" option? This would be used to indicate that the mention is valid, but that it was impossible to choose the correct choice from the ones provided. An example might be annotating locations as countries or cities, and receiving a location that is a county. The annotation is a valid location, but the "county" option wasn't available.
- includeSpurious - In addition to the choices you provide, do you want the annotator to also see an automatically generated "spurious" option? This would be used to indicate that the mention is not a valid example of the type being annotated. For example, when annotating locations, if a mention is presented that isn't a location, this option would be used to indicate that.

**OPTIONSFROMSTRING**

In this mode, the options to be presented are specified as a semicolon-separated list in the configuration file. The output feature is also specified in the configuration file.

- options - The options are listed here, separated by semicolons.
- outputFeat - The output annotations appear in the output annotation set, as described above, and are of the same type as the mention. The choice the user makes appears in the outputFeat, the name of which is specified here.

**OPTIONSFROMFEATURE**

This option indicates that the choices to be provided to the annotator should be taken from a set provided in a feature on the mentions. This should be a Set object containing String objects.

- optionsFeat - The feature on the mention that contains the choices.
- outputFeat - The feature on the output annotations to contain the choice.

**OPTIONSFROMTYPEANDFEATURE**

This option indicates that choices are to be found on a separate annotation type. For example, you may have a "Mention" type defining the locations to be annotated, but your options are taken from "Candidate" annotations that are co-located with the mentions.

- optionsType - The annotation type that defines the choice, for example "Candidate".
- optionsFeat = The feature indicating the textual string to be presented describing the choice. For example, "Candidate" annotations may have a feature called "PreferredName".

###Running the Tool

To run, the annotator requires the GATE jar as well as the GATE lib directory so something like this should work:

java -cp gate-SimpleManualAnnotator.jar:$GATE_HOME/bin/gate.jar:$GATE_HOME/lib/* gate.tools.SimpleManualAnnotator <config file> <directory of GATE documents>

A shell script is provided with the distribution.

##Usage

![GATE Simple Manual Annotator screenshot](https://raw.githubusercontent.com/GenevieveGorrell/gate-SimpleManualAnnotator/master/Screenshot-GATESimpleManualAnnotator.png "GATE Simple Manual Annotator screenshot")

The tool will automatically find the next un-done mention in the corpus to begin with. The position we are currently at in the corpus is indicated at the top. In the screenshot, we are on the first mention in the first document.

The mention is highlighted in teal and the context (in the screenshot, it is the sentence) is presented surrounding it.

The choice list is presented below the mention. Numbers are assigned to each choice, and letters are assigned to autogenerated options. This means you can use key presses to annotate quickly. If you want to use the number pad on the right of the keyboard, you need to press "Num Lock" on your keyboard first. You are of course welcome to use the mouse to select your option too.

Navigation options allow you to move forward and back through the mentions, or jump to the last or next un-done mention. You can also use right or down arrows to move to the next mention, or left or up arrows to move to the previous one. If you are using the autoadvance option, when you select a choice the tool will automatically move to the next mention, so you may never need to use the navigation buttons.

A "Save and Exit" button is provided for your reassurance, but in fact closing the window will also save your work. Every time you move to a new document, your work on that document is saved. If your computer crashed, you would lose the work on the current document, but not on completed documents. There is no way to not save your work. However you can quickly remove a large number of annotations by holding down Z with autoadvance switched on. If you are concerned that you might not want to save your work, you are recommended to make a back-up.

