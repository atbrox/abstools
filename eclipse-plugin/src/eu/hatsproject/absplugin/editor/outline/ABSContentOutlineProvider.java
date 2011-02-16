package eu.hatsproject.absplugin.editor.outline;

import static eu.hatsproject.absplugin.editor.outline.ABSContentOutlineUtils.isExportList;
import static eu.hatsproject.absplugin.editor.outline.ABSContentOutlineUtils.isImportList;
import static eu.hatsproject.absplugin.editor.outline.ABSContentOutlineUtils.isStandardLibImport;
import static eu.hatsproject.absplugin.util.Constants.ABS_FILE_EXTENSION;
import static eu.hatsproject.absplugin.util.Constants.EMPTY_OBJECT_ARRAY;
import static eu.hatsproject.absplugin.util.UtilityFunctions.hasABSFileExtension;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import abs.frontend.ast.*;
import eu.hatsproject.absplugin.builder.AbsNature;
import eu.hatsproject.absplugin.util.Constants;
import eu.hatsproject.absplugin.util.InternalASTNode;
import eu.hatsproject.absplugin.util.UtilityFunctions;

/**
 * The ABS content outline provider is responsible for dissecting the AST of an
 * ABS file (or more specifically a compilation unit) into a tree structure.
 * <br/><br/>
 * The ABS content provider is set using the TreeViewer's method
 * setContentProvider(ITreeContentProvider) during the setup of the TreeViewer.
 * 
 * @author cseise
 * 
 */
public class ABSContentOutlineProvider implements ITreeContentProvider {

	private static final ASTNode<?>[] EMPTY_NODES = new ASTNode<?>[0];

	/**
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {

		if (parentElement instanceof IFile) {
			return getChildrenOf((IFile) parentElement);
		} else if (parentElement instanceof InternalASTNode<?>) {
			InternalASTNode<?> node = (InternalASTNode<?>) parentElement;
			
			synchronized (node.getNature().modelLock) {
				ASTNode<?>[] children = getChildrenOfASTNode(node.getASTNode());
				return InternalASTNode.wrapASTNodes(children, node.getNature()).toArray();
			}
		}

		return new Object[0];
	}
	
	private ASTNode<?>[] getChildrenOfASTNode(ASTNode<?> parentElement){
		if (parentElement instanceof ModuleDecl) {
			//no use of static imports here due to name clash with local methods...
			return ABSContentOutlineUtils.getChildrenOf((ModuleDecl) parentElement).toArray(EMPTY_NODES);
		} else if (parentElement instanceof CompilationUnit) {
			return getChildrenOf((CompilationUnit) parentElement);
		} else if (parentElement instanceof ClassDecl) {
			return getChildrenOf((ClassDecl) parentElement);
		} else if (parentElement instanceof InterfaceDecl) {
			return getChildrenOf((InterfaceDecl) parentElement);
		} else if (parentElement instanceof DataTypeDecl) {
			return getChildrenOf((DataTypeDecl) parentElement);
		} else if (parentElement instanceof MainBlock) {
			return getChildrenOf((MainBlock) parentElement);
		} else if (parentElement instanceof List<?>) {
			return getChildrenOf((List<?>) parentElement);
		}
		
		return EMPTY_NODES;
	}

	private Object[] getChildrenOf(IFile file){
		
		if(!ABS_FILE_EXTENSION.equalsIgnoreCase(file.getFileExtension()))
			return null;
		AbsNature nature = UtilityFunctions.getAbsNature(file.getProject());
		if(nature != null){
			CompilationUnit cu = nature.getCompilationUnit(file);
			try {
				if (file.getProject().hasNature(Constants.NATURE_ID)){
					return getChildrenOf(cu,(AbsNature)file.getProject().getNature(Constants.NATURE_ID));
				}else{
					return null;
				}
			} catch (CoreException e) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),
						"Error", "Error in Content outline. Could not check project natures for project."
						+ file.getProject().getName() +
						"\n Maybe the project does not exist anymore or the project is not accessible!");
				e.printStackTrace();
				return null;
			}
				
		} else {

			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private ASTNode<?>[] getChildrenOf(List<?> parentElement) {
	    List<?> nodeList = ((List<?>) parentElement);
	    ArrayList<ASTNode<?>> impExp = new ArrayList<ASTNode<?>>();
	    
	    //Check whether we have an import or an export list
	    if (isImportList(nodeList)){
	    	for (Import i : (List<Import>)nodeList){
	    		if (isStandardLibImport(i)){
	    			continue;
	    		}
	    		impExp.add(i);					
	    	}						

	    	return impExp.toArray(EMPTY_NODES);				
	    }else if (isExportList(nodeList)){
	    	for (Export e : (List<Export>)nodeList) {
	    		impExp.add(e);
	    	}
	    	return impExp.toArray(EMPTY_NODES);
	    }
	    
	    return EMPTY_NODES;
	}
	
	/**
	 * Retrieve the Children of a CompilationUnit
	 * @param d The target CompilationUnit
	 * @return The children of a CompilationUnit, which are relevant for the Content Outline
	 */
	private InternalASTNode<?>[] getChildrenOf (CompilationUnit cu,AbsNature nature){
	    if (cu != null){
	        ArrayList<InternalASTNode<ModuleDecl>> modules = new ArrayList<InternalASTNode<ModuleDecl>>();
	        
	        for (ModuleDecl d : cu.getModuleDecls()) {
	    	modules.add(new InternalASTNode<ModuleDecl>(d,nature));
	        }
		return modules.toArray(new InternalASTNode<?>[0]);
	    } else{
	        return new InternalASTNode<?>[0];
	    }
	}	
	
	/**
	 * Retrieve the Children of a CompilationUnit
	 * @param d The target CompilationUnit
	 * @return The children of a CompilationUnit, which are relevant for the Content Outline
	 */
	private ASTNode<?>[] getChildrenOf (CompilationUnit cu){
	    if (cu != null){
	        ArrayList<ModuleDecl> modules = new ArrayList<ModuleDecl>();
	        
	        for (ModuleDecl d : cu.getModuleDecls()) {
	    	modules.add(d);
	        }
		return modules.toArray(EMPTY_NODES);
	    } else{
	        return EMPTY_NODES;
	    }
	}	
	
	/**
	 * Retrieve the Children of a ClassDecl
	 * @param d The target ClassDecl
	 * @return The children of a ClassDecl, which are relevant for the Content Outline
	 */
	private ASTNode<?>[] getChildrenOf(ClassDecl d) {
		ArrayList<ASTNode<?>> pDecl = new ArrayList<ASTNode<?>>();
		for (FieldDecl fD : d.getFieldList()) {
			pDecl.add(fD);
		}
		for (MethodImpl mD : d.getMethodList()) {
			pDecl.add(mD);
		}
		return pDecl.toArray(EMPTY_NODES);
	}		

	/**
	 * Retrieve the Children of an InterfaceDecl
	 * @param d The target InterfaceDecl
	 * @return The children of an InterfaceDecl, which are relevant for the Content Outline
	 */
	private ASTNode<?>[] getChildrenOf(InterfaceDecl d) {
		ArrayList<ASTNode<?>> pDecl = new ArrayList<ASTNode<?>>();
		for (MethodSig mD : d.getBodys()) {
			pDecl.add(mD);
		}
		return pDecl.toArray(EMPTY_NODES);
	}
	
	/**
	 * Retrieve the Children of a MainBlock
	 * @param d The target MainBlock
	 * @return The children of a MainBlock, which are relevant for the Content Outline
	 */		
	private ASTNode<?>[] getChildrenOf(MainBlock mb){
		ArrayList<ASTNode<?>> pDecl = new ArrayList<ASTNode<?>>();
		for (VarDecl mD : mb.getVars()) {
			pDecl.add(mD);
		}
		return pDecl.toArray(EMPTY_NODES);			
	}

	/**
	 * Retrieve the Children of a DataTypeDecl 
	 * @param d The target DataTypeDecl
	 * @return The children of a DataTypeDecl, which are relevant for the Content Outline
	 */		
	private ASTNode<?>[] getChildrenOf(DataTypeDecl d){
		ArrayList<ASTNode<?>> pDecl = new ArrayList<ASTNode<?>>();
		for (DataConstructor dc : d.getDataConstructors()){
			pDecl.add(dc);
		}
		return pDecl.toArray(EMPTY_NODES);
	}
	
	private boolean hasChildren(ModuleDecl m){
		return (m.getNumDecl() > 0) || (m.getNumImport() > 0) || (m.getNumExport() > 0) || (m.getNumProduct() > 0); 
	}
	
	private boolean hasChildren(ClassDecl c){
		return (((ClassDecl) c).getNumField() > 0) || (((ClassDecl) c).getNumMethod() > 0);
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof InternalASTNode){
			return getChildren(inputElement);
		}else if (inputElement instanceof Model
				|| inputElement instanceof CompilationUnit
				|| inputElement instanceof ModuleDecl
				|| inputElement instanceof ClassDecl
				|| inputElement instanceof InterfaceDecl
				|| inputElement instanceof DataTypeDecl
				|| inputElement instanceof MainBlock
				|| inputElement instanceof List<?>
				|| inputElement instanceof IFile) {
			throw new IllegalArgumentException("No unwrapped ASTNodes should be used. Use InternalASTnodes instead.");
		} else if (inputElement instanceof Object[]) {
			return (Object[]) inputElement;
		}
		return EMPTY_OBJECT_ARRAY;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof InternalASTNode<?> && element != null){
			InternalASTNode<?> node = ((InternalASTNode<?>) element);
			synchronized(node.getNature().modelLock){
				return hasChildren(node.getASTNode());
			}
		} else if (element instanceof IFile) {
			return hasChildren((IFile) element);
		}
		return false;

	}
	
	public boolean hasChildren(ASTNode<?> element) {

		if (element instanceof ModuleDecl) {
			return hasChildren((ModuleDecl) element);
		} else if (element instanceof ClassDecl) {
			return hasChildren((ClassDecl) element);
		} else if (element instanceof InterfaceDecl) {
			return ((InterfaceDecl) element).getNumBody() > 0;
		} else if (element instanceof DataTypeDecl) {
			return ((DataTypeDecl) element).getNumDataConstructor() > 0;
		} else if (element instanceof MainBlock) {
			return ((MainBlock) element).getNumVar() > 0;
		} else if (element instanceof List<?>) {
			return hasChilden((List<?>) element);
		}
		
		return false;
	}
	
	private boolean hasChildren(IFile file){
		if(!hasABSFileExtension(file))
			return false;
		AbsNature nature = UtilityFunctions.getAbsNature(file.getProject());
		if(nature != null){
			CompilationUnit cu = nature.getCompilationUnit(file);
			if(cu != null)
				return true;
		}
		return false;
	}

	private boolean hasChilden(List<?> element) {
	    List<?> nodeList = ((List<?>) element);
	    if (ABSContentOutlineUtils.isImportList(nodeList) || ABSContentOutlineUtils.isExportList(nodeList)){
	    	return true;
	    } else{
		return false;
	    }
	}

	@Override
	public void dispose() {
		//no-op
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof InternalASTNode<?> && element != null) {
			InternalASTNode<?> node = (InternalASTNode<?>) element;
			return InternalASTNode.wrapASTNode(node.getASTNode().getParent(),node.getNature());
		}
		return null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		//no-op
	}
}