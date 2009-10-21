/* FeatureIDE - An IDE to support feature-oriented software development
 * Copyright (C) 2005-2009  FeatureIDE Team, University of Magdeburg
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 *
 * See http://www.fosd.de/featureide/ for further information.
 */
package de.ovgu.featureide.ui.ahead.editors.jak;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import de.ovgu.featureide.ui.ahead.editors.JakEditor;
import featureide.core.CorePlugin;
import featureide.core.IFeatureProject;
import featureide.core.jakprojectmodel.IClass;
import featureide.core.jakprojectmodel.IField;
import featureide.core.jakprojectmodel.IJakProject;
import featureide.core.jakprojectmodel.IMethod;



/**
 * This class computes the proposals for the auto completion / content assistant.
 * 
 * @author Constanze Adler
 */
public class JakCompletionProcessor implements IContentAssistProcessor{
	public static final String ID = "featureide.ui.ahead.editors.jak.JakCompletionProcessor";
	
	protected static class Validator implements IContextInformationValidator, IContextInformationPresenter {

		protected int fInstallOffset;
		
		public void install(IContextInformation info, ITextViewer viewer,
				int offset) {
			fInstallOffset= offset;
		}

		public boolean isContextInformationValid(int offset) {
			return Math.abs(fInstallOffset - offset) < 5;
		}

		public boolean updatePresentation(int offset,
				TextPresentation presentation) {
			return false;
		}
		
	}
	
	protected final static String[] fgProposals=
	{"Super()","refines", "layer","abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "continue", "default", "do", "double", "else", "extends", "false", "final", "finally", "float", "for", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while" }; //$NON-NLS-48$ //$NON-NLS-47$ //$NON-NLS-46$ //$NON-NLS-45$ //$NON-NLS-44$ //$NON-NLS-43$ //$NON-NLS-42$ //$NON-NLS-41$ //$NON-NLS-40$ //$NON-NLS-39$ //$NON-NLS-38$ //$NON-NLS-37$ //$NON-NLS-36$ //$NON-NLS-35$ //$NON-NLS-34$ //$NON-NLS-33$ //$NON-NLS-32$ //$NON-NLS-31$ //$NON-NLS-30$ //$NON-NLS-29$ //$NON-NLS-28$ //$NON-NLS-27$ //$NON-NLS-26$ //$NON-NLS-25$ //$NON-NLS-24$ //$NON-NLS-23$ //$NON-NLS-22$ //$NON-NLS-21$ //$NON-NLS-20$ //$NON-NLS-19$ //$NON-NLS-18$ //$NON-NLS-17$ //$NON-NLS-16$ //$NON-NLS-15$ //$NON-NLS-14$ //$NON-NLS-13$ //$NON-NLS-12$ //$NON-NLS-11$ //$NON-NLS-10$ //$NON-NLS-9$ //$NON-NLS-8$ //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$

	private final char[] PROPOSAL_ACTIVATION_CHARS = new char[] { '.', '('};//,'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	private ICompletionProposal[] NO_COMPLETIONS = new ICompletionProposal[0];
	
	
	protected IContextInformationValidator fValidator= new Validator();
	private JakEditor editor;
	public JakCompletionProcessor(JakEditor editor){
		super();
		this.editor = editor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		List<CompletionProposal> propList = new ArrayList<CompletionProposal>();
		computeProposals(propList, viewer,offset);
		if (propList==null) return NO_COMPLETIONS;
		ICompletionProposal[] result= new ICompletionProposal[propList.size()];
		propList.toArray(result);
	
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		IContextInformation[] result= new IContextInformation[5];
		for (int i= 0; i < result.length; i++)
			result[i]= new ContextInformation(
					MessageFormat.format(JakEditorMessages.getString("CompletionProcessor.ContextInfo.display.pattern"), new Object[] { new Integer(i), new Integer(offset) }),  //$NON-NLS-1$
					MessageFormat.format(JakEditorMessages.getString("CompletionProcessor.ContextInfo.value.pattern"), new Object[] { new Integer(i), new Integer(offset - 5), new Integer(offset + 5)})); //$NON-NLS-1$
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return PROPOSAL_ACTIVATION_CHARS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { '#' };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return fValidator;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return null;
	}
	private void computeProposals(List<CompletionProposal> propList, ITextViewer viewer, int offset)
	{
		//retrieve current document
		IDocument doc = viewer.getDocument();
		try {
			collectCompletionProposals(viewer, offset);
			DocumentParser parser = new DocumentParser(doc);
			ArrayList<CompletionMethod> methods = parser.getMethods();
			ArrayList<CompletionField> fields = parser.getFields();
			
			int line = doc.getLineOfOffset(offset);
			int length = doc.getLineLength(line);
			String text = doc.get(offset-(length-1),length-1);
			String behind = null;
			text = text.trim();
			for (int i = 0; i<PROPOSAL_ACTIVATION_CHARS.length; i++)
			if (text.startsWith(".") || text.contains(";")){
				propList = null;
				return;
			}
			
			if (text.contains(".")){
				String[] getWords = text.split("[.]"); 
				char[]textToChar = text.toCharArray();
				if ((!(textToChar[textToChar.length-1]== '.')) && (textToChar.length>0))
					behind = getWords[getWords.length-1];
			}
			else {
				behind = text;
			}
			
			
				
			for (Iterator iter=fields.iterator();iter.hasNext();){
				CompletionField field = (CompletionField) iter.next();
				String prop;
				prop = field.getFieldName() + " : " + field.getType();
				if (behind==null){
					IContextInformation info= new ContextInformation(prop, field.getType()); //$NON-NLS-1$	
					propList.add(new CompletionProposal(field.getFieldName(), offset, 0, field.getFieldName().length(),field.getImage(), prop, info, MessageFormat.format(JakEditorMessages.getString("CompletionProcessor.Proposal.hoverinfo.pattern"), field.getType()))); //$NON-NLS-1$
				}
				else 
					if (field.getFieldName().startsWith(behind)){
						IContextInformation info= new ContextInformation(prop, field.getType()); //$NON-NLS-1$	
						propList.add(new CompletionProposal(field.getFieldName(), offset-behind.length(), behind.length(), field.getFieldName().length(),field.getImage(), prop, info, MessageFormat.format(JakEditorMessages.getString("CompletionProcessor.Proposal.hoverinfo.pattern"), field.getType()))); //$NON-NLS-1$
					}
			}
			for (Iterator iter=methods.iterator();iter.hasNext();){
				CompletionMethod method = (CompletionMethod)iter.next();
				String prop;
				prop = ((method.getReturnValue()).equals("")) ? method.getMethodName() : method.getMethodName()+" : " + method.getReturnValue();
				if (behind==null){				
					IContextInformation info= new ContextInformation(prop, prop); 
					propList.add(new CompletionProposal(method.getMethodName(), offset, 0, method.getMethodName().length(),method.getImg(), prop, info, MessageFormat.format(JakEditorMessages.getString("CompletionProcessor.Proposal.hoverinfo.pattern"), method.getParamList()))); //$NON-NLS-1$
				}
				else
					if(method.getMethodName().startsWith(behind)){
						IContextInformation info= new ContextInformation(prop, prop);
						propList.add(new CompletionProposal(method.getMethodName(),  offset-behind.length(), behind.length(), method.getMethodName().length(),method.getImg(), prop, info, MessageFormat.format(JakEditorMessages.getString("CompletionProcessor.Proposal.hoverinfo.pattern"), method.getParamList()))); //$NON-NLS-1$
					}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		
	}
		
	private void/*ArrayList*/ collectCompletionProposals(ITextViewer viewer, int offset) throws BadLocationException{
		IEditorInput input = editor.getEditorInput();
		
		if (input instanceof IFileEditorInput){
			IFile file = ((IFileEditorInput) input).getFile();
			IFeatureProject featureProject = CorePlugin.getProjectData(file);
			if (featureProject==null) return;
			
			IJakProject project = featureProject.getJakProject();
			if (project==null) return;
			
			IClass[] classes = project.getClasses();
		
			if (classes == null) return;
					
			for (IClass nextClass : classes){
				//IField[] fields = new IField[classes[i].getFieldCount()];
				//IMethod[] methods = new IMethod[classes[i].getMethodCount()];
				IField[] fields = nextClass.getFields();
				IMethod[] methods = nextClass.getMethods();
				for (IMethod method : methods)
					System.out.println(method.getMethodName());
				if (fields.length > 0) System.out.println(nextClass.getName()+": fields > 0");
				if (methods.length > 0) System.out.println(nextClass.getName()+": methods > 0");
							
			}
		}		
	}
	
	
	protected class CompletionMethod{
		private Image img;
		private String returnValue;
		private ArrayList<String> paramList;
		private String methodName;
		protected CompletionMethod(Image image, String returnVal, String methodName, ArrayList<String> parameterList){
			this.img = image;
			this.returnValue = returnVal;
			this.methodName = methodName;
			this.paramList = parameterList;
		}
		/**
		 * @return the img
		 */
		public Image getImg() {
			return img;
		}
		/**
		 * @return the returnValue
		 */
		public String getReturnValue() {
			return returnValue;
		}
		/**
		 * @return the paramList
		 */
		public ArrayList<String> getParamList() {
			return paramList;
		}
		/**
		 * @return the methodName
		 */
		public String getMethodName() {
			return methodName;
		}
		
		
	}
	protected class CompletionField{
		private String fieldName;
		private Image  image;
		private String type;
		protected CompletionField(String name, Image img, String type){
			this.fieldName = name;
			this.image = img;
			this.type = type;
		}
		/**
		 * @return the fieldName
		 */
		public String getFieldName() {
			return fieldName;
		}
		/**
		 * @return the image
		 */
		public Image getImage() {
			return image;
		}
		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}
	}
	protected class CompletionClass{
		private String className;
		private String parent;
		private String interfaces;
		private Image image;
		protected CompletionClass(Image img, String name, String parent, String interfaces){
			this.className = name;
			this.parent = parent;
			this.interfaces = interfaces;
			this.image = img;
		}
		/**
		 * @return the className
		 */
		public String getClassName() {
			return className;
		}
		/**
		 * @return the parent
		 */
		public String getParent() {
			return parent;
		}
		/**
		 * @return the interfaces
		 */
		public String getInterfaces() {
			return interfaces;
		}
		/**
		 * @return the image
		 */
		public Image getImage() {
			return image;
		}
	}
	protected class DocumentParser{
		
		private IDocument document; 
		private ArrayList<String> publicLines;
		private ArrayList<String> protectedLines;
		private ArrayList<String> privateLines;
		private ArrayList<CompletionMethod> methods;
		private ArrayList<CompletionField> fields;
		private ArrayList<CompletionClass> classes;
		
		protected DocumentParser(IDocument doc){
			document = doc;
			
			try {
				publicLines = find("public");
				protectedLines = find("protected");
				privateLines = find("private");
				methods = buildMethods();
				fields  = buildFields();
				classes = buildClasses();				
			} catch (BadLocationException e) {				
				e.printStackTrace();
			}
		}
		
		/**
		 * @return
		 */
		private ArrayList<CompletionClass> buildClasses() {
			ArrayList<CompletionClass> list = new ArrayList<CompletionClass>();
			for (Iterator iter=publicLines.iterator(); iter.hasNext();){
				String text = (String) iter.next();
				if (text.contains("class"))
					list.add(extractClassName(text,"public"));
			}
			for (Iterator iter=protectedLines.iterator(); iter.hasNext();){
				String text = (String) iter.next();
				if (!text.contains("class")||!text.contains("(")||text.contains(" new "))
					list.add(extractClassName(text,"protected"));
			}
			for (Iterator iter=privateLines.iterator(); iter.hasNext();){
				String text = (String) iter.next();
				if (!text.contains("class")||!text.contains("(")||text.contains(" new "))
					list.add(extractClassName(text,"private"));
			}
			return list;
		}

		/**
		 * @param text
		 * @param string
		 * @return
		 */
		private CompletionClass extractClassName(String text, String identifier) {
			CompletionClass currentClass = null;
			boolean isFinal = false;
			boolean isStatic = false;
			ISharedImages javaImages = JavaUI.getSharedImages();
			Image img = null;
			if (text.contains(identifier)){
				text = text.substring(identifier.length());
				text = text.trim();
				
				if (text.contains("final")){
					isFinal = true;
					int index = text.indexOf("final");
					text = text.substring(0,index) + text.substring(index+5);
					text = text.trim();
				}
				if (text.contains("static")){
					isStatic = true;
					int index = text.indexOf("static");
					text = text.substring(0,index) + text.substring(index+6);
					text = text.trim();
				}
				img = javaImages.getImage(ISharedImages.IMG_OBJS_CLASS);				
				if (text.startsWith("class")){
					text = text.substring(5);
					text = text.trim();
				}
				boolean itExtends = text.contains("extends");
				boolean itImplements = text.contains("implements");
				
				String[] toArray = text.split("[ ]");
				String name = "";
				String parent = "";
				String interfaces = "";
				for (int i = 0; i< toArray.length; i++){
					name= toArray[0];
					if(itExtends && toArray[i].equals("extends"))
						parent = toArray[i+1];
					if (itImplements && toArray[i].equals("implements"))
						interfaces = toArray[i+1];
						
				}
				currentClass = new CompletionClass(img,name,parent,interfaces);
			}
				return currentClass;
		}

		/**
		 * @return ArrayList of fields 
		 */
		private ArrayList<CompletionField> buildFields() {
			ArrayList<CompletionField> list = new ArrayList<CompletionField>();
			for (Iterator iter=publicLines.iterator(); iter.hasNext();){
				String text = (String) iter.next();
				if (!text.contains("class")&&(!text.contains("(")||text.contains(" new ")))
					list.add(extractFieldName(text,"public"));
			}
			for (Iterator iter=protectedLines.iterator(); iter.hasNext();){
				String text = (String) iter.next();
				if (!text.contains("class")&&(!text.contains("(")||text.contains(" new ")))
					list.add(extractFieldName(text,"protected"));
			}
			for (Iterator iter=privateLines.iterator(); iter.hasNext();){
				String text = (String) iter.next();
				if (!text.contains("class")&&(!text.contains("(")||text.contains(" new ")))
					list.add(extractFieldName(text,"private"));
			}
			return list;
		}

		/**
		 * @param text
		 * @param identifier
		 * @return Field
		 */
		private CompletionField extractFieldName(String text, String identifier) {
			CompletionField field = null;
			boolean isFinal = false;
			boolean isStatic = false;
			ISharedImages javaImages = JavaUI.getSharedImages();
			Image img = null;
			if (text.contains(identifier)){
				text = text.substring(identifier.length());
				text = text.trim();
				
				if (text.contains("final")){
					isFinal = true;
					int index = text.indexOf("final");
					text = text.substring(0,index) + text.substring(index+5);
					text = text.trim();
				}
				if (text.contains("static")){
					isStatic = true;
					int index = text.indexOf("static");
					text = text.substring(0,index) + text.substring(index+6);
					text = text.trim();
				}
				if (identifier.equals("public")){
					img = javaImages.getImage(ISharedImages.IMG_FIELD_PUBLIC);				
				}
				if (identifier.equals("protected")){
					img = javaImages.getImage(ISharedImages.IMG_FIELD_PROTECTED);				
				}
				if (identifier.equals("private")){
					img = javaImages.getImage(ISharedImages.IMG_FIELD_PRIVATE);				
				}
				String[] toArray = text.split("[ ]");
				String type = toArray[0];
				String name = toArray[1];
				if (name.contains(";")) name = name.replace(';',' ');
				name = name.trim();
				field = new CompletionField(name,img,type);
			}
			return field;
		}

		private ArrayList<String> find(String searchPattern) throws BadLocationException{
			ArrayList<String> list = new ArrayList<String>();
			FindReplaceDocumentAdapter searcher = new FindReplaceDocumentAdapter(document);
			IRegion reg = searcher.find(0, searchPattern, true, true, false, false);
			while (reg!=null){
				int wordSearchPos = reg.getOffset() + reg.getLength() - searchPattern.length();
				IRegion word = searcher.find(wordSearchPos, searchPattern, true, true, false, false);
				int line = document.getLineOfOffset(wordSearchPos);
				int length = document.getLineLength(line);
				String result = document.get(wordSearchPos,length-1);
				result = result.trim();
				list.add(result);
				int nextPos = word.getOffset()+word.getLength();
				if (nextPos>=document.getLength()){
					break;
				}
				reg = searcher.find(nextPos, searchPattern, true, true, false, false);
				
			}
			
			return list;
		}
		private ArrayList<CompletionMethod> buildMethods(){
			ArrayList<CompletionMethod> list = new ArrayList<CompletionMethod>();
			for (Iterator iter=publicLines.iterator(); iter.hasNext();){
				String text = (String) iter.next();
				if (text.contains("(")&&!text.contains("new"))
					list.add(extractMethodName(text,"public"));
			}
			for (Iterator iter=protectedLines.iterator(); iter.hasNext();){
				String text = (String) iter.next();
				if (text.contains("(")&&!text.contains("new"))
					list.add( extractMethodName(text,"protected"));
			}
			for (Iterator iter=privateLines.iterator(); iter.hasNext();){
				String text = (String) iter.next();
				if (text.contains("(")&&!text.contains("new"))
					list.add(extractMethodName(text,"private"));
			}
			
			return list;	
		}
		/**
		 * @return the methods
		 */
		public ArrayList<CompletionMethod> getMethods() {
			return methods;
		}

		/**
		 * @return the fields
		 */
		public ArrayList<CompletionField> getFields() {
			return fields;
		}

		/**
		 * @return the classes
		 */
		public ArrayList<CompletionClass> getClasses() {
			return classes;
		}

		private CompletionMethod extractMethodName(String text, String identifier){
			CompletionMethod method = null;
			boolean isFinal = false;
			boolean isStatic = false;
			ISharedImages javaImages = JavaUI.getSharedImages();
			Image img = null;
			if (text.contains(identifier)){
				text = text.substring(identifier.length());
				text = text.trim();
				if (text.contains("final")){
					isFinal = true;
					int index = text.indexOf("final");
					text = text.substring(0,index) + text.substring(index+5);
					text = text.trim();
				}
				if (text.contains("static")){
					isStatic = true;
					int index = text.indexOf("static");
					text = text.substring(0,index) + text.substring(index+6);
					text = text.trim();
				}
				if (identifier.equals("public")){
					img = javaImages.getImage(ISharedImages.IMG_OBJS_PUBLIC);				
				}
				if (identifier.equals("protected")){
					img = javaImages.getImage(ISharedImages.IMG_OBJS_PROTECTED);				
				}
				if (identifier.equals("private")){
					img = javaImages.getImage(ISharedImages.IMG_OBJS_PRIVATE);				
				}
				String[] toArray = text.split("[ ]");
				String returnValue = "";
				
				if (!toArray[0].contains("(")){
					returnValue = toArray[0];
					text = text.substring(returnValue.length());
					text = text.trim();
				}
				toArray = text.split("[(]");
				String methodName = toArray[0];
				text = text.substring(methodName.length()+1);
				text = text.trim();
				toArray = text.split("[)]");
				 
				if (toArray.length<1) return new CompletionMethod(img, returnValue, methodName+"()",null);
				methodName += "(" + toArray[0]+")";
				String param = toArray[0];
				ArrayList<String> parameters = new ArrayList<String>();
				toArray = param.split("[,]");
				for (int i=0; i<toArray.length; i++){
					toArray[i] = toArray[i].trim();
					String[] paramArray = toArray[i].split("[ ]");
					parameters.add(paramArray[0]);
				}
				method = new CompletionMethod(img,returnValue,methodName,parameters);
					
				
			}
				
			
			return method;
		}
		
	}
	
	
}
