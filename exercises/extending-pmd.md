# Extending PMD

Use XPath to define a new rule for PMD to prevent complex code. The rule should detect the use of three or more nested `if` statements in Java programs so it can detect patterns like the following:

```Java
if (...) {
    ...
    if (...) {
        ...
        if (...) {
            ....
        }
    }

}
```
Notice that the nested `if`s may not be direct children of the outer `if`s. They may be written, for example, inside a `for` loop or any other statement.
Write below the XML definition of your rule.

You can find more information on extending PMD in the following link: https://pmd.github.io/latest/pmd_userdocs_extending_writing_rules_intro.html, as well as help for using `pmd-designer` [here](https://github.com/selabs-ur1/VV-ISTIC-TP2/blob/master/exercises/designer-help.md).

Use your rule with different projects and describe you findings below. See the [instructions](../sujet.md) for suggestions on the projects to use.

## Answer

//IfStatement[descendant::IfStatement[descendant::IfStatement and @Else = "false"] and @Else = "false"]

Voici notre ruleSet ne contenant que la regle ThreeNestedIf (le fichier est inclus dans le repo) :
```
<?xml version="1.0"?>
<ruleset name="Custom Rules"
    xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 https://pmd.sourceforge.io/ruleset_2_0_0.xsd">

    <description>
        My custom rules
    </description>


    <!-- Your rules will come here -->
    <!--<rule ref="/home/gaby/VV/repoTP2/VV-ISTIC-TP2/exercises/nestedIf.xml" />-->
    <rule name="AtLeastThreeNestedIf"
      language="java"
      message="There are 3 or more nested If Statement"
      class="net.sourceforge.pmd.lang.rule.XPathRule">
   	<description>Detect when 3 or more if statement are nested, they do not have to be directly nested</description>
   	<priority>3</priority>
 	<properties>
      		<property name="version" value="3.1"/>
      		<property name="xpath">  
      			<value>
			<![CDATA[
			//IfStatement[descendant::IfStatement[descendant::IfStatement and @Else = "false"] and @Else = "false"]
			]]>
			</value>   
     		</property>
   	</properties>
	</rule>
</ruleset>
```

Sans les test @Else = "false", on aurait des faux positifs pour du code avec des else if, comme suit :
```
public static int size(final Object object) {
        if(true){
            int i=0;
        }else if(object == null){
            int j=0;
        }else if(false){
            int z=0;
        }
        return 0;
    }
```

1) Application de la regle sur le projet https://github.com/apache/commons-collections
commande : ```pmd check -f text -R /home/gaby/VV/repoTP2/VV-ISTIC-TP2/exercises/myRuleSet.xml -d /home/gaby/VV/input_for_exo_2/commons-collections-master/src/main/java/org/apache/commons/collections4```

resultats :
/home/gaby/VV/input_for_exo_2/commons-collections-master/src/main/java/org/apache/commons/collections4/MapUtils.java:226:	AtLeastThreeNestedIf:	There are 3 or more nested If Statement
/home/gaby/VV/input_for_exo_2/commons-collections-master/src/main/java/org/apache/commons/collections4/MapUtils.java:926:	AtLeastThreeNestedIf:	There are 3 or more nested If Statement
/home/gaby/VV/input_for_exo_2/commons-collections-master/src/main/java/org/apache/commons/collections4/comparators/ComparatorChain.java:280:	AtLeastThreeNestedIf:	There are 3 or more nested If Statement
/home/gaby/VV/input_for_exo_2/commons-collections-master/src/main/java/org/apache/commons/collections4/map/CompositeMap.java:137:	AtLeastThreeNestedIf:	There are 3 or more nested If Statement
/home/gaby/VV/input_for_exo_2/commons-collections-master/src/main/java/org/apache/commons/collections4/set/CompositeSet.java:372:	AtLeastThreeNestedIf:	There are 3 or more nested If Statement
/home/gaby/VV/input_for_exo_2/commons-collections-master/src/main/java/org/apache/commons/collections4/trie/AbstractPatriciaTrie.java:163:	AtLeastThreeNestedIf:	There are 3 or more nested If Statement
/home/gaby/VV/input_for_exo_2/commons-collections-master/src/main/java/org/apache/commons/collections4/trie/AbstractPatriciaTrie.java:887:	AtLeastThreeNestedIf:	There are 3 or more nested If Statement
/home/gaby/VV/input_for_exo_2/commons-collections-master/src/main/java/org/apache/commons/collections4/trie/AbstractPatriciaTrie.java:1217:	AtLeastThreeNestedIf:	There are 3 or more nested If Statement

Après examinations du code pour pour chacun des resultats, il n'y a que des vrais positifs.


