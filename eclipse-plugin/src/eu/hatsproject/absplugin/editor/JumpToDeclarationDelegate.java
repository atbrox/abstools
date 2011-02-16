package eu.hatsproject.absplugin.editor;

import java.io.PrintStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import abs.frontend.ast.ASTNode;
import abs.frontend.ast.Call;
import abs.frontend.ast.CompilationUnit;
import abs.frontend.ast.DataConstructorExp;
import abs.frontend.ast.Decl;
import abs.frontend.ast.FnApp;
import abs.frontend.ast.FromExport;
import abs.frontend.ast.FromImport;
import abs.frontend.ast.MethodSig;
import abs.frontend.ast.Name;
import abs.frontend.ast.NewExp;
import abs.frontend.ast.StarExport;
import abs.frontend.ast.StarImport;
import abs.frontend.ast.TypeUse;
import abs.frontend.ast.UnknownDecl;
import abs.frontend.ast.VarOrFieldDecl;
import abs.frontend.ast.VarOrFieldUse;
import abs.frontend.typechecker.KindedName;
import abs.frontend.typechecker.KindedName.Kind;
import abs.frontend.typechecker.Type;
import beaver.Symbol;
import eu.hatsproject.absplugin.builder.AbsNature;
import eu.hatsproject.absplugin.console.ConsoleManager;
import eu.hatsproject.absplugin.console.ConsoleManager.MessageType;
import eu.hatsproject.absplugin.util.UtilityFunctions;
import eu.hatsproject.absplugin.util.UtilityFunctions.EditorPosition;

public class JumpToDeclarationDelegate implements IEditorActionDelegate {
	private IEditorPart editor;
	
	private static final boolean doDebug = false;
	
	@Override
	public void run(IAction action) {
		if(!(editor instanceof ABSEditor)){
			return;
		}
		
		try{
			ABSEditor abseditor = (ABSEditor)editor;
			IFile file = (IFile)abseditor.getEditorInput().getAdapter(IFile.class);
			AbsNature nature = UtilityFunctions.getAbsNature(file);
			if(nature==null){
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "No ABSProject", "The file is not in an ABS project!");
				return;
			}
			
			synchronized (nature.modelLock) {
				IDocument doc = abseditor.getDocumentProvider().getDocument(abseditor.getEditorInput());
				
				CompilationUnit compunit = nature.getCompilationUnit(file);
				if(compunit==null){
					MessageDialog.openInformation(abseditor.getSite().getShell(),"Error", "AST not set!");
					return;
				}
				
				TextSelection sel = (TextSelection)abseditor.getSelectionProvider().getSelection();
				int carpos = sel.getOffset();
				ASTNode<?> node = UtilityFunctions.getASTNodeOfOffset(doc, compunit, carpos);
				
				EditorPosition pos = getPosition(compunit, node);
				if(doDebug) {
					PrintStream pstream = ConsoleManager.getDefault().getPrintStream(MessageType.MESSAGE_ERROR);
					pstream.println("Node:");
					pstream.println(node.getClass().toString());
					pstream.println("Bumping Tree:");
					node.dumpTree("", pstream);
				}
				if(pos!=null){
					ABSEditor targeteditor = UtilityFunctions.openABSEditorForFile(pos.getPath());
					if(targeteditor==null){
						MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "File not found!",
								"Could not find file "+pos.getPath().toOSString());
						return;
					}
					doc = targeteditor.getDocumentProvider().getDocument(targeteditor.getEditorInput());
					int startoff = doc.getLineOffset(pos.getLinestart()-1) + pos.getColstart()-1;
					targeteditor.getSelectionProvider().setSelection(new TextSelection(startoff,0));
				}
			}
		} catch(BadLocationException ex){
			ex.printStackTrace(ConsoleManager.getDefault().getPrintStream(MessageType.MESSAGE_ERROR));
		}
	}

	public static EditorPosition getPosition(CompilationUnit cu, ASTNode<?> node) {
		ASTNode<?> decl = null;
		if(node instanceof Decl){
			decl = (Decl)node;
		} else if(node instanceof FnApp){
			FnApp fnapp = (FnApp)node;
			String name = fnapp.getName();
			decl = fnapp.lookup(new KindedName(Kind.FUN, name));
		} else if(node instanceof VarOrFieldUse){
			VarOrFieldUse vofu = (VarOrFieldUse)node;
			String name = vofu.getName();
			decl = vofu.lookupVarOrFieldName(name,false);
		} else if(node instanceof Call){
			Call call = (Call)node;
			String mname = call.getMethod();
			Type type = call.getCallee().getType();
			decl = type.lookupMethod(mname);
		} else if(node instanceof DataConstructorExp){
			DataConstructorExp exp = (DataConstructorExp)node;
			decl = exp.getDecl();
		} else if(node instanceof VarOrFieldDecl){
			decl = node;
		} else if(node instanceof MethodSig){
			decl = node;
		} else if(node instanceof TypeUse){
			TypeUse tu = (TypeUse)node;
			decl = tu.getDecl();
		} else if(node instanceof NewExp){
			NewExp newexp = (NewExp)node;
			String classname = newexp.getClassName();
			decl = newexp.lookup(new KindedName(Kind.CLASS, classname));
		} else if(node instanceof FromImport){
			FromImport fimport = (FromImport)node;
			String moduleName = fimport.getModuleName();
			decl = fimport.lookupModule(moduleName);
		} else if(node instanceof StarImport){
			StarImport fimport = (StarImport)node;
			String moduleName = fimport.getModuleName();
			decl = fimport.lookupModule(moduleName);
		} else if(node instanceof FromExport){
			FromExport fexport = (FromExport)node;
			decl = fexport.getModuleDecl();
		} else if(node instanceof StarExport){
			StarExport fexport = (StarExport)node;
			decl = fexport.getModuleDecl();
		} else if(node instanceof Name){
			Name name = (Name)node;
			String simpleName = name.getString();
			decl = cu.lookupModule(simpleName);
		}
		
		if(decl == null || decl instanceof UnknownDecl){
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Declaration not found!", "Declaration for symbol under cursor not found!");
			return null;
		}
		
		CompilationUnit declcu = UtilityFunctions.getCompilationUnitOfASTNode(decl);
		
		int start = decl.getStartPos();
		int end = decl.getEndPos();
		
		return new EditorPosition(new Path(declcu.getFileName()), Symbol.getLine(start), Symbol.getColumn(start), Symbol.getLine(end), Symbol.getColumn(end));
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		//nothing

	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editor = targetEditor;

	}

}