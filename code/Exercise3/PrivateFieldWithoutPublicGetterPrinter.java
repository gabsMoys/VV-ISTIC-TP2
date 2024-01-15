package com.vv;

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
