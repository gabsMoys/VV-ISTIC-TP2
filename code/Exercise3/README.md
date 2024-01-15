# Code of your exercise

Put here all the code created for this exercise

Le main est légèrement modifié pour permettre l'écriture sur un fichier de sortie (n'ayant pas testé le packaging en jar j'ai laissé des chemins en dur dans le main)
```
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import com.github.javaparser.utils.SourceRoot;

public class Main {

	public static void main(String[] args) throws IOException {
		/*
		 * if (args.length == 0) {
		 * System.err.println("Should provide the path to the source code");
		 * System.exit(1); }
		 * 
		 * File file = new File(args[0]); if (!file.exists() || !file.isDirectory() ||
		 * !file.canRead()) {
		 * System.err.println("Provide a path to an existing readable directory");
		 * System.exit(2); }
		 */
		String outPath = "/home/gaby/VV/repoTP2/VV-ISTIC-TP2/noGetter.txt";
		File file = new File("/home/gaby/eclipse-workspace/VV_TP2_JP/src/main/java/com/vv/testClasse");
		PrintWriter out = new PrintWriter(new FileOutputStream(new File(outPath)));
		SourceRoot root = new SourceRoot(file.toPath());
		try {
			// PublicElementsPrinter printer = new PublicElementsPrinter();
			PrivateFieldWithoutPublicGetterPrinter visiteur = new PrivateFieldWithoutPublicGetterPrinter(out);
			root.parse("", (localPath, absolutePath, result) -> {
				result.ifSuccessful(unit -> unit.accept(visiteur, null));
				return SourceRoot.Callback.Result.DONT_SAVE;
			});
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.flush();// probablement redondant, le close fait sans doute un flush
				out.close();
			}

		}
	}

}
```

La nouvelle classe :
```
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

public class PrivateFieldWithoutPublicGetterPrinter extends VoidVisitorWithDefaults<Void> {

	private PrintWriter out;
	private List<FieldDeclaration> privateFields = new ArrayList<FieldDeclaration>();
	private String packageName;
	private String className;
	
	public PrivateFieldWithoutPublicGetterPrinter(PrintWriter out) {
		super();
		this.out = out;
	}
	
	//Les clé de cette map sont des noms de potentiels champs associé aux getter
	//Afin de ne pas avoir a parcourir tous les getters pour chaque champ privé
	//par exemple via visite des methode, on tombe sur "getTruc"
	//on  enregistre dans cette map "truc" -> getTruc
	//ensuite en parcourant privateFields, si on trouve un champ "truc", on accedera directement a la bonne entrée
	//pour determiner si le getter est valid pour le champ "truc"
	private HashMap<String,MethodDeclaration> getters = new HashMap<String,MethodDeclaration>();

	@Override
	public void visit(CompilationUnit unit, Void arg) {
		
		PackageDeclaration packageDeclaration = unit.getPackageDeclaration().orElse(null);
		if(packageDeclaration == null)
			this.packageName = "[Error Package]";
		else
			this.packageName = packageDeclaration.getNameAsString();
		
		for (TypeDeclaration<?> type : unit.getTypes()) {
			type.accept(this, null);
		}
	}

	/*
	 * Lors de a visite dune classe ou interface, on ne visite que les declarations
	 * de methodes & champs
	 */
	@Override
	public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
		//On nettoie les deux collection entre chaque classe
		privateFields = new ArrayList<FieldDeclaration>();
		getters = new HashMap<String,MethodDeclaration>();
		className = declaration.getNameAsString(); 
				
		List<BodyDeclaration<?>> decls = declaration.getMembers();
		
		if (decls != null && !decls.isEmpty())
			for (BodyDeclaration<?> bodyDecl : decls)
				if (bodyDecl.isFieldDeclaration() || bodyDecl.isMethodDeclaration())
					bodyDecl.accept(this, arg);
		
		//Après visite en profondeur, on itère sur privateFields pour écrire en sortie les champ privé nayant pas de getter
		for(FieldDeclaration fd : this.privateFields) {
			//Attention ici, fd peut en couvrir plusieurs declarations
			//exemple : private int a,b;
			//String fieldName = fd.getVariable(0).getNameAsString();
			Type declarationType = fd.getElementType();
			
			for(VariableDeclarator vd : fd.getVariables()) {
				String variableName = vd.getNameAsString();
				if(!hasAGetter(variableName,declarationType))
					this.out.println("from package : " + this.packageName + " , in class : " + this.className + " , field " + variableName + " does not have a getter");
					//System.out.println(declarationType.asString() + " " + vd.getNameAsString());
			}
				
		}
		
		this.out.flush();
	}
	
	/*
	 * Critères pour avoir un getter valide :
	 * - correspondance des noms -> se fait via check existence de clé sur getters (cf fromGetterToFieldName)
	 * - correspondance des type
	 */
	private boolean hasAGetter(String variableName, Type type) {
		//on commence par verifier quil y a bien un getter potentiel dans getters
		if(!getters.containsKey(variableName))
			return false;
		MethodDeclaration potentialGetter = getters.get(variableName);
		if(!type.equals(potentialGetter.getType()))
			return false;
		return true;
	}

	/*
	 * On se permet un simple if else car cette methode est appelée uniquement si le nom commence par "get" ou "is"
	 */
	private String fromGetterToFieldName(String getterName) {
		char firstLetter;
		if(getterName.startsWith("get")) {
			firstLetter = Character.toLowerCase(getterName.charAt(3));
			return firstLetter + getterName.substring(4);
		}else {
			firstLetter = Character.toLowerCase(getterName.charAt(2));
			return firstLetter + getterName.substring(3);
		}
	}
	
	
	private boolean validGetterName(String getterName) {
		return getterName != null
				&& (validGetName(getterName) || validIsName(getterName));
	}
	
	private boolean validGetName(String getName) {
		return getName.length() > 3 && getName.startsWith("get");
	}
	
	private boolean validIsName(String isName) {
		return isName.length() > 2 && isName.startsWith("is");
	}
	
	/*
	 * Critère pour etre un getter :
	 * - le nom de la methode doit commencer par "get" ou "is"
	 * - le nom de la methode ne doit pas uniquement etre "get" ou "is"
	 * - la methode doit être publique
	 * - la methode ne doit pas renvoyer void
	 * - la methode ne doit pas prendre de parametres
	 */
	private boolean validGetter(MethodDeclaration m) {
		if(m == null)
			return false;
		String methodName = m.getNameAsString();
		
		return (methodName != null) 
				&& validGetterName(methodName)
				&& m.isPublic()
				&& !m.getType().isVoidType()
				&& m.getParameters().isEmpty();	
	}
	
	/*
	 * Si n correspond à une declaration de getter, on l'enregistre dans "getters"
	 */
	@Override
	public void visit(MethodDeclaration m, Void arg) {
		
		if(validGetter(m)) {
			String methodName = m.getNameAsString();
			String potentialField = fromGetterToFieldName(methodName);
			this.getters.put(potentialField, m);
		}
	}

	/*
	 * Si fd est un champ privé, on l'enregistre dans privateFields
	 */
	@Override
	public void visit(FieldDeclaration fd, Void arg) {
		// TODO Auto-generated method stub
		if(fd.isPrivate())
			this.privateFields.add(fd);
	}

}
```

Classe de test exemple :
```
public class ExempleClass {

	private int a,bb;
	private ExempleClass ec;
	public int zozo;
	//private int bb;
	public String b;
	
	public int getA() {
		return this.a;
	}
	
	public void getAA(int n) {
		
	}
	
	public ExempleClass getec(int i) {
		return null;
	}
}
```
Exemple de sortie pour une classe comportant des champs privés sans getters :
```
from package : com.vv.testClasse , in class : ExempleClass , field bb does not have a getter
from package : com.vv.testClasse , in class : ExempleClass , field ec does not have a getter
```
